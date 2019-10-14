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

import com.google.common.io.ByteStreams;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * This class in meant to wrap an {@link InputStream} so that all bytes in the steam are read and
 * discarded on {@link InputStream#close()}. This ensures that the underlying connection has the
 * option to be reused.
 */
final class ConsumingInputStream extends FilterInputStream {
  private boolean closed = false;

  ConsumingInputStream(InputStream inputStream) {
    super(inputStream);
  }

  @Override
  public void close() throws IOException {
    if (!closed && in != null) {
      try {
        ByteStreams.exhaust(this);
        super.in.close();
      } finally {
        this.closed = true;
      }
    }
  }
}
