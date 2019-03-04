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

package com.google.api.client.protobuf;

import com.google.api.client.util.Beta;
import com.google.api.client.util.ObjectParser;
import com.google.protobuf.MessageLite;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.lang.reflect.Type;
import java.nio.charset.Charset;

/**
 * {@link Beta} <br>
 * Parses protocol buffer HTTP response content into a protocol buffer message.
 *
 * <p>Implementation is immutable and therefore thread-safe.
 *
 * <p>Data-classes are expected to extend {@link MessageLite}.
 *
 * <p>All Charset parameters are ignored for protocol buffers.
 *
 * @author Matthias Linder (mlinder)
 * @since 1.10
 */
@Beta
public class ProtoObjectParser implements ObjectParser {

  @SuppressWarnings("unchecked")
  public <T> T parseAndClose(InputStream in, Charset charset, Class<T> dataClass)
      throws IOException {
    return (T) ProtocolBuffers.parseAndClose(in, (Class<? extends MessageLite>) dataClass);
  }

  public Object parseAndClose(InputStream in, Charset charset, Type dataType) throws IOException {
    if (dataType instanceof Class<?>) {
      return parseAndClose(in, charset, (Class<?>) dataType);
    }
    throw new UnsupportedOperationException("dataType must be of Class<? extends MessageList>");
  }

  public <T> T parseAndClose(Reader reader, Class<T> dataClass) throws IOException {
    throw new UnsupportedOperationException("protocol buffers must be read from a binary stream");
  }

  public Object parseAndClose(Reader reader, Type dataType) throws IOException {
    throw new UnsupportedOperationException("protocol buffers must be read from a binary stream");
  }
}
