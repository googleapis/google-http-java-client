/*
 * Copyright (c) 2012 Google Inc.
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

package com.google.api.client.http.javanet;

import com.google.api.client.testing.http.javanet.MockHttpURLConnection;
import com.google.api.client.util.StringUtils;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import junit.framework.TestCase;

/**
 * Tests {@link NetHttpResponse}.
 *
 * @author Yaniv Inbar
 */
public class NetHttpResponseTest extends TestCase {

  private static final String VALID_RESPONSE = "This is a valid response.";
  private static final String ERROR_RESPONSE = "This is an error response.";

  public void testGetStatusCode() throws IOException {
    subtestGetStatusCode(0, -1);
    subtestGetStatusCode(200, 200);
    subtestGetStatusCode(404, 404);
  }

  public void subtestGetStatusCode(int expectedCode, int responseCode) throws IOException {
    assertEquals(
        expectedCode,
        new NetHttpResponse(new MockHttpURLConnection(null).setResponseCode(responseCode))
            .getStatusCode());
  }

  public void testGetContent() throws IOException {
    subtestGetContent(0);
    subtestGetContent(200);
    subtestGetContent(304);
    subtestGetContent(307);
    subtestGetContent(404);
    subtestGetContent(503);

    subtestGetContentWithShortRead(0);
    subtestGetContentWithShortRead(200);
    subtestGetContentWithShortRead(304);
    subtestGetContentWithShortRead(307);
    subtestGetContentWithShortRead(404);
    subtestGetContentWithShortRead(503);
  }

  public void subtestGetContent(int responseCode) throws IOException {
    NetHttpResponse response =
        new NetHttpResponse(
            new MockHttpURLConnection(null)
                .setResponseCode(responseCode)
                .setInputStream(new ByteArrayInputStream(StringUtils.getBytesUtf8(VALID_RESPONSE)))
                .setErrorStream(
                    new ByteArrayInputStream(StringUtils.getBytesUtf8(ERROR_RESPONSE))));
    InputStream is = response.getContent();
    byte[] buf = new byte[100];
    int bytes = 0, n = 0;
    while ((n = is.read(buf)) != -1) {
      bytes += n;
    }
    if (responseCode < 400) {
      assertEquals(VALID_RESPONSE, new String(buf, 0, bytes, Charset.forName("UTF-8")));
    } else {
      assertEquals(ERROR_RESPONSE, new String(buf, 0, bytes, Charset.forName("UTF-8")));
    }
  }

  public void subtestGetContentWithShortRead(int responseCode) throws IOException {
    NetHttpResponse response =
        new NetHttpResponse(
            new MockHttpURLConnection(null)
                .setResponseCode(responseCode)
                .setInputStream(new ByteArrayInputStream(StringUtils.getBytesUtf8(VALID_RESPONSE)))
                .setErrorStream(
                    new ByteArrayInputStream(StringUtils.getBytesUtf8(ERROR_RESPONSE))));
    InputStream is = response.getContent();
    byte[] buf = new byte[100];
    int bytes = 0, b = 0;
    while ((b = is.read()) != -1) {
      buf[bytes++] = (byte) b;
    }
    if (responseCode < 400) {
      assertEquals(VALID_RESPONSE, new String(buf, 0, bytes, Charset.forName("UTF-8")));
    } else {
      assertEquals(ERROR_RESPONSE, new String(buf, 0, bytes, Charset.forName("UTF-8")));
    }
  }

  public void testSkippingBytes() throws IOException {
    MockHttpURLConnection connection =
        new MockHttpURLConnection(null)
            .setResponseCode(200)
            .setInputStream(new ByteArrayInputStream(StringUtils.getBytesUtf8("0123456789")))
            .addHeader("Content-Length", "10");
    NetHttpResponse response = new NetHttpResponse(connection);
    InputStream is = response.getContent();
    // read 1 byte, then skip 9 (to EOF)
    assertEquals('0', is.read());
    assertEquals(9, is.skip(9));
    // expect EOF, not an exception
    assertEquals(-1, is.read());
  }
}
