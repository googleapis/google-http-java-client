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
import com.google.api.client.util.Data;
import com.google.api.client.util.DateTime;
import com.google.api.client.util.FieldInfo;
import com.google.api.client.util.Preconditions;
import com.google.api.client.util.Types;
import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;
import org.xmlpull.v1.XmlSerializer;

/**
 * {@link Beta} <br>
 * Thread-safe XML namespace dictionary that provides a one-to-one map of namespace alias to URI.
 *
 * <p>Implementation is thread-safe. For maximum efficiency, applications should use a single
 * globally-shared instance of the XML namespace dictionary.
 *
 * <p>A namespace alias is uniquely mapped to a single namespace URI, and a namespace URI is
 * uniquely mapped to a single namespace alias. In other words, it is not possible to have
 * duplicates.
 *
 * <p>Sample usage:
 *
 * <pre>{@code
 * static final XmlNamespaceDictionary DICTIONARY = new XmlNamespaceDictionary()
 * .set("", "http://www.w3.org/2005/Atom")
 * .set("activity", "http://activitystrea.ms/spec/1.0/")
 * .set("georss", "http://www.georss.org/georss")
 * .set("media", "http://search.yahoo.com/mrss/")
 * .set("thr", "http://purl.org/syndication/thread/1.0");
 * }</pre>
 *
 * @since 1.0
 * @author Yaniv Inbar
 */
@Beta
public final class XmlNamespaceDictionary {

  /**
   * Map from XML namespace alias (or {@code ""} for the default namespace) to XML namespace URI.
   */
  private final HashMap<String, String> namespaceAliasToUriMap = new HashMap<String, String>();

  /**
   * Map from XML namespace URI to XML namespace alias (or {@code ""} for the default namespace).
   */
  private final HashMap<String, String> namespaceUriToAliasMap = new HashMap<String, String>();

  /**
   * Returns the namespace alias (or {@code ""} for the default namespace) for the given namespace
   * URI.
   *
   * @param uri namespace URI
   * @since 1.3
   */
  public synchronized String getAliasForUri(String uri) {
    return namespaceUriToAliasMap.get(Preconditions.checkNotNull(uri));
  }

  /**
   * Returns the namespace URI for the given namespace alias (or {@code ""} for the default
   * namespace).
   *
   * @param alias namespace alias (or {@code ""} for the default namespace)
   * @since 1.3
   */
  public synchronized String getUriForAlias(String alias) {
    return namespaceAliasToUriMap.get(Preconditions.checkNotNull(alias));
  }

  /**
   * Returns an unmodified set of map entries for the map from namespace alias (or {@code ""} for
   * the default namespace) to namespace URI.
   *
   * @since 1.3
   */
  public synchronized Map<String, String> getAliasToUriMap() {
    return Collections.unmodifiableMap(namespaceAliasToUriMap);
  }

  /**
   * Returns an unmodified set of map entries for the map from namespace URI to namespace alias (or
   * {@code ""} for the default namespace).
   *
   * @since 1.3
   */
  public synchronized Map<String, String> getUriToAliasMap() {
    return Collections.unmodifiableMap(namespaceUriToAliasMap);
  }

  /**
   * Adds a namespace of the given alias and URI.
   *
   * <p>If the uri is {@code null}, the namespace alias will be removed. Similarly, if the alias is
   * {@code null}, the namespace URI will be removed. Otherwise, if the alias is already mapped to a
   * different URI, it will be remapped to the new URI. Similarly, if a URI is already mapped to a
   * different alias, it will be remapped to the new alias.
   *
   * @param alias alias or {@code null} to remove the namespace URI
   * @param uri namespace URI or {@code null} to remove the namespace alias
   * @return this namespace dictionary
   * @since 1.3
   */
  public synchronized XmlNamespaceDictionary set(String alias, String uri) {
    String previousUri = null;
    String previousAlias = null;
    if (uri == null) {
      if (alias != null) {
        previousUri = namespaceAliasToUriMap.remove(alias);
      }
    } else if (alias == null) {
      previousAlias = namespaceUriToAliasMap.remove(uri);
    } else {
      previousUri =
          namespaceAliasToUriMap.put(
              Preconditions.checkNotNull(alias), Preconditions.checkNotNull(uri));
      if (!uri.equals(previousUri)) {
        previousAlias = namespaceUriToAliasMap.put(uri, alias);
      } else {
        previousUri = null;
      }
    }
    if (previousUri != null) {
      namespaceUriToAliasMap.remove(previousUri);
    }
    if (previousAlias != null) {
      namespaceAliasToUriMap.remove(previousAlias);
    }
    return this;
  }

  /**
   * Shows a debug string representation of an element data object of key/value pairs.
   *
   * @param element element data object ({@link GenericXml}, {@link Map}, or any object with public
   *     fields)
   * @param elementName optional XML element local name prefixed by its namespace alias -- for
   *     example {@code "atom:entry"} -- or {@code null} to make up something
   */
  public String toStringOf(String elementName, Object element) {
    try {
      StringWriter writer = new StringWriter();
      XmlSerializer serializer = Xml.createSerializer();
      serializer.setOutput(writer);
      serialize(serializer, elementName, element, false);
      return writer.toString();
    } catch (IOException e) {
      throw new IllegalArgumentException(e);
    }
  }

  /**
   * Shows a debug string representation of an element data object of key/value pairs.
   *
   * @param element element data object ({@link GenericXml}, {@link Map}, or any object with public
   *     fields)
   * @param elementNamespaceUri XML namespace URI or {@code null} for no namespace
   * @param elementLocalName XML local name
   * @throws IOException I/O exception
   */
  public void serialize(
      XmlSerializer serializer, String elementNamespaceUri, String elementLocalName, Object element)
      throws IOException {
    serialize(serializer, elementNamespaceUri, elementLocalName, element, true);
  }

  /**
   * Shows a debug string representation of an element data object of key/value pairs.
   *
   * @param element element data object ({@link GenericXml}, {@link Map}, or any object with public
   *     fields)
   * @param elementName XML element local name prefixed by its namespace alias
   * @throws IOException I/O exception
   */
  public void serialize(XmlSerializer serializer, String elementName, Object element)
      throws IOException {
    serialize(serializer, elementName, element, true);
  }

  private void serialize(
      XmlSerializer serializer,
      String elementNamespaceUri,
      String elementLocalName,
      Object element,
      boolean errorOnUnknown)
      throws IOException {
    String elementAlias = elementNamespaceUri == null ? null : getAliasForUri(elementNamespaceUri);
    startDoc(serializer, element, errorOnUnknown, elementAlias)
        .serialize(serializer, elementNamespaceUri, elementLocalName);
    serializer.endDocument();
  }

  private void serialize(
      XmlSerializer serializer, String elementName, Object element, boolean errorOnUnknown)
      throws IOException {
    String elementAlias = "";
    if (elementName != null) {
      int colon = elementName.indexOf(':');
      if (colon != -1) {
        elementAlias = elementName.substring(0, colon);
      }
    }
    startDoc(serializer, element, errorOnUnknown, elementAlias).serialize(serializer, elementName);
    serializer.endDocument();
  }

  private ElementSerializer startDoc(
      XmlSerializer serializer, Object element, boolean errorOnUnknown, String elementAlias)
      throws IOException {
    serializer.startDocument(null, null);
    SortedSet<String> aliases = new TreeSet<String>();
    computeAliases(element, aliases);
    if (elementAlias != null) {
      aliases.add(elementAlias);
    }
    for (String alias : aliases) {
      String uri = getNamespaceUriForAliasHandlingUnknown(errorOnUnknown, alias);
      serializer.setPrefix(alias, uri);
    }
    return new ElementSerializer(element, errorOnUnknown);
  }

  private void computeAliases(Object element, SortedSet<String> aliases) {
    for (Map.Entry<String, Object> entry : Data.mapOf(element).entrySet()) {
      Object value = entry.getValue();
      if (value != null) {
        String name = entry.getKey();
        if (!Xml.TEXT_CONTENT.equals(name)) {
          int colon = name.indexOf(':');
          boolean isAttribute = name.charAt(0) == '@';
          if (colon != -1 || !isAttribute) {
            String alias = colon == -1 ? "" : name.substring(name.charAt(0) == '@' ? 1 : 0, colon);
            aliases.add(alias);
          }
          Class<?> valueClass = value.getClass();
          if (!isAttribute && !Data.isPrimitive(valueClass) && !valueClass.isEnum()) {
            if (value instanceof Iterable<?> || valueClass.isArray()) {
              for (Object subValue : Types.iterableOf(value)) {
                computeAliases(subValue, aliases);
              }
            } else {
              computeAliases(value, aliases);
            }
          }
        }
      }
    }
  }

  /**
   * Returns the namespace URI to use for serialization for a given namespace alias, possibly using
   * a predictable made-up namespace URI if the alias is not recognized.
   *
   * <p>Specifically, if the namespace alias is not recognized, the namespace URI returned will be
   * {@code "http://unknown/"} plus the alias, unless {@code errorOnUnknown} is {@code true} in
   * which case it will throw an {@link IllegalArgumentException}.
   *
   * @param errorOnUnknown whether to thrown an exception if the namespace alias is not recognized
   * @param alias namespace alias
   * @return namespace URI, using a predictable made-up namespace URI if the namespace alias is not
   *     recognized
   * @throws IllegalArgumentException if the namespace alias is not recognized and {@code
   *     errorOnUnknown} is {@code true}
   */
  String getNamespaceUriForAliasHandlingUnknown(boolean errorOnUnknown, String alias) {
    String result = getUriForAlias(alias);
    if (result == null) {
      Preconditions.checkArgument(
          !errorOnUnknown, "unrecognized alias: %s", alias.length() == 0 ? "(default)" : alias);
      return "http://unknown/" + alias;
    }
    return result;
  }

  /**
   * Returns the namespace alias to use for a given namespace URI, throwing an exception if the
   * namespace URI can be found in this dictionary.
   *
   * @param namespaceUri namespace URI
   * @throws IllegalArgumentException if the namespace URI is not found in this dictionary
   */
  String getNamespaceAliasForUriErrorOnUnknown(String namespaceUri) {
    String result = getAliasForUri(namespaceUri);
    Preconditions.checkArgument(
        result != null,
        "invalid XML: no alias declared for namesapce <%s>; "
            + "work-around by setting XML namepace directly by calling the set method of %s",
        namespaceUri,
        XmlNamespaceDictionary.class.getName());
    return result;
  }

  @Beta
  class ElementSerializer {
    private final boolean errorOnUnknown;
    Object textValue = null;
    final List<String> attributeNames = new ArrayList<String>();
    final List<Object> attributeValues = new ArrayList<Object>();
    final List<String> subElementNames = new ArrayList<String>();
    final List<Object> subElementValues = new ArrayList<Object>();

    ElementSerializer(Object elementValue, boolean errorOnUnknown) {
      this.errorOnUnknown = errorOnUnknown;
      Class<?> valueClass = elementValue.getClass();
      if (Data.isPrimitive(valueClass) && !Data.isNull(elementValue)) {
        textValue = elementValue;
      } else if (valueClass.isEnum() && !Data.isNull(elementValue)) {
        textValue = elementValue;
      } else {
        for (Map.Entry<String, Object> entry : Data.mapOf(elementValue).entrySet()) {
          Object fieldValue = entry.getValue();
          if (fieldValue != null && !Data.isNull(fieldValue)) {
            String fieldName = entry.getKey();
            if (Xml.TEXT_CONTENT.equals(fieldName)) {
              textValue = fieldValue;
            } else if (fieldName.charAt(0) == '@') {
              attributeNames.add(fieldName.substring(1));
              attributeValues.add(fieldValue);
            } else {
              subElementNames.add(fieldName);
              subElementValues.add(fieldValue);
            }
          }
        }
      }
    }

    void serialize(XmlSerializer serializer, String elementName) throws IOException {
      String elementLocalName = null;
      String elementNamespaceUri = null;
      if (elementName != null) {
        int colon = elementName.indexOf(':');
        elementLocalName = elementName.substring(colon + 1);
        String alias = colon == -1 ? "" : elementName.substring(0, colon);
        elementNamespaceUri = getNamespaceUriForAliasHandlingUnknown(errorOnUnknown, alias);
      }
      serialize(serializer, elementNamespaceUri, elementLocalName);
    }

    void serialize(XmlSerializer serializer, String elementNamespaceUri, String elementLocalName)
        throws IOException {
      boolean errorOnUnknown = this.errorOnUnknown;
      if (elementLocalName == null) {
        if (errorOnUnknown) {
          throw new IllegalArgumentException("XML name not specified");
        }
        elementLocalName = "unknownName";
      }
      serializer.startTag(elementNamespaceUri, elementLocalName);
      // attributes
      int num = attributeNames.size();
      for (int i = 0; i < num; i++) {
        String attributeName = attributeNames.get(i);
        int colon = attributeName.indexOf(':');
        String attributeLocalName = attributeName.substring(colon + 1);
        String attributeNamespaceUri =
            colon == -1
                ? null
                : getNamespaceUriForAliasHandlingUnknown(
                    errorOnUnknown, attributeName.substring(0, colon));
        serializer.attribute(
            attributeNamespaceUri, attributeLocalName, toSerializedValue(attributeValues.get(i)));
      }
      // text
      if (textValue != null) {
        serializer.text(toSerializedValue(textValue));
      }
      // elements
      num = subElementNames.size();
      for (int i = 0; i < num; i++) {
        Object subElementValue = subElementValues.get(i);
        String subElementName = subElementNames.get(i);
        Class<? extends Object> valueClass = subElementValue.getClass();
        if (subElementValue instanceof Iterable<?> || valueClass.isArray()) {
          for (Object subElement : Types.iterableOf(subElementValue)) {
            if (subElement != null && !Data.isNull(subElement)) {
              new ElementSerializer(subElement, errorOnUnknown)
                  .serialize(serializer, subElementName);
            }
          }
        } else {
          new ElementSerializer(subElementValue, errorOnUnknown)
              .serialize(serializer, subElementName);
        }
      }
      serializer.endTag(elementNamespaceUri, elementLocalName);
    }
  }

  static String toSerializedValue(Object value) {
    if (value instanceof Float) {
      Float f = (Float) value;
      if (f.floatValue() == Float.POSITIVE_INFINITY) {
        return "INF";
      }
      if (f.floatValue() == Float.NEGATIVE_INFINITY) {
        return "-INF";
      }
    }
    if (value instanceof Double) {
      Double d = (Double) value;
      if (d.doubleValue() == Double.POSITIVE_INFINITY) {
        return "INF";
      }
      if (d.doubleValue() == Double.NEGATIVE_INFINITY) {
        return "-INF";
      }
    }
    if (value instanceof String || value instanceof Number || value instanceof Boolean) {
      return value.toString();
    }
    if (value instanceof DateTime) {
      return ((DateTime) value).toStringRfc3339();
    }
    if (value instanceof Enum<?>) {
      return FieldInfo.of((Enum<?>) value).getName();
    }
    throw new IllegalArgumentException("unrecognized value type: " + value.getClass());
  }
}
