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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.google.api.client.http.LowLevelHttpResponse;
import com.google.api.client.util.ByteArrayStreamingContent;
import com.google.api.client.util.StringUtils;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import org.apache.http.Header;
import org.apache.http.HttpClientConnection;
import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicHttpResponse;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpRequestExecutor;
import org.junit.Test;

/**
 * Tests {@link ApacheHttpTransport}.
 *
 * @author Yaniv Inbar
 */
public class ApacheHttpTransportTest {

  @Test
  public void testApacheHttpTransport() {
    ApacheHttpTransport transport = new ApacheHttpTransport();
    DefaultHttpClient httpClient = (DefaultHttpClient) transport.getHttpClient();
    checkDefaultHttpClient(httpClient);
    checkHttpClient(httpClient);
  }

  @Test
  public void testApacheHttpTransportWithParam() {
    ApacheHttpTransport transport = new ApacheHttpTransport(new DefaultHttpClient());
    checkHttpClient(transport.getHttpClient());
  }

  @Test
  public void testNewDefaultHttpClient() {
    HttpClient client = ApacheHttpTransport.newDefaultHttpClient();
    checkHttpClient(client);
  }

  private void checkHttpTransport(ApacheHttpTransport transport) {
    assertNotNull(transport);
    HttpClient client = transport.getHttpClient();
    checkHttpClient(client);
  }

  private void checkHttpClient(HttpClient client) {
    assertNotNull(client);
    // TODO(chingor): Is it possible to test this effectively? The newer HttpClient implementations
    // are read-only and we're testing that we built the client with the right configuration
  }

  @Test
  public void testRequestsWithContent() throws Exception {
    HttpClient mockClient = mock(HttpClient.class);
    HttpResponse mockResponse = mock(HttpResponse.class);
    when(mockClient.execute(any(HttpUriRequest.class))).thenReturn(mockResponse);

    ApacheHttpTransport transport = new ApacheHttpTransport(mockClient);

    // Test GET.
    subtestUnsupportedRequestsWithContent(
        transport.buildRequest("GET", "http://www.test.url"), "GET");
    // Test DELETE.
    subtestUnsupportedRequestsWithContent(
        transport.buildRequest("DELETE", "http://www.test.url"), "DELETE");
    // Test HEAD.
    subtestUnsupportedRequestsWithContent(
        transport.buildRequest("HEAD", "http://www.test.url"), "HEAD");

    // Test PATCH.
    execute(transport.buildRequest("PATCH", "http://www.test.url"));
    // Test PUT.
    execute(transport.buildRequest("PUT", "http://www.test.url"));
    // Test POST.
    execute(transport.buildRequest("POST", "http://www.test.url"));
    // Test PATCH.
    execute(transport.buildRequest("PATCH", "http://www.test.url"));
  }

  private void subtestUnsupportedRequestsWithContent(ApacheHttpRequest request, String method)
      throws Exception {
    try {
      execute(request);
      fail("expected " + IllegalArgumentException.class);
    } catch (IllegalArgumentException e) {
      // expected
      assertEquals(
          e.getMessage(),
          "Apache HTTP client does not support " + method + " requests with content.");
    }
  }

  private void execute(ApacheHttpRequest request) throws Exception {
    byte[] bytes = StringUtils.getBytesUtf8("abc");
    request.setStreamingContent(new ByteArrayStreamingContent(bytes));
    request.setContentType("text/html");
    request.setContentLength(bytes.length);
    request.execute();
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
