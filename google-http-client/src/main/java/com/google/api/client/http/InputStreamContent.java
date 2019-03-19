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

import com.google.api.client.util.Preconditions;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Concrete implementation of {@link AbstractInputStreamContent} that simply handles the transfer of
 * data from an input stream to an output stream. This should only be used for streams that can not
 * be re-opened and retried. If you have a stream that it is possible to recreate please create a
 * new subclass of {@link AbstractInputStreamContent}.
 *
 * <p>The input stream is guaranteed to be closed at the end of {@link #writeTo(OutputStream)}.
 *
 * <p>Sample use with a URL:
 *
 * <pre>
 * <code>
 * private static void setRequestJpegContent(HttpRequest request, URL jpegUrl) throws IOException {
 * request.setContent(new InputStreamContent("image/jpeg", jpegUrl.openStream()));
 * }
 * </code>
 * </pre>
 *
 * <p>Implementation is not thread-safe.
 *
 * @since 1.0
 * @author Yaniv Inbar
 */
public final class InputStreamContent extends AbstractInputStreamContent {

  /** Content length or less than zero if not known. Defaults to {@code -1}. */
  private long length = -1;

  /** Whether or not retry is supported. Defaults to {@code false}. */
  private boolean retrySupported;

  /** Input stream to read from. */
  private final InputStream inputStream;

  /**
   * @param type Content type or {@code null} for none
   * @param inputStream Input stream to read from
   * @since 1.5
   */
  public InputStreamContent(String type, InputStream inputStream) {
    super(type);
    this.inputStream = Preconditions.checkNotNull(inputStream);
  }

  public long getLength() {
    return length;
  }

  public boolean retrySupported() {
    return retrySupported;
  }

  /**
   * Sets whether or not retry is supported. Defaults to {@code false}.
   *
   * <p>Should be set to {@code true} if {@link #getInputStream} is called to reset to the original
   * position of the input stream.
   *
   * @since 1.7
   */
  public InputStreamContent setRetrySupported(boolean retrySupported) {
    this.retrySupported = retrySupported;
    return this;
  }

  @Override
  public InputStream getInputStream() {
    return inputStream;
  }

  @Override
  public InputStreamContent setType(String type) {
    return (InputStreamContent) super.setType(type);
  }

  @Override
  public InputStreamContent setCloseInputStream(boolean closeInputStream) {
    return (InputStreamContent) super.setCloseInputStream(closeInputStream);
  }

  /**
   * Sets the content length or less than zero if not known.
   *
   * <p>Defaults to {@code -1}.
   *
   * @since 1.5
   */
  public InputStreamContent setLength(long length) {
    this.length = length;
    return this;
  }
}
