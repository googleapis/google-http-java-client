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

import java.io.IOException;

/**
 * Exception thrown when an error status code is detected in an HTTP response.
 *
 * @since 1.0
 * @author Yaniv Inbar
 */
public class HttpResponseException extends IOException {

  static final long serialVersionUID = 1;

  /** HTTP response. */
  private final HttpResponse response;

  /**
   * Returns the HTTP response.
   *
   * @since 1.5
   */
  public final HttpResponse getResponse() {
    return response;
  }

  /**
   * Constructor that uses {@link #computeMessage(HttpResponse)} for the detail message.
   *
   * @param response HTTP response
   */
  public HttpResponseException(HttpResponse response) {
    this(response, computeMessage(response));
  }

  /**
   * Constructor that allows an alternative detail message to be used.
   *
   * @param response HTTP response
   * @param message detail message to use or {@code null} for none
   * @since 1.6
   */
  public HttpResponseException(HttpResponse response, String message) {
    super(message);
    this.response = response;
  }

  /** Returns an exception message to use for the given HTTP response. */
  public static String computeMessage(HttpResponse response) {
    String statusMessage = response.getStatusMessage();
    int statusCode = response.getStatusCode();
    if (statusMessage == null) {
      return String.valueOf(statusCode);
    }
    StringBuilder buf = new StringBuilder(4 + statusMessage.length());
    return buf.append(statusCode).append(' ').append(statusMessage).toString();
  }
}
