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

import com.google.api.client.util.StringUtils;

import java.io.IOException;

/**
 * Exception thrown when an error status code is detected in an HTTP response.
 *
 * @since 1.0
 * @author Yaniv Inbar
 */
public class HttpResponseException extends IOException {

  private static final long serialVersionUID = -1875819453475890043L;

  /** HTTP status code. */
  private final int statusCode;

  /** Status message or {@code null}. */
  private final String statusMessage;

  /** HTTP headers. */
  private final transient HttpHeaders headers;

  /**
   * Constructor that constructs a detail message from the given HTTP response that includes the
   * status code, status message and HTTP response content.
   *
   * <p>
   * Callers of this constructor should call {@link HttpResponse#disconnect} after
   * {@link HttpResponseException} is instantiated. Example usage:
   * </p>
   *
   * <pre>
     try {
       throw new HttpResponseException(response);
     } finally {
       response.disconnect();
     }
   * </pre>
   *
   * @param response HTTP response
   */
  public HttpResponseException(HttpResponse response) {
    this(response, computeMessageWithContent(response));
  }

  /**
   * Constructor that allows an alternative detail message to be used.
   *
   * <p>
   * Callers of this constructor should call {@link HttpResponse#disconnect} after
   * {@link HttpResponseException} is instantiated. Example usage:
   * </p>
   *
   * <pre>
     try {
       throw new HttpResponseException(response, message);
     } finally {
       response.disconnect();
     }
   * </pre>
   *
   * @param response HTTP response
   * @param message detail message to use or {@code null} for none
   * @since 1.6
   */
  public HttpResponseException(HttpResponse response, String message) {
    super(message);
    statusCode = response.getStatusCode();
    statusMessage = response.getStatusMessage();
    headers = response.getHeaders();
  }

  /**
   * Returns whether received a successful HTTP status code {@code >= 200 && < 300} (see
   * {@link #getStatusCode()}).
   *
   * <p>
   * Upgrade warning: Overriding this method is no longer supported, and will be made final in
   * 1.10.
   * </p>
   *
   *
   * @since 1.7
   */
  public boolean isSuccessStatusCode() {
    return HttpStatusCodes.isSuccess(statusCode);
  }

  /**
   * Returns the HTTP status code or {@code 0} for none.
   *
   * <p>
   * Upgrade warning: Overriding this method is no longer supported, and will be made final in
   * 1.10.
   * </p>
   *
   * @since 1.7
   */
  public int getStatusCode() {
    return statusCode;
  }

  /**
   * Returns the HTTP status message or {@code null} for none.
   *
   * @since 1.9
   */
  public final String getStatusMessage() {
    return statusMessage;
  }

  /**
   * Returns the HTTP response headers.
   *
   * @since 1.7
   */
  public HttpHeaders getHeaders() {
    return headers;
  }

  /**
   * Returns an exception message to use for the given HTTP response.
   */
  private static String computeMessageWithContent(HttpResponse response) {
    StringBuilder builder = computeMessageBuffer(response);
    String content = "";
    try {
      content = response.parseAsString();
    } catch (IOException exception) {
      // it would be bad to throw an exception while throwing an exception
      exception.printStackTrace();
    }
    if (content.length() != 0) {
      builder.append(StringUtils.LINE_SEPARATOR).append(content);
    }
    return builder.toString();
  }

  /**
   * Returns an exception message string builder to use for the given HTTP response.
   *
   * @since 1.7
   */
  public static StringBuilder computeMessageBuffer(HttpResponse response) {
    StringBuilder builder = new StringBuilder();
    int statusCode = response.getStatusCode();
    if (statusCode != 0) {
      builder.append(statusCode);
    }
    String statusMessage = response.getStatusMessage();
    if (statusMessage != null) {
      if (statusCode != 0) {
        builder.append(' ');
      }
      builder.append(statusMessage);
    }
    return builder;
  }
}
