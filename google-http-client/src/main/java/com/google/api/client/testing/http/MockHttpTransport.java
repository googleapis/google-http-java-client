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
import com.google.api.client.util.Preconditions;

import java.io.IOException;
import java.util.Collections;
import java.util.Set;

/**
 * Mock for {@link HttpTransport}.
 *
 * <p>
 * Implementation is thread-safe. For maximum efficiency, applications should use a single
 * globally-shared instance of the HTTP transport.
 * </p>
 *
 * <p>
 * Upgrade warning: in prior version 1.11 this used {@link HttpMethod} to specify the set of
 * supported methods, but now it uses a {@link String} instead to allow for arbitrary methods to be
 * specified.
 * </p>
 *
 * @author Yaniv Inbar
 * @since 1.3
 */
@SuppressWarnings({"javadoc", "deprecation"})
public class MockHttpTransport extends HttpTransport {

  /** Supported HTTP methods or {@code null} to specify that all methods are supported. */
  private Set<String> supportedMethods;

  public MockHttpTransport() {
  }

  /**
   * @param builder builder
   *
   * @since 1.14
   */
  protected MockHttpTransport(Builder builder) {
    supportedMethods = builder.supportedMethods;
  }

  /**
   * @param supportedMethods supported HTTP methods or {@code null} to specify that all methods are
   *        supported
   *
   * @since 1.12
   * @deprecated (scheduled to be removed in 1.15) Use {@link #MockHttpTransport(Builder)}
   */
  @Deprecated
  protected MockHttpTransport(Set<String> supportedMethods) {
    this.supportedMethods = supportedMethods;
  }

  @Override
  public boolean supportsMethod(String method) throws IOException {
    return supportedMethods == null || supportedMethods.contains(method);
  }

  @Override
  protected LowLevelHttpRequest buildRequest(String method, String url) throws IOException {
    Preconditions.checkArgument(supportsMethod(method), "HTTP method %s not supported", method);
    return new MockLowLevelHttpRequest(url);
  }

  /**
   * Returns the unmodifiable set of supported HTTP methods or {@code null} to specify that all
   * methods are supported.
   */
  public final Set<String> getSupportedMethods() {
    return supportedMethods == null ? null : Collections.unmodifiableSet(supportedMethods);
  }

  @Deprecated
  @Override
  public LowLevelHttpRequest buildDeleteRequest(String url) throws IOException {
    return super.buildDeleteRequest(url);
  }

  @Deprecated
  @Override
  public LowLevelHttpRequest buildGetRequest(String url) throws IOException {
    return super.buildGetRequest(url);
  }

  @Deprecated
  @Override
  public LowLevelHttpRequest buildHeadRequest(String url) throws IOException {
    return super.buildHeadRequest(url);
  }

  @Deprecated
  @Override
  public LowLevelHttpRequest buildPatchRequest(String url) throws IOException {
    return super.buildPatchRequest(url);
  }

  @Deprecated
  @Override
  public LowLevelHttpRequest buildPostRequest(String url) throws IOException {
    return super.buildPostRequest(url);
  }

  @Deprecated
  @Override
  public LowLevelHttpRequest buildPutRequest(String url) throws IOException {
    return super.buildPutRequest(url);
  }

  @Deprecated
  @Override
  public boolean supportsHead() throws IOException {
    return super.supportsHead();
  }

  @Deprecated
  @Override
  public boolean supportsPatch() throws IOException {
    return super.supportsPatch();
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

    /** Supported HTTP methods or {@code null} to specify that all methods are supported. */
    Set<String> supportedMethods;

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
  }
}
