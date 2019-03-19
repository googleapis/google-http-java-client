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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import com.google.api.client.util.ArrayMap;
import com.google.api.client.util.Key;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import org.junit.Test;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;

/**
 * Tests {@link GenericXml}.
 *
 * <p>Tests are copies from {@link XmlTest}, but the dedicated Objects are derived from {@link
 * GenericXml}
 *
 * @author Yaniv Inbar
 * @author Gerald Madlmayr
 */
public class GenericXmlTest {

  private static final String XML =
      "<?xml version=\"1.0\"?><feed xmlns=\"http://www.w3.org"
          + "/2005/Atom\" xmlns:gd=\"http://schemas.google.com/g/2005\"><atom:entry xmlns=\"http"
          + "://schemas.google.com/g/2005\" xmlns:atom=\"http://www.w3.org/2005/Atom\" gd:etag"
          + "=\"abc\"><atom:title>One</atom:title></atom:entry><entry gd:etag=\"def\"><title "
          + "attribute=\"someattribute\">Two</title></entry></feed>";
  private static final String ANY_GENERIC_TYPE_XML =
      "<?xml version=\"1.0\"?><any attr=\"value\" "
          + "xmlns=\"http://www.w3.org/2005/Atom\"><elem><rep attr=\"param1\">rep1</rep><rep "
          + "attr=\"param2\">rep2</rep><value>content</value></elem></any>";
  private static final String SIMPLE_XML = "<any>test</any>";
  private static final String SIMPLE_XML_NUMERIC = "<any>1</any>";
  private static final String ANY_TYPE_XML =
      "<?xml version=\"1.0\"?><any attr=\"value\" "
          + "xmlns=\"http://www.w3.org/2005/Atom\"><elem>content</elem><rep>rep1</rep><rep>rep2</rep"
          + "><value>content</value></any>";
  private static final String ANY_TYPE_XML_PRIMITIVE_INT =
      "<?xml version=\"1.0\"?><any attr"
          + "=\"2\" xmlns=\"http://www.w3.org/2005/Atom\">1<intArray>1</intArray><intArray>2"
          + "</intArray></any>";
  private static final String ANY_TYPE_XML_PRIMITIVE_STR =
      "<?xml version=\"1.0\"?><any attr"
          + "=\"2+1\" xmlns=\"http://www.w3.org/2005/Atom\">1+1<strArray>1+1</strArray><strArray>2"
          + "+1</strArray></any>";
  private static final String ALL_TYPE =
      "<?xml version=\"1.0\"?><any xmlns=\"\"><integer"
          + "/><str/><genericXml/><anyEnum/><stringArray/><integerCollection/></any>";
  private static final String ANY_TYPE_XML_NESTED_ARRAY =
      "<?xml version=\"1.0\"?><any attr"
          + "=\"value\" xmlns=\"http://www.w3.org/2005/Atom\"><elem>content</elem><rep><p>rep1</p"
          + "><p>rep2</p></rep><rep><p>rep3</p><p>rep4</p></rep><value>content</value></any>";

  public GenericXmlTest() {}

  /**
   * The purpose of this test is to parse the given XML into a {@link GenericXml} Object that has no
   * fixed structure.
   */
  @SuppressWarnings("unchecked")
  @Test
  public void testParseToGenericXml() throws Exception {
    GenericXml xml = new GenericXml();
    XmlPullParser parser = Xml.createParser();
    parser.setInput(new StringReader(XML));
    XmlNamespaceDictionary namespaceDictionary = new XmlNamespaceDictionary();
    Xml.parseElement(parser, xml, namespaceDictionary, null);
    ArrayMap<String, String> expected =
        ArrayMap.of("gd", "http://schemas.google.com/g/2005", "", "http://www.w3.org/2005/Atom");
    assertEquals(expected, namespaceDictionary.getAliasToUriMap());
    assertEquals("feed", xml.name);
    Collection<GenericXml> foo = (Collection<GenericXml>) xml.get("entry");
    assertEquals(2, foo.size());
    ArrayMap<String, String> singleElementOne = ArrayMap.of("text()", "One");
    List<ArrayMap<String, String>> testOne = new ArrayList<ArrayMap<String, String>>();
    testOne.add(singleElementOne);
    assertEquals("abc", foo.toArray(new ArrayMap[] {})[0].get("@gd:etag"));
    assertEquals(testOne, foo.toArray(new ArrayMap[] {})[0].get("title"));
    ArrayMap<String, String> singleElementTwoAttrib =
        ArrayMap.of("@attribute", "someattribute", "text()", "Two");
    // ArrayMap<String, String> singleElementTwoValue =ArrayMap.of();
    List<ArrayMap<String, String>> testTwo = new ArrayList<ArrayMap<String, String>>();
    testTwo.add(singleElementTwoAttrib);
    // testTwo.add(singleElementTwoValue);
    assertEquals("def", foo.toArray(new ArrayMap[] {})[1].get("@gd:etag"));
    assertEquals(testTwo, foo.toArray(new ArrayMap[] {})[1].get("title"));
  }

  /** The purpose of this test is map a generic XML to an element inside a dedicated element. */
  @SuppressWarnings("unchecked")
  @Test
  public void testParseAnyGenericType() throws Exception {
    AnyGenericType xml = new AnyGenericType();
    XmlPullParser parser = Xml.createParser();
    parser.setInput(new StringReader(ANY_GENERIC_TYPE_XML));
    XmlNamespaceDictionary namespaceDictionary = new XmlNamespaceDictionary();
    Xml.parseElement(parser, xml, namespaceDictionary, null);
    assertTrue(xml.attr instanceof String);
    Collection<GenericXml> repList = (Collection<GenericXml>) xml.elem.get("rep");
    assertEquals(2, repList.size());
    Collection<GenericXml> repValue = (Collection<GenericXml>) xml.elem.get("value");
    assertEquals(1, repValue.size());
    // 1st rep element
    assertEquals(
        "@attr",
        ((Map.Entry)
                repList.toArray(new ArrayMap[] {})[0].entrySet().toArray(new Map.Entry[] {})[0])
            .getKey());
    assertEquals(
        "param1",
        ((Map.Entry)
                repList.toArray(new ArrayMap[] {})[0].entrySet().toArray(new Map.Entry[] {})[0])
            .getValue());
    assertEquals(
        "text()",
        ((Map.Entry)
                repList.toArray(new ArrayMap[] {})[0].entrySet().toArray(new Map.Entry[] {})[1])
            .getKey());
    assertEquals(
        "rep1",
        ((Map.Entry)
                repList.toArray(new ArrayMap[] {})[0].entrySet().toArray(new Map.Entry[] {})[1])
            .getValue());
    // 2nd rep element
    assertEquals(
        "@attr",
        ((Map.Entry)
                repList.toArray(new ArrayMap[] {})[1].entrySet().toArray(new Map.Entry[] {})[0])
            .getKey());
    assertEquals(
        "param2",
        ((Map.Entry)
                repList.toArray(new ArrayMap[] {})[1].entrySet().toArray(new Map.Entry[] {})[0])
            .getValue());
    assertEquals(
        "text()",
        ((Map.Entry)
                repList.toArray(new ArrayMap[] {})[1].entrySet().toArray(new Map.Entry[] {})[1])
            .getKey());
    assertEquals(
        "rep2",
        ((Map.Entry)
                repList.toArray(new ArrayMap[] {})[1].entrySet().toArray(new Map.Entry[] {})[1])
            .getValue());
    // value element
    assertEquals(
        "text()",
        ((Map.Entry)
                repValue.toArray(new ArrayMap[] {})[0].entrySet().toArray(new Map.Entry[] {})[0])
            .getKey());
    assertEquals(
        "content",
        ((Map.Entry)
                repValue.toArray(new ArrayMap[] {})[0].entrySet().toArray(new Map.Entry[] {})[0])
            .getValue());
    // serialize
    XmlSerializer serializer = Xml.createSerializer();
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    serializer.setOutput(out, "UTF-8");
    namespaceDictionary.serialize(serializer, "any", xml);
    assertEquals(ANY_GENERIC_TYPE_XML, out.toString());
  }

  /**
   * The purpose of this test is to map a {@link GenericXml} to the String element in the object.
   */
  @Test
  public void testParseSimpleTypeAsValueString() throws Exception {
    SimpleTypeStringGeneric xml = new SimpleTypeStringGeneric();
    XmlPullParser parser = Xml.createParser();
    parser.setInput(new StringReader(SIMPLE_XML));
    XmlNamespaceDictionary namespaceDictionary = new XmlNamespaceDictionary().set("", "");
    Xml.parseElement(parser, xml, namespaceDictionary, null);
    assertNotNull(xml);
    assertEquals(1, xml.values().size());
    assertEquals("test", xml.values().toArray()[0]);
    // serialize
    XmlSerializer serializer = Xml.createSerializer();
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    serializer.setOutput(out, "UTF-8");
    namespaceDictionary.serialize(serializer, "any", xml);
    assertEquals("<?xml version=\"1.0\"?><any xmlns=\"\">test</any>", out.toString());
  }

  /**
   * The purpose of this test is to map a {@link GenericXml} to the String element in the object.
   */
  @Test
  public void testParseSimpleTypeAsValueInteger() throws Exception {
    SimpleTypeNumericGeneric xml = new SimpleTypeNumericGeneric();
    XmlPullParser parser = Xml.createParser();
    parser.setInput(new StringReader(SIMPLE_XML_NUMERIC));
    XmlNamespaceDictionary namespaceDictionary = new XmlNamespaceDictionary().set("", "");
    Xml.parseElement(parser, xml, namespaceDictionary, null);
    assertNotNull(xml);
    assertEquals(1, xml.values().size());
    assertEquals(1, xml.values().toArray()[0]);
    // serialize
    XmlSerializer serializer = Xml.createSerializer();
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    serializer.setOutput(out, "UTF-8");
    namespaceDictionary.serialize(serializer, "any", xml);
    assertEquals("<?xml version=\"1.0\"?><any xmlns=\"\">1</any>", out.toString());
  }

  /**
   * The purpose of this test is to map a {@link GenericXml} to the String element in the object.
   */
  @Test
  public void testParseToAnyType() throws Exception {
    processAnyTypeGeneric(ANY_TYPE_XML);
  }

  /**
   * The purpose of this test is to map a {@link GenericXml} to the String element in the object.
   */
  @Test
  public void testParseToAnyTypeMissingField() throws Exception {
    AnyTypeMissingFieldGeneric xml = new AnyTypeMissingFieldGeneric();
    XmlPullParser parser = Xml.createParser();
    parser.setInput(new StringReader(ANY_TYPE_XML));
    XmlNamespaceDictionary namespaceDictionary = new XmlNamespaceDictionary();
    Xml.parseElement(parser, xml, namespaceDictionary, null);
    assertNotNull(xml);
    assertEquals(4, xml.values().size());
    // serialize
    XmlSerializer serializer = Xml.createSerializer();
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    serializer.setOutput(out, "UTF-8");
    namespaceDictionary.serialize(serializer, "any", xml);
    assertEquals(
        "<?xml version=\"1.0\"?><any attr=\"value\" xmlns=\"http://www.w3"
            + ".org/2005/Atom\"><elem>content</elem><value>content</value><rep>rep1</rep><rep>rep2</rep"
            + "></any>",
        out.toString());
  }

  /** The purpose of this test isto map a {@link GenericXml} to the String element in the object. */
  @Test
  public void testParseToAnyTypeAdditionalField() throws Exception {
    AnyTypeAdditionalFieldGeneric xml = new AnyTypeAdditionalFieldGeneric();
    XmlPullParser parser = Xml.createParser();
    parser.setInput(new StringReader(ANY_TYPE_XML));
    XmlNamespaceDictionary namespaceDictionary = new XmlNamespaceDictionary();
    Xml.parseElement(parser, xml, namespaceDictionary, null);
    assertNotNull(xml);
    assertEquals(4, xml.values().size());
    // serialize
    XmlSerializer serializer = Xml.createSerializer();
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    serializer.setOutput(out, "UTF-8");
    namespaceDictionary.serialize(serializer, "any", xml);
    assertEquals(ANY_TYPE_XML, out.toString());
  }

  /**
   * The purpose of this test is to map a {@link GenericXml} to the String element in the object.
   */
  @Test
  public void testParseToAnyTypePrimitiveInt() throws Exception {
    AnyTypePrimitiveIntGeneric xml = new AnyTypePrimitiveIntGeneric();
    XmlPullParser parser = Xml.createParser();
    parser.setInput(new StringReader(ANY_TYPE_XML_PRIMITIVE_INT));
    XmlNamespaceDictionary namespaceDictionary = new XmlNamespaceDictionary();
    Xml.parseElement(parser, xml, namespaceDictionary, null);
    assertNotNull(xml);
    assertEquals(3, xml.values().size());
    // serialize
    XmlSerializer serializer = Xml.createSerializer();
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    serializer.setOutput(out, "UTF-8");
    namespaceDictionary.serialize(serializer, "any", xml);
    assertEquals(ANY_TYPE_XML_PRIMITIVE_INT, out.toString());
  }

  /**
   * The purpose of this test is to map a {@link GenericXml} to the String element in the object.
   */
  @Test
  public void testParseToAnyTypeStringOnly() throws Exception {
    AnyTypePrimitiveStringGeneric xml = new AnyTypePrimitiveStringGeneric();
    XmlPullParser parser = Xml.createParser();
    parser.setInput(new StringReader(ANY_TYPE_XML_PRIMITIVE_STR));
    XmlNamespaceDictionary namespaceDictionary = new XmlNamespaceDictionary();
    Xml.parseElement(parser, xml, namespaceDictionary, null);
    assertNotNull(xml);
    assertEquals(3, xml.values().size());
    // serialize
    XmlSerializer serializer = Xml.createSerializer();
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    serializer.setOutput(out, "UTF-8");
    namespaceDictionary.serialize(serializer, "any", xml);
    assertEquals(ANY_TYPE_XML_PRIMITIVE_STR, out.toString());
  }

  /**
   * The purpose of this tests is to test the mapping of an XML to an object without any overlap. In
   * this case all the objects are stored in the {@link GenericXml#values} field.
   */
  @Test
  public void testParseIncorrectMapping() throws Exception {
    AnyTypeGeneric xml = new AnyTypeGeneric();
    XmlPullParser parser = Xml.createParser();
    parser.setInput(new StringReader(ALL_TYPE));
    XmlNamespaceDictionary namespaceDictionary = new XmlNamespaceDictionary().set("", "");
    Xml.parseElement(parser, xml, namespaceDictionary, null);
    assertNotNull(xml);
    assertEquals(6, xml.values().size());
    // serialize
    XmlSerializer serializer = Xml.createSerializer();
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    serializer.setOutput(out, "UTF-8");
    namespaceDictionary.serialize(serializer, "any", xml);
    assertEquals(
        "<?xml version=\"1.0\"?><any xmlns=\"\"><integer /><str /><genericXml /><anyEnum"
            + " /><stringArray /><integerCollection /></any>",
        out.toString());
  }

  /**
   * The purpose of this test is to map a {@link GenericXml} to the String element in the object.
   */
  @Test
  public void testParseAnyTypeWithNestedElementArrayMap() throws Exception {
    processAnyTypeGeneric(ANY_TYPE_XML_NESTED_ARRAY);
  }

  private void processAnyTypeGeneric(final String anyTypeXmlNestedArray)
      throws XmlPullParserException, IOException {
    AnyTypeGeneric xml = new AnyTypeGeneric();
    XmlPullParser parser = Xml.createParser();
    parser.setInput(new StringReader(anyTypeXmlNestedArray));
    XmlNamespaceDictionary namespaceDictionary = new XmlNamespaceDictionary();
    Xml.parseElement(parser, xml, namespaceDictionary, null);
    assertNotNull(xml);
    assertEquals(4, xml.values().size());
    // serialize
    XmlSerializer serializer = Xml.createSerializer();
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    serializer.setOutput(out, "UTF-8");
    namespaceDictionary.serialize(serializer, "any", xml);
    assertEquals(anyTypeXmlNestedArray, out.toString());
  }

  private static class AnyGenericType {
    @Key("@attr")
    public Object attr;

    @Key public GenericXml elem;
  }

  private static class SimpleTypeStringGeneric extends GenericXml {
    @Key("text()")
    public String value;
  }

  private static class SimpleTypeNumericGeneric extends GenericXml {
    @Key("text()")
    public int value;
  }

  private static class AnyTypeGeneric extends GenericXml {
    @Key("@attr")
    public Object attr;

    @Key public Object elem;
    @Key public Object rep;
    @Key public ValueTypeGeneric value;
  }

  private static class AnyTypeMissingFieldGeneric extends GenericXml {
    @Key("@attr")
    public Object attr;

    @Key public Object elem;
    @Key public ValueTypeGeneric value;
  }

  private static class AnyTypeAdditionalFieldGeneric extends GenericXml {
    @Key("@attr")
    public Object attr;

    @Key public Object elem;
    @Key public Object rep;
    @Key public Object additionalField;
    @Key public ValueTypeGeneric value;
  }

  public static class ValueTypeGeneric extends GenericXml {
    @Key("text()")
    public Object content;
  }

  private static class AnyTypePrimitiveIntGeneric extends GenericXml {
    @Key("text()")
    public int value;

    @Key("@attr")
    public int attr;

    @Key public int[] intArray;
  }

  private static class AnyTypePrimitiveStringGeneric extends GenericXml {
    @Key("text()")
    public String value;

    @Key("@attr")
    public String attr;

    @Key public String[] strArray;
  }
}
