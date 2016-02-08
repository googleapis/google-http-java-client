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

import java.io.IOException;
import java.io.OutputStream;

final class SizingOutputStream extends OutputStream {

  final OutputStream output;
  final long limit;

  /** Number of bytes written. */
  long count;

  public SizingOutputStream(final OutputStream output, final long limit) {
    this.output = output;
    this.limit = limit;
  }

  @Override
  public void write(byte[] b, int off, int len) throws IOException {
    count += len;
    if (!isOversized()) {
      output.write(b, off, len);
    }
  }

  @Override
  public void write(int b) throws IOException {
    count++;
    if (!isOversized()) {
      output.write(b);
    }
  }

  public boolean isOversized() {
    return count > limit;
  }
}
