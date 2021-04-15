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
import static org.junit.Assert.fail;
import static org.mockito.Mockito.when;

import com.google.api.client.googleapis.batch.BatchRequest;
import com.google.api.client.googleapis.batch.json.JsonBatchCallback;
import com.google.api.client.googleapis.json.GoogleJsonError;
import com.google.api.client.http.HttpHeaders;
import com.google.api.client.http.HttpResponseException;
import com.google.api.client.http.LowLevelHttpResponse;
import com.google.api.client.testing.http.MockHttpTransport;
import com.google.api.client.testing.http.MockLowLevelHttpRequest;
import com.google.api.services.storage.Storage;
import com.google.api.services.storage.model.StorageObject;
import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import org.junit.Test;
import org.mockito.Mockito;

/**
 * Test to verify https://github.com/apache/beam/pull/14527#discussion_r613980011.
 *
 * I wanted to put this in google-http-client module, but google-http-client-json dependency
 * would create a dependency cycle. Therefore I place this in this class.
 */
public class BatchTest {

  private static InputStream toStream(String content) throws IOException {
    return new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
  }

  @Test
  public void testErrorContentRead() throws IOException {
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
    final LowLevelHttpResponse mockResponse = Mockito.mock(LowLevelHttpResponse.class);
    when(mockResponse.getContentType()).thenReturn("multipart/mixed; boundary=" + contentBoundary);

    // 429: Too many requests, then 200: OK.
    when(mockResponse.getStatusCode()).thenReturn(429, 200);
    when(mockResponse.getContent()).thenReturn(toStream("rateLimitExceeded"), toStream(content));

    MockHttpTransport mockTransport =
        new MockHttpTransport.Builder()
            .setLowLevelHttpRequest(
                new MockLowLevelHttpRequest() {
                  @Override
                  public LowLevelHttpResponse execute() throws IOException {
                    return mockResponse;
                  }
                })
            .build();

    RetryHttpRequestInitializer httpRequestInitializer = new RetryHttpRequestInitializer();
    Storage storageClient = new Storage(mockTransport, JacksonFactory.getDefaultInstance(),
        httpRequestInitializer);

    BatchRequest batch = storageClient.batch(httpRequestInitializer);

    Storage.Objects.Get getRequest =
        storageClient.objects().get("testbucket", "testobject");

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
          }
        });

    try {
      batch.execute();
      fail("batch.execute should throw an exception");
    } catch (HttpResponseException ex) {
      assertEquals("rateLimitExceeded", ex.getContent());
    }
  }
}
