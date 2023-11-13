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

package com.google.api.client.xml;

import com.google.api.client.util.Beta;
import com.google.api.client.util.GenericData;
import com.google.api.client.util.Key;
import java.util.concurrent.ConcurrentMap;

/**
 * {@link Beta} <br>
 * Generic XML data that stores all unknown key name/value pairs.
 *
 * <p>Each data key name maps into the name of the XPath expression value for the XML element,
 * attribute, or text content (using {@code "text()"}). Subclasses can declare fields for known XML
 * content using the {@link Key} annotation. Each field can be of any visibility (private, package
 * private, protected, or public) and must not be static. {@code null} unknown data key names are
 * not allowed, but {@code null} data values are allowed.
 *
 * <p>Implementation is not thread-safe. For a thread-safe choice instead use an implementation of
 * {@link ConcurrentMap}.
 *
 * @since 1.0
 * @author Yaniv Inbar
 */
@Beta
public class GenericXml extends GenericData implements Cloneable {

  /**
   * Optional XML element local name prefixed by its namespace alias -- for example {@code
   * "atom:entry"} -- or {@code null} if not set.
   */
  public String name;

  /** Optional namespace dictionary or {@code null} if not set. */
  public XmlNamespaceDictionary namespaceDictionary;

  @Override
  public GenericXml clone() {
    return (GenericXml) super.clone();
  }

  @Override
  public String toString() {
    XmlNamespaceDictionary namespaceDictionary = this.namespaceDictionary;
    if (namespaceDictionary == null) {
      namespaceDictionary = new XmlNamespaceDictionary();
    }
    return namespaceDictionary.toStringOf(name, this);
  }

  @Override
  public GenericXml set(String fieldName, Object value) {
    return (GenericXml) super.set(fieldName, value);
  }
}
