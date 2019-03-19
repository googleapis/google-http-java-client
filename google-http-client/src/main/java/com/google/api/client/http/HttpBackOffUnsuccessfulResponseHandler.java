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
 * Back-off handler which handles an abnormal HTTP response with {@link BackOff}.
 *
 * <p>It is designed to work with only one {@link HttpRequest} at a time. As a result you MUST
 * create a new instance of {@link HttpBackOffUnsuccessfulResponseHandler} with a new instance of
 * {@link BackOff} for each instance of {@link HttpRequest}.
 *
 * <p>Sample usage:
 *
 * <pre>
 * request.setUnsuccessfulResponseHandler(
 * new HttpBackOffUnsuccessfulResponseHandler(new ExponentialBackOff()));
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
public class HttpBackOffUnsuccessfulResponseHandler implements HttpUnsuccessfulResponseHandler {

  /** Back-off policy. */
  private final BackOff backOff;

  /** Defines if back-off is required based on an abnormal HTTP response. */
  private BackOffRequired backOffRequired = BackOffRequired.ON_SERVER_ERROR;

  /** Sleeper. */
  private Sleeper sleeper = Sleeper.DEFAULT;

  /**
   * Constructs a new instance from a {@link BackOff}.
   *
   * @param backOff back-off policy
   */
  public HttpBackOffUnsuccessfulResponseHandler(BackOff backOff) {
    this.backOff = Preconditions.checkNotNull(backOff);
  }

  /** Returns the back-off. */
  public final BackOff getBackOff() {
    return backOff;
  }

  /**
   * Returns the {@link BackOffRequired} instance which determines if back-off is required based on
   * an abnormal HTTP response.
   */
  public final BackOffRequired getBackOffRequired() {
    return backOffRequired;
  }

  /**
   * Sets the {@link BackOffRequired} instance which determines if back-off is required based on an
   * abnormal HTTP response.
   *
   * <p>The default value is {@link BackOffRequired#ON_SERVER_ERROR}.
   *
   * <p>Overriding is only supported for the purpose of calling the super implementation and
   * changing the return type, but nothing else.
   */
  public HttpBackOffUnsuccessfulResponseHandler setBackOffRequired(
      BackOffRequired backOffRequired) {
    this.backOffRequired = Preconditions.checkNotNull(backOffRequired);
    return this;
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
  public HttpBackOffUnsuccessfulResponseHandler setSleeper(Sleeper sleeper) {
    this.sleeper = Preconditions.checkNotNull(sleeper);
    return this;
  }

  /**
   * {@inheritDoc}
   *
   * <p>Handles the request with {@link BackOff}. That means that if back-off is required a call to
   * {@link Sleeper#sleep(long)} will be made.
   */
  @Override
  public boolean handleResponse(HttpRequest request, HttpResponse response, boolean supportsRetry)
      throws IOException {
    if (!supportsRetry) {
      return false;
    }
    // check if back-off is required for this response
    if (backOffRequired.isRequired(response)) {
      try {
        return BackOffUtils.next(sleeper, backOff);
      } catch (InterruptedException exception) {
        // ignore
      }
    }
    return false;
  }

  /**
   * {@link Beta} <br>
   * Interface which defines if back-off is required based on an abnormal {@link HttpResponse}.
   *
   * @author Eyal Peled
   */
  @Beta
  public interface BackOffRequired {

    /** Invoked when an abnormal response is received and determines if back-off is required. */
    boolean isRequired(HttpResponse response);

    /**
     * Back-off required implementation which returns {@code true} to every {@link
     * #isRequired(HttpResponse)} call.
     */
    BackOffRequired ALWAYS =
        new BackOffRequired() {
          public boolean isRequired(HttpResponse response) {
            return true;
          }
        };

    /**
     * Back-off required implementation which its {@link #isRequired(HttpResponse)} returns {@code
     * true} if a server error occurred (5xx).
     */
    BackOffRequired ON_SERVER_ERROR =
        new BackOffRequired() {
          public boolean isRequired(HttpResponse response) {
            return response.getStatusCode() / 100 == 5;
          }
        };
  }
}
