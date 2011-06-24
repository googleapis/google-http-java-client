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

import com.google.api.client.http.AbstractHttpContent;
import com.google.api.client.json.Json;
import com.google.api.client.json.JsonEncoding;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.JsonGenerator;
import com.google.common.base.Preconditions;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Serializes JSON HTTP content based on the data key/value mapping object for an item.
 *
 * <p>
 * Sample usage:
 * </p>
 *
 * <pre>
 * <code>
  static void setContent(HttpRequest request, Object data) {
    request.setContent(new JsonHttpContent(new JacksonFactory(), data));
  }
 * </code>
 * </pre>
 *
 * <p>
 * Implementation is not thread-safe.
 * </p>
 *
 * @since 1.0
 * @author Yaniv Inbar
 */
public class JsonHttpContent extends AbstractHttpContent {
  // TODO(yanivi): ability to annotate fields as only needed for POST?

  /**
   * Content type. Default value is {@link Json#CONTENT_TYPE}.
   *
   * @deprecated (scheduled to be made private in 1.6) Use {@link #getType} or {@link #setType}
   */
  @Deprecated
  public String contentType = Json.CONTENT_TYPE;

  /**
   * JSON key name/value data.
   *
   * @deprecated (scheduled to be made private final in 1.6) Use {@link #getData}
   */
  @Deprecated
  public Object data;

  /**
   * JSON factory.
   *
   * @since 1.3
   * @deprecated (scheduled to be made private final in 1.6) Use {@link #getJsonFactory}
   */
  @Deprecated
  public JsonFactory jsonFactory;

  /**
   * @deprecated (scheduled to be removed in 1.6) Use {@link #JsonHttpContent(JsonFactory, Object)}
   */
  @Deprecated
  public JsonHttpContent() {
  }

  /**
   * @param jsonFactory JSON factory to use
   * @param data JSON key name/value data
   * @since 1.5
   */
  public JsonHttpContent(JsonFactory jsonFactory, Object data) {
    this.jsonFactory = Preconditions.checkNotNull(jsonFactory);
    this.data = Preconditions.checkNotNull(data);
  }

  public String getType() {
    return contentType;
  }

  public void writeTo(OutputStream out) throws IOException {
    JsonGenerator generator = jsonFactory.createJsonGenerator(out, JsonEncoding.UTF8);
    generator.serialize(data);
    generator.flush();
  }

  /**
   * Sets the content type or {@code null} for none.
   *
   * <p>
   * Defaults to {@link Json#CONTENT_TYPE}.
   * </p>
   *
   * @since 1.5
   */
  public JsonHttpContent setType(String type) {
    contentType = type;
    return this;
  }

  /**
   * Returns the JSON key name/value data.
   *
   * @since 1.5
   */
  public final Object getData() {
    return data;
  }

  /**
   * Returns the JSON factory.
   *
   * @since 1.5
   */
  public final JsonFactory getJsonFactory() {
    return jsonFactory;
  }
}
