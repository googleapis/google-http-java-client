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
import static org.junit.Assert.assertTrue;

import com.google.api.client.http.ByteArrayContent;
import com.google.api.client.http.HttpContent;
import com.google.api.client.http.InputStreamContent;
import com.google.api.client.http.LowLevelHttpResponse;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.classic.methods.HttpUriRequestBase;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.core5.http.ClassicHttpRequest;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.HttpHost;
import org.apache.hc.core5.http.io.entity.BasicHttpEntity;
import org.apache.hc.core5.http.protocol.HttpContext;
import org.apache.hc.core5.util.Timeout;
import org.junit.Test;

public class Apache5HttpRequestTest {
  @Test
  public void testContentLengthSet() throws Exception {
    HttpUriRequestBase base = new HttpPost("http://www.google.com");
    Apache5HttpRequest request =
        new Apache5HttpRequest(
            new MockHttpClient() {
              @Override
              public ClassicHttpResponse executeOpen(
                  HttpHost target, ClassicHttpRequest request, HttpContext context) {
                return new MockClassicHttpResponse();
              }
            },
            base);
    HttpContent content =
        new ByteArrayContent("text/plain", "sample".getBytes(StandardCharsets.UTF_8));
    request.setStreamingContent(content);
    request.setContentLength(content.getLength());
    request.execute();

    assertFalse(base.getEntity().isChunked());
    assertEquals(6, base.getEntity().getContentLength());
  }

  @Test
  public void testChunked() throws Exception {
    byte[] buf = new byte[300];
    Arrays.fill(buf, (byte) ' ');
    HttpUriRequestBase base = new HttpPost("http://www.google.com");
    Apache5HttpRequest request =
        new Apache5HttpRequest(
            new MockHttpClient() {
              @Override
              public ClassicHttpResponse executeOpen(
                  HttpHost target, ClassicHttpRequest request, HttpContext context) {
                return new MockClassicHttpResponse();
              }
            },
            base);
    HttpContent content = new InputStreamContent("text/plain", new ByteArrayInputStream(buf));
    request.setStreamingContent(content);
    request.execute();

    assertTrue(base.getEntity().isChunked());
    assertEquals(-1, base.getEntity().getContentLength());
  }

  @Test
  public void testExecute_closeContent_closesResponse() throws Exception {
    HttpUriRequestBase base = new HttpPost("http://www.google.com");
    final InputStream responseContentStream = new ByteArrayInputStream(new byte[] {1, 2, 3});
    BasicHttpEntity testEntity =
        new BasicHttpEntity(responseContentStream, ContentType.DEFAULT_BINARY);
    AtomicInteger closedResponseCounter = new AtomicInteger(0);
    ClassicHttpResponse classicResponse =
        new MockClassicHttpResponse() {
          @Override
          public HttpEntity getEntity() {
            return testEntity;
          }

          @Override
          public void close() {
            closedResponseCounter.incrementAndGet();
          }
        };

    Apache5HttpRequest request =
        new Apache5HttpRequest(
            new MockHttpClient() {
              @Override
              public ClassicHttpResponse executeOpen(
                  HttpHost target, ClassicHttpRequest request, HttpContext context) {
                return classicResponse;
              }
            },
            base);
    LowLevelHttpResponse response = request.execute();
    assertTrue(response instanceof Apache5HttpResponse);

    // we confirm that the classic response we prepared in this test is the same as the content's
    // response
    assertTrue(response.getContent() instanceof Apache5ResponseContent);
    assertEquals(classicResponse, ((Apache5ResponseContent) response.getContent()).getResponse());

    // we close the response's content stream and confirm the response is also closed
    assertEquals(0, closedResponseCounter.get());
    response.getContent().close();
    assertEquals(1, closedResponseCounter.get());
  }

  @Test
  public void testSetTimeout() throws Exception {
    HttpUriRequestBase base = new HttpPost("http://www.google.com");
    Apache5HttpRequest request =
        new Apache5HttpRequest(
            new MockHttpClient() {
              @Override
              public ClassicHttpResponse executeOpen(
                  HttpHost target, ClassicHttpRequest request, HttpContext context) {
                return new MockClassicHttpResponse();
              }
            },
            base);
    request.setTimeout(100, 200);
    request.execute();

    RequestConfig config = base.getConfig();
    assertEquals(Timeout.of(100, TimeUnit.MILLISECONDS), config.getConnectTimeout());
    assertEquals(Timeout.of(200, TimeUnit.MILLISECONDS), config.getResponseTimeout());
    assertEquals(Timeout.of(100, TimeUnit.MILLISECONDS), config.getConnectionRequestTimeout());
  }
}
