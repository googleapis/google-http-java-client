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

import com.google.api.client.util.Preconditions;
import com.google.api.client.util.StringUtils;
import java.io.ByteArrayInputStream;
import java.io.InputStream;

/**
 * Concrete implementation of {@link AbstractInputStreamContent} that generates repeatable input
 * streams based on the contents of byte array.
 *
 * <p>Sample use:
 *
 * <pre>
 * <code>
 * static void setJsonContent(HttpRequest request, byte[] json) {
 * request.setContent(new ByteArrayContent("application/json", json));
 * }
 * </code>
 * </pre>
 *
 * <p>Implementation is not thread-safe.
 *
 * @since 1.4
 * @author moshenko@google.com (Jacob Moshenko)
 */
public final class ByteArrayContent extends AbstractInputStreamContent {

  /** Byte array content. */
  private final byte[] byteArray;

  /** Starting offset into the byte array. */
  private final int offset;

  /** Length of bytes to read from byte array. */
  private final int length;

  /**
   * Constructor from byte array content that has already been encoded.
   *
   * @param type content type or {@code null} for none
   * @param array byte array content
   * @since 1.5
   */
  public ByteArrayContent(String type, byte[] array) {
    this(type, array, 0, array.length);
  }

  /**
   * Constructor from byte array content that has already been encoded, specifying a range of bytes
   * to read from the input byte array.
   *
   * @param type content type or {@code null} for none
   * @param array byte array content
   * @param offset starting offset into the byte array
   * @param length of bytes to read from byte array
   * @since 1.7
   */
  public ByteArrayContent(String type, byte[] array, int offset, int length) {
    super(type);
    this.byteArray = Preconditions.checkNotNull(array);
    Preconditions.checkArgument(
        offset >= 0 && length >= 0 && offset + length <= array.length,
        "offset %s, length %s, array length %s",
        offset,
        length,
        array.length);
    this.offset = offset;
    this.length = length;
  }

  /**
   * Returns a new instance with the UTF-8 encoding (using {@link StringUtils#getBytesUtf8(String)})
   * of the given content string.
   *
   * <p>Sample use:
   *
   * <pre>
   * <code>
   * static void setJsonContent(HttpRequest request, String json) {
   * request.setContent(ByteArrayContent.fromString("application/json", json));
   * }
   * </code>
   * </pre>
   *
   * @param type content type or {@code null} for none
   * @param contentString content string
   * @since 1.5
   */
  public static ByteArrayContent fromString(String type, String contentString) {
    return new ByteArrayContent(type, StringUtils.getBytesUtf8(contentString));
  }

  public long getLength() {
    return length;
  }

  public boolean retrySupported() {
    return true;
  }

  @Override
  public InputStream getInputStream() {
    return new ByteArrayInputStream(byteArray, offset, length);
  }

  @Override
  public ByteArrayContent setType(String type) {
    return (ByteArrayContent) super.setType(type);
  }

  @Override
  public ByteArrayContent setCloseInputStream(boolean closeInputStream) {
    return (ByteArrayContent) super.setCloseInputStream(closeInputStream);
  }
}
