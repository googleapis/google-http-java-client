/*
 * Copyright (c) 2011 Google Inc.
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
 * Thread-safe light-weight HTTP request factory layer on top of the HTTP transport that has an
 * optional {@link HttpRequestInitializer HTTP request initializer} for initializing requests.
 *
 * <p>
 * For example, to use a particular authorization header across all requests, use:
 * </p>
 *
 * <pre>
  public static HttpRequestFactory createRequestFactory(HttpTransport transport) {
    return transport.createRequestFactory(new HttpRequestInitializer() {
      public void initialize(HttpRequest request) throws Exception {
        request.getHeaders().setAuthorization("...");
      }
    });
  }
 * </pre>
 *
 * @since 1.4
 * @author Yaniv Inbar
 */
public final class HttpRequestFactory {

  /** HTTP transport. */
  private final HttpTransport transport;

  /** HTTP request initializer or {@code null} for none. */
  private final HttpRequestInitializer initializer;

  /**
   * @param transport HTTP transport
   * @param initializer HTTP request initializer or {@code null} for none
   */
  HttpRequestFactory(HttpTransport transport, HttpRequestInitializer initializer) {
    this.transport = transport;
    this.initializer = initializer;
  }

  /**
   * Returns the HTTP transport.
   *
   * @since 1.5
   */
  public HttpTransport getTransport() {
    return transport;
  }

  /**
   * Returns the HTTP request initializer or {@code null} for none.
   *
   * <p>
   * This initializer is invoked before setting its method, URL, or content.
   * </p>
   *
   * @since 1.5
   */
  public HttpRequestInitializer getInitializer() {
    return initializer;
  }

  /**
   * Builds a request for the given HTTP method, URL, and content.
   *
   * <p>
   * Upgrade warning: this method now throws an {@link Exception}.  In prior version 1.10 it threw
   * an {@link java.io.IOException}.
   * </p>
   *
   * @param method HTTP request method
   * @param url HTTP request URL or {@code null} for none
   * @param content HTTP request content or {@code null} for none
   * @return new HTTP request
   */
  public HttpRequest buildRequest(HttpMethod method, GenericUrl url, HttpContent content)
      throws Exception {
    HttpRequest request = transport.buildRequest();
    if (initializer != null) {
      initializer.initialize(request);
    }
    request.setMethod(method);
    if (url != null) {
      request.setUrl(url);
    }
    if (content != null) {
      request.setContent(content);
    }
    return request;
  }

  /**
   * Builds a {@code DELETE} request for the given URL.
   *
   * <p>
   * Upgrade warning: this method now throws an {@link Exception}.  In prior version 1.10 it threw
   * an {@link java.io.IOException}.
   * </p>
   *
   * @param url HTTP request URL or {@code null} for none
   * @return new HTTP request
   */
  public HttpRequest buildDeleteRequest(GenericUrl url) throws Exception {
    return buildRequest(HttpMethod.DELETE, url, null);
  }

  /**
   * Builds a {@code GET} request for the given URL.
   *
   * <p>
   * Upgrade warning: this method now throws an {@link Exception}.  In prior version 1.10 it threw
   * an {@link java.io.IOException}.
   * </p>
   *
   * @param url HTTP request URL or {@code null} for none
   * @return new HTTP request
   */
  public HttpRequest buildGetRequest(GenericUrl url) throws Exception {
    return buildRequest(HttpMethod.GET, url, null);
  }

  /**
   * Builds a {@code POST} request for the given URL and content.
   *
   * <p>
   * Upgrade warning: this method now throws an {@link Exception}.  In prior version 1.10 it threw
   * an {@link java.io.IOException}.
   * </p>
   *
   * @param url HTTP request URL or {@code null} for none
   * @param content HTTP request content or {@code null} for none
   * @return new HTTP request
   */
  public HttpRequest buildPostRequest(GenericUrl url, HttpContent content) throws Exception {
    return buildRequest(HttpMethod.POST, url, content);
  }

  /**
   * Builds a {@code PUT} request for the given URL and content.
   *
   * <p>
   * Upgrade warning: this method now throws an {@link Exception}.  In prior version 1.10 it threw
   * an {@link java.io.IOException}.
   * </p>
   *
   * @param url HTTP request URL or {@code null} for none
   * @param content HTTP request content or {@code null} for none
   * @return new HTTP request
   */
  public HttpRequest buildPutRequest(GenericUrl url, HttpContent content) throws Exception {
    return buildRequest(HttpMethod.PUT, url, content);
  }

  /**
   * Builds a {@code PATCH} request for the given URL and content.
   *
   * <p>
   * Upgrade warning: this method now throws an {@link Exception}.  In prior version 1.10 it threw
   * an {@link java.io.IOException}.
   * </p>
   *
   * @param url HTTP request URL or {@code null} for none
   * @param content HTTP request content or {@code null} for none
   * @return new HTTP request
   */
  public HttpRequest buildPatchRequest(GenericUrl url, HttpContent content) throws Exception {
    return buildRequest(HttpMethod.PATCH, url, content);
  }


  /**
   * Builds a {@code HEAD} request for the given URL.
   *
   * <p>
   * Upgrade warning: this method now throws an {@link Exception}.  In prior version 1.10 it threw
   * an {@link java.io.IOException}.
   * </p>
   *
   * @param url HTTP request URL or {@code null} for none
   * @return new HTTP request
   */
  public HttpRequest buildHeadRequest(GenericUrl url) throws Exception {
    return buildRequest(HttpMethod.HEAD, url, null);
  }
}
