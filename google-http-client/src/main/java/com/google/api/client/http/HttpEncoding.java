/*
 * Copyright (c) 2012 Google Inc.
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
import java.io.OutputStream;

/**
 * HTTP content encoding.
 *
 * <p>Implementations don't need to be thread-safe.
 *
 * @since 1.14
 * @author Yaniv Inbar
 */
public interface HttpEncoding {

  /** Returns the content encoding name (for example {@code "gzip"}) or {@code null} for none. */
  String getName();

  /**
   * Encodes the streaming content into the output stream.
   *
   * <p>Implementations must not close the output stream, and instead should flush the output
   * stream. Some callers may assume that the output stream has not been closed, and will fail to
   * work if it has been closed.
   *
   * @param content streaming content
   * @param out output stream
   */
  void encode(StreamingContent content, OutputStream out) throws IOException;
}
