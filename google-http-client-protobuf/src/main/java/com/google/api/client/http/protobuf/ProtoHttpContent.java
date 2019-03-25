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

package com.google.api.client.http.protobuf;

import com.google.api.client.http.AbstractHttpContent;
import com.google.api.client.http.HttpMediaType;
import com.google.api.client.protobuf.ProtocolBuffers;
import com.google.api.client.util.Beta;
import com.google.api.client.util.Preconditions;
import com.google.protobuf.MessageLite;
import java.io.IOException;
import java.io.OutputStream;

/**
 * {@link Beta} <br>
 * Serializes of a protocol buffer message to HTTP content.
 *
 * <p>Sample usage:
 *
 * <pre>
 * static HttpRequest buildPostRequest(
 * HttpRequestFactory requestFactory, GenericUrl url, MessageLite message) throws IOException {
 * return requestFactory.buildPostRequest(url, new ProtoHttpContent(message));
 * }
 * </pre>
 *
 * <p>Implementation is not thread-safe.
 *
 * @since 1.5
 * @author Yaniv Inbar
 */
@Beta
public class ProtoHttpContent extends AbstractHttpContent {

  /** Message to serialize. */
  private final MessageLite message;

  /** @param message message to serialize */
  public ProtoHttpContent(MessageLite message) {
    super(ProtocolBuffers.CONTENT_TYPE);
    this.message = Preconditions.checkNotNull(message);
  }

  @Override
  public long getLength() throws IOException {
    return message.getSerializedSize();
  }

  public void writeTo(OutputStream out) throws IOException {
    message.writeTo(out);
    out.flush();
  }

  /** Returns the message to serialize. */
  public final MessageLite getMessage() {
    return message;
  }

  @Override
  public ProtoHttpContent setMediaType(HttpMediaType mediaType) {
    return (ProtoHttpContent) super.setMediaType(mediaType);
  }
}
