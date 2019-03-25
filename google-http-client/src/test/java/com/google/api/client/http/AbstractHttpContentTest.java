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
import java.util.Arrays;
import junit.framework.TestCase;

/**
 * Tests {@link AbstractHttpContent}.
 *
 * @author Yaniv Inbar
 */
public class AbstractHttpContentTest extends TestCase {

  static class TestHttpContent extends AbstractHttpContent {

    private final boolean retrySupported;
    private final int length;

    TestHttpContent(boolean retrySupported, int length) {
      super("foo/bar");
      this.length = length;
      this.retrySupported = retrySupported;
    }

    @Override
    public String getType() {
      return null;
    }

    public void writeTo(OutputStream out) throws IOException {
      byte[] content = new byte[length];
      Arrays.fill(content, (byte) 32);
      out.write(content);
    }

    @Override
    public boolean retrySupported() {
      return retrySupported;
    }
  }

  public void testRetrySupported() {
    AbstractHttpContent content = new TestHttpContent(true, 0);
    assertTrue(content.retrySupported());
  }

  public void testComputeLength() throws Exception {
    subtestComputeLength(true, 0, 0);
    subtestComputeLength(true, 1, 1);
    subtestComputeLength(true, 2, 2);
    subtestComputeLength(false, -1, 2);
  }

  public void subtestComputeLength(boolean retrySupported, long expectedLengthHeader, int length)
      throws Exception {
    AbstractHttpContent content = new TestHttpContent(retrySupported, length);
    assertEquals(expectedLengthHeader, content.computeLength());
  }
}
