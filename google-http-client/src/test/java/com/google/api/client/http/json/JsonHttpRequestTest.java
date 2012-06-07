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

package com.google.api.client.http.json;

import com.google.api.client.http.HttpMethod;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson.JacksonFactory;
import com.google.api.client.testing.http.HttpTesting;
import com.google.api.client.testing.http.MockHttpTransport;

import junit.framework.TestCase;

import java.io.IOException;

/**
 * Tests {@link JsonHttpRequest}.
 *
 * @author rmistry@google.com (Ravi Mistry)
 */
public class JsonHttpRequestTest extends TestCase {

  private static class TestJsonHttpClient extends JsonHttpClient {

    boolean gzipContentEnabled;

    public TestJsonHttpClient(HttpTransport transport, JsonFactory jsonFactory, String rootUrl,
        String servicePath, HttpRequestInitializer httpRequestInitializer) {
      super(transport, jsonFactory, rootUrl, servicePath, httpRequestInitializer);
    }

    @Override
    protected HttpResponse executeUnparsed(HttpRequest request) throws IOException {
      if (gzipContentEnabled) {
        assertTrue(request.getEnableGZipContent());
      } else {
        assertFalse(request.getEnableGZipContent());
      }
      return request.execute();
    }
  }

  public void testEnableGZipContent() throws IOException {

    // Verify that enableGZipContent is true by default.
    TestJsonHttpClient client = new TestJsonHttpClient(
        new MockHttpTransport(), new JacksonFactory(), HttpTesting.SIMPLE_URL, "test/", null);
    client.gzipContentEnabled = true;
    JsonHttpRequest jsonHttpRequest =
        new JsonHttpRequest(client, HttpMethod.POST, "", "test content");
    jsonHttpRequest.executeUnparsed();

    // Set enableGZipContent to false and assert.
    client.gzipContentEnabled = false;
    jsonHttpRequest.setEnableGZipContent(false);
    jsonHttpRequest.executeUnparsed();

    // Set enableGZipContent to true and assert.
    client.gzipContentEnabled = true;
    jsonHttpRequest.setEnableGZipContent(true);
    jsonHttpRequest.executeUnparsed();
  }
}
