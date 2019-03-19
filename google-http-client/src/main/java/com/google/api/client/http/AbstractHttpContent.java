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

import com.google.api.client.util.Charsets;
import com.google.api.client.util.IOUtils;
import com.google.api.client.util.StreamingContent;
import java.io.IOException;
import java.nio.charset.Charset;

/**
 * Abstract implementation of an HTTP content with typical options.
 *
 * <p>Implementation is not thread-safe.
 *
 * @since 1.5
 * @author Yaniv Inbar
 */
public abstract class AbstractHttpContent implements HttpContent {

  /** Media type used for the Content-Type header or {@code null} for none. */
  private HttpMediaType mediaType;

  /** Cached value for the computed length from {@link #computeLength()}. */
  private long computedLength = -1;

  /**
   * @param mediaType Media type string (for example "type/subtype") this content represents or
   *     {@code null} to leave out. Can also contain parameters like {@code "charset=utf-8"}
   * @since 1.10
   */
  protected AbstractHttpContent(String mediaType) {
    this(mediaType == null ? null : new HttpMediaType(mediaType));
  }

  /**
   * @param mediaType Media type this content represents or {@code null} to leave out
   * @since 1.10
   */
  protected AbstractHttpContent(HttpMediaType mediaType) {
    this.mediaType = mediaType;
  }

  /**
   * Default implementation calls {@link #computeLength()} once and caches it for future
   * invocations, but subclasses may override.
   */
  public long getLength() throws IOException {
    if (computedLength == -1) {
      computedLength = computeLength();
    }
    return computedLength;
  }

  /**
   * Returns the media type to use for the Content-Type header, or {@code null} if unspecified.
   *
   * @since 1.10
   */
  public final HttpMediaType getMediaType() {
    return mediaType;
  }

  /**
   * Sets the media type to use for the Content-Type header, or {@code null} if unspecified.
   *
   * <p>This will also overwrite any previously set parameter of the media type (for example {@code
   * "charset"}), and therefore might change other properties as well.
   *
   * @since 1.10
   */
  public AbstractHttpContent setMediaType(HttpMediaType mediaType) {
    this.mediaType = mediaType;
    return this;
  }

  /**
   * Returns the charset specified in the media type or {@code Charsets#UTF_8} if not specified.
   *
   * @since 1.10
   */
  protected final Charset getCharset() {
    return mediaType == null || mediaType.getCharsetParameter() == null
        ? Charsets.ISO_8859_1
        : mediaType.getCharsetParameter();
  }

  public String getType() {
    return mediaType == null ? null : mediaType.build();
  }

  /**
   * Computes and returns the content length or less than zero if not known.
   *
   * <p>Subclasses may override, but by default this computes the length by calling {@link
   * #computeLength(HttpContent)}.
   */
  protected long computeLength() throws IOException {
    return computeLength(this);
  }

  /** Default implementation returns {@code true}, but subclasses may override. */
  public boolean retrySupported() {
    return true;
  }

  /**
   * Returns the computed content length based using {@link IOUtils#computeLength(StreamingContent)}
   * or instead {@code -1} if {@link HttpContent#retrySupported()} is {@code false} because the
   * stream must not be read twice.
   *
   * @param content HTTP content
   * @return computed content length or {@code -1} if retry is not supported
   * @since 1.14
   */
  public static long computeLength(HttpContent content) throws IOException {
    if (!content.retrySupported()) {
      return -1;
    }
    return IOUtils.computeLength(content);
  }
}
