/*
 * Copyright (c) 2010 Google Inc.
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

package com.google.api.client.http;

import com.google.api.client.http.HttpRequestTest.E;
import com.google.api.client.testing.http.MockLowLevelHttpRequest;
import com.google.api.client.util.Key;
import com.google.common.collect.ImmutableList;

import junit.framework.TestCase;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.List;
import java.util.Map;

/**
 * Tests {@link HttpHeaders}.
 *
 * @author Yaniv Inbar
 */
public class HttpHeadersTest extends TestCase {

  public HttpHeadersTest() {
  }

  public HttpHeadersTest(String name) {
    super(name);
  }

  public void testBasicAuthentication() {
    HttpHeaders headers = new HttpHeaders();
    headers.setBasicAuthentication(BasicAuthenticationTest.USERNAME,
        BasicAuthenticationTest.PASSWORD);
    assertEquals(BasicAuthenticationTest.AUTH_HEADER, headers.getAuthorization());
  }

  public static class MyHeaders extends HttpHeaders {

    @Key
    public String foo;

    @Key
    Object objNum;

    @Key
    Object objList;

    @Key
    List<String> list;

    @Key
    String[] r;

    @Key
    E value;

    @Key
    E otherValue;
  }

  public void testSerializeHeaders() throws IOException {
    // custom headers
    MyHeaders myHeaders = new MyHeaders();
    myHeaders.foo = "bar";
    myHeaders.objNum = 5;
    myHeaders.list = ImmutableList.of("a", "b", "c");
    myHeaders.objList = ImmutableList.of("a2", "b2", "c2");
    myHeaders.r = new String[] {"a1", "a2"};
    myHeaders.setAcceptEncoding(null);
    myHeaders.setUserAgent("foo");
    myHeaders.set("a", "b");
    myHeaders.value = E.VALUE;
    myHeaders.otherValue = E.OTHER_VALUE;

    final MockLowLevelHttpRequest lowLevelRequest = new MockLowLevelHttpRequest();
    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    Writer writer = new OutputStreamWriter(outputStream);

    HttpHeaders.serializeHeaders(myHeaders, null, null, lowLevelRequest);

    // check headers in the lowLevelRequest
    Map<String, List<String>> headers = lowLevelRequest.getHeaders();
    assertEquals(ImmutableList.of("bar"), headers.get("foo"));
    assertEquals(ImmutableList.of("a", "b", "c"), headers.get("list"));
    assertEquals(ImmutableList.of("a2", "b2", "c2"), headers.get("objList"));
    assertEquals(ImmutableList.of("a1", "a2"), headers.get("r"));
    assertFalse(headers.containsKey("Accept-Encoding"));
    assertEquals(ImmutableList.of("foo"), headers.get("User-Agent"));
    assertEquals(ImmutableList.of("b"), headers.get("a"));
    assertEquals(ImmutableList.of("VALUE"), headers.get("value"));
    assertEquals(ImmutableList.of("other"), headers.get("otherValue"));

    HttpHeaders.serializeHeadersForMultipartRequests(myHeaders, null, null, writer);

    // check headers in the output stream
    StringBuilder expectedOutput = new StringBuilder();
    expectedOutput.append("foo: bar\r\n");
    expectedOutput.append("list: a\r\n");
    expectedOutput.append("list: b\r\n");
    expectedOutput.append("list: c\r\n");
    expectedOutput.append("objList: a2\r\n");
    expectedOutput.append("objList: b2\r\n");
    expectedOutput.append("objList: c2\r\n");
    expectedOutput.append("objNum: 5\r\n");
    expectedOutput.append("otherValue: other\r\n");
    expectedOutput.append("r: a1\r\n");
    expectedOutput.append("r: a2\r\n");
    expectedOutput.append("User-Agent: foo\r\n");
    expectedOutput.append("value: VALUE\r\n");
    expectedOutput.append("a: b\r\n");

    assertEquals(expectedOutput.toString(), outputStream.toString());
  }
}
