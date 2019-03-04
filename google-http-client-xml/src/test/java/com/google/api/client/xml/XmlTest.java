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
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import com.google.api.client.util.ArrayMap;
import com.google.api.client.util.Key;
import java.io.ByteArrayOutputStream;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.junit.Test;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlSerializer;

/**
 * Tests {@link Xml}.
 *
 * @author Yaniv Inbar
 * @author Gerald Madlmayr
 */
public class XmlTest {

  private static final String SIMPLE_XML = "<any>test</any>";
  private static final String SIMPLE_XML_NUMERIC = "<any>1</any>";
  private static final String START_WITH_TEXT = "<?xml version=\"1.0\"?>start_with_text</any>";
  private static final String MISSING_END_ELEMENT =
      "<?xml version=\"1.0\"?><any xmlns=\"\">" + "missing_end_element";
  private static final String START_WITH_END_ELEMENT =
      "<?xml version=\"1.0\"?></p><any " + "xmlns=\"\">start_with_end_elemtn</any>";
  private static final String START_WITH_END_ELEMENT_NESTED =
      "<?xml version=\"1.0\"?><any " + "xmlns=\"\"></p>start_with_end_element_nested</any>";
  private static final String ANY_TYPE_XML =
      "<?xml version=\"1.0\"?><any attr=\"value\" "
          + "xmlns=\"http://www.w3.org/2005/Atom\"><elem>content</elem><rep>rep1</rep><rep>rep2"
          + "</rep><value>content</value></any>";
  private static final String ANY_TYPE_MISSING_XML =
      "<?xml version=\"1.0\"?><any attr=\"value\" "
          + "xmlns=\"http://www.w3.org/2005/Atom\"><elem>content</elem><value>content</value"
          + "></any>";
  private static final String ANY_TYPE_XML_PRIMITIVE_INT =
      "<?xml version=\"1.0\"?><any attr"
          + "=\"2\" xmlns=\"http://www.w3.org/2005/Atom\">1<intArray>1</intArray><intArray>2"
          + "</intArray></any>";
  private static final String ANY_TYPE_XML_PRIMITIVE_STR =
      "<?xml version=\"1.0\"?><any attr"
          + "=\"2+1\" xmlns=\"http://www.w3.org/2005/Atom\">1+1<strArray>1+1</strArray><strArray>2"
          + "+1</strArray></any>";
  private static final String NESTED_NS =
      "<?xml version=\"1.0\"?><any xmlns=\"http://www.w3"
          + ".org/2005/Atom\"><app:edited xmlns:app='http://www.w3.org/2007/app'>2011-08-09T04:38"
          + ":14.017Z</app:edited></any>";
  private static final String NESTED_NS_SERIALIZED =
      "<?xml version=\"1.0\"?><any xmlns"
          + "=\"http://www.w3.org/2005/Atom\" xmlns:app=\"http://www.w3.org/2007/app\"><app:edited"
          + ">2011-08-09T04:38:14.017Z</app:edited></any>";
  private static final String INF_TEST =
      "<?xml version=\"1.0\"?><any xmlns=\"\"><dblInfNeg"
          + ">-INF</dblInfNeg><dblInfPos>INF</dblInfPos><fltInfNeg>-INF</fltInfNeg><fltInfPos>INF"
          + "</fltInfPos></any>";
  private static final String ALL_TYPE =
      "<?xml version=\"1.0\"?><any xmlns=\"\"><integer"
          + "/><str/><genericXml/><anyEnum/><stringArray/><integerCollection/></any>";
  private static final String ALL_TYPE_WITH_DATA =
      "<?xml version=\"1.0\"?><any xmlns=\"\">"
          + "<anyEnum>ENUM_1</anyEnum><anyEnum>ENUM_2</anyEnum><genericXml><html><head><title"
          + ">Title</title></head><body><p>Test</p></body></html></genericXml><integer>1</integer"
          + "><integerCollection>1</integerCollection><integerCollection>2</integerCollection><str"
          + ">str1</str><stringArray>arr1</stringArray><stringArray>arr2</stringArray></any>";
  private static final String ANY_TYPE_XML_NESTED_ARRAY =
      "<?xml version=\"1.0\"?><any attr"
          + "=\"value\" xmlns=\"http://www.w3.org/2005/Atom\"><elem>content</elem><rep><p>rep1</p"
          + "><p>rep2</p></rep><rep><p>rep3</p><p>rep4</p></rep><value>content</value></any>";

  /**
   * The purpose of this test is to map a single element to a single field of a destination object.
   * In this case the object mapped is a {@link String}; no namespace used.
   */
  @Test
  public void testParseSimpleTypeAsValueString() throws Exception {
    SimpleTypeString xml = new SimpleTypeString();
    XmlPullParser parser = Xml.createParser();
    parser.setInput(new StringReader(SIMPLE_XML));
    XmlNamespaceDictionary namespaceDictionary = new XmlNamespaceDictionary().set("", "");
    Xml.parseElement(parser, xml, namespaceDictionary, null);
    // check type
    assertEquals("test", xml.value);
    // serialize
    XmlSerializer serializer = Xml.createSerializer();
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    serializer.setOutput(out, "UTF-8");
    namespaceDictionary.serialize(serializer, "any", xml);
    assertEquals("<?xml version=\"1.0\"?><any xmlns=\"\">test</any>", out.toString());
  }

  /**
   * The purpose of this test is to map a single element to a single field of a destination object.
   * In this is it is not an object but a {@code int}. no namespace used.
   */
  @Test
  public void testParseSimpleTypeAsValueInteger() throws Exception {
    SimpleTypeNumeric xml = new SimpleTypeNumeric();
    XmlPullParser parser = Xml.createParser();
    parser.setInput(new StringReader(SIMPLE_XML_NUMERIC));
    XmlNamespaceDictionary namespaceDictionary = new XmlNamespaceDictionary().set("", "");
    Xml.parseElement(parser, xml, namespaceDictionary, null);
    // check type
    assertEquals(1, xml.value);
    // serialize
    XmlSerializer serializer = Xml.createSerializer();
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    serializer.setOutput(out, "UTF-8");
    namespaceDictionary.serialize(serializer, "any", xml);
    assertEquals("<?xml version=\"1.0\"?><any xmlns=\"\">1</any>", out.toString());
  }

  /** Negative test to check for text without a start-element. */
  @Test
  public void testWithTextFail() throws Exception {
    SimpleTypeString xml = new SimpleTypeString();
    XmlPullParser parser = Xml.createParser();
    parser.setInput(new StringReader(START_WITH_TEXT));
    XmlNamespaceDictionary namespaceDictionary = new XmlNamespaceDictionary().set("", "");
    try {
      Xml.parseElement(parser, xml, namespaceDictionary, null);
      fail();
    } catch (final Exception e) {
      assertEquals(
          "only whitespace content allowed before start tag and not s (position: "
              + "START_DOCUMENT seen <?xml version=\"1.0\"?>s... @1:22)",
          e.getMessage().trim());
    }
  }

  /** Negative test to check for missing end-element. */
  @Test
  public void testWithMissingEndElementFail() throws Exception {
    SimpleTypeString xml = new SimpleTypeString();
    XmlPullParser parser = Xml.createParser();
    parser.setInput(new StringReader(MISSING_END_ELEMENT));
    XmlNamespaceDictionary namespaceDictionary = new XmlNamespaceDictionary().set("", "");
    try {
      Xml.parseElement(parser, xml, namespaceDictionary, null);
      fail();
    } catch (final Exception e) {
      assertEquals(
          "no more data available - expected end tag </any> to close start tag <any"
              + "> from line 1, parser stopped on START_TAG seen ...<any xmlns"
              + "=\"\">missing_end_element... @1:54",
          e.getMessage().trim());
    }
  }

  /** Negative test with that start with a end-element. */
  @Test
  public void testWithEndElementStarting() throws Exception {
    SimpleTypeString xml = new SimpleTypeString();
    XmlPullParser parser = Xml.createParser();
    parser.setInput(new StringReader(START_WITH_END_ELEMENT));
    XmlNamespaceDictionary namespaceDictionary = new XmlNamespaceDictionary().set("", "");
    try {
      Xml.parseElement(parser, xml, namespaceDictionary, null);
      fail();
    } catch (final Exception e) {
      assertEquals(
          "expected start tag name and not / (position: START_DOCUMENT seen <?xml "
              + "version=\"1.0\"?></... @1:23)",
          e.getMessage().trim());
    }
  }

  /** Negative test with that start with a end element tag nested in an started element. */
  @Test
  public void testWithEndElementNested() throws Exception {
    SimpleTypeString xml = new SimpleTypeString();
    XmlPullParser parser = Xml.createParser();
    parser.setInput(new StringReader(START_WITH_END_ELEMENT_NESTED));
    XmlNamespaceDictionary namespaceDictionary = new XmlNamespaceDictionary().set("", "");
    try {
      Xml.parseElement(parser, xml, namespaceDictionary, null);
      fail();
    } catch (final Exception e) {
      assertEquals(
          "end tag name </p> must match start tag name <any> from line 1 (position:"
              + " START_TAG seen ...<any xmlns=\"\"></p>... @1:39)",
          e.getMessage().trim());
    }
  }

  /** Negative test that maps a string to an integer and causes an exception. */
  @Test
  public void testFailMappingOfDataType() throws Exception {
    SimpleTypeNumeric xml = new SimpleTypeNumeric();
    XmlPullParser parser = Xml.createParser();
    parser.setInput(new StringReader(SIMPLE_XML));
    XmlNamespaceDictionary namespaceDictionary = new XmlNamespaceDictionary().set("", "");
    try {
      Xml.parseElement(parser, xml, namespaceDictionary, null);
      fail();
    } catch (final Exception e) {
      assertEquals("For input string: \"test\"", e.getMessage().trim());
    }
  }

  /**
   * The purpose of this tests it to test the {@link Key} Annotation for mapping of elements and
   * attributes. All elements/attributes are matched.
   */
  @Test
  public void testParseToAnyType() throws Exception {
    AnyType xml = new AnyType();
    XmlPullParser parser = Xml.createParser();
    parser.setInput(new StringReader(ANY_TYPE_XML));
    XmlNamespaceDictionary namespaceDictionary = new XmlNamespaceDictionary();
    Xml.parseElement(parser, xml, namespaceDictionary, null);
    assertTrue(xml.attr instanceof String);
    assertTrue(xml.elem.toString(), xml.elem instanceof ArrayList<?>);
    assertTrue(xml.rep.toString(), xml.rep instanceof ArrayList<?>);
    assertNotNull(xml.value);
    assertTrue(xml.value.content instanceof String);
    // serialize
    XmlSerializer serializer = Xml.createSerializer();
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    serializer.setOutput(out, "UTF-8");
    namespaceDictionary.serialize(serializer, "any", xml);
    assertEquals(ANY_TYPE_XML, out.toString());
  }

  /**
   * The purpose of this tests it to test the {@link Key} annotation for mapping of elements and
   * attributes. The matched object misses some field that are present int the XML ('elem' is
   * missing and therefore ignored).
   */
  @Test
  public void testParseToAnyTypeMissingField() throws Exception {
    AnyTypeMissingField xml = new AnyTypeMissingField();
    XmlPullParser parser = Xml.createParser();
    parser.setInput(new StringReader(ANY_TYPE_XML));
    XmlNamespaceDictionary namespaceDictionary = new XmlNamespaceDictionary();
    Xml.parseElement(parser, xml, namespaceDictionary, null);
    assertTrue(xml.attr instanceof String);
    assertTrue(xml.elem.toString(), xml.elem instanceof ArrayList<?>);
    assertNotNull(xml.value);
    assertTrue(xml.value.content instanceof String);
    // serialize
    XmlSerializer serializer = Xml.createSerializer();
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    serializer.setOutput(out, "UTF-8");
    namespaceDictionary.serialize(serializer, "any", xml);
    assertEquals(ANY_TYPE_MISSING_XML, out.toString());
  }

  /**
   * The purpose of this tests it to test the {@link Key} Annotation for mapping of elements and
   * attributes. The matched object has an additional field, that will not be used and stays {@code
   * null}.
   */
  @Test
  public void testParseToAnyTypeAdditionalField() throws Exception {
    AnyTypeAdditionalField xml = new AnyTypeAdditionalField();
    XmlPullParser parser = Xml.createParser();
    parser.setInput(new StringReader(ANY_TYPE_XML));
    XmlNamespaceDictionary namespaceDictionary = new XmlNamespaceDictionary();
    Xml.parseElement(parser, xml, namespaceDictionary, null);
    assertTrue(xml.attr instanceof String);
    assertTrue(xml.elem.toString(), xml.elem instanceof ArrayList<?>);
    assertNotNull(xml.value);
    assertNull(xml.additionalField);
    assertTrue(xml.rep.toString(), xml.rep instanceof ArrayList<?>);
    assertTrue(xml.value.content instanceof String);
    // serialize
    XmlSerializer serializer = Xml.createSerializer();
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    serializer.setOutput(out, "UTF-8");
    namespaceDictionary.serialize(serializer, "any", xml);
    assertEquals(ANY_TYPE_XML, out.toString());
  }

  /**
   * The purpose of this test is to see, if there is an exception of the parameter 'destination' in
   * {@link Xml#parseElement} is {@code null}. The parser internally will skip mapping of the XML
   * structure, but will parse it anyway.
   */
  @Test
  public void testParseToAnyTypeWithNullDestination() throws Exception {
    XmlPullParser parser = Xml.createParser();
    parser.setInput(new StringReader(ANY_TYPE_XML));
    XmlNamespaceDictionary namespaceDictionary = new XmlNamespaceDictionary();
    Xml.parseElement(parser, null, namespaceDictionary, null);
  }

  /**
   * The purpose of this test is to see, if parsing works with a {@link Xml.CustomizeParser}. The
   * XML will be mapped to {@link AnyType}.
   */
  @Test
  public void testParseAnyTypeWithCustomParser() throws Exception {
    AnyType xml = new AnyType();
    XmlPullParser parser = Xml.createParser();
    parser.setInput(new StringReader(ANY_TYPE_XML));
    XmlNamespaceDictionary namespaceDictionary = new XmlNamespaceDictionary();
    Xml.parseElement(parser, xml, namespaceDictionary, new Xml.CustomizeParser());
    assertTrue(xml.attr instanceof String);
    assertTrue(xml.elem.toString(), xml.elem instanceof ArrayList<?>);
    assertTrue(xml.rep.toString(), xml.rep instanceof ArrayList<?>);
    assertNotNull(xml.value);
    assertTrue(xml.value.content instanceof String);
    // serialize
    XmlSerializer serializer = Xml.createSerializer();
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    serializer.setOutput(out, "UTF-8");
    namespaceDictionary.serialize(serializer, "any", xml);
    assertEquals(ANY_TYPE_XML, out.toString());
  }

  /**
   * The purpose of this test it to parse elements which will be mapped to a {@link
   * javax.lang.model.type.PrimitiveType}. Therefore {@code int}s are mapped to attributes, elements
   * and element arrays.
   */
  @Test
  public void testParseToAnyTypePrimitiveInt() throws Exception {
    AnyTypePrimitiveInt xml = new AnyTypePrimitiveInt();
    XmlPullParser parser = Xml.createParser();
    parser.setInput(new StringReader(ANY_TYPE_XML_PRIMITIVE_INT));
    XmlNamespaceDictionary namespaceDictionary = new XmlNamespaceDictionary();
    Xml.parseElement(parser, xml, namespaceDictionary, new Xml.CustomizeParser());
    assertEquals(1, xml.value);
    assertEquals(2, xml.attr);
    assertEquals(2, xml.intArray.length);
    assertEquals(1, xml.intArray[0]);
    assertEquals(2, xml.intArray[1]);
    // serialize
    XmlSerializer serializer = Xml.createSerializer();
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    serializer.setOutput(out, "UTF-8");
    namespaceDictionary.serialize(serializer, "any", xml);
    assertEquals(ANY_TYPE_XML_PRIMITIVE_INT, out.toString());
  }

  /**
   * The purpose of this test it to parse elements which will be mapped to a Java {@link
   * javax.lang.model.type.PrimitiveType}. Therefore {@code int}s are mapped to attributes, elements
   * and element arrays.
   */
  @Test
  public void testParseToAnyTypeStringOnly() throws Exception {
    AnyTypePrimitiveString xml = new AnyTypePrimitiveString();
    XmlPullParser parser = Xml.createParser();
    parser.setInput(new StringReader(ANY_TYPE_XML_PRIMITIVE_STR));
    XmlNamespaceDictionary namespaceDictionary = new XmlNamespaceDictionary();
    Xml.parseElement(parser, xml, namespaceDictionary, new Xml.CustomizeParser());
    assertEquals("1+1", xml.value);
    assertEquals("2+1", xml.attr);
    assertEquals(2, xml.strArray.length);
    assertEquals("1+1", xml.strArray[0]);
    assertEquals("2+1", xml.strArray[1]);
    // serialize
    XmlSerializer serializer = Xml.createSerializer();
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    serializer.setOutput(out, "UTF-8");
    namespaceDictionary.serialize(serializer, "any", xml);
    assertEquals(ANY_TYPE_XML_PRIMITIVE_STR, out.toString());
  }

  /** The purpose of this test is to map nested elements with a namespace attribute. */
  @Test
  public void testParseOfNestedNs() throws Exception {
    XmlPullParser parser = Xml.createParser();
    parser.setInput(new StringReader(NESTED_NS));
    XmlNamespaceDictionary namespaceDictionary = new XmlNamespaceDictionary();
    GenericXml xml = new GenericXml();
    Xml.parseElement(parser, xml, namespaceDictionary, null);
    // GenericXml anyValue = (GenericXml) xml.get("any");
    // serialize
    XmlSerializer serializer = Xml.createSerializer();
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    serializer.setOutput(out, "UTF-8");
    namespaceDictionary.serialize(serializer, "any", xml);
    assertEquals(NESTED_NS_SERIALIZED, out.toString());
  }

  /**
   * The purpose of this test is to map the infinity values of both {@code doubles} and {@code
   * floats}.
   */
  @Test
  public void testParseInfiniteValues() throws Exception {
    AnyTypeInf xml = new AnyTypeInf();
    XmlPullParser parser = Xml.createParser();
    parser.setInput(new StringReader(INF_TEST));
    XmlNamespaceDictionary namespaceDictionary = new XmlNamespaceDictionary().set("", "");
    Xml.parseElement(parser, xml, namespaceDictionary, null);
    // check type
    assertEquals(Double.NEGATIVE_INFINITY, xml.dblInfNeg, 0.0001);
    assertEquals(Double.POSITIVE_INFINITY, xml.dblInfPos, 0.0001);
    assertEquals(Float.NEGATIVE_INFINITY, xml.fltInfNeg, 0.0001);
    assertEquals(Float.POSITIVE_INFINITY, xml.fltInfPos, 0.0001);
    // serialize
    XmlSerializer serializer = Xml.createSerializer();
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    serializer.setOutput(out, "UTF-8");
    namespaceDictionary.serialize(serializer, "any", xml);
    assertEquals(INF_TEST, out.toString());
  }

  /**
   * The purpose of this test is to map multiple different data types in a single test, without
   * data. (explorative)
   */
  @Test
  public void testParseEmptyElements() throws Exception {
    AllType xml = new AllType();
    XmlPullParser parser = Xml.createParser();
    parser.setInput(new StringReader(ALL_TYPE));
    XmlNamespaceDictionary namespaceDictionary = new XmlNamespaceDictionary().set("", "");
    Xml.parseElement(parser, xml, namespaceDictionary, null);
    // check type
    assertEquals(0, xml.integer);
    // TODO: Shouldn't array size == 0? (currently generated via a = new x[1]).
    assertEquals(1, xml.stringArray.length);
    assertEquals(1, xml.anyEnum.length);
    assertNotNull(xml.genericXml);
    assertNotNull(xml.integerCollection);
    assertNull(xml.str);
    // serialize
    XmlSerializer serializer = Xml.createSerializer();
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    serializer.setOutput(out, "UTF-8");
    namespaceDictionary.serialize(serializer, "any", xml);
    assertEquals(
        "<?xml version=\"1.0\"?><any xmlns=\"\"><genericXml /><integer>0</integer" + "></any>",
        out.toString());
  }

  /**
   * The purpose of this test is to map multiple different data types in a single test, with data.
   * (explorative)
   */
  @Test
  public void testParseAllElements() throws Exception {
    AllType xml = new AllType();
    XmlPullParser parser = Xml.createParser();
    parser.setInput(new StringReader(ALL_TYPE_WITH_DATA));
    XmlNamespaceDictionary namespaceDictionary = new XmlNamespaceDictionary().set("", "");
    Xml.parseElement(parser, xml, namespaceDictionary, null);
    // check type

    // serialize
    XmlSerializer serializer = Xml.createSerializer();
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    serializer.setOutput(out, "UTF-8");
    namespaceDictionary.serialize(serializer, "any", xml);
    assertEquals(ALL_TYPE_WITH_DATA, out.toString());
  }

  /**
   * The purpose of this tests is to map a completely unrelated XML to a given destination object.
   * (explorative)
   */
  @Test
  public void testParseIncorrectMapping() throws Exception {
    AnyType xml = new AnyType();
    XmlPullParser parser = Xml.createParser();
    parser.setInput(new StringReader(ALL_TYPE));
    XmlNamespaceDictionary namespaceDictionary = new XmlNamespaceDictionary().set("", "");
    Xml.parseElement(parser, xml, namespaceDictionary, null);
    // check type
    assertNull(xml.elem);
    assertNull(xml.value);
    assertNull(xml.rep);
    assertNull(xml.rep);
    // serialize
    XmlSerializer serializer = Xml.createSerializer();
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    serializer.setOutput(out, "UTF-8");
    namespaceDictionary.serialize(serializer, "any", xml);
    assertEquals("<?xml version=\"1.0\"?><any xmlns=\"\" />", out.toString());
  }

  /**
   * The purpose of this test is to map the sub elements of an {@link ArrayMap} again to an {@link
   * ArrayMap}.
   */
  @Test
  public void testParseAnyTypeWithNestedElementArrayMap() throws Exception {
    AnyType xml = new AnyType();
    XmlPullParser parser = Xml.createParser();
    parser.setInput(new StringReader(ANY_TYPE_XML_NESTED_ARRAY));
    XmlNamespaceDictionary namespaceDictionary = new XmlNamespaceDictionary();
    Xml.parseElement(parser, xml, namespaceDictionary, null);
    assertTrue(xml.attr instanceof String);
    assertTrue(xml.elem.toString(), xml.elem instanceof ArrayList<?>);
    assertTrue(xml.rep.toString(), xml.rep instanceof ArrayList<?>);
    assertNotNull(xml.value);
    assertTrue(xml.value.content instanceof String);
    assertEquals(1, ((Collection<?>) xml.elem).size());
    assertEquals(2, ((Collection<?>) xml.rep).size());
    assertEquals(1, ((Collection<?>) xml.rep).toArray(new ArrayMap[] {})[0].size());
    assertEquals(1, ((Collection<?>) xml.rep).toArray(new ArrayMap[] {})[1].size());
    assertEquals(
        "rep1",
        ((ArrayList<?>) ((ArrayList<?>) xml.rep).toArray(new ArrayMap[] {})[0].get("p"))
            .toArray(new ArrayMap[] {})[0].getValue(0));
    assertEquals(
        "rep2",
        ((ArrayList<?>) ((ArrayList<?>) xml.rep).toArray(new ArrayMap[] {})[0].get("p"))
            .toArray(new ArrayMap[] {})[1].getValue(0));
    assertEquals(
        "rep3",
        ((ArrayList<?>) ((ArrayList<?>) xml.rep).toArray(new ArrayMap[] {})[1].get("p"))
            .toArray(new ArrayMap[] {})[0].getValue(0));
    assertEquals(
        "rep4",
        ((ArrayList<?>) ((ArrayList<?>) xml.rep).toArray(new ArrayMap[] {})[1].get("p"))
            .toArray(new ArrayMap[] {})[1].getValue(0));

    // serialize
    XmlSerializer serializer = Xml.createSerializer();
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    serializer.setOutput(out, "UTF-8");
    namespaceDictionary.serialize(serializer, "any", xml);
    assertEquals(ANY_TYPE_XML_NESTED_ARRAY, out.toString());
  }

  public static class SimpleTypeString {
    @Key("text()")
    public String value;
  }

  public static class SimpleTypeNumeric {
    @Key("text()")
    public int value;
  }

  public static class AnyType {
    @Key("@attr")
    public Object attr;

    @Key public Object elem;
    @Key public Object rep;
    @Key public ValueType value;
  }

  public static class AnyTypeMissingField {
    @Key("@attr")
    public Object attr;

    @Key public Object elem;
    @Key public ValueType value;
  }

  public static class AnyTypeAdditionalField {
    @Key("@attr")
    public Object attr;

    @Key public Object elem;
    @Key public Object rep;
    @Key public Object additionalField;
    @Key public ValueType value;
  }

  public static class ValueType {
    @Key("text()")
    public Object content;
  }

  public static class AnyTypePrimitiveInt {
    @Key("text()")
    public int value;

    @Key("@attr")
    public int attr;

    @Key public int[] intArray;
  }

  public static class AnyTypePrimitiveString {
    @Key("text()")
    public String value;

    @Key("@attr")
    public String attr;

    @Key public String[] strArray;
  }

  private static class AnyTypeInf {
    @Key public double dblInfNeg;
    @Key public double dblInfPos;
    @Key public float fltInfNeg;
    @Key public float fltInfPos;
  }

  private static class AllType {
    @Key public int integer;
    @Key public String str;
    @Key public GenericXml genericXml;
    @Key public XmlEnumTest.AnyEnum[] anyEnum;
    @Key public String[] stringArray;
    @Key public List<Integer> integerCollection;
  }
}
