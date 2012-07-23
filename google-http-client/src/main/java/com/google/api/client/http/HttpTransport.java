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

import java.io.IOException;
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
 * <li>Starting with SDK 2.3, strongly recommended to use {@code
 * com.google.api.client.javanet.NetHttpTransport}. Their Apache HTTP Client implementation is not
 * as well maintained.</li>
 * <li>For SDK 2.2 and earlier, use {@code com.google.api.client.apache.ApacheHttpTransport}. {@code
 * com.google.api.client.javanet.NetHttpTransport} is not recommended due to some bugs in the
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
 * @since 1.0
 * @author Yaniv Inbar
 */
public abstract class HttpTransport {

  static final Logger LOGGER = Logger.getLogger(HttpTransport.class.getName());

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
   * <p>
   * Default implementation returns {@code false}.
   * </p>
   *
   * @since 1.3
   */
  public boolean supportsHead() {
    return false;
  }

  /**
   * Returns whether this HTTP transport implementation supports the {@code PATCH} request method.
   * <p>
   * Default implementation returns {@code false}.
   * </p>
   *
   * @since 1.3
   */
  public boolean supportsPatch() {
    return false;
  }

  /**
   * Builds a {@code DELETE} request.
   *
   * @param url URL
   * @throws IOException I/O exception
   * @since 1.3
   */
  protected abstract LowLevelHttpRequest buildDeleteRequest(String url) throws IOException;

  /**
   * Builds a {@code GET} request.
   *
   * @param url URL
   * @throws IOException I/O exception
   * @since 1.3
   */
  protected abstract LowLevelHttpRequest buildGetRequest(String url) throws IOException;

  /**
   * Builds a {@code HEAD} request. Won't be called if {@link #supportsHead()} returns {@code false}
   * .
   * <p>
   * Default implementation throws an {@link UnsupportedOperationException}.
   *
   * @param url URL
   * @throws IOException I/O exception
   * @since 1.3
   */
  protected LowLevelHttpRequest buildHeadRequest(String url) throws IOException {
    throw new UnsupportedOperationException();
  }

  /**
   * Builds a {@code PATCH} request. Won't be called if {@link #supportsPatch()} returns {@code
   * false}.
   * <p>
   * Default implementation throws an {@link UnsupportedOperationException}.
   *
   * @param url URL
   * @throws IOException I/O exception
   * @since 1.3
   */
  protected LowLevelHttpRequest buildPatchRequest(String url) throws IOException {
    throw new UnsupportedOperationException();
  }

  /**
   * Builds a {@code POST} request.
   *
   * @param url URL
   * @throws IOException I/O exception
   * @since 1.3
   */
  protected abstract LowLevelHttpRequest buildPostRequest(String url) throws IOException;

  /**
   * Builds a {@code PUT} request.
   *
   * @param url URL
   * @throws IOException I/O exception
   * @since 1.3
   */
  protected abstract LowLevelHttpRequest buildPutRequest(String url) throws IOException;

  /**
   * Default implementation does nothing, but subclasses may override to possibly release allocated
   * system resources or close connections.
   *
   * @throws IOException I/O exception
   * @since 1.4
   */
  public void shutdown() throws IOException {
  }
}
