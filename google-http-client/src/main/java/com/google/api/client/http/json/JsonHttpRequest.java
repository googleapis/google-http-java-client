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

package com.google.api.client.http.json;

import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpHeaders;
import com.google.api.client.http.HttpMethod;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.UriTemplate;
import com.google.api.client.util.GenericData;
import com.google.common.base.Preconditions;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * JSON HTTP request to {@link JsonHttpClient}.
 *
 * <p>
 * Implementation is not thread-safe.
 * </p>
 *
 * @since 1.6
 * @author Ravi Mistry
 */
public class JsonHttpRequest extends GenericData {

  private final JsonHttpClient client;
  private final HttpMethod method;
  private final String uriTemplate;
  private final Object content;

  /** The HTTP headers used for the JSON HTTP request. */
  private HttpHeaders requestHeaders = new HttpHeaders();

  /** The HTTP headers of the last response or {@code null} for none. */
  private HttpHeaders lastResponseHeaders;

  /** Whether to enable GZip compression of HTTP content. */
  private boolean enableGZipContent = true;

  /**
   * Builds an instance of {@link JsonHttpRequest}.
   *
   * @param client The JSON HTTP client which handles this request
   * @param method HTTP Method type
   * @param uriTemplate URI template for the path relative to the base URL specified in JSON HTTP
   *        client. If it starts with a "/" the base path from the base URL will be stripped out.
   *        The URI template can also be a full URL. URI template expansion is done using {@link
   *        UriTemplate#expand(String, String, Object, boolean)}
   * @param content A POJO that can be serialized into JSON or {@code null} for none
   */
  public JsonHttpRequest(JsonHttpClient client, HttpMethod method, String uriTemplate,
      Object content) {
    this.client = Preconditions.checkNotNull(client);
    this.method = Preconditions.checkNotNull(method);
    this.uriTemplate = Preconditions.checkNotNull(uriTemplate);
    this.content = content;
  }

  /**
   * Sets whether to enable GZip compression of HTTP content. Subclasses should override by
   * calling super.
   *
   * <p>
   * By default it is {@code true}.
   * </p>
   *
   * @since 1.10
   */
  public JsonHttpRequest setEnableGZipContent(boolean enableGZipContent) {
    this.enableGZipContent = enableGZipContent;
    return this;
  }

  /**
   * Returns whether to enable GZip compression of HTTP content.
   *
   * @since 1.10
   */
  public final boolean getEnableGZipContent() {
    return enableGZipContent;
  }

  /** Returns the HTTP Method type. */
  public final HttpMethod getMethod() {
    return method;
  }

  /** Returns the URI template. */
  public final String getUriTemplate() {
    return uriTemplate;
  }

  /** Returns a POJO that can be serialized into JSON or {@code null} for none. */
  public final Object getJsonContent() {
    return content;
  }

  /** Returns the JSON HTTP client which handles this request. */
  public final JsonHttpClient getClient() {
    return client;
  }

  /**
   * Sets the HTTP headers used for the JSON HTTP request. Subclasses may override by calling
   * super.
   *
   * <p>
   * These headers are set on the request after {@link #buildHttpRequest} is called, this means that
   * {@link HttpRequestInitializer#initialize} is called first.
   * </p>
   *
   * @since 1.9
   */
  public JsonHttpRequest setRequestHeaders(HttpHeaders headers) {
    this.requestHeaders = headers;
    return this;
  }

  /**
   * Gets the HTTP headers used for the JSON HTTP request.
   *
   * @since 1.9
   */
  public final HttpHeaders getRequestHeaders() {
    return requestHeaders;
  }

  /**
   * Gets the HTTP headers of the last response or {@code null} for none.
   *
   * @since 1.9
   */
  public final HttpHeaders getLastResponseHeaders() {
    return lastResponseHeaders;
  }

  /**
   * Creates a new instance of {@link GenericUrl} suitable for use against this service.
   *
   * @return newly created {@link GenericUrl}
   */
  @SuppressWarnings("deprecation")
  public final GenericUrl buildHttpRequestUrl() {
    String baseUrl;
    if (getClient().isBaseUrlUsed()) {
      baseUrl = getClient().getBaseUrl();
    } else {
      baseUrl = getClient().getRootUrl() + getClient().getServicePath();
    }
    return new GenericUrl(UriTemplate.expand(baseUrl, uriTemplate, this, true));
  }

  /**
   * Create an {@link HttpRequest} suitable for use against this service. Subclasses may override if
   * specific behavior is required.
   *
   * @return newly created {@link HttpRequest}
   */
  public HttpRequest buildHttpRequest() throws IOException {
    HttpRequest request = client.buildHttpRequest(method, buildHttpRequestUrl(), content);
    // Add specified headers (if any) to the headers in the request.
    request.getHeaders().putAll(getRequestHeaders());
    return request;
  }

  /**
   * Sends the request to the server and returns the raw {@link HttpResponse}. Subclasses may
   * override if specific behavior is required.
   *
   * <p>
   * Callers are responsible for disconnecting the HTTP response by calling
   * {@link HttpResponse#disconnect}. Example usage:
   * </p>
   *
   * <pre>
     HttpResponse response = jsonHttpRequest.executeUnparsed();
     try {
       // process response..
     } finally {
       response.disconnect();
     }
   * </pre>
   *
   * @return the {@link HttpResponse}
   * @throws IOException if the request fails
   */
  public HttpResponse executeUnparsed() throws IOException {
    HttpRequest request = buildHttpRequest();
    request.setEnableGZipContent(enableGZipContent);
    HttpResponse response = client.executeUnparsed(request);
    lastResponseHeaders = response.getHeaders();
    return response;
  }

  /**
   * Sends the request to the server and returns the content input stream of {@link HttpResponse}.
   * Subclasses may override if specific behavior is required.
   *
   * <p>
   * Callers are responsible for closing the input stream after it is processed. Example sample:
   * </p>
   *
   * <pre>
     InputStream is = request.executeAsInputStream();
     try {
       // Process input stream..
     } finally {
       is.close();
     }
   * </pre>
   *
   * @return input stream of the response content
   * @throws IOException if the request fails
   * @since 1.8
   */
  public InputStream executeAsInputStream() throws IOException {
    HttpResponse response = executeUnparsed();
    return response.getContent();
  }

  /**
   * Sends the request to the server and writes the content input stream of {@link HttpResponse}
   * into the given destination output stream.
   *
   * <p>
   * This method closes the content of the HTTP response from {@link HttpResponse#getContent()}.
   * </p>
   *
   * @param outputStream destination output stream
   * @throws IOException I/O exception
   * @since 1.9
   */
  public void download(OutputStream outputStream) throws IOException {
    HttpResponse response = executeUnparsed();
    response.download(outputStream);
  }
}
