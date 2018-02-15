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

/**
 *
 * Generic HTTP response callback for asynchronous computation.
 *
 * <p>
 * An implementation of {@link HttpResponseCallback} with empty methods
 * allowing sub-classes to override only methods that they're interested in.
 *
 * This class implements all interfaces defined in {@link HttpResponseCallback}
 * excepts {@link #onSuccess(HttpResponse)}
 * </p>
 *
 * @author hussachai@gmail.com (Hussachai Puripunpinyo)
 *
 * @since 1.21
 *
 */
public abstract class HttpResponseCallbackAdapter implements HttpResponseCallback {

  /**
   * {@inheritDoc}
   * <p>This implementation is empty. </p>
   */
  public void onComplete() {
  }

  /**
   * {@inheritDoc}
   * <p>This implementation is empty. </p>
   */
  public void onFailure(Throwable throwable) {
  }

  /**
   * {@inheritDoc}
   * <p>This implementation is empty. </p>
   */
  public void onInterrupted() {
  }


}
