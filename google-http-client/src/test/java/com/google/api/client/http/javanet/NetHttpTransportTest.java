/*
 * Copyright 2012 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.api.client.http.javanet;

import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.testing.http.HttpTesting;
import com.google.api.client.testing.http.javanet.MockHttpURLConnection;
import com.google.api.client.util.ByteArrayStreamingContent;
import com.google.api.client.util.StringUtils;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.URL;
import java.security.KeyStore;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import junit.framework.TestCase;
import org.junit.Test;

/**
 * Tests {@link NetHttpTransport}.
 *
 * @author Yaniv Inbar
 */
public class NetHttpTransportTest extends TestCase {

  private static final String[] METHODS = {
    "GET", "POST", "HEAD", "OPTIONS", "PUT", "DELETE", "TRACE"
  };

  public void testNotMtlsWithoutClientCert() throws Exception {
    KeyStore trustStore = KeyStore.getInstance("JKS");

    NetHttpTransport transport =
        new NetHttpTransport.Builder().trustCertificates(trustStore).build();
    assertFalse(transport.isMtls());
  }

  public void testIsMtlsWithClientCert() throws Exception {
    KeyStore trustStore = KeyStore.getInstance("JKS");
    KeyStore keyStore = KeyStore.getInstance("PKCS12");

    // Load client certificate and private key from secret.p12 file.
    keyStore.load(
        this.getClass()
            .getClassLoader()
            .getResourceAsStream("com/google/api/client/util/secret.p12"),
        "notasecret".toCharArray());

    NetHttpTransport transport =
        new NetHttpTransport.Builder()
            .trustCertificates(trustStore, keyStore, "notasecret")
            .build();
    assertTrue(transport.isMtls());
  }

  public void testExecute_mock() throws Exception {
    for (String method : METHODS) {
      boolean isPutOrPost = method.equals("PUT") || method.equals("POST");
      MockHttpURLConnection connection = new MockHttpURLConnection(new URL(HttpTesting.SIMPLE_URL));
      connection.setRequestMethod(method);
      NetHttpRequest request = new NetHttpRequest(connection);
      setContent(request, null, "");
      request.execute();
      assertEquals(isPutOrPost, connection.doOutputCalled());
      setContent(request, null, " ");
      if (isPutOrPost) {
        request.execute();
      } else {
        try {
          request.execute();
          fail("expected " + IllegalArgumentException.class);
        } catch (IllegalArgumentException e) {
          // expected
        }
      }
      assertEquals(isPutOrPost, connection.doOutputCalled());
    }
  }

  public void testExecute_methodUnchanged() throws Exception {
    String body = "Arbitrary body";
    byte[] buf = StringUtils.getBytesUtf8(body);
    for (String method : METHODS) {
      HttpURLConnection connection =
          new MockHttpURLConnection(new URL(HttpTesting.SIMPLE_URL))
              .setResponseCode(200)
              .setInputStream(new ByteArrayInputStream(buf));
      connection.setRequestMethod(method);
      NetHttpRequest request = new NetHttpRequest(connection);
      setContent(request, "text/html", "");
      request.execute().getContent().close();
      assertEquals(method, connection.getRequestMethod());
    }
  }

  public void testAbruptTerminationIsNoticedWithContentLength() throws Exception {
    String incompleteBody = "" + "Fixed size body test.\r\n" + "Incomplete response.";
    byte[] buf = StringUtils.getBytesUtf8(incompleteBody);
    MockHttpURLConnection connection =
        new MockHttpURLConnection(new URL(HttpTesting.SIMPLE_URL))
            .setResponseCode(200)
            .addHeader("Content-Length", "205")
            .setInputStream(new ByteArrayInputStream(buf));
    connection.setRequestMethod("GET");
    NetHttpRequest request = new NetHttpRequest(connection);
    setContent(request, null, "");
    NetHttpResponse response = (NetHttpResponse) (request.execute());

    InputStream in = response.getContent();
    boolean thrown = false;
    try {
      while (in.read() != -1) {
        // This space is intentionally left blank.
      }
    } catch (IOException ioe) {
      thrown = true;
    }
    assertTrue(thrown);
  }

  public void testAbruptTerminationIsNoticedWithContentLengthWithReadToBuf() throws Exception {
    String incompleteBody = "" + "Fixed size body test.\r\n" + "Incomplete response.";
    byte[] buf = StringUtils.getBytesUtf8(incompleteBody);
    MockHttpURLConnection connection =
        new MockHttpURLConnection(new URL(HttpTesting.SIMPLE_URL))
            .setResponseCode(200)
            .addHeader("Content-Length", "205")
            .setInputStream(new ByteArrayInputStream(buf));
    connection.setRequestMethod("GET");
    NetHttpRequest request = new NetHttpRequest(connection);
    setContent(request, null, "");
    NetHttpResponse response = (NetHttpResponse) (request.execute());

    InputStream in = response.getContent();
    boolean thrown = false;
    try {
      while (in.read(new byte[100]) != -1) {
        // This space is intentionally left blank.
      }
    } catch (IOException ioe) {
      thrown = true;
    }
    assertTrue(thrown);
  }

  private void setContent(NetHttpRequest request, String type, String value) throws Exception {
    byte[] bytes = StringUtils.getBytesUtf8(value);
    request.setStreamingContent(new ByteArrayStreamingContent(bytes));
    request.setContentType(type);
    request.setContentLength(bytes.length);
  }

  static class FakeServer implements AutoCloseable {
    private final HttpServer server;
    private final ExecutorService executorService;

    public FakeServer(HttpHandler httpHandler) throws IOException {
      this.server = HttpServer.create(new InetSocketAddress(0), 0);
      this.executorService = Executors.newFixedThreadPool(1);
      server.setExecutor(this.executorService);
      server.createContext("/", httpHandler);
      server.start();
    }

    public int getPort() {
      return server.getAddress().getPort();
    }

    @Override
    public void close() {
      this.server.stop(0);
      this.executorService.shutdownNow();
    }
  }

  @Test(timeout = 10_000L)
  public void testDisconnectShouldNotWaitToReadResponse() throws IOException {
    // This handler waits for 100s before returning writing content. The test should
    // timeout if disconnect waits for the response before closing the connection.
    final HttpHandler handler =
        new HttpHandler() {
          @Override
          public void handle(HttpExchange httpExchange) throws IOException {
            byte[] response = httpExchange.getRequestURI().toString().getBytes();
            httpExchange.sendResponseHeaders(200, response.length);

            // Sleep for longer than the test timeout
            try {
              Thread.sleep(100_000);
            } catch (InterruptedException e) {
              throw new IOException("interrupted", e);
            }
            try (OutputStream out = httpExchange.getResponseBody()) {
              out.write(response);
            }
          }
        };

    try (FakeServer server = new FakeServer(handler)) {
      HttpTransport transport = new NetHttpTransport();
      GenericUrl testUrl = new GenericUrl("http://localhost/foo//bar");
      testUrl.setPort(server.getPort());
      com.google.api.client.http.HttpResponse response =
          transport.createRequestFactory().buildGetRequest(testUrl).execute();
      // disconnect should not wait to read the entire content
      response.disconnect();
    }
  }
}
