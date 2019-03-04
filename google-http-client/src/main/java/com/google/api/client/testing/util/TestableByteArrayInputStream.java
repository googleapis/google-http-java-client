/*
 * Copyright (c) 2013 Google Inc.
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

package com.google.api.client.testing.util;

import com.google.api.client.util.Beta;
import java.io.ByteArrayInputStream;
import java.io.IOException;

/**
 * {@link Beta} <br>
 * Testable extension for a byte array input stream.
 *
 * @author Yaniv Inbar
 * @since 1.14
 */
@Beta
public class TestableByteArrayInputStream extends ByteArrayInputStream {

  /** Whether the input stream has been closed. */
  private boolean closed;

  /** @param buf buffer */
  public TestableByteArrayInputStream(byte[] buf) {
    super(buf);
  }

  /**
   * @param buf buffer
   * @param offset offset in the buffer of the first byte to read
   * @param length maximum number of bytes to read from the buffer
   */
  public TestableByteArrayInputStream(byte buf[], int offset, int length) {
    super(buf);
  }

  /**
   * {@inheritDoc}
   *
   * <p>Overriding is supported, but overriding method must call the super implementation.
   */
  @Override
  public void close() throws IOException {
    closed = true;
  }

  /** Returns the written buffer value as a modifiable byte array. */
  public final byte[] getBuffer() {
    return buf;
  }

  /** Returns whether the output stream has been closed. */
  public final boolean isClosed() {
    return closed;
  }
}
