/*
 * Copyright 2024 Google LLC
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

package com.google.api.client.http.apache.v5;

import com.google.api.client.util.Preconditions;
import com.google.api.client.util.StreamingContent;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import org.apache.hc.core5.http.io.entity.AbstractHttpEntity;

/**
 * Translation class to make google-http-client entity conform with Apache 5.x {@link
 * AbstractHttpEntity}
 */
final class Apache5ContentEntity extends AbstractHttpEntity {

  /** Content length or less than zero if not known. */
  private final long contentLength;

  /** Streaming content. */
  private final StreamingContent streamingContent;

  /**
   * @param contentLength content length or less than zero if not known
   * @param streamingContent streaming content
   */
  Apache5ContentEntity(
      long contentLength,
      StreamingContent streamingContent,
      String contentType,
      String contentEncoding) {
    super(contentType, contentEncoding, contentLength == -1);
    this.contentLength = contentLength;
    this.streamingContent = Preconditions.checkNotNull(streamingContent);
  }

  @Override
  public InputStream getContent() {
    throw new UnsupportedOperationException();
  }

  @Override
  public long getContentLength() {
    return contentLength;
  }

  @Override
  public boolean isRepeatable() {
    return false;
  }

  @Override
  public boolean isStreaming() {
    return true;
  }

  @Override
  public void writeTo(OutputStream out) throws IOException {
    if (contentLength != 0) {
      streamingContent.writeTo(out);
    }
  }

  @Override
  public void close() throws IOException {}
}
