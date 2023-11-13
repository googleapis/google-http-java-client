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

import com.google.api.client.util.StreamingContent;
import java.io.IOException;
import java.io.OutputStream;

/**
 * Serializes HTTP request content into an output stream.
 *
 * <p>Implementations don't need to be thread-safe.
 *
 * @since 1.0
 * @author Yaniv Inbar
 */
public interface HttpContent extends StreamingContent {

  /** Returns the content length or less than zero if not known. */
  long getLength() throws IOException;

  /** Returns the content type or {@code null} for none. */
  String getType();

  /**
   * Returns whether or not retry is supported on this content type.
   *
   * @since 1.4
   */
  boolean retrySupported();

  void writeTo(OutputStream out) throws IOException;
}
