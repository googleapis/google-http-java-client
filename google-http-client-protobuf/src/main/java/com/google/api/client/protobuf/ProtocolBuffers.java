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

package com.google.api.client.protobuf;

import com.google.protobuf.MessageLite;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;

/**
 * Utilities for protocol buffers.
 *
 * <p>
 * There is no official media type for protocol buffers registered with the <a
 * href="http://www.iana.org/assignments/media-types/application/index.html">IANA</a>.
 * {@link #CONTENT_TYPE} and {@link #ALT_CONTENT_TYPE} are some of the more popular choices being
 * used today, but other media types are also in use.
 * </p>
 *
 * @since 1.5
 * @author Yaniv Inbar
 */
public class ProtocolBuffers {

  /** {@code "application/x-protobuf"} content type. */
  public static final String CONTENT_TYPE = "application/x-protobuf";

  /** {@code "application/x-protobuffer"} content type. */
  public static final String ALT_CONTENT_TYPE = "application/x-protobuffer";

  /**
   * Parses protocol buffer content from an input stream (closing the input stream) into a protocol
   * buffer message.
   *
   * @param <T> destination message type
   * @param messageClass destination message class that has a {@code parseFrom(InputStream)} public
   *        static method
   * @return new instance of the parsed destination message class
   */
  public static <T extends MessageLite> T parseAndClose(
      InputStream inputStream, Class<T> messageClass) throws IOException {
    try {
      Method newBuilder = messageClass.getDeclaredMethod("parseFrom", InputStream.class);
      return messageClass.cast(newBuilder.invoke(null, inputStream));
    } catch (Exception e) {
      IOException io = new IOException("Error parsing message of type " + messageClass);
      io.initCause(e);
      throw io;
    } finally {
      inputStream.close();
    }
  }

  private ProtocolBuffers() {
  }
}
