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

import com.google.api.client.http.HttpParser;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.protobuf.ProtocolBuffers;
import com.google.common.base.Preconditions;
import com.google.protobuf.MessageLite;

import java.io.IOException;

/**
 * Parses protocol buffer HTTP response content into a protocol buffer message.
 *
 * <p>
 * Implementation is immutable and therefore thread-safe.
 * </p>
 *
 * <p>
 * Sample usage:
 * </p>
 *
 * <pre>
 * <code>
  static void setParser(HttpRequest request) {
    request.addParser(new ProtoHttpParser());
  }
 * </code>
 * </pre>
 *
 * @since 1.5
 * @author Yaniv Inbar
 */
public class ProtoHttpParser implements HttpParser {

  /** Content type. */
  private final String contentType;

  public ProtoHttpParser() {
    contentType = ProtocolBuffers.CONTENT_TYPE;
  }

  /**
   * @param contentType content type
   */
  protected ProtoHttpParser(String contentType) {
    this.contentType = contentType;
  }

  public final String getContentType() {
    return contentType;
  }

  @SuppressWarnings("unchecked")
  public <T> T parse(HttpResponse response, Class<T> dataClass) throws IOException {
    return (T) ProtocolBuffers.parseAndClose(
        response.getContent(), (Class<? extends MessageLite>) dataClass);
  }

  /** Returns an instance of a new builder. */
  public static Builder builder() {
    return new Builder();
  }

  /**
   * Builder for {@link ProtoHttpParser}.
   * <p>
   * Implementation is not thread-safe.
   * </p>
   */
  public static class Builder {

    /** Content type or {@code null} for none. */
    private String contentType = ProtocolBuffers.CONTENT_TYPE;

    protected Builder() {
    }

    /** Builds a new instance of {@link ProtoHttpParser}. */
    public ProtoHttpParser build() {
      return new ProtoHttpParser(contentType);
    }

    /** Returns the content type or {@code null} for none. */
    public final String getContentType() {
      return contentType;
    }

    /**
     * Sets the content type.
     *
     * <p>
     * Default value is {@link ProtocolBuffers#CONTENT_TYPE}.
     * </p>
     */
    public Builder setContentType(String contentType) {
      this.contentType = Preconditions.checkNotNull(contentType);
      return this;
    }
  }
}
