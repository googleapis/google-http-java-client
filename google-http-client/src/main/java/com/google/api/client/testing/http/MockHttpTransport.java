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

package com.google.api.client.testing.http;

import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.LowLevelHttpRequest;
import com.google.api.client.util.Beta;
import com.google.api.client.util.Preconditions;
import java.io.IOException;
import java.util.Collections;
import java.util.Set;

/**
 * {@link Beta} <br>
 * Mock for {@link HttpTransport}.
 *
 * <p>Implementation is thread-safe. For maximum efficiency, applications should use a single
 * globally-shared instance of the HTTP transport.
 *
 * @author Yaniv Inbar
 * @since 1.3
 */
@Beta
public class MockHttpTransport extends HttpTransport {

  /** Supported HTTP methods or {@code null} to specify that all methods are supported. */
  private Set<String> supportedMethods;

  /**
   * The {@link MockLowLevelHttpRequest} to be returned by {@link #buildRequest}. If this field is
   * {@code null}, {@link #buildRequest} will create a new instance from its arguments.
   */
  private MockLowLevelHttpRequest lowLevelHttpRequest;

  /**
   * The {@link MockLowLevelHttpResponse} to be returned when this {@link MockHttpTransport}
   * executes the associated request. Note that this field is ignored if the caller provided a
   * non-{@code null} {@link MockLowLevelHttpRequest} with this {@link MockHttpTransport} was built.
   */
  private MockLowLevelHttpResponse lowLevelHttpResponse;

  public MockHttpTransport() {}

  /**
   * @param builder builder
   * @since 1.14
   */
  protected MockHttpTransport(Builder builder) {
    supportedMethods = builder.supportedMethods;
    lowLevelHttpRequest = builder.lowLevelHttpRequest;
    lowLevelHttpResponse = builder.lowLevelHttpResponse;
  }

  @Override
  public boolean supportsMethod(String method) throws IOException {
    return supportedMethods == null || supportedMethods.contains(method);
  }

  @Override
  public LowLevelHttpRequest buildRequest(String method, String url) throws IOException {
    Preconditions.checkArgument(supportsMethod(method), "HTTP method %s not supported", method);
    if (lowLevelHttpRequest != null) {
      return lowLevelHttpRequest;
    }
    lowLevelHttpRequest = new MockLowLevelHttpRequest(url);
    if (lowLevelHttpResponse != null) {
      lowLevelHttpRequest.setResponse(lowLevelHttpResponse);
    }
    return lowLevelHttpRequest;
  }

  /**
   * Returns the unmodifiable set of supported HTTP methods or {@code null} to specify that all
   * methods are supported.
   */
  public final Set<String> getSupportedMethods() {
    return supportedMethods == null ? null : Collections.unmodifiableSet(supportedMethods);
  }

  /**
   * Returns the {@link MockLowLevelHttpRequest} that is associated with this {@link Builder}, or
   * {@code null} if no such instance exists.
   *
   * @since 1.18
   */
  public final MockLowLevelHttpRequest getLowLevelHttpRequest() {
    return lowLevelHttpRequest;
  }

  /**
   * {@link Beta} <br>
   * Builder for {@link MockHttpTransport}.
   *
   * <p>Implementation is not thread-safe.
   *
   * @since 1.5
   */
  @Beta
  public static class Builder {

    /** Supported HTTP methods or {@code null} to specify that all methods are supported. */
    Set<String> supportedMethods;

    /**
     * The {@link MockLowLevelHttpRequest} to be returned by {@link #buildRequest}. If this field is
     * {@code null}, {@link #buildRequest} will create a new instance from its arguments.
     */
    MockLowLevelHttpRequest lowLevelHttpRequest;

    /**
     * The {@link MockLowLevelHttpResponse} that should be the result of the {@link
     * MockLowLevelHttpRequest} to be returned by {@link #buildRequest}. Note that this field is
     * ignored if the caller provides a {@link MockLowLevelHttpRequest} via {@link
     * #setLowLevelHttpRequest}.
     */
    MockLowLevelHttpResponse lowLevelHttpResponse;

    /**
     * Constructs a new {@link Builder}. Note that this constructor was {@code protected} in version
     * 1.17 and its predecessors, and was made {@code public} in version 1.18.
     */
    public Builder() {}

    /** Builds a new instance of {@link MockHttpTransport}. */
    public MockHttpTransport build() {
      return new MockHttpTransport(this);
    }

    /**
     * Returns the supported HTTP methods or {@code null} to specify that all methods are supported.
     */
    public final Set<String> getSupportedMethods() {
      return supportedMethods;
    }

    /**
     * Sets the supported HTTP methods or {@code null} to specify that all methods are supported.
     */
    public final Builder setSupportedMethods(Set<String> supportedMethods) {
      this.supportedMethods = supportedMethods;
      return this;
    }

    /**
     * Sets the {@link MockLowLevelHttpRequest} that will be returned by {@link #buildRequest}, if
     * non-{@code null}. If {@code null}, {@link #buildRequest} will create a new {@link
     * MockLowLevelHttpRequest} arguments.
     *
     * <p>Note that the user can set a low level HTTP Request only if a low level HTTP response has
     * not been set on this instance.
     *
     * @since 1.18
     */
    public final Builder setLowLevelHttpRequest(MockLowLevelHttpRequest lowLevelHttpRequest) {
      Preconditions.checkState(
          lowLevelHttpResponse == null,
          "Cannnot set a low level HTTP request when a low level HTTP response has been set.");
      this.lowLevelHttpRequest = lowLevelHttpRequest;
      return this;
    }

    /**
     * Returns the {@link MockLowLevelHttpRequest} that is associated with this {@link Builder}, or
     * {@code null} if no such instance exists.
     *
     * @since 1.18
     */
    public final MockLowLevelHttpRequest getLowLevelHttpRequest() {
      return lowLevelHttpRequest;
    }

    /**
     * Sets the {@link MockLowLevelHttpResponse} that will be the result when the {@link
     * MockLowLevelHttpRequest} returned by {@link #buildRequest} is executed. Note that the
     * response can be set only the caller has not provided a {@link MockLowLevelHttpRequest} via
     * {@link #setLowLevelHttpRequest}.
     *
     * @throws IllegalStateException if the caller has already set a {@link LowLevelHttpRequest} in
     *     this instance
     * @since 1.18
     */
    public final Builder setLowLevelHttpResponse(MockLowLevelHttpResponse lowLevelHttpResponse) {
      Preconditions.checkState(
          lowLevelHttpRequest == null,
          "Cannot set a low level HTTP response when a low level HTTP request has been set.");
      this.lowLevelHttpResponse = lowLevelHttpResponse;
      return this;
    }

    /**
     * Returns the {@link MockLowLevelHttpResponse} that is associated with this {@link Builder}, or
     * {@code null} if no such instance exists.
     *
     * @since 1.18
     */
    MockLowLevelHttpResponse getLowLevelHttpResponse() {
      return this.lowLevelHttpResponse;
    }
  }
}
