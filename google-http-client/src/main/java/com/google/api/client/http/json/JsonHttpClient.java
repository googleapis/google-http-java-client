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

  /** The initializer to use when creating an {@link JsonHttpRequest} or {@code null} for none. */
  private final JsonHttpRequestInitializer jsonHttpRequestInitializer;

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
   * Returns the base URL of the service, for example {@code "https://www.googleapis.com/tasks/v1/"}
   * . Must be URL-encoded and must end with a "/". This is determined when the library is generated
   * and normally should not be changed.
   *
   * <p>
   * Overriding this method is not supported, except for the Google generated libraries for
   * backwards-compatibility reasons. This method will be made final in the 1.7 release.
   * </p>
   */
  public String getBaseUrl() {
    return baseUrl;
  }

  /**
   * Returns the application name to be sent in the User-Agent header of each request or {@code
   * null} for none.
   *
   * <p>
   * Overriding this method is not supported, except for the Google generated libraries for
   * backwards-compatibility reasons. This method will be made final in the 1.7 release.
   * </p>
   */
  public String getApplicationName() {
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

  /** Returns the JSON HTTP request initializer or {@code null} for none. */
  public final JsonHttpRequestInitializer getJsonHttpRequestInitializer() {
    return jsonHttpRequestInitializer;
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
   * Initializes a {@link JsonHttpRequest} using a {@link JsonHttpRequestInitializer}. Subclasses
   * may override if specific behavior is required.
   *
   * <p>
   * Must be called before the JSON HTTP request is executed, preferably right after the request is
   * instantiated. Sample usage:
   * </p>
   *
   * <pre>
    public class Get extends JsonHttpRequest {
      ...
    }

    public Get get(String userId) throws IOException {
      Get result = new Get(userId);
      initialize(result);
      return result;
    }
   * </pre>
   *
   * @param jsonHttpRequest JSON HTTP Request type
   */
  protected void initialize(JsonHttpRequest jsonHttpRequest) throws IOException {
    if (getJsonHttpRequestInitializer() != null) {
      getJsonHttpRequestInitializer().initialize(jsonHttpRequest);
    }
  }

  /**
   * Constructor with required parameters.
   *
   * <p>
   * Use {@link #builder} if you need to specify any of the optional parameters.
   * </p>
   *
   * @param transport The transport to use for requests
   * @param jsonFactory A factory for creating JSON parsers and serializers
   * @param baseUrl The base URL of the service. Must end with a "/"
   */
  public JsonHttpClient(HttpTransport transport, JsonFactory jsonFactory, String baseUrl) {
    this(transport, null, null, jsonFactory, baseUrl, null);
  }

  /**
   * Construct the {@link JsonHttpClient}.
   *
   * @param transport The transport to use for requests
   * @param jsonHttpRequestInitializer The initializer to use when creating an
   *        {@link JsonHttpRequest} or {@code null} for none
   * @param httpRequestInitializer The initializer to use when creating an {@link HttpRequest} or
   *        {@code null} for none
   * @param jsonFactory A factory for creating JSON parsers and serializers
   * @param baseUrl The base URL of the service. Must end with a "/"
   * @param applicationName The application name to be sent in the User-Agent header of requests or
   *        {@code null} for none
   */
  protected JsonHttpClient(HttpTransport transport,
      JsonHttpRequestInitializer jsonHttpRequestInitializer,
      HttpRequestInitializer httpRequestInitializer,
      JsonFactory jsonFactory,
      String baseUrl,
      String applicationName) {
    this.jsonHttpRequestInitializer = jsonHttpRequestInitializer;
    this.baseUrl = Preconditions.checkNotNull(baseUrl);
    this.applicationName = applicationName;
    this.jsonFactory = Preconditions.checkNotNull(jsonFactory);
    Preconditions.checkNotNull(transport);
    this.requestFactory = httpRequestInitializer == null
        ? transport.createRequestFactory() : transport.createRequestFactory(httpRequestInitializer);
  }

  /**
   * Create an {@link HttpRequest} suitable for use against this service. Subclasses may override if
   * specific behavior is required.
   *
   * @param method HTTP Method type
   * @param uriTemplate URI template for the path relative to the base URL. Must not start with a
   *        "/"
   * @param jsonHttpRequest JSON HTTP Request type
   * @return newly created {@link HttpRequest}
   */
  protected HttpRequest buildHttpRequest(
      HttpMethod method, String uriTemplate, JsonHttpRequest jsonHttpRequest) throws IOException {
    GenericUrl url =
        new GenericUrl(UriTemplate.expand(getBaseUrl() + uriTemplate, jsonHttpRequest, true));
    HttpRequest httpRequest = requestFactory.buildRequest(method, url, null);
    httpRequest.addParser(getJsonHttpParser());
    if (getApplicationName() != null) {
      httpRequest.getHeaders().setUserAgent(getApplicationName());
    }
    return httpRequest;
  }

  /**
   * Builds and executes a {@link HttpRequest}. Subclasses may override if specific behavior is
   * required.
   *
   * @param method HTTP Method type
   * @param uriTemplate URI template for the path relative to the base URL. Must not start with a
   *        "/"
   * @param body A POJO that can be serialized into JSON or {@code null} for none
   * @param jsonHttpRequest JSON HTTP Request type
   * @return {@link HttpRequest} type
   * @throws IOException if the request fails
   */
  protected HttpResponse execute(
      HttpMethod method, String uriTemplate, Object body, JsonHttpRequest jsonHttpRequest)
      throws IOException {
    HttpRequest request = buildHttpRequest(method, uriTemplate, jsonHttpRequest);
    if (body != null) {
      request.setContent(createSerializer(body));
      request.setEnableGZipContent(true);
    } else if (method == HttpMethod.POST || method == HttpMethod.PUT
        || method == HttpMethod.PATCH) {
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

    /** The initializer to use when creating an {@link JsonHttpRequest} or {@code null} for none. */
    private JsonHttpRequestInitializer jsonHttpRequestInitializer;

    /** The initializer to use when creating an {@link HttpRequest} or {@code null} for none. */
    private HttpRequestInitializer httpRequestInitializer;

    /** The JSON parser to user for parsing requests. */
    private final JsonFactory jsonFactory;

    /**
     * The base URL of the service, for example {@code "https://www.googleapis.com/tasks/v1/"}. Must
     * be URL-encoded and must end with a "/". This is determined when the library is generated and
     * normally should not be changed.
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
      return new JsonHttpClient(transport,
          jsonHttpRequestInitializer,
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

    /** Sets the JSON HTTP request initializer. Subclasses should override by calling super. */
    public Builder setJsonHttpRequestInitializer(
        JsonHttpRequestInitializer jsonHttpRequestInitializer) {
      this.jsonHttpRequestInitializer = jsonHttpRequestInitializer;
      return this;
    }

    /** Returns the JSON HTTP request initializer or {@code null} for none. */
    public JsonHttpRequestInitializer getJsonHttpRequestInitializer() {
      return jsonHttpRequestInitializer;
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
     * Returns the application name to be used in the UserAgent header of each request or {@code
     * null} for none.
     */
    public final String getApplicationName() {
      return applicationName;
    }
  }
}
