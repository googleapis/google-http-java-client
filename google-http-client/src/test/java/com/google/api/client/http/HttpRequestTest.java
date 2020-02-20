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

import com.google.api.client.testing.http.HttpTesting;
import com.google.api.client.testing.http.MockHttpTransport;
import com.google.api.client.testing.http.MockHttpUnsuccessfulResponseHandler;
import com.google.api.client.testing.http.MockLowLevelHttpRequest;
import com.google.api.client.testing.http.MockLowLevelHttpResponse;
import com.google.api.client.testing.util.LogRecordingHandler;
import com.google.api.client.testing.util.MockBackOff;
import com.google.api.client.testing.util.MockSleeper;
import com.google.api.client.util.BackOff;
import com.google.api.client.util.Key;
import com.google.api.client.util.LoggingStreamingContent;
import com.google.api.client.util.StringUtils;
import com.google.api.client.util.Value;
import com.google.common.base.Charsets;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.regex.Pattern;
import junit.framework.TestCase;
import org.junit.Assert;

/**
 * Tests {@link HttpRequest}.
 *
 * @author Yaniv Inbar
 */
public class HttpRequestTest extends TestCase {

  private static final ImmutableSet<String> BASIC_METHODS =
      ImmutableSet.of(HttpMethods.GET, HttpMethods.PUT, HttpMethods.POST, HttpMethods.DELETE);
  private static final ImmutableSet<String> OTHER_METHODS =
      ImmutableSet.of(HttpMethods.HEAD, HttpMethods.PATCH);

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

  public void testNotSupportedByDefault() throws Exception {
    MockHttpTransport transport = new MockHttpTransport();
    HttpRequest request =
        transport.createRequestFactory().buildGetRequest(HttpTesting.SIMPLE_GENERIC_URL);
    for (String method : BASIC_METHODS) {
      request.setRequestMethod(method);
      request.execute();
    }
    for (String method : OTHER_METHODS) {
      transport =
          new MockHttpTransport.Builder().setSupportedMethods(ImmutableSet.<String>of()).build();
      request = transport.createRequestFactory().buildGetRequest(HttpTesting.SIMPLE_GENERIC_URL);
      request.setRequestMethod(method);
      try {
        request.execute();
        fail("expected IllegalArgumentException");
      } catch (IllegalArgumentException e) {
      }
      transport =
          new MockHttpTransport.Builder().setSupportedMethods(ImmutableSet.of(method)).build();
      request = transport.createRequestFactory().buildGetRequest(HttpTesting.SIMPLE_GENERIC_URL);
      request.setRequestMethod(method);
      request.execute();
    }
  }

  static class MockExecutor implements Executor {
    private Runnable runnable;

    public void actuallyRun() {
      runnable.run();
    }

    public void execute(Runnable command) {
      this.runnable = command;
    }
  }

  @Deprecated
  private static class MockBackOffPolicy implements BackOffPolicy {

    int backOffCalls;
    int resetCalls;
    boolean returnBackOffStop;

    MockBackOffPolicy() {}

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

  /** Transport used for testing the redirection logic in HttpRequest. */
  static class RedirectTransport extends MockHttpTransport {

    int lowLevelExecCalls;

    boolean removeLocation;
    boolean infiniteRedirection;
    int redirectStatusCode = HttpStatusCodes.STATUS_CODE_MOVED_PERMANENTLY;
    String[] expectedContent;

    LowLevelHttpRequest retryableGetRequest =
        new MockLowLevelHttpRequest() {

          @Override
          public LowLevelHttpResponse execute() throws IOException {
            if (expectedContent != null) {
              assertEquals(
                  String.valueOf(lowLevelExecCalls),
                  expectedContent[lowLevelExecCalls],
                  getContentAsString());
            }
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

    @Override
    public LowLevelHttpRequest buildRequest(String method, String url) {
      return retryableGetRequest;
    }
  }

  private void setBackOffUnsuccessfulResponseHandler(
      HttpRequest request, BackOff backOff, final HttpUnsuccessfulResponseHandler handler) {
    final HttpBackOffUnsuccessfulResponseHandler backOffHandler =
        new HttpBackOffUnsuccessfulResponseHandler(backOff).setSleeper(new MockSleeper());
    request.setUnsuccessfulResponseHandler(
        new HttpUnsuccessfulResponseHandler() {
          public boolean handleResponse(
              HttpRequest request, HttpResponse response, boolean supportsRetry)
              throws IOException {
            return handler.handleResponse(request, response, supportsRetry)
                || backOffHandler.handleResponse(request, response, supportsRetry);
          }
        });
  }

  public void test301Redirect() throws Exception {
    // Set up RedirectTransport to redirect on the first request and then return success.
    RedirectTransport fakeTransport = new RedirectTransport();
    HttpRequest request =
        fakeTransport.createRequestFactory().buildGetRequest(new GenericUrl("http://gmail.com"));
    HttpResponse resp = request.execute();

    Assert.assertEquals(200, resp.getStatusCode());
    Assert.assertEquals(2, fakeTransport.lowLevelExecCalls);
  }

  @Deprecated
  public void test301RedirectWithUnsuccessfulResponseHandled() throws Exception {
    MockHttpUnsuccessfulResponseHandler handler = new MockHttpUnsuccessfulResponseHandler(true);
    MockBackOffPolicy backOffPolicy = new MockBackOffPolicy();
    // Set up RedirectTransport to redirect on the first request and then return success.
    RedirectTransport fakeTransport = new RedirectTransport();
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

  public void test301RedirectWithBackOffUnsuccessfulResponseHandled() throws Exception {
    MockHttpUnsuccessfulResponseHandler handler = new MockHttpUnsuccessfulResponseHandler(true);
    // Set up RedirectTransport to redirect on the first request and then return success.
    RedirectTransport fakeTransport = new RedirectTransport();
    HttpRequest request =
        fakeTransport.createRequestFactory().buildGetRequest(new GenericUrl("http://gmail.com"));

    MockBackOff backOff = new MockBackOff();
    setBackOffUnsuccessfulResponseHandler(request, backOff, handler);

    HttpResponse resp = request.execute();

    Assert.assertEquals(200, resp.getStatusCode());
    Assert.assertEquals(2, fakeTransport.lowLevelExecCalls);
    // Assert that the redirect logic was not invoked because the response handler could handle the
    // request. The request url should be the original http://gmail.com
    Assert.assertEquals("http://gmail.com", request.getUrl().toString());
    // Assert that the backoff was not invoked since the response handler could handle the request
    Assert.assertEquals(0, backOff.getNumberOfTries());
    Assert.assertTrue(handler.isCalled());
  }

  @Deprecated
  public void test301RedirectWithUnsuccessfulResponseNotHandled() throws Exception {
    // Create an Unsuccessful response handler that always returns false.
    MockHttpUnsuccessfulResponseHandler handler = new MockHttpUnsuccessfulResponseHandler(false);
    MockBackOffPolicy backOffPolicy = new MockBackOffPolicy();
    // Set up RedirectTransport to redirect on the first request and then return success.
    RedirectTransport fakeTransport = new RedirectTransport();
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

  public void test301RedirectWithBackOffUnsuccessfulResponseNotHandled() throws Exception {
    // Create an Unsuccessful response handler that always returns false.
    MockHttpUnsuccessfulResponseHandler handler = new MockHttpUnsuccessfulResponseHandler(false);
    // Set up RedirectTransport to redirect on the first request and then return success.
    RedirectTransport fakeTransport = new RedirectTransport();
    HttpRequest request =
        fakeTransport.createRequestFactory().buildGetRequest(new GenericUrl("http://gmail.com"));

    MockBackOff backOff = new MockBackOff();
    setBackOffUnsuccessfulResponseHandler(request, backOff, handler);

    HttpResponse resp = request.execute();

    Assert.assertEquals(200, resp.getStatusCode());
    // Assert that the redirect logic was invoked because the response handler could not handle the
    // request. The request url should have changed from http://gmail.com to http://google.com
    Assert.assertEquals(HttpTesting.SIMPLE_URL, request.getUrl().toString());
    Assert.assertEquals(2, fakeTransport.lowLevelExecCalls);
    // Assert that the backoff was not invoked since it's not required for 3xx errors
    Assert.assertEquals(0, backOff.getNumberOfTries());
  }

  public void test303Redirect() throws Exception {
    // Set up RedirectTransport to redirect on the first request and then return success.
    RedirectTransport fakeTransport = new RedirectTransport();
    fakeTransport.redirectStatusCode = HttpStatusCodes.STATUS_CODE_SEE_OTHER;
    byte[] content = new byte[300];
    Arrays.fill(content, (byte) ' ');
    HttpRequest request =
        fakeTransport
            .createRequestFactory()
            .buildPostRequest(
                new GenericUrl("http://gmail.com"), new ByteArrayContent(null, content));
    request.setRequestMethod(HttpMethods.POST);
    HttpResponse resp = request.execute();

    Assert.assertEquals(200, resp.getStatusCode());
    Assert.assertEquals(2, fakeTransport.lowLevelExecCalls);
    // Assert that the method in the request was changed to a GET due to the 303.
    Assert.assertEquals(HttpMethods.GET, request.getRequestMethod());
    // Assert that the content is null, since GET requests don't support non-zero content-length
    Assert.assertNull(request.getContent());
  }

  public void testInfiniteRedirects() throws Exception {
    // Set up RedirectTransport to cause infinite redirections.
    RedirectTransport fakeTransport = new RedirectTransport();
    fakeTransport.infiniteRedirection = true;
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

  public void testMissingLocationRedirect() throws Exception {
    // Set up RedirectTransport to set responses with missing location headers.
    RedirectTransport fakeTransport = new RedirectTransport();
    fakeTransport.removeLocation = true;
    HttpRequest request =
        fakeTransport.createRequestFactory().buildGetRequest(new GenericUrl("http://gmail.com"));
    try {
      request.execute();
      fail("expected HttpResponseException");
    } catch (HttpResponseException e) {
    }

    Assert.assertEquals(1, fakeTransport.lowLevelExecCalls);
  }

  private static class FailThenSuccessBackoffTransport extends MockHttpTransport {

    public int lowLevelExecCalls;
    int errorStatusCode;
    int callsBeforeSuccess;

    protected FailThenSuccessBackoffTransport(int errorStatusCode, int callsBeforeSuccess) {
      this.errorStatusCode = errorStatusCode;
      this.callsBeforeSuccess = callsBeforeSuccess;
    }

    public LowLevelHttpRequest retryableGetRequest =
        new MockLowLevelHttpRequest() {

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
    public LowLevelHttpRequest buildRequest(String method, String url) {
      return retryableGetRequest;
    }
  }

  private static class FailThenSuccessConnectionErrorTransport extends MockHttpTransport {

    public int lowLevelExecCalls;
    int callsBeforeSuccess;
    List<String> userAgentHeader = Lists.newArrayList();

    protected FailThenSuccessConnectionErrorTransport(int callsBeforeSuccess) {
      this.callsBeforeSuccess = callsBeforeSuccess;
    }

    @Override
    public LowLevelHttpRequest buildRequest(String method, String url) {
      return new MockLowLevelHttpRequest() {

        @Override
        public LowLevelHttpResponse execute() throws IOException {
          lowLevelExecCalls++;

          userAgentHeader = getHeaderValues("User-Agent");

          if (lowLevelExecCalls <= callsBeforeSuccess) {
            throw new IOException();
          }
          // Return success when count is more than callsBeforeSuccess
          MockLowLevelHttpResponse response = new MockLowLevelHttpResponse();
          response.setStatusCode(200);
          return response;
        }
      };
    }
  }

  private static class StatusCodesTransport extends MockHttpTransport {

    int statusCode = 200;

    public StatusCodesTransport() {}

    public MockLowLevelHttpRequest retryableGetRequest =
        new MockLowLevelHttpRequest() {

          @Override
          public LowLevelHttpResponse execute() throws IOException {
            MockLowLevelHttpResponse response = new MockLowLevelHttpResponse();
            response.setStatusCode(statusCode);
            return response;
          }
        };

    @Override
    public LowLevelHttpRequest buildRequest(String method, String url) {
      return retryableGetRequest;
    }
  }

  public void testHandleRedirect() throws Exception {
    StatusCodesTransport transport = new StatusCodesTransport();
    HttpRequest req =
        transport.createRequestFactory().buildGetRequest(new GenericUrl("http://not/used"));
    HttpResponse response = req.execute();
    // 200 should not be redirected
    assertFalse(req.handleRedirect(response.getStatusCode(), response.getHeaders()));

    subtestRedirect(301, true);
    subtestRedirect(302, true);
    subtestRedirect(303, true);
    subtestRedirect(307, true);
    subtestRedirect(307, false);
  }

  private void subtestRedirect(int statusCode, boolean setLocation) throws Exception {
    StatusCodesTransport transport = new StatusCodesTransport();
    transport.statusCode = statusCode;
    HttpRequest req =
        transport.createRequestFactory().buildGetRequest(new GenericUrl("http://not/used"));
    req.setThrowExceptionOnExecuteError(false);
    req.getHeaders()
        .setAuthorization("auth")
        .setIfMatch("etag")
        .setIfNoneMatch("etag")
        .setIfModifiedSince("date")
        .setIfUnmodifiedSince("date")
        .setIfRange("range");
    HttpResponse response = req.execute();
    if (setLocation) {
      response.getHeaders().setLocation("http://redirect/location");
    }
    boolean handleRedirect = req.handleRedirect(response.getStatusCode(), response.getHeaders());
    if (setLocation) {
      assertTrue(handleRedirect);
      assertNull(req.getHeaders().getAuthorization());
      assertNull(req.getHeaders().getIfMatch());
      assertNull(req.getHeaders().getIfNoneMatch());
      assertNull(req.getHeaders().getIfModifiedSince());
      assertNull(req.getHeaders().getIfUnmodifiedSince());
      assertNull(req.getHeaders().getIfRange());
      assertEquals("http://redirect/location", req.getUrl().toString());
    } else {
      assertFalse(handleRedirect);
      assertEquals("auth", req.getHeaders().getAuthorization());
      assertEquals("etag", req.getHeaders().getIfMatch());
      assertEquals("etag", req.getHeaders().getIfNoneMatch());
      assertEquals("date", req.getHeaders().getIfModifiedSince());
      assertEquals("date", req.getHeaders().getIfUnmodifiedSince());
      assertEquals("range", req.getHeaders().getIfRange());
      assertEquals("http://not/used", req.getUrl().toString());
    }
  }

  public void testHandleRedirect_relativeLocation() throws IOException {
    subtestHandleRedirect_relativeLocation("http://some.org/a/b", "z", "http://some.org/a/z");
    subtestHandleRedirect_relativeLocation("http://some.org/a/b", "z/", "http://some.org/a/z/");
    subtestHandleRedirect_relativeLocation("http://some.org/a/b", "/z", "http://some.org/z");
    subtestHandleRedirect_relativeLocation("http://some.org/a/b", "x/z", "http://some.org/a/x/z");
    subtestHandleRedirect_relativeLocation(
        "http://some.org/a/b", "http://other.org/c", "http://other.org/c");
  }

  public void subtestHandleRedirect_relativeLocation(
      String curLocation, String relLocation, String newLocation) throws IOException {
    HttpTransport transport = new MockHttpTransport();
    HttpRequest req = transport.createRequestFactory().buildGetRequest(new GenericUrl(curLocation));
    HttpHeaders responseHeaders = new HttpHeaders().setLocation(relLocation);
    req.handleRedirect(HttpStatusCodes.STATUS_CODE_SEE_OTHER, responseHeaders);
    assertEquals(newLocation, req.getUrl().toString());
  }

  @Deprecated
  public void testExecuteErrorWithRetryEnabled() throws Exception {
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

  public void testExecuteErrorWithIOExceptionHandler() throws Exception {
    int callsBeforeSuccess = 3;
    FailThenSuccessConnectionErrorTransport fakeTransport =
        new FailThenSuccessConnectionErrorTransport(callsBeforeSuccess);
    HttpRequest req =
        fakeTransport.createRequestFactory().buildGetRequest(new GenericUrl("http://not/used"));
    req.setIOExceptionHandler(new HttpBackOffIOExceptionHandler(BackOff.ZERO_BACKOFF));
    req.setNumberOfRetries(callsBeforeSuccess + 1);
    HttpResponse resp = req.execute();

    Assert.assertEquals(200, resp.getStatusCode());
    Assert.assertEquals(4, fakeTransport.lowLevelExecCalls);
  }

  @Deprecated
  public void testExecuteErrorWithRetryEnabledBeyondRetryLimit() throws Exception {
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

  public void testExecuteErrorWithIOExceptionHandlerBeyondRetryLimit() throws Exception {
    int callsBeforeSuccess = 11;
    FailThenSuccessConnectionErrorTransport fakeTransport =
        new FailThenSuccessConnectionErrorTransport(callsBeforeSuccess);
    HttpRequest req =
        fakeTransport.createRequestFactory().buildGetRequest(new GenericUrl("http://not/used"));
    req.setIOExceptionHandler(new HttpBackOffIOExceptionHandler(BackOff.ZERO_BACKOFF));
    req.setNumberOfRetries(callsBeforeSuccess - 1);
    try {
      req.execute();
      fail("Expected: " + IOException.class);
    } catch (IOException e) {
      // Expected
    }
    Assert.assertEquals(callsBeforeSuccess, fakeTransport.lowLevelExecCalls);
  }

  public void testExecuteErrorWithoutIOExceptionHandler() throws Exception {
    int callsBeforeSuccess = 3;
    FailThenSuccessConnectionErrorTransport fakeTransport =
        new FailThenSuccessConnectionErrorTransport(callsBeforeSuccess);
    HttpRequest req =
        fakeTransport.createRequestFactory().buildGetRequest(new GenericUrl("http://not/used"));
    // I/O exception handler is null by default
    req.setNumberOfRetries(callsBeforeSuccess + 1);
    try {
      req.execute();
      fail("Expected: " + IOException.class);
    } catch (IOException e) {
      // Expected
    }
    Assert.assertEquals(1, fakeTransport.lowLevelExecCalls);
  }

  @Deprecated
  public void testUserAgentWithExecuteErrorAndRetryEnabled() throws Exception {
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

  public void testUserAgentWithExecuteErrorAndIOExceptionHandler() throws Exception {
    int callsBeforeSuccess = 3;
    FailThenSuccessConnectionErrorTransport fakeTransport =
        new FailThenSuccessConnectionErrorTransport(callsBeforeSuccess);
    HttpRequest req =
        fakeTransport.createRequestFactory().buildGetRequest(new GenericUrl("http://not/used"));
    req.setIOExceptionHandler(new HttpBackOffIOExceptionHandler(BackOff.ZERO_BACKOFF));
    req.setNumberOfRetries(callsBeforeSuccess + 1);
    HttpResponse resp = req.execute();

    Assert.assertEquals(1, fakeTransport.userAgentHeader.size());
    Assert.assertEquals(HttpRequest.USER_AGENT_SUFFIX, fakeTransport.userAgentHeader.get(0));
    Assert.assertEquals(200, resp.getStatusCode());
    Assert.assertEquals(4, fakeTransport.lowLevelExecCalls);
  }

  public void testAbnormalResponseHandlerWithNoBackOff() throws Exception {
    FailThenSuccessBackoffTransport fakeTransport =
        new FailThenSuccessBackoffTransport(HttpStatusCodes.STATUS_CODE_UNAUTHORIZED, 1);
    MockHttpUnsuccessfulResponseHandler handler = new MockHttpUnsuccessfulResponseHandler(true);

    HttpRequest req =
        fakeTransport.createRequestFactory().buildGetRequest(new GenericUrl("http://not/used"));
    req.setUnsuccessfulResponseHandler(handler);
    HttpResponse resp = req.execute();

    Assert.assertEquals(200, resp.getStatusCode());
    Assert.assertEquals(2, fakeTransport.lowLevelExecCalls);
    Assert.assertTrue(handler.isCalled());
  }

  @Deprecated
  public void testAbnormalResponseHandlerWithBackOff() throws Exception {
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

  public void testAbnormalResponseHandlerWithBackOffUnsuccessfulResponseHandler() throws Exception {
    FailThenSuccessBackoffTransport fakeTransport =
        new FailThenSuccessBackoffTransport(HttpStatusCodes.STATUS_CODE_SERVER_ERROR, 1);
    MockHttpUnsuccessfulResponseHandler handler = new MockHttpUnsuccessfulResponseHandler(true);

    HttpRequest req =
        fakeTransport.createRequestFactory().buildGetRequest(new GenericUrl("http://not/used"));
    MockBackOff backOff = new MockBackOff();
    setBackOffUnsuccessfulResponseHandler(req, backOff, handler);
    HttpResponse resp = req.execute();

    Assert.assertEquals(200, resp.getStatusCode());
    Assert.assertEquals(2, fakeTransport.lowLevelExecCalls);
    Assert.assertEquals(0, backOff.getNumberOfTries());
    Assert.assertTrue(handler.isCalled());
  }

  @Deprecated
  public void testBackOffSingleCall() throws Exception {
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

  public void testBackOffUnsuccessfulResponseSingleCall() throws Exception {
    FailThenSuccessBackoffTransport fakeTransport =
        new FailThenSuccessBackoffTransport(HttpStatusCodes.STATUS_CODE_SERVER_ERROR, 1);
    MockHttpUnsuccessfulResponseHandler handler = new MockHttpUnsuccessfulResponseHandler(false);

    HttpRequest req =
        fakeTransport.createRequestFactory().buildGetRequest(new GenericUrl("http://not/used"));
    MockBackOff backOff = new MockBackOff();
    setBackOffUnsuccessfulResponseHandler(req, backOff, handler);
    HttpResponse resp = req.execute();

    Assert.assertEquals(200, resp.getStatusCode());
    Assert.assertEquals(2, fakeTransport.lowLevelExecCalls);
    Assert.assertEquals(1, backOff.getNumberOfTries());
    Assert.assertTrue(handler.isCalled());
  }

  @Deprecated
  public void testBackOffMultipleCalls() throws Exception {
    int callsBeforeSuccess = 5;
    FailThenSuccessBackoffTransport fakeTransport =
        new FailThenSuccessBackoffTransport(
            HttpStatusCodes.STATUS_CODE_SERVER_ERROR, callsBeforeSuccess);
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

  public void testBackOffUnsucessfulReponseMultipleCalls() throws Exception {
    int callsBeforeSuccess = 5;
    FailThenSuccessBackoffTransport fakeTransport =
        new FailThenSuccessBackoffTransport(
            HttpStatusCodes.STATUS_CODE_SERVER_ERROR, callsBeforeSuccess);
    MockHttpUnsuccessfulResponseHandler handler = new MockHttpUnsuccessfulResponseHandler(false);

    HttpRequest req =
        fakeTransport.createRequestFactory().buildGetRequest(new GenericUrl("http://not/used"));
    MockBackOff backOff = new MockBackOff();
    setBackOffUnsuccessfulResponseHandler(req, backOff, handler);
    HttpResponse resp = req.execute();

    Assert.assertEquals(200, resp.getStatusCode());
    Assert.assertEquals(callsBeforeSuccess + 1, fakeTransport.lowLevelExecCalls);
    Assert.assertEquals(callsBeforeSuccess, backOff.getNumberOfTries());
    Assert.assertTrue(handler.isCalled());
  }

  @Deprecated
  public void testBackOffCallsBeyondRetryLimit() throws Exception {
    int callsBeforeSuccess = 11;
    FailThenSuccessBackoffTransport fakeTransport =
        new FailThenSuccessBackoffTransport(
            HttpStatusCodes.STATUS_CODE_SERVER_ERROR, callsBeforeSuccess);
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

  public void testBackOffUnsuccessfulReponseCallsBeyondRetryLimit() throws Exception {
    int callsBeforeSuccess = 11;
    FailThenSuccessBackoffTransport fakeTransport =
        new FailThenSuccessBackoffTransport(
            HttpStatusCodes.STATUS_CODE_SERVER_ERROR, callsBeforeSuccess);
    MockHttpUnsuccessfulResponseHandler handler = new MockHttpUnsuccessfulResponseHandler(false);

    HttpRequest req =
        fakeTransport.createRequestFactory().buildGetRequest(new GenericUrl("http://not/used"));
    req.setNumberOfRetries(callsBeforeSuccess - 1);
    MockBackOff backOff = new MockBackOff();
    setBackOffUnsuccessfulResponseHandler(req, backOff, handler);
    try {
      req.execute();
      fail("expected HttpResponseException");
    } catch (HttpResponseException e) {
    }
    Assert.assertEquals(callsBeforeSuccess, fakeTransport.lowLevelExecCalls);
    Assert.assertEquals(callsBeforeSuccess - 1, backOff.getMaxTries());
    Assert.assertTrue(handler.isCalled());
  }

  @Deprecated
  public void testBackOffUnRecognizedStatusCode() throws Exception {
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

  public void testBackOffUnsuccessfulReponseUnRecognizedStatusCode() throws Exception {
    FailThenSuccessBackoffTransport fakeTransport =
        new FailThenSuccessBackoffTransport(HttpStatusCodes.STATUS_CODE_UNAUTHORIZED, 1);
    MockHttpUnsuccessfulResponseHandler handler = new MockHttpUnsuccessfulResponseHandler(false);

    HttpRequest req =
        fakeTransport.createRequestFactory().buildGetRequest(new GenericUrl("http://not/used"));
    MockBackOff backOff = new MockBackOff();
    setBackOffUnsuccessfulResponseHandler(req, backOff, handler);
    try {
      req.execute();
    } catch (HttpResponseException e) {
    }

    Assert.assertEquals(1, fakeTransport.lowLevelExecCalls);
    // The back-off should not be called since it does not support 401 status codes.
    Assert.assertEquals(0, backOff.getNumberOfTries());
    Assert.assertTrue(handler.isCalled());
  }

  @Deprecated
  public void testBackOffStop() throws Exception {
    int callsBeforeSuccess = 5;
    FailThenSuccessBackoffTransport fakeTransport =
        new FailThenSuccessBackoffTransport(
            HttpStatusCodes.STATUS_CODE_SERVER_ERROR, callsBeforeSuccess);
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

  public void testBackOffUnsucessfulResponseStop() throws Exception {
    int callsBeforeSuccess = 5;
    FailThenSuccessBackoffTransport fakeTransport =
        new FailThenSuccessBackoffTransport(
            HttpStatusCodes.STATUS_CODE_SERVER_ERROR, callsBeforeSuccess);
    MockHttpUnsuccessfulResponseHandler handler = new MockHttpUnsuccessfulResponseHandler(false);

    HttpRequest req =
        fakeTransport.createRequestFactory().buildGetRequest(new GenericUrl("http://not/used"));
    MockBackOff backOff = new MockBackOff().setMaxTries(1);
    setBackOffUnsuccessfulResponseHandler(req, backOff, handler);
    try {
      req.execute();
    } catch (HttpResponseException e) {
    }

    Assert.assertEquals(2, fakeTransport.lowLevelExecCalls);
    // The back-off should be called only once, since the its max tries is set to 1
    Assert.assertEquals(1, backOff.getNumberOfTries());
    Assert.assertTrue(handler.isCalled());
  }

  public enum E {
    @Value
    VALUE,
    @Value("other")
    OTHER_VALUE,
  }

  public static class MyHeaders extends HttpHeaders {

    @Key public String foo;

    @Key Object objNum;

    @Key Object objList;

    @Key List<String> list;

    @Key String[] r;

    @Key E value;

    @Key E otherValue;
  }

  public void testExecute_headerSerialization() throws Exception {
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
    HttpTransport transport =
        new MockHttpTransport() {
          @Override
          public LowLevelHttpRequest buildRequest(String method, String url) throws IOException {
            return lowLevelRequest;
          }
        };
    HttpRequest request =
        transport.createRequestFactory().buildGetRequest(HttpTesting.SIMPLE_GENERIC_URL);
    request.setHeaders(myHeaders);
    request.execute();
    // check headers
    assertEquals(ImmutableList.of("bar"), lowLevelRequest.getHeaderValues("foo"));
    assertEquals(ImmutableList.of("a", "b", "c"), lowLevelRequest.getHeaderValues("list"));
    assertEquals(ImmutableList.of("a2", "b2", "c2"), lowLevelRequest.getHeaderValues("objlist"));
    assertEquals(ImmutableList.of("a1", "a2"), lowLevelRequest.getHeaderValues("r"));
    assertTrue(lowLevelRequest.getHeaderValues("accept-encoding").isEmpty());
    assertEquals(
        ImmutableList.of("foo Google-HTTP-Java-Client/" + HttpRequest.VERSION + " (gzip)"),
        lowLevelRequest.getHeaderValues("user-agent"));
    assertEquals(ImmutableList.of("b"), lowLevelRequest.getHeaderValues("a"));
    assertEquals(ImmutableList.of("VALUE"), lowLevelRequest.getHeaderValues("value"));
    assertEquals(ImmutableList.of("other"), lowLevelRequest.getHeaderValues("othervalue"));
  }

  public void testGZipEncoding() throws Exception {
    class MyTransport extends MockHttpTransport {

      boolean expectGZip;

      @Override
      public LowLevelHttpRequest buildRequest(String method, String url) throws IOException {
        return new MockLowLevelHttpRequest() {

          @Override
          public LowLevelHttpResponse execute() throws IOException {
            if (expectGZip) {
              assertEquals(HttpEncodingStreamingContent.class, getStreamingContent().getClass());
              assertEquals("gzip", getContentEncoding());
              assertEquals(-1, getContentLength());
            } else {
              assertFalse(
                  getStreamingContent().getClass().equals(HttpEncodingStreamingContent.class));
              assertNull(getContentEncoding());
              assertEquals(300, getContentLength());
            }
            char[] content = new char[300];
            Arrays.fill(content, ' ');
            assertEquals(new String(content), getContentAsString());
            return super.execute();
          }
        };
      }
    }
    MyTransport transport = new MyTransport();
    byte[] content = new byte[300];
    Arrays.fill(content, (byte) ' ');
    HttpRequest request =
        transport
            .createRequestFactory()
            .buildPostRequest(
                HttpTesting.SIMPLE_GENERIC_URL,
                new ByteArrayContent(
                    new HttpMediaType("text/plain").setCharsetParameter(Charsets.UTF_8).build(),
                    content));
    assertNull(request.getEncoding());
    request.execute();
    assertNull(request.getEncoding());
    request.execute();
    request.setEncoding(new GZipEncoding());
    transport.expectGZip = true;
    request.execute();
  }

  public void testContentLoggingLimitWithLoggingEnabledAndDisabled() throws Exception {

    class MyTransport extends MockHttpTransport {

      boolean expectLogContent;

      @Override
      public LowLevelHttpRequest buildRequest(String method, String url) throws IOException {
        return new MockLowLevelHttpRequest() {
          @Override
          public LowLevelHttpResponse execute() throws IOException {
            if (expectLogContent) {
              assertEquals(LoggingStreamingContent.class, getStreamingContent().getClass());
            } else {
              assertEquals(ByteArrayContent.class, getStreamingContent().getClass());
            }
            return super.execute();
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
        transport
            .createRequestFactory()
            .buildPostRequest(
                HttpTesting.SIMPLE_GENERIC_URL, new ByteArrayContent("text/html", content));

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

  public void testVersion() {
    assertNotNull("version constant should not be null", HttpRequest.VERSION);
    Pattern semverPattern = Pattern.compile("\\d+\\.\\d+\\.\\d+(-SNAPSHOT)?");
    assertTrue(semverPattern.matcher(HttpRequest.VERSION).matches());
  }

  public void testUserAgent() {
    assertTrue(HttpRequest.USER_AGENT_SUFFIX.contains("Google-HTTP-Java-Client"));
    assertTrue(HttpRequest.USER_AGENT_SUFFIX.contains("gzip"));
  }

  public void testExecute_headers() throws Exception {
    HttpTransport transport = new MockHttpTransport();
    HttpRequest request =
        transport.createRequestFactory().buildGetRequest(HttpTesting.SIMPLE_GENERIC_URL);
    request.getHeaders().setAccept("*/*");
    request.getHeaders().set("accept", Arrays.asList("text/plain"));
    request.execute();
  }

  public void testSuppressUserAgentSuffix() throws Exception {
    class MyTransport extends MockHttpTransport {
      String expectedUserAgent;

      @Override
      public LowLevelHttpRequest buildRequest(String method, String url) throws IOException {
        return new MockLowLevelHttpRequest() {
          @Override
          public LowLevelHttpResponse execute() throws IOException {
            assertEquals(expectedUserAgent, getFirstHeaderValue("User-Agent"));
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

  public void testExecuteAsync()
      throws IOException, InterruptedException, ExecutionException, TimeoutException {
    MockExecutor mockExecutor = new MockExecutor();
    HttpTransport transport = new MockHttpTransport();
    HttpRequest request =
        transport.createRequestFactory().buildGetRequest(HttpTesting.SIMPLE_GENERIC_URL);
    Future<HttpResponse> futureResponse = request.executeAsync(mockExecutor);

    assertFalse(futureResponse.isDone());
    mockExecutor.actuallyRun();
    assertTrue(futureResponse.isDone());
    assertNotNull(futureResponse.get(10, TimeUnit.MILLISECONDS));
  }

  public void testExecute_redirects() throws Exception {
    class MyTransport extends MockHttpTransport {
      int count = 1;

      @Override
      public LowLevelHttpRequest buildRequest(String method, String url) throws IOException {
        // expect that it redirected to new URL every time using the count
        assertEquals(HttpTesting.SIMPLE_URL + "_" + count, url);
        count++;
        return new MockLowLevelHttpRequest()
            .setResponse(
                new MockLowLevelHttpResponse()
                    .setStatusCode(HttpStatusCodes.STATUS_CODE_MOVED_PERMANENTLY)
                    .setHeaderNames(Arrays.asList("Location"))
                    .setHeaderValues(Arrays.asList(HttpTesting.SIMPLE_URL + "_" + count)));
      }
    }
    MyTransport transport = new MyTransport();
    HttpRequest request =
        transport
            .createRequestFactory()
            .buildGetRequest(new GenericUrl(HttpTesting.SIMPLE_URL + "_" + transport.count));
    try {
      request.execute();
      fail("expected " + HttpResponseException.class);
    } catch (HttpResponseException e) {
      assertEquals(HttpStatusCodes.STATUS_CODE_MOVED_PERMANENTLY, e.getStatusCode());
    }
  }

  public void testExecute_redirectWithIncorrectContentRetryableSetting() throws Exception {
    // TODO(yanivi): any way we can warn user about this?
    RedirectTransport fakeTransport = new RedirectTransport();
    String contentValue = "hello";
    fakeTransport.expectedContent = new String[] {contentValue, ""};
    byte[] bytes = StringUtils.getBytesUtf8(contentValue);
    InputStreamContent content =
        new InputStreamContent(
            new HttpMediaType("text/plain").setCharsetParameter(Charsets.UTF_8).build(),
            new ByteArrayInputStream(bytes));
    content.setRetrySupported(true);
    HttpRequest request =
        fakeTransport
            .createRequestFactory()
            .buildPostRequest(HttpTesting.SIMPLE_GENERIC_URL, content);
    HttpResponse resp = request.execute();
    assertEquals(200, resp.getStatusCode());
    assertEquals(2, fakeTransport.lowLevelExecCalls);
  }

  public void testExecute_curlLogger() throws Exception {
    LogRecordingHandler recorder = new LogRecordingHandler();
    HttpTransport.LOGGER.setLevel(Level.CONFIG);
    HttpTransport.LOGGER.addHandler(recorder);
    new MockHttpTransport()
        .createRequestFactory()
        .buildGetRequest(new GenericUrl("http://google.com/#q=a'b'c"))
        .execute();
    boolean found = false;
    for (String message : recorder.messages()) {
      if (message.startsWith("curl")) {
        found = true;
        assertTrue(message.contains("curl -v --compressed -H 'Accept-Encoding: gzip'"));
        assertTrue(
            message.contains(
                "-H 'User-Agent: Google-HTTP-Java-Client/" + HttpRequest.VERSION + " (gzip)'"));
        assertTrue(message.contains("' -- 'http://google.com/#q=a'\"'\"'b'\"'\"'c'"));
      }
    }
    assertTrue(found);
  }

  public void testExecute_curlLoggerWithContentEncoding() throws Exception {
    LogRecordingHandler recorder = new LogRecordingHandler();
    HttpTransport.LOGGER.setLevel(Level.CONFIG);
    HttpTransport.LOGGER.addHandler(recorder);

    String contentValue = "hello";
    byte[] bytes = StringUtils.getBytesUtf8(contentValue);
    InputStreamContent content =
        new InputStreamContent(
            new HttpMediaType("text/plain").setCharsetParameter(Charsets.UTF_8).build(),
            new ByteArrayInputStream(bytes));

    new MockHttpTransport()
        .createRequestFactory()
        .buildPostRequest(new GenericUrl("http://google.com/#q=a'b'c"), content)
        .setEncoding(new GZipEncoding())
        .execute();

    boolean found = false;
    for (String message : recorder.messages()) {
      if (message.startsWith("curl")) {
        found = true;
        assertTrue(message.contains("curl -v --compressed -X POST -H 'Accept-Encoding: gzip'"));
        assertTrue(message.contains("-H 'User-Agent: " + HttpRequest.USER_AGENT_SUFFIX + "'"));
        assertTrue(
            message.contains(
                "-H 'Content-Type: text/plain; charset=UTF-8' -H 'Content-Encoding: gzip'"));
        assertTrue(message.contains("-d '@-' -- 'http://google.com/#q=a'\"'\"'b'\"'\"'c' << $$$"));
      }
    }
    assertTrue(found);
  }

  public void testVersion_matchesAcceptablePatterns() throws Exception {
    String acceptableVersionPattern =
        "unknown-version|(?:\\d+\\.\\d+\\.\\d+(?:-.*?)?(?:-SNAPSHOT)?)";
    String version = HttpRequest.VERSION;
    assertTrue(
        String.format("the loaded version '%s' did not match the acceptable pattern", version),
        version.matches(acceptableVersionPattern)
    );
  }
}
