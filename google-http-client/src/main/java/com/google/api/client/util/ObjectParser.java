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

package com.google.api.client.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.lang.reflect.Type;
import java.nio.charset.Charset;

/**
 * Parses a data source into the specified data type.
 *
 * <p>Implementations should normally be thread-safe.
 *
 * @author Yaniv Inbar
 * @since 1.10
 */
public interface ObjectParser {

  /**
   * Parses the given input stream into a new instance of the the given data class of key/value
   * pairs and closes the input stream.
   *
   * @param in input stream which contains the data to parse
   * @param charset charset which should be used to decode the input stream or {@code null} if
   *     unknown
   * @param dataClass class into which the data is parsed
   */
  <T> T parseAndClose(InputStream in, Charset charset, Class<T> dataClass) throws IOException;

  /**
   * Parses the given input stream into a new instance of the the given data type of key/value pairs
   * and closes the input stream.
   *
   * @param in input stream which contains the data to parse
   * @param charset charset which should be used to decode the input stream or {@code null} if
   *     unknown
   * @param dataType type into which the data is parsed
   */
  Object parseAndClose(InputStream in, Charset charset, Type dataType) throws IOException;

  /**
   * Parses the given reader into a new instance of the the given data class of key/value pairs and
   * closes the reader.
   *
   * @param reader reader which contains the text data to parse
   * @param dataClass class into which the data is parsed
   */
  <T> T parseAndClose(Reader reader, Class<T> dataClass) throws IOException;

  /**
   * Parses the given reader into a new instance of the the given data type of key/value pairs and
   * closes the reader.
   *
   * @param reader reader which contains the text data to parse
   * @param dataType type into which the data is parsed
   */
  Object parseAndClose(Reader reader, Type dataType) throws IOException;
}
