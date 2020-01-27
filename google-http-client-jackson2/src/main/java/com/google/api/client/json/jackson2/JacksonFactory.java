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

package com.google.api.client.json.jackson2;

import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.JsonGenerator;
import com.google.api.client.json.JsonParser;
import com.google.api.client.json.JsonToken;
import com.google.api.client.util.Preconditions;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.Charset;

/**
 * Low-level JSON library implementation based on Jackson 2.
 *
 * <p>Implementation is thread-safe. For maximum efficiency,
 * applications should use a single globally-shared instance of the JSON factory.
 *
 * @since 1.11
 * @author Yaniv Inbar
 */
public final class JacksonFactory extends JsonFactory {

  /** JSON factory. */
  private final com.fasterxml.jackson.core.JsonFactory factory =
      new com.fasterxml.jackson.core.JsonFactory();

  {
    // don't auto-close JSON content in order to ensure consistent behavior across JSON factories
    // TODO(rmistry): Should we disable the JsonGenerator.Feature.AUTO_CLOSE_TARGET feature?
    factory.configure(
        com.fasterxml.jackson.core.JsonGenerator.Feature.AUTO_CLOSE_JSON_CONTENT, false);
  }

  /**
   * Returns a global thread-safe instance.
   *
   * @since 1.16
   */
  public static JacksonFactory getDefaultInstance() {
    return InstanceHolder.INSTANCE;
  }

  /** Holder for the result of {@link #getDefaultInstance()}. */
  static class InstanceHolder {
    static final JacksonFactory INSTANCE = new JacksonFactory();
  }

  @Override
  public JsonGenerator createJsonGenerator(OutputStream out, Charset enc) throws IOException {
    return new JacksonGenerator(
        this, factory.createJsonGenerator(out, com.fasterxml.jackson.core.JsonEncoding.UTF8));
  }

  @Override
  public JsonGenerator createJsonGenerator(Writer writer) throws IOException {
    return new JacksonGenerator(this, factory.createJsonGenerator(writer));
  }

  @Override
  public JsonParser createJsonParser(Reader reader) throws IOException {
    Preconditions.checkNotNull(reader);
    return new JacksonParser(this, factory.createJsonParser(reader));
  }

  @Override
  public JsonParser createJsonParser(InputStream in) throws IOException {
    Preconditions.checkNotNull(in);
    return new JacksonParser(this, factory.createJsonParser(in));
  }

  @Override
  public JsonParser createJsonParser(InputStream in, Charset charset) throws IOException {
    Preconditions.checkNotNull(in);
    return new JacksonParser(this, factory.createJsonParser(in));
  }

  @Override
  public JsonParser createJsonParser(String value) throws IOException {
    Preconditions.checkNotNull(value);
    return new JacksonParser(this, factory.createJsonParser(value));
  }

  static JsonToken convert(com.fasterxml.jackson.core.JsonToken token) {
    if (token == null) {
      return null;
    }
    switch (token) {
      case END_ARRAY:
        return JsonToken.END_ARRAY;
      case START_ARRAY:
        return JsonToken.START_ARRAY;
      case END_OBJECT:
        return JsonToken.END_OBJECT;
      case START_OBJECT:
        return JsonToken.START_OBJECT;
      case VALUE_FALSE:
        return JsonToken.VALUE_FALSE;
      case VALUE_TRUE:
        return JsonToken.VALUE_TRUE;
      case VALUE_NULL:
        return JsonToken.VALUE_NULL;
      case VALUE_STRING:
        return JsonToken.VALUE_STRING;
      case VALUE_NUMBER_FLOAT:
        return JsonToken.VALUE_NUMBER_FLOAT;
      case VALUE_NUMBER_INT:
        return JsonToken.VALUE_NUMBER_INT;
      case FIELD_NAME:
        return JsonToken.FIELD_NAME;
      default:
        return JsonToken.NOT_AVAILABLE;
    }
  }
}
