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

import java.io.IOException;
import java.io.OutputStream;
import java.util.zip.GZIPOutputStream;

/**
 * Serializes another source of HTTP content using GZip compression.
 *
 * <p>
 * Implementation is not thread-safe.
 * </p>
 *
 * @author Yaniv Inbar
 */
final class GZipContent extends AbstractHttpContent {

  private final HttpContent httpContent;

  /**
   * @param httpContent another source of HTTP content
   */
  GZipContent(HttpContent httpContent, String contentType) {
    super(contentType);
    this.httpContent = httpContent;
  }

  /**
   * Writes the content to the given output stream and closes the output stream.
   *
   * <p>
   * The output stream is closed by calling {@link GZIPOutputStream#close} to avoid a resource leak
   * caused due to an instance of {@link java.util.zip.Deflater} being left open. See Bug <a
   * href='http://code.google.com/p/google-http-java-client/issues/detail?id=61'>61</a> for more
   * information.
   * </p>
   */
  public void writeTo(OutputStream out) throws IOException {
    GZIPOutputStream zipper = new GZIPOutputStream(out);
    httpContent.writeTo(zipper);
    zipper.close();
  }

  @Override
  public String getEncoding() {
    return "gzip";
  }

  @Override
  public boolean retrySupported() {
    return httpContent.retrySupported();
  }

  @Override
  public GZipContent setMediaType(HttpMediaType mediaType) {
    super.setMediaType(mediaType);
    return this;
  }
}
