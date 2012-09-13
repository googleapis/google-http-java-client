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

import java.util.Arrays;
import java.util.logging.Logger;

/**
 * Thread-safe abstract HTTP transport.
 *
 * <p>
 * Implementation is thread-safe, and sub-classes must be thread-safe. For maximum efficiency,
 * applications should use a single globally-shared instance of the HTTP transport.
 * </p>
 *
 * <p>
 * The recommended concrete implementation HTTP transport library to use depends on what environment
 * you are running in:
 * </p>
 * <ul>
 * <li>Google App Engine: use {@code com.google.api.client.appengine.UrlFetchTransport}.
 * <ul>
 * <li>{@code com.google.api.client.apache.ApacheHttpTransport} doesn't work on App Engine because
 * the Apache HTTP Client opens its own sockets (though in theory there are ways to hack it to work
 * on App Engine that might work).</li>
 * <li>{@code com.google.api.client.javanet.NetHttpTransport} is discouraged due to a bug in the App
 * Engine SDK itself in how it parses HTTP headers in the response.</li>
 * </ul>
 * </li>
 * <li>Android:
 * <ul>
 * <li>Starting with SDK 2.3, strongly recommended to use
 * {@code com.google.api.client.javanet.NetHttpTransport}. Their Apache HTTP Client implementation
 * is not as well maintained.</li>
 * <li>For SDK 2.2 and earlier, use {@code com.google.api.client.apache.ApacheHttpTransport}.
 * {@code com.google.api.client.javanet.NetHttpTransport} is not recommended due to some bugs in the
 * Android SDK implementation of HttpURLConnection.</li>
 * </ul>
 * </li>
 * <li>Other Java environments
 * <ul>
 * <li>{@code com.google.api.client.javanet.NetHttpTransport} is based on the HttpURLConnection
 * built into the Java SDK, so it is normally the preferred choice.</li>
 * <li>{@code com.google.api.client.apache.ApacheHttpTransport} is a good choice for users of the
 * Apache HTTP Client, especially if you need some of the configuration options available in that
 * library.</li>
 * </ul>
 * </li>
 * </ul>
 *
 * <p>
 * Some HTTP transports do not support all HTTP methods. Use {@link #supportsMethod(String)} to
 * check if a certain HTTP method is supported. Calling {@link #buildRequest()} on a method that is
 * not supported will result in an {@link IllegalArgumentException}.
 * </p>
 *
 * <p>
 * Subclasses should override {@link #supportsMethod(String)} and
 * {@link #buildRequest(String, String)} to build requests and specify which HTTP methods are
 * supported.
 * </p>
 *
 * @since 1.0
 * @author Yaniv Inbar
 */
public abstract class HttpTransport {

  static final Logger LOGGER = Logger.getLogger(HttpTransport.class.getName());

  /**
   * All valid request methods as specified in {@link #supportsMethod(String)}, sorted in ascending
   * alphabetical order.
   */
  private static final String[] SUPPORTED_METHODS =
      {HttpMethods.DELETE, HttpMethods.GET, HttpMethods.POST, HttpMethods.PUT};
  static {
    Arrays.sort(SUPPORTED_METHODS);
  }

  /**
   * Returns a new instance of an HTTP request factory based on this HTTP transport.
   *
   * @return new instance of an HTTP request factory
   * @since 1.4
   */
  public final HttpRequestFactory createRequestFactory() {
    return createRequestFactory(null);
  }

  /**
   * Returns a new instance of an HTTP request factory based on this HTTP transport with the given
   * HTTP request initializer.
   *
   * @param initializer HTTP request initializer or {@code null} for none
   * @return new instance of an HTTP request factory
   * @since 1.4
   */
  public final HttpRequestFactory createRequestFactory(HttpRequestInitializer initializer) {
    return new HttpRequestFactory(this, initializer);
  }

  /**
   * Builds a request without specifying the HTTP method.
   *
   * @return new HTTP request
   */
  HttpRequest buildRequest() {
    return new HttpRequest(this, null);
  }

  /**
   * Returns whether this HTTP transport implementation supports the {@code HEAD} request method.
   *
   * <p>
   * Default implementation calls {@link #supportsMethod}.
   * </p>
   *
   * @since 1.3
   * @deprecated (scheduled to be removed in 1.13) Use {@link #supportsMethod(String)} instead
   */
  @Deprecated
  public boolean supportsHead() {
    return supportsMethod("HEAD");
  }

  /**
   * Returns whether this HTTP transport implementation supports the {@code PATCH} request method.
   *
   * <p>
   * Default implementation calls {@link #supportsMethod}.
   * </p>
   *
   * @since 1.3
   * @deprecated (scheduled to be removed in 1.13) Use {@link #supportsMethod(String)} instead
   */
  @Deprecated
  public boolean supportsPatch() {
    return supportsMethod("PATCH");
  }

  /**
   * Returns whether a specified HTTP method is supported by this transport.
   *
   * <p>
   * Default implementation returns true if and only if the request method is {@code "DELETE"},
   * {@code "GET"}, {@code "POST"}, or {@code "PUT"}. Subclasses should override.
   * </p>
   *
   * @param method HTTP method
   * @since 1.12
   */
  public boolean supportsMethod(String method) {
    return Arrays.binarySearch(SUPPORTED_METHODS, method) >= 0;
  }

  /**
   * Builds a low level HTTP request for the given HTTP method.
   *
   * <p>
   * Default implementation throws an {@link IllegalArgumentException}. Subclasses must override,
   * though they should call the super implementation for an unsupported HTTP method.
   * </p>
   *
   * @param method HTTP method
   * @param url URL
   * @return new low level HTTP request
   * @throws IllegalArgumentException if HTTP method is not supported
   * @since 1.12
   */
  protected LowLevelHttpRequest buildRequest(String method, String url) throws Exception {
    throw new IllegalArgumentException("HTTP transport doesn't support " + method);
  }

  /**
   * Builds a {@code DELETE} request.
   *
   * <p>
   * Default implementation calls {@link #buildRequest}.
   * </p>
   *
   * <p>
   * Upgrade warning: this method now throws an {@link Exception}. In prior version 1.11 it threw an
   * {@link java.io.IOException}.
   * </p>
   *
   * @param url URL
   * @since 1.3
   * @deprecated (scheduled to be removed in 1.13) Use {@link #buildRequest(String, String)} instead
   */
  @Deprecated
  protected LowLevelHttpRequest buildDeleteRequest(String url) throws Exception {
    return buildRequest("DELETE", url);
  }

  /**
   * Builds a {@code GET} request.
   *
   * <p>
   * Default implementation calls {@link #buildRequest}.
   * </p>
   *
   * <p>
   * Upgrade warning: this method now throws an {@link Exception}. In prior version 1.11 it threw an
   * {@link java.io.IOException}.
   * </p>
   *
   * @param url URL
   * @since 1.3
   * @deprecated (scheduled to be removed in 1.13) Use {@link #buildRequest(String, String)} instead
   */
  @Deprecated
  protected LowLevelHttpRequest buildGetRequest(String url) throws Exception {
    return buildRequest("GET", url);
  }

  /**
   * Builds a {@code HEAD} request.
   *
   * <p>
   * Won't be called if {@link #supportsHead()} returns {@code false}.
   * </p>
   *
   * <p>
   * Default implementation calls {@link #buildRequest}.
   * </p>
   *
   * <p>
   * Upgrade warning: in prior version it threw an {@link UnsupportedOperationException} by
   * default}.
   * </p>
   *
   * <p>
   * Upgrade warning: this method now throws an {@link Exception}. In prior version 1.11 it threw an
   * {@link java.io.IOException}.
   * </p>
   *
   * @param url URL
   * @since 1.3
   * @deprecated (scheduled to be removed in 1.13) Use {@link #buildRequest(String, String)} instead
   */
  @Deprecated
  protected LowLevelHttpRequest buildHeadRequest(String url) throws Exception {
    return buildRequest("HEAD", url);
  }

  /**
   * Builds a {@code PATCH} request.
   *
   * <p>
   * Won't be called if {@link #supportsPatch()} returns {@code false}.
   * </p>
   *
   * <p>
   * Default implementation calls {@link #buildRequest}.
   * </p>
   *
   * <p>
   * Upgrade warning: in prior version it threw an {@link UnsupportedOperationException} by
   * default}.
   * </p>
   *
   * <p>
   * Upgrade warning: this method now throws an {@link Exception}. In prior version 1.11 it threw an
   * {@link java.io.IOException}.
   * </p>
   *
   * @param url URL
   * @since 1.3
   * @deprecated (scheduled to be removed in 1.13) Use {@link #buildRequest(String, String)} instead
   */
  @Deprecated
  protected LowLevelHttpRequest buildPatchRequest(String url) throws Exception {
    return buildRequest("PATCH", url);
  }

  /**
   * Builds a {@code POST} request.
   *
   * <p>
   * Default implementation calls {@link #buildRequest}.
   * </p>
   *
   * <p>
   * Upgrade warning: this method now throws an {@link Exception}. In prior version 1.11 it threw an
   * {@link java.io.IOException}.
   * </p>
   *
   * @param url URL
   * @since 1.3
   * @deprecated (scheduled to be removed in 1.13) Use {@link #buildRequest(String, String)} instead
   */
  @Deprecated
  protected LowLevelHttpRequest buildPostRequest(String url) throws Exception {
    return buildRequest("POST", url);
  }

  /**
   * Builds a {@code PUT} request.
   *
   * <p>
   * Default implementation calls {@link #buildRequest}.
   * </p>
   *
   * <p>
   * Upgrade warning: this method now throws an {@link Exception}. In prior version 1.11 it threw an
   * {@link java.io.IOException}.
   * </p>
   *
   * @param url URL
   * @since 1.3
   * @deprecated (scheduled to be removed in 1.13) Use {@link #buildRequest(String, String)} instead
   */
  @Deprecated
  protected LowLevelHttpRequest buildPutRequest(String url) throws Exception {
    return buildRequest("PUT", url);
  }

  /**
   * Default implementation does nothing, but subclasses may override to possibly release allocated
   * system resources or close connections.
   *
   * <p>
   * Upgrade warning: this method now throws an {@link Exception}. In prior version 1.11 it threw an
   * {@link java.io.IOException}.
   * </p>
   *
   * @since 1.4
   */
  public void shutdown() throws Exception {
  }
}
