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
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

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

  private static final Logger LOGGER = Logger.getLogger(UrlEncodedContent.class.getName());

  /** Key name/value data. */
  private Object data;

  /**
   * <p>
   * Upgrade warning: prior to version 1.7, the {@code data} parameter could be {@code null} but now
   * {@code null} is not allowed and instead a new instance of an implementation of {@link Map} will
   * be created. In version 1.8 a {@link NullPointerException} will be thrown instead.
   * </p>
   *
   * @param data key name/value data
   */
  public UrlEncodedContent(Object data) {
    super(new HttpMediaType(UrlEncodedParser.CONTENT_TYPE).setCharsetParameter(Charsets.UTF_8));
    setData(data);
  }

  public void writeTo(OutputStream out) throws IOException {
    Writer writer = new BufferedWriter(new OutputStreamWriter(out, getCharset()));
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
   * Sets the content type or {@code null} for none. Will override any pre-set media type parameter.
   *
   * <p>
   * Default value is {@link UrlEncodedParser#CONTENT_TYPE}.
   * </p>
   * <p>
   * Overriding is only supported for the purpose of calling the super implementation and changing
   * the return type, but nothing else.
   * </p>
   *
   * @since 1.5
   * @deprecated (scheduled to be removed in 1.11) Use {@link #setMediaType(HttpMediaType)} instead.
   */
  @Deprecated
  public UrlEncodedContent setType(String type) {
    setMediaType(new HttpMediaType(type));
    return this;
  }

  @Override
  public UrlEncodedContent setMediaType(HttpMediaType mediaType) {
    super.setMediaType(mediaType);
    return this;
  }

  /**
   * Returns the key name/value data or {@code null} for none.
   *
   * <p>
   * Upgrade warning: prior to version 1.7 this could return {@code null} but now it always returns
   * a {@code non-null} value. Also overriding this method is no longer supported, and will be made
   * final in 1.8.
   * </p>
   *
   * @since 1.5
   */
  public Object getData() {
    return data;
  }

  /**
   * Sets the key name/value data.
   *
   * <p>
   * Overriding is only supported for the purpose of calling the super implementation and changing
   * the return type, but nothing else.
   * </p>
   * <p>
   * Upgrade warning: prior to version 1.7, the {@code data} parameter could be {@code null} but now
   * {@code null} is not allowed and instead a new instance of an implementation of {@link Map} will
   * be created. In version 1.8 a {@link NullPointerException} will be thrown instead.
   * </p>
   *
   * @since 1.5
   */
  public UrlEncodedContent setData(Object data) {
    if (data == null) {
      LOGGER.warning("UrlEncodedContent.setData(null) no longer supported");
      data = new HashMap<String, Object>();
    }
    this.data = data;
    return this;
  }

  /**
   * Returns the URL-encoded content of the given HTTP request, or if none return and set as content
   * a new instance of {@link UrlEncodedContent} (whose {@link #getData()} is an implementation of
   * {@link Map}).
   *
   * @param request HTTP request
   * @return URL-encoded content
   * @throws ClassCastException if the HTTP request has a content defined that is not
   *         {@link UrlEncodedContent}
   * @since 1.7
   */
  public static UrlEncodedContent getContent(HttpRequest request) {
    HttpContent content = request.getContent();
    if (content != null) {
      return (UrlEncodedContent) content;
    }
    UrlEncodedContent result = new UrlEncodedContent(new HashMap<String, Object>());
    request.setContent(result);
    return result;
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
