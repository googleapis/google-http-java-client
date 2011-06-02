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

import com.google.api.client.http.HttpContent;
import com.google.api.client.protobuf.ProtocolBuffers;
import com.google.common.base.Preconditions;
import com.google.protobuf.MessageLite;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Serializes of a protocol buffer message to HTTP content.
 *
 * <p>
 * Implementation is immutable and therefore thread-safe, as long as the message itself is not
 * modified.
 * </p>
 * <p>
 * Sample usage:
 *
 * <pre>
 * <code>
  static HttpRequest buildPostRequest(
      HttpRequestFactory requestFactory, GenericUrl url, MessageLite message) throws IOException {
    return requestFactory.buildPostRequest(url, ProtoHttpContent.builder(message).build());
  }
 * </code>
 * </pre>
 *
 * @since 1.5
 * @author Yaniv Inbar
 */
public class ProtoHttpContent implements HttpContent {

  /** Message to serialize. */
  private final MessageLite message;

  /** Content type or {@code null} for none. */
  private final String type;

  /**
   * @param message message to serialize
   * @param type content type or {@code null} for none
   */
  protected ProtoHttpContent(MessageLite message, String type) {
    this.message = Preconditions.checkNotNull(message);
    this.type = type;
  }

  public String getEncoding() {
    return null;
  }

  public long getLength() throws IOException {
    return message.getSerializedSize();
  }

  public final String getType() {
    return type;
  }

  public boolean retrySupported() {
    return true;
  }

  public void writeTo(OutputStream out) throws IOException {
    message.writeTo(out);
  }

  /** Returns the message to serialize. */
  public final MessageLite getMessage() {
    return message;
  }

  /**
   * Returns an instance of a new builder.
   *
   * @param message message to serialize
   */
  public static Builder builder(MessageLite message) {
    return new Builder(message);
  }

  /**
   * Builder for {@link ProtoHttpContent}.
   * <p>
   * Implementation is not thread-safe.
   * </p>
   */
  public static class Builder {

    /** Content type or {@code null} for none. */
    private String contentType = ProtocolBuffers.CONTENT_TYPE;

    /** Message to serialize. */
    private final MessageLite message;

    /**
     * @param message message to serialize
     */
    protected Builder(MessageLite message) {
      this.message = Preconditions.checkNotNull(message);
    }

    /** Builds a new instance of {@link ProtoHttpContent}. */
    public ProtoHttpContent build() {
      return new ProtoHttpContent(message, contentType);
    }

    /** Returns the content type or {@code null} for none. */
    public final String getContentType() {
      return contentType;
    }

    /**
     * Sets the content type or {@code null} for none.
     *
     * <p>
     * Default value is {@link ProtocolBuffers#CONTENT_TYPE}.
     * </p>
     */
    public final Builder setContentType(String contentType) {
      this.contentType = contentType;
      return this;
    }

    /** Returns the message to serialize. */
    public final MessageLite getMessage() {
      return message;
    }
  }
}
