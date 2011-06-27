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

import java.io.IOException;
import java.io.OutputStream;

/**
 * Abstract implementation of an HTTP content with typical options.
 *
 * <p>
 * Implementation is not thread-safe.
 * </p>
 *
 * @since 1.5
 * @author Yaniv Inbar
 */
public abstract class AbstractHttpContent implements HttpContent {

  /** Cached value for the computed length from {@link #computeLength()}. */
  private long computedLength = -1;

  /** Default implementation returns {@code null}, but subclasses may override. */
  public String getEncoding() {
    return null;
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
   * Computes and returns the content length or less than zero if not known.
   *
   * <p>
   * Subclasses may override, but by default this computes the length by calling
   * {@link #writeTo(OutputStream)} with an output stream that does not process the bytes written,
   * but only retains the count of bytes. If {@link #retrySupported()} is {@code false}, it will
   * instead return {@code -1}.
   * </p>
   */
  protected long computeLength() throws IOException {
    if (!retrySupported()) {
      return -1;
    }
    ByteCountingOutputStream countingStream = new ByteCountingOutputStream();
    writeTo(countingStream);
    return countingStream.count;
  }

  /** Default implementation returns {@code true}, but subclasses may override. */
  public boolean retrySupported() {
    return true;
  }
}
