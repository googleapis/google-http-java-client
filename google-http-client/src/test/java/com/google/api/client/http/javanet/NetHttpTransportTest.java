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

import com.google.api.client.testing.http.HttpTesting;
import com.google.api.client.testing.http.javanet.MockHttpURLConnection;
import com.google.api.client.util.ByteArrayStreamingContent;
import com.google.api.client.util.StringUtils;

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

  public void testExecute_mock() throws Exception {
    for (String method : METHODS) {
      boolean isPutOrPost = method.equals("PUT") || method.equals("POST");
      MockHttpURLConnection connection = new MockHttpURLConnection(new URL(HttpTesting.SIMPLE_URL));
      connection.setRequestMethod(method);
      NetHttpRequest request = new NetHttpRequest(connection);
      setContent(request, null, "");
      request.execute();
      assertEquals(isPutOrPost, connection.doOutputCalled());
      setContent(request, null, " ");
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

  public void testExecute_methodUnchanged() throws Exception {
    for (String method : METHODS) {
      HttpURLConnection connection =
          (HttpURLConnection) new URL("http://www.google.com").openConnection();
      connection.setRequestMethod(method);
      NetHttpRequest request = new NetHttpRequest(connection);
      setContent(request, "text/html", "");
      try {
        request.execute().getContent().close();
      } catch (IOException e) {
        // expected when not connected to network
      }
      assertEquals(method, connection.getRequestMethod());
    }
  }

  private void setContent(NetHttpRequest request, String type, String value) throws Exception {
    byte[] bytes = StringUtils.getBytesUtf8(value);
    request.setStreamingContent(new ByteArrayStreamingContent(bytes));
    request.setContentType(type);
    request.setContentLength(bytes.length);
  }
}
