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

import com.google.api.client.http.AbstractHttpContent;
import com.google.api.client.xml.Xml;
import com.google.api.client.xml.XmlNamespaceDictionary;
import com.google.common.base.Preconditions;

import org.xmlpull.v1.XmlSerializer;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Abstract serializer for XML HTTP content based on the data key/value mapping object for an item.
 *
 * <p>
 * Implementation is not thread-safe.
 * </p>
 *
 * @since 1.0
 * @author Yaniv Inbar
 */
public abstract class AbstractXmlHttpContent extends AbstractHttpContent {

  /**
   * @deprecated (scheduled to be removed in 1.6) Use
   *             {@link #AbstractXmlHttpContent(XmlNamespaceDictionary)}
   */
  @Deprecated
  public AbstractXmlHttpContent() {
  }

  /**
   * @param namespaceDictionary XML namespace dictionary
   * @since 1.5
   */
  protected AbstractXmlHttpContent(XmlNamespaceDictionary namespaceDictionary) {
    this.namespaceDictionary = Preconditions.checkNotNull(namespaceDictionary);
  }

  /**
   * Content type. Default value is {@link XmlHttpParser#CONTENT_TYPE}, though subclasses may define
   * a different default value.
   *
   * @deprecated (scheduled to be made private in 1.6) Use {@link #getType} or {@link #setType}
   */
  @Deprecated
  public String contentType = XmlHttpParser.CONTENT_TYPE;

  /**
   * XML namespace dictionary.
   *
   * @deprecated (scheduled to be made private final in 1.6) Use {@link #getNamespaceDictionary}
   */
  @Deprecated
  public XmlNamespaceDictionary namespaceDictionary;

  public final String getType() {
    return contentType;
  }

  public final void writeTo(OutputStream out) throws IOException {
    XmlSerializer serializer = Xml.createSerializer();
    serializer.setOutput(out, "UTF-8");
    writeTo(serializer);
  }

  /**
   * Sets the content type or {@code null} for none.
   *
   * <p>
   * Defaults to {@link XmlHttpParser#CONTENT_TYPE}, though subclasses may define a different
   * default value.
   * </p>
   *
   * @since 1.5
   */
  public AbstractXmlHttpContent setType(String type) {
    contentType = type;
    return this;
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
   * Writes the content to the given XML serializer.
   *
   * @throws IOException I/O exception
   */
  protected abstract void writeTo(XmlSerializer serializer) throws IOException;
}
