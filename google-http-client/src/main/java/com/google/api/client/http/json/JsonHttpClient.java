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
import com.google.api.client.http.HttpMethod;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestFactory;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;

import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Logger;

/**
 * JSON HTTP Client.
 *
 * @since 1.6
 * @author Ravi Mistry
 */
public class JsonHttpClient {

  static final Logger LOGGER = Logger.getLogger(JsonHttpClient.class.getName());

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
   */
  public final String getBaseUrl() {
    return baseUrl;
  }

  /**
   * Returns the application name to be sent in the User-Agent header of each request or {@code
   * null} for none.
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
    Preconditions.checkArgument(baseUrl.endsWith("/"));
    this.applicationName = applicationName;
    this.jsonFactory = Preconditions.checkNotNull(jsonFactory);
    Preconditions.checkNotNull(transport);
    this.requestFactory = httpRequestInitializer == null
        ? transport.createRequestFactory() : transport.createRequestFactory(httpRequestInitializer);
  }

  /**
   * Create an {@link HttpRequest} suitable for use against this service.
   *
   * <p>
   * Subclasses may override if specific behavior is required, for example if a sequence of requests
   * need to be built instead of a single request then subclasses should throw an
   * {@link UnsupportedOperationException}. Subclasses which override this method can make use of
   * {@link HttpRequest#addParser}, {@link HttpRequest#setContent} and
   * {@link HttpRequest#setEnableGZipContent}.
   * </p>
   *
   * @param method HTTP Method type
   * @param url The complete URL of the service where requests should be sent
   * @param body A POJO that can be serialized into JSON or {@code null} for none
   * @return newly created {@link HttpRequest}
   * @since 1.7
   */
  protected HttpRequest buildHttpRequest(HttpMethod method, GenericUrl url, Object body)
      throws IOException {
    HttpRequest httpRequest = requestFactory.buildRequest(method, url, null);
    httpRequest.addParser(getJsonHttpParser());
    if (getApplicationName() != null) {
      httpRequest.getHeaders().setUserAgent(getApplicationName());
    }
    if (body != null) {
      httpRequest.setContent(createSerializer(body));
    }
    return httpRequest;
  }

  /**
   * Builds and executes a {@link HttpRequest}. Subclasses may override if specific behavior is
   * required, for example if a sequence of requests need to be built instead of a single request.
   *
   * <p>
   * Callers are responsible for closing the response's content input stream by calling
   * {@link HttpResponse#ignore}.
   * </p>
   *
   * @param method HTTP Method type
   * @param url The complete URL of the service where requests should be sent
   * @param body A POJO that can be serialized into JSON or {@code null} for none
   * @return {@link HttpRequest} type
   * @throws IOException if the request fails
   * @since 1.7
   */
  protected HttpResponse executeUnparsed(HttpMethod method, GenericUrl url, Object body)
      throws IOException {
    HttpRequest request = buildHttpRequest(method, url, body);
    return request.execute();
  }

  /**
   * Builds and executes an {@link HttpRequest} and then returns the content input stream of
   * {@link HttpResponse}. Subclasses may override if specific behavior is required.
   *
   * <p>
   * Callers are responsible for closing the input stream.
   * </p>
   *
   * @param method HTTP Method type
   * @param url The complete URL of the service where requests should be sent
   * @param body A POJO that can be serialized into JSON or {@code null} for none
   * @return input stream of the response content
   * @throws IOException if the request fails
   * @since 1.8
   */
  protected InputStream executeAsInputStream(HttpMethod method, GenericUrl url, Object body)
      throws IOException {
    HttpResponse response = executeUnparsed(method, url, body);
    return response.getContent();
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
    private GenericUrl baseUrl;

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
      setBaseUrl(baseUrl);
    }

    /** Builds a new instance of {@link JsonHttpClient}. */
    public JsonHttpClient build() {
      if (Strings.isNullOrEmpty(applicationName)) {
        LOGGER.warning("Application name is not set. Call setApplicationName.");
      }
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

    /**
     * Returns the base URL of the service, for example
     * {@code "https://www.googleapis.com/tasks/v1/"}. Must be URL-encoded and must end with a "/".
     * This is determined when the library is generated and normally should not be changed.
     */
    public final GenericUrl getBaseUrl() {
      return baseUrl;
    }

    /**
     * Sets the base URL of the service, for example {@code "https://www.googleapis.com/tasks/v1/"}.
     * Must be URL-encoded and must end with a "/". This is determined when the library is generated
     * and normally should not be changed. Subclasses should override by calling super.
     *
     * @since 1.7
     */
    public Builder setBaseUrl(GenericUrl baseUrl) {
      this.baseUrl = Preconditions.checkNotNull(baseUrl);
      Preconditions.checkArgument(baseUrl.build().endsWith("/"));
      return this;
    }

    /**
     * Sets the JSON HTTP request initializer or {@code null} for none. Subclasses should override
     * by calling super.
     */
    public Builder setJsonHttpRequestInitializer(
        JsonHttpRequestInitializer jsonHttpRequestInitializer) {
      this.jsonHttpRequestInitializer = jsonHttpRequestInitializer;
      return this;
    }

    /** Returns the JSON HTTP request initializer or {@code null} for none. */
    public JsonHttpRequestInitializer getJsonHttpRequestInitializer() {
      return jsonHttpRequestInitializer;
    }

    /**
     * Sets the HTTP request initializer or {@code null} for none. Subclasses should override by
     * calling super.
     */
    public Builder setHttpRequestInitializer(HttpRequestInitializer httpRequestInitializer) {
      this.httpRequestInitializer = httpRequestInitializer;
      return this;
    }

    /** Returns the HTTP request initializer or {@code null} for none. */
    public final HttpRequestInitializer getHttpRequestInitializer() {
      return httpRequestInitializer;
    }

    /**
     * Sets the application name to be used in the UserAgent header of each request or {@code null}
     * for none. Subclasses should override by calling super.
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
