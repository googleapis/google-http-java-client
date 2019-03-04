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
 * Streaming content whose source is a byte array.
 *
 * <p>Implementation is not thread-safe.
 *
 * @since 1.14
 * @author Yaniv Inbar
 */
public class ByteArrayStreamingContent implements StreamingContent {

  /** Byte array content. */
  private final byte[] byteArray;

  /** Starting offset into the byte array. */
  private final int offset;

  /** Length of bytes to read from byte array. */
  private final int length;

  /** @param byteArray byte array content */
  public ByteArrayStreamingContent(byte[] byteArray) {
    this(byteArray, 0, byteArray.length);
  }

  /**
   * @param byteArray byte array content
   * @param offset starting offset into the byte array
   * @param length of bytes to read from byte array
   */
  public ByteArrayStreamingContent(byte[] byteArray, int offset, int length) {
    this.byteArray = Preconditions.checkNotNull(byteArray);
    Preconditions.checkArgument(offset >= 0 && length >= 0 && offset + length <= byteArray.length);
    this.offset = offset;
    this.length = length;
  }

  public void writeTo(OutputStream out) throws IOException {
    out.write(byteArray, offset, length);
    out.flush();
  }
}
