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

package com.google.api.client.http.json;

import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpMethod;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.LowLevelHttpRequest;
import com.google.api.client.http.LowLevelHttpResponse;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson.JacksonFactory;
import com.google.api.client.testing.http.MockHttpTransport;
import com.google.api.client.testing.http.MockLowLevelHttpRequest;
import com.google.api.client.testing.http.MockLowLevelHttpResponse;

import junit.framework.TestCase;

import java.io.IOException;

/**
 * Tests {@link JsonHttpClient}.
 *
 * @author Ravi Mistry
 */
public class JsonHttpClientTest extends TestCase {

  public JsonHttpClientTest() {
  }

  public JsonHttpClientTest(String name) {
    super(name);
  }

  static private class TestRemoteRequestInitializer implements JsonHttpRequestInitializer {

    boolean isCalled;

    TestRemoteRequestInitializer() {
    }

    public void initialize(JsonHttpRequest request) {
      isCalled = true;
    }
  }

  public void testJsonHttpClientBuilder() {
    HttpTransport transport = new NetHttpTransport();
    JsonFactory jsonFactory = new JacksonFactory();
    GenericUrl baseUrl = new GenericUrl("http://www.testgoogleapis.com/test/path/v1/");
    JsonHttpRequestInitializer jsonHttpRequestInitializer = new TestRemoteRequestInitializer();
    String applicationName = "Test Application";

    JsonHttpClient client =
        JsonHttpClient.builder(transport, jsonFactory, baseUrl)
            .setJsonHttpRequestInitializer(jsonHttpRequestInitializer)
            .setApplicationName(applicationName).build();

    assertEquals(baseUrl.build(), client.getBaseUrl());
    assertEquals(applicationName, client.getApplicationName());
    assertEquals(jsonFactory, client.getJsonFactory());
    assertEquals(jsonHttpRequestInitializer, client.getJsonHttpRequestInitializer());
  }

  public void testBaseServerAndBasePathBuilder() {
    JsonHttpClient client =
        JsonHttpClient
            .builder(new NetHttpTransport(), new JacksonFactory(),
                new GenericUrl("http://www.testgoogleapis.com/test/path/v1/"))
            .setBaseUrl(new GenericUrl("http://www.googleapis.com/test/path/v2/"))
            .build();

    assertEquals("http://www.googleapis.com/test/path/v2/", client.getBaseUrl());
  }

  public void testInitialize() throws IOException {
    TestRemoteRequestInitializer remoteRequestInitializer = new TestRemoteRequestInitializer();
    JsonHttpClient client =
      JsonHttpClient.builder(
          new NetHttpTransport(), new JacksonFactory(), new GenericUrl("http://www.test.com/"))
              .setJsonHttpRequestInitializer(remoteRequestInitializer)
              .setApplicationName("Test Application").build();
    client.initialize(null);
    assertTrue(remoteRequestInitializer.isCalled);
  }

  public void testExecute() throws IOException {
    final String testBaseUrl = "http://www.test.com/";
    final String testUriTemplate = "uri/template";
    HttpTransport transport = new MockHttpTransport() {
      @Override
      public LowLevelHttpRequest buildGetRequest(final String url) {
        return new MockLowLevelHttpRequest() {
          @Override
          public LowLevelHttpResponse execute() {
            MockLowLevelHttpResponse response = new MockLowLevelHttpResponse();
            // Assert the requested URL is the expected one.
            assertEquals(testBaseUrl + testUriTemplate, url);
            return response;
          }
        };
      }
    };
    JsonHttpClient client =
      JsonHttpClient.builder(
          transport, new JacksonFactory(), new GenericUrl(testBaseUrl)).build();
    client.executeUnparsed(HttpMethod.GET, new GenericUrl(testBaseUrl + testUriTemplate), null);
  }
}
