/**
 * Copyright 2019 Google LLC
 *
 * <p>Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of the License at
 *
 * <p>https://www.apache.org/licenses/LICENSE-2.0
 *
 * <p>Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.google.api.client.http;

import javax.annotation.Nullable;

/** A wrapper for OpenCensus' Span. */
interface Span {

  /** Adds requestMethod, host, path, and url to the Span's attributes. */
  void addCommonHttpAttributes(String requestMethod, String host, String path, String url);

  /** Adds user agent to the Span's attributes. */
  void addUserAgent(String value);

  /** Adds an annotation to the Span. */
  void addAnnotation(String description);

  /**
   * Propagate information of current tracing context. This information will be injected into HTTP
   * header.
   */
  void addHeaders(HttpHeaders headers);

  /** Marks the end of a {@link Span}. */
  void end(@Nullable Integer statusCode);

  /**
   * Records a new message event which contains the size of the request content. Note that the size
   * represents the message size in application layer, i.e., content-length.
   */
  void recordSentMessageEvent(long contentLength);

  /**
   * Records a new message event which contains the size of the response content. Note that the size
   * represents the message size in application layer, i.e., content-length.
   */
  void recordReceivedMessageEvent(long contentLength);
}
