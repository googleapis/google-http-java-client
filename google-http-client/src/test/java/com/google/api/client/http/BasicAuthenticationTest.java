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

import static org.junit.Assert.assertEquals;

import com.google.api.client.testing.http.HttpTesting;
import com.google.api.client.testing.http.MockHttpTransport;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/**
 * Tests {@link BasicAuthentication}.
 *
 * @author Yaniv Inbar
 */
@RunWith(JUnit4.class)
public class BasicAuthenticationTest {

  static final String USERNAME = "Aladdin";

  static final String PASSWORD = "open sesame";

  static final String AUTH_HEADER = "Basic QWxhZGRpbjpvcGVuIHNlc2FtZQ==";

  @Test
  public void testConstructor() {
    BasicAuthentication auth = new BasicAuthentication(USERNAME, PASSWORD);
    assertEquals(USERNAME, auth.getUsername());
    assertEquals(PASSWORD, auth.getPassword());
  }

  @Test
  public void testInitialize() throws Exception {
    BasicAuthentication auth = new BasicAuthentication(USERNAME, PASSWORD);
    HttpRequest request =
        new MockHttpTransport()
            .createRequestFactory()
            .buildGetRequest(HttpTesting.SIMPLE_GENERIC_URL);
    auth.intercept(request);
    assertEquals(AUTH_HEADER, request.getHeaders().getAuthorization());
  }
}
