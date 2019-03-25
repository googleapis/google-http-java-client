/*
 * Copyright (c) 2011 Google Inc.
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
import static org.junit.Assert.fail;

import com.google.api.client.util.Key;
import com.google.api.client.util.Value;
import java.io.ByteArrayOutputStream;
import java.io.StringReader;
import java.util.ArrayList;
import org.junit.Test;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlSerializer;

/**
 * Tests {@link Xml}.
 *
 * @author Gerald Madlmayr
 */
public class XmlEnumTest {

  private static final String XML =
      "<?xml version=\"1.0\"?><any anyEnum=\"ENUM_1\" attr"
          + "=\"value\" xmlns=\"http://www.w3.org/2005/Atom\"><anotherEnum>ENUM_2</anotherEnum"
          + "><elem>content</elem><rep>rep1</rep><rep>rep2</rep><value>ENUM_1</value></any>";
  private static final String XML_ENUM_ELEMENT_ONLY =
      "<?xml version=\"1.0\"?><any xmlns"
          + "=\"http://www.w3.org/2005/Atom\"><elementEnum>ENUM_2</elementEnum></any>";
  private static final String XML_ENUM_ATTRIBUTE_ONLY =
      "<?xml version=\"1.0\"?><any "
          + "attributeEnum=\"ENUM_1\" xmlns=\"http://www.w3.org/2005/Atom\" />";
  private static final String XML_ENUM_INCORRECT =
      "<?xml version=\"1.0\"?><any xmlns=\"http"
          + "://www.w3.org/2005/Atom\"><elementEnum>ENUM_3</elementEnum></any>";
  private static final String XML_ENUM_ELEMENT_ONLY_NESTED =
      "<?xml version=\"1.0\"?><any "
          + "xmlns=\"http://www.w3.org/2005/Atom\"><elementEnum>ENUM_2<nested>something</nested"
          + "></elementEnum></any>";

  @Test
  public void testParseAnyType() throws Exception {
    AnyType xml = new AnyType();
    XmlPullParser parser = Xml.createParser();
    parser.setInput(new StringReader(XML));
    XmlNamespaceDictionary namespaceDictionary = new XmlNamespaceDictionary();
    Xml.parseElement(parser, xml, namespaceDictionary, null);
    assertTrue(xml.attr instanceof String);
    assertTrue(xml.elem.toString(), xml.elem instanceof ArrayList<?>);
    assertTrue(xml.rep.toString(), xml.rep instanceof ArrayList<?>);
    assertNotNull(xml.value);
    assertNotNull(xml.value.content);
    assertNotNull(xml.anyEnum);
    assertNotNull(xml.anotherEnum);
    assertEquals(xml.anyEnum, AnyEnum.ENUM_1);
    assertEquals(xml.anotherEnum, AnyEnum.ENUM_2);
    assertEquals(xml.value.content, AnyEnum.ENUM_1);
    // serialize
    XmlSerializer serializer = Xml.createSerializer();
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    serializer.setOutput(out, "UTF-8");
    namespaceDictionary.serialize(serializer, "any", xml);
    assertEquals(XML, out.toString());
  }

  /** The purpose of this test is to parse an XML element to an objects's field. */
  @Test
  public void testParseToEnumElementType() throws Exception {
    assertEquals(XML_ENUM_ELEMENT_ONLY, testStandardXml(XML_ENUM_ELEMENT_ONLY));
  }

  /**
   * The purpose of this test is to parse an XML element to an objects's field, whereas there are
   * additional nested elements in the tag.
   */
  @Test
  public void testParseToEnumElementTypeWithNestedElement() throws Exception {
    assertEquals(XML_ENUM_ELEMENT_ONLY, testStandardXml(XML_ENUM_ELEMENT_ONLY_NESTED));
  }

  /**
   * Private Method to handle standard parsing and mapping to {@link AnyTypeEnumElementOnly}.
   *
   * @param xmlString XML String that needs to be mapped to {@link AnyTypeEnumElementOnly}.
   * @return returns the serialized string of the XML object.
   * @throws Exception thrown if there is an issue processing the XML.
   */
  private String testStandardXml(final String xmlString) throws Exception {
    AnyTypeEnumElementOnly xml = new AnyTypeEnumElementOnly();
    XmlPullParser parser = Xml.createParser();
    parser.setInput(new StringReader(xmlString));
    XmlNamespaceDictionary namespaceDictionary = new XmlNamespaceDictionary();
    Xml.parseElement(parser, xml, namespaceDictionary, null);
    assertNotNull(xml.elementEnum);
    assertEquals(xml.elementEnum, AnyEnum.ENUM_2);
    // serialize
    XmlSerializer serializer = Xml.createSerializer();
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    serializer.setOutput(out, "UTF-8");
    namespaceDictionary.serialize(serializer, "any", xml);
    return out.toString();
  }

  /** The purpose of this test is to parse an XML attribute to an object's field. */
  @Test
  public void testParse_enumAttributeType() throws Exception {
    XmlEnumTest.AnyTypeEnumAttributeOnly xml = new XmlEnumTest.AnyTypeEnumAttributeOnly();
    XmlPullParser parser = Xml.createParser();
    parser.setInput(new StringReader(XML_ENUM_ATTRIBUTE_ONLY));
    XmlNamespaceDictionary namespaceDictionary = new XmlNamespaceDictionary();
    Xml.parseElement(parser, xml, namespaceDictionary, null);
    assertNotNull(xml.attributeEnum);
    assertEquals(xml.attributeEnum, AnyEnum.ENUM_1);
    // serialize
    XmlSerializer serializer = Xml.createSerializer();
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    serializer.setOutput(out, "UTF-8");
    namespaceDictionary.serialize(serializer, "any", xml);
    assertEquals(XML_ENUM_ATTRIBUTE_ONLY, out.toString());
  }

  /**
   * The purpose of this test is to parse an XML element to an object's field which is an
   * enumeration, whereas the enumeration element does not exist.
   */
  @Test
  public void testParse_enumElementTypeIncorrect() throws Exception {
    XmlEnumTest.AnyTypeEnumElementOnly xml = new XmlEnumTest.AnyTypeEnumElementOnly();
    XmlPullParser parser = Xml.createParser();
    parser.setInput(new StringReader(XML_ENUM_INCORRECT));
    XmlNamespaceDictionary namespaceDictionary = new XmlNamespaceDictionary();
    try {
      Xml.parseElement(parser, xml, namespaceDictionary, null);
      // fail test, if there is no exception
      fail();
    } catch (final IllegalArgumentException e) {
      assertEquals("given enum name ENUM_3 not part of enumeration", e.getMessage());
    }
  }

  public enum AnyEnum {
    @Value
    ENUM_1,
    @Value
    ENUM_2
  }

  private static class AnyType {
    @Key("@attr")
    private Object attr;

    @Key private Object elem;
    @Key private Object rep;

    @Key("@anyEnum")
    private XmlEnumTest.AnyEnum anyEnum;

    @Key private XmlEnumTest.AnyEnum anotherEnum;
    @Key private ValueType value;
  }

  private static class AnyTypeEnumElementOnly {
    @Key private XmlEnumTest.AnyEnum elementEnum;
  }

  private static class AnyTypeEnumAttributeOnly {
    @Key("@attributeEnum")
    private AnyEnum attributeEnum;
  }

  /** Needs to be public, this is referenced in another element. */
  public static class ValueType {
    @Key("text()")
    private XmlEnumTest.AnyEnum content;
  }
}
