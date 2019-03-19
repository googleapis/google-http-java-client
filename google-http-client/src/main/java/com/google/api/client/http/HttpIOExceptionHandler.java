/*
 * Copyright (c) 2013 Google Inc.
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

import com.google.api.client.util.BackOff;
import com.google.api.client.util.Beta;
import java.io.IOException;

/**
 * {@link Beta} <br>
 * Handles an {@link IOException} in an HTTP request.
 *
 * <p>For example, this might be used to handle an {@link IOException} with {@link BackOff} policy.
 *
 * <pre>
 * public static class HttpBackOffIOExceptionHandler implements HttpIOExceptionHandler {
 * BackOff backOff;
 * Sleeper sleeper;
 * public boolean handle(HttpRequest request, boolean supportsRetry) throws IOException {
 * if (!supportsRetry) {
 * return false;
 * }
 * try {
 * return BackOffUtils.next(sleeper, backOff);
 * } catch (InterruptedException exception) {
 * return false;
 * }
 * }
 * }
 * </pre>
 *
 * @author Eyal Peled
 * @since 1.15
 */
@Beta
public interface HttpIOExceptionHandler {

  /**
   * Invoked when an {@link IOException} is thrown during an HTTP request.
   *
   * <p>There is a simple rule that one must follow: If you modify the request object or modify its
   * execute interceptors in a way that should resolve the error, you must return {@code true} to
   * issue a retry.
   *
   * @param request request object that can be read from for context or modified before retry
   * @param supportsRetry whether there will actually be a retry if this handler return {@code true}
   *     . Some handlers may want to have an effect only when there will actually be a retry after
   *     they handle their event (e.g. a handler that implements backoff policy).
   * @return whether or not this handler has made a change that will require the request to be
   *     re-sent.
   */
  boolean handleIOException(HttpRequest request, boolean supportsRetry) throws IOException;
}
