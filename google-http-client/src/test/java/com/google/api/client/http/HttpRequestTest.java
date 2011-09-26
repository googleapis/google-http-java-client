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

import com.google.api.client.json.Json;
import com.google.api.client.testing.http.HttpTesting;
import com.google.api.client.testing.http.MockHttpTransport;
import com.google.api.client.testing.http.MockLowLevelHttpRequest;
import com.google.api.client.testing.http.MockLowLevelHttpResponse;
import com.google.api.client.util.Key;
import com.google.api.client.util.Value;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

import junit.framework.Assert;
import junit.framework.TestCase;

import java.io.IOException;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;

/**
 * Tests {@link HttpRequest}.
 *
 * @author Yaniv Inbar
 */
public class HttpRequestTest extends TestCase {

  private static final EnumSet<HttpMethod> BASIC_METHODS =
      EnumSet.of(HttpMethod.GET, HttpMethod.PUT, HttpMethod.POST, HttpMethod.DELETE);
  private static final EnumSet<HttpMethod> OTHER_METHODS =
      EnumSet.of(HttpMethod.HEAD, HttpMethod.PATCH);

  public HttpRequestTest(String name) {
    super(name);
  }

  public void testNotSupportedByDefault() throws IOException {
    MockHttpTransport transport = new MockHttpTransport();
    HttpRequest request =
        transport.createRequestFactory().buildGetRequest(new GenericUrl("http://www.google.com"));
    for (HttpMethod method : BASIC_METHODS) {
      request.setMethod(method);
      request.execute();
    }
    for (HttpMethod method : OTHER_METHODS) {
      transport = MockHttpTransport
          .builder().setSupportedOptionalMethods(ImmutableSet.<HttpMethod>of()).build();
      request =
          transport.createRequestFactory().buildGetRequest(new GenericUrl("http://www.google.com"));
      request.setMethod(method);
      try {
        request.execute();
        fail("expected IllegalArgumentException");
      } catch (IllegalArgumentException e) {
      }
      transport =
          MockHttpTransport.builder().setSupportedOptionalMethods(ImmutableSet.of(method)).build();
      request =
          transport.createRequestFactory().buildGetRequest(new GenericUrl("http://www.google.com"));
      request.setMethod(method);
      request.execute();
    }
  }

  static private class FailThenSuccessTransport extends MockHttpTransport {

    public int lowLevelExecCalls = 0;

    public LowLevelHttpRequest retryableGetRequest = new MockLowLevelHttpRequest() {

      @Override
      public LowLevelHttpResponse execute() {
        lowLevelExecCalls++;

        if (lowLevelExecCalls == 1) {
          // Return failure on the first call
          MockLowLevelHttpResponse response = new MockLowLevelHttpResponse();
          response.setContent("INVALID TOKEN");
          response.setStatusCode(401);
          return response;
        }
        // Return success on the second
        MockLowLevelHttpResponse response = new MockLowLevelHttpResponse();
        response.setContent("{\"data\":{\"foo\":{\"v1\":{}}}}");
        response.setStatusCode(200);
        return response;
      }
    };

    protected FailThenSuccessTransport() {
    }

    @Override
    public LowLevelHttpRequest buildGetRequest(String url) {
      return retryableGetRequest;
    }
  }

  static private class TrackInvocationHandler implements HttpUnsuccessfulResponseHandler {
    public boolean isCalled = false;

    public TrackInvocationHandler() {
    }

    @SuppressWarnings("unused")
    public boolean handleResponse(
        HttpRequest request, HttpResponse response, boolean retrySupported) throws IOException {
      isCalled = true;
      return true;
    }
  }

  public void testAbnormalResponseHandler() throws IOException {

    FailThenSuccessTransport fakeTransport = new FailThenSuccessTransport();
    TrackInvocationHandler handler = new TrackInvocationHandler();

    HttpRequest req =
        fakeTransport.createRequestFactory().buildGetRequest(new GenericUrl("http://not/used"));
    req.setUnsuccessfulResponseHandler(handler);
    HttpResponse resp = req.execute();

    Assert.assertEquals(200, resp.getStatusCode());
    Assert.assertEquals(2, fakeTransport.lowLevelExecCalls);
    Assert.assertTrue(handler.isCalled);
  }

  public enum E {

    @Value
    VALUE,
    @Value("other")
    OTHER_VALUE,
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

  public void testExecute_headerSerialization() throws IOException {
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
    // execute request
    final MockLowLevelHttpRequest lowLevelRequest = new MockLowLevelHttpRequest();
    HttpTransport transport = new MockHttpTransport() {
      @Override
      public LowLevelHttpRequest buildGetRequest(String url) throws IOException {
        return lowLevelRequest;
      }
    };
    HttpRequest request =
        transport.createRequestFactory().buildGetRequest(HttpTesting.SIMPLE_GENERIC_URL);
    request.setHeaders(myHeaders);
    request.execute();
    // check headers
    Map<String, List<String>> headers = lowLevelRequest.getHeaders();
    assertEquals(ImmutableList.of("bar"), headers.get("foo"));
    assertEquals(ImmutableList.of("a", "b", "c"), headers.get("list"));
    assertEquals(ImmutableList.of("a2", "b2", "c2"), headers.get("objList"));
    assertEquals(ImmutableList.of("a1", "a2"), headers.get("r"));
    assertFalse(headers.containsKey("acceptEncoding"));
    assertEquals(
        ImmutableList.of("foo " + HttpRequest.USER_AGENT_SUFFIX), headers.get("User-Agent"));
    assertEquals(ImmutableList.of("b"), headers.get("a"));
    assertEquals(ImmutableList.of("VALUE"), headers.get("value"));
    assertEquals(ImmutableList.of("other"), headers.get("otherValue"));
  }

  public void testNormalizeMediaType() {
    assertEquals(Json.CONTENT_TYPE, HttpRequest.normalizeMediaType(Json.CONTENT_TYPE));
    assertEquals("text/html", HttpRequest.normalizeMediaType("text/html; charset=ISO-8859-4"));
  }

  public void testEnabledGZipContent() throws IOException {
    class MyTransport extends MockHttpTransport {

      boolean expectGZip;

      @Override
      public LowLevelHttpRequest buildPostRequest(String url) throws IOException {
        return new MockLowLevelHttpRequest() {

          @Override
          public void setContent(HttpContent content) throws IOException {
            if (expectGZip) {
              assertEquals(GZipContent.class, content.getClass());
              assertEquals("gzip", content.getEncoding());
            } else {
              assertEquals(ByteArrayContent.class, content.getClass());
              assertNull(content.getEncoding());
            }
            super.setContent(content);
          }
        };
      }
    }
    MyTransport transport = new MyTransport();
    byte[] content = new byte[300];
    Arrays.fill(content, (byte) ' ');
    HttpRequest request = transport.createRequestFactory().buildPostRequest(
        HttpTesting.SIMPLE_GENERIC_URL, new ByteArrayContent(null, content));
    assertFalse(request.getEnableGZipContent());
    request.execute();
    assertFalse(request.getEnableGZipContent());
    request.execute();
    request.setEnableGZipContent(true);
    transport.expectGZip = true;
    request.execute();
  }

  public void testUserAgent() {
    assertTrue(HttpRequest.USER_AGENT_SUFFIX.contains("Google-HTTP-Java-Client"));
    assertTrue(HttpRequest.USER_AGENT_SUFFIX.contains("gzip"));
  }
}
