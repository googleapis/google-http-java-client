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
import com.google.api.client.testing.http.MockHttpTransport;
import com.google.api.client.testing.http.MockLowLevelHttpRequest;
import com.google.api.client.testing.http.MockLowLevelHttpResponse;
import com.google.api.client.testing.http.json.MockJsonFactory;

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

  @SuppressWarnings("deprecation")
  public void testJsonHttpClientBuilderWithBaseUrl() {
    HttpTransport transport = new NetHttpTransport();
    JsonFactory jsonFactory = new MockJsonFactory();
    GenericUrl baseUrl = new GenericUrl("http://www.testgoogleapis.com/test/path/v1/");
    JsonHttpRequestInitializer jsonHttpRequestInitializer = new TestRemoteRequestInitializer();
    String applicationName = "Test Application";

    JsonHttpClient.Builder builder =
        new JsonHttpClient.Builder(transport, jsonFactory, baseUrl)
            .setJsonHttpRequestInitializer(jsonHttpRequestInitializer)
        .setApplicationName(applicationName);
    try {
      builder.getServicePath();
      fail("Expected " + IllegalArgumentException.class);
    } catch (IllegalArgumentException e) {
      // expected
      assertEquals("service path cannot be used if base URL is used.", e.getMessage());
    }
    try {
      builder.setServicePath("test/");
      fail("Expected " + IllegalArgumentException.class);
    } catch (IllegalArgumentException e) {
      // expected
      assertEquals("service path cannot be used if base URL is used.", e.getMessage());
    }
    try {
      builder.getRootUrl();
      fail("Expected " + IllegalArgumentException.class);
    } catch (IllegalArgumentException e) {
      // expected
      assertEquals("root URL cannot be used if base URL is used.", e.getMessage());
    }
    try {
      builder.setRootUrl("http://www.test.com/");
      fail("Expected " + IllegalArgumentException.class);
    } catch (IllegalArgumentException e) {
      // expected
      assertEquals("root URL cannot be used if base URL is used.", e.getMessage());
    }

    JsonHttpClient client = builder.build();

    assertEquals(baseUrl.build(), client.getBaseUrl());
    assertEquals(applicationName, client.getApplicationName());
    assertEquals(jsonFactory, client.getJsonFactory());
    assertEquals(jsonHttpRequestInitializer, client.getJsonHttpRequestInitializer());
    try {
      client.getServicePath();
      fail("Expected " + IllegalArgumentException.class);
    } catch (IllegalArgumentException e) {
      // expected
      assertEquals("service path cannot be used if base URL is used.", e.getMessage());
    }
    try {
      client.getRootUrl();
      fail("Expected " + IllegalArgumentException.class);
    } catch (IllegalArgumentException e) {
      // expected
      assertEquals("root URL cannot be used if base URL is used.", e.getMessage());
    }
  }

  @SuppressWarnings("deprecation")
  public void testJsonHttpClientBuilderWithRootUrlAndServicePath() {
    HttpTransport transport = new NetHttpTransport();
    JsonFactory jsonFactory = new MockJsonFactory();
    String rootUrl = "http://www.testgoogleapis.com/";
    String servicePath = "test/path/v1/";
    JsonHttpRequestInitializer jsonHttpRequestInitializer = new TestRemoteRequestInitializer();
    String applicationName = "Test Application";

    JsonHttpClient.Builder builder = new JsonHttpClient.Builder(
        transport, jsonFactory, rootUrl, servicePath, null)
        .setJsonHttpRequestInitializer(jsonHttpRequestInitializer)
        .setApplicationName(applicationName);
    try {
      builder.getBaseUrl();
      fail("Expected " + IllegalArgumentException.class);
    } catch (IllegalArgumentException e) {
      // expected
    }
    try {
      builder.setBaseUrl(new GenericUrl(rootUrl));
      fail("Expected " + IllegalArgumentException.class);
    } catch (IllegalArgumentException e) {
      // expected
    }
    // With no "/" at the end.
    builder.setRootUrl("http://www.testgoogleapis.com");
    assertEquals("http://www.testgoogleapis.com/", builder.getRootUrl());
    // With no "/" at the end.
    builder.setServicePath("test");
    assertEquals("test/", builder.getServicePath());
    // With "/" at the start.
    builder.setServicePath("/test");
    assertEquals("test/", builder.getServicePath());
    try {
      // With "?".
      builder.setServicePath("?");
      fail("Expected " + IllegalArgumentException.class);
    } catch (IllegalArgumentException e) {
      // expected
      assertEquals("service path must equal \"/\" if it is of length 1.", e.getMessage());
    }
    // Ensure an empty string is allowed for the service path.
    builder.setServicePath("");
    JsonHttpClient client = builder.build();
    assertEquals(rootUrl, client.getRootUrl());
    assertEquals("", client.getServicePath());

    // Ensure a "/" string is allowed for the service path.
    builder.setServicePath("/");
    client = builder.build();
    assertEquals(rootUrl, client.getRootUrl());
    assertEquals("", client.getServicePath());

    assertEquals(applicationName, client.getApplicationName());
    assertEquals(jsonFactory, client.getJsonFactory());
    assertEquals(jsonHttpRequestInitializer, client.getJsonHttpRequestInitializer());
    assertEquals(rootUrl, client.getBaseUrl());
  }

  @SuppressWarnings("deprecation")
  public void testBaseServerAndBasePathBuilder() {
    JsonHttpClient client =
        new JsonHttpClient.Builder(new NetHttpTransport(),
            new MockJsonFactory(), new GenericUrl("http://www.testgoogleapis.com/test/path/v1/"))
            .setBaseUrl(new GenericUrl("http://www.googleapis.com/test/path/v2/"))
            .build();

    assertEquals("http://www.googleapis.com/test/path/v2/", client.getBaseUrl());
  }

  @SuppressWarnings("deprecation")
  public void testInitializeWithBaseUrl() throws IOException {
    TestRemoteRequestInitializer remoteRequestInitializer = new TestRemoteRequestInitializer();
    JsonHttpClient client = new JsonHttpClient.Builder(new NetHttpTransport(),
        new MockJsonFactory(),
        new GenericUrl("http://www.test.com/"))
        .setJsonHttpRequestInitializer(remoteRequestInitializer)
        .setApplicationName("Test Application").build();
    client.initialize(null);
    assertTrue(remoteRequestInitializer.isCalled);
  }

  public void testInitializeWithRootUrl() throws IOException {
    TestRemoteRequestInitializer remoteRequestInitializer = new TestRemoteRequestInitializer();
    JsonHttpClient client = new JsonHttpClient.Builder(
        new NetHttpTransport(), new MockJsonFactory(), "http://www.test.com/", "test/",
        null).setJsonHttpRequestInitializer(remoteRequestInitializer)
        .setApplicationName("Test Application").build();
    client.initialize(null);
    assertTrue(remoteRequestInitializer.isCalled);
  }

  @SuppressWarnings("deprecation")
  public void testExecuteWithBaseUrl() throws IOException {
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
      new JsonHttpClient.Builder(
        transport, new MockJsonFactory(), new GenericUrl(testBaseUrl)).build();
    client.executeUnparsed(HttpMethod.GET, new GenericUrl(testBaseUrl + testUriTemplate), null);
  }

  public void testExecuteWithRootUrl() throws IOException {
    final String testRootUrl = "http://www.test.com/";
    final String testServicePath = "test123/";
    final String testUriTemplate = "uri/template";
    HttpTransport transport = new MockHttpTransport() {
        @Override
      public LowLevelHttpRequest buildGetRequest(final String url) {
        return new MockLowLevelHttpRequest() {
            @Override
          public LowLevelHttpResponse execute() {
            MockLowLevelHttpResponse response = new MockLowLevelHttpResponse();
            // Assert the requested URL is the expected one.
            assertEquals(testRootUrl + testServicePath + testUriTemplate, url);
            return response;
          }
        };
      }
    };
    JsonHttpClient client = new JsonHttpClient.Builder(
        transport, new MockJsonFactory(), testRootUrl, testServicePath, null).build();
    client.executeUnparsed(
        HttpMethod.GET, new GenericUrl(testRootUrl + testServicePath + testUriTemplate), null);
  }
}
