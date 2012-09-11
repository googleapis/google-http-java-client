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
    long someLong;

    @Key
    List<String> list;

    @Key
    String[] r;

    @Key
    E value;

    @Key
    E otherValue;
  }

  @SuppressWarnings("deprecation")
  public void testSerializeHeaders() throws Exception {
    // custom headers
    MyHeaders myHeaders = new MyHeaders();
    myHeaders.foo = "bar";
    myHeaders.objNum = 5;
    myHeaders.list = ImmutableList.of("a", "b", "c");
    myHeaders.objList = ImmutableList.of("a2", "b2", "c2");
    myHeaders.r = new String[] {"a1", "a2"};
    myHeaders.setAcceptEncoding(null);
    myHeaders.setContentLength(Long.MAX_VALUE);
    myHeaders.setUserAgent("foo");
    myHeaders.set("a", "b");
    myHeaders.value = E.VALUE;
    myHeaders.otherValue = E.OTHER_VALUE;

    final MockLowLevelHttpRequest lowLevelRequest = new MockLowLevelHttpRequest();
    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    Writer writer = new OutputStreamWriter(outputStream);

    HttpHeaders.serializeHeaders(myHeaders, null, null, null, lowLevelRequest, null);

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
    assertEquals(ImmutableList.of(String.valueOf(Long.MAX_VALUE)), headers.get("Content-Length"));

    HttpHeaders.serializeHeadersForMultipartRequests(myHeaders, null, null, writer);

    // check headers in the output stream
    StringBuilder expectedOutput = new StringBuilder();
    expectedOutput.append("Content-Length: " + String.valueOf(Long.MAX_VALUE) + "\r\n");
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
    expectedOutput.append("someLong: 0\r\n");
    expectedOutput.append("User-Agent: foo\r\n");
    expectedOutput.append("value: VALUE\r\n");
    expectedOutput.append("a: b\r\n");

    assertEquals(expectedOutput.toString(), outputStream.toString());
  }

  @SuppressWarnings("unchecked")
  public void testFromHttpHeaders() {
    HttpHeaders rawHeaders = new HttpHeaders();
    rawHeaders.setContentLength(Long.MAX_VALUE);
    rawHeaders.setContentType("foo/bar");
    rawHeaders.setUserAgent("FooBar");
    rawHeaders.set("foo", "bar");
    rawHeaders.set("someLong", "5");
    rawHeaders.set("list", ImmutableList.of("a", "b", "c"));
    rawHeaders.set("objNum", "5");
    rawHeaders.set("objList", ImmutableList.of("a2", "b2", "c2"));
    rawHeaders.set("r", new String[] { "a1", "a2" });
    rawHeaders.set("a", "b");
    rawHeaders.set("value", E.VALUE);
    rawHeaders.set("otherValue", E.OTHER_VALUE);

    MyHeaders myHeaders = new MyHeaders();
    myHeaders.fromHttpHeaders(rawHeaders);
    assertEquals(Long.MAX_VALUE, myHeaders.getContentLength().longValue());
    assertEquals("foo/bar", myHeaders.getContentType());
    assertEquals("FooBar", myHeaders.getUserAgent());
    assertEquals("bar", myHeaders.foo);
    assertEquals(5, myHeaders.someLong);
    assertEquals(ImmutableList.of("5"), myHeaders.objNum);
    assertEquals(ImmutableList.of("a", "b", "c"), myHeaders.list);
    assertEquals(ImmutableList.of("a2", "b2", "c2"), myHeaders.objList);
    assertEquals(ImmutableList.of("a1", "a2"), ImmutableList.copyOf(myHeaders.r));
    assertEquals(ImmutableList.of("b"), myHeaders.get("a"));
    assertEquals(E.VALUE, myHeaders.value);
    assertEquals(E.OTHER_VALUE, myHeaders.otherValue);
  }
}
