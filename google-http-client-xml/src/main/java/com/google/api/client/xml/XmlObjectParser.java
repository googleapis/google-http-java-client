/*
 * Copyright (c) 2010 Google Inc.
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

package com.google.api.client.xml;

import com.google.api.client.util.Beta;
import com.google.api.client.util.ObjectParser;
import com.google.api.client.util.Preconditions;
import com.google.api.client.util.Types;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.lang.reflect.Type;
import java.nio.charset.Charset;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

/**
 * {@link Beta} <br>
 * XML HTTP parser into an data class of key/value pairs.
 *
 * <p>Implementation is thread-safe.
 *
 * <p>Sample usage:
 *
 * <pre>
 * static void setParser(HttpRequest request, XmlNamespaceDictionary namespaceDictionary) {
 * request.setParser(new XmlObjectParser(namespaceDictionary));
 * }
 * </pre>
 *
 * @since 1.10
 * @author Matthias Linder (mlinder)
 */
@Beta
public class XmlObjectParser implements ObjectParser {
  /** XML namespace dictionary. */
  private final XmlNamespaceDictionary namespaceDictionary;

  /**
   * Creates an XmlObjectParser using the specified non-null namespace dictionary.
   *
   * @param namespaceDictionary XML namespace dictionary
   */
  public XmlObjectParser(XmlNamespaceDictionary namespaceDictionary) {
    this.namespaceDictionary = Preconditions.checkNotNull(namespaceDictionary);
  }

  /** Returns the XML namespace dictionary. */
  public final XmlNamespaceDictionary getNamespaceDictionary() {
    return namespaceDictionary;
  }

  private Object readObject(XmlPullParser parser, Type dataType)
      throws XmlPullParserException, IOException {
    Preconditions.checkArgument(dataType instanceof Class<?>, "dataType has to be of Class<?>");
    Object result = Types.newInstance((Class<?>) dataType);
    Xml.parseElement(parser, result, namespaceDictionary, null);
    return result;
  }

  @SuppressWarnings("unchecked")
  public <T> T parseAndClose(InputStream in, Charset charset, Class<T> dataClass)
      throws IOException {
    return (T) parseAndClose(in, charset, (Type) dataClass);
  }

  public Object parseAndClose(InputStream in, Charset charset, Type dataType) throws IOException {
    try {
      // Initialize the parser
      XmlPullParser parser = Xml.createParser();
      parser.setInput(in, charset.name());
      return readObject(parser, dataType);
    } catch (XmlPullParserException e) {
      IOException exception = new IOException();
      exception.initCause(e);
      throw exception;
    } finally {
      in.close();
    }
  }

  @SuppressWarnings("unchecked")
  public <T> T parseAndClose(Reader reader, Class<T> dataClass) throws IOException {
    return (T) parseAndClose(reader, (Type) dataClass);
  }

  public Object parseAndClose(Reader reader, Type dataType) throws IOException {
    try {
      // Initialize the parser
      XmlPullParser parser = Xml.createParser();
      parser.setInput(reader);
      return readObject(parser, dataType);
    } catch (XmlPullParserException e) {
      IOException exception = new IOException();
      exception.initCause(e);
      throw exception;
    } finally {
      reader.close();
    }
  }
}
