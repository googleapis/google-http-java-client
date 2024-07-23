/*
 * Copyright 2019 Google LLC
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

package com.google.api.client.http.apache.v3;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.junit.Assume.assumeFalse;
import static org.junit.Assume.assumeTrue;

import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpResponseException;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.LowLevelHttpResponse;
import com.google.api.client.testing.http.apache.MockHttpClient;
import com.google.api.client.util.ByteArrayStreamingContent;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.hc.client5.http.impl.async.CloseableHttpAsyncClient;
import org.apache.hc.client5.http.impl.async.HttpAsyncClientBuilder;
import org.apache.hc.core5.http.ClassicHttpRequest;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.HttpException;
import org.apache.hc.core5.http.HttpRequest;
import org.apache.hc.core5.http.HttpResponse;
import org.apache.hc.core5.http.HttpVersion;
import org.apache.hc.core5.http.impl.io.HttpRequestExecutor;
import org.apache.hc.core5.http.io.HttpClientConnection;
import org.apache.hc.core5.http.message.BasicHttpResponse;
import org.apache.hc.core5.http.protocol.HttpContext;
import org.junit.Assert;
import org.junit.Test;

/**
 * Tests {@link ApacheHttpTransport}.
 *
 * @author Yaniv Inbar
 */
public class ApacheHttpTransportTest {

  private static class MockHttpResponse extends BasicHttpResponse {
    public MockHttpResponse() {
      super(200, "OK");
    }
  }

  @Test
  public void testApacheHttpTransport() {
    ApacheHttpTransport transport = new ApacheHttpTransport();
    checkHttpTransport(transport);
    assertFalse(transport.isMtls());
  }

  @Test
  public void testApacheHttpTransportWithParam() {
    ApacheHttpTransport transport = new ApacheHttpTransport(HttpAsyncClientBuilder.create(), true);
    checkHttpTransport(transport);
    assertTrue(transport.isMtls());
  }

  @Test
  public void testNewDefaultHttpClient() {
    HttpAsyncClientBuilder client = ApacheHttpTransport.newDefaultHttpClientBuilder();
    checkHttpClient(client.build());
  }

  private void checkHttpTransport(ApacheHttpTransport transport) {
    assertNotNull(transport);
    HttpAsyncClientBuilder clientBuilder = transport.getHttpClientBuilder();
    checkHttpClient(clientBuilder.build());
  }

  private void checkHttpClient(CloseableHttpAsyncClient client) {
    assertNotNull(client);
    // TODO(chingor): Is it possible to test this effectively? The newer HttpClient implementations
    // are read-only and we're testing that we built the client with the right configuration
  }

//  @Test
//  public void testRequestsWithContent() throws IOException {
//    HttpClient mockClient =
//        new MockHttpClient() {
//          @Override
//          public CloseableHttpResponse execute(HttpUriRequest request)
//              throws IOException, ClientProtocolException {
//            return new MockHttpResponse();
//          }
//        };
//    ApacheHttpTransport transport = new ApacheHttpTransport(mockClient);
//
//    // Test GET.
//    subtestUnsupportedRequestsWithContent(
//        transport.buildRequest("GET", "http://www.test.url"), "GET");
//    // Test DELETE.
//    subtestUnsupportedRequestsWithContent(
//        transport.buildRequest("DELETE", "http://www.test.url"), "DELETE");
//    // Test HEAD.
//    subtestUnsupportedRequestsWithContent(
//        transport.buildRequest("HEAD", "http://www.test.url"), "HEAD");
//
//    // Test PATCH.
//    execute(transport.buildRequest("PATCH", "http://www.test.url"));
//    // Test PUT.
//    execute(transport.buildRequest("PUT", "http://www.test.url"));
//    // Test POST.
//    execute(transport.buildRequest("POST", "http://www.test.url"));
//    // Test PATCH.
//    execute(transport.buildRequest("PATCH", "http://www.test.url"));
//  }

//  private void subtestUnsupportedRequestsWithContent(ApacheHttpRequest request, String method)
//      throws IOException {
//    try {
//      execute(request);
//      fail("expected " + IllegalStateException.class);
//    } catch (IllegalStateException e) {
//      // expected
//      assertEquals(
//          e.getMessage(),
//          "Apache HTTP client does not support " + method + " requests with content.");
//    }
//  }

  private void execute(ApacheHttpRequest request) throws IOException {
    byte[] bytes = "abc".getBytes(StandardCharsets.UTF_8);
    request.setStreamingContent(new ByteArrayStreamingContent(bytes));
    request.setContentType("text/html");
    request.setContentLength(bytes.length);
    request.execute();
  }

//  @Test
//  public void testRequestShouldNotFollowRedirects() throws IOException {
//    final AtomicInteger requestsAttempted = new AtomicInteger(0);
//    HttpRequestExecutor requestExecutor =
//        new HttpRequestExecutor() {
//
//          @Override
//          public ClassicHttpResponse execute(
//                  ClassicHttpRequest request, HttpClientConnection connection, HttpContext context)
//                  throws IOException, HttpException {
//            ClassicHttpResponse response = new BasicHttpResponse(302);
//            response.addHeader("location", "https://google.com/path");
//            requestsAttempted.incrementAndGet();
//            return response;
//          }
//        };
//    CloseableHttpAsyncClient client = HttpAsyncClientBuilder.create().setRequest(requestExecutor).build();
//    ApacheHttpTransport transport = new ApacheHttpTransport(client);
//    ApacheHttpRequest request = transport.buildRequest("GET", "https://google.com");
//    LowLevelHttpResponse response = request.execute();
//    assertEquals(1, requestsAttempted.get());
//    assertEquals(302, response.getStatusCode());
//  }

//  @Test
//  public void testRequestCanSetHeaders() {
//    final AtomicBoolean interceptorCalled = new AtomicBoolean(false);
//    HttpClient client =
//        HttpClients.custom()
//            .addInterceptorFirst(
//                new HttpRequestInterceptor() {
//                  @Override
//                  public void process(HttpRequest request, HttpContext context)
//                      throws HttpException, IOException {
//                    Header header = request.getFirstHeader("foo");
//                    assertNotNull("Should have found header", header);
//                    assertEquals("bar", header.getValue());
//                    interceptorCalled.set(true);
//                    throw new IOException("cancelling request");
//                  }
//                })
//            .build();
//
//    ApacheHttpTransport transport = new ApacheHttpTransport(client);
//    ApacheHttpRequest request = transport.buildRequest("GET", "https://google.com");
//    request.addHeader("foo", "bar");
//    try {
//      LowLevelHttpResponse response = request.execute();
//      fail("should not actually make the request");
//    } catch (IOException exception) {
//      assertEquals("cancelling request", exception.getMessage());
//    }
//    assertTrue("Expected to have called our test interceptor", interceptorCalled.get());
//  }

//  @Test(timeout = 10_000L)
//  public void testConnectTimeout() {
//    // Apache HttpClient doesn't appear to behave correctly on windows
//    assumeFalse(isWindows());
//    // TODO(chanseok): Java 17 returns an IOException (SocketException: Network is unreachable).
//    // Figure out a way to verify connection timeout works on Java 17+.
//    assumeTrue(System.getProperty("java.version").compareTo("17") < 0);
//
//    HttpTransport httpTransport = new ApacheHttpTransport();
//    GenericUrl url = new GenericUrl("http://google.com:81");
//    try {
//      httpTransport.createRequestFactory().buildGetRequest(url).setConnectTimeout(100).execute();
//      fail("should have thrown an exception");
//    } catch (HttpHostConnectException | ConnectTimeoutException expected) {
//      // expected
//    } catch (IOException e) {
//      fail("unexpected IOException: " + e.getClass().getName() + ": " + e.getMessage());
//    }
//  }
//
//  private static class FakeServer implements AutoCloseable {
//    private final HttpServer server;
//    private final ExecutorService executorService;
//
//    FakeServer(HttpHandler httpHandler) throws IOException {
//      server = HttpServer.create(new InetSocketAddress(0), 0);
//      executorService = Executors.newFixedThreadPool(1);
//      server.setExecutor(executorService);
//      server.createContext("/", httpHandler);
//      server.start();
//    }
//
//    public int getPort() {
//      return server.getAddress().getPort();
//    }
//
//    @Override
//    public void close() {
//      server.stop(0);
//      executorService.shutdownNow();
//    }
//  }
//
//  @Test
//  public void testNormalizedUrl() throws IOException {
//    final HttpHandler handler =
//        new HttpHandler() {
//          @Override
//          public void handle(HttpExchange httpExchange) throws IOException {
//            byte[] response = httpExchange.getRequestURI().toString().getBytes();
//            httpExchange.sendResponseHeaders(200, response.length);
//            try (OutputStream out = httpExchange.getResponseBody()) {
//              out.write(response);
//            }
//          }
//        };
//    try (FakeServer server = new FakeServer(handler)) {
//      HttpTransport transport = new ApacheHttpTransport();
//      GenericUrl testUrl = new GenericUrl("http://localhost/foo//bar");
//      testUrl.setPort(server.getPort());
//      com.google.api.client.http.HttpResponse response =
//          transport.createRequestFactory().buildGetRequest(testUrl).execute();
//      assertEquals(200, response.getStatusCode());
//      assertEquals("/foo//bar", response.parseAsString());
//    }
//  }
//
//  @Test
//  public void testReadErrorStream() throws IOException {
//    final HttpHandler handler =
//        new HttpHandler() {
//          @Override
//          public void handle(HttpExchange httpExchange) throws IOException {
//            byte[] response = "Forbidden".getBytes(StandardCharsets.UTF_8);
//            httpExchange.sendResponseHeaders(403, response.length);
//            try (OutputStream out = httpExchange.getResponseBody()) {
//              out.write(response);
//            }
//          }
//        };
//    try (FakeServer server = new FakeServer(handler)) {
//      HttpTransport transport = new ApacheHttpTransport();
//      GenericUrl testUrl = new GenericUrl("http://localhost/foo//bar");
//      testUrl.setPort(server.getPort());
//      com.google.api.client.http.HttpRequest getRequest =
//          transport.createRequestFactory().buildGetRequest(testUrl);
//      getRequest.setThrowExceptionOnExecuteError(false);
//      com.google.api.client.http.HttpResponse response = getRequest.execute();
//      assertEquals(403, response.getStatusCode());
//      assertEquals("Forbidden", response.parseAsString());
//    }
//  }
//
//  @Test
//  public void testReadErrorStream_withException() throws IOException {
//    final HttpHandler handler =
//        new HttpHandler() {
//          @Override
//          public void handle(HttpExchange httpExchange) throws IOException {
//            byte[] response = "Forbidden".getBytes(StandardCharsets.UTF_8);
//            httpExchange.sendResponseHeaders(403, response.length);
//            try (OutputStream out = httpExchange.getResponseBody()) {
//              out.write(response);
//            }
//          }
//        };
//    try (FakeServer server = new FakeServer(handler)) {
//      HttpTransport transport = new ApacheHttpTransport();
//      GenericUrl testUrl = new GenericUrl("http://localhost/foo//bar");
//      testUrl.setPort(server.getPort());
//      com.google.api.client.http.HttpRequest getRequest =
//          transport.createRequestFactory().buildGetRequest(testUrl);
//      try {
//        getRequest.execute();
//        Assert.fail();
//      } catch (HttpResponseException ex) {
//        assertEquals("Forbidden", ex.getContent());
//      }
//    }
//  }
//
//  private boolean isWindows() {
//    return System.getProperty("os.name").startsWith("Windows");
//  }
}
