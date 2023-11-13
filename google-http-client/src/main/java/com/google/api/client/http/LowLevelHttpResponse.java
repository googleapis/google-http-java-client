/*
 * Copyright 2010 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.api.client.http;

import java.io.IOException;
import java.io.InputStream;

/**
 * Low-level HTTP response.
 *
 * <p>This allows providing a different implementation of the HTTP response that is more compatible
 * with the Java environment used.
 *
 * <p>Implementation has no fields and therefore thread-safe, but sub-classes are not necessarily
 * thread-safe.
 *
 * @since 1.0
 * @author Yaniv Inbar
 */
public abstract class LowLevelHttpResponse {

  /**
   * Returns the HTTP response content input stream or {@code null} for none.
   *
   * @throws IOException I/O exception
   */
  public abstract InputStream getContent() throws IOException;

  /** Returns the content encoding (for example {@code "gzip"}) or {@code null} for none. */
  public abstract String getContentEncoding() throws IOException;

  /** Returns the content length or {@code 0} for none. */
  public abstract long getContentLength() throws IOException;

  /** Returns the content type or {@code null} for none. */
  public abstract String getContentType() throws IOException;

  /** Returns the response status line or {@code null} for none. */
  public abstract String getStatusLine() throws IOException;

  /** Returns the response status code or {@code <=0} for none. */
  public abstract int getStatusCode() throws IOException;

  /** Returns the HTTP reason phrase or {@code null} for none. */
  public abstract String getReasonPhrase() throws IOException;

  /**
   * Returns the number of HTTP response headers.
   *
   * <p>Note that multiple headers of the same name need to be supported, in which case each header
   * value is treated as a separate header.
   */
  public abstract int getHeaderCount() throws IOException;

  /** Returns the HTTP response header name at the given zero-based index. */
  public abstract String getHeaderName(int index) throws IOException;

  /** Returns the HTTP response header value at the given zero-based index. */
  public abstract String getHeaderValue(int index) throws IOException;

  /**
   * Default implementation does nothing, but subclasses may override to attempt to abort the
   * connection or release allocated system resources for this connection.
   *
   * @throws IOException I/O exception
   * @since 1.4
   */
  public void disconnect() throws IOException {}
}
