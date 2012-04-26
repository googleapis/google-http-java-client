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

import junit.framework.TestCase;

import java.io.IOException;
import java.net.HttpURLConnection;

/**
 * Tests {@link NetHttpResponse}.
 *
 * @author Yaniv Inbar
 */
public class NetHttpResponseTest extends TestCase {

  static class MockHttpURLConnection extends HttpURLConnection {

    private final int mockResponseCode;

    protected MockHttpURLConnection(int responseCode) {
      super(null);
      this.mockResponseCode = responseCode;
    }

    @Override
    public void disconnect() {
    }

    @Override
    public boolean usingProxy() {
      return false;
    }

    @Override
    public void connect() throws IOException {
    }

    @Override
    public int getResponseCode() throws IOException {
      return mockResponseCode;
    }

  }

  public void testGetStatusCode() throws IOException {
    assertEquals(0, new NetHttpResponse(new MockHttpURLConnection(-1)).getStatusCode());
    assertEquals(200, new NetHttpResponse(new MockHttpURLConnection(200)).getStatusCode());
  }
}
