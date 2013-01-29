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

package com.google.api.client.http;


import com.google.api.client.util.ByteArrayStreamingContent;
import com.google.api.client.util.StringUtils;

import junit.framework.TestCase;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * Tests {@link GZipEncoding}.
 *
 * @author Yaniv Inbar
 */
public class GZipEncodingTest extends TestCase {

  public void test() throws IOException {
    final boolean[] closed = {false};
    GZipEncoding encoding = new GZipEncoding();
    ByteArrayStreamingContent content =
        new ByteArrayStreamingContent(StringUtils.getBytesUtf8("oooooooooooooooooooooooooooo"));
    ByteArrayOutputStream out = new ByteArrayOutputStream() {

      @Override
      public void close() throws IOException {
        closed[0] = true;
      }

    };
    encoding.encode(content, out);
    assertEquals(23, out.size());
    assertFalse(closed[0]);
  }
}
