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

import com.google.api.client.util.StreamingContent;
import java.io.IOException;

/**
 * Low-level HTTP request.
 *
 * <p>This allows providing a different implementation of the HTTP request that is more compatible
 * with the Java environment used.
 *
 * <p>Implementation has no fields and therefore thread-safe, but sub-classes are not necessarily
 * thread-safe.
 *
 * @since 1.0
 * @author Yaniv Inbar
 */
public abstract class LowLevelHttpRequest {

  /** Content length or less than zero if not known. */
  private long contentLength = -1;

  /** Content encoding (for example {@code "gzip"}) or {@code null} for none. */
  private String contentEncoding;

  /** Content type or {@code null} for none. */
  private String contentType;

  /** Streaming content or {@code null} for no content. */
  private StreamingContent streamingContent;

  /**
   * Adds a header to the HTTP request.
   *
   * <p>Note that multiple headers of the same name need to be supported, in which case {@link
   * #addHeader} will be called for each instance of the header.
   *
   * @param name header name
   * @param value header value
   */
  public abstract void addHeader(String name, String value) throws IOException;

  /**
   * Sets the content length or less than zero if not known.
   *
   * <p>Default value is {@code -1}.
   *
   * @throws IOException I/O exception
   * @since 1.14
   */
  public final void setContentLength(long contentLength) throws IOException {
    this.contentLength = contentLength;
  }

  /**
   * Returns the content length or less than zero if not known.
   *
   * @since 1.14
   */
  public final long getContentLength() {
    return contentLength;
  }

  /**
   * Sets the content encoding (for example {@code "gzip"}) or {@code null} for none.
   *
   * @throws IOException I/O exception
   * @since 1.14
   */
  public final void setContentEncoding(String contentEncoding) throws IOException {
    this.contentEncoding = contentEncoding;
  }

  /**
   * Returns the content encoding (for example {@code "gzip"}) or {@code null} for none.
   *
   * @since 1.14
   */
  public final String getContentEncoding() {
    return contentEncoding;
  }

  /**
   * Sets the content type or {@code null} for none.
   *
   * @throws IOException I/O exception
   * @since 1.14
   */
  public final void setContentType(String contentType) throws IOException {
    this.contentType = contentType;
  }

  /**
   * Returns the content type or {@code null} for none.
   *
   * @since 1.14
   */
  public final String getContentType() {
    return contentType;
  }

  /**
   * Sets the streaming content or {@code null} for no content.
   *
   * @throws IOException I/O exception
   * @since 1.14
   */
  public final void setStreamingContent(StreamingContent streamingContent) throws IOException {
    this.streamingContent = streamingContent;
  }

  /**
   * Returns the streaming content or {@code null} for no content.
   *
   * @since 1.14
   */
  public final StreamingContent getStreamingContent() {
    return streamingContent;
  }

  /**
   * Sets the connection and read timeouts.
   *
   * <p>Default implementation does nothing, but subclasses should normally override.
   *
   * @param connectTimeout timeout in milliseconds to establish a connection or {@code 0} for an
   *     infinite timeout
   * @param readTimeout Timeout in milliseconds to read data from an established connection or
   *     {@code 0} for an infinite timeout
   * @throws IOException I/O exception
   * @since 1.4
   */
  public void setTimeout(int connectTimeout, int readTimeout) throws IOException {}

  /**
   * Sets the write timeout for POST/PUT requests.
   *
   * <p>Default implementation does nothing, but subclasses should normally override.
   *
   * @param writeTimeout timeout in milliseconds to establish a connection or {@code 0} for an
   *     infinite timeout
   * @throws IOException I/O exception
   * @since 1.27
   */
  public void setWriteTimeout(int writeTimeout) throws IOException {}

  /** Executes the request and returns a low-level HTTP response object. */
  public abstract LowLevelHttpResponse execute() throws IOException;
}
