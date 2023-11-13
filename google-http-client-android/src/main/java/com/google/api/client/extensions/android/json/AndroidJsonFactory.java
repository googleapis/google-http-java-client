/*
 * Copyright 2012 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.api.client.extensions.android.json;

import android.annotation.TargetApi;
import android.util.JsonReader;
import android.util.JsonWriter;
import com.google.api.client.extensions.android.AndroidUtils;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.JsonGenerator;
import com.google.api.client.json.JsonParser;
import com.google.api.client.util.Beta;
import com.google.api.client.util.Charsets;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.StringReader;
import java.io.Writer;
import java.nio.charset.Charset;

/**
 * {@link Beta} <br>
 * Low-level JSON library implementation based on GSON.
 *
 * <p>Implementation is thread-safe, and sub-classes must be thread-safe. For maximum efficiency,
 * applications should use a single globally-shared instance of the JSON factory.
 *
 * <p>Required minimum Android SDK 3.0 (level 11).
 *
 * @since 1.11
 * @author Yaniv Inbar
 */
@TargetApi(11)
@Beta
public class AndroidJsonFactory extends JsonFactory {

  /**
   * Returns a global thread-safe instance.
   *
   * @since 1.17
   */
  public static AndroidJsonFactory getDefaultInstance() {
    return InstanceHolder.INSTANCE;
  }

  /** Holder for the result of {@link #getDefaultInstance()}. */
  @Beta
  static class InstanceHolder {
    static final AndroidJsonFactory INSTANCE = new AndroidJsonFactory();
  }

  // TODO(yanivi): figure out how to run unit tests based on Android platform

  public AndroidJsonFactory() {
    AndroidUtils.checkMinimumSdkLevel(11);
  }

  @Override
  public JsonParser createJsonParser(InputStream in) {
    // TODO(mlinder): Charset should be detected automatically by the parser. Related to:
    // http://github.com/googleapis/google-http-java-client/issues/6
    return createJsonParser(new InputStreamReader(in, Charsets.UTF_8));
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
    return new AndroidJsonParser(this, new JsonReader(reader));
  }

  @Override
  public JsonGenerator createJsonGenerator(OutputStream out, Charset enc) {
    return createJsonGenerator(new OutputStreamWriter(out, enc));
  }

  @Override
  public JsonGenerator createJsonGenerator(Writer writer) {
    return new AndroidJsonGenerator(this, new JsonWriter(writer));
  }
}
