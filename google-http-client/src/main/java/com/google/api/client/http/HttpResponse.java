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
import com.google.api.client.util.Strings;
import com.google.api.client.util.Types;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
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

  /**
   * Content encoding or {@code null}.
   *
   * @deprecated (scheduled to be made private in 1.6) Use {@link #getContentEncoding}
   */
  @Deprecated
  public final String contentEncoding;

  /**
   * Content length or less than zero if not known. May be reset by {@link #getContent} if response
   * had GZip compression.
   */
  private long contentLength;

  /**
   * Content type or {@code null} for none.
   *
   * @deprecated (scheduled to be made private in 1.6) Use {@link #getContentType}
   */
  @Deprecated
  public final String contentType;

  /**
   * HTTP headers.
   * <p>
   * If a header name is used for multiple headers, only the last one is retained. The value is
   * initialized to {@link HttpRequest#responseHeaders} before being parsed from the actual HTTP
   * response headers.
   * <p>
   *
   * @deprecated (scheduled to be made private in 1.6) Use {@link #getHeaders}
   */
  @Deprecated
  public final HttpHeaders headers;

  /**
   * Whether received a successful status code {@code >= 200 && < 300}.
   *
   * @deprecated (scheduled to be made private in 1.6) Use {@link #isSuccessStatusCode()}
   */
  @Deprecated
  public final boolean isSuccessStatusCode;

  /** Low-level HTTP response. */
  private LowLevelHttpResponse response;

  /**
   * Status code.
   *
   * @deprecated (scheduled to be made private in 1.6) Use {@link #getStatusCode}
   */
  @Deprecated
  public final int statusCode;

  /**
   * Status message or {@code null}.
   *
   * @deprecated (scheduled to be made private in 1.6) Use {@link #getStatusMessage}
   */
  @Deprecated
  public final String statusMessage;

  /**
   * HTTP transport.
   *
   * @deprecated (scheduled to be made private in 1.6) Use {@link #getTransport}
   */
  @Deprecated
  public final HttpTransport transport;

  /**
   * HTTP request.
   *
   * @since 1.4
   * @deprecated (scheduled to be made private in 1.6) Use {@link #getRequest}
   */
  @Deprecated
  public final HttpRequest request;

  /**
   * Whether to disable response content logging during {@link #getContent()} (unless
   * {@link Level#ALL} is loggable which forces all logging).
   * <p>
   * Useful for example if content has sensitive data such as an authentication token. Defaults to
   * {@code false}.
   *
   * @deprecated (scheduled to be made private in 1.6) Use {@link #getDisableContentLogging} or
   *             {@link #setDisableContentLogging}
   */
  @Deprecated
  public boolean disableContentLogging;

  HttpResponse(HttpRequest request, LowLevelHttpResponse response) {
    this.request = request;
    transport = request.getTransport();
    headers = request.getResponseHeaders();
    this.response = response;
    contentLength = response.getContentLength();
    contentType = response.getContentType();
    contentEncoding = response.getContentEncoding();
    int code = response.getStatusCode();
    statusCode = code;
    isSuccessStatusCode = isSuccessStatusCode(code);
    String message = response.getReasonPhrase();
    statusMessage = message;
    Logger logger = HttpTransport.LOGGER;
    boolean loggable = logger.isLoggable(Level.CONFIG);
    StringBuilder logbuf = null;
    if (loggable) {
      logbuf = new StringBuilder();
      logbuf.append("-------------- RESPONSE --------------").append(Strings.LINE_SEPARATOR);
      String statusLine = response.getStatusLine();
      if (statusLine != null) {
        logbuf.append(statusLine);
      } else {
        logbuf.append(code);
        if (message != null) {
          logbuf.append(' ').append(message);
        }
      }
      logbuf.append(Strings.LINE_SEPARATOR);
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
        logbuf.append(headerName + ": " + headerValue).append(Strings.LINE_SEPARATOR);
      }
      String fieldName = fieldNameMap.get(headerName);
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
   * Sets whether to disable response content logging during {@link #getContent()} (unless
   * {@link Level#ALL} is loggable which forces all logging).
   *
   * @since 1.5
   */
  public boolean getDisableContentLogging() {
    return disableContentLogging;
  }

  /**
   * Sets whether to disable response content logging during {@link #getContent()} (unless
   * {@link Level#ALL} is loggable which forces all logging).
   *
   * <p>
   * Useful for example if content has sensitive data such as an authentication token. Defaults to
   * {@code false}.
   * </p>
   *
   * @since 1.5
   */
  public HttpResponse setDisableContentLogging(boolean disableContentLogging) {
    this.disableContentLogging = disableContentLogging;
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
    return isSuccessStatusCode;
  }

  /**
   * Returns the HTTP status code.
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
      byte[] debugContentByteArray = null;
      Logger logger = HttpTransport.LOGGER;
      boolean loggable =
          !disableContentLogging && logger.isLoggable(Level.CONFIG) || logger.isLoggable(Level.ALL);
      if (loggable) {
        ByteArrayOutputStream debugStream = new ByteArrayOutputStream();
        AbstractInputStreamContent.copy(content, debugStream);
        debugContentByteArray = debugStream.toByteArray();
        content = new ByteArrayInputStream(debugContentByteArray);
        logger.config("Response size: " + debugContentByteArray.length + " bytes");
      }
      // gzip encoding
      String contentEncoding = this.contentEncoding;
      if (contentEncoding != null && contentEncoding.contains("gzip")) {
        content = new GZIPInputStream(content);
        contentLength = -1;
        if (loggable) {
          ByteArrayOutputStream debugStream = new ByteArrayOutputStream();
          AbstractInputStreamContent.copy(content, debugStream);
          debugContentByteArray = debugStream.toByteArray();
          content = new ByteArrayInputStream(debugContentByteArray);
        }
      }
      if (loggable) {
        // print content using a buffered input stream that can be re-read
        String contentType = this.contentType;
        if (debugContentByteArray.length != 0 && LogContent.isTextBasedContentType(contentType)) {
          logger.config(new String(debugContentByteArray));
        }
      }
      this.content = content;
    }
    return content;
  }

  /**
   * Closes the the content of the HTTP response from {@link #getContent()}, ignoring any content.
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
      InputStream content = getContent();
      if (contentType == null) {
        if (content != null) {
          throw new IllegalArgumentException(
              "Missing Content-Type header in response: " + parseAsString());
        }
        return null;
      }
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
      return new String(buffer, 0, length);
    } finally {
      content.close();
    }
  }

  /**
   * Returns whether the given HTTP response status code is a success code {@code >= 200 and < 300}.
   */
  public static boolean isSuccessStatusCode(int statusCode) {
    return statusCode >= 200 && statusCode < 300;
  }
}
