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
import com.google.api.client.http.HttpMediaType;
import com.google.api.client.util.Beta;
import com.google.api.client.util.Preconditions;
import com.google.api.client.xml.Xml;
import com.google.api.client.xml.XmlNamespaceDictionary;
import java.io.IOException;
import java.io.OutputStream;
import org.xmlpull.v1.XmlSerializer;

/**
 * {@link Beta} <br>
 * Abstract serializer for XML HTTP content based on the data key/value mapping object for an item.
 *
 * <p>Implementation is not thread-safe.
 *
 * @since 1.0
 * @author Yaniv Inbar
 */
@Beta
public abstract class AbstractXmlHttpContent extends AbstractHttpContent {

  /**
   * @param namespaceDictionary XML namespace dictionary
   * @since 1.5
   */
  protected AbstractXmlHttpContent(XmlNamespaceDictionary namespaceDictionary) {
    super(new HttpMediaType(Xml.MEDIA_TYPE));
    this.namespaceDictionary = Preconditions.checkNotNull(namespaceDictionary);
  }

  /** XML namespace dictionary. */
  private final XmlNamespaceDictionary namespaceDictionary;

  public final void writeTo(OutputStream out) throws IOException {
    XmlSerializer serializer = Xml.createSerializer();
    serializer.setOutput(out, getCharset().name());
    writeTo(serializer);
  }

  @Override
  public AbstractXmlHttpContent setMediaType(HttpMediaType mediaType) {
    super.setMediaType(mediaType);
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
