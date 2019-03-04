/*
 * Copyright (c) 2010 Google Inc.J
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

package com.google.api.client.json;

import com.google.api.client.util.Charsets;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.Charset;

/**
 * Abstract low-level JSON factory.
 *
 * <p>Implementation is thread-safe, and sub-classes must be thread-safe. For maximum efficiency,
 * applications should use a single globally-shared instance of the JSON factory.
 *
 * @since 1.3
 * @author Yaniv Inbar
 */
public abstract class JsonFactory {

  /**
   * Returns a new instance of a low-level JSON parser for the given input stream. The parser tries
   * to detect the charset of the input stream by itself.
   *
   * @param in input stream
   * @return new instance of a low-level JSON parser
   */
  public abstract JsonParser createJsonParser(InputStream in) throws IOException;

  /**
   * Returns a new instance of a low-level JSON parser for the given input stream.
   *
   * @param in input stream
   * @param charset charset in which the input stream is encoded or {@code null} to let the parser
   *     detect the charset
   * @return new instance of a low-level JSON parser
   * @since 1.10
   */
  public abstract JsonParser createJsonParser(InputStream in, Charset charset) throws IOException;

  /**
   * Returns a new instance of a low-level JSON parser for the given string value.
   *
   * @param value string value
   * @return new instance of a low-level JSON parser
   */
  public abstract JsonParser createJsonParser(String value) throws IOException;

  /**
   * Returns a new instance of a low-level JSON parser for the given reader.
   *
   * @param reader reader
   * @return new instance of a low-level JSON parser
   */
  public abstract JsonParser createJsonParser(Reader reader) throws IOException;

  /**
   * Returns a new instance of a low-level JSON serializer for the given output stream and encoding.
   *
   * @param out output stream
   * @param enc encoding
   * @return new instance of a low-level JSON serializer
   * @since 1.10
   */
  public abstract JsonGenerator createJsonGenerator(OutputStream out, Charset enc)
      throws IOException;

  /**
   * Returns a new instance of a low-level JSON serializer for the given writer.
   *
   * @param writer writer
   * @return new instance of a low-level JSON serializer
   */
  public abstract JsonGenerator createJsonGenerator(Writer writer) throws IOException;

  /**
   * Creates an object parser which uses this factory to parse JSON data.
   *
   * @since 1.10
   */
  public final JsonObjectParser createJsonObjectParser() {
    return new JsonObjectParser(this);
  }

  /**
   * Returns a serialized JSON string representation for the given item using {@link
   * JsonGenerator#serialize(Object)}.
   *
   * @param item data key/value pairs
   * @return serialized JSON string representation
   */
  public final String toString(Object item) throws IOException {
    return toString(item, false);
  }

  /**
   * Returns a pretty-printed serialized JSON string representation for the given item using {@link
   * JsonGenerator#serialize(Object)} with {@link JsonGenerator#enablePrettyPrint()}.
   *
   * <p>The specifics of how the JSON representation is made pretty is implementation dependent, and
   * should not be relied on. However, it is assumed to be legal, and in fact differs from {@link
   * #toString(Object)} only by adding whitespace that does not change its meaning.
   *
   * @param item data key/value pairs
   * @return serialized JSON string representation
   * @since 1.6
   */
  public final String toPrettyString(Object item) throws IOException {
    return toString(item, true);
  }

  /**
   * Returns a UTF-8 encoded byte array of the serialized JSON representation for the given item
   * using {@link JsonGenerator#serialize(Object)}.
   *
   * @param item data key/value pairs
   * @return byte array of the serialized JSON representation
   * @since 1.7
   */
  public final byte[] toByteArray(Object item) throws IOException {
    return toByteStream(item, false).toByteArray();
  }

  /**
   * Returns a serialized JSON string representation for the given item using {@link
   * JsonGenerator#serialize(Object)}.
   *
   * @param item data key/value pairs
   * @param pretty whether to return a pretty representation
   * @return serialized JSON string representation
   */
  private String toString(Object item, boolean pretty) throws IOException {
    return toByteStream(item, pretty).toString("UTF-8");
  }

  /**
   * Returns a UTF-8 byte array output stream of the serialized JSON representation for the given
   * item using {@link JsonGenerator#serialize(Object)}.
   *
   * @param item data key/value pairs
   * @param pretty whether to return a pretty representation
   * @return serialized JSON string representation
   */
  private ByteArrayOutputStream toByteStream(Object item, boolean pretty) throws IOException {
    ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
    JsonGenerator generator = createJsonGenerator(byteStream, Charsets.UTF_8);
    if (pretty) {
      generator.enablePrettyPrint();
    }
    generator.serialize(item);
    generator.flush();
    return byteStream;
  }

  /**
   * Parses a string value as a JSON object, array, or value into a new instance of the given
   * destination class using {@link JsonParser#parse(Class)}.
   *
   * @param value JSON string value
   * @param destinationClass destination class that has an accessible default constructor to use to
   *     create a new instance
   * @return new instance of the parsed destination class
   * @since 1.4
   */
  public final <T> T fromString(String value, Class<T> destinationClass) throws IOException {
    return createJsonParser(value).parse(destinationClass);
  }

  /**
   * Parse and close an input stream as a JSON object, array, or value into a new instance of the
   * given destination class using {@link JsonParser#parseAndClose(Class)}.
   *
   * <p>Tries to detect the charset of the input stream automatically.
   *
   * @param inputStream JSON value in an input stream
   * @param destinationClass destination class that has an accessible default constructor to use to
   *     create a new instance
   * @return new instance of the parsed destination class
   * @since 1.7
   */
  public final <T> T fromInputStream(InputStream inputStream, Class<T> destinationClass)
      throws IOException {
    return createJsonParser(inputStream).parseAndClose(destinationClass);
  }

  /**
   * Parse and close an input stream as a JSON object, array, or value into a new instance of the
   * given destination class using {@link JsonParser#parseAndClose(Class)}.
   *
   * @param inputStream JSON value in an input stream
   * @param charset Charset in which the stream is encoded
   * @param destinationClass destination class that has an accessible default constructor to use to
   *     create a new instance
   * @return new instance of the parsed destination class
   * @since 1.10
   */
  public final <T> T fromInputStream(
      InputStream inputStream, Charset charset, Class<T> destinationClass) throws IOException {
    return createJsonParser(inputStream, charset).parseAndClose(destinationClass);
  }

  /**
   * Parse and close a reader as a JSON object, array, or value into a new instance of the given
   * destination class using {@link JsonParser#parseAndClose(Class)}.
   *
   * @param reader JSON value in a reader
   * @param destinationClass destination class that has an accessible default constructor to use to
   *     create a new instance
   * @return new instance of the parsed destination class
   * @since 1.7
   */
  public final <T> T fromReader(Reader reader, Class<T> destinationClass) throws IOException {
    return createJsonParser(reader).parseAndClose(destinationClass);
  }
}
