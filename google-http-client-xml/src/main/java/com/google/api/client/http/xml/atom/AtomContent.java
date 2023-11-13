/*
 * Copyright 2011 Google LLC
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

import com.google.api.client.http.HttpMediaType;
import com.google.api.client.http.xml.AbstractXmlHttpContent;
import com.google.api.client.util.Beta;
import com.google.api.client.util.Preconditions;
import com.google.api.client.xml.XmlNamespaceDictionary;
import com.google.api.client.xml.atom.Atom;
import java.io.IOException;
import org.xmlpull.v1.XmlSerializer;

/**
 * {@link Beta} <br>
 * Serializes Atom XML HTTP content based on the data key/value mapping object for an Atom entry.
 *
 * <p>Default value for {@link #getType()} is {@link Atom#MEDIA_TYPE}.
 *
 * <p>Sample usages:
 *
 * <pre>
 * static void setAtomEntryContent(
 * HttpRequest request, XmlNamespaceDictionary namespaceDictionary, Object entry) {
 * request.setContent(AtomContent.forEntry(namespaceDictionary, entry));
 * }
 *
 * static void setAtomBatchContent(
 * HttpRequest request, XmlNamespaceDictionary namespaceDictionary, Object batchFeed) {
 * request.setContent(AtomContent.forFeed(namespaceDictionary, batchFeed));
 * }
 * </pre>
 *
 * <p>Implementation is not thread-safe.
 *
 * @since 1.4
 * @author Yaniv Inbar
 */
@Beta
public class AtomContent extends AbstractXmlHttpContent {

  /** {@code true} for an Atom entry or {@code false} for an Atom feed. */
  private final boolean isEntry;

  /** Key/value pair data for the Atom entry. */
  private final Object entry;

  /**
   * @param namespaceDictionary XML namespace dictionary
   * @param entry key/value pair data for the Atom entry
   * @param isEntry {@code true} for an Atom entry or {@code false} for an Atom feed
   * @since 1.5
   */
  protected AtomContent(XmlNamespaceDictionary namespaceDictionary, Object entry, boolean isEntry) {
    super(namespaceDictionary);
    setMediaType(new HttpMediaType(Atom.MEDIA_TYPE));
    this.entry = Preconditions.checkNotNull(entry);
    this.isEntry = isEntry;
  }

  /**
   * Returns a new instance of HTTP content for an Atom entry.
   *
   * @param namespaceDictionary XML namespace dictionary
   * @param entry data key/value pair for the Atom entry
   * @since 1.5
   */
  public static AtomContent forEntry(XmlNamespaceDictionary namespaceDictionary, Object entry) {
    return new AtomContent(namespaceDictionary, entry, true);
  }

  /**
   * Returns a new instance of HTTP content for an Atom feed.
   *
   * @param namespaceDictionary XML namespace dictionary
   * @param feed data key/value pair for the Atom feed
   * @since 1.5
   */
  public static AtomContent forFeed(XmlNamespaceDictionary namespaceDictionary, Object feed) {
    return new AtomContent(namespaceDictionary, feed, false);
  }

  @Override
  public AtomContent setMediaType(HttpMediaType mediaType) {
    super.setMediaType(mediaType);
    return this;
  }

  @Override
  public final void writeTo(XmlSerializer serializer) throws IOException {
    getNamespaceDictionary()
        .serialize(serializer, Atom.ATOM_NAMESPACE, isEntry ? "entry" : "feed", entry);
  }

  /**
   * Returns {@code true} for an Atom entry or {@code false} for an Atom feed.
   *
   * @since 1.5
   */
  public final boolean isEntry() {
    return isEntry;
  }

  /**
   * Returns the key name/value pair data for the Atom entry or Atom feed.
   *
   * @since 1.5
   */
  public final Object getData() {
    return entry;
  }
}
