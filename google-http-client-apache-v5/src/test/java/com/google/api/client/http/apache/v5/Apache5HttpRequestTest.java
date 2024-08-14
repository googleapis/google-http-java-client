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
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.classic.methods.HttpUriRequestBase;
import org.apache.hc.core5.http.ClassicHttpRequest;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.HttpHost;
import org.apache.hc.core5.http.protocol.HttpContext;
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
}
