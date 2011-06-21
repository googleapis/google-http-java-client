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

package com.google.api.client.http.json;

import com.google.api.client.http.HttpParser;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.json.Json;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.JsonParser;
import com.google.api.client.json.JsonToken;
import com.google.common.base.Preconditions;

import java.io.IOException;
import java.io.InputStream;

/**
 * Parses HTTP JSON response content into an data class of key/value pairs.
 *
 * <p>
 * Implementation is thread-safe as long as the fields are not set directly (which is deprecated
 * usage).
 * </p>
 *
 * <p>
 * Sample usage:
 * </p>
 *
 * <pre>
 * <code>
  static void setParser(HttpRequest request) {
    request.addParser(new JsonHttpParser(new JacksonFactory()));
  }
 * </code>
 * </pre>
 *
 * @since 1.0
 * @author Yaniv Inbar
 */
public class JsonHttpParser implements HttpParser {

  /**
   * Content type. Default value is {@link Json#CONTENT_TYPE}.
   *
   * @deprecated (scheduled to be made private final in 1.6) Use {@link #getContentType} or
   *             {@link Builder#setContentType}
   */
  @Deprecated
  public String contentType = Json.CONTENT_TYPE;

  /**
   * JSON factory.
   *
   * @since 1.3
   * @deprecated (scheduled to be made private final in 1.6) Use {@link #getJsonFactory}
   */
  @Deprecated
  public JsonFactory jsonFactory;

  /**
   * @deprecated (scheduled to be removed in 1.6) Use {@link #JsonHttpParser(JsonFactory)}
   */
  @Deprecated
  public JsonHttpParser() {
  }

  /**
   * @param jsonFactory JSON factory
   * @since 1.5
   */
  public JsonHttpParser(JsonFactory jsonFactory) {
    this.jsonFactory = Preconditions.checkNotNull(jsonFactory);
  }

  /**
   * @param jsonFactory JSON factory
   * @param contentType content type or {@code null} for none
   * @since 1.5
   */
  protected JsonHttpParser(JsonFactory jsonFactory, String contentType) {
    this(jsonFactory);
    this.contentType = contentType;
  }

  public final String getContentType() {
    return contentType;
  }

  public <T> T parse(HttpResponse response, Class<T> dataClass) throws IOException {
    return parserForResponse(jsonFactory, response).parseAndClose(dataClass, null);
  }

  /**
   * Returns the JSON factory.
   *
   * @since 1.5
   */
  public final JsonFactory getJsonFactory() {
    return jsonFactory;
  }

  /**
   * Returns a JSON parser to use for parsing the given HTTP response.
   * <p>
   * The response content will be closed if any throwable is thrown. On success, the current token
   * will be the first top token, which is normally {@link JsonToken#START_ARRAY} or
   * {@link JsonToken#START_OBJECT}.
   * </p>
   *
   * @param jsonFactory JSON factory to use
   * @param response HTTP response
   * @return JSON parser
   * @throws IllegalArgumentException if content type is not {@link Json#CONTENT_TYPE}
   * @since 1.3
   */
  public static JsonParser parserForResponse(JsonFactory jsonFactory, HttpResponse response)
      throws IOException {
    InputStream content = response.getContent();
    try {
      JsonParser parser = jsonFactory.createJsonParser(content);
      parser.nextToken();
      content = null;
      return parser;
    } finally {
      if (content != null) {
        content.close();
      }
    }
  }

  /**
   * Returns an instance of a new builder.
   *
   * @param jsonFactory JSON factory
   * @since 1.5
   */
  public static Builder builder(JsonFactory jsonFactory) {
    return new Builder(jsonFactory);
  }

  /**
   * Builder for {@link JsonHttpParser}.
   *
   * <p>
   * Implementation is not thread-safe.
   * </p>
   *
   * @since 1.5
   */
  public static class Builder {

    /** Content type or {@code null} for none. */
    private String contentType = Json.CONTENT_TYPE;

    /** JSON factory. */
    private final JsonFactory jsonFactory;

    /**
     * @param jsonFactory JSON factory
     */
    protected Builder(JsonFactory jsonFactory) {
      this.jsonFactory = jsonFactory;
    }

    /** Builds a new instance of {@link JsonHttpParser}. */
    public JsonHttpParser build() {
      return new JsonHttpParser(jsonFactory, contentType);
    }

    /** Returns the content type or {@code null} for none. */
    public final String getContentType() {
      return contentType;
    }

    /**
     * Sets the content type.
     *
     * <p>
     * Default value is {@link Json#CONTENT_TYPE}.
     * </p>
     */
    public Builder setContentType(String contentType) {
      this.contentType = Preconditions.checkNotNull(contentType);
      return this;
    }

    /** Returns the JSON factory. */
    public final JsonFactory getJsonFactory() {
      return jsonFactory;
    }
  }
}
