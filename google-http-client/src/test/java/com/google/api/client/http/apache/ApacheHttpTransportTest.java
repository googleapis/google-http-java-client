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

package com.google.api.client.http.apache;

import com.google.api.client.http.ByteArrayContent;

import junit.framework.TestCase;
import org.apache.http.HttpVersion;
import org.apache.http.client.HttpClient;
import org.apache.http.client.params.ClientPNames;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.DefaultHttpRequestRetryHandler;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;

import java.io.IOException;

/**
 * Tests {@link ApacheHttpTransport}.
 *
 * @author Yaniv Inbar
 */
public class ApacheHttpTransportTest extends TestCase {

  public void testApacheHttpTransport() {
    ApacheHttpTransport transport = new ApacheHttpTransport();
    DefaultHttpClient httpClient = (DefaultHttpClient) transport.getHttpClient();
    checkDefaultHttpClient(httpClient);
    checkHttpClient(httpClient);
  }

  public void testApacheHttpTransportWithParam() {
    ApacheHttpTransport transport = new ApacheHttpTransport(new DefaultHttpClient());
    checkHttpClient(transport.getHttpClient());
  }

  public void testNewDefaultHttpClient() {
    checkDefaultHttpClient(ApacheHttpTransport.newDefaultHttpClient());
  }

  public void testRequestsWithContent() throws IOException {
    ApacheHttpTransport transport = new ApacheHttpTransport();

    // Test GET.
    subtestUnsupportedRequestsWithContent(transport.buildGetRequest("http://www.test.url"), "GET");
    // Test DELETE.
    subtestUnsupportedRequestsWithContent(
        transport.buildDeleteRequest("http://www.test.url"), "DELETE");
    // Test HEAD.
    subtestUnsupportedRequestsWithContent(
        transport.buildHeadRequest("http://www.test.url"), "HEAD");

    // Test PUT.
    subtestSupportedRequestsWithContent(transport.buildPutRequest("http://www.test.url"));
    // Test POST.
    subtestSupportedRequestsWithContent(transport.buildPostRequest("http://www.test.url"));
    // Test PATCH.
    subtestSupportedRequestsWithContent(transport.buildPatchRequest("http://www.test.url"));
  }

  private void subtestUnsupportedRequestsWithContent(ApacheHttpRequest request, String method)
      throws IOException {
    try {
      request.setContent(ByteArrayContent.fromString("text/html", "abc"));
      fail("expected " + IllegalArgumentException.class);
    } catch (IllegalArgumentException e) {
      // expected
      assertEquals(e.getMessage(),
          "Apache HTTP client does not support " + method + " requests with content.");
    }
  }

  private void subtestSupportedRequestsWithContent(ApacheHttpRequest request) throws IOException {
    request.setContent(ByteArrayContent.fromString("text/html", "abc"));
  }

  private void checkDefaultHttpClient(DefaultHttpClient client) {
    HttpParams params = client.getParams();
    assertTrue(client.getConnectionManager() instanceof ThreadSafeClientConnManager);
    assertEquals(8192, params.getIntParameter(CoreConnectionPNames.SOCKET_BUFFER_SIZE, -1));
    DefaultHttpRequestRetryHandler retryHandler =
        (DefaultHttpRequestRetryHandler) client.getHttpRequestRetryHandler();
    assertEquals(0, retryHandler.getRetryCount());
    assertFalse(retryHandler.isRequestSentRetryEnabled());
  }

  private void checkHttpClient(HttpClient client) {
    HttpParams params = client.getParams();
    assertFalse(params.getBooleanParameter(ClientPNames.HANDLE_REDIRECTS, true));
    assertEquals(HttpVersion.HTTP_1_1, HttpProtocolParams.getVersion(params));
  }
}
