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

package com.google.api.client.extensions.android.json;

import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.JsonParser;
import com.google.api.client.json.JsonToken;
import com.google.api.client.util.Beta;
import com.google.api.client.util.Preconditions;

import android.annotation.TargetApi;
import android.util.JsonReader;

import java.io.EOFException;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

/**
 * {@link Beta} <br/>
 * Low-level JSON serializer implementation based on GSON.
 *
 * <p>
 * Implementation is not thread-safe.
 * </p>
 *
 * @author Yaniv Inbar
 */
@TargetApi(11)
@Beta
class AndroidJsonParser extends JsonParser {
  private final JsonReader reader;
  private final AndroidJsonFactory factory;

  private List<String> currentNameStack = new ArrayList<String>();
  private JsonToken currentToken;
  private String currentText;

  AndroidJsonParser(AndroidJsonFactory factory, JsonReader reader) {
    this.factory = factory;
    this.reader = reader;
    // lenient to allow top-level values of any type
    reader.setLenient(true);
  }

  @Override
  public void close() throws IOException {
    reader.close();
  }

  @Override
  public String getCurrentName() {
    return currentNameStack.isEmpty() ? null : currentNameStack.get(currentNameStack.size() - 1);
  }

  @Override
  public JsonToken getCurrentToken() {
    return currentToken;
  }

  @Override
  public JsonFactory getFactory() {
    return factory;
  }

  @Override
  public byte getByteValue() {
    checkNumber();
    return Byte.valueOf(currentText);
  }

  @Override
  public short getShortValue() {
    checkNumber();
    return Short.valueOf(currentText);
  }


  @Override
  public int getIntValue() {
    checkNumber();
    return Integer.valueOf(currentText);
  }

  @Override
  public float getFloatValue() {
    checkNumber();
    return Float.valueOf(currentText);
  }

  @Override
  public BigInteger getBigIntegerValue() {
    checkNumber();
    return new BigInteger(currentText);
  }

  @Override
  public BigDecimal getDecimalValue() {
    checkNumber();
    return new BigDecimal(currentText);
  }

  @Override
  public double getDoubleValue() {
    checkNumber();
    return Double.valueOf(currentText);
  }

  @Override
  public long getLongValue() {
    checkNumber();
    return Long.valueOf(currentText);
  }

  private void checkNumber() {
    Preconditions.checkArgument(
        currentToken == JsonToken.VALUE_NUMBER_INT || currentToken == JsonToken.VALUE_NUMBER_FLOAT);
  }

  @Override
  public String getText() {
    return currentText;
  }

  @Override
  public JsonToken nextToken() throws IOException {
    if (currentToken != null) {
      switch (currentToken) {
        case START_ARRAY:
          reader.beginArray();
          currentNameStack.add(null);
          break;
        case START_OBJECT:
          reader.beginObject();
          currentNameStack.add(null);
          break;
        default:
          break;
      }
    }
    // work around bug in GSON parser that it throws an EOFException for an empty document
    // see http://code.google.com/p/google-gson/issues/detail?id=330
    android.util.JsonToken peek;
    try {
      peek = reader.peek();
    } catch (EOFException e) {
      peek = android.util.JsonToken.END_DOCUMENT;
    }
    switch (peek) {
      case BEGIN_ARRAY:
        currentText = "[";
        currentToken = JsonToken.START_ARRAY;
        break;
      case END_ARRAY:
        currentText = "]";
        currentToken = JsonToken.END_ARRAY;
        currentNameStack.remove(currentNameStack.size() - 1);
        reader.endArray();
        break;
      case BEGIN_OBJECT:
        currentText = "{";
        currentToken = JsonToken.START_OBJECT;
        break;
      case END_OBJECT:
        currentText = "}";
        currentToken = JsonToken.END_OBJECT;
        currentNameStack.remove(currentNameStack.size() - 1);
        reader.endObject();
        break;
      case BOOLEAN:
        if (reader.nextBoolean()) {
          currentText = "true";
          currentToken = JsonToken.VALUE_TRUE;
        } else {
          currentText = "false";
          currentToken = JsonToken.VALUE_FALSE;
        }
        break;
      case NULL:
        currentText = "null";
        currentToken = JsonToken.VALUE_NULL;
        reader.nextNull();
        break;
      case STRING:
        currentText = reader.nextString();
        currentToken = JsonToken.VALUE_STRING;
        break;
      case NUMBER:
        currentText = reader.nextString();
        currentToken = currentText.indexOf('.') == -1
            ? JsonToken.VALUE_NUMBER_INT : JsonToken.VALUE_NUMBER_FLOAT;
        break;
      case NAME:
        currentText = reader.nextName();
        currentToken = JsonToken.FIELD_NAME;
        currentNameStack.set(currentNameStack.size() - 1, currentText);
        break;
      default:
        currentText = null;
        currentToken = null;
    }
    return currentToken;
  }

  @Override
  public JsonParser skipChildren() throws IOException {
    if (currentToken != null) {
      switch (currentToken) {
        case START_ARRAY:
          reader.skipValue();
          currentText = "]";
          currentToken = JsonToken.END_ARRAY;
          break;
        case START_OBJECT:
          reader.skipValue();
          currentText = "}";
          currentToken = JsonToken.END_OBJECT;
          break;
        default:
          break;
      }
    }
    return this;
  }
}
