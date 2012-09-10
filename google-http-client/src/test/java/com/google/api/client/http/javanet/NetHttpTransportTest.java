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

import com.google.api.client.http.ByteArrayContent;
import com.google.api.client.testing.http.HttpTesting;
import com.google.api.client.testing.http.javanet.MockHttpURLConnection;

import junit.framework.TestCase;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Tests {@link NetHttpTransport}.
 *
 * @author Yaniv Inbar
 */
public class NetHttpTransportTest extends TestCase {

  private static final String[] METHODS =
      {"GET", "POST", "HEAD", "OPTIONS", "PUT", "DELETE", "TRACE"};

  @Deprecated
  public void testSupportsHead() {
    NetHttpTransport transport = new NetHttpTransport();
    assertTrue(transport.supportsHead());
  }

  public void testExecute_mock() throws IOException {
    for (String method : METHODS) {
      boolean isPutOrPost = method.equals("PUT") || method.equals("POST");
      MockHttpURLConnection connection = new MockHttpURLConnection(new URL(HttpTesting.SIMPLE_URL));
      NetHttpRequest request = new NetHttpRequest(method, connection);
      request.setContent(ByteArrayContent.fromString(null, ""));
      request.execute();
      assertEquals(isPutOrPost, connection.doOutputCalled());
      request.setContent(ByteArrayContent.fromString(null, " "));
      if (isPutOrPost) {
        request.execute();
      } else {
        try {
          request.execute();
          fail("expected " + IllegalArgumentException.class);
        } catch (IllegalArgumentException e) {
          // expected
        }
      }
      assertEquals(isPutOrPost, connection.doOutputCalled());
    }
  }

  public void testExecute_methodUnchanged() throws IOException {
    for (String method : METHODS) {
      HttpURLConnection connection =
          (HttpURLConnection) new URL("http://www.google.com").openConnection();
      NetHttpRequest request = new NetHttpRequest(method, connection);
      request.setContent(ByteArrayContent.fromString("text/html", ""));
      request.execute().getContent().close();
      assertEquals(method, connection.getRequestMethod());
    }
  }
}
