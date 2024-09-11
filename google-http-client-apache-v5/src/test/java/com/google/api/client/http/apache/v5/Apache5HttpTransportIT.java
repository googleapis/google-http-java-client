/*
 * Copyright 2024 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.api.client.http.apache.v5;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpTransport;
import com.google.common.collect.ImmutableMap;
import java.io.IOException;
import org.apache.hc.client5.http.ConnectTimeoutException;
import org.junit.Test;

public class Apache5HttpTransportIT {
  private static final ImmutableMap<String, Object> payload =
      ImmutableMap.<String, Object>of("foo", "bar");

  // Sets a 5 second delay before response
  private static final String NO_CONNECT_URL = "https://google.com:81";

  @Test(timeout = 10_000L)
  public void testConnectTimeoutGet() throws IOException {
    HttpTransport transport = new Apache5HttpTransport();
    try {
      transport
          .createRequestFactory()
          .buildGetRequest(new GenericUrl(NO_CONNECT_URL))
          .setConnectTimeout(100)
          .execute();
      fail("No exception thrown for HTTP error response");
    } catch (ConnectTimeoutException e) {
      assertTrue(
          "Expected exception message to contain a connection timeout message",
          e.getMessage().contains("Connect timed out"));
    }
  }

  @Test(timeout = 10_000L)
  public void testConnectTimeoutPost() throws IOException {
    Apache5HttpTransport transport = new Apache5HttpTransport();
    Apache5HttpRequest request = transport.buildRequest("POST", NO_CONNECT_URL);
    request.setTimeout(100, 0);
    try {
      request.execute();
      fail("No exception thrown for HTTP error response");
    } catch (ConnectTimeoutException e) {
      assertTrue(
          "Expected exception message to contain a connection timeout message",
          e.getMessage().contains("Connect timed out"));
    }
  }
}
