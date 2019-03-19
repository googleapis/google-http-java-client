/*
 * Copyright (c) 2015 Google Inc.
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

package com.google.api.client.testing.http.javanet;

import com.google.api.client.testing.http.HttpTesting;
import com.google.api.client.util.StringUtils;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import junit.framework.TestCase;

/** Tests {@link MockHttpURLConnection}. */
public class MockHttpUrlConnectionTest extends TestCase {

  private static final String RESPONSE_BODY = "body";
  private static final String HEADER_NAME = "Custom-Header";

  public void testSetGetHeaders() throws IOException {
    MockHttpURLConnection connection = new MockHttpURLConnection(new URL(HttpTesting.SIMPLE_URL));
    connection.addHeader(HEADER_NAME, "100");
    assertEquals("100", connection.getHeaderField(HEADER_NAME));
  }

  public void testSetGetMultipleHeaders() throws IOException {
    MockHttpURLConnection connection = new MockHttpURLConnection(new URL(HttpTesting.SIMPLE_URL));
    List<String> values = Arrays.asList("value1", "value2", "value3");
    for (String value : values) {
      connection.addHeader(HEADER_NAME, value);
    }
    Map<String, List<String>> headers = connection.getHeaderFields();
    assertEquals(3, headers.get(HEADER_NAME).size());
    for (int i = 0; i < 3; i++) {
      assertEquals(values.get(i), headers.get(HEADER_NAME).get(i));
    }
  }

  public void testGetNonExistingHeader() throws IOException {
    MockHttpURLConnection connection = new MockHttpURLConnection(new URL(HttpTesting.SIMPLE_URL));
    assertNull(connection.getHeaderField(HEADER_NAME));
  }

  public void testSetInputStreamAndInputStreamImmutable() throws IOException {
    MockHttpURLConnection connection = new MockHttpURLConnection(new URL(HttpTesting.SIMPLE_URL));
    connection.setInputStream(new ByteArrayInputStream(StringUtils.getBytesUtf8(RESPONSE_BODY)));
    connection.setInputStream(new ByteArrayInputStream(StringUtils.getBytesUtf8("override")));
    byte[] buf = new byte[10];
    InputStream in = connection.getInputStream();
    int n = 0, bytes = 0;
    while ((n = in.read(buf)) != -1) {
      bytes += n;
    }
    assertEquals(RESPONSE_BODY, new String(buf, 0, bytes));
  }
}
