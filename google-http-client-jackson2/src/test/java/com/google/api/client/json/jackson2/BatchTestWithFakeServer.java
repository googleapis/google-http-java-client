/*
 *
 *  * Copyright 2021 Google LLC.
 *  *
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  * you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  *     http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *
 *
 */

package com.google.api.client.json.jackson2;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import com.google.api.client.googleapis.batch.BatchRequest;
import com.google.api.client.googleapis.batch.json.JsonBatchCallback;
import com.google.api.client.googleapis.json.GoogleJsonError;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpHeaders;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.HttpUnsuccessfulResponseHandler;
import com.google.api.client.http.apache.v2.ApacheHttpTransport;
import com.google.api.services.storage.Storage;
import com.google.api.services.storage.model.StorageObject;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.junit.Test;

/**
 * Test to verify https://github.com/apache/beam/pull/14527#discussion_r613980011.
 *
 * <p>I wanted to put this in google-http-client module, but google-http-client-json dependency
 * would create a dependency cycle. Therefore I place this in this class.
 */
public class BatchTestWithFakeServer {

  @Test
  public void testErrorContentReadRetry_withFakeServer() throws IOException {
    final int statusCode429_TooManyRequest = 429;

    final HttpHandler handler =
        new HttpHandler() {
          int count = 0;

          @Override
          public void handle(HttpExchange httpExchange) throws IOException {
            Headers responseHeaders = httpExchange.getResponseHeaders();
            if (count == 0) {
              // 1st request
              byte[] response = "rateLimitExceeded".getBytes(StandardCharsets.UTF_8);
              responseHeaders.set("Content-Type", "text/plain;  charset=UTF-8");
              httpExchange.sendResponseHeaders(statusCode429_TooManyRequest, response.length);
              try (OutputStream out = httpExchange.getResponseBody()) {
                out.write(response);
              }
              count++;
            } else {
              // 2nd request
              String contentBoundary = "batch_foobarbaz";
              String contentBoundaryLine = "--" + contentBoundary;
              String endOfContentBoundaryLine = "--" + contentBoundary + "--";
              String content =
                  contentBoundaryLine
                      + "\n"
                      + "Content-Type: application/http\n"
                      + "\n"
                      + "HTTP/1.1 404 Not Found\n"
                      + "Content-Length: -1\n"
                      + "\n"
                      + "{\"error\":{\"code\":404}}"
                      + "\n"
                      + "\n"
                      + endOfContentBoundaryLine
                      + "\n";
              byte[] response = content.getBytes(StandardCharsets.UTF_8);
              responseHeaders.set("Content-Type", "multipart/mixed; boundary=" + contentBoundary);
              httpExchange.sendResponseHeaders(200, response.length);
              try (OutputStream out = httpExchange.getResponseBody()) {
                out.write(response);
              }
            }
          }
        };
    try (FakeServer server = new FakeServer(handler)) {
      HttpTransport transport = new ApacheHttpTransport();
      GenericUrl testUrl = new GenericUrl("http://localhost/foo//bar");
      testUrl.setPort(server.getPort());

      HttpRequestInitializer httpRequestInitializer =
          new HttpRequestInitializer() {
            @Override
            public void initialize(HttpRequest request) throws IOException {
              request.setUnsuccessfulResponseHandler(
                  new HttpUnsuccessfulResponseHandler() {
                    @Override
                    public boolean handleResponse(
                        HttpRequest request, HttpResponse response, boolean supportsRetry)
                        throws IOException {
                      // true to retry
                      boolean willRetry = response.getStatusCode() == statusCode429_TooManyRequest;
                      return willRetry;
                    }
                  });
            }
          };

      Storage storageClient =
          new Storage(transport, JacksonFactory.getDefaultInstance(), httpRequestInitializer);

      BatchRequest batch = storageClient.batch(httpRequestInitializer);
      batch.setBatchUrl(testUrl);

      Storage.Objects.Get getRequest = storageClient.objects().get("testbucket", "testobject");

      final GoogleJsonError[] capturedGoogleJsonError = new GoogleJsonError[1];
      getRequest.queue(
          batch,
          new JsonBatchCallback<StorageObject>() {
            @Override
            public void onSuccess(StorageObject response, HttpHeaders httpHeaders)
                throws IOException {
              System.out.println("Got response: " + response);
            }

            @Override
            public void onFailure(GoogleJsonError e, HttpHeaders httpHeaders) throws IOException {
              System.out.println("Got error: " + e);
              capturedGoogleJsonError[0] = e;
            }
          });

      batch.execute();
      assertNotNull(capturedGoogleJsonError[0]);

      // From {"error":{"code":404}}
      assertEquals(404, capturedGoogleJsonError[0].getCode());
    }
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
}
