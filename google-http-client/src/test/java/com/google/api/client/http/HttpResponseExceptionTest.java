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

import com.google.api.client.testing.http.HttpTesting;
import com.google.api.client.testing.http.MockHttpTransport;
import com.google.api.client.testing.http.MockLowLevelHttpRequest;
import com.google.api.client.testing.http.MockLowLevelHttpResponse;

import junit.framework.TestCase;

import java.io.IOException;

/**
 * Tests {@link HttpResponseException}.
 *
 * @author Yaniv Inbar
 */
public class HttpResponseExceptionTest extends TestCase {

  public void testConstructor() throws IOException {
    HttpTransport transport = new MockHttpTransport();
    HttpRequest request =
        transport.createRequestFactory().buildGetRequest(HttpTesting.SIMPLE_GENERIC_URL);
    HttpResponse response = request.execute();
    HttpResponseException e = new HttpResponseException(response);
    assertEquals("200", e.getMessage());
  }

  public void testConstructorWithMessage() throws IOException {
    HttpTransport transport = new MockHttpTransport();
    HttpRequest request =
        transport.createRequestFactory().buildGetRequest(HttpTesting.SIMPLE_GENERIC_URL);
    HttpResponse response = request.execute();
    HttpResponseException e = new HttpResponseException(response, "foo");
    assertEquals("foo", e.getMessage());
  }

  public void testComputeMessage() throws IOException {
    HttpTransport transport = new MockHttpTransport() {
      @Override
      public LowLevelHttpRequest buildGetRequest(String url) throws IOException {
        return new MockLowLevelHttpRequest() {
          @Override
          public LowLevelHttpResponse execute() throws IOException {
            MockLowLevelHttpResponse result = new MockLowLevelHttpResponse();
            result.setReasonPhrase("Foo");
            return result;
          }
        };
      }
    };
    HttpRequest request =
        transport.createRequestFactory().buildGetRequest(HttpTesting.SIMPLE_GENERIC_URL);
    HttpResponse response = request.execute();
    assertEquals("200 Foo", HttpResponseException.computeMessage(response));
  }
}
