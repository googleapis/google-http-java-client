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

import java.io.IOException;

/**
 * HTTP response interceptor to intercept the end of {@link HttpRequest#execute()} before returning
 * a successful response or throwing an exception for an unsuccessful response.
 *
 * <p>For example, this might be used to add a simple timer on requests:
 *
 * <pre>
 * public static class TimerResponseInterceptor implements HttpResponseInterceptor {
 *
 * private final long startTime = System.nanoTime();
 *
 * public void interceptResponse(HttpResponse response) {
 * long elapsedNanos = System.nanoTime() - startTime;
 * System.out.println("elapsed seconds: " + TimeUnit.NANOSECONDS.toSeconds(elapsedNanos) + "s");
 * }
 * }
 * </pre>
 *
 * <p>Sample usage with a request factory:
 *
 * <pre>
 * public static HttpRequestFactory createRequestFactory(HttpTransport transport) {
 * return transport.createRequestFactory(new HttpRequestInitializer() {
 *
 * {@literal @}Override
 * public void initialize(HttpRequest request) {
 * request.setResponseInterceptor(new TimerResponseInterceptor());
 * }
 * });
 * }
 * </pre>
 *
 * <p>More complex usage example:
 *
 * <pre>
 * public static HttpRequestFactory createRequestFactory2(HttpTransport transport) {
 * final HttpResponseInterceptor responseInterceptor = new TimerResponseInterceptor();
 * return transport.createRequestFactory(new HttpRequestInitializer() {
 *
 * public void initialize(HttpRequest request) {
 * request.setResponseInterceptor(new HttpResponseInterceptor() {
 *
 * public void interceptResponse(HttpResponse response) throws IOException {
 * responseInterceptor.interceptResponse(response);
 * }
 * });
 * }
 * });
 * }
 * </pre>
 *
 * <p>Implementations should normally be thread-safe.
 *
 * @author Yaniv Inbar
 * @since 1.13
 */
public interface HttpResponseInterceptor {

  /**
   * Invoked at the end of {@link HttpRequest#execute()} before returning a successful response or
   * throwing an exception for an unsuccessful response.
   *
   * <p>Do not read from the content stream unless you intend to throw an exception. Otherwise, it
   * would prevent the caller of {@link HttpRequest#execute()} to be able to read the stream from
   * {@link HttpResponse#getContent()}. If you intend to throw an exception, you should parse the
   * response, or alternatively pass the response as part of the exception.
   *
   * @param response HTTP response
   */
  void interceptResponse(HttpResponse response) throws IOException;
}
