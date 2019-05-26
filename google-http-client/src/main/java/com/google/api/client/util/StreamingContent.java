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

package com.google.api.client.util;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Streaming content interface to write bytes to an output stream.
 *
 * <p>Implementations don't need to be thread-safe.
 *
 * @since 1.14
 * @author Yaniv Inbar
 */
public interface StreamingContent {

  /**
   * Writes the byte content to the given output stream.
   *
   * <p>Implementations must not close the output stream, and instead should flush the output
   * stream. Some callers may assume that the output stream has not been closed, and will fail to
   * work if it has been closed.
   *
   * @param out output stream
   */
  void writeTo(OutputStream out) throws IOException;
}
