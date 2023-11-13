/*
 * Copyright 2010 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.api.client.http;

import java.io.IOException;

/**
 * Interface which handles abnormal HTTP responses (in other words not 2XX).
 *
 * <p>For example, this might be used to refresh an OAuth 2 token:
 *
 * <pre>
 * public static class RefreshTokenHandler implements HttpUnsuccessfulResponseHandler {
 * public boolean handleResponse(
 * HttpRequest request, HttpResponse response, boolean retrySupported) throws IOException {
 * if (response.getStatusCode() == HttpStatusCodes.STATUS_CODE_UNAUTHORIZED) {
 * refreshToken();
 * }
 * return false;
 * }
 * }
 * </pre>
 *
 * <p>Sample usage with a request factory:
 *
 * <pre>
 * public static HttpRequestFactory createRequestFactory(HttpTransport transport) {
 * final RefreshTokenHandler handler = new RefreshTokenHandler();
 * return transport.createRequestFactory(new HttpRequestInitializer() {
 * public void initialize(HttpRequest request) {
 * request.setUnsuccessfulResponseHandler(handler);
 * }
 * });
 * }
 * </pre>
 *
 * <p>More complex usage example:
 *
 * <pre>
 * public static HttpRequestFactory createRequestFactory2(HttpTransport transport) {
 * final RefreshTokenHandler handler = new RefreshTokenHandler();
 * return transport.createRequestFactory(new HttpRequestInitializer() {
 * public void initialize(HttpRequest request) {
 * request.setUnsuccessfulResponseHandler(new HttpUnsuccessfulResponseHandler() {
 * public boolean handleResponse(
 * HttpRequest request, HttpResponse response, boolean retrySupported)
 * throws IOException {
 * return handler.handleResponse(request, response, retrySupported);
 * }
 * });
 * }
 * });
 * }
 * </pre>
 *
 * @author moshenko@google.com (Jacob Moshenko)
 * @since 1.4
 */
public interface HttpUnsuccessfulResponseHandler {

  /**
   * Handler that will be invoked when an abnormal response is received. There are a few simple
   * rules that one must follow:
   *
   * <ul>
   *   <li>If you modify the request object or modify its execute interceptors in a way that should
   *       resolve the error, you must return true to issue a retry.
   *   <li>Do not read from the content stream, this will prevent the eventual end user from having
   *       access to it.
   * </ul>
   *
   * @param request Request object that can be read from for context or modified before retry
   * @param response Response to process
   * @param supportsRetry Whether there will actually be a retry if this handler return {@code
   *     true}. Some handlers may want to have an effect only when there will actually be a retry
   *     after they handle their event (e.g. a handler that implements exponential backoff).
   * @return Whether or not this handler has made a change that will require the request to be
   *     re-sent.
   */
  boolean handleResponse(HttpRequest request, HttpResponse response, boolean supportsRetry)
      throws IOException;
}
