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

package com.google.api.client.xml.atom;

import com.google.api.client.util.Types;
import com.google.api.client.xml.Xml;
import com.google.api.client.xml.XmlNamespaceDictionary;
import com.google.common.base.Preconditions;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;

/**
 * Abstract base class for an Atom feed parser when the feed type is known in advance.
 *
 * <p>
 * Implementation is not thread-safe.
 * </p>
 *
 * @param <T> feed type
 * @since 1.0
 * @author Yaniv Inbar
 */
// TODO(yanivi): remove @SuppressWarnings("deprecation") for 1.6
@SuppressWarnings("deprecation")
public abstract class AbstractAtomFeedParser<T> {

  /** Whether the feed has been parsed. */
  private boolean feedParsed;

  /**
   * XML pull parser to use.
   *
   * @deprecated (scheduled to be made private final in 1.6) Use {@link #getParser}
   */
  @Deprecated
  public XmlPullParser parser;

  /**
   * Input stream to read.
   *
   * @deprecated (scheduled to be made private final in 1.6) Use {@link #getInputStream}
   */
  @Deprecated
  public InputStream inputStream;

  /**
   * Feed class to parse.
   *
   * @deprecated (scheduled to be made private final in 1.6) Use {@link #getFeedClass}
   */
  @Deprecated
  public Class<T> feedClass;

  /**
   * XML namespace dictionary.
   *
   * @deprecated (scheduled to be made private final in 1.6) Use {@link #getNamespaceDictionary}
   */
  @Deprecated
  public XmlNamespaceDictionary namespaceDictionary;

  /**
   * @deprecated (scheduled to be removed in 1.6) Use {@link
   *             #AbstractAtomFeedParser(XmlNamespaceDictionary, XmlPullParser, InputStream, Class)}
   */
  @Deprecated
  public AbstractAtomFeedParser() {
  }

  /**
   * @param namespaceDictionary XML namespace dictionary
   * @param parser XML pull parser to use
   * @param inputStream input stream to read
   * @param feedClass feed class to parse
   * @since 1.5
   */
  protected AbstractAtomFeedParser(XmlNamespaceDictionary namespaceDictionary, XmlPullParser parser,
      InputStream inputStream, Class<T> feedClass) {
    this.namespaceDictionary = Preconditions.checkNotNull(namespaceDictionary);
    this.parser = Preconditions.checkNotNull(parser);
    this.inputStream = Preconditions.checkNotNull(inputStream);
    this.feedClass = Preconditions.checkNotNull(feedClass);
  }

  /**
   * Returns the XML pull parser to use.
   *
   * @since 1.5
   */
  public final XmlPullParser getParser() {
    return parser;
  }

  /**
   * Returns the input stream to read.
   *
   * @since 1.5
   */
  public final InputStream getInputStream() {
    return inputStream;
  }

  /**
   * Returns the feed class to parse.
   *
   * @since 1.5
   */
  public final Class<T> getFeedClass() {
    return feedClass;
  }

  /**
   * Returns the XML namespace dictionary.
   *
   * @since 1.5
   */
  public final XmlNamespaceDictionary getNamespaceDictionary() {
    return namespaceDictionary;
  }

  /**
   * Parse the feed and return a new parsed instance of the feed type. This method can be skipped if
   * all you want are the items.
   *
   * @throws IOException I/O exception
   * @throws XmlPullParserException XML pull parser exception
   */
  public T parseFeed() throws IOException, XmlPullParserException {
    boolean close = true;
    try {
      this.feedParsed = true;
      T result = Types.newInstance(feedClass);
      Xml.parseElement(parser, result, namespaceDictionary, Atom.StopAtAtomEntry.INSTANCE);
      close = false;
      return result;
    } finally {
      if (close) {
        close();
      }
    }
  }

  /**
   * Parse the next item in the feed and return a new parsed instance of the item type. If there is
   * no item to parse, it will return {@code null} and automatically close the parser (in which case
   * there is no need to call {@link #close()}.
   *
   * @throws IOException I/O exception
   * @throws XmlPullParserException XML pull parser exception
   */
  public Object parseNextEntry() throws IOException, XmlPullParserException {
    if (!feedParsed) {
      feedParsed = true;
      Xml.parseElement(parser, null, namespaceDictionary, Atom.StopAtAtomEntry.INSTANCE);
    }
    boolean close = true;
    try {
      if (parser.getEventType() == XmlPullParser.START_TAG) {
        Object result = parseEntryInternal();
        parser.next();
        close = false;
        return result;
      }
    } finally {
      if (close) {
        close();
      }
    }
    return null;
  }

  /** Closes the underlying parser. */
  public void close() throws IOException {
    inputStream.close();
  }

  /**
   * Parses a single entry.
   *
   * @return object representing the entry
   * @throws IOException I/O exception
   * @throws XmlPullParserException XML pull parser exception
   */
  protected abstract Object parseEntryInternal() throws IOException, XmlPullParserException;
}
