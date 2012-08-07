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
import com.google.api.client.testing.http.MockHttpUnsuccessfulResponseHandler;
import com.google.api.client.testing.http.MockLowLevelHttpRequest;
import com.google.api.client.testing.http.MockLowLevelHttpResponse;
import com.google.api.client.util.Key;
import com.google.api.client.util.Value;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;

import junit.framework.Assert;
import junit.framework.TestCase;

import java.io.ByteArrayOutputStream;
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

  private static final EnumSet<HttpMethod> BASIC_METHODS = EnumSet.of(HttpMethod.GET,
      HttpMethod.PUT, HttpMethod.POST, HttpMethod.DELETE);
  private static final EnumSet<HttpMethod> OTHER_METHODS = EnumSet.of(HttpMethod.HEAD,
      HttpMethod.PATCH);

  public HttpRequestTest(String name) {
    super(name);
  }

  @Override
  public void setUp() {
    // suppress logging warnings to the console
    HttpTransport.LOGGER.setLevel(java.util.logging.Level.SEVERE);
  }

  @Override
  public void tearDown() {
    // back to the standard logging level for console
    HttpTransport.LOGGER.setLevel(java.util.logging.Level.WARNING);
  }

  public void testNotSupportedByDefault() throws IOException {
    MockHttpTransport transport = new MockHttpTransport();
    HttpRequest request =
        transport.createRequestFactory().buildGetRequest(HttpTesting.SIMPLE_GENERIC_URL);
    for (HttpMethod method : BASIC_METHODS) {
      request.setMethod(method);
      request.execute();
    }
    for (HttpMethod method : OTHER_METHODS) {
      transport =
          MockHttpTransport.builder().setSupportedOptionalMethods(ImmutableSet.<HttpMethod>of())
              .build();
      request = transport.createRequestFactory().buildGetRequest(HttpTesting.SIMPLE_GENERIC_URL);
      request.setMethod(method);
      try {
        request.execute();
        fail("expected IllegalArgumentException");
      } catch (IllegalArgumentException e) {
      }
      transport =
          MockHttpTransport.builder().setSupportedOptionalMethods(ImmutableSet.of(method)).build();
      request = transport.createRequestFactory().buildGetRequest(HttpTesting.SIMPLE_GENERIC_URL);
      request.setMethod(method);
      request.execute();
    }
  }

  static private class MockBackOffPolicy implements BackOffPolicy {

    int backOffCalls;
    int resetCalls;
    boolean returnBackOffStop;

    MockBackOffPolicy() {
    }

    public boolean isBackOffRequired(int statusCode) {
      switch (statusCode) {
        case HttpStatusCodes.STATUS_CODE_SERVER_ERROR: // 500
        case HttpStatusCodes.STATUS_CODE_SERVICE_UNAVAILABLE: // 503
          return true;
        default:
          return false;
      }
    }

    public void reset() {
      resetCalls++;
    }

    public long getNextBackOffMillis() {
      backOffCalls++;
      if (returnBackOffStop) {
        return BackOffPolicy.STOP;
      }
      return 0;
    }
  }

  /**
   * Transport used for testing the redirection logic in HttpRequest.
   */
  static private class RedirectTransport extends MockHttpTransport {

    int lowLevelExecCalls;

    final boolean removeLocation;
    final boolean infiniteRedirection;
    final int redirectStatusCode;

    LowLevelHttpRequest retryableGetRequest = new MockLowLevelHttpRequest() {

      @Override
      public LowLevelHttpResponse execute() {
        lowLevelExecCalls++;

        if (infiniteRedirection || lowLevelExecCalls == 1) {
          // Return redirect on only the first call.
          // If infiniteRedirection is true then always return the redirect status code.
          MockLowLevelHttpResponse response = new MockLowLevelHttpResponse();
          response.setStatusCode(redirectStatusCode);
          if (!removeLocation) {
            response.addHeader("Location", HttpTesting.SIMPLE_URL);
          }
          return response;
        }
        // Return success on the second if infiniteRedirection is False.
        MockLowLevelHttpResponse response = new MockLowLevelHttpResponse();
        response.setContent("{\"data\":{\"foo\":{\"v1\":{}}}}");
        return response;
      }
    };

    protected RedirectTransport(boolean removeLocation, boolean infiniteRedirection,
        int redirectStatusCode) {
      this.removeLocation = removeLocation;
      this.infiniteRedirection = infiniteRedirection;
      this.redirectStatusCode = redirectStatusCode;
    }

    @Override
    public LowLevelHttpRequest buildGetRequest(String url) {
      return retryableGetRequest;
    }

    @Override
    public LowLevelHttpRequest buildPostRequest(String url) {
      return retryableGetRequest;
    }
  }

  public void test301Redirect() throws IOException {
    // Set up RedirectTransport to redirect on the first request and then return success.
    RedirectTransport fakeTransport =
        new RedirectTransport(false, false, HttpStatusCodes.STATUS_CODE_MOVED_PERMANENTLY);
    HttpRequest request =
        fakeTransport.createRequestFactory().buildGetRequest(new GenericUrl("http://gmail.com"));
    HttpResponse resp = request.execute();

    Assert.assertEquals(200, resp.getStatusCode());
    Assert.assertEquals(2, fakeTransport.lowLevelExecCalls);
  }

  public void test301RedirectWithUnsuccessfulResponseHandled() throws IOException {
    MockHttpUnsuccessfulResponseHandler handler = new MockHttpUnsuccessfulResponseHandler(true);
    MockBackOffPolicy backOffPolicy = new MockBackOffPolicy();
    // Set up RedirectTransport to redirect on the first request and then return success.
    RedirectTransport fakeTransport =
        new RedirectTransport(false, false, HttpStatusCodes.STATUS_CODE_MOVED_PERMANENTLY);
    HttpRequest request =
        fakeTransport.createRequestFactory().buildGetRequest(new GenericUrl("http://gmail.com"));
    request.setUnsuccessfulResponseHandler(handler);
    request.setBackOffPolicy(backOffPolicy);
    HttpResponse resp = request.execute();

    Assert.assertEquals(200, resp.getStatusCode());
    Assert.assertEquals(2, fakeTransport.lowLevelExecCalls);
    // Assert that the redirect logic was not invoked because the response handler could handle the
    // request. The request url should be the original http://gmail.com
    Assert.assertEquals("http://gmail.com", request.getUrl().toString());
    // Assert that the backoff policy was not invoked because the response handler could handle the
    // request.
    Assert.assertEquals(1, backOffPolicy.resetCalls);
    Assert.assertEquals(0, backOffPolicy.backOffCalls);
    Assert.assertTrue(handler.isCalled());
  }

  public void test301RedirectWithUnsuccessfulResponseNotHandled() throws IOException {
    // Create an Unsuccessful response handler that always returns false.
    MockHttpUnsuccessfulResponseHandler handler = new MockHttpUnsuccessfulResponseHandler(false);
    MockBackOffPolicy backOffPolicy = new MockBackOffPolicy();
    // Set up RedirectTransport to redirect on the first request and then return success.
    RedirectTransport fakeTransport =
        new RedirectTransport(false, false, HttpStatusCodes.STATUS_CODE_MOVED_PERMANENTLY);
    HttpRequest request =
        fakeTransport.createRequestFactory().buildGetRequest(new GenericUrl("http://gmail.com"));
    request.setUnsuccessfulResponseHandler(handler);
    request.setBackOffPolicy(backOffPolicy);
    HttpResponse resp = request.execute();

    Assert.assertEquals(200, resp.getStatusCode());
    // Assert that the redirect logic was invoked because the response handler could not handle the
    // request. The request url should have changed from http://gmail.com to http://google.com
    Assert.assertEquals(HttpTesting.SIMPLE_URL, request.getUrl().toString());
    Assert.assertEquals(2, fakeTransport.lowLevelExecCalls);
    // Assert that the backoff policy is never invoked (except to reset) because the response
    // handler returned false.
    Assert.assertEquals(1, backOffPolicy.resetCalls);
    Assert.assertEquals(0, backOffPolicy.backOffCalls);
  }

  public void test303Redirect() throws IOException {
    // Set up RedirectTransport to redirect on the first request and then return success.
    RedirectTransport fakeTransport =
        new RedirectTransport(false, false, HttpStatusCodes.STATUS_CODE_SEE_OTHER);
    byte[] content = new byte[300];
    Arrays.fill(content, (byte) ' ');
    HttpRequest request =
        fakeTransport.createRequestFactory().buildPostRequest(new GenericUrl("http://gmail.com"),
            new ByteArrayContent(null, content));
    request.setMethod(HttpMethod.POST);
    HttpResponse resp = request.execute();

    Assert.assertEquals(200, resp.getStatusCode());
    Assert.assertEquals(2, fakeTransport.lowLevelExecCalls);
    // Assert that the method in the request was changed to a GET due to the 303.
    Assert.assertEquals(HttpMethod.GET, request.getMethod());
  }

  public void testInfiniteRedirects() throws IOException {
    // Set up RedirectTransport to cause infinite redirections.
    RedirectTransport fakeTransport =
        new RedirectTransport(false, true, HttpStatusCodes.STATUS_CODE_MOVED_PERMANENTLY);
    HttpRequest request =
        fakeTransport.createRequestFactory().buildGetRequest(new GenericUrl("http://gmail.com"));
    try {
      request.execute();
      fail("expected HttpResponseException");
    } catch (HttpResponseException e) {
    }

    // Should be called 1 more than the number of retries allowed (because the first request is not
    // counted as a retry).
    Assert.assertEquals(request.getNumberOfRetries() + 1, fakeTransport.lowLevelExecCalls);
  }

  public void testMissingLocationRedirect() throws IOException {
    // Set up RedirectTransport to set responses with missing location headers.
    RedirectTransport fakeTransport =
        new RedirectTransport(true, false, HttpStatusCodes.STATUS_CODE_MOVED_PERMANENTLY);
    HttpRequest request =
        fakeTransport.createRequestFactory().buildGetRequest(new GenericUrl("http://gmail.com"));
    try {
      request.execute();
      fail("expected HttpResponseException");
    } catch (HttpResponseException e) {
    }

    Assert.assertEquals(1, fakeTransport.lowLevelExecCalls);
  }

  static private class FailThenSuccessBackoffTransport extends MockHttpTransport {

    public int lowLevelExecCalls;
    int errorStatusCode;
    int callsBeforeSuccess;

    protected FailThenSuccessBackoffTransport(int errorStatusCode, int callsBeforeSuccess) {
      this.errorStatusCode = errorStatusCode;
      this.callsBeforeSuccess = callsBeforeSuccess;
    }

    public LowLevelHttpRequest retryableGetRequest = new MockLowLevelHttpRequest() {

      @Override
      public LowLevelHttpResponse execute() {
        lowLevelExecCalls++;

        if (lowLevelExecCalls <= callsBeforeSuccess) {
          // Return failure on the first call
          MockLowLevelHttpResponse response = new MockLowLevelHttpResponse();
          response.setContent("INVALID TOKEN");
          response.setStatusCode(errorStatusCode);
          return response;
        }
        // Return success on the second
        MockLowLevelHttpResponse response = new MockLowLevelHttpResponse();
        response.setContent("{\"data\":{\"foo\":{\"v1\":{}}}}");
        response.setStatusCode(200);
        return response;
      }
    };

    @Override
    public LowLevelHttpRequest buildGetRequest(String url) {
      return retryableGetRequest;
    }
  }

  static private class FailThenSuccessConnectionErrorTransport extends MockHttpTransport {

    public int lowLevelExecCalls;
    int callsBeforeSuccess;
    List<String> userAgentHeader = Lists.newArrayList();

    protected FailThenSuccessConnectionErrorTransport(int callsBeforeSuccess) {
      this.callsBeforeSuccess = callsBeforeSuccess;
    }

    public MockLowLevelHttpRequest retryableGetRequest = new MockLowLevelHttpRequest() {

      @Override
      public LowLevelHttpResponse execute() throws IOException {
        lowLevelExecCalls++;

        userAgentHeader = getHeaders().get("User-Agent");

        if (lowLevelExecCalls <= callsBeforeSuccess) {
          throw new IOException();
        }
        // Return success when count is more than callsBeforeSuccess
        MockLowLevelHttpResponse response = new MockLowLevelHttpResponse();
        response.setStatusCode(200);
        return response;
      }
    };

    @Override
    public LowLevelHttpRequest buildGetRequest(String url) {
      retryableGetRequest.getHeaders().clear();
      return retryableGetRequest;
    }
  }

  public void testExecuteErrorWithRetryEnabled() throws IOException {
    int callsBeforeSuccess = 3;
    FailThenSuccessConnectionErrorTransport fakeTransport =
        new FailThenSuccessConnectionErrorTransport(callsBeforeSuccess);
    HttpRequest req =
        fakeTransport.createRequestFactory().buildGetRequest(new GenericUrl("http://not/used"));
    req.setRetryOnExecuteIOException(true);
    req.setNumberOfRetries(callsBeforeSuccess + 1);
    HttpResponse resp = req.execute();

    Assert.assertEquals(200, resp.getStatusCode());
    Assert.assertEquals(4, fakeTransport.lowLevelExecCalls);
  }

  public void testExecuteErrorWithRetryEnabledBeyondRetryLimit() throws IOException {
    int callsBeforeSuccess = 11;
    FailThenSuccessConnectionErrorTransport fakeTransport =
        new FailThenSuccessConnectionErrorTransport(callsBeforeSuccess);
    HttpRequest req =
        fakeTransport.createRequestFactory().buildGetRequest(new GenericUrl("http://not/used"));
    req.setRetryOnExecuteIOException(true);
    req.setNumberOfRetries(callsBeforeSuccess - 1);
    try {
      req.execute();
      fail("Expected: " + IOException.class);
    } catch (IOException e) {
      // Expected
    }
    Assert.assertEquals(callsBeforeSuccess, fakeTransport.lowLevelExecCalls);
  }

  public void testExecuteErrorWithRetryDisabled() throws IOException {
    int callsBeforeSuccess = 3;
    FailThenSuccessConnectionErrorTransport fakeTransport =
        new FailThenSuccessConnectionErrorTransport(callsBeforeSuccess);
    HttpRequest req =
        fakeTransport.createRequestFactory().buildGetRequest(new GenericUrl("http://not/used"));
    // retryOnExecuteError is disabled by default.
    req.setNumberOfRetries(callsBeforeSuccess + 1);
    try {
      req.execute();
      fail("Expected: " + IOException.class);
    } catch (IOException e) {
      // Expected
    }
    Assert.assertEquals(1, fakeTransport.lowLevelExecCalls);
  }

  public void testUserAgentWithExecuteErrorAndRetryEnabled() throws IOException {
    int callsBeforeSuccess = 3;
    FailThenSuccessConnectionErrorTransport fakeTransport =
        new FailThenSuccessConnectionErrorTransport(callsBeforeSuccess);
    HttpRequest req =
        fakeTransport.createRequestFactory().buildGetRequest(new GenericUrl("http://not/used"));
    req.setRetryOnExecuteIOException(true);
    req.setNumberOfRetries(callsBeforeSuccess + 1);
    HttpResponse resp = req.execute();

    Assert.assertEquals(1, fakeTransport.userAgentHeader.size());
    Assert.assertEquals(HttpRequest.USER_AGENT_SUFFIX, fakeTransport.userAgentHeader.get(0));
    Assert.assertEquals(200, resp.getStatusCode());
    Assert.assertEquals(4, fakeTransport.lowLevelExecCalls);
  }

  public void testAbnormalResponseHandlerWithNoBackOff() throws IOException {
    FailThenSuccessBackoffTransport fakeTransport =
        new FailThenSuccessBackoffTransport(HttpStatusCodes.STATUS_CODE_UNAUTHORIZED, 1);
    MockHttpUnsuccessfulResponseHandler handler = new MockHttpUnsuccessfulResponseHandler(true);

    HttpRequest req =
        fakeTransport.createRequestFactory().buildGetRequest(new GenericUrl("http://not/used"));
    req.setUnsuccessfulResponseHandler(handler);
    req.setBackOffPolicy(null);
    HttpResponse resp = req.execute();

    Assert.assertEquals(200, resp.getStatusCode());
    Assert.assertEquals(2, fakeTransport.lowLevelExecCalls);
    Assert.assertTrue(handler.isCalled());
  }

  public void testAbnormalResponseHandlerWithBackOff() throws IOException {
    FailThenSuccessBackoffTransport fakeTransport =
        new FailThenSuccessBackoffTransport(HttpStatusCodes.STATUS_CODE_SERVER_ERROR, 1);
    MockHttpUnsuccessfulResponseHandler handler = new MockHttpUnsuccessfulResponseHandler(true);
    MockBackOffPolicy backOffPolicy = new MockBackOffPolicy();

    HttpRequest req =
        fakeTransport.createRequestFactory().buildGetRequest(new GenericUrl("http://not/used"));
    req.setUnsuccessfulResponseHandler(handler);
    req.setBackOffPolicy(backOffPolicy);
    HttpResponse resp = req.execute();

    Assert.assertEquals(200, resp.getStatusCode());
    Assert.assertEquals(2, fakeTransport.lowLevelExecCalls);
    Assert.assertEquals(1, backOffPolicy.resetCalls);
    Assert.assertEquals(0, backOffPolicy.backOffCalls);
    Assert.assertTrue(handler.isCalled());
  }

  public void testBackOffSingleCall() throws IOException {
    FailThenSuccessBackoffTransport fakeTransport =
        new FailThenSuccessBackoffTransport(HttpStatusCodes.STATUS_CODE_SERVER_ERROR, 1);
    MockHttpUnsuccessfulResponseHandler handler = new MockHttpUnsuccessfulResponseHandler(false);
    MockBackOffPolicy backOffPolicy = new MockBackOffPolicy();

    HttpRequest req =
        fakeTransport.createRequestFactory().buildGetRequest(new GenericUrl("http://not/used"));
    req.setUnsuccessfulResponseHandler(handler);
    req.setBackOffPolicy(backOffPolicy);
    HttpResponse resp = req.execute();

    Assert.assertEquals(200, resp.getStatusCode());
    Assert.assertEquals(2, fakeTransport.lowLevelExecCalls);
    Assert.assertEquals(1, backOffPolicy.resetCalls);
    Assert.assertEquals(1, backOffPolicy.backOffCalls);
    Assert.assertTrue(handler.isCalled());
  }

  public void testBackOffMultipleCalls() throws IOException {
    int callsBeforeSuccess = 5;
    FailThenSuccessBackoffTransport fakeTransport =
        new FailThenSuccessBackoffTransport(HttpStatusCodes.STATUS_CODE_SERVER_ERROR,
            callsBeforeSuccess);
    MockHttpUnsuccessfulResponseHandler handler = new MockHttpUnsuccessfulResponseHandler(false);
    MockBackOffPolicy backOffPolicy = new MockBackOffPolicy();

    HttpRequest req =
        fakeTransport.createRequestFactory().buildGetRequest(new GenericUrl("http://not/used"));
    req.setUnsuccessfulResponseHandler(handler);
    req.setBackOffPolicy(backOffPolicy);
    HttpResponse resp = req.execute();

    Assert.assertEquals(200, resp.getStatusCode());
    Assert.assertEquals(callsBeforeSuccess + 1, fakeTransport.lowLevelExecCalls);
    Assert.assertEquals(1, backOffPolicy.resetCalls);
    Assert.assertEquals(callsBeforeSuccess, backOffPolicy.backOffCalls);
    Assert.assertTrue(handler.isCalled());
  }

  public void testBackOffCallsBeyondRetryLimit() throws IOException {
    int callsBeforeSuccess = 11;
    FailThenSuccessBackoffTransport fakeTransport =
        new FailThenSuccessBackoffTransport(HttpStatusCodes.STATUS_CODE_SERVER_ERROR,
            callsBeforeSuccess);
    MockHttpUnsuccessfulResponseHandler handler = new MockHttpUnsuccessfulResponseHandler(false);
    MockBackOffPolicy backOffPolicy = new MockBackOffPolicy();

    HttpRequest req =
        fakeTransport.createRequestFactory().buildGetRequest(new GenericUrl("http://not/used"));
    req.setNumberOfRetries(callsBeforeSuccess - 1);
    req.setUnsuccessfulResponseHandler(handler);
    req.setBackOffPolicy(backOffPolicy);
    try {
      req.execute();
      fail("expected HttpResponseException");
    } catch (HttpResponseException e) {
    }
    Assert.assertEquals(callsBeforeSuccess, fakeTransport.lowLevelExecCalls);
    Assert.assertEquals(1, backOffPolicy.resetCalls);
    Assert.assertEquals(callsBeforeSuccess - 1, backOffPolicy.backOffCalls);
    Assert.assertTrue(handler.isCalled());
  }

  public void testBackOffUnRecognizedStatusCode() throws IOException {
    FailThenSuccessBackoffTransport fakeTransport =
        new FailThenSuccessBackoffTransport(HttpStatusCodes.STATUS_CODE_UNAUTHORIZED, 1);
    MockHttpUnsuccessfulResponseHandler handler = new MockHttpUnsuccessfulResponseHandler(false);
    MockBackOffPolicy backOffPolicy = new MockBackOffPolicy();

    HttpRequest req =
        fakeTransport.createRequestFactory().buildGetRequest(new GenericUrl("http://not/used"));
    req.setUnsuccessfulResponseHandler(handler);
    req.setBackOffPolicy(backOffPolicy);
    try {
      req.execute();
    } catch (HttpResponseException e) {
    }

    Assert.assertEquals(1, fakeTransport.lowLevelExecCalls);
    Assert.assertEquals(1, backOffPolicy.resetCalls);
    // The BackOffPolicy should not be called since it does not support 401 status codes.
    Assert.assertEquals(0, backOffPolicy.backOffCalls);
    Assert.assertTrue(handler.isCalled());
  }

  public void testBackOffStop() throws IOException {
    int callsBeforeSuccess = 5;
    FailThenSuccessBackoffTransport fakeTransport =
        new FailThenSuccessBackoffTransport(HttpStatusCodes.STATUS_CODE_SERVER_ERROR,
            callsBeforeSuccess);
    MockHttpUnsuccessfulResponseHandler handler = new MockHttpUnsuccessfulResponseHandler(false);
    MockBackOffPolicy backOffPolicy = new MockBackOffPolicy();
    backOffPolicy.returnBackOffStop = true;

    HttpRequest req =
        fakeTransport.createRequestFactory().buildGetRequest(new GenericUrl("http://not/used"));
    req.setUnsuccessfulResponseHandler(handler);
    req.setBackOffPolicy(backOffPolicy);
    try {
      req.execute();
    } catch (HttpResponseException e) {
    }

    Assert.assertEquals(1, fakeTransport.lowLevelExecCalls);
    Assert.assertEquals(1, backOffPolicy.resetCalls);
    // The BackOffPolicy should be called only once and then it should return BackOffPolicy.STOP
    // should stop all back off retries.
    Assert.assertEquals(1, backOffPolicy.backOffCalls);
    Assert.assertTrue(handler.isCalled());
  }

  public enum E {

    @Value
    VALUE, @Value("other")
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
    assertFalse(headers.containsKey("Accept-Encoding"));
    assertEquals(ImmutableList.of("foo " + HttpRequest.USER_AGENT_SUFFIX),
        headers.get("User-Agent"));
    assertEquals(ImmutableList.of("b"), headers.get("a"));
    assertEquals(ImmutableList.of("VALUE"), headers.get("value"));
    assertEquals(ImmutableList.of("other"), headers.get("otherValue"));
  }

  @SuppressWarnings("deprecation")
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
              assertFalse(content.getClass().equals(GZipContent.class));
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
    HttpRequest request =
        transport.createRequestFactory().buildPostRequest(HttpTesting.SIMPLE_GENERIC_URL,
            new ByteArrayContent(null, content));
    assertFalse(request.getEnableGZipContent());
    request.execute();
    assertFalse(request.getEnableGZipContent());
    request.execute();
    request.setEnableGZipContent(true);
    transport.expectGZip = true;
    request.execute();
  }

  public void testContentLoggingLimitWithLoggingEnabledAndDisabled() throws IOException {

    class MyTransport extends MockHttpTransport {

      boolean expectLogContent;

      @Override
      public LowLevelHttpRequest buildPostRequest(String url) throws IOException {
        return new MockLowLevelHttpRequest() {
          @Override
          public void setContent(HttpContent content) throws IOException {
            if (expectLogContent) {
              assertEquals(LogContent.class, content.getClass());
            } else {
              assertEquals(ByteArrayContent.class, content.getClass());
            }
            super.setContent(content);
          }
        };
      }
    }
    MyTransport transport = new MyTransport();
    // Set the logging level.
    HttpTransport.LOGGER.setLevel(java.util.logging.Level.CONFIG);
    // Create content of length 300.
    byte[] content = new byte[300];
    Arrays.fill(content, (byte) ' ');
    HttpRequest request =
        transport.createRequestFactory().buildPostRequest(HttpTesting.SIMPLE_GENERIC_URL,
            new ByteArrayContent("text/html", content));

    // Assert logging is enabled by default.
    assertTrue(request.isLoggingEnabled());

    // Set the content logging limit to be equal to the length of the content.
    transport.expectLogContent = true;
    request.setContentLoggingLimit(300);
    request.execute();

    // Set the content logging limit to be less than the length of the content.
    transport.expectLogContent = true;
    request.setContentLoggingLimit(299);
    request.execute();

    // Set the content logging limit to 0 to disable content logging.
    transport.expectLogContent = true;
    request.setContentLoggingLimit(0);
    request.execute();

    // Set the content logging limit to be equal to the length of the content with logging disabled.
    transport.expectLogContent = false;
    request.setContentLoggingLimit(300);
    request.setLoggingEnabled(false);
    request.execute();

    // Assert that an exception is thrown if content logging limit < 0.
    try {
      request.setContentLoggingLimit(-1);
      fail("Expected: " + IllegalArgumentException.class);
    } catch (IllegalArgumentException e) {
      // Expected
    }
  }

  public void testUserAgent() {
    assertTrue(HttpRequest.USER_AGENT_SUFFIX.contains("Google-HTTP-Java-Client"));
    assertTrue(HttpRequest.USER_AGENT_SUFFIX.contains("gzip"));
  }

  public void testExecute_emptyContent() throws IOException {
    class MyTransport extends MockHttpTransport {
      String expectedContent;

      @Override
      public LowLevelHttpRequest buildPostRequest(String url) throws IOException {
        return new MockLowLevelHttpRequest() {
          @Override
          public LowLevelHttpResponse execute() throws IOException {
            if (expectedContent == null) {
              assertNull(getContent());
            } else if (getContent() == null) {
              assertEquals(expectedContent, null);
            } else {
              ByteArrayOutputStream stream = new ByteArrayOutputStream();
              getContent().writeTo(stream);
              assertEquals(expectedContent, stream.toString());
            }
            return super.execute();
          }
        };
      }
    }
    MyTransport transport = new MyTransport();
    HttpRequestFactory requestFactory = transport.createRequestFactory();
    // Turn off allowEmptyContent and assert
    for (HttpMethod method : HttpMethod.values()) {
      boolean isOverriden =
          method == HttpMethod.PUT || method == HttpMethod.PATCH || method == HttpMethod.POST;
      transport.expectedContent = isOverriden ? " " : null;
      requestFactory.buildRequest(method, HttpTesting.SIMPLE_GENERIC_URL, null)
          .setAllowEmptyContent(false).execute();
      transport.expectedContent = isOverriden ? " " : "";
      requestFactory
          .buildRequest(method, HttpTesting.SIMPLE_GENERIC_URL,
              ByteArrayContent.fromString(null, "")).setAllowEmptyContent(false).execute();
      transport.expectedContent = "abc";
      requestFactory.buildRequest(method, HttpTesting.SIMPLE_GENERIC_URL,
          ByteArrayContent.fromString(null, "abc")).execute();
    }

    // Leave allowEmptyContent turned on (default value) and assert
    for (HttpMethod method : HttpMethod.values()) {
      transport.expectedContent = null;
      requestFactory.buildRequest(method, HttpTesting.SIMPLE_GENERIC_URL, null).execute();
      transport.expectedContent = "";
      requestFactory.buildRequest(method, HttpTesting.SIMPLE_GENERIC_URL,
          ByteArrayContent.fromString(null, "")).execute();
      transport.expectedContent = "abc";
      requestFactory.buildRequest(method, HttpTesting.SIMPLE_GENERIC_URL,
          ByteArrayContent.fromString(null, "abc")).execute();
    }
  }

  public void testExecute_headers() throws IOException {
    HttpTransport transport = new MockHttpTransport();
    HttpRequest request =
        transport.createRequestFactory().buildGetRequest(HttpTesting.SIMPLE_GENERIC_URL);
    request.getHeaders().setAccept("*/*");
    request.getHeaders().set("accept", "text/plain");
    request.execute();
  }

  public void testSuppressUserAgentSuffix() throws IOException {
    class MyTransport extends MockHttpTransport {
      String expectedUserAgent;

    @Override
      public LowLevelHttpRequest buildGetRequest(String url) throws IOException {
        return new MockLowLevelHttpRequest() {
            @Override
          public LowLevelHttpResponse execute() throws IOException {
            List<String> userAgents = getHeaders().get("User-Agent");
            String actualUserAgent = userAgents == null ? null : userAgents.get(0);
            assertEquals(expectedUserAgent, actualUserAgent);
            return super.execute();
          }
        };
      }
    }
    MyTransport transport = new MyTransport();
    HttpRequest request =
        transport.createRequestFactory().buildGetRequest(HttpTesting.SIMPLE_GENERIC_URL);

    // Do not specify a User-Agent.
    transport.expectedUserAgent = HttpRequest.USER_AGENT_SUFFIX;
    request.setSuppressUserAgentSuffix(false);
    request.execute();

    // Do not specify a User-Agent.
    transport.expectedUserAgent = null;
    request.setSuppressUserAgentSuffix(true);
    request.execute();

    // Specify a User-Agent with suppress=false.
    transport.expectedUserAgent = "Testing " + HttpRequest.USER_AGENT_SUFFIX;
    request.getHeaders().setUserAgent("Testing");
    request.setSuppressUserAgentSuffix(false);
    request.execute();

    // Specify a User-Agent with suppress=true.
    transport.expectedUserAgent = "Testing";
    request.getHeaders().setUserAgent("Testing");
    request.setSuppressUserAgentSuffix(true);
    request.execute();
  }
}
