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
import com.google.api.client.util.GenericData;
import com.google.api.client.util.Types;
import com.google.common.base.Preconditions;
import com.google.common.primitives.UnsignedInteger;
import com.google.common.primitives.UnsignedLong;

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

  /**
   * Flushes any buffered content to the underlying output stream or writer.
   *
   * <p>
   * Upgrade warning: this method now throws an {@link Exception}. In prior version 1.11 it threw an
   * {@link java.io.IOException}.
   * </p>
   */
  public abstract void flush() throws Exception;

  /**
   * Closes the serializer and the underlying output stream or writer, and releases any memory
   * associated with it.
   *
   * <p>
   * Upgrade warning: this method now throws an {@link Exception}. In prior version 1.11 it threw an
   * {@link java.io.IOException}.
   * </p>
   */
  public abstract void close() throws Exception;

  /**
   * Writes a JSON start array character '['.
   *
   * <p>
   * Upgrade warning: this method now throws an {@link Exception}. In prior version 1.11 it threw an
   * {@link java.io.IOException}.
   * </p>
   */
  public abstract void writeStartArray() throws Exception;

  /**
   * Writes a JSON end array character ']'.
   *
   * <p>
   * Upgrade warning: this method now throws an {@link Exception}. In prior version 1.11 it threw an
   * {@link java.io.IOException}.
   * </p>
   */
  public abstract void writeEndArray() throws Exception;

  /**
   * Writes a JSON start object character '{'.
   *
   * <p>
   * Upgrade warning: this method now throws an {@link Exception}. In prior version 1.11 it threw an
   * {@link java.io.IOException}.
   * </p>
   */
  public abstract void writeStartObject() throws Exception;

  /**
   * Writes a JSON end object character '}'.
   *
   * <p>
   * Upgrade warning: this method now throws an {@link Exception}. In prior version 1.11 it threw an
   * {@link java.io.IOException}.
   * </p>
   */
  public abstract void writeEndObject() throws Exception;

  /**
   * Writes a JSON quoted field name.
   *
   * <p>
   * Upgrade warning: this method now throws an {@link Exception}. In prior version 1.11 it threw an
   * {@link java.io.IOException}.
   * </p>
   */
  public abstract void writeFieldName(String name) throws Exception;

  /**
   * Writes a literal JSON null value.
   *
   * <p>
   * Upgrade warning: this method now throws an {@link Exception}. In prior version 1.11 it threw an
   * {@link java.io.IOException}.
   * </p>
   */
  public abstract void writeNull() throws Exception;

  /**
   * Writes a JSON quoted string value.
   *
   * <p>
   * Upgrade warning: this method now throws an {@link Exception}. In prior version 1.11 it threw an
   * {@link java.io.IOException}.
   * </p>
   */
  public abstract void writeString(String value) throws Exception;

  /**
   * Writes a literal JSON boolean value ('true' or 'false').
   *
   * <p>
   * Upgrade warning: this method now throws an {@link Exception}. In prior version 1.11 it threw an
   * {@link java.io.IOException}.
   * </p>
   */
  public abstract void writeBoolean(boolean state) throws Exception;

  /**
   * Writes a JSON int value.
   *
   * <p>
   * Upgrade warning: this method now throws an {@link Exception}. In prior version 1.11 it threw an
   * {@link java.io.IOException}.
   * </p>
   */
  public abstract void writeNumber(int v) throws Exception;

  /**
   * Writes a JSON long value.
   *
   * <p>
   * Upgrade warning: this method now throws an {@link Exception}. In prior version 1.11 it threw an
   * {@link java.io.IOException}.
   * </p>
   */
  public abstract void writeNumber(long v) throws Exception;

  /**
   * Writes a JSON big integer value.
   *
   * <p>
   * Upgrade warning: this method now throws an {@link Exception}. In prior version 1.11 it threw an
   * {@link java.io.IOException}.
   * </p>
   */
  public abstract void writeNumber(BigInteger v) throws Exception;

  /**
   * Writes a JSON unsigned integer value.
   *
   * <p>
   * Upgrade warning: this method now throws an {@link Exception}. In prior version 1.11 it threw an
   * {@link java.io.IOException}.
   * </p>
   *
   * @since 1.9
   */
  public abstract void writeNumber(UnsignedInteger v) throws Exception;

  /**
   * Writes a JSON unsigned long value.
   *
   * <p>
   * Upgrade warning: this method now throws an {@link Exception}. In prior version 1.11 it threw an
   * {@link java.io.IOException}.
   * </p>
   *
   * @since 1.9
   */
  public abstract void writeNumber(UnsignedLong v) throws Exception;

  /**
   * Writes a JSON float value.
   *
   * <p>
   * Upgrade warning: this method now throws an {@link Exception}. In prior version 1.11 it threw an
   * {@link java.io.IOException}.
   * </p>
   */
  public abstract void writeNumber(float v) throws Exception;

  /**
   * Writes a JSON double value.
   *
   * <p>
   * Upgrade warning: this method now throws an {@link Exception}. In prior version 1.11 it threw an
   * {@link java.io.IOException}.
   * </p>
   */
  public abstract void writeNumber(double v) throws Exception;

  /**
   * Writes a JSON big decimal value.
   *
   * <p>
   * Upgrade warning: this method now throws an {@link Exception}. In prior version 1.11 it threw an
   * {@link java.io.IOException}.
   * </p>
   */
  public abstract void writeNumber(BigDecimal v) throws Exception;

  /**
   * Writes a JSON numeric value that has already been encoded properly.
   *
   * <p>
   * Upgrade warning: this method now throws an {@link Exception}. In prior version 1.11 it threw an
   * {@link java.io.IOException}.
   * </p>
   */
  public abstract void writeNumber(String encodedValue) throws Exception;

  /**
   * Serializes the given JSON value object, or if {@code value} is {@code null} it does no
   * serialization.
   *
   * <p>
   * Upgrade warning: this method now throws an {@link Exception}. In prior version 1.11 it threw an
   * {@link java.io.IOException}.
   * </p>
   */
  public final void serialize(Object value) throws Exception {
    serialize(false, value);
  }

  private void serialize(boolean isJsonString, Object value) throws Exception {
    if (value == null) {
      return;
    }
    Class<?> valueClass = value.getClass();
    if (Data.isNull(value)) {
      writeNull();
    } else if (value instanceof String) {
      writeString((String) value);
    } else if (value instanceof Number) {
      if (isJsonString) {
        writeString(value.toString());
      } else if (value instanceof BigDecimal) {
        writeNumber((BigDecimal) value);
      } else if (value instanceof BigInteger) {
        writeNumber((BigInteger) value);
      } else if (value instanceof UnsignedInteger) {
        writeNumber((UnsignedInteger) value);
      } else if (value instanceof UnsignedLong) {
        writeNumber((UnsignedLong) value);
      } else if (value instanceof Long) {
        writeNumber((Long) value);
      } else if (value instanceof Float) {
        float floatValue = ((Number) value).floatValue();
        Preconditions.checkArgument(!Float.isInfinite(floatValue) && !Float.isNaN(floatValue));
        writeNumber(floatValue);
      } else if (value instanceof Integer || value instanceof Short || value instanceof Byte) {
        writeNumber(((Number) value).intValue());
      } else {
        double doubleValue = ((Number) value).doubleValue();
        Preconditions.checkArgument(!Double.isInfinite(doubleValue) && !Double.isNaN(doubleValue));
        writeNumber(doubleValue);
      }
    } else if (value instanceof Boolean) {
      writeBoolean((Boolean) value);
    } else if (value instanceof DateTime) {
      writeString(((DateTime) value).toStringRfc3339());
    } else if (value instanceof Iterable<?> || valueClass.isArray()) {
      writeStartArray();
      for (Object o : Types.iterableOf(value)) {
        serialize(isJsonString, o);
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
      // only inspect fields of POJO (possibly extends GenericData) but not generic Map
      boolean isMapNotGenericData = value instanceof Map<?, ?> && !(value instanceof GenericData);
      ClassInfo classInfo = isMapNotGenericData ? null : ClassInfo.of(valueClass);
      for (Map.Entry<String, Object> entry : Data.mapOf(value).entrySet()) {
        Object fieldValue = entry.getValue();
        if (fieldValue != null) {
          String fieldName = entry.getKey();
          boolean isJsonStringForField;
          if (isMapNotGenericData) {
            isJsonStringForField = isJsonString;
          } else {
            Field field = classInfo.getField(fieldName);
            isJsonStringForField = field != null && field.getAnnotation(JsonString.class) != null;
          }
          writeFieldName(fieldName);
          serialize(isJsonStringForField, fieldValue);
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
