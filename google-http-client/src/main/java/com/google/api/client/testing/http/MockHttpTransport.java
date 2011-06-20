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

import com.google.api.client.http.HttpMethod;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.LowLevelHttpRequest;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;

/**
 * Mock for {@link HttpTransport}.
 *
 * <p>
 * Implementation is thread-safe, as long as {@link #supportedOptionalMethods} is not modified
 * directly (which is deprecated usage). For maximum efficiency, applications should use a single
 * globally-shared instance of the HTTP transport.
 * </p>
 *
 * @author Yaniv Inbar
 * @since 1.3
 */
public class MockHttpTransport extends HttpTransport {

  /**
   * Default supported optional methods.
   *
   * @since 1.5
   */
  public static final Set<HttpMethod> DEFAULT_SUPPORTED_OPTIONAL_METHODS =
      Collections.unmodifiableSet(
          new HashSet<HttpMethod>(Arrays.asList(HttpMethod.HEAD, HttpMethod.PATCH)));

  /**
   * Set of supported optional methods or {@link HttpMethod#HEAD} and {@link HttpMethod#PATCH} by
   * default.
   *
   * @deprecated (scheduled to be made private in 1.6) Use {@link #getSupportedOptionalMethods} or
   *             {@link Builder#setSupportedOptionalMethods}
   */
  @Deprecated
  public EnumSet<HttpMethod> supportedOptionalMethods =
      EnumSet.of(HttpMethod.HEAD, HttpMethod.PATCH);

  public MockHttpTransport() {
  }

  /**
   * @param supportedOptionalMethods set of supported optional methods
   * @since 1.5
   */
  protected MockHttpTransport(Set<HttpMethod> supportedOptionalMethods) {
    this.supportedOptionalMethods =
        supportedOptionalMethods.isEmpty() ? EnumSet.noneOf(HttpMethod.class) : EnumSet.copyOf(
            supportedOptionalMethods);
  }

  /**
   * Returns the set of supported optional methods.
   *
   * <p>
   * Default value is {@link #DEFAULT_SUPPORTED_OPTIONAL_METHODS}.
   * </p>
   *
   * @since 1.5
   */
  public final Set<HttpMethod> getSupportedOptionalMethods() {
    return supportedOptionalMethods;
  }

  /**
   * @param supportedOptionalMethods the supportedOptionalMethods to set
   */
  public void setSupportedOptionalMethods(EnumSet<HttpMethod> supportedOptionalMethods) {
    this.supportedOptionalMethods = supportedOptionalMethods;
  }

  @Override
  public LowLevelHttpRequest buildDeleteRequest(String url) throws IOException {
    return new MockLowLevelHttpRequest(url);
  }

  @Override
  public LowLevelHttpRequest buildGetRequest(String url) throws IOException {
    return new MockLowLevelHttpRequest(url);
  }

  @Override
  public LowLevelHttpRequest buildHeadRequest(String url) throws IOException {
    if (!supportsHead()) {
      return super.buildHeadRequest(url);
    }
    return new MockLowLevelHttpRequest(url);
  }

  @Override
  public LowLevelHttpRequest buildPatchRequest(String url) throws IOException {
    if (!supportsPatch()) {
      return super.buildPatchRequest(url);
    }
    return new MockLowLevelHttpRequest(url);
  }

  @Override
  public LowLevelHttpRequest buildPostRequest(String url) throws IOException {
    return new MockLowLevelHttpRequest(url);
  }

  @Override
  public LowLevelHttpRequest buildPutRequest(String url) throws IOException {
    return new MockLowLevelHttpRequest(url);
  }

  @Override
  public boolean supportsHead() {
    return supportedOptionalMethods.contains(HttpMethod.HEAD);
  }

  @Override
  public boolean supportsPatch() {
    return supportedOptionalMethods.contains(HttpMethod.PATCH);
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
   * Builder for {@link MockHttpTransport}.
   *
   * <p>
   * Implementation is not thread-safe.
   * </p>
   *
   * @since 1.5
   */
  public static class Builder {

    private Set<HttpMethod> supportedOptionalMethods = DEFAULT_SUPPORTED_OPTIONAL_METHODS;

    protected Builder() {
    }

    /** Builds a new instance of {@link MockHttpTransport}. */
    public MockHttpTransport build() {
      return new MockHttpTransport(supportedOptionalMethods);
    }

    /**
     * Returns the set of supported optional methods.
     *
     * <p>
     * Default value is {@link #DEFAULT_SUPPORTED_OPTIONAL_METHODS}.
     * </p>
     */
    public final Set<HttpMethod> getSupportedOptionalMethods() {
      return supportedOptionalMethods;
    }

    /**
     * Sets the set of supported optional methods.
     *
     * <p>
     * Default value is {@link #DEFAULT_SUPPORTED_OPTIONAL_METHODS}.
     * </p>
     */
    public Builder setSupportedOptionalMethods(Set<HttpMethod> supportedOptionalMethods) {
      this.supportedOptionalMethods = supportedOptionalMethods;
      return this;
    }
  }
}
