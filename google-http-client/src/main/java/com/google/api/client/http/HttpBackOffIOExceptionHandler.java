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
import com.google.api.client.util.BackOffUtils;
import com.google.api.client.util.Beta;
import com.google.api.client.util.Preconditions;
import com.google.api.client.util.Sleeper;
import java.io.IOException;

/**
 * {@link Beta} <br>
 * {@link HttpIOExceptionHandler} implementation with {@link BackOff}.
 *
 * <p>It is designed to work with only one {@link HttpRequest} at a time. As a result you MUST
 * create a new instance of {@link HttpBackOffIOExceptionHandler} with a new instance of {@link
 * BackOff} for each instance of {@link HttpRequest}.
 *
 * <p>Sample usage:
 *
 * <pre>
 * request.setIOExceptionHandler(new HttpBackOffIOExceptionHandler(new ExponentialBackOff());
 * </pre>
 *
 * <p>Note: Implementation doesn't call {@link BackOff#reset} at all, since it expects a new {@link
 * BackOff} instance.
 *
 * <p>Implementation is not thread-safe
 *
 * @author Eyal Peled
 * @since 1.15
 */
@Beta
public class HttpBackOffIOExceptionHandler implements HttpIOExceptionHandler {

  /** Back-off policy. */
  private final BackOff backOff;

  /** Sleeper. */
  private Sleeper sleeper = Sleeper.DEFAULT;

  /**
   * Constructs a new instance from a {@link BackOff}.
   *
   * @param backOff back-off policy
   */
  public HttpBackOffIOExceptionHandler(BackOff backOff) {
    this.backOff = Preconditions.checkNotNull(backOff);
  }

  /** Returns the back-off. */
  public final BackOff getBackOff() {
    return backOff;
  }

  /** Returns the sleeper. */
  public final Sleeper getSleeper() {
    return sleeper;
  }

  /**
   * Sets the sleeper.
   *
   * <p>The default value is {@link Sleeper#DEFAULT}.
   *
   * <p>Overriding is only supported for the purpose of calling the super implementation and
   * changing the return type, but nothing else.
   */
  public HttpBackOffIOExceptionHandler setSleeper(Sleeper sleeper) {
    this.sleeper = Preconditions.checkNotNull(sleeper);
    return this;
  }

  /**
   * {@inheritDoc}
   *
   * <p>Handles the request with {@link BackOff}. That means that if back-off is required a call to
   * {@link Sleeper#sleep(long)} will be made.
   */
  public boolean handleIOException(HttpRequest request, boolean supportsRetry) throws IOException {
    if (!supportsRetry) {
      return false;
    }
    try {
      return BackOffUtils.next(sleeper, backOff);
    } catch (InterruptedException exception) {
      return false;
    }
  }
}
