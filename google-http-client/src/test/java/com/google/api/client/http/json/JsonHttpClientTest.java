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
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson.JacksonFactory;

import java.io.IOException;

import junit.framework.Assert;
import junit.framework.TestCase;

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

  static private class TestRemoteRequestInitializer implements RemoteRequestInitializer {

    boolean isCalled;

    TestRemoteRequestInitializer() {
    }

    public void initialize(RemoteRequest request) {
      isCalled = true;
    }
  }

  public void testJsonHttpClientBuilder() {
    HttpTransport transport = new NetHttpTransport();
    JsonFactory jsonFactory = new JacksonFactory();
    GenericUrl baseUrl = new GenericUrl("http://www.testgoogleapis.com/test/path/v1");
    RemoteRequestInitializer remoteRequestInitializer = new TestRemoteRequestInitializer();
    String applicationName = "Test Application";

    JsonHttpClient client =
        JsonHttpClient.builder(transport, jsonFactory, baseUrl)
            .setRemoteRequestInitializer(remoteRequestInitializer)
            .setApplicationName(applicationName).build();

    Assert.assertEquals(baseUrl.build(), client.getBaseUrl());
    Assert.assertEquals(applicationName, client.getApplicationName());
    Assert.assertEquals(jsonFactory, client.getJsonFactory());
    Assert.assertEquals(remoteRequestInitializer, client.getRemoteRequestInitializer());
  }

  public void testInitialize() throws IOException {
    TestRemoteRequestInitializer remoteRequestInitializer = new TestRemoteRequestInitializer();
    JsonHttpClient client =
      JsonHttpClient.builder(
          new NetHttpTransport(), new JacksonFactory(), new GenericUrl("http://www.test.com"))
              .setRemoteRequestInitializer(remoteRequestInitializer)
              .setApplicationName("Test Application").build();
    client.initialize(null);
    Assert.assertTrue(remoteRequestInitializer.isCalled);
  }
}
