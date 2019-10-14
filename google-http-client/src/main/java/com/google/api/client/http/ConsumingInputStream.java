/*
 * Copyright 2019 Google LLC
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

import com.google.api.client.util.Preconditions;
import com.google.common.io.ByteStreams;
import java.io.IOException;
import java.io.InputStream;

/**
 * This class in meant to wrap an {@link InputStream} so that all bytes in the steam are read and
 * discarded on {@link InputStream#close()}. This ensures that the underlying connection has the
 * option to be reused.
 */
final class ConsumingInputStream extends InputStream {
  private InputStream inputStream;
  private boolean closed = false;

  ConsumingInputStream(InputStream inputStream) {
    this.inputStream = Preconditions.checkNotNull(inputStream);
  }

  @Override
  public int read() throws IOException {
    return inputStream.read();
  }

  @Override
  public int read(byte[] b, int off, int len) throws IOException {
    return inputStream.read(b, off, len);
  }

  @Override
  public void close() throws IOException {
    if (!closed && inputStream != null) {
      try {
        ByteStreams.exhaust(this);
        inputStream.close();
      } finally {
        this.closed = true;
      }
    }
  }
}
