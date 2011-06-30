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

package com.google.api.client.http;

import com.google.api.client.util.Data;
import com.google.api.client.util.FieldInfo;
import com.google.api.client.util.Types;
import com.google.api.client.util.escape.CharEscapers;
import com.google.common.base.Charsets;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Map;

/**
 * Implements support for HTTP form content encoding serialization of type {@code
 * application/x-www-form-urlencoded} as specified in the <a href=
 * "http://www.w3.org/TR/REC-html40/interact/forms.html#h-17.13.4.1">HTML 4.0 Specification</a>.
 *
 * <p>
 * Sample usage:
 * </p>
 *
 * <pre>
 * <code>
  static void setContent(HttpRequest request, Object item) {
    request.setContent(new UrlEncodedContent(item));
  }
 * </code>
 * </pre>
 *
 * <p>
 * Implementation is not thread-safe.
 * </p>
 *
 * @since 1.0
 * @author Yaniv Inbar
 */
public class UrlEncodedContent extends AbstractHttpContent {

  /** Content type. Default value is {@link UrlEncodedParser#CONTENT_TYPE}. */
  private String contentType = UrlEncodedParser.CONTENT_TYPE;

  /** Key name/value data or {@code null} for none. */
  private Object data;

  /**
   * @param data key name/value data or {@code null} for none
   */
  public UrlEncodedContent(Object data) {
    this.data = data;
  }

  public String getType() {
    return contentType;
  }

  public void writeTo(OutputStream out) throws IOException {
    Writer writer = new BufferedWriter(new OutputStreamWriter(out, Charsets.UTF_8));
    boolean first = true;
    for (Map.Entry<String, Object> nameValueEntry : Data.mapOf(data).entrySet()) {
      Object value = nameValueEntry.getValue();
      if (value != null) {
        String name = CharEscapers.escapeUri(nameValueEntry.getKey());
        Class<? extends Object> valueClass = value.getClass();
        if (value instanceof Iterable<?> || valueClass.isArray()) {
          for (Object repeatedValue : Types.iterableOf(value)) {
            first = appendParam(first, writer, name, repeatedValue);
          }
        } else {
          first = appendParam(first, writer, name, value);
        }
      }
    }
    writer.flush();
  }

  /**
   * Sets the content type or {@code null} for none.
   *
   * <p>
   * Defaults to {@link UrlEncodedParser#CONTENT_TYPE}.
   * </p>
   *
   * @since 1.5
   */
  public UrlEncodedContent setType(String type) {
    contentType = type;
    return this;
  }

  /**
   * Returns the key name/value data or {@code null} for none.
   *
   * @since 1.5
   */
  public Object getData() {
    return data;
  }

  /**
   * Sets the key name/value data or {@code null} for none.
   *
   * @since 1.5
   */
  public UrlEncodedContent setData(Object data) {
    this.data = data;
    return this;
  }

  private static boolean appendParam(boolean first, Writer writer, String name, Object value)
      throws IOException {
    // ignore nulls
    if (value == null || Data.isNull(value)) {
      return first;
    }
    // append value
    if (first) {
      first = false;
    } else {
      writer.write("&");
    }
    writer.write(name);
    String stringValue = CharEscapers.escapeUri(
        value instanceof Enum<?> ? FieldInfo.of((Enum<?>) value).getName() : value.toString());
    if (stringValue.length() != 0) {
      writer.write("=");
      writer.write(stringValue);
    }
    return first;
  }
}
