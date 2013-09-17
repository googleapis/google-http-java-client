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
 * {@link Beta} <br/>
 * Mock for {@link HttpTransport}.
 *
 * <p>
 * Implementation is thread-safe. For maximum efficiency, applications should use a single
 * globally-shared instance of the HTTP transport.
 * </p>
 *
 * @author Yaniv Inbar
 * @since 1.3
 */
@Beta
public class MockHttpTransport extends HttpTransport {

  /** Supported HTTP methods or {@code null} to specify that all methods are supported. */
  private Set<String> supportedMethods;

  /**
   * The {@link MockLowLevelHttpRequest} to be returned by {@link #buildRequest}. If
   * this field is {@code null}, {@link #buildRequest} will create a new instance
   * from its arguments.
   * */
  private MockLowLevelHttpRequest lowLevelHttpRequest;

  public MockHttpTransport() {
  }

  /**
   * @param builder builder
   *
   * @since 1.14
   */
  protected MockHttpTransport(Builder builder) {
    supportedMethods = builder.supportedMethods;
    lowLevelHttpRequest = builder.lowLevelHttpRequest;
  }

  @Override
  public boolean supportsMethod(String method) throws IOException {
    return supportedMethods == null || supportedMethods.contains(method);
  }

  @Override
  public LowLevelHttpRequest buildRequest(String method, String url) throws IOException {
    Preconditions.checkArgument(supportsMethod(method), "HTTP method %s not supported", method);
    return lowLevelHttpRequest == null
        ? new MockLowLevelHttpRequest(url)
        : lowLevelHttpRequest;
  }

  /**
   * Returns the unmodifiable set of supported HTTP methods or {@code null} to specify that all
   * methods are supported.
   */
  public final Set<String> getSupportedMethods() {
    return supportedMethods == null ? null : Collections.unmodifiableSet(supportedMethods);
  }

  /**
   * Returns the {@link MockLowLevelHttpRequest} that is associated with this {@link Builder},
   * or {@code null} if no such instance exists.
   *
   * @since 1.18
   */
  public final MockLowLevelHttpRequest getLowLevelHttpRequest() {
    return lowLevelHttpRequest;
  }

  /**
   * Returns an instance of a new builder.
   *
   * @since 1.5
   */
  public static Builder builder() {
    return new Builder();
  }

  /**
   * {@link Beta} <br/>
   * Builder for {@link MockHttpTransport}.
   *
   * <p>
   * Implementation is not thread-safe.
   * </p>
   *
   * @since 1.5
   */
  @Beta
  public static class Builder {

    /** Supported HTTP methods or {@code null} to specify that all methods are supported. */
    Set<String> supportedMethods;

    /**
     * The {@link MockLowLevelHttpRequest} to be returned by {@link #buildRequest}. If
     * this field is {@code null}, {@link #buildRequest} will create a new instance
     * from its arguments.
     * */
    MockLowLevelHttpRequest lowLevelHttpRequest;

    protected Builder() {
    }

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
     * non-{@code null}. If {@code null}, {@link #buildRequest} will create a new
     * {@link MockLowLevelHttpRequest} arguments.
     *
     * @since 1.18
     */
    public final Builder setLowLevelHttpRequest(MockLowLevelHttpRequest lowLevelHttpRequest) {
      this.lowLevelHttpRequest = lowLevelHttpRequest;
      return this;
    }

    /**
     * Returns the {@link MockLowLevelHttpRequest} that is associated with this {@link Builder},
     * or {@code null} if no such instance exists.
     *
     * @since 1.18
     */
    public final MockLowLevelHttpRequest getLowLevelHttpRequest() {
      return lowLevelHttpRequest;
    }
  }
}
