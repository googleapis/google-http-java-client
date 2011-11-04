/*
 * Copyright (c) 2010 Google Inc.
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

import com.google.api.client.util.ClassInfo;
import com.google.api.client.util.Data;
import com.google.api.client.util.DateTime;
import com.google.api.client.util.FieldInfo;
import com.google.api.client.util.Types;

import java.io.IOException;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Map;

/**
 * Abstract low-level JSON serializer.
 *
 * <p>
 * Implementation has no fields and therefore thread-safe, but sub-classes are not necessarily
 * thread-safe.
 * </p>
 *
 * @since 1.3
 * @author Yaniv Inbar
 */
public abstract class JsonGenerator {

  /** Returns the JSON factory from which this generator was created. */
  public abstract JsonFactory getFactory();

  /** Flushes any buffered content to the underlying output stream or writer. */
  public abstract void flush() throws IOException;

  /**
   * Closes the serializer and the underlying output stream or writer, and releases any memory
   * associated with it.
   */
  public abstract void close() throws IOException;

  /** Writes a JSON start array character '['. */
  public abstract void writeStartArray() throws IOException;

  /** Writes a JSON end array character ']'. */
  public abstract void writeEndArray() throws IOException;

  /** Writes a JSON start object character '{'. */
  public abstract void writeStartObject() throws IOException;

  /** Writes a JSON end object character '}'. */
  public abstract void writeEndObject() throws IOException;

  /** Writes a JSON quoted field name. */
  public abstract void writeFieldName(String name) throws IOException;

  /** Writes a literal JSON null value. */
  public abstract void writeNull() throws IOException;

  /** Writes a JSON quoted string value. */
  public abstract void writeString(String value) throws IOException;

  /** Writes a literal JSON boolean value ('true' or 'false'). */
  public abstract void writeBoolean(boolean state) throws IOException;

  /** Writes a JSON int value. */
  public abstract void writeNumber(int v) throws IOException;

  /** Writes a JSON long value. */
  public abstract void writeNumber(long v) throws IOException;

  /** Writes a JSON big integer value. */
  public abstract void writeNumber(BigInteger v) throws IOException;

  /** Writes a JSON float value. */
  public abstract void writeNumber(float v) throws IOException;

  /** Writes a JSON double value. */
  public abstract void writeNumber(double v) throws IOException;

  /** Writes a JSON big decimal value. */
  public abstract void writeNumber(BigDecimal v) throws IOException;

  /** Writes a JSON numeric value that has already been encoded properly. */
  public abstract void writeNumber(String encodedValue) throws IOException;

  /** Serializes the given JSON value object. */
  public final void serialize(Object value) throws IOException {
    if (value == null) {
      return;
    }
    Class<?> valueClass = value.getClass();
    if (Data.isNull(value)) {
      writeNull();
    } else if (value instanceof String) {
      writeString((String) value);
    } else if (value instanceof BigDecimal) {
      writeNumber((BigDecimal) value);
    } else if (value instanceof BigInteger) {
      writeNumber((BigInteger) value);
    } else if (value instanceof Double) {
      // TODO(yanivi): double: what about +- infinity?
      writeNumber((Double) value);
    } else if (value instanceof Long) {
      writeNumber((Long) value);
    } else if (value instanceof Float) {
      // TODO(yanivi): what about +- infinity?
      writeNumber((Float) value);
    } else if (value instanceof Integer || value instanceof Short || value instanceof Byte) {
      writeNumber(((Number) value).intValue());
    } else if (value instanceof Boolean) {
      writeBoolean((Boolean) value);
    } else if (value instanceof DateTime) {
      writeString(((DateTime) value).toStringRfc3339());
    } else if (value instanceof Iterable<?> || valueClass.isArray()) {
      writeStartArray();
      for (Object o : Types.iterableOf(value)) {
        serialize(o);
      }
      writeEndArray();
    } else if (valueClass.isEnum()) {
      String name = FieldInfo.of((Enum<?>) value).getName();
      if (name == null) {
        writeNull();
      } else {
        writeString(name);
      }
    } else {
      writeStartObject();
      ClassInfo classInfo = ClassInfo.of(valueClass);
      for (Map.Entry<String, Object> entry : Data.mapOf(value).entrySet()) {
        Object fieldValue = entry.getValue();
        if (fieldValue != null) {
          String fieldName = entry.getKey();
          if (fieldValue instanceof Number) {
            Field field = classInfo.getField(fieldName);
            if (field != null && field.getAnnotation(JsonString.class) != null) {
              fieldValue = fieldValue.toString();
            }
          }
          writeFieldName(fieldName);
          serialize(fieldValue);
        }
      }
      writeEndObject();
    }
  }

  /**
   * Requests that the output be pretty printed (by default it is not).
   *
   * <p>
   * Default implementation does nothing, but implementations may override to provide actual pretty
   * printing.
   * </p>
   *
   * @throws IOException possible I/O exception (unused in default implementation)
   *
   * @since 1.6
   */
  public void enablePrettyPrint() throws IOException {
  }
}
