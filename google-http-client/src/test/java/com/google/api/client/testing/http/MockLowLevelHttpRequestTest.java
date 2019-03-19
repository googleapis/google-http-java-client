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

package com.google.api.client.testing.http;

import com.google.api.client.util.ByteArrayStreamingContent;
import com.google.api.client.util.StringUtils;
import junit.framework.TestCase;

/**
 * Tests {@link MockLowLevelHttpRequest}.
 *
 * @author Yaniv Inbar
 */
public class MockLowLevelHttpRequestTest extends TestCase {

  public void testGetContentAsString() throws Exception {
    subtestGetContentAsString("", null);
    subtestGetContentAsString("hello", "hello");
  }

  public void subtestGetContentAsString(String expected, String content) throws Exception {
    MockLowLevelHttpRequest request = new MockLowLevelHttpRequest();
    if (content != null) {
      byte[] bytes = StringUtils.getBytesUtf8(content);
      request.setStreamingContent(new ByteArrayStreamingContent(bytes));
    }
    assertEquals(expected, request.getContentAsString());
  }
}
