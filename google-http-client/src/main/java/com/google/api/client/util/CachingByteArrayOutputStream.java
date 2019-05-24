/*
 * Copyright 2019 Google LLC
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

import java.io.ByteArrayOutputStream;

/**
 * Output stream that extends the built-in {@link ByteArrayOutputStream} to return the internal
 * byte buffer rather than creating a copy.
 */
public class CachingByteArrayOutputStream extends ByteArrayOutputStream {

  /**
   * Returns the content length of the buffer.
   *
   * @return tthe content length of the buffer.
   */
  public int getContentLength() {
    return count;
  }

  /**
   * Returns the buffer where the byte data is stored.
   *
   * @return the buffer where the byte data is stored.
   */
  public byte[] getBuffer() {
    return buf;
  }

}
