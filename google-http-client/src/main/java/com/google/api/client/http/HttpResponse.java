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

import com.google.api.client.util.ArrayValueMap;
import com.google.api.client.util.ClassInfo;
import com.google.api.client.util.Data;
import com.google.api.client.util.FieldInfo;
import com.google.api.client.util.LoggingInputStream;
import com.google.api.client.util.StringUtils;
import com.google.api.client.util.Types;
import com.google.common.base.Preconditions;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.GZIPInputStream;

/**
 * HTTP response.
 *
 * <p>
 * Implementation is not thread-safe.
 * </p>
 *
 * @since 1.0
 * @author Yaniv Inbar
 */
public final class HttpResponse {

  /** HTTP response content or {@code null} before {@link #getContent()}. */
  private InputStream content;

  /** Content encoding or {@code null}. */
  private final String contentEncoding;

  /**
   * Content length or less than zero if not known. May be reset by {@link #getContent} if response
   * had GZip compression.
   */
  private long contentLength;

  /** Content type or {@code null} for none. */
  private final String contentType;

  /** HTTP headers. */
  private final HttpHeaders headers;

  /** Low-level HTTP response. */
  private LowLevelHttpResponse response;

  /** Status code. */
  private final int statusCode;

  /** Status message or {@code null}. */
  private final String statusMessage;

  /** HTTP transport. */
  private final HttpTransport transport;

  /** HTTP request. */
  private final HttpRequest request;

  /**
   * Determines the limit to the content size that will be logged during {@link #getContent()}.
   *
   * <p>
   * Content will only be logged if {@link #isLoggingEnabled} is {@code true}.
   * </p>
   *
   * <p>
   * If the content size is greater than this limit then it will not be logged.
   * </p>
   *
   * <p>
   * Can be set to {@code 0} to disable content logging. This is useful for example if content has
   * sensitive data such as authentication information.
   * </p>
   *
   * <p>
   * Defaults to {@link HttpRequest#getContentLoggingLimit()}.
   * </p>
   */
  private int contentLoggingLimit;

  /**
   * Determines whether logging should be enabled on this response.
   *
   * <p>
   * Defaults to {@link HttpRequest#isLoggingEnabled()}.
   * </p>
   */
  private boolean loggingEnabled;

  HttpResponse(HttpRequest request, LowLevelHttpResponse response) {
    this.request = request;
    transport = request.getTransport();
    headers = request.getResponseHeaders();
    contentLoggingLimit = request.getContentLoggingLimit();
    loggingEnabled = request.isLoggingEnabled();
    this.response = response;
    contentLength = response.getContentLength();
    contentType = response.getContentType();
    contentEncoding = response.getContentEncoding();
    int code = response.getStatusCode();
    statusCode = code;
    String message = response.getReasonPhrase();
    statusMessage = message;
    Logger logger = HttpTransport.LOGGER;
    boolean loggable = loggingEnabled && logger.isLoggable(Level.CONFIG);
    StringBuilder logbuf = null;
    if (loggable) {
      logbuf = new StringBuilder();
      logbuf.append("-------------- RESPONSE --------------").append(StringUtils.LINE_SEPARATOR);
      String statusLine = response.getStatusLine();
      if (statusLine != null) {
        logbuf.append(statusLine);
      } else {
        logbuf.append(code);
        if (message != null) {
          logbuf.append(' ').append(message);
        }
      }
      logbuf.append(StringUtils.LINE_SEPARATOR);
    }
    // headers
    int size = response.getHeaderCount();
    Class<? extends HttpHeaders> headersClass = headers.getClass();
    List<Type> context = Arrays.<Type>asList(headersClass);
    ClassInfo classInfo = ClassInfo.of(headersClass);
    HashMap<String, String> fieldNameMap = HttpHeaders.getFieldNameMap(headersClass);
    ArrayValueMap arrayValueMap = new ArrayValueMap(headers);
    for (int i = 0; i < size; i++) {
      String headerName = response.getHeaderName(i);
      String headerValue = response.getHeaderValue(i);
      if (loggable) {
        logbuf.append(headerName + ": " + headerValue).append(StringUtils.LINE_SEPARATOR);
      }
      String fieldName = fieldNameMap.get(headerName.toLowerCase());
      if (fieldName == null) {
        fieldName = headerName;
      }
      // use field information if available
      FieldInfo fieldInfo = classInfo.getFieldInfo(fieldName);
      if (fieldInfo != null) {
        Type type = Data.resolveWildcardTypeOrTypeVariable(context, fieldInfo.getGenericType());
        // type is now class, parameterized type, or generic array type
        if (Types.isArray(type)) {
          // array that can handle repeating values
          Class<?> rawArrayComponentType =
              Types.getRawArrayComponentType(context, Types.getArrayComponentType(type));
          arrayValueMap.put(fieldInfo.getField(), rawArrayComponentType,
              parseValue(rawArrayComponentType, context, headerValue));
        } else if (Types.isAssignableToOrFrom(
            Types.getRawArrayComponentType(context, type), Iterable.class)) {
          // iterable that can handle repeating values
          @SuppressWarnings("unchecked")
          Collection<Object> collection = (Collection<Object>) fieldInfo.getValue(headers);
          if (collection == null) {
            collection = Data.newCollectionInstance(type);
            fieldInfo.setValue(headers, collection);
          }
          Type subFieldType = type == Object.class ? null : Types.getIterableParameter(type);
          collection.add(parseValue(subFieldType, context, headerValue));
        } else {
          // parse value based on field type
          fieldInfo.setValue(headers, parseValue(type, context, headerValue));
        }
      } else {
        // store header values in an array list
        @SuppressWarnings("unchecked")
        ArrayList<String> listValue = (ArrayList<String>) headers.get(fieldName);
        if (listValue == null) {
          listValue = new ArrayList<String>();
          headers.set(fieldName, listValue);
        }
        listValue.add(headerValue);
      }
    }
    arrayValueMap.setValues();
    // log from buffer
    if (loggable) {
      logger.config(logbuf.toString());
    }
  }

  /**
   * Returns the limit to the content size that will be logged during {@link #getContent()}.
   *
   * <p>
   * Content will only be logged if {@link #isLoggingEnabled} is {@code true}.
   * </p>
   *
   * <p>
   * If the content size is greater than this limit then it will not be logged.
   * </p>
   *
   * <p>
   * Can be set to {@code 0} to disable content logging. This is useful for example if content has
   * sensitive data such as authentication information.
   * </p>
   *
   * <p>
   * Defaults to {@link HttpRequest#getContentLoggingLimit()}.
   * </p>
   *
   * <p>
   * Upgrade warning: prior to version 1.9, the default was {@code 100,000} bytes, but now it is
   * {@link HttpRequest#getContentLoggingLimit()}.
   * </p>
   *
   * @since 1.7
   */
  public int getContentLoggingLimit() {
    return contentLoggingLimit;
  }

  /**
   * Set the limit to the content size that will be logged during {@link #getContent()}.
   *
   * <p>
   * Content will only be logged if {@link #isLoggingEnabled} is {@code true}.
   * </p>
   *
   * <p>
   * If the content size is greater than this limit then it will not be logged.
   * </p>
   *
   * <p>
   * Can be set to {@code 0} to disable content logging. This is useful for example if content has
   * sensitive data such as authentication information.
   * </p>
   *
   * <p>
   * Defaults to {@link HttpRequest#getContentLoggingLimit()}.
   * </p>
   *
   * <p>
   * Upgrade warning: prior to version 1.9, the default was {@code 100,000} bytes, but now it is
   * {@link HttpRequest#getContentLoggingLimit()}.
   * </p>
   *
   * @since 1.7
   */
  public HttpResponse setContentLoggingLimit(int contentLoggingLimit) {
    Preconditions.checkArgument(
        contentLoggingLimit >= 0, "The content logging limit must be non-negative.");
    this.contentLoggingLimit = contentLoggingLimit;
    return this;
  }

  /**
   * Returns whether logging should be enabled on this response.
   *
   * <p>
   * Defaults to {@link HttpRequest#isLoggingEnabled()}.
   * </p>
   *
   * @since 1.9
   */
  public boolean isLoggingEnabled() {
    return loggingEnabled;
  }

  /**
   * Sets whether logging should be enabled on this response.
   *
   * <p>
   * Defaults to {@link HttpRequest#isLoggingEnabled()}.
   * </p>
   *
   * @since 1.9
   */
  public HttpResponse setLoggingEnabled(boolean loggingEnabled) {
    this.loggingEnabled = loggingEnabled;
    return this;
  }

  /**
   * Returns the content encoding or {@code null} for none.
   *
   * @since 1.5
   */
  public String getContentEncoding() {
    return contentEncoding;
  }

  /**
   * Returns the content type or {@code null} for none.
   *
   * @since 1.5
   */
  public String getContentType() {
    return contentType;
  }

  /**
   * Returns the HTTP response headers.
   *
   * @since 1.5
   */
  public HttpHeaders getHeaders() {
    return headers;
  }

  /**
   * Returns whether received a successful HTTP status code {@code >= 200 && < 300} (see
   * {@link #getStatusCode()}).
   *
   * @since 1.5
   */
  public boolean isSuccessStatusCode() {
    return HttpStatusCodes.isSuccess(statusCode);
  }

  /**
   * Returns the HTTP status code or {@code 0} for none.
   *
   * @since 1.5
   */
  public int getStatusCode() {
    return statusCode;
  }

  /**
   * Returns the HTTP status message or {@code null} for none.
   *
   * @since 1.5
   */
  public String getStatusMessage() {
    return statusMessage;
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
   * Returns the HTTP request.
   *
   * @since 1.5
   */
  public HttpRequest getRequest() {
    return request;
  }

  private static Object parseValue(Type valueType, List<Type> context, String value) {
    Type resolved = Data.resolveWildcardTypeOrTypeVariable(context, valueType);
    return Data.parsePrimitiveValue(resolved, value);
  }

  /**
   * Returns the content of the HTTP response.
   * <p>
   * The result is cached, so subsequent calls will be fast.
   *
   * @return input stream content of the HTTP response or {@code null} for none
   * @throws IOException I/O exception
   */
  public InputStream getContent() throws IOException {
    LowLevelHttpResponse response = this.response;
    if (response == null) {
      return content;
    }
    InputStream content = this.response.getContent();
    this.response = null;
    if (content != null) {
      // gzip encoding (wrap content with GZipInputStream)
      String contentEncoding = this.contentEncoding;
      if (contentEncoding != null && contentEncoding.contains("gzip")) {
        content = new GZIPInputStream(content);
        contentLength = -1;
      }
      // logging (wrap content with LoggingInputStream)
      Logger logger = HttpTransport.LOGGER;
      if (loggingEnabled && logger.isLoggable(Level.CONFIG)) {
        content = new LoggingInputStream(content, logger, Level.CONFIG, contentLoggingLimit);
      }
      this.content = content;
    }
    return content;
  }

  /**
   * Writes the content of the HTTP response into the given destination output stream.
   *
   * <p>
   * Sample usage: <code>
    HttpRequest request =
        requestFactory.buildGetRequest(new GenericUrl(
            "https://www.google.com/images/srpr/logo3w.png"));
    OutputStream outputStream =
        new FileOutputStream(new File ("/tmp/logo3w.png"));
    try {
      HttpResponse response = request.execute();
      response.download(outputStream);
    } finally {
      outputStream.close();
    }
   * </code>
   * </p>
   *
   * <p>
   * This method closes the content of the HTTP response from {@link #getContent()}.
   * </p>
   *
   * <p>
   * This method does not close the given output stream.
   * </p>
   *
   * @param outputStream destination output stream
   * @throws IOException I/O exception
   * @since 1.9
   */
  public void download(OutputStream outputStream) throws IOException {
    InputStream inputStream = getContent();
    AbstractInputStreamContent.copy(inputStream, outputStream);
  }

  /**
   * Closes the content of the HTTP response from {@link #getContent()}, ignoring any content.
   */
  public void ignore() throws IOException {
    InputStream content = getContent();
    if (content != null) {
      content.close();
    }
  }

  /**
   * Disconnect using {@link LowLevelHttpResponse#disconnect()}.
   *
   * @since 1.4
   */
  public void disconnect() throws IOException {
    response.disconnect();
  }

  /**
   * Returns the HTTP response content parser to use for the content type of this HTTP response or
   * {@code null} for none.
   */
  public HttpParser getParser() {
    return request.getParser(contentType);
  }

  /**
   * Parses the content of the HTTP response from {@link #getContent()} and reads it into a data
   * class of key/value pairs using the parser returned by {@link #getParser()} .
   *
   * @return parsed data class or {@code null} for no content
   * @throws IOException I/O exception
   * @throws IllegalArgumentException if no parser is defined for the given content type or if there
   *         is no content type defined in the HTTP response
   */
  public <T> T parseAs(Class<T> dataClass) throws IOException {
    HttpParser parser = getParser();
    if (parser == null) {
      Preconditions.checkArgument(contentType != null, "Missing Content-Type header in response");
      throw new IllegalArgumentException("No parser defined for Content-Type: " + contentType);
    }
    return parser.parse(this, dataClass);
  }

  /**
   * Parses the content of the HTTP response from {@link #getContent()} and reads it into a string.
   * <p>
   * Since this method returns {@code ""} for no content, a simpler check for no content is to check
   * if {@link #getContent()} is {@code null}.
   *
   * @return parsed string or {@code ""} for no content
   * @throws IOException I/O exception
   */
  public String parseAsString() throws IOException {
    InputStream content = getContent();
    if (content == null) {
      return "";
    }
    try {
      long contentLength = this.contentLength;
      int bufferSize = contentLength == -1 ? 4096 : (int) contentLength;
      int length = 0;
      byte[] buffer = new byte[bufferSize];
      byte[] tmp = new byte[4096];
      int bytesRead;
      while ((bytesRead = content.read(tmp)) != -1) {
        if (length + bytesRead > bufferSize) {
          bufferSize = Math.max(bufferSize << 1, length + bytesRead);
          byte[] newbuffer = new byte[bufferSize];
          System.arraycopy(buffer, 0, newbuffer, 0, length);
          buffer = newbuffer;
        }
        System.arraycopy(tmp, 0, buffer, length, bytesRead);
        length += bytesRead;
      }
      return new String(buffer, 0, length, "UTF-8");
    } finally {
      content.close();
    }
  }
}
