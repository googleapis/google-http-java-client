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

package com.google.api.client.http;

import java.io.IOException;

/**
 *
 * HTTP response callback for asynchronous computation.
 *
 * <p>
 * This callback is used with either {@link HttpRequest#executeAsync(java.util.concurrent.Executor, HttpResponseCallback)}
 * or {@link HttpRequest#executeAsync(HttpResponseCallback)}
 * to execute the HTTP request in a asynchronous non-blocking fashion.
 * </p>
 *
 * @author hussachai@gmail.com (Hussachai Puripunpinyo)
 *
 * @since 1.21
 */
public interface HttpResponseCallback {

  /**
   * This method will be called when a Future is completed with either
   * failure or success.
   * @since 1.21
   */
  public void onComplete();

  /**
   * This method will be called when a Future is completed with no error.
   * @param response
   * @since 1.21
   */
  public void onSuccess(HttpResponse response) throws IOException;

  /**
   * This method will be called when a Future is completed with an error.
   * @param throwable
   * @since 1.21
   */
  public void onFailure(Throwable throwable);

  /**
   * This method will be called when a Future is cancelled.
   * There is no guarantee that the HTTP request is executed.
   * @since 1.21
   */
  public void onInterrupted();

}
