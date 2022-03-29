/*
 * Copyright 2022 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.api.client.http;

import static com.google.common.truth.Truth.assertThat;

import com.google.common.io.ByteStreams;
import com.google.common.io.CountingInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.SequenceInputStream;
import java.util.zip.GZIPInputStream;
import org.junit.Test;

public final class GzipSupportTest {

  @SuppressWarnings("UnstableApiUsage") // CountingInputStream is @Beta
  @Test
  public void gzipInputStreamConsumesAllBytes() throws IOException {
    byte[] data = new byte[] {(byte) 'a', (byte) 'b'};
    // `echo -n a > a.txt && gzip -n9 a.txt`
    byte[] member0 =
        new byte[] {
          0x1f,
          (byte) 0x8b,
          0x08,
          0x00,
          0x00,
          0x00,
          0x00,
          0x00,
          0x02,
          0x03,
          0x4b,
          0x04,
          0x00,
          (byte) 0x43,
          (byte) 0xbe,
          (byte) 0xb7,
          (byte) 0xe8,
          0x01,
          0x00,
          0x00,
          0x00
        };
    // `echo -n b > b.txt && gzip -n9 b.txt`
    byte[] member1 =
        new byte[] {
          0x1f,
          (byte) 0x8b,
          0x08,
          0x00,
          0x00,
          0x00,
          0x00,
          0x00,
          0x02,
          0x03,
          0x4b,
          0x02,
          0x00,
          (byte) 0xf9,
          (byte) 0xef,
          (byte) 0xbe,
          (byte) 0x71,
          0x01,
          0x00,
          0x00,
          0x00
        };
    int totalZippedBytes = member0.length + member1.length;
    try (InputStream s =
            new SequenceInputStream(
                new ByteArrayInputStream(member0), new ByteArrayInputStream(member1));
        CountingInputStream countS = new CountingInputStream(s);
        GZIPInputStream g = GzipSupport.newGzipInputStream(countS);
        CountingInputStream countG = new CountingInputStream(g)) {
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      ByteStreams.copy(countG, baos);
      assertThat(baos.toByteArray()).isEqualTo(data);
      assertThat(countG.getCount()).isEqualTo(data.length);
      assertThat(countS.getCount()).isEqualTo(totalZippedBytes);
    }
  }
}
