/*
 * Copyright (c) 2013 Google Inc.
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

package com.google.api.client.testing.json;

import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.JsonGenerator;
import com.google.api.client.json.JsonParser;
import com.google.api.client.util.Beta;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.Charset;

/**
 * {@link Beta} <br>
 * Mock for {@link JsonFactory}.
 *
 * <p>Implementation is thread-safe.
 *
 * @author rmistry@google.com (Ravi Mistry)
 * @since 1.15 (since 1.11 as com.google.api.client.testing.http.json.MockJsonFactory)
 */
@Beta
public class MockJsonFactory extends JsonFactory {

  @Override
  public JsonParser createJsonParser(InputStream in) throws IOException {
    return new MockJsonParser(this);
  }

  @Override
  public JsonParser createJsonParser(InputStream in, Charset charset) throws IOException {
    return new MockJsonParser(this);
  }

  @Override
  public JsonParser createJsonParser(String value) throws IOException {
    return new MockJsonParser(this);
  }

  @Override
  public JsonParser createJsonParser(Reader reader) throws IOException {
    return new MockJsonParser(this);
  }

  @Override
  public JsonGenerator createJsonGenerator(OutputStream out, Charset enc) throws IOException {
    return new MockJsonGenerator(this);
  }

  @Override
  public JsonGenerator createJsonGenerator(Writer writer) throws IOException {
    return new MockJsonGenerator(this);
  }
}
