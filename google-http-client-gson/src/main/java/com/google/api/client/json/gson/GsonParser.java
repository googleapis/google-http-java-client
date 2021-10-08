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
import com.google.api.client.json.JsonParser;
import com.google.api.client.json.JsonToken;
import com.google.gson.stream.JsonReader;
import java.io.EOFException;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

/**
 * Low-level JSON serializer implementation based on GSON.
 *
 * <p>Implementation is not thread-safe.
 *
 * @author Yaniv Inbar
 */
class GsonParser extends JsonParser {
  private final JsonReader reader;
  private final GsonFactory factory;

  private List<String> currentNameStack = new ArrayList<String>();
  private JsonToken currentToken;
  private String currentText;

  GsonParser(GsonFactory factory, JsonReader reader) {
    this.factory = factory;
    this.reader = reader;
    reader.setLenient(false);
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
  public byte getByteValue() throws IOException {
    checkNumber();
    return Byte.parseByte(currentText);
  }

  @Override
  public short getShortValue() throws IOException {
    checkNumber();
    return Short.parseShort(currentText);
  }

  @Override
  public int getIntValue() throws IOException {
    checkNumber();
    return Integer.parseInt(currentText);
  }

  @Override
  public float getFloatValue() throws IOException {
    checkNumber();
    return Float.parseFloat(currentText);
  }

  @Override
  public BigInteger getBigIntegerValue() throws IOException {
    checkNumber();
    return new BigInteger(currentText);
  }

  @Override
  public BigDecimal getDecimalValue() throws IOException {
    checkNumber();
    return new BigDecimal(currentText);
  }

  @Override
  public double getDoubleValue() throws IOException {
    checkNumber();
    return Double.parseDouble(currentText);
  }

  @Override
  public long getLongValue() throws IOException {
    checkNumber();
    return Long.parseLong(currentText);
  }

  private void checkNumber() throws IOException {
    if (currentToken != JsonToken.VALUE_NUMBER_INT
        && currentToken != JsonToken.VALUE_NUMBER_FLOAT) {
      throw new IOException("Token is not a number");
    }
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
    // see https://github.com/google/gson/issues/330
    com.google.gson.stream.JsonToken peek;
    try {
      peek = reader.peek();
    } catch (EOFException e) {
      peek = com.google.gson.stream.JsonToken.END_DOCUMENT;
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
        currentToken =
            currentText.indexOf('.') == -1
                ? JsonToken.VALUE_NUMBER_INT
                : JsonToken.VALUE_NUMBER_FLOAT;
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
