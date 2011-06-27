/*
 * Copyright (c) 2011 Google Inc.
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
import java.io.OutputStream;

/**
 * Output stream that throws away any content and only retains the count of bytes written to the
 * stream.
 *
 * @author Yaniv Inbar
 */
final class ByteCountingOutputStream extends OutputStream {

  /** Number of bytes written. */
  long count;

  @Override
  public void write(byte[] b, int off, int len) throws IOException {
    count += len;
  }

  @Override
  public void write(int b) throws IOException {
    count++;
  }
}
