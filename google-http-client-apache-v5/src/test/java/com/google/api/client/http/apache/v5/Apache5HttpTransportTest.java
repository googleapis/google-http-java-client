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

package com.google.api.client.http.apache.v5;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.junit.Assume.assumeTrue;

import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpResponseException;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.LowLevelHttpResponse;
import com.google.api.client.util.ByteArrayStreamingContent;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import org.apache.hc.client5.http.ConnectTimeoutException;
import org.apache.hc.client5.http.HttpHostConnectException;
import org.apache.hc.client5.http.classic.HttpClient;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;
import org.apache.hc.core5.http.ClassicHttpRequest;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.ConnectionRequestTimeoutException;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.EntityDetails;
import org.apache.hc.core5.http.Header;
import org.apache.hc.core5.http.HttpException;
import org.apache.hc.core5.http.HttpHeaders;
import org.apache.hc.core5.http.HttpHost;
import org.apache.hc.core5.http.HttpRequest;
import org.apache.hc.core5.http.HttpRequestInterceptor;
import org.apache.hc.core5.http.HttpRequestMapper;
import org.apache.hc.core5.http.HttpResponse;
import org.apache.hc.core5.http.HttpStatus;
import org.apache.hc.core5.http.impl.bootstrap.HttpServer;
import org.apache.hc.core5.http.impl.io.HttpRequestExecutor;
import org.apache.hc.core5.http.impl.io.HttpService;
import org.apache.hc.core5.http.io.HttpClientConnection;
import org.apache.hc.core5.http.io.HttpRequestHandler;
import org.apache.hc.core5.http.io.entity.ByteArrayEntity;
import org.apache.hc.core5.http.io.support.BasicHttpServerRequestHandler;
import org.apache.hc.core5.http.protocol.HttpContext;
import org.apache.hc.core5.http.protocol.HttpProcessor;
import org.junit.Assert;
import org.junit.Test;

/** Tests {@link Apache5HttpTransport}. */
public class Apache5HttpTransportTest {

  @Test
  public void testApacheHttpTransport() {
    Apache5HttpTransport transport = new Apache5HttpTransport();
    checkHttpTransport(transport);
    assertFalse(transport.isMtls());
  }

  @Test
  public void testApacheHttpTransportWithParam() {
    Apache5HttpTransport transport = new Apache5HttpTransport(HttpClients.custom().build(), true);
    checkHttpTransport(transport);
    assertTrue(transport.isMtls());
  }

  @Test
  public void testNewDefaultHttpClient() {
    HttpClient client = Apache5HttpTransport.newDefaultHttpClient();
    checkHttpClient(client);
  }

  private void checkHttpTransport(Apache5HttpTransport transport) {
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
  public void testRequestsWithContent() throws IOException {
    // This test confirms that we can set the content on any type of request
    HttpClient mockClient =
        new MockHttpClient() {
          @Override
          public ClassicHttpResponse executeOpen(
              HttpHost target, ClassicHttpRequest request, HttpContext context) {
            return new MockClassicHttpResponse();
          }
        };
    Apache5HttpTransport transport = new Apache5HttpTransport(mockClient);

    // Test GET.
    execute(transport.buildRequest("GET", "http://www.test.url"));
    // Test DELETE.
    execute(transport.buildRequest("DELETE", "http://www.test.url"));
    // Test HEAD.
    execute(transport.buildRequest("HEAD", "http://www.test.url"));

    // Test PATCH.
    execute(transport.buildRequest("PATCH", "http://www.test.url"));
    // Test PUT.
    execute(transport.buildRequest("PUT", "http://www.test.url"));
    // Test POST.
    execute(transport.buildRequest("POST", "http://www.test.url"));
    // Test PATCH.
    execute(transport.buildRequest("PATCH", "http://www.test.url"));
  }

  private void execute(Apache5HttpRequest request) throws IOException {
    byte[] bytes = "abc".getBytes(StandardCharsets.UTF_8);
    request.setStreamingContent(new ByteArrayStreamingContent(bytes));
    request.setContentType("text/html");
    request.setContentLength(bytes.length);
    request.execute();
  }

  @Test
  public void testRequestShouldNotFollowRedirects() throws IOException {
    final AtomicInteger requestsAttempted = new AtomicInteger(0);
    HttpRequestExecutor requestExecutor =
        new HttpRequestExecutor() {
          @Override
          public ClassicHttpResponse execute(
              ClassicHttpRequest request, HttpClientConnection connection, HttpContext context)
              throws IOException, HttpException {
            ClassicHttpResponse response = new MockClassicHttpResponse();
            response.setCode(302);
            response.setReasonPhrase(null);
            response.addHeader("location", "https://google.com/path");
            response.addHeader(HttpHeaders.SET_COOKIE, "");
            requestsAttempted.incrementAndGet();
            return response;
          }
        };
    HttpClient client = HttpClients.custom().setRequestExecutor(requestExecutor).build();
    Apache5HttpTransport transport = new Apache5HttpTransport(client);
    Apache5HttpRequest request = transport.buildRequest("GET", "https://google.com");
    LowLevelHttpResponse response = request.execute();
    assertEquals(1, requestsAttempted.get());
    assertEquals(302, response.getStatusCode());
  }

  @Test
  public void testRequestCanSetHeaders() {
    final AtomicBoolean interceptorCalled = new AtomicBoolean(false);
    HttpClient client =
        HttpClients.custom()
            .addRequestInterceptorFirst(
                new HttpRequestInterceptor() {
                  @Override
                  public void process(
                      HttpRequest request, EntityDetails details, HttpContext context)
                      throws HttpException, IOException {
                    Header header = request.getFirstHeader("foo");
                    assertNotNull("Should have found header", header);
                    assertEquals("bar", header.getValue());
                    interceptorCalled.set(true);
                    throw new IOException("cancelling request");
                  }
                })
            .build();

    Apache5HttpTransport transport = new Apache5HttpTransport(client);
    Apache5HttpRequest request = transport.buildRequest("GET", "https://google.com");
    request.addHeader("foo", "bar");
    try {
      LowLevelHttpResponse response = request.execute();
      fail("should not actually make the request");
    } catch (IOException exception) {
      assertEquals("cancelling request", exception.getMessage());
    }
    assertTrue("Expected to have called our test interceptor", interceptorCalled.get());
  }

  @Test(timeout = 10_000L)
  public void testConnectTimeout() {
    // TODO(chanseok): Java 17 returns an IOException (SocketException: Network is unreachable).
    // Figure out a way to verify connection timeout works on Java 17+.
    assumeTrue(System.getProperty("java.version").compareTo("17") < 0);

    HttpTransport httpTransport = new Apache5HttpTransport();
    GenericUrl url = new GenericUrl("http://google.com:81");
    try {
      httpTransport.createRequestFactory().buildGetRequest(url).setConnectTimeout(100).execute();
      fail("should have thrown an exception");
    } catch (HttpHostConnectException | ConnectTimeoutException expected) {
      // expected
    } catch (IOException e) {
      fail("unexpected IOException: " + e.getClass().getName() + ": " + e.getMessage());
    }
  }

  @Test(timeout = 5000)
  public void testConnectionRequestTimeout() throws Exception {
    final CountDownLatch latch = new CountDownLatch(1);
    final HttpRequestHandler handler =
        new HttpRequestHandler() {
          @Override
          public void handle(
              ClassicHttpRequest request, ClassicHttpResponse response, HttpContext context)
              throws HttpException, IOException {
            try {
              latch.await(); // Wait for the signal to proceed
            } catch (InterruptedException e) {
              Thread.currentThread().interrupt();
            }
            response.setCode(HttpStatus.SC_OK);
          }
        };

    try (FakeServer server = new FakeServer(handler)) {
      PoolingHttpClientConnectionManager connectionManager =
          new PoolingHttpClientConnectionManager();
      connectionManager.setMaxTotal(1);

      RequestConfig requestConfig =
          RequestConfig.custom().setConnectionRequestTimeout(1, TimeUnit.SECONDS).build();

      HttpClient httpClient =
          HttpClientBuilder.create()
              .setConnectionManager(connectionManager)
              .setDefaultRequestConfig(requestConfig)
              .build();

      HttpTransport transport = new Apache5HttpTransport(httpClient, requestConfig, false);
      final GenericUrl url = new GenericUrl("http://localhost:" + server.getPort());

      ExecutorService executor = Executors.newFixedThreadPool(2);

      // First request takes the only connection
      executor.submit(
          () -> {
            try {
              transport.createRequestFactory().buildGetRequest(url).execute();
            } catch (IOException e) {
              // This request might fail if the test finishes before it completes, which is fine.
            }
          });

      // Give the first request time to acquire the connection
      Thread.sleep(100);

      // Second request should time out waiting for a connection
      try {
        transport.createRequestFactory().buildGetRequest(url).execute();
        fail("Should have thrown ConnectTimeoutException");
      } catch (ConnectTimeoutException e) {
        // Expected
      } finally {
        latch.countDown(); // Allow the first request to complete
        executor.shutdownNow();
      }
    }
  }

  @Test(timeout = 5000)
  public void testConnectionRequestTimeoutFromDefaultRequestConfig() throws Exception {
    final CountDownLatch latch = new CountDownLatch(1);
    final HttpRequestHandler handler =
        new HttpRequestHandler() {
          @Override
          public void handle(
              ClassicHttpRequest request, ClassicHttpResponse response, HttpContext context)
              throws HttpException, IOException {
            try {
              latch.await(); // Wait for the signal to proceed
            } catch (InterruptedException e) {
              Thread.currentThread().interrupt();
            }
            response.setCode(HttpStatus.SC_OK);
          }
        };

    try (FakeServer server = new FakeServer(handler)) {
      PoolingHttpClientConnectionManager connectionManager =
          new PoolingHttpClientConnectionManager();
      connectionManager.setMaxTotal(1); // Only one connection in the pool

      RequestConfig requestConfig =
          RequestConfig.custom().setConnectionRequestTimeout(1, TimeUnit.SECONDS).build();

      HttpClient httpClient =
          Apache5HttpTransport.newDefaultHttpClientBuilder()
              .setConnectionManager(connectionManager)
              .setDefaultRequestConfig(requestConfig)
              .build();

      HttpTransport transport = new Apache5HttpTransport(httpClient, requestConfig, false);
      final GenericUrl url = new GenericUrl("http://localhost:" + server.getPort());

      ExecutorService executor = Executors.newFixedThreadPool(2);

      // First request takes the only connection
      executor.submit(
          () -> {
            try {
              transport.createRequestFactory().buildGetRequest(url).execute();
            } catch (IOException e) {
              // This request might fail if the test finishes before it completes, which is fine.
            }
          });

      // Give the first request time to acquire the connection
      Thread.sleep(100);

      // Second request should time out waiting for a connection
      try {
        transport.createRequestFactory().buildGetRequest(url).execute();
        fail("Should have thrown ConnectTimeoutException");
      } catch (ConnectionRequestTimeoutException e) {
        // Expected
      } finally {
        latch.countDown(); // Allow the first request to complete
        executor.shutdownNow();
      }
    }
  }

  private static class FakeServer implements AutoCloseable {
    private final HttpServer server;

    FakeServer(final HttpRequestHandler httpHandler) throws IOException {
      HttpRequestMapper<HttpRequestHandler> mapper =
          new HttpRequestMapper<HttpRequestHandler>() {
            @Override
            public HttpRequestHandler resolve(HttpRequest request, HttpContext context)
                throws HttpException {
              return httpHandler;
            };
          };
      server =
          new HttpServer(
              0,
              HttpService.builder()
                  .withHttpProcessor(
                      new HttpProcessor() {
                        @Override
                        public void process(
                            HttpRequest request, EntityDetails entity, HttpContext context)
                            throws HttpException, IOException {}

                        @Override
                        public void process(
                            HttpResponse response, EntityDetails entity, HttpContext context)
                            throws HttpException, IOException {}
                      })
                  .withHttpServerRequestHandler(new BasicHttpServerRequestHandler(mapper))
                  .build(),
              null,
              null,
              null,
              null,
              null,
              null);
      //       server.createContext("/", httpHandler);
      server.start();
    }

    public int getPort() {
      return server.getLocalPort();
    }

    @Override
    public void close() {
      server.initiateShutdown();
    }
  }

  @Test
  public void testNormalizedUrl() throws IOException {
    final HttpRequestHandler handler =
        new HttpRequestHandler() {
          @Override
          public void handle(
              ClassicHttpRequest request, ClassicHttpResponse response, HttpContext context)
              throws HttpException, IOException {
            // Extract the request URI and convert to bytes
            byte[] responseData = request.getRequestUri().getBytes(StandardCharsets.UTF_8);

            // Set the response headers (status code and content length)
            response.setCode(HttpStatus.SC_OK);
            response.setHeader(HttpHeaders.CONTENT_LENGTH, String.valueOf(responseData.length));

            // Set the response entity (body)
            ByteArrayEntity entity = new ByteArrayEntity(responseData, ContentType.TEXT_PLAIN);
            response.setEntity(entity);
          }
        };
    try (FakeServer server = new FakeServer(handler)) {
      HttpTransport transport = new Apache5HttpTransport();
      GenericUrl testUrl = new GenericUrl("http://localhost/foo//bar");
      testUrl.setPort(server.getPort());
      com.google.api.client.http.HttpResponse response =
          transport.createRequestFactory().buildGetRequest(testUrl).execute();
      assertEquals(200, response.getStatusCode());
      assertEquals("/foo//bar", response.parseAsString());
    }
  }

  @Test
  public void testReadErrorStream() throws IOException {
    final HttpRequestHandler handler =
        new HttpRequestHandler() {
          @Override
          public void handle(
              ClassicHttpRequest request, ClassicHttpResponse response, HttpContext context)
              throws HttpException, IOException {
            byte[] responseData = "Forbidden".getBytes(StandardCharsets.UTF_8);
            response.setCode(HttpStatus.SC_FORBIDDEN); // 403 Forbidden
            response.setHeader(HttpHeaders.CONTENT_LENGTH, String.valueOf(responseData.length));
            ByteArrayEntity entity = new ByteArrayEntity(responseData, ContentType.TEXT_PLAIN);
            response.setEntity(entity);
          }
        };
    try (FakeServer server = new FakeServer(handler)) {
      HttpTransport transport = new Apache5HttpTransport();
      GenericUrl testUrl = new GenericUrl("http://localhost/foo//bar");
      testUrl.setPort(server.getPort());
      com.google.api.client.http.HttpRequest getRequest =
          transport.createRequestFactory().buildGetRequest(testUrl);
      getRequest.setThrowExceptionOnExecuteError(false);
      com.google.api.client.http.HttpResponse response = getRequest.execute();
      assertEquals(403, response.getStatusCode());
      assertEquals("Forbidden", response.parseAsString());
    }
  }

  @Test
  public void testReadErrorStream_withException() throws IOException {
    final HttpRequestHandler handler =
        new HttpRequestHandler() {
          @Override
          public void handle(
              ClassicHttpRequest request, ClassicHttpResponse response, HttpContext context)
              throws HttpException, IOException {
            byte[] responseData = "Forbidden".getBytes(StandardCharsets.UTF_8);
            response.setCode(HttpStatus.SC_FORBIDDEN); // 403 Forbidden
            response.setHeader(HttpHeaders.CONTENT_LENGTH, String.valueOf(responseData.length));
            ByteArrayEntity entity = new ByteArrayEntity(responseData, ContentType.TEXT_PLAIN);
            response.setEntity(entity);
          }
        };
    try (FakeServer server = new FakeServer(handler)) {
      HttpTransport transport = new Apache5HttpTransport();
      GenericUrl testUrl = new GenericUrl("http://localhost/foo//bar");
      testUrl.setPort(server.getPort());
      com.google.api.client.http.HttpRequest getRequest =
          transport.createRequestFactory().buildGetRequest(testUrl);
      try {
        getRequest.execute();
        Assert.fail();
      } catch (HttpResponseException ex) {
        assertEquals("Forbidden", ex.getContent());
      }
    }
  }

  private boolean isWindows() {
    return System.getProperty("os.name").startsWith("Windows");
  }
}
