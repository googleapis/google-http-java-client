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
import com.google.api.client.protobuf.ProtocolBuffers;
import com.google.common.base.Preconditions;
import com.google.protobuf.MessageLite;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Serializes of a protocol buffer message to HTTP content.
 *
 * <p>
 * Sample usage:
 * </p>
 *
 * <pre>
 * <code>
  static HttpRequest buildPostRequest(
      HttpRequestFactory requestFactory, GenericUrl url, MessageLite message) throws IOException {
    return requestFactory.buildPostRequest(url, new ProtoHttpContent(message));
  }
 * </code>
 * </pre>
 *
 * <p>
 * Implementation is not thread-safe.
 * </p>
 *
 * @since 1.5
 * @author Yaniv Inbar
 */
public class ProtoHttpContent extends AbstractHttpContent {

  /** Message to serialize. */
  private final MessageLite message;

  /** Content type or {@code null} for none. */
  private String type = ProtocolBuffers.CONTENT_TYPE;

  /**
   * @param message message to serialize
   */
  public ProtoHttpContent(MessageLite message) {
    this.message = Preconditions.checkNotNull(message);
  }

  @Override
  public long getLength() throws IOException {
    return message.getSerializedSize();
  }

  public String getType() {
    return type;
  }

  public void writeTo(OutputStream out) throws IOException {
    message.writeTo(out);
  }

  /**
   * Sets the content type or {@code null} for none.
   *
   * <p>
   * Default value is {@link ProtocolBuffers#CONTENT_TYPE}.
   * </p>
   */
  public ProtoHttpContent setType(String type) {
    this.type = type;
    return this;
  }

  /** Returns the message to serialize. */
  public final MessageLite getMessage() {
    return message;
  }
}
