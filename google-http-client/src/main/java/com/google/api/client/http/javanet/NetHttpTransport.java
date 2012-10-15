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

package com.google.api.client.http.javanet;

import com.google.api.client.http.HttpMethods;
import com.google.api.client.http.HttpTransport;
import com.google.common.base.Preconditions;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.Arrays;

/**
 * Thread-safe HTTP low-level transport based on the {@code java.net} package.
 *
 * <p>
 * Users should consider modifying the keep alive property on {@link NetHttpTransport} to control
 * whether the socket should be returned to a pool of connected sockets. More information is
 * available <a
 * href='http://docs.oracle.com/javase/7/docs/technotes/guides/net/http-keepalive.html'>here</a>.
 * </p>
 *
 * <p>
 * Implementation is thread-safe. For maximum efficiency, applications should use a single
 * globally-shared instance of the HTTP transport.
 * </p>
 *
 * @since 1.0
 * @author Yaniv Inbar
 */
public final class NetHttpTransport extends HttpTransport {

  /**
   * All valid request methods as specified in {@link HttpURLConnection#setRequestMethod}, sorted in
   * ascending alphabetical order.
   */
  private static final String[] SUPPORTED_METHODS = {HttpMethods.DELETE,
      HttpMethods.GET,
      HttpMethods.HEAD,
      HttpMethods.OPTIONS,
      HttpMethods.POST,
      HttpMethods.PUT,
      HttpMethods.TRACE};
  static {
    Arrays.sort(SUPPORTED_METHODS);
  }

  @Override
  public boolean supportsMethod(String method) {
    return Arrays.binarySearch(SUPPORTED_METHODS, method) >= 0;
  }

  @Override
  protected NetHttpRequest buildRequest(String method, String url) throws IOException {
    Preconditions.checkArgument(supportsMethod(method), "HTTP method %s not supported", method);
    return new NetHttpRequest(method, url);
  }

  @Deprecated
  @Override
  public boolean supportsHead() {
    return true;
  }

  @Deprecated
  @Override
  public NetHttpRequest buildDeleteRequest(String url) throws IOException {
    return buildRequest("DELETE", url);
  }

  @Deprecated
  @Override
  public NetHttpRequest buildGetRequest(String url) throws IOException {
    return buildRequest("GET", url);
  }

  @Deprecated
  @Override
  public NetHttpRequest buildHeadRequest(String url) throws IOException {
    return buildRequest("HEAD", url);
  }

  @Deprecated
  @Override
  public NetHttpRequest buildPostRequest(String url) throws IOException {
    return buildRequest("POST", url);
  }

  @Deprecated
  @Override
  public NetHttpRequest buildPutRequest(String url) throws IOException {
    return buildRequest("PUT", url);
  }
}
