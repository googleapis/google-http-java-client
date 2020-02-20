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

import com.google.api.client.util.ByteStreams;
import com.google.api.client.util.IOUtils;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Serializes HTTP request content from an input stream into an output stream.
 *
 * <p>The {@link #type} field is required. Subclasses should implement the {@link #getLength()},
 * {@link #getInputStream()}, and {@link #retrySupported()} for their specific type of input stream.
 * By default, all content is read from the input stream. If instead you want to limit the maximum
 * amount of content read from the input stream, you may use {@link ByteStreams#limit(InputStream,
 * long)}.
 *
 * <p>Implementations don't need to be thread-safe.
 *
 * @since 1.4
 * @author moshenko@google.com (Jacob Moshenko)
 */
public abstract class AbstractInputStreamContent implements HttpContent {

  /** Content type or {@code null} for none. */
  private String type;

  /**
   * Whether the input stream should be closed at the end of {@link #writeTo}. Default is {@code
   * true}.
   */
  private boolean closeInputStream = true;

  /**
   * @param type Content type or {@code null} for none
   * @since 1.5
   */
  public AbstractInputStreamContent(String type) {
    setType(type);
  }

  /**
   * Return an input stream for the specific implementation type of {@link
   * AbstractInputStreamContent}. If the specific implementation will return {@code true} for {@link
   * #retrySupported()} this should be a factory function which will create a new {@link
   * InputStream} from the source data whenever invoked.
   *
   * @since 1.7
   */
  public abstract InputStream getInputStream() throws IOException;

  public void writeTo(OutputStream out) throws IOException {
    IOUtils.copy(getInputStream(), out, closeInputStream);
    out.flush();
  }

  public String getType() {
    return type;
  }

  /**
   * Returns whether the input stream should be closed at the end of {@link #writeTo}. Default is
   * {@code true}.
   *
   * @since 1.7
   */
  public final boolean getCloseInputStream() {
    return closeInputStream;
  }

  /**
   * Sets the content type or {@code null} for none. Subclasses should override by calling super.
   *
   * @since 1.5
   */
  public AbstractInputStreamContent setType(String type) {
    this.type = type;
    return this;
  }

  /**
   * Sets whether the input stream should be closed at the end of {@link #writeTo}. Default is
   * {@code true}. Subclasses should override by calling super.
   *
   * @since 1.7
   */
  public AbstractInputStreamContent setCloseInputStream(boolean closeInputStream) {
    this.closeInputStream = closeInputStream;
    return this;
  }
}
