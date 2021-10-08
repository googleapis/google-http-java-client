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
import com.google.api.client.util.Preconditions;
import com.google.api.client.util.Types;
import com.google.api.client.util.escape.CharEscapers;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;

/**
 * Implements support for HTTP form content encoding serialization of type {@code
 * application/x-www-form-urlencoded} as specified in the <a href=
 * "http://www.w3.org/TR/REC-html40/interact/forms.html#h-17.13.4.1">HTML 4.0 Specification</a>.
 *
 * <p>Sample usage:
 *
 * <pre>
 * static void setContent(HttpRequest request, Object item) {
 * request.setContent(new UrlEncodedContent(item));
 * }
 * </pre>
 *
 * <p>Implementation is not thread-safe.
 *
 * @author Yaniv Inbar
 * @since 1.0
 */
public class UrlEncodedContent extends AbstractHttpContent {

  /** Key name/value data. */
  private Object data;

  /** Use URI Path encoder flag. False by default (use legacy and deprecated escapeUri) */
  private boolean uriPathEncodingFlag;

  /**
   * Initialize the UrlEncodedContent with the legacy and deprecated escapeUri encoder
   *
   * @param data key name/value data
   */
  public UrlEncodedContent(Object data) {
    super(UrlEncodedParser.MEDIA_TYPE);
    setData(data);
    this.uriPathEncodingFlag = false;
  }

  /**
   * Initialize the UrlEncodedContent with or without the legacy and deprecated escapeUri encoder
   *
   * @param data key name/value data
   * @param useUriPathEncoding escapes the string value so it can be safely included in URI path
   *     segments. For details on escaping URIs, see <a
   *     href="http://tools.ietf.org/html/rfc3986#section-2.4">RFC 3986 - section 2.4</a>
   */
  public UrlEncodedContent(Object data, boolean useUriPathEncoding) {
    super(UrlEncodedParser.MEDIA_TYPE);
    setData(data);
    this.uriPathEncodingFlag = useUriPathEncoding;
  }

  @Override
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
            first = appendParam(first, writer, name, repeatedValue, this.uriPathEncodingFlag);
          }
        } else {
          first = appendParam(first, writer, name, value, this.uriPathEncodingFlag);
        }
      }
    }
    writer.flush();
  }

  @Override
  public UrlEncodedContent setMediaType(HttpMediaType mediaType) {
    super.setMediaType(mediaType);
    return this;
  }

  /**
   * Returns the key name/value data or {@code null} for none.
   *
   * @since 1.5
   */
  public final Object getData() {
    return data;
  }

  /**
   * Sets the key name/value data.
   *
   * <p>Overriding is only supported for the purpose of calling the super implementation and
   * changing the return type, but nothing else.
   *
   * @since 1.5
   */
  public UrlEncodedContent setData(Object data) {
    this.data = Preconditions.checkNotNull(data);
    return this;
  }

  /**
   * Returns the URL-encoded content of the given HTTP request, or if none return and set as content
   * a new instance of {@link UrlEncodedContent} (whose {@link #getData()} is an implementation of
   * {@link Map}).
   *
   * @param request HTTP request
   * @return URL-encoded content
   * @throws ClassCastException if the HTTP request has a content defined that is not {@link
   *     UrlEncodedContent}
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

  private static boolean appendParam(
      boolean first, Writer writer, String name, Object value, boolean uriPathEncodingFlag)
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
    String stringValue =
        value instanceof Enum<?> ? FieldInfo.of((Enum<?>) value).getName() : value.toString();

    if (uriPathEncodingFlag) {
      stringValue = CharEscapers.escapeUriPath(stringValue);
    } else {
      stringValue = CharEscapers.escapeUri(stringValue);
    }
    if (stringValue.length() != 0) {
      writer.write("=");
      writer.write(stringValue);
    }
    return first;
  }
}
