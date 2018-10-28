package com.google.api.client.xml;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import java.io.ByteArrayOutputStream;
import java.io.StringReader;
import java.util.ArrayList;
import org.junit.Test;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlSerializer;
import com.google.api.client.util.Key;
import com.google.api.client.util.Value;

/**
 * Tests {@link Xml}.
 *
 * @author Gerald Madlmayr
 */
public class XmlEnumTest {

  public enum AnyEnum {
    @Value ENUM_1,
    @Value ENUM_2
  }

  public static class AnyType {
    @Key("@attr")
    public Object attr;
    @Key
    public Object elem;
    @Key
    public Object rep;
    @Key("@anyEnum")
    public XmlEnumTest.AnyEnum anyEnum;
    @Key
    public XmlEnumTest.AnyEnum anotherEnum;
    @Key
    public ValueType value;
  }

  public static class AnyTypeEnumElementOnly {
    @Key
    public XmlEnumTest.AnyEnum elementEnum;
  }

  public static class AnyTypeEnumAttributeOnly {
    @Key("@attributeEnum")
    public XmlEnumTest.AnyEnum attributeEnum;
  }

  public static class ValueType {
    @Key("text()")
    public XmlEnumTest.AnyEnum content;
  }

  private static final String XML =
      "<?xml version=\"1.0\"?><any anyEnum=\"ENUM_1\" attr=\"value\" xmlns=\"http://www.w3.org/2005/Atom\">"
          + "<anotherEnum>ENUM_2</anotherEnum><elem>content</elem><rep>rep1</rep><rep>rep2</rep><value>ENUM_1</value></any>";

  private static final String XML_ENUM_ELEMENT_ONLY =
      "<?xml version=\"1.0\"?><any xmlns=\"http://www.w3.org/2005/Atom\"><elementEnum>ENUM_2</elementEnum></any>";

  private static final String XML_ENUM_ATTRIBUTE_ONLY =
      "<?xml version=\"1.0\"?><any attributeEnum=\"ENUM_1\" xmlns=\"http://www.w3.org/2005/Atom\" />";

  private static final String XML_ENUM_INCORRECT =
      "<?xml version=\"1.0\"?><any xmlns=\"http://www.w3.org/2005/Atom\"><elementEnum>ENUM_3</elementEnum></any>";


  private static final String XML_ENUM_ELEMENT_ONLY_NESTED =
      "<?xml version=\"1.0\"?><any xmlns=\"http://www.w3.org/2005/Atom\"><elementEnum>ENUM_2<nested>something</nested></elementEnum></any>";

  @SuppressWarnings("cast")
  @Test
  public void testParse_anyType() throws Exception {
    AnyType xml = new AnyType();
    XmlPullParser parser = Xml.createParser();
    parser.setInput(new StringReader(XML));
    XmlNamespaceDictionary namespaceDictionary = new XmlNamespaceDictionary();
    Xml.parseElement(parser, xml, namespaceDictionary, null);
    assertTrue(xml.attr instanceof String);
    assertTrue(xml.elem.toString(), xml.elem instanceof ArrayList<?>);
    assertTrue(xml.rep.toString(), xml.rep instanceof ArrayList<?>);
    assertTrue(xml.value instanceof ValueType);
    assertTrue(xml.value.content instanceof XmlEnumTest.AnyEnum);
    assertTrue(xml.anyEnum instanceof XmlEnumTest.AnyEnum);
    assertTrue(xml.anotherEnum instanceof XmlEnumTest.AnyEnum);
    assertTrue(xml.anyEnum.equals(AnyEnum.ENUM_1));
    assertTrue(xml.anotherEnum.equals(AnyEnum.ENUM_2));
    assertTrue(xml.value.content.equals(AnyEnum.ENUM_1));
    // serialize
    XmlSerializer serializer = Xml.createSerializer();
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    serializer.setOutput(out, "UTF-8");
    namespaceDictionary.serialize(serializer, "any", xml);
    assertEquals(XML, out.toString());
  }

  /**
   * The purpose of this test is to parse an XML element to an objects's member variable
   */
  @Test
  public void testParseToEnumElementType() throws Exception {
    assertEquals(XML_ENUM_ELEMENT_ONLY, testStandardXml(XML_ENUM_ELEMENT_ONLY));
  }


  /**
   * The purpose of this test is to parse an XML element to an objects's member variable, whereas
   * there are additional nested elements in the tag.
   */
  @Test
  public void testParseToEnumElementTypeWithNestedElement() throws Exception {
    assertEquals(XML_ENUM_ELEMENT_ONLY, testStandardXml(XML_ENUM_ELEMENT_ONLY_NESTED));
  }

  /**
   * Private Method to handle standard parsing and mapping to {@link AnyTypeEnumElementOnly}
   * @param xmlString XML String that needs to be mapped to {@link AnyTypeEnumElementOnly}
   * @return Returns the serialized string of the XML Objects
   * @throws Exception
   */
  private String testStandardXml(final String xmlString) throws Exception {
    AnyTypeEnumElementOnly xml = new AnyTypeEnumElementOnly();
    XmlPullParser parser = Xml.createParser();
    parser.setInput(new StringReader(xmlString));
    XmlNamespaceDictionary namespaceDictionary = new XmlNamespaceDictionary();
    Xml.parseElement(parser, xml, namespaceDictionary, null);
    assertTrue(xml.elementEnum instanceof XmlEnumTest.AnyEnum);
    assertTrue(xml.elementEnum.equals(AnyEnum.ENUM_2));
    // serialize
    XmlSerializer serializer = Xml.createSerializer();
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    serializer.setOutput(out, "UTF-8");
    namespaceDictionary.serialize(serializer, "any", xml);
    return out.toString();

  }

  /**
   * The purpose of this test is to parse an XML attribute to an objects's member variable
   */
  @Test
  public void testParse_enumAttributeType() throws Exception {
    XmlEnumTest.AnyTypeEnumAttributeOnly xml = new XmlEnumTest.AnyTypeEnumAttributeOnly();
    XmlPullParser parser = Xml.createParser();
    parser.setInput(new StringReader(XML_ENUM_ATTRIBUTE_ONLY));
    XmlNamespaceDictionary namespaceDictionary = new XmlNamespaceDictionary();
    Xml.parseElement(parser, xml, namespaceDictionary, null);
    assertTrue(xml.attributeEnum instanceof XmlEnumTest.AnyEnum);
    assertTrue(xml.attributeEnum.equals(AnyEnum.ENUM_1));
    // serialize
    XmlSerializer serializer = Xml.createSerializer();
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    serializer.setOutput(out, "UTF-8");
    namespaceDictionary.serialize(serializer, "any", xml);
    assertEquals(XML_ENUM_ATTRIBUTE_ONLY, out.toString());
  }

  /**
   * The purpose of this test is to parse an XML element to an objects's member variable, whereas
   * the enumeration element does not exist.
   */
  @Test
  public void testParse_enumElementTypeIncorrect() throws Exception {
    XmlEnumTest.AnyTypeEnumElementOnly xml = new XmlEnumTest.AnyTypeEnumElementOnly();
    XmlPullParser parser = Xml.createParser();
    parser.setInput(new StringReader(XML_ENUM_INCORRECT));
    XmlNamespaceDictionary namespaceDictionary = new XmlNamespaceDictionary();
    try{
      Xml.parseElement(parser, xml, namespaceDictionary, null);
      // fail test, if there is no exception
      fail();
    } catch (final IllegalArgumentException e){
      assertEquals("given enum name ENUM_3 not part of enumeration", e.getMessage());
    }
  }
}
