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

package com.google.api.client.http.xml;

import com.google.api.client.http.HttpParser;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.util.Types;
import com.google.api.client.xml.Xml;
import com.google.api.client.xml.XmlNamespaceDictionary;
import com.google.common.base.Preconditions;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;

/**
 * XML HTTP parser into an data class of key/value pairs.
 *
 * <p>
 * Implementation is thread-safe.
 * </p>
 *
 * <p>
 * Sample usage:
 * </p>
 *
 * <pre>
  static void setParser(HttpRequest request, XmlNamespaceDictionary namespaceDictionary) {
    request.addParser(new XmlHttpParser(namespaceDictionary));
  }
 * </pre>
 *
 * @since 1.0
 * @author Yaniv Inbar
 */
public class XmlHttpParser implements HttpParser {

  /** {@code "application/xml"} content type. */
  public static final String CONTENT_TYPE = "application/xml";

  /** Content type. Default value is {@link #CONTENT_TYPE}. */
  private final String contentType;

  /** XML namespace dictionary. */
  private final XmlNamespaceDictionary namespaceDictionary;

  /**
   * @param namespaceDictionary XML namespace dictionary
   * @since 1.5
   */
  public XmlHttpParser(XmlNamespaceDictionary namespaceDictionary) {
    this(namespaceDictionary, CONTENT_TYPE);
  }

  /**
   * @param namespaceDictionary XML namespace dictionary
   * @param contentType content type or {@code null} for none
   * @since 1.5
   */
  protected XmlHttpParser(XmlNamespaceDictionary namespaceDictionary, String contentType) {
    this.namespaceDictionary = Preconditions.checkNotNull(namespaceDictionary);
    this.contentType = contentType;
  }

  public final String getContentType() {
    return contentType;
  }

  /**
   * Default implementation parses the content of the response into the data class of key/value
   * pairs, but subclasses may override.
   */
  public <T> T parse(HttpResponse response, Class<T> dataClass) throws IOException {
    InputStream content = response.getContent();
    try {
      T result = Types.newInstance(dataClass);
      XmlPullParser parser = Xml.createParser();
      parser.setInput(content, null);
      Xml.parseElement(parser, result, namespaceDictionary, null);
      return result;
    } catch (XmlPullParserException e) {
      IOException exception = new IOException();
      exception.initCause(e);
      throw exception;
    } finally {
      content.close();
    }
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
   * Returns an instance of a new builder.
   *
   * @param namespaceDictionary XML namespace dictionary
   * @since 1.5
   */
  public static Builder builder(XmlNamespaceDictionary namespaceDictionary) {
    return new Builder(namespaceDictionary);
  }

  /**
   * Builder for {@link XmlHttpParser}.
   *
   * <p>
   * Implementation is not thread-safe.
   * </p>
   *
   * @since 1.5
   */
  public static class Builder {

    /** Content type or {@code null} for none. */
    private String contentType = CONTENT_TYPE;

    /** JSON factory. */
    private final XmlNamespaceDictionary namespaceDictionary;

    /**
     * @param namespaceDictionary XML namespace dictionary
     */
    protected Builder(XmlNamespaceDictionary namespaceDictionary) {
      this.namespaceDictionary = Preconditions.checkNotNull(namespaceDictionary);
    }

    /** Builds a new instance of {@link XmlHttpParser}. */
    public XmlHttpParser build() {
      return new XmlHttpParser(namespaceDictionary, contentType);
    }

    /** Returns the content type or {@code null} for none. */
    public final String getContentType() {
      return contentType;
    }

    /**
     * Sets the content type.
     *
     * <p>
     * Default value is {@link #CONTENT_TYPE}.
     * </p>
     */
    public Builder setContentType(String contentType) {
      this.contentType = Preconditions.checkNotNull(contentType);
      return this;
    }

    /** Returns the XML namespace dictionary. */
    public final XmlNamespaceDictionary getNamespaceDictionary() {
      return namespaceDictionary;
    }
  }
}
