/*
 * Copyright (c) 2012 Google Inc.
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

package com.google.api.client.json;

import com.google.api.client.util.ObjectParser;
import com.google.common.base.Preconditions;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.lang.reflect.Type;
import java.nio.charset.Charset;

/**
 * Parses JSON data into an data class of key/value pairs.
 *
 * <p>
 * Implementation is thread-safe.
 * </p>
 *
 * <p>
 * Sample usage:
 * </p>
 *
 * <pre>
 * <code>
  static void setParser(HttpRequest request) {
    request.setParser(new JsonObjectParser(new JacksonFactory()));
  }
 * </code>
 * </pre>
 *
 * @author Matthias Linder (mlinder)
 * @since 1.10
 */
public class JsonObjectParser implements ObjectParser {
  /** JSON factory. */
  private final JsonFactory jsonFactory;

  /**
   * Returns the JSON factory.
   */
  public final JsonFactory getJsonFactory() {
    return jsonFactory;
  }

  /**
   * Constructor with required parameters.
   *
   * @param jsonFactory JSON factory
   */
  public JsonObjectParser(JsonFactory jsonFactory) {
    this.jsonFactory = Preconditions.checkNotNull(jsonFactory);
  }

  @SuppressWarnings("unchecked")
  public <T> T parseAndClose(InputStream in, Charset charset, Class<T> dataClass)
      throws IOException {
    return (T) parseAndClose(in, charset, (Type) dataClass);
  }

  public Object parseAndClose(InputStream in, Charset charset, Type dataType)
      throws IOException {
    JsonParser parser = jsonFactory.createJsonParser(in, charset);
    return parser.parse(dataType, true, null);
  }

  @SuppressWarnings("unchecked")
  public <T> T parseAndClose(Reader reader, Class<T> dataClass) throws IOException {
    return (T) parseAndClose(reader, (Type) dataClass);
  }

  public Object parseAndClose(Reader reader, Type dataType) throws IOException {
    JsonParser parser = jsonFactory.createJsonParser(reader);
    return parser.parse(dataType, true, null);
  }
}
