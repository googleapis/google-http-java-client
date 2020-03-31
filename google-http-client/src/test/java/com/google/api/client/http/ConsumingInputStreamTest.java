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

import static org.junit.Assert.assertEquals;

import com.google.api.client.util.Charsets;
import java.io.IOException;
import java.io.InputStream;

import org.junit.Test;

public class ConsumingInputStreamTest {

  @Test
  public void testClose_drainsBytesOnClose() throws IOException {
    MockInputStream mockInputStream = new MockInputStream("abc123".getBytes(Charsets.UTF_8));
    InputStream consumingInputStream = new ConsumingInputStream(mockInputStream);

    assertEquals(6, mockInputStream.getBytesToRead());

    // read one byte
    consumingInputStream.read();
    assertEquals(5, mockInputStream.getBytesToRead());

    // closing the stream should read the remaining bytes
    consumingInputStream.close();
    assertEquals(0, mockInputStream.getBytesToRead());
  }

  private class MockInputStream extends InputStream {
    private int bytesToRead;

    MockInputStream(byte[] data) {
      this.bytesToRead = data.length;
    }

    @Override
    public int read() throws IOException {
      if (bytesToRead == 0) {
        return -1;
      }
      bytesToRead--;
      return 1;
    }

    int getBytesToRead() {
      return bytesToRead;
    }
  }
}
