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

import com.google.api.client.http.ByteArrayContent;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpMethod;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestFactory;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.UriTemplate;
import com.google.api.client.json.Json;
import com.google.api.client.json.JsonFactory;
import com.google.common.base.Preconditions;

import java.io.IOException;

/**
 * JSON HTTP Client.
 *
 * @since 1.6
 * @author Ravi Mistry
 */
public class JsonHttpClient {

  /** The request factory for connections to the server. */
  private final HttpRequestFactory requestFactory;

  /** The initializer to use when creating an {@link RemoteRequest} or {@code null} for none. */
  private final RemoteRequestInitializer remoteRequestInitializer;

  /**
   * The base URL of the service, for example {@code "https://www.googleapis.com/tasks/v1/"}. Must
   * be URL-encoded and must end with a "/". This is determined when the library is generated and
   * normally should not be changed.
   */
  private final String baseUrl;

  /**
   * The application name to be sent in the User-Agent header of each request or {@code null} for
   * none.
   */
  private final String applicationName;

  /** The JSON factory to use for creating JSON parsers and serializers. */
  private final JsonFactory jsonFactory;

  /**
   * The JSON parser to use for parsing requests or {@code null} for none if it is used before
   * initialized in {@link #getJsonHttpParser}.
   */
  private JsonHttpParser jsonHttpParser;

  /**
   * Returns the base URL of the service, for example
   * {@code "https://www.googleapis.com/tasks/v1/"}. Must be URL-encoded and must end with a "/".
   * This is determined when the library is generated and normally should not be changed.
   */
  public final String getBaseUrl() {
    return baseUrl;
  }

  /**
   * Returns the application name to be sent in the User-Agent header of each request or
   * {@code null} for none.
   */
  public final String getApplicationName() {
    return applicationName;
  }

  /** Returns the JSON Factory. */
  public final JsonFactory getJsonFactory() {
    return jsonFactory;
  }

  /** Returns the HTTP request factory. */
  public final HttpRequestFactory getRequestFactory() {
    return requestFactory;
  }

  /** Returns the Remote request initializer or {@code null} for none. */
  public final RemoteRequestInitializer getRemoteRequestInitializer() {
    return remoteRequestInitializer;
  }

  /**
   * Returns the JSON HTTP Parser. Initializes the parser once and then caches it for all subsequent
   * calls to this method.
   */
  public final JsonHttpParser getJsonHttpParser() {
    if (jsonHttpParser == null) {
      jsonHttpParser = createParser();
    }
    return jsonHttpParser;
  }

  /**
   * Creates a JSON parser. Subclasses may override if specific {@link JsonHttpParser}
   * implementations are required.
   */
  protected JsonHttpParser createParser() {
    return new JsonHttpParser(jsonFactory);
  }

  /**
   * Create a JSON serializer for a request object. Subclasses may override if specific
   * {@link JsonHttpContent} implementations are required.
   *
   * @param body A POJO that can be serialized into JSON
   */
  protected JsonHttpContent createSerializer(Object body) {
    return new JsonHttpContent(getJsonFactory(), body);
  }

  /**
   * Initializes a {@link RemoteRequest} using a {@link RemoteRequestInitializer}. Subclasses may
   * override if specific behavior is required.
   * Must be called before the remote request is executed, preferably right after the remote request
   * is instantiated. Sample usage:
   * <pre>
    public class Get extends RemoteRequest {
      ...
    }

    public Get get(String userId) throws IOException {
      Get result = new Get(userId);
      initialize(result);
      return result;
    }
   * </pre>
   *
   * @param remoteRequest Remote Request type
   */
  protected void initialize(RemoteRequest remoteRequest) throws IOException {
    if (remoteRequestInitializer != null) {
      remoteRequestInitializer.initialize(remoteRequest);
    }
  }

  /**
   * Construct the {@link JsonHttpClient}.
   *
   * @param transport The transport to use for requests
   * @param remoteRequestInitializer The initializer to use when creating an {@link RemoteRequest}
   *        or {@code null} for none
   * @param httpRequestInitializer The initializer to use when creating an {@link HttpRequest} or
   *        {@code null} for none
   * @param jsonFactory A factory for creating JSON parsers and serializers
   * @param baseUrl The base URL of the service. Must end with a "/"
   * @param applicationName The application name to be sent in the User-Agent header of requests or
   *        {@code null} for none
   */
  protected JsonHttpClient(
      HttpTransport transport,
      RemoteRequestInitializer remoteRequestInitializer,
      HttpRequestInitializer httpRequestInitializer,
      JsonFactory jsonFactory,
      String baseUrl,
      String applicationName) {
    this.remoteRequestInitializer = remoteRequestInitializer;
    this.baseUrl = Preconditions.checkNotNull(baseUrl);
    this.applicationName = applicationName;
    this.jsonFactory = Preconditions.checkNotNull(jsonFactory);
    Preconditions.checkNotNull(transport);
    this.requestFactory = httpRequestInitializer == null
        ? transport.createRequestFactory() : transport.createRequestFactory(httpRequestInitializer);
  }

  /**
   * Create an {@link HttpRequest} suitable for use against this service. Subclasses may
   * override if specific behavior is required.
   *
   * @param method HTTP Method type
   * @param uriTemplate URI template for the path relative to the base URL. Must not start with
   *        a "/"
   * @param remoteRequest Remote Request type
   * @return newly created {@link HttpRequest}
   */
  protected HttpRequest buildHttpRequest(
      HttpMethod method, String uriTemplate, RemoteRequest remoteRequest) throws IOException {
    GenericUrl url = new GenericUrl(
        UriTemplate.expand(baseUrl + uriTemplate, remoteRequest, true));
    HttpRequest httpRequest = requestFactory.buildRequest(method, url, null);
    httpRequest.addParser(getJsonHttpParser());
    if (applicationName != null) {
      httpRequest.getHeaders().setUserAgent(applicationName);
    }
    return httpRequest;
  }

  /**
   * Builds and executes a {@link HttpRequest}. Subclasses may override if specific behavior is
   * required.
   *
   * @param method HTTP Method type
   * @param uriTemplate URI template for the path relative to the base URL. Must not start with
   *        a "/"
   * @param body A POJO that can be serialized into JSON or {@code null} for none
   * @param remoteRequest Remote Request type
   * @return {@link HttpRequest} type
   * @throws IOException if the request fails
   */
  protected HttpResponse execute(HttpMethod method, String uriTemplate, Object body,
      RemoteRequest remoteRequest) throws IOException {
    HttpRequest request = buildHttpRequest(method, uriTemplate, remoteRequest);
    if (body != null) {
      request.setContent(createSerializer(body));
      request.setEnableGZipContent(true);
    } else if (
        method == HttpMethod.POST || method == HttpMethod.PUT || method == HttpMethod.PATCH) {
      // Some servers will fail to process a POST/PUT/PATCH unless the Content-Length header >= 1.
      request.setContent(ByteArrayContent.fromString(Json.CONTENT_TYPE, " "));
    }
    return request.execute();
  }

  /**
   * Returns an instance of a new builder.
   *
   * @param transport The transport to use for requests
   * @param jsonFactory A factory for creating JSON parsers and serializers
   * @param baseUrl The base URL of the service. Must end with a "/"
   */
  public static Builder builder(
      HttpTransport transport, JsonFactory jsonFactory, GenericUrl baseUrl) {
    return new Builder(transport, jsonFactory, baseUrl);
  }

  /**
   * Builder for {@link JsonHttpClient}.
   *
   * <p>
   * Implementation is not thread-safe.
   * </p>
   *
   * @since 1.6
   */
  public static class Builder {

    /** The transport to use for requests. */
    private final HttpTransport transport;

    /** The initializer to use when creating an {@link RemoteRequest} or {@code null} for none. */
    private RemoteRequestInitializer remoteRequestInitializer;

    /** The initializer to use when creating an {@link HttpRequest} or {@code null} for none. */
    private HttpRequestInitializer httpRequestInitializer;

    /** The JSON parser to user for parsing requests. */
    private final JsonFactory jsonFactory;

    /**
     * The base URL of the service, for example {@code "https://www.googleapis.com/tasks/v1/"}.
     * Must be URL-encoded and must end with a "/". This is determined when the library is
     * generated and normally should not be changed.
     */
    private final GenericUrl baseUrl;

    /**
     * The application name to be sent in the User-Agent header of each request or {@code null} for
     * none.
     */
    private String applicationName;

    /**
     * Returns an instance of a new builder.
     *
     * @param transport The transport to use for requests
     * @param jsonFactory A factory for creating JSON parsers and serializers
     * @param baseUrl The base URL of the service. Must end with a "/"
     */
    protected Builder(HttpTransport transport, JsonFactory jsonFactory, GenericUrl baseUrl) {
      this.transport = transport;
      this.jsonFactory = jsonFactory;
      this.baseUrl = baseUrl;
    }

    /** Builds a new instance of {@link JsonHttpClient}. */
    public JsonHttpClient build() {
      return new JsonHttpClient(
          transport,
          remoteRequestInitializer,
          httpRequestInitializer,
          jsonFactory,
          baseUrl.build(),
          applicationName);
    }

    /** Returns the JSON factory. */
    public final JsonFactory getJsonFactory() {
      return jsonFactory;
    }

    /** Returns the HTTP transport. */
    public final HttpTransport getTransport() {
      return transport;
    }

    /** Returns the base URL of the service. */
    public final GenericUrl getBaseUrl() {
      return baseUrl;
    }

    /** Sets the Remote request initializer. Subclasses should override by calling super. */
    public Builder setRemoteRequestInitializer(RemoteRequestInitializer remoteRequestInitializer) {
      this.remoteRequestInitializer = remoteRequestInitializer;
      return this;
    }

    /** Returns the Remote request initializer or {@code null} for none. */
    public RemoteRequestInitializer getRemoteRequestInitializer() {
      return remoteRequestInitializer;
    }

    /** Sets the HTTP request initializer. Subclasses should override by calling super. */
    public Builder setHttpRequestInitializer(HttpRequestInitializer httpRequestInitializer) {
      this.httpRequestInitializer = httpRequestInitializer;
      return this;
    }

    /** Returns the HTTP request initializer or {@code null} for none. */
    public final HttpRequestInitializer getHttpRequestInitializer() {
      return httpRequestInitializer;
    }

    /**
     * Sets the application name to be used in the UserAgent header of each request. Subclasses
     * should override by calling super.
     */
    public Builder setApplicationName(String applicationName) {
      this.applicationName = applicationName;
      return this;
   }

    /**
     * Returns the application name to be used in the UserAgent header of each request or
     * {@code null} for none.
     */
    public final String getApplicationName() {
      return applicationName;
    }
  }
}
