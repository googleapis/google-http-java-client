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
import com.google.api.client.json.JsonParser;
import com.google.api.client.json.JsonToken;
import com.google.api.client.util.Beta;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;

/**
 * {@link Beta} <br>
 * Mock for {@link JsonParser}.
 *
 * <p>Implementation is thread-safe.
 *
 * @author rmistry@google.com (Ravi Mistry)
 * @since 1.15 (since 1.11 as com.google.api.client.testing.http.json.MockJsonParser)
 */
@Beta
public class MockJsonParser extends JsonParser {

  private boolean isClosed;

  private final JsonFactory factory;

  MockJsonParser(JsonFactory factory) {
    this.factory = factory;
  }

  @Override
  public JsonFactory getFactory() {
    return factory;
  }

  @Override
  public void close() throws IOException {
    isClosed = true;
  }

  @Override
  public JsonToken nextToken() throws IOException {
    return null;
  }

  @Override
  public JsonToken getCurrentToken() {
    return null;
  }

  @Override
  public String getCurrentName() throws IOException {
    return null;
  }

  @Override
  public JsonParser skipChildren() throws IOException {
    return null;
  }

  @Override
  public String getText() throws IOException {
    return null;
  }

  @Override
  public byte getByteValue() throws IOException {
    return 0;
  }

  @Override
  public short getShortValue() throws IOException {
    return 0;
  }

  @Override
  public int getIntValue() throws IOException {
    return 0;
  }

  @Override
  public float getFloatValue() throws IOException {
    return 0;
  }

  @Override
  public long getLongValue() throws IOException {
    return 0;
  }

  @Override
  public double getDoubleValue() throws IOException {
    return 0;
  }

  @Override
  public BigInteger getBigIntegerValue() throws IOException {
    return null;
  }

  @Override
  public BigDecimal getDecimalValue() throws IOException {
    return null;
  }

  /**
   * Returns whether {@link #close()} was called.
   *
   * @since 1.15
   */
  public boolean isClosed() {
    return isClosed;
  }
}
