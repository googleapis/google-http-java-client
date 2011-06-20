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

package com.google.api.client.http;

import com.google.api.client.util.Strings;
import com.google.common.base.Preconditions;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

/**
 * Concrete implementation of {@link AbstractInputStreamContent} that generates repeatable input
 * streams based on the contents of byte array.
 *
 * <p>
 * Sample use:
 * </p>
 *
 * <pre>
 * <code>
  static void setJsonContent(HttpRequest request, byte[] json) {
    request.setContent(new ByteArrayContent("application/json", json));
  }
 * </code>
 * </pre>
 *
 * <p>
 * Implementation is not thread-safe.
 * </p>
 *
 * @since 1.4
 * @author moshenko@google.com (Jacob Moshenko)
 */
public final class ByteArrayContent extends AbstractInputStreamContent {

  private final byte[] byteArray;

  @Deprecated
  private static final byte[] EMPTY_ARRAY = new byte[] {};

  /**
   * @param array Data source for creating input streams.
   * @deprecated (scheduled to be removed in 1.6) Use {@link #ByteArrayContent(String, byte[])}
   */
  @Deprecated
  public ByteArrayContent(byte[] array) {
    this(null, array);
  }

  /**
   * Create an instance from the byte contents of the string. This assumes that the string is
   * encoded in UTF-8 and uses {@code Strings.toBytesUtf8()} to perform the conversion.
   *
   * @param contentString String to use as the source data for creating input streams
   * @deprecated (scheduled to be removed in 1.6) Use {@link #fromString(String, String)}
   */
  @Deprecated
  public ByteArrayContent(String contentString) {
    this(Strings.toBytesUtf8(contentString));
  }

  /**
   * Create an instance with no data.
   *
   * @deprecated (scheduled to be removed in 1.6) Use {@code new ByteArrayContent(null, new
   *             byte[]{})}
   */
  @Deprecated
  public ByteArrayContent() {
    this(EMPTY_ARRAY);
  }

  /**
   * @param type content type or {@code null} for none
   * @param array byte array content
   * @since 1.5
   */
  public ByteArrayContent(String type, byte[] array) {
    super(type);
    this.byteArray = Preconditions.checkNotNull(array);
  }

  /**
   * Returns a new instance with the UTF-8 encoding (using {@code Strings.toBytesUtf8()}) of the
   * given content string.
   * <p>
   * Sample use:
   * </p>
   *
   * <pre>
   * <code>
  static void setJsonContent(HttpRequest request, String json) {
    request.setContent(ByteArrayContent.fromString("application/json", json));
  }
   * </code>
   * </pre>
   *
   * @param type content type or {@code null} for none
   * @param contentString content string
   * @since 1.5
   */
  public static ByteArrayContent fromString(String type, String contentString) {
    return new ByteArrayContent(type, Strings.toBytesUtf8(contentString));
  }

  public long getLength() {
    return byteArray.length;
  }

  public boolean retrySupported() {
    return true;
  }

  @Override
  protected InputStream getInputStream() {
    return new ByteArrayInputStream(byteArray);
  }

  @Override
  public ByteArrayContent setEncoding(String encoding) {
    return (ByteArrayContent) super.setEncoding(encoding);
  }

  @Override
  public ByteArrayContent setType(String type) {
    return (ByteArrayContent) super.setType(type);
  }
}
