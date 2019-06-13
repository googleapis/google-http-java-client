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
import com.google.api.client.util.Preconditions;
import com.google.api.client.util.Sets;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.lang.reflect.Type;
import java.nio.charset.Charset;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Parses JSON data into an data class of key/value pairs.
 *
 * <p>Implementation is thread-safe.
 *
 * <p>Sample usage:
 *
 * <pre>
 * <code>
 * static void setParser(HttpRequest request) {
 * request.setParser(new JsonObjectParser(new JacksonFactory()));
 * }
 * </code>
 * </pre>
 *
 * @author Matthias Linder (mlinder)
 * @since 1.10
 */
public class JsonObjectParser implements ObjectParser {

  /** JSON factory. */
  private final JsonFactory jsonFactory;

  /** Wrapper keys for the JSON content or empty for none. */
  private final Set<String> wrapperKeys;

  /** @param jsonFactory JSON factory */
  public JsonObjectParser(JsonFactory jsonFactory) {
    this(new Builder(jsonFactory));
  }

  /**
   * @param builder builder
   * @since 1.14
   */
  protected JsonObjectParser(Builder builder) {
    jsonFactory = builder.jsonFactory;
    wrapperKeys = new HashSet<String>(builder.wrapperKeys);
  }

  @SuppressWarnings("unchecked")
  public <T> T parseAndClose(InputStream in, Charset charset, Class<T> dataClass)
      throws IOException {
    return (T) parseAndClose(in, charset, (Type) dataClass);
  }

  public Object parseAndClose(InputStream in, Charset charset, Type dataType) throws IOException {
    JsonParser parser = jsonFactory.createJsonParser(in, charset);
    initializeParser(parser);
    return parser.parse(dataType, true);
  }

  @SuppressWarnings("unchecked")
  public <T> T parseAndClose(Reader reader, Class<T> dataClass) throws IOException {
    return (T) parseAndClose(reader, (Type) dataClass);
  }

  public Object parseAndClose(Reader reader, Type dataType) throws IOException {
    JsonParser parser = jsonFactory.createJsonParser(reader);
    initializeParser(parser);
    return parser.parse(dataType, true);
  }

  /** Returns the JSON factory. */
  public final JsonFactory getJsonFactory() {
    return jsonFactory;
  }

  /**
   * Returns the unmodifiable set of wrapper keys for the JSON content.
   *
   * @since 1.14
   */
  public Set<String> getWrapperKeys() {
    return Collections.unmodifiableSet(wrapperKeys);
  }

  /**
   * Initialize the parser to skip to wrapped keys (if any).
   *
   * @param parser JSON parser
   */
  private void initializeParser(JsonParser parser) throws IOException {
    if (wrapperKeys.isEmpty()) {
      return;
    }
    boolean failed = true;
    try {
      String match = parser.skipToKey(wrapperKeys);
      Preconditions.checkArgument(
          match != null && parser.getCurrentToken() != JsonToken.END_OBJECT,
          "wrapper key(s) not found: %s",
          wrapperKeys);
      failed = false;
    } finally {
      if (failed) {
        parser.close();
      }
    }
  }

  /**
   * Builder.
   *
   * <p>Implementation is not thread-safe.
   *
   * @since 1.14
   */
  public static class Builder {

    /** JSON factory. */
    final JsonFactory jsonFactory;

    /** Wrapper keys for the JSON content or empty for none. */
    Collection<String> wrapperKeys = Sets.newHashSet();

    /** @param jsonFactory JSON factory */
    public Builder(JsonFactory jsonFactory) {
      this.jsonFactory = Preconditions.checkNotNull(jsonFactory);
    }

    /** Returns a new instance of a JSON object parser. */
    public JsonObjectParser build() {
      return new JsonObjectParser(this);
    }

    /** Returns the JSON factory. */
    public final JsonFactory getJsonFactory() {
      return jsonFactory;
    }

    /** Returns the wrapper keys for the JSON content. */
    public final Collection<String> getWrapperKeys() {
      return wrapperKeys;
    }

    /**
     * Sets the wrapper keys for the JSON content.
     *
     * <p>Overriding is only supported for the purpose of calling the super implementation and
     * changing the return type, but nothing else.
     */
    public Builder setWrapperKeys(Collection<String> wrapperKeys) {
      this.wrapperKeys = wrapperKeys;
      return this;
    }
  }
}
