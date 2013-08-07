/*
 * Copyright (c) 2012 Google Inc.
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

package com.google.api.client.http.javanet;

import com.google.api.client.testing.http.javanet.MockHttpURLConnection;

import junit.framework.TestCase;

import java.io.IOException;

/**
 * Tests {@link NetHttpResponse}.
 *
 * @author Yaniv Inbar
 */
public class NetHttpResponseTest extends TestCase {

  public void testGetStatusCode() throws IOException {
    subtestGetStatusCode(0, -1);
    subtestGetStatusCode(200, 200);
    subtestGetStatusCode(404, 404);
  }

  public void subtestGetStatusCode(int expectedCode, int responseCode) throws IOException {
    assertEquals(expectedCode, new NetHttpResponse(
        new MockHttpURLConnection(null).setResponseCode(responseCode)).getStatusCode());
  }

  public void testGetContent() throws IOException {
    subtestGetContent(0);
    subtestGetContent(200);
    subtestGetContent(304);
    subtestGetContent(307);
    subtestGetContent(404);
    subtestGetContent(503);
  }

  public void subtestGetContent(int responseCode) throws IOException {
    NetHttpResponse response =
        new NetHttpResponse(new MockHttpURLConnection(null).setResponseCode(responseCode));
    int bytes = response.getContent().read(new byte[100]);
    if (responseCode < 400) {
      assertEquals(MockHttpURLConnection.INPUT_BUF.length, bytes);
    } else {
      assertEquals(MockHttpURLConnection.ERROR_BUF.length, bytes);
    }
  }
}
