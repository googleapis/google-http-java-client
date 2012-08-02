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

package com.google.api.client.testing.http.json;

import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.JsonGenerator;
import com.google.common.primitives.UnsignedInteger;
import com.google.common.primitives.UnsignedLong;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;

/**
 * Mock for {@link JsonGenerator}.
 *
 * <p>
 * Implementation is thread-safe.
 * </p>
 *
 * @author rmistry@google.com (Ravi Mistry)
 * @since 1.11
 */
public class MockJsonGenerator extends JsonGenerator {

  private final JsonFactory factory;

  MockJsonGenerator(JsonFactory factory) {
    this.factory = factory;
  }

  @Override
  public JsonFactory getFactory() {
    return factory;
  }

  @Override
  public void flush() throws IOException {
  }

  @Override
  public void close() throws IOException {
  }

  @Override
  public void writeStartArray() throws IOException {
  }

  @Override
  public void writeEndArray() throws IOException {
  }

  @Override
  public void writeStartObject() throws IOException {
  }

  @Override
  public void writeEndObject() throws IOException {
  }

  @Override
  public void writeFieldName(String name) throws IOException {
  }

  @Override
  public void writeNull() throws IOException {
  }

  @Override
  public void writeString(String value) throws IOException {
  }

  @Override
  public void writeBoolean(boolean state) throws IOException {
  }

  @Override
  public void writeNumber(int v) throws IOException {
  }

  @Override
  public void writeNumber(long v) throws IOException {
  }

  @Override
  public void writeNumber(BigInteger v) throws IOException {
  }

  @Override
  public void writeNumber(UnsignedInteger v) throws IOException {
  }

  @Override
  public void writeNumber(UnsignedLong v) throws IOException {
  }

  @Override
  public void writeNumber(float v) throws IOException {
  }

  @Override
  public void writeNumber(double v) throws IOException {
  }

  @Override
  public void writeNumber(BigDecimal v) throws IOException {
  }

  @Override
  public void writeNumber(String encodedValue) throws IOException {
  }
}
