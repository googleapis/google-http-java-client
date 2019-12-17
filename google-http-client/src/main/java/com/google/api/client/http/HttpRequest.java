/*
 * Copyright (c) 2010 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package com.google.api.client.http;

import com.google.api.client.util.Beta;
import com.google.api.client.util.LoggingStreamingContent;
import com.google.api.client.util.ObjectParser;
import com.google.api.client.util.Preconditions;
import com.google.api.client.util.Sleeper;
import com.google.api.client.util.StreamingContent;
import com.google.api.client.util.StringUtils;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import io.opencensus.common.Scope;
import io.opencensus.contrib.http.util.HttpTraceAttributeConstants;
import io.opencensus.trace.AttributeValue;
import io.opencensus.trace.Span;
import io.opencensus.trace.Tracer;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.concurrent.Callable;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * HTTP request.
 *
 * <p>Implementation is not thread-safe.
 *
 * @since 1.0
 * @author Yaniv Inbar
 */
public final class HttpRequest {

  /**
   * Current version of the Google API Client Library for Java.
   *
   * @since 1.8
   */
  public static final String VERSION = getVersion();

  /**
   * User agent suffix for all requests.
   *
   * <p>Includes a {@code "(gzip)"} suffix in case the server -- as Google's servers may do --
   * checks the {@code User-Agent} header to try to detect if the client accepts gzip-encoded
   * responses.
   *
   * @since 1.4
   */
  public static final String USER_AGENT_SUFFIX = "Google-HTTP-Java-Client/" + VERSION + " (gzip)";

  /**
   * The default number of retries that will be allowed to execute before the request will be
   * terminated.
   *
   * @see #getNumberOfRetries
   * @since 1.22
   */
  public static final int DEFAULT_NUMBER_OF_RETRIES = 10;

  /**
   * HTTP request execute interceptor to intercept the start of {@link #execute()} (before executing
   * the HTTP request) or {@code null} for none.
   */
  private HttpExecuteInterceptor executeInterceptor;

  /** HTTP request headers. */
  private HttpHeaders headers = new HttpHeaders();

  /**
   * HTTP response headers.
   *
   * <p>For example, this can be used if you want to use a subclass of {@link HttpHeaders} called
   * MyHeaders to process the response:
   *
   * <pre>
   * static String executeAndGetValueOfSomeCustomHeader(HttpRequest request) {
   * MyHeaders responseHeaders = new MyHeaders();
   * request.responseHeaders = responseHeaders;
   * HttpResponse response = request.execute();
   * return responseHeaders.someCustomHeader;
   * }
   * </pre>
   */
  private HttpHeaders responseHeaders = new HttpHeaders();

  /**
   * The number of retries that will be allowed to execute before the request will be terminated or
   * {@code 0} to not retry requests. Retries occur as a result of either {@link
   * HttpUnsuccessfulResponseHandler} or {@link HttpIOExceptionHandler} which handles abnormal HTTP
   * response or the I/O exception.
   */
  private int numRetries = DEFAULT_NUMBER_OF_RETRIES;

  /**
   * Determines the limit to the content size that will be logged during {@link #execute()}.
   *
   * <p>Content will only be logged if {@link #isLoggingEnabled} is {@code true}.
   *
   * <p>If the content size is greater than this limit then it will not be logged.
   *
   * <p>Can be set to {@code 0} to disable content logging. This is useful for example if content
   * has sensitive data such as authentication information.
   *
   * <p>Defaults to 16KB.
   */
  private int contentLoggingLimit = 0x4000;

  /** Determines whether logging should be enabled for this request. Defaults to {@code true}. */
  private boolean loggingEnabled = true;

  /** Determines whether logging in form of curl commands should be enabled for this request. */
  private boolean curlLoggingEnabled = true;

  /** HTTP request content or {@code null} for none. */
  private HttpContent content;

  /** HTTP transport. */
  private final HttpTransport transport;

  /** HTTP request method or {@code null} for none. */
  private String requestMethod;

  /** HTTP request URL. */
  private GenericUrl url;

  /** Timeout in milliseconds to establish a connection or {@code 0} for an infinite timeout. */
  private int connectTimeout = 20 * 1000;

  /**
   * Timeout in milliseconds to read data from an established connection or {@code 0} for an
   * infinite timeout.
   */
  private int readTimeout = 20 * 1000;

  /** Timeout in milliseconds to set POST/PUT data or {@code 0} for an infinite timeout. */
  private int writeTimeout = 0;

  /** HTTP unsuccessful (non-2XX) response handler or {@code null} for none. */
  private HttpUnsuccessfulResponseHandler unsuccessfulResponseHandler;

  /** HTTP I/O exception handler or {@code null} for none. */
  @Beta private HttpIOExceptionHandler ioExceptionHandler;

  /** HTTP response interceptor or {@code null} for none. */
  private HttpResponseInterceptor responseInterceptor;

  /** Parser used to parse responses. */
  private ObjectParser objectParser;

  /** HTTP content encoding or {@code null} for none. */
  private HttpEncoding encoding;

  /** The {@link BackOffPolicy} to use between retry attempts or {@code null} for none. */
  @Deprecated @Beta private BackOffPolicy backOffPolicy;

  /** Whether to automatically follow redirects ({@code true} by default). */
  private boolean followRedirects = true;

  /** Whether to use raw redirect URLs ({@code false} by default). */
  private boolean useRawRedirectUrls = false;

  /**
   * Whether to throw an exception at the end of {@link #execute()} on an HTTP error code (non-2XX)
   * after all retries and response handlers have been exhausted ({@code true} by default).
   */
  private boolean throwExceptionOnExecuteError = true;

  /**
   * Whether to retry the request if an {@link IOException} is encountered in {@link
   * LowLevelHttpRequest#execute()}.
   */
  @Deprecated @Beta private boolean retryOnExecuteIOException = false;

  /**
   * Whether to not add the suffix {@link #USER_AGENT_SUFFIX} to the User-Agent header.
   *
   * <p>It is {@code false} by default.
   */
  private boolean suppressUserAgentSuffix;

  /** Sleeper. */
  private Sleeper sleeper = Sleeper.DEFAULT;

  /** OpenCensus tracing component. */
  private final Tracer tracer = OpenCensusUtils.getTracer();

  /**
   * Determines whether {@link HttpResponse#getContent()} of this request should return raw input
   * stream or not.
   *
   * <p>It is {@code false} by default.
   */
  private boolean responseReturnRawInputStream = false;

  /**
   * @param transport HTTP transport
   * @param requestMethod HTTP request method or {@code null} for none
   */
  HttpRequest(HttpTransport transport, String requestMethod) {
    this.transport = transport;
    setRequestMethod(requestMethod);
  }

  /**
   * Returns the HTTP transport.
   *
   * @since 1.5
   */
  public HttpTransport getTransport() {
    return transport;
  }

  /**
   * Returns the HTTP request method or {@code null} for none.
   *
   * @since 1.12
   */
  public String getRequestMethod() {
    return requestMethod;
  }

  /**
   * Sets the HTTP request method or {@code null} for none.
   *
   * @since 1.12
   */
  public HttpRequest setRequestMethod(String requestMethod) {
    Preconditions.checkArgument(requestMethod == null || HttpMediaType.matchesToken(requestMethod));
    this.requestMethod = requestMethod;
    return this;
  }

  /**
   * Returns the HTTP request URL.
   *
   * @since 1.5
   */
  public GenericUrl getUrl() {
    return url;
  }

  /**
   * Sets the HTTP request URL.
   *
   * @since 1.5
   */
  public HttpRequest setUrl(GenericUrl url) {
    this.url = Preconditions.checkNotNull(url);
    return this;
  }

  /**
   * Returns the HTTP request content or {@code null} for none.
   *
   * @since 1.5
   */
  public HttpContent getContent() {
    return content;
  }

  /**
   * Sets the HTTP request content or {@code null} for none.
   *
   * @since 1.5
   */
  public HttpRequest setContent(HttpContent content) {
    this.content = content;
    return this;
  }

  /**
   * Returns the HTTP content encoding or {@code null} for none.
   *
   * @since 1.14
   */
  public HttpEncoding getEncoding() {
    return encoding;
  }

  /**
   * Sets the HTTP content encoding or {@code null} for none.
   *
   * @since 1.14
   */
  public HttpRequest setEncoding(HttpEncoding encoding) {
    this.encoding = encoding;
    return this;
  }

  /**
   * {@link Beta} <br>
   * Returns the {@link BackOffPolicy} to use between retry attempts or {@code null} for none.
   *
   * @since 1.7
   * @deprecated (scheduled to be removed in 1.18). {@link
   *     #setUnsuccessfulResponseHandler(HttpUnsuccessfulResponseHandler)} with a new {@link
   *     HttpBackOffUnsuccessfulResponseHandler} instead.
   */
  @Deprecated
  @Beta
  public BackOffPolicy getBackOffPolicy() {
    return backOffPolicy;
  }

  /**
   * {@link Beta} <br>
   * Sets the {@link BackOffPolicy} to use between retry attempts or {@code null} for none.
   *
   * @since 1.7
   * @deprecated (scheduled to be removed in 1.18). Use {@link
   *     #setUnsuccessfulResponseHandler(HttpUnsuccessfulResponseHandler)} with a new {@link
   *     HttpBackOffUnsuccessfulResponseHandler} instead.
   */
  @Deprecated
  @Beta
  public HttpRequest setBackOffPolicy(BackOffPolicy backOffPolicy) {
    this.backOffPolicy = backOffPolicy;
    return this;
  }

  /**
   * Returns the limit to the content size that will be logged during {@link #execute()}.
   *
   * <p>If the content size is greater than this limit then it will not be logged.
   *
   * <p>Content will only be logged if {@link #isLoggingEnabled} is {@code true}.
   *
   * <p>Can be set to {@code 0} to disable content logging. This is useful for example if content
   * has sensitive data such as authentication information.
   *
   * <p>Defaults to 16KB.
   *
   * @since 1.7
   */
  public int getContentLoggingLimit() {
    return contentLoggingLimit;
  }

  /**
   * Set the limit to the content size that will be logged during {@link #execute()}.
   *
   * <p>If the content size is greater than this limit then it will not be logged.
   *
   * <p>Content will only be logged if {@link #isLoggingEnabled} is {@code true}.
   *
   * <p>Can be set to {@code 0} to disable content logging. This is useful for example if content
   * has sensitive data such as authentication information.
   *
   * <p>Defaults to 16KB.
   *
   * @since 1.7
   */
  public HttpRequest setContentLoggingLimit(int contentLoggingLimit) {
    Preconditions.checkArgument(
        contentLoggingLimit >= 0, "The content logging limit must be non-negative.");
    this.contentLoggingLimit = contentLoggingLimit;
    return this;
  }

  /**
   * Returns whether logging should be enabled for this request.
   *
   * <p>Defaults to {@code true}.
   *
   * @since 1.9
   */
  public boolean isLoggingEnabled() {
    return loggingEnabled;
  }

  /**
   * Sets whether logging should be enabled for this request.
   *
   * <p>Defaults to {@code true}.
   *
   * @since 1.9
   */
  public HttpRequest setLoggingEnabled(boolean loggingEnabled) {
    this.loggingEnabled = loggingEnabled;
    return this;
  }

  /**
   * Returns whether logging in form of curl commands is enabled for this request.
   *
   * @since 1.11
   */
  public boolean isCurlLoggingEnabled() {
    return curlLoggingEnabled;
  }

  /**
   * Sets whether logging in form of curl commands should be enabled for this request.
   *
   * <p>Defaults to {@code true}.
   *
   * @since 1.11
   */
  public HttpRequest setCurlLoggingEnabled(boolean curlLoggingEnabled) {
    this.curlLoggingEnabled = curlLoggingEnabled;
    return this;
  }

  /**
   * Returns the timeout in milliseconds to establish a connection or {@code 0} for an infinite
   * timeout.
   *
   * @since 1.5
   */
  public int getConnectTimeout() {
    return connectTimeout;
  }

  /**
   * Sets the timeout in milliseconds to establish a connection or {@code 0} for an infinite
   * timeout.
   *
   * <p>By default it is 20000 (20 seconds).
   *
   * @since 1.5
   */
  public HttpRequest setConnectTimeout(int connectTimeout) {
    Preconditions.checkArgument(connectTimeout >= 0);
    this.connectTimeout = connectTimeout;
    return this;
  }

  /**
   * Returns the timeout in milliseconds to read data from an established connection or {@code 0}
   * for an infinite timeout.
   *
   * <p>By default it is 20000 (20 seconds).
   *
   * @since 1.5
   */
  public int getReadTimeout() {
    return readTimeout;
  }

  /**
   * Sets the timeout in milliseconds to read data from an established connection or {@code 0} for
   * an infinite timeout.
   *
   * @since 1.5
   */
  public HttpRequest setReadTimeout(int readTimeout) {
    Preconditions.checkArgument(readTimeout >= 0);
    this.readTimeout = readTimeout;
    return this;
  }

  /**
   * Returns the timeout in milliseconds to send POST/PUT data or {@code 0} for an infinite timeout.
   *
   * <p>By default it is 0 (infinite).
   *
   * @since 1.27
   */
  public int getWriteTimeout() {
    return writeTimeout;
  }

  /**
   * Sets the timeout in milliseconds to send POST/PUT data or {@code 0} for an infinite timeout.
   *
   * @since 1.27
   */
  public HttpRequest setWriteTimeout(int writeTimeout) {
    Preconditions.checkArgument(writeTimeout >= 0);
    this.writeTimeout = writeTimeout;
    return this;
  }

  /**
   * Returns the HTTP request headers.
   *
   * @since 1.5
   */
  public HttpHeaders getHeaders() {
    return headers;
  }

  /**
   * Sets the HTTP request headers.
   *
   * <p>By default, this is a new unmodified instance of {@link HttpHeaders}.
   *
   * @since 1.5
   */
  public HttpRequest setHeaders(HttpHeaders headers) {
    this.headers = Preconditions.checkNotNull(headers);
    return this;
  }

  /**
   * Returns the HTTP response headers.
   *
   * @since 1.5
   */
  public HttpHeaders getResponseHeaders() {
    return responseHeaders;
  }

  /**
   * Sets the HTTP response headers.
   *
   * <p>By default, this is a new unmodified instance of {@link HttpHeaders}.
   *
   * <p>For example, this can be used if you want to use a subclass of {@link HttpHeaders} called
   * MyHeaders to process the response:
   *
   * <pre>
   * static String executeAndGetValueOfSomeCustomHeader(HttpRequest request) {
   * MyHeaders responseHeaders = new MyHeaders();
   * request.responseHeaders = responseHeaders;
   * HttpResponse response = request.execute();
   * return responseHeaders.someCustomHeader;
   * }
   * </pre>
   *
   * @since 1.5
   */
  public HttpRequest setResponseHeaders(HttpHeaders responseHeaders) {
    this.responseHeaders = Preconditions.checkNotNull(responseHeaders);
    return this;
  }

  /**
   * Returns the HTTP request execute interceptor to intercept the start of {@link #execute()}
   * (before executing the HTTP request) or {@code null} for none.
   *
   * @since 1.5
   */
  public HttpExecuteInterceptor getInterceptor() {
    return executeInterceptor;
  }

  /**
   * Sets the HTTP request execute interceptor to intercept the start of {@link #execute()} (before
   * executing the HTTP request) or {@code null} for none.
   *
   * @since 1.5
   */
  public HttpRequest setInterceptor(HttpExecuteInterceptor interceptor) {
    this.executeInterceptor = interceptor;
    return this;
  }

  /**
   * Returns the HTTP unsuccessful (non-2XX) response handler or {@code null} for none.
   *
   * @since 1.5
   */
  public HttpUnsuccessfulResponseHandler getUnsuccessfulResponseHandler() {
    return unsuccessfulResponseHandler;
  }

  /**
   * Sets the HTTP unsuccessful (non-2XX) response handler or {@code null} for none.
   *
   * @since 1.5
   */
  public HttpRequest setUnsuccessfulResponseHandler(
      HttpUnsuccessfulResponseHandler unsuccessfulResponseHandler) {
    this.unsuccessfulResponseHandler = unsuccessfulResponseHandler;
    return this;
  }

  /**
   * {@link Beta} <br>
   * Returns the HTTP I/O exception handler or {@code null} for none.
   *
   * @since 1.15
   */
  @Beta
  public HttpIOExceptionHandler getIOExceptionHandler() {
    return ioExceptionHandler;
  }

  /**
   * {@link Beta} <br>
   * Sets the HTTP I/O exception handler or {@code null} for none.
   *
   * @since 1.15
   */
  @Beta
  public HttpRequest setIOExceptionHandler(HttpIOExceptionHandler ioExceptionHandler) {
    this.ioExceptionHandler = ioExceptionHandler;
    return this;
  }

  /**
   * Returns the HTTP response interceptor or {@code null} for none.
   *
   * @since 1.13
   */
  public HttpResponseInterceptor getResponseInterceptor() {
    return responseInterceptor;
  }

  /**
   * Sets the HTTP response interceptor or {@code null} for none.
   *
   * @since 1.13
   */
  public HttpRequest setResponseInterceptor(HttpResponseInterceptor responseInterceptor) {
    this.responseInterceptor = responseInterceptor;
    return this;
  }

  /**
   * Returns the number of retries that will be allowed to execute before the request will be
   * terminated or {@code 0} to not retry requests. Retries occur as a result of either {@link
   * HttpUnsuccessfulResponseHandler} or {@link HttpIOExceptionHandler} which handles abnormal HTTP
   * response or the I/O exception.
   *
   * @since 1.5
   */
  public int getNumberOfRetries() {
    return numRetries;
  }

  /**
   * Sets the number of retries that will be allowed to execute before the request will be
   * terminated or {@code 0} to not retry requests. Retries occur as a result of either {@link
   * HttpUnsuccessfulResponseHandler} or {@link HttpIOExceptionHandler} which handles abnormal HTTP
   * response or the I/O exception.
   *
   * <p>The default value is {@link #DEFAULT_NUMBER_OF_RETRIES}.
   *
   * @since 1.5
   */
  public HttpRequest setNumberOfRetries(int numRetries) {
    Preconditions.checkArgument(numRetries >= 0);
    this.numRetries = numRetries;
    return this;
  }

  /**
   * Sets the {@link ObjectParser} used to parse the response to this request or {@code null} for
   * none.
   *
   * <p>This parser will be preferred over any registered HttpParser.
   *
   * @since 1.10
   */
  public HttpRequest setParser(ObjectParser parser) {
    this.objectParser = parser;
    return this;
  }

  /**
   * Returns the {@link ObjectParser} used to parse the response or {@code null} for none.
   *
   * @since 1.10
   */
  public final ObjectParser getParser() {
    return objectParser;
  }

  /**
   * Returns whether to follow redirects automatically.
   *
   * @since 1.6
   */
  public boolean getFollowRedirects() {
    return followRedirects;
  }

  /**
   * Sets whether to follow redirects automatically.
   *
   * <p>The default value is {@code true}.
   *
   * @since 1.6
   */
  public HttpRequest setFollowRedirects(boolean followRedirects) {
    this.followRedirects = followRedirects;
    return this;
  }

  /** Return whether to use raw redirect URLs. */
  public boolean getUseRawRedirectUrls() {
    return useRawRedirectUrls;
  }

  /**
   * Sets whether to use raw redirect URLs.
   *
   * <p>The default value is {@code false}.
   */
  public HttpRequest setUseRawRedirectUrls(boolean useRawRedirectUrls) {
    this.useRawRedirectUrls = useRawRedirectUrls;
    return this;
  }

  /**
   * Returns whether to throw an exception at the end of {@link #execute()} on an HTTP error code
   * (non-2XX) after all retries and response handlers have been exhausted.
   *
   * @since 1.7
   */
  public boolean getThrowExceptionOnExecuteError() {
    return throwExceptionOnExecuteError;
  }

  /**
   * Sets whether to throw an exception at the end of {@link #execute()} on a HTTP error code
   * (non-2XX) after all retries and response handlers have been exhausted.
   *
   * <p>The default value is {@code true}.
   *
   * @since 1.7
   */
  public HttpRequest setThrowExceptionOnExecuteError(boolean throwExceptionOnExecuteError) {
    this.throwExceptionOnExecuteError = throwExceptionOnExecuteError;
    return this;
  }

  /**
   * {@link Beta} <br>
   * Returns whether to retry the request if an {@link IOException} is encountered in {@link
   * LowLevelHttpRequest#execute()}.
   *
   * @since 1.9
   * @deprecated (scheduled to be removed in 1.18) Use {@link
   *     #setIOExceptionHandler(HttpIOExceptionHandler)} instead.
   */
  @Deprecated
  @Beta
  public boolean getRetryOnExecuteIOException() {
    return retryOnExecuteIOException;
  }

  /**
   * {@link Beta} <br>
   * Sets whether to retry the request if an {@link IOException} is encountered in {@link
   * LowLevelHttpRequest#execute()}.
   *
   * <p>The default value is {@code false}.
   *
   * @since 1.9
   * @deprecated (scheduled to be removed in 1.18) Use {@link
   *     #setIOExceptionHandler(HttpIOExceptionHandler)} instead.
   */
  @Deprecated
  @Beta
  public HttpRequest setRetryOnExecuteIOException(boolean retryOnExecuteIOException) {
    this.retryOnExecuteIOException = retryOnExecuteIOException;
    return this;
  }

  /**
   * Returns whether to not add the suffix {@link #USER_AGENT_SUFFIX} to the User-Agent header.
   *
   * @since 1.11
   */
  public boolean getSuppressUserAgentSuffix() {
    return suppressUserAgentSuffix;
  }

  /**
   * Sets whether to not add the suffix {@link #USER_AGENT_SUFFIX} to the User-Agent header.
   *
   * <p>The default value is {@code false}.
   *
   * @since 1.11
   */
  public HttpRequest setSuppressUserAgentSuffix(boolean suppressUserAgentSuffix) {
    this.suppressUserAgentSuffix = suppressUserAgentSuffix;
    return this;
  }

  /**
   * Returns whether {@link HttpResponse#getContent()} should return raw input stream for this
   * request.
   *
   * @since 1.29
   */
  public boolean getResponseReturnRawInputStream() {
    return responseReturnRawInputStream;
  }

  /**
   * Sets whether {@link HttpResponse#getContent()} should return raw input stream for this request.
   *
   * <p>The default value is {@code false}.
   *
   * @since 1.29
   */
  public HttpRequest setResponseReturnRawInputStream(boolean responseReturnRawInputStream) {
    this.responseReturnRawInputStream = responseReturnRawInputStream;
    return this;
  }

  /**
   * Execute the HTTP request and returns the HTTP response.
   *
   * <p>Note that regardless of the returned status code, the HTTP response content has not been
   * parsed yet, and must be parsed by the calling code.
   *
   * <p>Note that when calling to this method twice or more, the state of this HTTP request object
   * isn't cleared, so the request will continue where it was left. For example, the state of the
   * {@link HttpUnsuccessfulResponseHandler} attached to this HTTP request will remain the same as
   * it was left after last execute.
   *
   * <p>Almost all details of the request and response are logged if {@link Level#CONFIG} is
   * loggable. The only exception is the value of the {@code Authorization} header which is only
   * logged if {@link Level#ALL} is loggable.
   *
   * <p>Callers should call {@link HttpResponse#disconnect} when the returned HTTP response object
   * is no longer needed. However, {@link HttpResponse#disconnect} does not have to be called if the
   * response stream is properly closed. Example usage:
   *
   * <pre>
   * HttpResponse response = request.execute();
   * try {
   * // process the HTTP response object
   * } finally {
   * response.disconnect();
   * }
   * </pre>
   *
   * @return HTTP response for an HTTP success response (or HTTP error response if {@link
   *     #getThrowExceptionOnExecuteError()} is {@code false})
   * @throws HttpResponseException for an HTTP error response (only if {@link
   *     #getThrowExceptionOnExecuteError()} is {@code true})
   * @see HttpResponse#isSuccessStatusCode()
   */
  @SuppressWarnings("deprecation")
  public HttpResponse execute() throws IOException {
    boolean retryRequest = false;
    Preconditions.checkArgument(numRetries >= 0);
    int retriesRemaining = numRetries;
    if (backOffPolicy != null) {
      // Reset the BackOffPolicy at the start of each execute.
      backOffPolicy.reset();
    }
    HttpResponse response = null;
    IOException executeException;

    Preconditions.checkNotNull(requestMethod);
    Preconditions.checkNotNull(url);

    Span span =
        tracer
            .spanBuilder(OpenCensusUtils.SPAN_NAME_HTTP_REQUEST_EXECUTE)
            .setRecordEvents(OpenCensusUtils.isRecordEvent())
            .startSpan();
    do {
      span.addAnnotation("retry #" + (numRetries - retriesRemaining));
      // Cleanup any unneeded response from a previous iteration
      if (response != null) {
        response.ignore();
      }

      response = null;
      executeException = null;

      // run the interceptor
      if (executeInterceptor != null) {
        executeInterceptor.intercept(this);
      }
      // build low-level HTTP request
      String urlString = url.build();
      addSpanAttribute(span, HttpTraceAttributeConstants.HTTP_METHOD, requestMethod);
      addSpanAttribute(span, HttpTraceAttributeConstants.HTTP_HOST, url.getHost());
      addSpanAttribute(span, HttpTraceAttributeConstants.HTTP_PATH, url.getRawPath());
      addSpanAttribute(span, HttpTraceAttributeConstants.HTTP_URL, urlString);

      LowLevelHttpRequest lowLevelHttpRequest = transport.buildRequest(requestMethod, urlString);
      Logger logger = HttpTransport.LOGGER;
      boolean loggable = loggingEnabled && logger.isLoggable(Level.CONFIG);
      StringBuilder logbuf = null;
      StringBuilder curlbuf = null;
      // log method and URL
      if (loggable) {
        logbuf = new StringBuilder();
        logbuf.append("-------------- REQUEST  --------------").append(StringUtils.LINE_SEPARATOR);
        logbuf
            .append(requestMethod)
            .append(' ')
            .append(urlString)
            .append(StringUtils.LINE_SEPARATOR);

        // setup curl logging
        if (curlLoggingEnabled) {
          curlbuf = new StringBuilder("curl -v --compressed");
          if (!requestMethod.equals(HttpMethods.GET)) {
            curlbuf.append(" -X ").append(requestMethod);
          }
        }
      }
      // add to user agent
      String originalUserAgent = headers.getUserAgent();
      if (!suppressUserAgentSuffix) {
        if (originalUserAgent == null) {
          headers.setUserAgent(USER_AGENT_SUFFIX);
          addSpanAttribute(span, HttpTraceAttributeConstants.HTTP_USER_AGENT, USER_AGENT_SUFFIX);
        } else {
          String newUserAgent = originalUserAgent + " " + USER_AGENT_SUFFIX;
          headers.setUserAgent(newUserAgent);
          addSpanAttribute(span, HttpTraceAttributeConstants.HTTP_USER_AGENT, newUserAgent);
        }
      }
      OpenCensusUtils.propagateTracingContext(span, headers);

      // headers
      HttpHeaders.serializeHeaders(headers, logbuf, curlbuf, logger, lowLevelHttpRequest);
      if (!suppressUserAgentSuffix) {
        // set the original user agent back so that retries do not keep appending to it
        headers.setUserAgent(originalUserAgent);
      }

      // content
      StreamingContent streamingContent = content;
      final boolean contentRetrySupported = streamingContent == null || content.retrySupported();
      if (streamingContent != null) {
        final String contentEncoding;
        long contentLength = -1;
        final String contentType = content.getType();
        // log content
        if (loggable) {
          streamingContent =
              new LoggingStreamingContent(
                  streamingContent, HttpTransport.LOGGER, Level.CONFIG, contentLoggingLimit);
        }
        // encoding
        if (encoding == null) {
          contentEncoding = null;
          contentLength = content.getLength();
        } else {
          contentEncoding = encoding.getName();
          streamingContent = new HttpEncodingStreamingContent(streamingContent, encoding);
        }
        // append content headers to log buffer
        if (loggable) {
          if (contentType != null) {
            String header = "Content-Type: " + contentType;
            logbuf.append(header).append(StringUtils.LINE_SEPARATOR);
            if (curlbuf != null) {
              curlbuf.append(" -H '" + header + "'");
            }
          }
          if (contentEncoding != null) {
            String header = "Content-Encoding: " + contentEncoding;
            logbuf.append(header).append(StringUtils.LINE_SEPARATOR);
            if (curlbuf != null) {
              curlbuf.append(" -H '" + header + "'");
            }
          }
          if (contentLength >= 0) {
            String header = "Content-Length: " + contentLength;
            logbuf.append(header).append(StringUtils.LINE_SEPARATOR);
            // do not log @ curl as the user will most likely manipulate the content
          }
        }
        if (curlbuf != null) {
          curlbuf.append(" -d '@-'");
        }
        // send content information to low-level HTTP request
        lowLevelHttpRequest.setContentType(contentType);
        lowLevelHttpRequest.setContentEncoding(contentEncoding);
        lowLevelHttpRequest.setContentLength(contentLength);
        lowLevelHttpRequest.setStreamingContent(streamingContent);
      }
      // log from buffer
      if (loggable) {
        logger.config(logbuf.toString());
        if (curlbuf != null) {
          curlbuf.append(" -- '");
          curlbuf.append(urlString.replaceAll("\'", "'\"'\"'"));
          curlbuf.append("'");
          if (streamingContent != null) {
            curlbuf.append(" << $$$");
          }
          logger.config(curlbuf.toString());
        }
      }

      // We need to make sure our content type can support retry
      // null content is inherently able to be retried
      retryRequest = contentRetrySupported && retriesRemaining > 0;

      // execute
      lowLevelHttpRequest.setTimeout(connectTimeout, readTimeout);
      lowLevelHttpRequest.setWriteTimeout(writeTimeout);

      // switch tracing scope to current span
      @SuppressWarnings("MustBeClosedChecker")
      Scope ws = tracer.withSpan(span);
      OpenCensusUtils.recordSentMessageEvent(span, lowLevelHttpRequest.getContentLength());
      try {
        LowLevelHttpResponse lowLevelHttpResponse = lowLevelHttpRequest.execute();
        if (lowLevelHttpResponse != null) {
          OpenCensusUtils.recordReceivedMessageEvent(span, lowLevelHttpResponse.getContentLength());
        }
        // Flag used to indicate if an exception is thrown before the response is constructed.
        boolean responseConstructed = false;
        try {
          response = new HttpResponse(this, lowLevelHttpResponse);
          responseConstructed = true;
        } finally {
          if (!responseConstructed) {
            InputStream lowLevelContent = lowLevelHttpResponse.getContent();
            if (lowLevelContent != null) {
              lowLevelContent.close();
            }
          }
        }
      } catch (IOException e) {
        if (!retryOnExecuteIOException
            && (ioExceptionHandler == null
                || !ioExceptionHandler.handleIOException(this, retryRequest))) {
          // static analysis shows response is always null here
          span.end(OpenCensusUtils.getEndSpanOptions(null));
          throw e;
        }
        // Save the exception in case the retries do not work and we need to re-throw it later.
        executeException = e;
        if (loggable) {
          logger.log(Level.WARNING, "exception thrown while executing request", e);
        }
      } finally {
        ws.close();
      }

      // Flag used to indicate if an exception is thrown before the response has completed
      // processing.
      boolean responseProcessed = false;
      try {
        if (response != null && !response.isSuccessStatusCode()) {
          boolean errorHandled = false;
          if (unsuccessfulResponseHandler != null) {
            // Even if we don't have the potential to retry, we might want to run the
            // handler to fix conditions (like expired tokens) that might cause us
            // trouble on our next request
            errorHandled = unsuccessfulResponseHandler.handleResponse(this, response, retryRequest);
          }
          if (!errorHandled) {
            if (handleRedirect(response.getStatusCode(), response.getHeaders())) {
              // The unsuccessful request's error could not be handled and it is a redirect request.
              errorHandled = true;
            } else if (retryRequest
                && backOffPolicy != null
                && backOffPolicy.isBackOffRequired(response.getStatusCode())) {
              // The unsuccessful request's error could not be handled and should be backed off
              // before retrying
              long backOffTime = backOffPolicy.getNextBackOffMillis();
              if (backOffTime != BackOffPolicy.STOP) {
                try {
                  sleeper.sleep(backOffTime);
                } catch (InterruptedException exception) {
                  // ignore
                }
                errorHandled = true;
              }
            }
          }
          // A retry is required if the error was successfully handled or if it is a redirect
          // request or if the back off policy determined a retry is necessary.
          retryRequest &= errorHandled;
          // need to close the response stream before retrying a request
          if (retryRequest) {
            response.ignore();
          }
        } else {
          // Retry is not required for a successful status code unless the response is null.
          retryRequest &= (response == null);
        }
        // Once there are no more retries remaining, this will be -1
        // Count redirects as retries, we want a finite limit of redirects.
        retriesRemaining--;

        responseProcessed = true;
      } finally {
        if (response != null && !responseProcessed) {
          response.disconnect();
        }
      }
    } while (retryRequest);
    span.end(OpenCensusUtils.getEndSpanOptions(response == null ? null : response.getStatusCode()));

    if (response == null) {
      // Retries did not help resolve the execute exception, re-throw it.
      throw executeException;
    }
    // response interceptor
    if (responseInterceptor != null) {
      responseInterceptor.interceptResponse(response);
    }
    // throw an exception if unsuccessful response
    if (throwExceptionOnExecuteError && !response.isSuccessStatusCode()) {
      try {
        throw new HttpResponseException(response);
      } finally {
        response.disconnect();
      }
    }
    return response;
  }

  /**
   * {@link Beta} <br>
   * Executes this request asynchronously in a single separate thread using the supplied executor.
   *
   * @param executor executor to run the asynchronous request
   * @return future for accessing the HTTP response
   * @since 1.13
   */
  @Beta
  public Future<HttpResponse> executeAsync(Executor executor) {
    FutureTask<HttpResponse> future =
        new FutureTask<HttpResponse>(
            new Callable<HttpResponse>() {

              public HttpResponse call() throws Exception {
                return execute();
              }
            });
    executor.execute(future);
    return future;
  }

  /**
   * {@link Beta} <br>
   * Executes this request asynchronously using {@link #executeAsync(Executor)} in a single separate
   * thread using {@link Executors#newFixedThreadPool(int)}.
   *
   * @return A future for accessing the results of the asynchronous request.
   * @since 1.13
   */
  @Beta
  public Future<HttpResponse> executeAsync() {
    return executeAsync(
        Executors.newFixedThreadPool(1, new ThreadFactoryBuilder().setDaemon(true).build()));
  }

  /**
   * Sets up this request object to handle the necessary redirect if redirects are turned on, it is
   * a redirect status code and the header has a location.
   *
   * <p>When the status code is {@code 303} the method on the request is changed to a GET as per the
   * RFC2616 specification. On a redirect, it also removes the {@code "Authorization"} and all
   * {@code "If-*"} request headers.
   *
   * <p>Upgrade warning: When handling a status code of 303, {@link #handleRedirect(int,
   * HttpHeaders)} now correctly removes any content from the body of the new request, as GET
   * requests should not have content. It did not do this in prior version 1.16.
   *
   * @return whether the redirect was successful
   * @since 1.11
   */
  public boolean handleRedirect(int statusCode, HttpHeaders responseHeaders) {
    String redirectLocation = responseHeaders.getLocation();
    if (getFollowRedirects()
        && HttpStatusCodes.isRedirect(statusCode)
        && redirectLocation != null) {
      // resolve the redirect location relative to the current location
      setUrl(new GenericUrl(url.toURL(redirectLocation), useRawRedirectUrls));
      // on 303 change method to GET
      if (statusCode == HttpStatusCodes.STATUS_CODE_SEE_OTHER) {
        setRequestMethod(HttpMethods.GET);
        // GET requests do not support non-zero content length
        setContent(null);
      }
      // remove Authorization and If-* headers
      headers.setAuthorization((String) null);
      headers.setIfMatch((String) null);
      headers.setIfNoneMatch((String) null);
      headers.setIfModifiedSince((String) null);
      headers.setIfUnmodifiedSince((String) null);
      headers.setIfRange((String) null);
      return true;
    }
    return false;
  }

  /**
   * Returns the sleeper.
   *
   * @since 1.15
   */
  public Sleeper getSleeper() {
    return sleeper;
  }

  /**
   * Sets the sleeper. The default value is {@link Sleeper#DEFAULT}.
   *
   * @since 1.15
   */
  public HttpRequest setSleeper(Sleeper sleeper) {
    this.sleeper = Preconditions.checkNotNull(sleeper);
    return this;
  }

  private static void addSpanAttribute(Span span, String key, String value) {
    if (value != null) {
      span.putAttribute(key, AttributeValue.stringAttributeValue(value));
    }
  }

  private static String getVersion() {
    // attempt to read the library's version from a properties file generated during the build
    // this value should be read and cached for later use
    String version = "unknown-version";
    try (InputStream inputStream = HttpRequest.class.getResourceAsStream("/google-http-client.properties")) {
      if (inputStream != null) {
        final Properties properties = new Properties();
        properties.load(inputStream);
        version = properties.getProperty("google-http-client.version");
      }
    } catch (IOException e) {
      // ignore
    }
    return version;
  }
}
