/*
 * Copyright (c) 2012 Google Inc.
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
import com.google.api.client.util.StreamingContent;
import java.io.IOException;
import java.io.OutputStream;

/**
 * Streaming content based on an HTTP encoding.
 *
 * <p>Implementation is thread-safe only if the streaming content and HTTP encoding are thread-safe.
 *
 * @since 1.14
 * @author Yaniv Inbar
 */
public final class HttpEncodingStreamingContent implements StreamingContent {

  /** Streaming content. */
  private final StreamingContent content;

  /** HTTP encoding. */
  private final HttpEncoding encoding;

  /**
   * @param content streaming content
   * @param encoding HTTP encoding
   */
  public HttpEncodingStreamingContent(StreamingContent content, HttpEncoding encoding) {
    this.content = Preconditions.checkNotNull(content);
    this.encoding = Preconditions.checkNotNull(encoding);
  }

  public void writeTo(OutputStream out) throws IOException {
    encoding.encode(content, out);
  }

  /** Returns the streaming content. */
  public StreamingContent getContent() {
    return content;
  }

  /** Returns the HTTP encoding. */
  public HttpEncoding getEncoding() {
    return encoding;
  }
}
