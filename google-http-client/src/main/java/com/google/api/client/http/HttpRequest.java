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

import com.google.api.client.util.Data;
import com.google.api.client.util.FieldInfo;
import com.google.api.client.util.Strings;
import com.google.api.client.util.Types;
import com.google.common.base.Preconditions;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * HTTP request.
 *
 * <p>
 * Implementation is not thread-safe.
 * </p>
 *
 * @since 1.0
 * @author Yaniv Inbar
 */
public final class HttpRequest {

  /**
   * User agent suffix for all requests.
   *
   * <p>
   * Includes a {@code "(gzip)"} suffix in case the server -- as Google's servers may do -- checks
   * the {@code User-Agent} header to try to detect if the client accepts gzip-encoded responses.
   * </p>
   *
   * @since 1.4
   */
  public static final String USER_AGENT_SUFFIX =
      "Google-HTTP-Java-Client/" + Strings.VERSION + " (gzip)";

  /**
   * HTTP request execute interceptor to intercept the start of {@link #execute()} (before executing
   * the HTTP request) or {@code null} for none.
   */
  private HttpExecuteInterceptor interceptor;

  /** HTTP request headers. */
  private HttpHeaders headers = new HttpHeaders();

  /**
   * HTTP response headers.
   *
   * <p>
   * For example, this can be used if you want to use a subclass of {@link HttpHeaders} called
   * MyHeaders to process the response:
   * </p>
   *
   * <pre>
  static String executeAndGetValueOfSomeCustomHeader(HttpRequest request) {
    MyHeaders responseHeaders = new MyHeaders();
    request.responseHeaders = responseHeaders;
    HttpResponse response = request.execute();
    return responseHeaders.someCustomHeader;
  }
   * </pre>
   */
  private HttpHeaders responseHeaders = new HttpHeaders();

  /**
   * Set the number of retries that will be allowed to execute as the result of an
   * {@link HttpUnsuccessfulResponseHandler} before being terminated or {@code 0} to not retry
   * requests. The default value is {@code 10}.
   */
  private int numRetries = 10;

  /**
   * Whether to disable request content logging during {@link #execute()} (unless {@link Level#ALL}
   * is loggable which forces all logging).
   *
   * <p>
   * Useful for example if content has sensitive data such as an authentication information.
   * Defaults to {@code false}.
   * </p>
   */
  private boolean disableContentLogging;

  /** HTTP request content or {@code null} for none. */
  private HttpContent content;

  /** HTTP transport. */
  private final HttpTransport transport;

  /** HTTP request method. */
  private HttpMethod method;

  /** HTTP request URL. */
  private GenericUrl url;

  /** Timeout in milliseconds to establish a connection or {@code 0} for an infinite timeout. */
  private int connectTimeout = 20 * 1000;

  /**
   * Timeout in milliseconds to read data from an established connection or {@code 0} for an
   * infinite timeout.
   */
  private int readTimeout = 20 * 1000;

  /** HTTP unsuccessful (non-2XX) response handler or {@code null} for none. */
  private HttpUnsuccessfulResponseHandler unsuccessfulResponseHandler;

  /** Map from normalized content type to HTTP parser. */
  private final Map<String, HttpParser> contentTypeToParserMap = new HashMap<String, HttpParser>();

  /** Whether to enable gzip compression of HTTP content ({@code false} by default). */
  private boolean enableGZipContent;

  /** Whether to automatically follow redirects ({@code true} by default). */
  private boolean followRedirects = true;

  /**
   * @param transport HTTP transport
   * @param method HTTP request method (may be {@code null}
   */
  HttpRequest(HttpTransport transport, HttpMethod method) {
    this.transport = transport;
    this.method = method;
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
   * Returns the HTTP request method.
   *
   * @since 1.5
   */
  public HttpMethod getMethod() {
    return method;
  }

  /**
   * Sets the HTTP request method.
   *
   * @since 1.5
   */
  public HttpRequest setMethod(HttpMethod method) {
    this.method = Preconditions.checkNotNull(method);
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
   * Returns whether to enable gzip compression of HTTP content.
   *
   * @since 1.5
   */
  public boolean getEnableGZipContent() {
    return enableGZipContent;
  }

  /**
   * Returns whether to enable gzip compression of HTTP content.
   *
   * <p>
   * By default it is {@code false}.
   * </p>
   *
   * <p>
   * To avoid the overhead of GZip compression for small content, one may want to set this to
   * {@code true} only for {@link HttpContent#getLength()} above a certain limit. For example:
   * </p>
   *
   * <pre>
  public static class MyInterceptor implements HttpExecuteInterceptor {
    public void intercept(HttpRequest request) throws IOException {
      if (request.getContent() != null && request.getContent().getLength() >= 256) {
        request.setEnableGZipContent(true);
      }
    }
  }
   * </pre>
   *
   * @since 1.5
   */
  public HttpRequest setEnableGZipContent(boolean enableGZipContent) {
    this.enableGZipContent = enableGZipContent;
    return this;
  }

  /**
   * Returns whether to disable request content logging during {@link #execute()} (unless
   * {@link Level#ALL} is loggable which forces all logging).
   *
   * @since 1.5
   */
  public boolean getDisableContentLogging() {
    return disableContentLogging;
  }

  /**
   * Returns whether to disable request content logging during {@link #execute()} (unless
   * {@link Level#ALL} is loggable which forces all logging).
   *
   * <p>
   * Useful for example if content has sensitive data such as an authentication information.
   * Defaults to {@code false}.
   * </p>
   *
   * @since 1.5
   */
  public HttpRequest setDisableContentLogging(boolean disableContentLogging) {
    this.disableContentLogging = disableContentLogging;
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
   * <p>
   * By default it is 20000 (20 seconds).
   * </p>
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
   * <p>
   * By default it is 20000 (20 seconds).
   * </p>
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
   * <p>
   * By default, this is a new unmodified instance of {@link HttpHeaders}.
   * </p>
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
   * <p>
   * By default, this is a new unmodified instance of {@link HttpHeaders}.
   * </p>
   *
   * <p>
   * For example, this can be used if you want to use a subclass of {@link HttpHeaders} called
   * MyHeaders to process the response:
   * </p>
   *
   * <pre>
  static String executeAndGetValueOfSomeCustomHeader(HttpRequest request) {
    MyHeaders responseHeaders = new MyHeaders();
    request.responseHeaders = responseHeaders;
    HttpResponse response = request.execute();
    return responseHeaders.someCustomHeader;
  }
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
    return interceptor;
  }

  /**
   * Sets the HTTP request execute interceptor to intercept the start of {@link #execute()} (before
   * executing the HTTP request) or {@code null} for none.
   *
   * @since 1.5
   */
  public HttpRequest setInterceptor(HttpExecuteInterceptor interceptor) {
    this.interceptor = interceptor;
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
   * Returns the HTTP unsuccessful (non-2XX) response handler or {@code null} for none.
   *
   * @since 1.5
   */
  public HttpRequest setUnsuccessfulResponseHandler(
      HttpUnsuccessfulResponseHandler unsuccessfulResponseHandler) {
    this.unsuccessfulResponseHandler = unsuccessfulResponseHandler;
    return this;
  }

  /**
   * Returns the number of retries that will be allowed to execute as the result of an
   * {@link HttpUnsuccessfulResponseHandler} before being terminated or {@code 0} to not retry
   * requests.
   *
   * @since 1.5
   */
  public int getNumberOfRetries() {
    return numRetries;
  }

  /**
   * Returns the number of retries that will be allowed to execute as the result of an
   * {@link HttpUnsuccessfulResponseHandler} before being terminated or {@code 0} to not retry
   * requests.
   *
   * <p>
   * The default value is {@code 10}.
   * </p>
   *
   * @since 1.5
   */
  public HttpRequest setNumberOfRetries(int numRetries) {
    Preconditions.checkArgument(numRetries >= 0);
    this.numRetries = numRetries;
    return this;
  }

  /**
   * Adds an HTTP response content parser.
   * <p>
   * If there is already a previous parser defined for this new parser (as defined by
   * {@link #getParser(String)} then the previous parser will be removed.
   * </p>
   *
   * @since 1.4
   */
  public void addParser(HttpParser parser) {
    String contentType = normalizeMediaType(parser.getContentType());
    contentTypeToParserMap.put(contentType, parser);
  }

  /**
   * Returns the HTTP response content parser to use for the given content type or {@code null} if
   * none is defined.
   *
   * @param contentType content type or {@code null} for {@code null} result
   * @return HTTP response content parser or {@code null} for {@code null} input
   * @since 1.4
   */
  public final HttpParser getParser(String contentType) {
    contentType = normalizeMediaType(contentType);
    return contentTypeToParserMap.get(contentType);
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
   * <p>
   * The default value is {@code true}.
   * </p>
   *
   * @since 1.6
   */
  public HttpRequest setFollowRedirects(boolean followRedirects) {
    this.followRedirects = followRedirects;
    return this;
  }

  /**
   * Execute the HTTP request and returns the HTTP response.
   * <p>
   * Note that regardless of the returned status code, the HTTP response content has not been parsed
   * yet, and must be parsed by the calling code.
   * <p>
   * Almost all details of the request and response are logged if {@link Level#CONFIG} is loggable.
   * The only exception is the value of the {@code Authorization} header which is only logged if
   * {@link Level#ALL} is loggable.
   *
   * @return HTTP response for an HTTP success code
   * @throws HttpResponseException for an HTTP error code
   * @see HttpResponse#isSuccessStatusCode()
   */
  public HttpResponse execute() throws IOException {
    boolean requiresRetry = false;
    boolean retrySupported = false;
    Preconditions.checkArgument(numRetries >= 0);
    int retriesRemaining = numRetries;
    HttpResponse response = null;

    Preconditions.checkNotNull(method);
    Preconditions.checkNotNull(url);

    do {
      // Cleanup any unneeded response from a previous iteration
      if (response != null) {
        response.ignore();
      }
      // run the interceptor
      if (interceptor != null) {
        interceptor.intercept(this);
      }
      // build low-level HTTP request
      String urlString = url.build();
      LowLevelHttpRequest lowLevelHttpRequest;
      switch (method) {
        case DELETE:
          lowLevelHttpRequest = transport.buildDeleteRequest(urlString);
          break;
        default:
          lowLevelHttpRequest = transport.buildGetRequest(urlString);
          break;
        case HEAD:
          Preconditions.checkArgument(
              transport.supportsHead(), "HTTP transport doesn't support HEAD");
          lowLevelHttpRequest = transport.buildHeadRequest(urlString);
          break;
        case PATCH:
          Preconditions.checkArgument(
              transport.supportsPatch(), "HTTP transport doesn't support PATCH");
          lowLevelHttpRequest = transport.buildPatchRequest(urlString);
          break;
        case POST:
          lowLevelHttpRequest = transport.buildPostRequest(urlString);
          break;
        case PUT:
          lowLevelHttpRequest = transport.buildPutRequest(urlString);
          break;
      }
      Logger logger = HttpTransport.LOGGER;
      boolean loggable = logger.isLoggable(Level.CONFIG);
      StringBuilder logbuf = null;
      // log method and URL
      if (loggable) {
        logbuf = new StringBuilder();
        logbuf.append("-------------- REQUEST  --------------").append(Strings.LINE_SEPARATOR);
        logbuf.append(method).append(' ').append(urlString).append(Strings.LINE_SEPARATOR);
      }
      // add to user agent
      if (headers.getUserAgent() == null) {
        headers.setUserAgent(USER_AGENT_SUFFIX);
      } else {
        headers.setUserAgent(headers.getUserAgent() + " " + USER_AGENT_SUFFIX);
      }
      // headers
      HashSet<String> headerNames = new HashSet<String>();
      for (Map.Entry<String, Object> headerEntry : headers.entrySet()) {
        String name = headerEntry.getKey();
        String lowerCase = name.toLowerCase();
        Preconditions.checkArgument(headerNames.add(lowerCase),
            "multiple headers of the same name (headers are case insensitive): %s", lowerCase);
        Object value = headerEntry.getValue();
        if (value != null) {
          Class<? extends Object> valueClass = value.getClass();
          if (value instanceof Iterable<?> || valueClass.isArray()) {
            for (Object repeatedValue : Types.iterableOf(value)) {
              addHeader(logger, logbuf, lowLevelHttpRequest, name, repeatedValue);
            }
          } else {
            addHeader(logger, logbuf, lowLevelHttpRequest, name, value);
          }
        }
      }
      // content
      HttpContent content = this.content;
      if (content != null) {
        // TODO(yanivi): instead of isTextBasedContentType, have an enableLogContent boolean?
        // TODO(yanivi): alternatively, HttpContent.supportsLogging()?
        String contentEncoding = content.getEncoding();
        long contentLength = content.getLength();
        String contentType = content.getType();
        // log content
        if (contentLength != 0 && contentEncoding == null
            && LogContent.isTextBasedContentType(contentType)
            && (loggable && !disableContentLogging || logger.isLoggable(Level.ALL))) {
          content = new LogContent(content, contentType, contentEncoding, contentLength);
        }
        // gzip
        if (enableGZipContent) {
          content = new GZipContent(content, contentType);
          contentEncoding = content.getEncoding();
          contentLength = content.getLength();
        }
        // append content headers to log buffer
        if (loggable) {
          if (contentType != null) {
            logbuf.append("Content-Type: " + contentType).append(Strings.LINE_SEPARATOR);
          }
          if (contentEncoding != null) {
            logbuf.append("Content-Encoding: " + contentEncoding).append(Strings.LINE_SEPARATOR);
          }
          if (contentLength >= 0) {
            logbuf.append("Content-Length: " + contentLength).append(Strings.LINE_SEPARATOR);
          }
        }
        lowLevelHttpRequest.setContent(content);
      }
      // log from buffer
      if (loggable) {
        logger.config(logbuf.toString());
      }

      // execute
      lowLevelHttpRequest.setTimeout(connectTimeout, readTimeout);
      response = new HttpResponse(this, lowLevelHttpRequest.execute());

      // We need to make sure our content type can support retry
      // null content is inherently able to be retried
      retrySupported = retriesRemaining > 0 && (content == null || content.retrySupported());
      requiresRetry = false;

      if (!response.isSuccessStatusCode()) {
        boolean errorHandled = false;
        boolean redirectRequest = false;
        if (unsuccessfulResponseHandler != null) {
          // Even if we don't have the potential to retry, we might want to run the
          // handler to fix conditions (like expired tokens) that might cause us
          // trouble on our next request
          errorHandled = unsuccessfulResponseHandler.handleResponse(this, response, retrySupported);
        }
        if (!errorHandled && getFollowRedirects() && isRedirected(response)) {
          // The unsuccessful request's error could not be handled and it is a redirect request.
          handleRedirect(response);
          redirectRequest = true;
        }
        // A retry is required if the error was successfully handled or if it is a redirect request.
        requiresRetry = errorHandled || redirectRequest;
        // Once there are no more retries remaining, this will be -1
        // Count redirects as retries, we want a finite limit of redirects.
        retriesRemaining--;
      }
    } while (requiresRetry && retrySupported);

    if (!response.isSuccessStatusCode()) {
      throw new HttpResponseException(response);
    }
    return response;
  }

  /**
   * Sets up this request object to handle the necessary redirect.
   */
  private void handleRedirect(HttpResponse response) {
    String redirectLocation = response.getHeaders().getLocation();
    setUrl(new GenericUrl(redirectLocation));

    // As per the RFC2616 specification for 303. The response to the request can be found
    // under a different URI and should be retrieved using a GET method on that resource.
    if (response.getStatusCode() == HttpStatusCodes.STATUS_CODE_SEE_OTHER) {
      setMethod(HttpMethod.GET);
    }
  }

  /**
   * Returns whether it is a redirect request.
   */
  private boolean isRedirected(HttpResponse response) {
    int statusCode = response.getStatusCode();
    switch (statusCode) {
      case HttpStatusCodes.STATUS_CODE_MOVED_PERMANENTLY: // 301
      case HttpStatusCodes.STATUS_CODE_FOUND: // 302
      case HttpStatusCodes.STATUS_CODE_SEE_OTHER: // 303
      case HttpStatusCodes.STATUS_CODE_TEMPORARY_REDIRECT: // 307
        // Redirect requests must have a location header specified.
        return response.getHeaders().getLocation() != null;
      default:
        return false;
    }
  }

  private static void addHeader(Logger logger, StringBuilder logbuf,
      LowLevelHttpRequest lowLevelHttpRequest, String name, Object value) {
    // ignore nulls
    if (value == null || Data.isNull(value)) {
      return;
    }
    // compute value
    String stringValue =
        value instanceof Enum<?> ? FieldInfo.of((Enum<?>) value).getName() : value.toString();
    // log header
    if (logbuf != null) {
      logbuf.append(name).append(": ");
      if ("Authorization".equals(name) && !logger.isLoggable(Level.ALL)) {
        logbuf.append("<Not Logged>");
      } else {
        logbuf.append(stringValue);
      }
      logbuf.append(Strings.LINE_SEPARATOR);
    }
    // add header
    lowLevelHttpRequest.addHeader(name, stringValue);
  }

  /**
   * Returns the normalized media type without parameters of the form {@code type "/" subtype"} as
   * specified in <a href="http://tools.ietf.org/html/rfc2616#section-3.7">Media Types</a>.
   *
   * @param mediaType unnormalized media type with possible parameters or {@code null} for
   *        {@code null} result
   * @return normalized media type without parameters or {@code null} for {@code null} input
   * @since 1.4
   */
  public static String normalizeMediaType(String mediaType) {
    if (mediaType == null) {
      return null;
    }
    int semicolon = mediaType.indexOf(';');
    return semicolon == -1 ? mediaType : mediaType.substring(0, semicolon);
  }
}
