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
import com.google.api.client.json.JsonObjectParser;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;

import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Logger;

/**
 * JSON HTTP Client.
 *
 * <p>
 * Implementation is not thread-safe.
 * </p>
 *
 * <p>
 * Upgrade warning: prior to version 1.10 there was a {@code builder} method in
 * {@link JsonHttpClient}, this has been removed in version 1.10. The Builder can now be
 * instantiated with
 * {@link Builder#Builder(HttpTransport, JsonFactory, String, String, HttpRequestInitializer)}.
 * </p>
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
  @Deprecated
  private final String baseUrl;

  /** The root URL of the service, for example {@code "https://www.googleapis.com/"}. */
  private final String rootUrl;

  /** The service path of the service, for example {@code "tasks/v1/"}. */
  private final String servicePath;


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
  @Deprecated
  private JsonHttpParser jsonHttpParser;

  /**
   * The JSON parser to use for parsing streams.
   */
  private final JsonObjectParser jsonObjectParser;

  /**
   * Set to {@code true} if baseUrl is used instead of {@code servicePath} and {@code rootUrl}.
   */
  @Deprecated
  private final boolean baseUrlUsed;

  /**
   * Returns if baseUrl is used instead of {@code servicePath} and {@code rootUrl}.
   *
   * @since 1.10
   * @deprecated (scheduled to be removed in 1.11) Use {@link #getRootUrl} and
   *             {@link #getServicePath} instead.
   */
  @Deprecated
  protected final boolean isBaseUrlUsed() {
    return baseUrlUsed;
  }

  /**
   * Returns the base URL of the service, for example {@code "https://www.googleapis.com/tasks/v1/"}
   * . Must be URL-encoded and must end with a "/". This is determined when the library is generated
   * and normally should not be changed.
   */
  public final String getBaseUrl() {
    if (baseUrlUsed) {
      return baseUrl;
    }
    return rootUrl + servicePath;
  }

  /**
   * Returns the root URL of the service, for example {@code https://www.googleapis.com/}. Must be
   * URL-encoded and must end with a "/".
   *
   * @since 1.10
   */
  public final String getRootUrl() {
    Preconditions.checkArgument(!baseUrlUsed, "root URL cannot be used if base URL is used.");
    return rootUrl;
  }

  /**
   * Returns the service path of the service, for example {@code "tasks/v1/"}. Must be URL-encoded
   * and must end with a "/" and not begin with a "/". It is allowed to be an empty string
   * {@code ""} or a forward slash {@code "/"}, if it is a forward slash then it is treated as an
   * empty string
   *
   * @since 1.10
   */
  public final String getServicePath() {
    Preconditions.checkArgument(!baseUrlUsed, "service path cannot be used if base URL is used.");
    return servicePath;
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

  /** Returns the JSON HTTP request initializer or {@code null} for none. */
  public final JsonHttpRequestInitializer getJsonHttpRequestInitializer() {
    return jsonHttpRequestInitializer;
  }

  /**
   * Returns the JSON HTTP Parser. Initializes the parser once and then caches it for all subsequent
   * calls to this method.
   *
   * @deprecated (scheduled to be removed in 1.11) Use {@link #getJsonObjectParser()} instead.
   */
  @Deprecated
  public final JsonHttpParser getJsonHttpParser() {
    if (jsonHttpParser == null) {
      jsonHttpParser = createParser();
    }
    return jsonHttpParser;
  }

  /**
   * Creates a JSON parser. Subclasses may override if specific {@link JsonHttpParser}
   * implementations are required.
   *
   * @deprecated (scheduled to be removed in 1.11) Use
   *             {@link Builder#setObjectParser(JsonObjectParser)} instead.
   */
  @Deprecated
  protected JsonHttpParser createParser() {
    return new JsonHttpParser(jsonFactory);
  }

  /**
   * Returns the JSON Object Parser.
   *
   * @since 1.10
   */
  public final JsonObjectParser getJsonObjectParser() {
    return jsonObjectParser;
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
   * Use {@link Builder} if you need to specify any of the optional parameters.
   * </p>
   *
   * @param transport The transport to use for requests
   * @param jsonFactory A factory for creating JSON parsers and serializers
   * @param baseUrl The base URL of the service. Must end with a "/"
   * @deprecated (scheduled to be removed in 1.11) Use {@link #JsonHttpClient(HttpTransport,
   *             JsonFactory, String, String, HttpRequestInitializer)}.
   */
  @Deprecated
  public JsonHttpClient(HttpTransport transport, JsonFactory jsonFactory, String baseUrl) {
    this(transport, null, null, jsonFactory, baseUrl, null);
  }

  /**
   * Constructor with required parameters.
   *
   * <p>
   * Use {@link Builder} if you need to specify any of the optional parameters.
   * </p>
   *
   * @param transport The transport to use for requests
   * @param jsonFactory A factory for creating JSON parsers and serializers
   * @param rootUrl The root URL of the service. Must end with a "/"
   * @param servicePath The service path of the service. Must end with a "/" and not begin with a
   *        "/". It is allowed to be an empty string {@code ""} or a forward slash {@code "/"}, if
   *        it is a forward slash then it is treated as an empty string
   * @param httpRequestInitializer The HTTP request initializer or {@code null} for none
   * @since 1.10
   */
  public JsonHttpClient(HttpTransport transport, JsonFactory jsonFactory, String rootUrl,
      String servicePath, HttpRequestInitializer httpRequestInitializer) {
    this(transport, null, httpRequestInitializer, jsonFactory, null, rootUrl, servicePath, null);
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
   * @deprecated (scheduled to be removed in 1.11) Use {@link #JsonHttpClient(HttpTransport,
   *             JsonHttpRequestInitializer, HttpRequestInitializer, JsonFactory, JsonObjectParser,
   *             String, String, String)}.
   */
  @Deprecated
  protected JsonHttpClient(HttpTransport transport,
      JsonHttpRequestInitializer jsonHttpRequestInitializer,
      HttpRequestInitializer httpRequestInitializer,
      JsonFactory jsonFactory,
      String baseUrl,
      String applicationName) {
    this(transport,
        jsonHttpRequestInitializer,
        httpRequestInitializer,
        jsonFactory,
        null,
        baseUrl,
        applicationName);
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
   * @deprecated (scheduled to be removed in 1.11) Use {@link #JsonHttpClient(HttpTransport,
   *             JsonHttpRequestInitializer, HttpRequestInitializer, JsonFactory, JsonObjectParser,
   *             String, String, String)}.
   * @since 1.10
   */
  @Deprecated
  protected JsonHttpClient(HttpTransport transport,
      JsonHttpRequestInitializer jsonHttpRequestInitializer,
      HttpRequestInitializer httpRequestInitializer,
      JsonFactory jsonFactory,
      JsonObjectParser jsonObjectParser,
      String baseUrl,
      String applicationName) {
    this.jsonHttpRequestInitializer = jsonHttpRequestInitializer;
    this.baseUrl = Preconditions.checkNotNull(baseUrl);
    Preconditions.checkArgument(baseUrl.endsWith("/"));
    this.applicationName = applicationName;
    this.jsonFactory = Preconditions.checkNotNull(jsonFactory);
    this.jsonObjectParser = jsonObjectParser;
    Preconditions.checkNotNull(transport);
    this.requestFactory = httpRequestInitializer == null
        ? transport.createRequestFactory() : transport.createRequestFactory(httpRequestInitializer);
    this.baseUrlUsed = true;
    this.rootUrl = null;
    this.servicePath = null;
  }

  /**
   * Construct the {@link JsonHttpClient}.
   *
   *
   * @param transport The transport to use for requests
   * @param jsonHttpRequestInitializer The initializer to use when creating an
   *        {@link JsonHttpRequest} or {@code null} for none
   * @param httpRequestInitializer The initializer to use when creating an {@link HttpRequest} or
   *        {@code null} for none
   * @param jsonFactory A factory for creating JSON parsers and serializers
   * @param jsonObjectParser JSON parser to use or {@code null} if unused. {@code null} won't be
   *        accepted from 1.11 on.
   * @param rootUrl The root URL of the service. Must end with a "/"
   * @param servicePath The service path of the service. Must end with a "/" and not begin with a
   *        "/". It is allowed to be an empty string {@code ""} or a forward slash {@code "/"}, if
   *        it is a forward slash then it is treated as an empty string
   * @param applicationName The application name to be sent in the User-Agent header of requests or
   *        {@code null} for none
   * @since 1.10
   */
  protected JsonHttpClient(HttpTransport transport,
      JsonHttpRequestInitializer jsonHttpRequestInitializer,
      HttpRequestInitializer httpRequestInitializer,
      JsonFactory jsonFactory,
      JsonObjectParser jsonObjectParser,
      String rootUrl,
      String servicePath,
      String applicationName) {
    this.jsonHttpRequestInitializer = jsonHttpRequestInitializer;
    this.rootUrl = Preconditions.checkNotNull(rootUrl, "root URL cannot be null.");
    Preconditions.checkArgument(rootUrl.endsWith("/"), "root URL must end with a \"/\".");
    Preconditions.checkNotNull(servicePath, "service path cannot be null");
    if (servicePath.length() == 1) {
      Preconditions.checkArgument(
          "/".equals(servicePath), "service path must equal \"/\" if it is of length 1.");
      servicePath = "";
    } else if (servicePath.length() > 0) {
      Preconditions.checkArgument(servicePath.endsWith("/") && !servicePath.startsWith("/"),
          "service path must end with a \"/\" and not begin with a \"/\".");
    }
    this.servicePath = servicePath;
    this.applicationName = applicationName;
    this.jsonFactory = Preconditions.checkNotNull(jsonFactory);
    Preconditions.checkNotNull(transport);
    this.jsonObjectParser = jsonObjectParser;
    this.requestFactory = httpRequestInitializer == null
        ? transport.createRequestFactory() : transport.createRequestFactory(httpRequestInitializer);
    this.baseUrl = null;
    this.baseUrlUsed = false;
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
  @SuppressWarnings("deprecation")
  protected HttpRequest buildHttpRequest(HttpMethod method, GenericUrl url, Object body)
      throws IOException {
    HttpRequest httpRequest = requestFactory.buildRequest(method, url, null);
    JsonObjectParser parser = getJsonObjectParser();

    if (parser != null) {
      httpRequest.setParser(parser);
    } else {
      httpRequest.addParser(getJsonHttpParser());
    }
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
   * Callers are responsible for disconnecting the HTTP response by calling
   * {@link HttpResponse#disconnect}. Example usage:
   * </p>
   *
   * <pre>
     HttpResponse response = client.executeUnparsed(method, url, body);
     try {
       // process response..
     } finally {
       response.disconnect();
     }
   * </pre>
   *
   * @param method HTTP Method type
   * @param url The complete URL of the service where requests should be sent
   * @param body A POJO that can be serialized into JSON or {@code null} for none
   * @return {@link HttpResponse} type
   * @throws IOException if the request fails
   * @since 1.7
   */
  protected HttpResponse executeUnparsed(HttpMethod method, GenericUrl url, Object body)
      throws IOException {
    HttpRequest request = buildHttpRequest(method, url, body);
    return executeUnparsed(request);
  }

  /**
   * Executes the specified {@link HttpRequest}. Subclasses may override if specific behavior is
   * required, for example if a custom error is required to be thrown.
   *
   * <p>
   * Callers are responsible for disconnecting the HTTP response by calling
   * {@link HttpResponse#disconnect}. Example usage:
   * </p>
   *
   * <pre>
     HttpResponse response = client.executeUnparsed(request);
     try {
       // process response..
     } finally {
       response.disconnect();
     }
   * </pre>
   *
   * @param request HTTP Request
   * @return {@link HttpResponse} type
   * @throws IOException if the request fails
   * @since 1.9
   */
  protected HttpResponse executeUnparsed(HttpRequest request) throws IOException {
    return request.execute();
  }

  /**
   * Builds and executes an {@link HttpRequest} and then returns the content input stream of
   * {@link HttpResponse}. Subclasses may override if specific behavior is required.
   *
   * <p>
   * Callers are responsible for closing the input stream after it is processed. Example usage:
   * </p>
   *
   * <pre>
     InputStream is = client.executeAsInputStream();
     try {
       // Process input stream..
     } finally {
       is.close();
     }
   * </pre>
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

    /** The JSON parser factory to user for parsing requests. */
    private final JsonFactory jsonFactory;

    /** The JSON parser used to parse streams. */
    private JsonObjectParser jsonObjectParser;

    /**
     * The base URL of the service, for example {@code "https://www.googleapis.com/tasks/v1/"}. Must
     * be URL-encoded and must end with a "/". This is determined when the library is generated and
     * normally should not be changed.
     */
    @Deprecated
    private GenericUrl baseUrl;

    /** The root URL of the service, for example {@code "https://www.googleapis.com/"}. */
    private String rootUrl;

    /** The service path of the service, for example {@code "tasks/v1/"}. */
    private String servicePath;

    /**
     * The application name to be sent in the User-Agent header of each request or {@code null} for
     * none.
     */
    private String applicationName;

    /**
     * Set to {@code true} if baseUrl is used instead of servicePath and rootUrl.
     */
    @Deprecated
    private boolean baseUrlUsed;

    /**
     * Returns an instance of a new builder.
     *
     * @param transport The transport to use for requests
     * @param jsonFactory A factory for creating JSON parsers and serializers
     * @param baseUrl The base URL of the service. Must end with a "/"
     *
     * @deprecated (scheduled to be removed in 1.11) Use {@link #Builder(HttpTransport, JsonFactory,
     *             String, String, HttpRequestInitializer)} instead.
     */
    @Deprecated
    protected Builder(HttpTransport transport, JsonFactory jsonFactory, GenericUrl baseUrl) {
      this.transport = transport;
      this.jsonFactory = jsonFactory;
      baseUrlUsed = true;
      setBaseUrl(baseUrl);
    }

    /**
     * Returns an instance of a new builder.
     *
     * @param transport The transport to use for requests
     * @param jsonFactory A factory for creating JSON parsers and serializers
     * @param rootUrl The root URL of the service. Must end with a "/"
     * @param servicePath The service path of the service. Must end with a "/" and not begin with a
     *        "/". It is allowed to be an empty string {@code ""} or a forward slash {@code "/"}, if
     *        it is a forward slash then it is treated as an empty string
     * @param httpRequestInitializer The HTTP request initializer or {@code null} for none
     * @since 1.10
     */
    public Builder(HttpTransport transport, JsonFactory jsonFactory, String rootUrl,
        String servicePath,
        HttpRequestInitializer httpRequestInitializer) {
      this.transport = transport;
      this.jsonFactory = jsonFactory;
      setRootUrl(rootUrl);
      setServicePath(servicePath);
      this.httpRequestInitializer = httpRequestInitializer;
    }

    /**
     * Builds a new instance of {@link JsonHttpClient}.
     */
    public JsonHttpClient build() {
      if (Strings.isNullOrEmpty(applicationName)) {
        LOGGER.warning("Application name is not set. Call setApplicationName.");
      }
      if (baseUrlUsed) {
        return new JsonHttpClient(transport,
            jsonHttpRequestInitializer,
            httpRequestInitializer,
            jsonFactory,
            jsonObjectParser,
            baseUrl.build(),
            applicationName);
      }
      return new JsonHttpClient(transport,
          jsonHttpRequestInitializer,
          httpRequestInitializer,
          jsonFactory,
          jsonObjectParser,
          rootUrl,
          servicePath,
          applicationName);
    }

    /**
     * Returns if {@code baseUrl} is used instead of {@code rootUrl} and {@code servicePath}.
     *
     * @since 1.10
     * @deprecated (scheduled to be removed in 1.11) Use {@link #getRootUrl} and
     *             {@link #getServicePath} instead.
     */
    @Deprecated
    protected final boolean isBaseUrlUsed() {
      return baseUrlUsed;
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
     * Returns the JSON parser used or {@code null} if not specified.
     *
     * <p>
     * Warning: This method will stop returning {@code null} in 1.11, and will return
     * {@link JsonFactory#createJsonObjectParser()} instead.
     * </p>
     *
     * @since 1.10
     */
    public final JsonObjectParser getObjectParser() {
      return jsonObjectParser;
    }

    /**
     * Specifies the JSON parser to use or {@code null} if no used.
     *
     * <p>
     * Warning: This method will stop accepting {@code null} in 1.11. The default will then be
     * {@link JsonFactory#createJsonObjectParser()}.
     * </p>
     *
     * @since 1.10
     */
    public Builder setObjectParser(JsonObjectParser parser) {
      jsonObjectParser = parser;
      return this;
    }

    /**
     * Returns the base URL of the service, for example
     * {@code "https://www.googleapis.com/tasks/v1/"}. Must be URL-encoded and must end with a "/".
     * This is determined when the library is generated and normally should not be changed.
     *
     * <p>
     * Use this method only if {@code baseUrl} is used instead of {@code rootUrl} and
     * {@code servicePath}.
     * </p>
     *
     * @deprecated (scheduled to be removed in 1.11) Use {@link #getRootUrl} and
     *             {@link #getServicePath} instead.
     */
    @Deprecated
    public final GenericUrl getBaseUrl() {
      Preconditions.checkArgument(baseUrlUsed);
      return baseUrl;
    }

    /**
     * Sets the base URL of the service, for example {@code "https://www.googleapis.com/tasks/v1/"}.
     * Must be URL-encoded and must end with a "/". This is determined when the library is generated
     * and normally should not be changed. Subclasses should override by calling super.
     *
     * <p>
     * Use this method only if {@code baseUrl} is used instead of {@code rootUrl} and
     * {@code servicePath}.
     * </p>
     *
     * @since 1.7
     * @deprecated (scheduled to be removed in 1.11) Use {@link #setRootUrl} and
     *             {@link #setServicePath} instead.
     */
    @Deprecated
    public Builder setBaseUrl(GenericUrl baseUrl) {
      Preconditions.checkArgument(baseUrlUsed);
      this.baseUrl = Preconditions.checkNotNull(baseUrl);
      Preconditions.checkArgument(baseUrl.build().endsWith("/"));
      return this;
    }

    /**
     * Returns the root URL of the service, for example {@code https://www.googleapis.com/}. Must be
     * URL-encoded and must end with a "/".
     *
     * <p>
     * Use this method only if {@code rootUrl} and {@code servicePath} are used instead of
     * {@code baseUrl}.
     * </p>
     *
     * @since 1.10
     */
    public final String getRootUrl() {
      Preconditions.checkArgument(!baseUrlUsed, "root URL cannot be used if base URL is used.");
      return rootUrl;
    }

    /**
     * Sets the root URL of the service, for example {@code https://www.googleapis.com/}. Must be
     * URL-encoded and must end with a "/". This is determined when the library is generated and
     * normally should be changed. Subclasses should override by calling super.
     *
     * <p>
     * Use this method only if {@code rootUrl} and {@code servicePath} are used instead of
     * {@code baseUrl}.
     * </p>
     *
     * @since 1.10
     */
    public Builder setRootUrl(String rootUrl) {
      Preconditions.checkArgument(!baseUrlUsed, "root URL cannot be used if base URL is used.");
      Preconditions.checkNotNull(rootUrl, "root URL cannot be null.");
      Preconditions.checkArgument(rootUrl.endsWith("/"), "root URL must end with a \"/\".");
      this.rootUrl = rootUrl;
      return this;
    }


    /**
     * Returns the service path of the service, for example {@code "tasks/v1/"}. Must be URL-encoded
     * and must end with a "/" and not begin with a "/". It is allowed to be an empty string
     * {@code ""}.
     *
     * <p>
     * Use this method only if {@code rootUrl} and {@code servicePath} are used instead of
     * {@code baseUrl}.
     * </p>
     *
     * @since 1.10
     */
    public final String getServicePath() {
      Preconditions.checkArgument(!baseUrlUsed, "service path cannot be used if base URL is used.");
      return servicePath;
    }

    /**
     * Sets the service path of the service, for example {@code "tasks/v1/"}. Must be URL-encoded
     * and must end with a "/" and not begin with a "/". It is allowed to be an empty string
     * {@code ""} or a forward slash {@code "/"}, if it is a forward slash then it is treated as an
     * empty string. This is determined when the library is generated and normally should not be
     * changed. Subclasses should override by calling super.
     *
     * <p>
     * Use this method only if {@code rootUrl} and {@code servicePath} are used instead of
     * {@code baseUrl}.
     * </p>
     *
     * @since 1.10
     */
    public Builder setServicePath(String servicePath) {
      Preconditions.checkArgument(!baseUrlUsed, "service path cannot be used if base URL is used.");
      servicePath = Preconditions.checkNotNull(servicePath, "service path cannot be null.");
      if (servicePath.length() == 1) {
        Preconditions.checkArgument(
            "/".equals(servicePath), "service path must equal \"/\" if it is of length 1.");
        servicePath = "";
      } else if (servicePath.length() > 0) {
        Preconditions.checkArgument(servicePath.endsWith("/") && !servicePath.startsWith("/"),
            "service path must end with a \"/\" and not begin with a \"/\".");
      }
      this.servicePath = servicePath;
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
     * Returns the application name to be used in the UserAgent header of each request or
     * {@code null} for none.
     */
    public final String getApplicationName() {
      return applicationName;
    }
  }
}
