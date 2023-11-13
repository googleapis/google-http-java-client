/*
 * Copyright 2010 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.api.client.http.xml.atom;

import com.google.api.client.http.HttpResponse;
import com.google.api.client.util.Beta;
import com.google.api.client.util.Preconditions;
import com.google.api.client.util.Types;
import com.google.api.client.xml.Xml;
import com.google.api.client.xml.XmlNamespaceDictionary;
import com.google.api.client.xml.atom.AbstractAtomFeedParser;
import com.google.api.client.xml.atom.Atom;
import java.io.IOException;
import java.io.InputStream;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

/**
 * {@link Beta} <br>
 * Atom feed pull parser when the Atom entry class is known in advance.
 *
 * <p>Implementation is not thread-safe.
 *
 * @param <T> feed type
 * @param <E> entry type
 * @since 1.4
 * @author Yaniv Inbar
 */
@Beta
public final class AtomFeedParser<T, E> extends AbstractAtomFeedParser<T> {

  /** Atom entry class to parse. */
  private final Class<E> entryClass;

  /**
   * @param namespaceDictionary XML namespace dictionary
   * @param parser XML pull parser to use
   * @param inputStream input stream to read
   * @param feedClass feed class to parse
   * @since 1.5
   */
  public AtomFeedParser(
      XmlNamespaceDictionary namespaceDictionary,
      XmlPullParser parser,
      InputStream inputStream,
      Class<T> feedClass,
      Class<E> entryClass) {
    super(namespaceDictionary, parser, inputStream, feedClass);
    this.entryClass = Preconditions.checkNotNull(entryClass);
  }

  @SuppressWarnings("unchecked")
  @Override
  public E parseNextEntry() throws IOException, XmlPullParserException {
    return (E) super.parseNextEntry();
  }

  @Override
  protected Object parseEntryInternal() throws IOException, XmlPullParserException {
    E result = Types.newInstance(entryClass);
    Xml.parseElement(getParser(), result, getNamespaceDictionary(), null);
    return result;
  }

  /**
   * Returns the Atom entry class to parse.
   *
   * @since 1.5
   */
  public final Class<E> getEntryClass() {
    return entryClass;
  }

  /**
   * Parses the given HTTP response using the given feed class and entry class.
   *
   * @param <T> feed type
   * @param <E> entry type
   * @param response HTTP response
   * @param namespaceDictionary XML namespace dictionary
   * @param feedClass feed class
   * @param entryClass entry class
   * @return Atom feed parser
   * @throws IOException I/O exception
   * @throws XmlPullParserException XML pull parser exception
   */
  public static <T, E> AtomFeedParser<T, E> create(
      HttpResponse response,
      XmlNamespaceDictionary namespaceDictionary,
      Class<T> feedClass,
      Class<E> entryClass)
      throws IOException, XmlPullParserException {
    InputStream content = response.getContent();
    try {
      Atom.checkContentType(response.getContentType());
      XmlPullParser parser = Xml.createParser();
      parser.setInput(content, null);
      AtomFeedParser<T, E> result =
          new AtomFeedParser<T, E>(namespaceDictionary, parser, content, feedClass, entryClass);
      content = null;
      return result;
    } finally {
      if (content != null) {
        content.close();
      }
    }
  }
}
