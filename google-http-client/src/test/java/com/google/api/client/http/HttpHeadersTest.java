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
import com.google.api.client.testing.http.MockLowLevelHttpResponse;
import com.google.api.client.util.Key;
import com.google.common.collect.ImmutableList;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Arrays;
import java.util.List;
import junit.framework.TestCase;

/**
 * Tests {@link HttpHeaders}.
 *
 * @author Yaniv Inbar
 */
public class HttpHeadersTest extends TestCase {

  public HttpHeadersTest() {}

  public HttpHeadersTest(String name) {
    super(name);
  }

  public void testBasicAuthentication() {
    HttpHeaders headers = new HttpHeaders();
    headers.setBasicAuthentication(
        BasicAuthenticationTest.USERNAME, BasicAuthenticationTest.PASSWORD);
    assertEquals(BasicAuthenticationTest.AUTH_HEADER, headers.getAuthorization());
  }

  public static class MyHeaders extends HttpHeaders {

    @Key public String foo;

    @Key Object objNum;

    @Key Object objList;

    @Key long someLong;

    @Key List<String> list;

    @Key String[] r;

    @Key E value;

    @Key E otherValue;
  }

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
    myHeaders.addWarning("warn0");
    myHeaders.addWarning("warn1");
    myHeaders.set("a", "b");
    myHeaders.value = E.VALUE;
    myHeaders.otherValue = E.OTHER_VALUE;

    final MockLowLevelHttpRequest lowLevelRequest = new MockLowLevelHttpRequest();
    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    Writer writer = new OutputStreamWriter(outputStream);

    HttpHeaders.serializeHeaders(myHeaders, null, null, null, lowLevelRequest, null);

    // check headers in the lowLevelRequest
    assertEquals(ImmutableList.of("bar"), lowLevelRequest.getHeaderValues("foo"));
    assertEquals(ImmutableList.of("a", "b", "c"), lowLevelRequest.getHeaderValues("list"));
    assertEquals(ImmutableList.of("a2", "b2", "c2"), lowLevelRequest.getHeaderValues("objlist"));
    assertEquals(ImmutableList.of("a1", "a2"), lowLevelRequest.getHeaderValues("r"));
    assertTrue(lowLevelRequest.getHeaderValues("accept-encoding").isEmpty());
    assertEquals(ImmutableList.of("foo"), lowLevelRequest.getHeaderValues("user-agent"));
    assertEquals(ImmutableList.of("warn0", "warn1"), lowLevelRequest.getHeaderValues("warning"));
    assertEquals(ImmutableList.of("b"), lowLevelRequest.getHeaderValues("a"));
    assertEquals(ImmutableList.of("VALUE"), lowLevelRequest.getHeaderValues("value"));
    assertEquals(ImmutableList.of("other"), lowLevelRequest.getHeaderValues("othervalue"));
    assertEquals(
        ImmutableList.of(String.valueOf(Long.MAX_VALUE)),
        lowLevelRequest.getHeaderValues("content-length"));

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
    expectedOutput.append("Warning: warn0\r\n");
    expectedOutput.append("Warning: warn1\r\n");
    expectedOutput.append("a: b\r\n");

    assertEquals(expectedOutput.toString(), outputStream.toString());
  }

  @SuppressWarnings("unchecked")
  public void testFromHttpHeaders() {
    HttpHeaders rawHeaders = new HttpHeaders();
    rawHeaders.setContentLength(Long.MAX_VALUE);
    rawHeaders.setContentType("foo/bar");
    rawHeaders.setUserAgent("FooBar");
    rawHeaders.addWarning("warn0");
    rawHeaders.addWarning("warn1");
    rawHeaders.set("foo", "bar");
    rawHeaders.set("someLong", "5");
    rawHeaders.set("list", ImmutableList.of("a", "b", "c"));
    rawHeaders.set("objNum", "5");
    rawHeaders.set("objList", ImmutableList.of("a2", "b2", "c2"));
    rawHeaders.set("r", new String[] {"a1", "a2"});
    rawHeaders.set("a", "b");
    rawHeaders.set("value", E.VALUE);
    rawHeaders.set("otherValue", E.OTHER_VALUE);

    MyHeaders myHeaders = new MyHeaders();
    myHeaders.fromHttpHeaders(rawHeaders);
    assertEquals(Long.MAX_VALUE, myHeaders.getContentLength().longValue());
    assertEquals("foo/bar", myHeaders.getContentType());
    assertEquals("FooBar", myHeaders.getUserAgent());
    assertEquals("warn0", myHeaders.getWarning().get(0));
    assertEquals("warn1", myHeaders.getWarning().get(1));
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

  private static final String AUTHORIZATION_HEADERS =
      "Accept-Encoding: gzip\r\nAuthorization: Foo\r\nAuthorization: Bar\r\n";

  public void testAuthorizationHeader() throws IOException {
    // serialization
    HttpHeaders headers = new HttpHeaders();
    headers.setAuthorization(Arrays.asList("Foo", "Bar"));
    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    Writer writer = new OutputStreamWriter(outputStream);
    HttpHeaders.serializeHeadersForMultipartRequests(headers, null, null, writer);
    assertEquals(AUTHORIZATION_HEADERS, outputStream.toString());
    // parsing
    MockLowLevelHttpResponse response = new MockLowLevelHttpResponse();
    response.addHeader("Authorization", "Foo");
    response.addHeader("Authorization", "Bar");
    headers = new HttpHeaders();
    headers.fromHttpResponse(response, null);
    Object authHeader = headers.get("Authorization");
    assertTrue(authHeader.toString(), ImmutableList.of("Foo", "Bar").equals(authHeader));
  }

  public void testHeaderStringValues() {
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
    myHeaders.addWarning("warn");
    myHeaders.set("a", "b");
    myHeaders.value = E.VALUE;
    myHeaders.otherValue = E.OTHER_VALUE;
    // check first header string values
    assertEquals("bar", myHeaders.getFirstHeaderStringValue("foo"));
    assertEquals("a", myHeaders.getFirstHeaderStringValue("list"));
    assertEquals("a2", myHeaders.getFirstHeaderStringValue("objlist"));
    assertEquals("a1", myHeaders.getFirstHeaderStringValue("r"));
    assertNull(myHeaders.getFirstHeaderStringValue("accept-encoding"));
    assertEquals("foo", myHeaders.getFirstHeaderStringValue("user-agent"));
    assertEquals("warn", myHeaders.getFirstHeaderStringValue("warning"));
    assertEquals("b", myHeaders.getFirstHeaderStringValue("a"));
    assertEquals("VALUE", myHeaders.getFirstHeaderStringValue("value"));
    assertEquals("other", myHeaders.getFirstHeaderStringValue("othervalue"));
    assertEquals(
        String.valueOf(Long.MAX_VALUE), myHeaders.getFirstHeaderStringValue("content-length"));
    // check header string values
    assertEquals(ImmutableList.of("bar"), myHeaders.getHeaderStringValues("foo"));
    assertEquals(ImmutableList.of("a", "b", "c"), myHeaders.getHeaderStringValues("list"));
    assertEquals(ImmutableList.of("a2", "b2", "c2"), myHeaders.getHeaderStringValues("objlist"));
    assertEquals(ImmutableList.of("a1", "a2"), myHeaders.getHeaderStringValues("r"));
    assertTrue(myHeaders.getHeaderStringValues("accept-encoding").isEmpty());
    assertEquals(ImmutableList.of("foo"), myHeaders.getHeaderStringValues("user-agent"));
    assertEquals(ImmutableList.of("warn"), myHeaders.getHeaderStringValues("warning"));
    assertEquals(ImmutableList.of("b"), myHeaders.getHeaderStringValues("a"));
    assertEquals(ImmutableList.of("VALUE"), myHeaders.getHeaderStringValues("value"));
    assertEquals(ImmutableList.of("other"), myHeaders.getHeaderStringValues("othervalue"));
    assertEquals(
        ImmutableList.of(String.valueOf(Long.MAX_VALUE)),
        myHeaders.getHeaderStringValues("content-length"));
  }

  public static class SlugHeaders extends HttpHeaders {
    @Key("Slug")
    String slug;
  }

  public void testParseAge() throws Exception {
    MockLowLevelHttpResponse httpResponse =
        new MockLowLevelHttpResponse()
            .setHeaderNames(Arrays.asList("Age"))
            .setHeaderValues(Arrays.asList("3456"));

    HttpHeaders httpHeaders = new HttpHeaders();
    httpHeaders.fromHttpResponse(httpResponse, null);
    assertEquals(3456L, httpHeaders.getAge().longValue());
  }

  public void testFromHttpResponse_normalFlow() throws Exception {
    MockLowLevelHttpResponse httpResponse =
        new MockLowLevelHttpResponse()
            .setHeaderNames(Arrays.asList("Content-Type", "Slug"))
            .setHeaderValues(Arrays.asList("foo/bar", "123456789"));

    // Test the normal HttpHeaders class
    HttpHeaders httpHeaders = new HttpHeaders();
    httpHeaders.fromHttpResponse(httpResponse, null);
    assertEquals("foo/bar", httpHeaders.getContentType());
    assertEquals(ImmutableList.of("123456789"), httpHeaders.get("Slug"));

    // Test creating a SlugHeaders obj using the HttpHeaders' data
    SlugHeaders slugHeaders = new SlugHeaders();
    slugHeaders.fromHttpHeaders(httpHeaders);
    assertEquals("foo/bar", slugHeaders.getContentType());
    assertEquals("123456789", slugHeaders.slug);
  }

  public void testFromHttpResponse_doubleConvert() throws Exception {
    MockLowLevelHttpResponse httpResponse =
        new MockLowLevelHttpResponse()
            .setHeaderNames(Arrays.asList("Content-Type", "Slug"))
            .setHeaderValues(Arrays.asList("foo/bar", "123456789"));

    // Test the normal HttpHeaders class
    SlugHeaders slugHeaders = new SlugHeaders();
    slugHeaders.fromHttpResponse(httpResponse, null);
    assertEquals("foo/bar", slugHeaders.getContentType());
    assertEquals("123456789", slugHeaders.slug);

    // Test creating a HttpHeaders obj using the HttpHeaders' data
    SlugHeaders slugHeaders2 = new SlugHeaders();
    slugHeaders2.fromHttpHeaders(slugHeaders);
    assertEquals("foo/bar", slugHeaders2.getContentType());
    assertEquals("123456789", slugHeaders2.slug);
  }

  public void testFromHttpResponse_clearOldValue() throws Exception {
    HttpHeaders headers = new HttpHeaders();
    headers.put("Foo", "oldValue");
    headers.fromHttpResponse(
        new MockLowLevelHttpResponse()
            .setHeaderNames(Arrays.asList("Foo"))
            .setHeaderValues(Arrays.asList("newvalue")),
        null);
    assertEquals(Arrays.asList("newvalue"), headers.get("Foo"));
  }

  public static class V extends HttpHeaders {
    @Key Void v;
    @Key String s;
  }

  public void testFromHttpResponse_void(String value) throws Exception {
    MockLowLevelHttpResponse httpResponse =
        new MockLowLevelHttpResponse()
            .setHeaderNames(Arrays.asList("v", "v", "s"))
            .setHeaderValues(Arrays.asList("ignore", "ignore2", "svalue"));
    V v = new V();
    v.fromHttpResponse(httpResponse, null);
    assertNull(v.v);
    assertEquals("svalue", v.s);
  }
}
