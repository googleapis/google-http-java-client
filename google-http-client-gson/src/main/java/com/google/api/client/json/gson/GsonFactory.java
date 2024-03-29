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

package com.google.api.client.json.gson;

import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.JsonGenerator;
import com.google.api.client.json.JsonParser;
import com.google.api.client.util.Beta;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.StringReader;
import java.io.Writer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * Low-level JSON library implementation based on GSON.
 *
 * <p>Implementation is thread-safe, and sub-classes must be thread-safe. For maximum efficiency,
 * applications should use a single globally-shared instance of the JSON factory.
 *
 * @since 1.3
 * @author Yaniv Inbar
 */
public class GsonFactory extends JsonFactory {

  /**
   * {@link Beta} <br>
   * Returns a global thread-safe instance.
   *
   * @since 1.16
   */
  @Beta
  public static GsonFactory getDefaultInstance() {
    return InstanceHolder.INSTANCE;
  }

  /** Controls the behavior of leniency in reading JSON value */
  private boolean readLeniency = false;

  /** Holder for the result of {@link #getDefaultInstance()}. */
  @Beta
  static class InstanceHolder {
    static final GsonFactory INSTANCE = new GsonFactory();
  }

  // Keeping the default, non-arg constructor for backward compatibility. Users should use
  // getDefaultInstance() or builder() instead.
  public GsonFactory() {}

  private GsonFactory(Builder builder) {
    readLeniency = builder.readLeniency;
  }

  @Override
  public JsonParser createJsonParser(InputStream in) {
    return createJsonParser(new InputStreamReader(in, StandardCharsets.UTF_8));
  }

  @Override
  public JsonParser createJsonParser(InputStream in, Charset charset) {
    if (charset == null) {
      return createJsonParser(in);
    }
    return createJsonParser(new InputStreamReader(in, charset));
  }

  @Override
  public JsonParser createJsonParser(String value) {
    return createJsonParser(new StringReader(value));
  }

  @Override
  public JsonParser createJsonParser(Reader reader) {
    return new GsonParser(this, new JsonReader(reader));
  }

  @Override
  public JsonGenerator createJsonGenerator(OutputStream out, Charset enc) {
    return createJsonGenerator(new OutputStreamWriter(out, enc));
  }

  @Override
  public JsonGenerator createJsonGenerator(Writer writer) {
    return new GsonGenerator(this, new JsonWriter(writer));
  }

  /** Returns true if it is lenient to input JSON value. */
  boolean getReadLeniency() {
    return readLeniency;
  }

  /** Returns the builder * */
  public static Builder builder() {
    return new Builder();
  }

  /** Builder for GsonFactory. */
  public static final class Builder {
    // Do not directly call this constructor
    private Builder() {}

    private boolean readLeniency = false;

    /**
     * Set to {@code true} when you want to the JSON parser to be lenient to reading JSON value. By
     * default, it is {@code false}.
     */
    public Builder setReadLeniency(boolean readLeniency) {
      this.readLeniency = readLeniency;
      return this;
    }

    /** Builds GsonFactory instance. */
    public GsonFactory build() {
      return new GsonFactory(this);
    }
  }
}
