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

package com.google.api.client.test.json;

import com.google.api.client.http.json.JsonHttpContent;
import com.google.api.client.json.GenericJson;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.JsonGenerator;
import com.google.api.client.json.JsonObjectParser;
import com.google.api.client.json.JsonParser;
import com.google.api.client.json.JsonPolymorphicTypeMap;
import com.google.api.client.json.JsonPolymorphicTypeMap.TypeDef;
import com.google.api.client.json.JsonString;
import com.google.api.client.json.JsonToken;
import com.google.api.client.util.ArrayMap;
import com.google.api.client.util.Data;
import com.google.api.client.util.Key;
import com.google.api.client.util.NullValue;
import com.google.api.client.util.StringUtils;
import com.google.api.client.util.Value;
import com.google.common.base.Charsets;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.reflect.TypeToken;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import junit.framework.TestCase;

/**
 * Abstract test case for testing a {@link JsonFactory}.
 *
 * @author Yaniv Inbar
 */
public abstract class AbstractJsonFactoryTest extends TestCase {

  public AbstractJsonFactoryTest(String name) {
    super(name);
  }

  protected abstract JsonFactory newFactory();

  private static final String EMPTY = "";
  private static final String JSON_THREE_ELEMENTS =
      "{"
          + "  \"one\": { \"num\": 1 }"
          + ", \"two\": { \"num\": 2 }"
          + ", \"three\": { \"num\": 3 }"
          + "}";

  public void testParse_empty() throws Exception {
    JsonParser parser = newFactory().createJsonParser(EMPTY);
    parser.nextToken();
    try {
      parser.parseAndClose(HashMap.class);
      fail("expected " + IllegalArgumentException.class);
    } catch (IllegalArgumentException e) {
      // expected
    }
  }

  private static final String EMPTY_OBJECT = "{}";

  public void testParse_emptyMap() throws Exception {
    JsonParser parser = newFactory().createJsonParser(EMPTY_OBJECT);
    parser.nextToken();
    @SuppressWarnings("unchecked")
    HashMap<String, Object> map = parser.parseAndClose(HashMap.class);
    assertTrue(map.isEmpty());
  }

  public void testParse_emptyGenericJson() throws Exception {
    JsonParser parser = newFactory().createJsonParser(EMPTY_OBJECT);
    parser.nextToken();
    GenericJson json = parser.parseAndClose(GenericJson.class);
    assertTrue(json.isEmpty());
  }

  public void testParser_partialEmpty() throws Exception {
    JsonFactory factory = newFactory();
    JsonParser parser;
    parser = factory.createJsonParser(EMPTY_OBJECT);
    parser.nextToken();
    parser.nextToken();
    // current token is now end_object
    @SuppressWarnings("unchecked")
    HashMap<String, Object> result = parser.parseAndClose(HashMap.class);
    assertEquals(EMPTY_OBJECT, factory.toString(result));
    // check types and values
    assertTrue(result.isEmpty());
  }

  private static final String JSON_ENTRY = "{\"title\":\"foo\"}";
  private static final String JSON_FEED =
      "{\"entries\":[" + "{\"title\":\"foo\"}," + "{\"title\":\"bar\"}]}";

  public void testParseEntry() throws Exception {
    Entry entry = newFactory().createJsonParser(JSON_ENTRY).parseAndClose(Entry.class);
    assertEquals("foo", entry.title);
  }

  public void testParser_partialEntry() throws Exception {
    JsonFactory factory = newFactory();
    JsonParser parser;
    parser = factory.createJsonParser(JSON_ENTRY);
    parser.nextToken();
    parser.nextToken();
    // current token is now field_name
    Entry result = parser.parseAndClose(Entry.class);
    assertEquals(JSON_ENTRY, factory.toString(result));
    // check types and values
    assertEquals("foo", result.title);
  }

  public void testParseFeed() throws Exception {
    JsonParser parser = newFactory().createJsonParser(JSON_FEED);
    parser.nextToken();
    Feed feed = parser.parseAndClose(Feed.class);
    Iterator<Entry> iterator = feed.entries.iterator();
    assertEquals("foo", iterator.next().title);
    assertEquals("bar", iterator.next().title);
    assertFalse(iterator.hasNext());
  }

  @SuppressWarnings("unchecked")
  public void testParseEntryAsMap() throws Exception {
    JsonParser parser = newFactory().createJsonParser(JSON_ENTRY);
    parser.nextToken();
    HashMap<String, Object> map = parser.parseAndClose(HashMap.class);
    assertEquals("foo", map.remove("title"));
    assertTrue(map.isEmpty());
  }

  public void testSkipToKey_missingEmpty() throws Exception {
    JsonParser parser = newFactory().createJsonParser(EMPTY_OBJECT);
    parser.nextToken();
    parser.skipToKey("missing");
    assertEquals(JsonToken.END_OBJECT, parser.getCurrentToken());
  }

  public void testSkipToKey_missing() throws Exception {
    JsonParser parser = newFactory().createJsonParser(JSON_ENTRY);
    parser.nextToken();
    parser.skipToKey("missing");
    assertEquals(JsonToken.END_OBJECT, parser.getCurrentToken());
  }

  public void testSkipToKey_found() throws Exception {
    JsonParser parser = newFactory().createJsonParser(JSON_ENTRY);
    parser.nextToken();
    parser.skipToKey("title");
    assertEquals(JsonToken.VALUE_STRING, parser.getCurrentToken());
    assertEquals("foo", parser.getText());
  }

  public void testSkipToKey_startWithFieldName() throws Exception {
    JsonParser parser = newFactory().createJsonParser(JSON_ENTRY);
    parser.nextToken();
    parser.nextToken();
    parser.skipToKey("title");
    assertEquals(JsonToken.VALUE_STRING, parser.getCurrentToken());
    assertEquals("foo", parser.getText());
  }

  public void testSkipChildren_string() throws Exception {
    JsonParser parser = newFactory().createJsonParser(JSON_ENTRY);
    parser.nextToken();
    parser.skipToKey("title");
    parser.skipChildren();
    assertEquals(JsonToken.VALUE_STRING, parser.getCurrentToken());
    assertEquals("foo", parser.getText());
  }

  public void testSkipChildren_object() throws Exception {
    JsonParser parser = newFactory().createJsonParser(JSON_ENTRY);
    parser.nextToken();
    parser.skipChildren();
    assertEquals(JsonToken.END_OBJECT, parser.getCurrentToken());
  }

  public void testSkipChildren_array() throws Exception {
    JsonParser parser = newFactory().createJsonParser(JSON_FEED);
    parser.nextToken();
    parser.skipToKey("entries");
    parser.skipChildren();
    assertEquals(JsonToken.END_ARRAY, parser.getCurrentToken());
  }

  public void testNextToken() throws Exception {
    JsonParser parser = newFactory().createJsonParser(JSON_FEED);
    assertEquals(JsonToken.START_OBJECT, parser.nextToken());
    assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
    assertEquals(JsonToken.START_ARRAY, parser.nextToken());
    assertEquals(JsonToken.START_OBJECT, parser.nextToken());
    assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
    assertEquals(JsonToken.VALUE_STRING, parser.nextToken());
    assertEquals(JsonToken.END_OBJECT, parser.nextToken());
    assertEquals(JsonToken.START_OBJECT, parser.nextToken());
    assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
    assertEquals(JsonToken.VALUE_STRING, parser.nextToken());
    assertEquals(JsonToken.END_OBJECT, parser.nextToken());
    assertEquals(JsonToken.END_ARRAY, parser.nextToken());
    assertEquals(JsonToken.END_OBJECT, parser.nextToken());
    assertNull(parser.nextToken());
  }

  public void testCurrentToken() throws Exception {
    JsonParser parser = newFactory().createJsonParser(JSON_FEED);
    assertNull(parser.getCurrentToken());
    parser.nextToken();
    assertEquals(JsonToken.START_OBJECT, parser.getCurrentToken());
    parser.nextToken();
    assertEquals(JsonToken.FIELD_NAME, parser.getCurrentToken());
    parser.nextToken();
    assertEquals(JsonToken.START_ARRAY, parser.getCurrentToken());
    parser.nextToken();
    assertEquals(JsonToken.START_OBJECT, parser.getCurrentToken());
    parser.nextToken();
    assertEquals(JsonToken.FIELD_NAME, parser.getCurrentToken());
    parser.nextToken();
    assertEquals(JsonToken.VALUE_STRING, parser.getCurrentToken());
    parser.nextToken();
    assertEquals(JsonToken.END_OBJECT, parser.getCurrentToken());
    parser.nextToken();
    assertEquals(JsonToken.START_OBJECT, parser.getCurrentToken());
    parser.nextToken();
    assertEquals(JsonToken.FIELD_NAME, parser.getCurrentToken());
    parser.nextToken();
    assertEquals(JsonToken.VALUE_STRING, parser.getCurrentToken());
    parser.nextToken();
    assertEquals(JsonToken.END_OBJECT, parser.getCurrentToken());
    parser.nextToken();
    assertEquals(JsonToken.END_ARRAY, parser.getCurrentToken());
    parser.nextToken();
    assertEquals(JsonToken.END_OBJECT, parser.getCurrentToken());
    parser.nextToken();
    assertNull(parser.getCurrentToken());
  }

  public static class Entry {
    @Key public String title;
  }

  public static class Feed {
    @Key public Collection<Entry> entries;
  }

  public static class A {
    @Key public Map<String, String> map;
  }

  static final String CONTAINED_MAP = "{\"map\":{\"title\":\"foo\"}}";

  public void testParse() throws Exception {
    JsonParser parser = newFactory().createJsonParser(CONTAINED_MAP);
    parser.nextToken();
    A a = parser.parse(A.class);
    assertEquals(ImmutableMap.of("title", "foo"), a.map);
  }

  public static class NumberTypes {
    @Key byte byteValue;
    @Key Byte byteObjValue;
    @Key short shortValue;
    @Key Short shortObjValue;
    @Key int intValue;
    @Key Integer intObjValue;
    @Key float floatValue;
    @Key Float floatObjValue;
    @Key long longValue;
    @Key Long longObjValue;
    @Key double doubleValue;
    @Key Double doubleObjValue;
    @Key BigInteger bigIntegerValue;
    @Key BigDecimal bigDecimalValue;

    @Key("yetAnotherBigDecimalValue")
    BigDecimal anotherBigDecimalValue;

    @Key List<Long> longListValue;

    @Key Map<String, Long> longMapValue;
  }

  public static class NumberTypesAsString {
    @Key @JsonString byte byteValue;
    @Key @JsonString Byte byteObjValue;
    @Key @JsonString short shortValue;
    @Key @JsonString Short shortObjValue;
    @Key @JsonString int intValue;
    @Key @JsonString Integer intObjValue;
    @Key @JsonString float floatValue;
    @Key @JsonString Float floatObjValue;
    @Key @JsonString long longValue;
    @Key @JsonString Long longObjValue;
    @Key @JsonString double doubleValue;
    @Key @JsonString Double doubleObjValue;
    @Key @JsonString BigInteger bigIntegerValue;
    @Key @JsonString BigDecimal bigDecimalValue;

    @Key("yetAnotherBigDecimalValue")
    @JsonString
    BigDecimal anotherBigDecimalValue;

    @Key @JsonString List<Long> longListValue;

    @Key @JsonString Map<String, Long> longMapValue;
  }

  static final String NUMBER_TYPES =
      "{\"bigDecimalValue\":1.0,\"bigIntegerValue\":1,\"byteObjValue\":1,\"byteValue\":1,"
          + "\"doubleObjValue\":1.0,\"doubleValue\":1.0,\"floatObjValue\":1.0,\"floatValue\":1.0,"
          + "\"intObjValue\":1,\"intValue\":1,\"longListValue\":[1],\"longMapValue\":{\"a\":1},"
          + "\"longObjValue\":1,\"longValue\":1,\"shortObjValue\":1,\"shortValue\":1,"
          + "\"yetAnotherBigDecimalValue\":1}";

  static final String NUMBER_TYPES_AS_STRING =
      "{\"bigDecimalValue\":\"1.0\",\"bigIntegerValue\":\"1\",\"byteObjValue\":\"1\","
          + "\"byteValue\":\"1\",\"doubleObjValue\":\"1.0\",\"doubleValue\":\"1.0\","
          + "\"floatObjValue\":\"1.0\",\"floatValue\":\"1.0\",\"intObjValue\":\"1\","
          + "\"intValue\":\"1\",\"longListValue\":[\"1\"],\"longMapValue\":{\"a\":\"1\"},"
          + "\"longObjValue\":\"1\",\"longValue\":\"1\","
          + "\"shortObjValue\":\"1\","
          + "\"shortValue\":\"1\",\"yetAnotherBigDecimalValue\":\"1\"}";

  public void testParser_numberTypes() throws Exception {
    JsonFactory factory = newFactory();
    JsonParser parser;
    // number types
    parser = factory.createJsonParser(NUMBER_TYPES);
    parser.nextToken();
    NumberTypes result = parser.parse(NumberTypes.class);
    assertEquals(NUMBER_TYPES, factory.toString(result));
    // number types as string
    parser = factory.createJsonParser(NUMBER_TYPES_AS_STRING);
    parser.nextToken();
    NumberTypesAsString resultAsString = parser.parse(NumberTypesAsString.class);
    assertEquals(NUMBER_TYPES_AS_STRING, factory.toString(resultAsString));
    // number types with @JsonString
    try {
      parser = factory.createJsonParser(NUMBER_TYPES_AS_STRING);
      parser.nextToken();
      parser.parse(NumberTypes.class);
      fail("expected IllegalArgumentException");
    } catch (IllegalArgumentException e) {
      // expected
    }
    // number types as string without @JsonString
    try {
      parser = factory.createJsonParser(NUMBER_TYPES);
      parser.nextToken();
      parser.parse(NumberTypesAsString.class);
      fail("expected IllegalArgumentException");
    } catch (IllegalArgumentException e) {
      // expected
    }
  }

  public void testToFromString() throws Exception {
    JsonFactory factory = newFactory();
    NumberTypes result = factory.fromString(NUMBER_TYPES, NumberTypes.class);
    assertEquals(NUMBER_TYPES, factory.toString(result));
  }

  private static final String UTF8_VALUE = "123\u05D9\u05e0\u05D9\u05D1";
  private static final String UTF8_JSON = "{\"value\":\"" + UTF8_VALUE + "\"}";

  public void testToFromString_UTF8() throws Exception {
    JsonFactory factory = newFactory();
    GenericJson result = factory.fromString(UTF8_JSON, GenericJson.class);
    assertEquals(UTF8_VALUE, result.get("value"));
    assertEquals(UTF8_JSON, factory.toString(result));
  }

  public static class AnyType {
    @Key public Object arr;
    @Key public Object bool;
    @Key public Object num;
    @Key public Object obj;
    @Key public Object str;
    @Key public Object nul;
  }

  static final String ANY_TYPE =
      "{\"arr\":[1],\"bool\":true,\"nul\":null,\"num\":5,"
          + "\"obj\":{\"key\":\"value\"},\"str\":\"value\"}";

  public void testParser_anyType() throws Exception {
    JsonFactory factory = newFactory();
    JsonParser parser;
    parser = factory.createJsonParser(ANY_TYPE);
    parser.nextToken();
    AnyType result = parser.parse(AnyType.class);
    assertEquals(ANY_TYPE, factory.toString(result));
  }

  public static class ArrayType {
    @Key int[] arr;

    @Key int[][] arr2;

    @Key public Integer[] integerArr;
  }

  static final String ARRAY_TYPE = "{\"arr\":[4,5],\"arr2\":[[1,2],[3]],\"integerArr\":[6,7]}";

  public void testParser_arrayType() throws Exception {
    JsonFactory factory = newFactory();
    JsonParser parser;
    parser = factory.createJsonParser(ARRAY_TYPE);
    parser.nextToken();
    ArrayType result = parser.parse(ArrayType.class);
    assertEquals(ARRAY_TYPE, factory.toString(result));
    // check types and values
    int[] arr = result.arr;
    assertTrue(Arrays.equals(new int[] {4, 5}, arr));
    int[][] arr2 = result.arr2;
    assertEquals(2, arr2.length);
    int[] arr20 = arr2[0];
    assertEquals(2, arr20.length);
    int arr200 = arr20[0];
    assertEquals(1, arr200);
    assertEquals(2, arr20[1]);
    int[] arr21 = arr2[1];
    assertEquals(1, arr21.length);
    assertEquals(3, arr21[0]);
    Integer[] integerArr = result.integerArr;
    assertEquals(2, integerArr.length);
    assertEquals(6, integerArr[0].intValue());
  }

  public static class CollectionOfCollectionType {
    @Key public LinkedList<LinkedList<String>> arr;
  }

  static final String COLLECTION_TYPE = "{\"arr\":[[\"a\",\"b\"],[\"c\"]]}";

  public void testParser_collectionType() throws Exception {
    JsonFactory factory = newFactory();
    JsonParser parser;
    parser = factory.createJsonParser(COLLECTION_TYPE);
    parser.nextToken();
    CollectionOfCollectionType result = parser.parse(CollectionOfCollectionType.class);
    assertEquals(COLLECTION_TYPE, factory.toString(result));
    // check that it is actually a linked list
    LinkedList<LinkedList<String>> arr = result.arr;
    LinkedList<String> linkedlist = arr.get(0);
    assertEquals("a", linkedlist.get(0));
  }

  public static class MapOfMapType {
    @Key public Map<String, Map<String, Integer>>[] value;
  }

  static final String MAP_TYPE =
      "{\"value\":[{\"map1\":{\"k1\":1,\"k2\":2},\"map2\":{\"kk1\":3,\"kk2\":4}}]}";

  public void testParser_mapType() throws Exception {
    // parse
    JsonFactory factory = newFactory();
    JsonParser parser;
    parser = factory.createJsonParser(MAP_TYPE);
    parser.nextToken();
    MapOfMapType result = parser.parse(MapOfMapType.class);
    // serialize
    assertEquals(MAP_TYPE, factory.toString(result));
    // check parsed result
    Map<String, Map<String, Integer>>[] value = result.value;
    Map<String, Map<String, Integer>> firstMap = value[0];
    Map<String, Integer> map1 = firstMap.get("map1");
    Integer integer = map1.get("k1");
    assertEquals(1, integer.intValue());
    Map<String, Integer> map2 = firstMap.get("map2");
    assertEquals(3, map2.get("kk1").intValue());
    assertEquals(4, map2.get("kk2").intValue());
  }

  public void testParser_hashmapForMapType() throws Exception {
    // parse
    JsonFactory factory = newFactory();
    JsonParser parser;
    parser = factory.createJsonParser(MAP_TYPE);
    parser.nextToken();
    @SuppressWarnings("unchecked")
    HashMap<String, ArrayList<ArrayMap<String, ArrayMap<String, BigDecimal>>>> result =
        parser.parse(HashMap.class);
    // serialize
    assertEquals(MAP_TYPE, factory.toString(result));
    // check parsed result
    ArrayList<ArrayMap<String, ArrayMap<String, BigDecimal>>> value = result.get("value");
    ArrayMap<String, ArrayMap<String, BigDecimal>> firstMap = value.get(0);
    ArrayMap<String, BigDecimal> map1 = firstMap.get("map1");
    BigDecimal integer = map1.get("k1");
    assertEquals(1, integer.intValue());
  }

  public static class WildCardTypes {
    @Key public Collection<? super Integer>[] lower;
    @Key public Map<String, ?> map;
    @Key public Collection<? super TreeMap<String, ? extends Integer>> mapInWild;
    @Key public Map<String, ? extends Integer> mapUpper;
    @Key public Collection<?>[] simple;
    @Key public Collection<? extends Integer>[] upper;
  }

  static final String WILDCARD_TYPE =
      "{\"lower\":[[1,2,3]],\"map\":{\"v\":1},\"mapInWild\":[{\"v\":1}],"
          + "\"mapUpper\":{\"v\":1},\"simple\":[[1,2,3]],\"upper\":[[1,2,3]]}";

  @SuppressWarnings("unchecked")
  public void testParser_wildCardType() throws Exception {
    // parse
    JsonFactory factory = newFactory();
    JsonParser parser;
    parser = factory.createJsonParser(WILDCARD_TYPE);
    parser.nextToken();
    WildCardTypes result = parser.parse(WildCardTypes.class);
    // serialize
    assertEquals(WILDCARD_TYPE, factory.toString(result));
    // check parsed result
    Collection<?>[] simple = result.simple;
    ArrayList<BigDecimal> wildcard = (ArrayList<BigDecimal>) simple[0];
    BigDecimal wildcardFirstValue = wildcard.get(0);
    assertEquals(1, wildcardFirstValue.intValue());
    Collection<? extends Integer>[] upper = result.upper;
    ArrayList<Integer> wildcardUpper = (ArrayList<Integer>) upper[0];
    Integer wildcardFirstValueUpper = wildcardUpper.get(0);
    assertEquals(1, wildcardFirstValueUpper.intValue());
    Collection<? super Integer>[] lower = result.lower;
    ArrayList<Integer> wildcardLower = (ArrayList<Integer>) lower[0];
    Integer wildcardFirstValueLower = wildcardLower.get(0);
    assertEquals(1, wildcardFirstValueLower.intValue());
    Map<String, BigDecimal> map = (Map<String, BigDecimal>) result.map;
    BigDecimal mapValue = map.get("v");
    assertEquals(1, mapValue.intValue());
    Map<String, Integer> mapUpper = (Map<String, Integer>) result.mapUpper;
    Integer mapUpperValue = mapUpper.get("v");
    assertEquals(1, mapUpperValue.intValue());
    Collection<? super TreeMap<String, ? extends Integer>> mapInWild = result.mapInWild;
    TreeMap<String, ? extends Integer> mapInWildFirst =
        (TreeMap<String, ? extends Integer>) mapInWild.toArray()[0];
    Integer mapInWildFirstValue = mapInWildFirst.get("v");
    assertEquals(1, mapInWildFirstValue.intValue());
  }

  public static class TypeVariableType<T> {

    @Key public T[][] arr;

    @Key public LinkedList<LinkedList<T>> list;

    @Key public T nullValue;

    @Key public T value;
  }

  public static class IntegerTypeVariableType extends TypeVariableType<Integer> {}

  public static class IntArrayTypeVariableType extends TypeVariableType<int[]> {}

  public static class DoubleListTypeVariableType extends TypeVariableType<List<Double>> {}

  public static class FloatMapTypeVariableType extends TypeVariableType<Map<String, Float>> {}

  static final String INTEGER_TYPE_VARIABLE_TYPE =
      "{\"arr\":[null,[null,1]],\"list\":[null,[null,1]],\"nullValue\":null,\"value\":1}";

  static final String INT_ARRAY_TYPE_VARIABLE_TYPE =
      "{\"arr\":[null,[null,[1]]],\"list\":[null,[null,[1]]],\"nullValue\":null,\"value\":[1]}";

  static final String DOUBLE_LIST_TYPE_VARIABLE_TYPE =
      "{\"arr\":[null,[null,[1.0]]],\"list\":[null,[null,[1.0]]],"
          + "\"nullValue\":null,\"value\":[1.0]}";

  static final String FLOAT_MAP_TYPE_VARIABLE_TYPE =
      "{\"arr\":[null,[null,{\"a\":1.0}]],\"list\":[null,[null,{\"a\":1.0}]],"
          + "\"nullValue\":null,\"value\":{\"a\":1.0}}";

  public void testParser_integerTypeVariableType() throws Exception {
    // parse
    JsonFactory factory = newFactory();
    JsonParser parser;
    parser = factory.createJsonParser(INTEGER_TYPE_VARIABLE_TYPE);
    parser.nextToken();
    IntegerTypeVariableType result = parser.parse(IntegerTypeVariableType.class);
    // serialize
    assertEquals(INTEGER_TYPE_VARIABLE_TYPE, factory.toString(result));
    // check parsed result
    // array
    Integer[][] arr = result.arr;
    assertEquals(2, arr.length);
    assertEquals(Data.nullOf(Integer[].class), arr[0]);
    Integer[] subArr = arr[1];
    assertEquals(2, subArr.length);
    assertEquals(Data.NULL_INTEGER, subArr[0]);
    Integer arrValue = subArr[1];
    assertEquals(1, arrValue.intValue());
    // collection
    LinkedList<LinkedList<Integer>> list = result.list;
    assertEquals(2, list.size());
    assertEquals(Data.nullOf(LinkedList.class), list.get(0));
    LinkedList<Integer> subList = list.get(1);
    assertEquals(2, subList.size());
    assertEquals(Data.NULL_INTEGER, subList.get(0));
    arrValue = subList.get(1);
    assertEquals(1, arrValue.intValue());
    // null value
    Integer nullValue = result.nullValue;
    assertEquals(Data.NULL_INTEGER, nullValue);
    // value
    Integer value = result.value;
    assertEquals(1, value.intValue());
  }

  public void testParser_intArrayTypeVariableType() throws Exception {
    // parse
    JsonFactory factory = newFactory();
    JsonParser parser;
    parser = factory.createJsonParser(INT_ARRAY_TYPE_VARIABLE_TYPE);
    parser.nextToken();
    IntArrayTypeVariableType result = parser.parse(IntArrayTypeVariableType.class);
    // serialize
    assertEquals(INT_ARRAY_TYPE_VARIABLE_TYPE, factory.toString(result));
    // check parsed result
    // array
    int[][][] arr = result.arr;
    assertEquals(2, arr.length);
    assertEquals(Data.nullOf(int[][].class), arr[0]);
    int[][] subArr = arr[1];
    assertEquals(2, subArr.length);
    assertEquals(Data.nullOf(int[].class), subArr[0]);
    int[] arrValue = subArr[1];
    assertTrue(Arrays.equals(new int[] {1}, arrValue));
    // collection
    LinkedList<LinkedList<int[]>> list = result.list;
    assertEquals(2, list.size());
    assertEquals(Data.nullOf(LinkedList.class), list.get(0));
    LinkedList<int[]> subList = list.get(1);
    assertEquals(2, subList.size());
    assertEquals(Data.nullOf(int[].class), subList.get(0));
    arrValue = subList.get(1);
    assertTrue(Arrays.equals(new int[] {1}, arrValue));
    // null value
    int[] nullValue = result.nullValue;
    assertEquals(Data.nullOf(int[].class), nullValue);
    // value
    int[] value = result.value;
    assertTrue(Arrays.equals(new int[] {1}, value));
  }

  public void testParser_doubleListTypeVariableType() throws Exception {
    // parse
    JsonFactory factory = newFactory();
    JsonParser parser;
    parser = factory.createJsonParser(DOUBLE_LIST_TYPE_VARIABLE_TYPE);
    parser.nextToken();
    DoubleListTypeVariableType result = parser.parse(DoubleListTypeVariableType.class);
    // serialize
    assertEquals(DOUBLE_LIST_TYPE_VARIABLE_TYPE, factory.toString(result));
    // check parsed result
    // array
    List<Double>[][] arr = result.arr;
    assertEquals(2, arr.length);
    assertEquals(Data.nullOf(List[].class), arr[0]);
    List<Double>[] subArr = arr[1];
    assertEquals(2, subArr.length);
    assertEquals(Data.nullOf(ArrayList.class), subArr[0]);
    List<Double> arrValue = subArr[1];
    assertEquals(1, arrValue.size());
    Double dValue = arrValue.get(0);
    assertEquals(1.0, dValue);
    // collection
    LinkedList<LinkedList<List<Double>>> list = result.list;
    assertEquals(2, list.size());
    assertEquals(Data.nullOf(LinkedList.class), list.get(0));
    LinkedList<List<Double>> subList = list.get(1);
    assertEquals(2, subList.size());
    assertEquals(Data.nullOf(ArrayList.class), subList.get(0));
    arrValue = subList.get(1);
    assertEquals(ImmutableList.of(Double.valueOf(1)), arrValue);
    // null value
    List<Double> nullValue = result.nullValue;
    assertEquals(Data.nullOf(ArrayList.class), nullValue);
    // value
    List<Double> value = result.value;
    assertEquals(ImmutableList.of(Double.valueOf(1)), value);
  }

  public void testParser_floatMapTypeVariableType() throws Exception {
    // parse
    JsonFactory factory = newFactory();
    JsonParser parser;
    parser = factory.createJsonParser(FLOAT_MAP_TYPE_VARIABLE_TYPE);
    parser.nextToken();
    FloatMapTypeVariableType result = parser.parse(FloatMapTypeVariableType.class);
    // serialize
    assertEquals(FLOAT_MAP_TYPE_VARIABLE_TYPE, factory.toString(result));
    // check parsed result
    // array
    Map<String, Float>[][] arr = result.arr;
    assertEquals(2, arr.length);
    assertEquals(Data.nullOf(Map[].class), arr[0]);
    Map<String, Float>[] subArr = arr[1];
    assertEquals(2, subArr.length);
    assertEquals(Data.nullOf(HashMap.class), subArr[0]);
    Map<String, Float> arrValue = subArr[1];
    assertEquals(1, arrValue.size());
    Float fValue = arrValue.get("a");
    assertEquals(1.0f, fValue);
    // collection
    LinkedList<LinkedList<Map<String, Float>>> list = result.list;
    assertEquals(2, list.size());
    assertEquals(Data.nullOf(LinkedList.class), list.get(0));
    LinkedList<Map<String, Float>> subList = list.get(1);
    assertEquals(2, subList.size());
    assertEquals(Data.nullOf(HashMap.class), subList.get(0));
    arrValue = subList.get(1);
    assertEquals(1, arrValue.size());
    fValue = arrValue.get("a");
    assertEquals(1.0f, fValue);
    // null value
    Map<String, Float> nullValue = result.nullValue;
    assertEquals(Data.nullOf(HashMap.class), nullValue);
    // value
    Map<String, Float> value = result.value;
    assertEquals(1, value.size());
    fValue = value.get("a");
    assertEquals(1.0f, fValue);
  }

  @SuppressWarnings("unchecked")
  public void testParser_treemapForTypeVariableType() throws Exception {
    // parse
    JsonFactory factory = newFactory();
    JsonParser parser;
    parser = factory.createJsonParser(INTEGER_TYPE_VARIABLE_TYPE);
    parser.nextToken();
    TreeMap<String, Object> result = parser.parse(TreeMap.class);
    // serialize
    assertEquals(INTEGER_TYPE_VARIABLE_TYPE, factory.toString(result));
    // check parsed result
    // array
    ArrayList<Object> arr = (ArrayList<Object>) result.get("arr");
    assertEquals(2, arr.size());
    assertEquals(Data.nullOf(Object.class), arr.get(0));
    ArrayList<BigDecimal> subArr = (ArrayList<BigDecimal>) arr.get(1);
    assertEquals(2, subArr.size());
    assertEquals(Data.nullOf(Object.class), subArr.get(0));
    BigDecimal arrValue = subArr.get(1);
    assertEquals(1, arrValue.intValue());
    // null value
    Object nullValue = result.get("nullValue");
    assertEquals(Data.nullOf(Object.class), nullValue);
    // value
    BigDecimal value = (BigDecimal) result.get("value");
    assertEquals(1, value.intValue());
  }

  public static class StringNullValue {
    @Key public String[][] arr2;
    @Key public String[] arr;
    @Key public String value;
  }

  static final String NULL_VALUE = "{\"arr\":[null],\"arr2\":[null,[null]],\"value\":null}";

  public void testParser_nullValue() throws Exception {
    // parse
    JsonFactory factory = newFactory();
    JsonParser parser;
    parser = factory.createJsonParser(NULL_VALUE);
    parser.nextToken();
    StringNullValue result = parser.parse(StringNullValue.class);
    // serialize
    assertEquals(NULL_VALUE, factory.toString(result));
    // check parsed result
    assertEquals(Data.NULL_STRING, result.value);
    String[] arr = result.arr;
    assertEquals(1, arr.length);
    assertEquals(Data.nullOf(String.class), arr[0]);
    String[][] arr2 = result.arr2;
    assertEquals(2, arr2.length);
    assertEquals(Data.nullOf(String[].class), arr2[0]);
    String[] subArr2 = arr2[1];
    assertEquals(1, subArr2.length);
    assertEquals(Data.NULL_STRING, subArr2[0]);
  }

  public enum E {
    @Value
    VALUE,
    @Value("other")
    OTHER_VALUE,
    @NullValue
    NULL,
    IGNORED_VALUE
  }

  public static class EnumValue {
    @Key public E value;
    @Key public E otherValue;
    @Key public E nullValue;
  }

  static final String ENUM_VALUE =
      "{\"nullValue\":null,\"otherValue\":\"other\",\"value\":\"VALUE\"}";

  public void testParser_enumValue() throws Exception {
    // parse
    JsonFactory factory = newFactory();
    JsonParser parser;
    parser = factory.createJsonParser(ENUM_VALUE);
    parser.nextToken();
    EnumValue result = parser.parse(EnumValue.class);
    // serialize
    assertEquals(ENUM_VALUE, factory.toString(result));
    // check parsed result
    assertEquals(E.VALUE, result.value);
    assertEquals(E.OTHER_VALUE, result.otherValue);
    assertEquals(E.NULL, result.nullValue);
  }

  public static class X<XT> {
    @Key Y<XT> y;
  }

  public static class Y<YT> {
    @Key Z<YT> z;
  }

  public static class Z<ZT> {
    @Key ZT f;
  }

  public static class TypeVariablesPassedAround extends X<LinkedList<String>> {}

  static final String TYPE_VARS = "{\"y\":{\"z\":{\"f\":[\"abc\"]}}}";

  public void testParser_typeVariablesPassAround() throws Exception {
    // parse
    JsonFactory factory = newFactory();
    JsonParser parser;
    parser = factory.createJsonParser(TYPE_VARS);
    parser.nextToken();
    TypeVariablesPassedAround result = parser.parse(TypeVariablesPassedAround.class);
    // serialize
    assertEquals(TYPE_VARS, factory.toString(result));
    // check parsed result
    LinkedList<String> f = result.y.z.f;
    assertEquals("abc", f.get(0));
  }

  static final String STRING_ARRAY = "[\"a\",\"b\",\"c\"]";

  public void testParser_stringArray() throws Exception {
    JsonFactory factory = newFactory();
    JsonParser parser;
    parser = factory.createJsonParser(STRING_ARRAY);
    parser.nextToken();
    String[] result = parser.parse(String[].class);
    assertEquals(STRING_ARRAY, factory.toString(result));
    // check types and values
    assertTrue(Arrays.equals(new String[] {"a", "b", "c"}, result));
  }

  static final String INT_ARRAY = "[1,2,3]";

  public void testParser_intArray() throws Exception {
    JsonFactory factory = newFactory();
    JsonParser parser;
    parser = factory.createJsonParser(INT_ARRAY);
    parser.nextToken();
    int[] result = parser.parse(int[].class);
    assertEquals(INT_ARRAY, factory.toString(result));
    // check types and values
    assertTrue(Arrays.equals(new int[] {1, 2, 3}, result));
  }

  private static final String EMPTY_ARRAY = "[]";

  public void testParser_emptyArray() throws Exception {
    JsonFactory factory = newFactory();
    String[] result = factory.createJsonParser(EMPTY_ARRAY).parse(String[].class);
    assertEquals(EMPTY_ARRAY, factory.toString(result));
    // check types and values
    assertEquals(0, result.length);
  }

  public void testParser_partialEmptyArray() throws Exception {
    JsonFactory factory = newFactory();
    JsonParser parser;
    parser = factory.createJsonParser(EMPTY_ARRAY);
    parser.nextToken();
    parser.nextToken();
    // token is now end_array
    String[] result = parser.parse(String[].class);
    assertEquals(EMPTY_ARRAY, factory.toString(result));
    // check types and values
    assertEquals(0, result.length);
  }

  private static final String NUMBER_TOP_VALUE = "1";

  public void testParser_num() throws Exception {
    JsonFactory factory = newFactory();
    int result = factory.createJsonParser(NUMBER_TOP_VALUE).parse(int.class);
    assertEquals(NUMBER_TOP_VALUE, factory.toString(result));
    // check types and values
    assertEquals(1, result);
  }

  private static final String STRING_TOP_VALUE = "\"a\"";

  public void testParser_string() throws Exception {
    JsonFactory factory = newFactory();
    String result = factory.createJsonParser(STRING_TOP_VALUE).parse(String.class);
    assertEquals(STRING_TOP_VALUE, factory.toString(result));
    // check types and values
    assertEquals("a", result);
  }

  private static final String NULL_TOP_VALUE = "null";

  public void testParser_null() throws Exception {
    JsonFactory factory = newFactory();
    String result = factory.createJsonParser(NULL_TOP_VALUE).parse(String.class);
    assertEquals(NULL_TOP_VALUE, factory.toString(result));
    // check types and values
    assertTrue(Data.isNull(result));
  }

  private static final String BOOL_TOP_VALUE = "true";

  public void testParser_bool() throws Exception {
    JsonFactory factory = newFactory();
    boolean result = factory.createJsonParser(BOOL_TOP_VALUE).parse(boolean.class);
    assertEquals(BOOL_TOP_VALUE, factory.toString(result));
    // check types and values
    assertTrue(result);
  }

  public final void testGenerateEntry() throws Exception {
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    JsonGenerator generator = newFactory().createJsonGenerator(out, Charsets.UTF_8);
    Entry entry = new Entry();
    entry.title = "foo";
    generator.serialize(entry);
    generator.flush();
    assertEquals(JSON_ENTRY, StringUtils.newStringUtf8(out.toByteArray()));
  }

  public final void testGenerateFeed() throws Exception {
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    JsonGenerator generator = newFactory().createJsonGenerator(out, Charsets.UTF_8);
    Feed feed = new Feed();
    Entry entryFoo = new Entry();
    entryFoo.title = "foo";
    Entry entryBar = new Entry();
    entryBar.title = "bar";
    feed.entries = new ArrayList<Entry>();
    feed.entries.add(entryFoo);
    feed.entries.add(entryBar);
    generator.serialize(feed);
    generator.flush();
    assertEquals(JSON_FEED, StringUtils.newStringUtf8(out.toByteArray()));
  }

  public final void testToString_entry() throws Exception {
    Entry entry = new Entry();
    entry.title = "foo";
    assertEquals(JSON_ENTRY, newFactory().toString(entry));
  }

  public final void testToString_Feed() throws Exception {
    Feed feed = new Feed();
    Entry entryFoo = new Entry();
    entryFoo.title = "foo";
    Entry entryBar = new Entry();
    entryBar.title = "bar";
    feed.entries = new ArrayList<Entry>();
    feed.entries.add(entryFoo);
    feed.entries.add(entryBar);
    assertEquals(JSON_FEED, newFactory().toString(feed));
  }

  public final void testToByteArray_entry() throws Exception {
    Entry entry = new Entry();
    entry.title = "foo";
    assertTrue(
        Arrays.equals(StringUtils.getBytesUtf8(JSON_ENTRY), newFactory().toByteArray(entry)));
  }

  public final void testToPrettyString_entryApproximate() throws Exception {
    Entry entry = new Entry();
    entry.title = "foo";
    JsonFactory factory = newFactory();
    String prettyString = factory.toPrettyString(entry);
    assertEquals(JSON_ENTRY, factory.toString(factory.fromString(prettyString, Entry.class)));
  }

  public final void testToPrettyString_FeedApproximate() throws Exception {
    Feed feed = new Feed();
    Entry entryFoo = new Entry();
    entryFoo.title = "foo";
    Entry entryBar = new Entry();
    entryBar.title = "bar";
    feed.entries = new ArrayList<Entry>();
    feed.entries.add(entryFoo);
    feed.entries.add(entryBar);
    JsonFactory factory = newFactory();
    String prettyString = factory.toPrettyString(feed);
    assertEquals(JSON_FEED, factory.toString(factory.fromString(prettyString, Feed.class)));
  }

  public void testParser_nullInputStream() throws Exception {
    try {
      newFactory().createJsonParser((InputStream) null, Charsets.UTF_8);
      fail("expected " + NullPointerException.class);
    } catch (NullPointerException e) {
      // expected
    }
  }

  public void testParser_nullString() throws Exception {
    try {
      newFactory().createJsonParser((String) null);
      fail("expected " + NullPointerException.class);
    } catch (NullPointerException e) {
      // expected
    }
  }

  public void testParser_nullReader() throws Exception {
    try {
      newFactory().createJsonParser((Reader) null);
      fail("expected " + NullPointerException.class);
    } catch (NullPointerException e) {
      // expected
    }
  }

  public void testObjectParserParse_entry() throws Exception {
    @SuppressWarnings("serial")
    Entry entry =
        (Entry)
            newFactory()
                .createJsonObjectParser()
                .parseAndClose(new StringReader(JSON_ENTRY), new TypeToken<Entry>() {}.getType());
    assertEquals("foo", entry.title);
  }

  public void testObjectParserParse_stringList() throws Exception {
    JsonFactory factory = newFactory();
    @SuppressWarnings({"unchecked", "serial"})
    List<String> result =
        (List<String>)
            factory
                .createJsonObjectParser()
                .parseAndClose(
                    new StringReader(STRING_ARRAY), new TypeToken<List<String>>() {}.getType());
    result.get(0);
    assertEquals(STRING_ARRAY, factory.toString(result));
    // check types and values
    assertTrue(ImmutableList.of("a", "b", "c").equals(result));
  }

  public void testToString_withFactory() {
    GenericJson data = new GenericJson();
    data.put("a", "b");
    data.setFactory(newFactory());
    assertEquals("{\"a\":\"b\"}", data.toString());
  }

  public void testFactory() {
    JsonFactory factory = newFactory();
    GenericJson data = new GenericJson();
    data.setFactory(factory);
    assertEquals(factory, data.getFactory());
  }

  /** Returns a JsonParser which parses the specified string. */
  private JsonParser createParser(String json) throws Exception {
    return newFactory().createJsonParser(json);
  }

  public void testSkipToKey_firstKey() throws Exception {
    JsonParser parser = createParser(JSON_THREE_ELEMENTS);
    assertEquals("one", parser.skipToKey(ImmutableSet.of("one")));
    parser.skipToKey("num");
    assertEquals(1, parser.getIntValue());
  }

  public void testSkipToKey_lastKey() throws Exception {
    JsonParser parser = createParser(JSON_THREE_ELEMENTS);
    assertEquals("three", parser.skipToKey(ImmutableSet.of("three")));
    parser.skipToKey("num");
    assertEquals(3, parser.getIntValue());
  }

  public void testSkipToKey_multipleKeys() throws Exception {
    JsonParser parser = createParser(JSON_THREE_ELEMENTS);
    assertEquals("two", parser.skipToKey(ImmutableSet.of("foo", "three", "two")));
    parser.skipToKey("num");
    assertEquals(2, parser.getIntValue());
  }

  public void testSkipToKey_noMatch() throws Exception {
    JsonParser parser = createParser(JSON_THREE_ELEMENTS);
    assertEquals(null, parser.skipToKey(ImmutableSet.of("foo", "bar", "num")));
    assertEquals(JsonToken.END_OBJECT, parser.getCurrentToken());
  }

  public final void testGson() throws Exception {
    byte[] asciiJson = Charsets.UTF_8.encode("{ \"foo\": 123 }").array();
    JsonParser jp =
        newFactory().createJsonParser(new ByteArrayInputStream(asciiJson), Charsets.UTF_8);
    assertEquals(com.google.api.client.json.JsonToken.START_OBJECT, jp.nextToken());
    assertEquals(com.google.api.client.json.JsonToken.FIELD_NAME, jp.nextToken());
    assertEquals(com.google.api.client.json.JsonToken.VALUE_NUMBER_INT, jp.nextToken());
    assertEquals(123, jp.getIntValue());
    assertEquals(com.google.api.client.json.JsonToken.END_OBJECT, jp.nextToken());
  }

  public final void testParse_array() throws Exception {
    byte[] jsonData = Charsets.UTF_8.encode("[ 123, 456 ]").array();
    JsonParser jp =
        newFactory().createJsonParser(new ByteArrayInputStream(jsonData), Charsets.UTF_8);
    Type myType = Integer[].class;
    Integer[] array = (Integer[]) jp.parse(myType, true);
    assertNotNull(array);
    assertEquals((Integer) 123, array[0]);
    assertEquals((Integer) 456, array[1]);
  }

  public static class TestClass {
    public TestClass() {}

    @Key("foo")
    public int foo;
  }

  public final void testParse_class() throws Exception {
    byte[] jsonData = Charsets.UTF_8.encode("{ \"foo\": 123 }").array();
    JsonParser jp =
        newFactory().createJsonParser(new ByteArrayInputStream(jsonData), Charsets.UTF_8);
    Type myType = TestClass.class;
    TestClass instance = (TestClass) jp.parse(myType, true);
    assertNotNull(instance);
    assertEquals(123, instance.foo);
  }

  public final void testCreateJsonParser_nullCharset() throws Exception {
    byte[] jsonData = Charsets.UTF_8.encode("{ \"foo\": 123 }").array();
    JsonParser jp = newFactory().createJsonParser(new ByteArrayInputStream(jsonData), null);
    Type myType = TestClass.class;
    TestClass instance = (TestClass) jp.parse(myType, true);
    assertNotNull(instance);
    assertEquals(123, instance.foo);
  }

  public final void testGenerate_infinityOrNanError() throws Exception {
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    JsonGenerator generator = newFactory().createJsonGenerator(out, Charsets.UTF_8);
    NumberTypes num = new NumberTypes();
    for (float f : new float[] {Float.NaN, Float.POSITIVE_INFINITY, Float.NEGATIVE_INFINITY}) {
      num.floatValue = f;
      try {
        generator.serialize(num);
        fail("expected " + IllegalArgumentException.class);
      } catch (IllegalArgumentException e) {
        // ignore
      }
    }
    num.floatValue = 0;
    for (double d : new double[] {Double.NaN, Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY}) {
      num.doubleValue = d;
      try {
        generator.serialize(num);
        fail("expected " + IllegalArgumentException.class);
      } catch (IllegalArgumentException e) {
        // ignore
      }
    }
  }

  public static class ExtendsGenericJson extends GenericJson {
    @Key @JsonString Long numAsString;

    @Override
    public ExtendsGenericJson set(String fieldName, Object value) {
      return (ExtendsGenericJson) super.set(fieldName, value);
    }
  }

  static final String EXTENDS_JSON = "{\"numAsString\":\"1\",\"num\":1}";

  public void testParser_extendsGenericJson() throws Exception {
    JsonFactory factory = newFactory();
    JsonParser parser;
    // number types
    parser = factory.createJsonParser(EXTENDS_JSON);
    parser.nextToken();
    ExtendsGenericJson result = parser.parse(ExtendsGenericJson.class);
    assertEquals(EXTENDS_JSON, factory.toString(result));
  }

  public static class Simple {
    @Key String a;
  }

  static final String SIMPLE = "{\"a\":\"b\"}";
  static final String SIMPLE_WRAPPED = "{\"d\":{\"a\":\"b\"}}";

  public void testJsonObjectParser_reader() throws Exception {
    JsonFactory factory = newFactory();
    JsonObjectParser parser = new JsonObjectParser(factory);
    Simple simple = parser.parseAndClose(new StringReader(SIMPLE), Simple.class);
    assertEquals("b", simple.a);
  }

  public void testJsonObjectParser_inputStream() throws Exception {
    JsonFactory factory = newFactory();
    JsonObjectParser parser = new JsonObjectParser(factory);
    Simple simple =
        parser.parseAndClose(
            new ByteArrayInputStream(StringUtils.getBytesUtf8(SIMPLE)),
            Charsets.UTF_8,
            Simple.class);
    assertEquals("b", simple.a);
  }

  public void testJsonObjectParser_readerWrapped() throws Exception {
    JsonFactory factory = newFactory();
    JsonObjectParser parser =
        new JsonObjectParser.Builder(factory).setWrapperKeys(Collections.singleton("d")).build();
    Simple simple = parser.parseAndClose(new StringReader(SIMPLE_WRAPPED), Simple.class);
    assertEquals("b", simple.a);
  }

  public void testJsonObjectParser_inputStreamWrapped() throws Exception {
    JsonFactory factory = newFactory();
    JsonObjectParser parser =
        new JsonObjectParser.Builder(factory).setWrapperKeys(Collections.singleton("d")).build();
    Simple simple =
        parser.parseAndClose(
            new ByteArrayInputStream(StringUtils.getBytesUtf8(SIMPLE_WRAPPED)),
            Charsets.UTF_8,
            Simple.class);
    assertEquals("b", simple.a);
  }

  public void testJsonHttpContent_simple() throws Exception {
    JsonFactory factory = newFactory();
    Simple simple = new Simple();
    simple.a = "b";
    JsonHttpContent content = new JsonHttpContent(factory, simple);
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    content.writeTo(out);
    assertEquals(SIMPLE, out.toString("UTF-8"));
  }

  public void testJsonHttpContent_wrapped() throws Exception {
    JsonFactory factory = newFactory();
    Simple simple = new Simple();
    simple.a = "b";
    JsonHttpContent content = new JsonHttpContent(factory, simple).setWrapperKey("d");
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    content.writeTo(out);
    assertEquals(SIMPLE_WRAPPED, out.toString("UTF-8"));
  }

  public static class V {
    @Key Void v;
    @Key String s;
  }

  public void testParse_void() throws Exception {
    subtestParse_void(null);
    subtestParse_void("\"a\"");
    subtestParse_void("null");
    subtestParse_void("true");
    subtestParse_void("false");
    subtestParse_void("123");
    subtestParse_void("123.456");
    subtestParse_void("[1]");
    subtestParse_void("{\"v\":\"ignored\"}");
  }

  public void subtestParse_void(String value) throws Exception {
    JsonFactory factory = newFactory();
    String inputString = "{" + (value == null ? "" : "\"v\":" + value + ",") + "\"s\":\"svalue\"}";
    V v = factory.fromString(inputString, V.class);
    assertNull(v.v);
    assertEquals("svalue", v.s);
    assertNull(factory.fromString(inputString, Void.class));
  }

  public static class BooleanTypes {
    @Key Boolean boolObj;
    @Key boolean bool;
  }

  public static final String BOOLEAN_TYPE_EMPTY = "{}";
  public static final String BOOLEAN_TYPE_EMPTY_OUTPUT = "{\"bool\":false}";
  public static final String BOOLEAN_TYPE_TRUE = "{\"bool\":true,\"boolObj\":true}";
  public static final String BOOLEAN_TYPE_FALSE = "{\"bool\":false,\"boolObj\":false}";
  public static final String BOOLEAN_TYPE_NULL = "{\"boolObj\":null}";
  public static final String BOOLEAN_TYPE_NULL_OUTPUT = "{\"bool\":false,\"boolObj\":null}";
  public static final String BOOLEAN_TYPE_WRONG = "{\"boolObj\":{}}";

  public void testParse_boolean() throws Exception {
    JsonFactory factory = newFactory();
    BooleanTypes parsed;
    // empty
    parsed = factory.fromString(BOOLEAN_TYPE_EMPTY, BooleanTypes.class);
    assertFalse(parsed.bool);
    assertNull(parsed.boolObj);
    assertEquals(BOOLEAN_TYPE_EMPTY_OUTPUT, factory.toString(parsed));
    // true
    parsed = factory.fromString(BOOLEAN_TYPE_TRUE, BooleanTypes.class);
    assertTrue(parsed.bool);
    assertTrue(parsed.boolObj.booleanValue() && !Data.isNull(parsed.boolObj));
    assertEquals(BOOLEAN_TYPE_TRUE, factory.toString(parsed));
    // false
    parsed = factory.fromString(BOOLEAN_TYPE_FALSE, BooleanTypes.class);
    assertFalse(parsed.bool);
    assertTrue(!parsed.boolObj.booleanValue() && !Data.isNull(parsed.boolObj));
    assertEquals(BOOLEAN_TYPE_FALSE, factory.toString(parsed));
    // null
    parsed = factory.fromString(BOOLEAN_TYPE_NULL, BooleanTypes.class);
    assertFalse(parsed.bool);
    assertTrue(Data.isNull(parsed.boolObj));
    assertEquals(BOOLEAN_TYPE_NULL_OUTPUT, factory.toString(parsed));
    // wrong
    try {
      factory.fromString(BOOLEAN_TYPE_WRONG, BooleanTypes.class);
      fail("expected " + IllegalArgumentException.class);
    } catch (IllegalArgumentException e) {
    }
  }

  public abstract static class Animal {
    @Key public String name;

    @Key("legCount")
    public int numberOfLegs;

    @Key
    @JsonPolymorphicTypeMap(
        typeDefinitions = {
          @TypeDef(key = "dog", ref = Dog.class),
          @TypeDef(key = "bug", ref = Centipede.class),
          @TypeDef(key = "human", ref = Human.class),
          @TypeDef(key = "dogwithfamily", ref = DogWithFamily.class),
          @TypeDef(key = "human with pets", ref = HumanWithPets.class)
        })
    public String type;
  }

  public static class Dog extends Animal {
    @Key public int tricksKnown;
  }

  public static class Centipede extends Animal {
    @Key("bodyColor")
    public String color;
  }

  // Test regular heterogeneous schema cases:
  public static final String DOG =
      "{\"legCount\":4,\"name\":\"Fido\",\"tricksKnown\":3,\"type\":\"dog\"}";
  public static final String CENTIPEDE =
      "{\"bodyColor\":\"green\",\"legCount\":68,\"name\":\"Mr. Icky\",\"type\":\"bug\"}";

  // Test heterogeneous scheme optimized case where the type is the first value:
  public static final String DOG_OPTIMIZED =
      "{\"type\":\"dog\",\"name\":\"Fido\",\"legCount\":4,\"tricksKnown\":3}";
  public static final String CENTIPEDE_OPTIMIZED =
      "{\"type\":\"bug\",\"bodyColor\":\"green\",\"name\":\"Mr. Icky\",\"legCount\":68}";

  // Test heterogeneous scheme with additional, unused information:
  public static final String DOG_EXTRA_INFO =
      "{\"name\":\"Fido\",\"legCount\":4,\"unusedInfo\":\"this is not being used!\","
          + "\"tricksKnown\":3,\"type\":\"dog\",\"unused\":{\"foo\":200}}";
  public static final String CENTIPEDE_EXTRA_INFO =
      "{\"unused\":0, \"bodyColor\":\"green\",\"name\":\"Mr. Icky\",\"legCount\":68,\"type\":"
          + "\"bug\"}";

  public void testParser_heterogeneousSchemata() throws Exception {
    testParser_heterogeneousSchemata_Helper(DOG, CENTIPEDE);
    // TODO(ngmiceli): Test that this uses the optimized flow (once implemented)
    testParser_heterogeneousSchemata_Helper(DOG_OPTIMIZED, CENTIPEDE_OPTIMIZED);
    testParser_heterogeneousSchemata_Helper(DOG_EXTRA_INFO, CENTIPEDE_EXTRA_INFO);
  }

  private void testParser_heterogeneousSchemata_Helper(String dogJson, String centipedeJson)
      throws Exception {
    // Test for Dog
    JsonFactory factory = newFactory();
    JsonParser parser;
    parser = factory.createJsonParser(dogJson);
    Animal dog = parser.parse(Animal.class);
    // Always outputs keys in alphabetical order
    assertEquals(DOG, factory.toString(dog));
    assertEquals(Dog.class, dog.getClass());
    assertEquals("Fido", dog.name);
    assertEquals("dog", dog.type);
    assertEquals(4, dog.numberOfLegs);
    assertEquals(3, ((Dog) dog).tricksKnown);

    // Test for Centipede
    parser = factory.createJsonParser(centipedeJson);
    parser.nextToken();
    Animal centipede = parser.parse(Animal.class);
    // Always outputs keys in alphabetical order
    assertEquals(CENTIPEDE, factory.toString(centipede));
    assertEquals(Centipede.class, centipede.getClass());
    assertEquals("Mr. Icky", centipede.name);
    assertEquals("bug", centipede.type);
    assertEquals(68, centipede.numberOfLegs);
    assertEquals("green", ((Centipede) centipede).color);
  }

  public static final String ANIMAL_WITHOUT_TYPE = "{\"legCount\":3,\"name\":\"Confused\"}";

  public void testParser_heterogeneousSchema_missingType() throws Exception {
    JsonFactory factory = newFactory();
    JsonParser parser;
    parser = factory.createJsonParser(ANIMAL_WITHOUT_TYPE);
    try {
      parser.parse(Animal.class);
    } catch (IllegalArgumentException e) {
      return; // expected
    }
    fail("IllegalArgumentException expected on heterogeneous schema without type field specified");
  }

  public static class Human extends Animal {
    @Key public Dog bestFriend;
  }

  // Test a subclass with an additional object in it.
  public static final String HUMAN =
      "{\"bestFriend\":" + DOG + ",\"legCount\":2,\"name\":\"Joe\",\"type\":\"human\"}";

  public void testParser_heterogeneousSchema_withObject() throws Exception {
    JsonFactory factory = newFactory();
    JsonParser parser = factory.createJsonParser(HUMAN);
    Animal human = parser.parse(Animal.class);
    assertEquals(HUMAN, factory.toString(human));
    Dog dog = ((Human) human).bestFriend;
    assertEquals(DOG, factory.toString(dog));
    assertEquals(Dog.class, dog.getClass());
    assertEquals("Fido", dog.name);
    assertEquals("dog", dog.type);
    assertEquals(4, dog.numberOfLegs);
    assertEquals(3, dog.tricksKnown);
    assertEquals("Joe", human.name);
    assertEquals(2, human.numberOfLegs);
    assertEquals("human", human.type);
  }

  public static class AnimalGenericJson extends GenericJson {
    @Key public String name;

    @Key("legCount")
    public int numberOfLegs;

    @Key
    @JsonPolymorphicTypeMap(typeDefinitions = {@TypeDef(key = "dog", ref = DogGenericJson.class)})
    public String type;
  }

  public static class DogGenericJson extends AnimalGenericJson {
    @Key public int tricksKnown;
  }

  public static final String DOG_EXTRA_INFO_ORDERED =
      "{\"legCount\":4,\"name\":\"Fido\",\"tricksKnown\":3,\"type\":\"dog\","
          + "\"unusedInfo\":\"this is not being used!\",\"unused\":{\"foo\":200}}";

  @SuppressWarnings("unchecked")
  public void testParser_heterogeneousSchema_genericJson() throws Exception {
    JsonFactory factory = newFactory();
    JsonParser parser = factory.createJsonParser(DOG_EXTRA_INFO);
    AnimalGenericJson dog = parser.parse(AnimalGenericJson.class);
    assertEquals(DOG_EXTRA_INFO_ORDERED, factory.toString(dog));
    assertEquals(DogGenericJson.class, dog.getClass());
    assertEquals("Fido", dog.name);
    assertEquals("dog", dog.type);
    assertEquals(4, dog.numberOfLegs);
    assertEquals(3, ((DogGenericJson) dog).tricksKnown);
    assertEquals("this is not being used!", dog.get("unusedInfo"));
    BigDecimal foo = ((BigDecimal) ((ArrayMap<String, Object>) dog.get("unused")).get("foo"));
    assertEquals(200, foo.intValue());
  }

  public static final String DOG_WITH_FAMILY =
      "{\"children\":["
          + DOG
          + ","
          + CENTIPEDE
          + "],\"legCount\":4,\"name\":\"Bob\",\"nicknames\":[\"Fluffy\",\"Hey, you\"],"
          + "\"tricksKnown\":10,\"type\":\"dogwithfamily\"}";

  public static class DogWithFamily extends Dog {
    @Key public String[] nicknames;
    @Key public Animal[] children;
  }

  public void testParser_heterogeneousSchema_withArrays() throws Exception {
    JsonFactory factory = newFactory();
    JsonParser parser = factory.createJsonParser(DOG_WITH_FAMILY);
    Animal dog = parser.parse(DogWithFamily.class);
    assertEquals(DOG_WITH_FAMILY, factory.toString(dog));
    assertEquals(DogWithFamily.class, dog.getClass());
    assertEquals("Bob", dog.name);
    assertEquals("dogwithfamily", dog.type);
    assertEquals(4, dog.numberOfLegs);
    assertEquals(10, ((DogWithFamily) dog).tricksKnown);
    String[] nicknames = {"Fluffy", "Hey, you"};
    assertTrue(Arrays.equals(nicknames, ((DogWithFamily) dog).nicknames));
    Animal child = ((DogWithFamily) dog).children[0];
    assertEquals("Fido", child.name);
    assertEquals(3, ((Dog) child).tricksKnown);
    Animal child2 = ((DogWithFamily) dog).children[1];
    assertEquals("Mr. Icky", child2.name);
    assertEquals(68, ((Centipede) child2).numberOfLegs);
  }

  public static final String DOG_WITH_NO_FAMILY = "{\"legCount\":4,\"type\":\"dogwithfamily\"}";
  public static final String DOG_WITH_NO_FAMILY_PARSED =
      "{\"legCount\":4,\"tricksKnown\":0,\"type\":\"dogwithfamily\"}";

  public void testParser_heterogeneousSchema_withNullArrays() throws Exception {
    JsonFactory factory = newFactory();
    JsonParser parser = factory.createJsonParser(DOG_WITH_NO_FAMILY);
    Animal dog = parser.parse(DogWithFamily.class);
    assertEquals(DogWithFamily.class, dog.getClass());
    assertEquals(DOG_WITH_NO_FAMILY_PARSED, factory.toString(dog));
    assertEquals(4, dog.numberOfLegs);
    assertEquals(0, ((Dog) dog).tricksKnown);
    assertEquals(null, dog.name);
    assertEquals(null, ((DogWithFamily) dog).nicknames);
    assertEquals(null, ((DogWithFamily) dog).children);
  }

  public static class PolymorphicWithMultipleAnnotations {
    @Key String a;

    @Key
    @JsonPolymorphicTypeMap(typeDefinitions = {@TypeDef(key = "dog", ref = Dog.class)})
    String b;

    @Key String c;

    @Key
    @JsonPolymorphicTypeMap(typeDefinitions = {@TypeDef(key = "bug", ref = Centipede.class)})
    String d;
  }

  public static final String MULTIPLE_ANNOTATIONS_JSON =
      "{\"a\":\"foo\",\"b\":\"dog\",\"c\":\"bar\",\"d\":\"bug\"}";

  public void testParser_polymorphicClass_tooManyAnnotations() throws Exception {
    JsonFactory factory = newFactory();
    JsonParser parser = factory.createJsonParser(MULTIPLE_ANNOTATIONS_JSON);
    try {
      parser.parse(PolymorphicWithMultipleAnnotations.class);
    } catch (IllegalArgumentException e) {
      return; // expected
    }
    fail(
        "Expected IllegalArgumentException on class with multiple @JsonPolymorphicTypeMap"
            + " annotations.");
  }

  public static class PolymorphicWithNumericType {
    @Key
    @JsonPolymorphicTypeMap(
        typeDefinitions = {
          @TypeDef(key = "1", ref = NumericTypedSubclass1.class),
          @TypeDef(key = "2", ref = NumericTypedSubclass2.class)
        })
    Integer type;
  }

  public static class NumericTypedSubclass1 extends PolymorphicWithNumericType {}

  public static class NumericTypedSubclass2 extends PolymorphicWithNumericType {}

  public static final String POLYMORPHIC_NUMERIC_TYPE_1 = "{\"foo\":\"bar\",\"type\":1}";
  public static final String POLYMORPHIC_NUMERIC_TYPE_2 = "{\"foo\":\"bar\",\"type\":2}";

  public void testParser_heterogeneousSchema_numericType() throws Exception {
    JsonFactory factory = newFactory();
    JsonParser parser = factory.createJsonParser(POLYMORPHIC_NUMERIC_TYPE_1);
    PolymorphicWithNumericType t1 = parser.parse(PolymorphicWithNumericType.class);
    assertEquals(NumericTypedSubclass1.class, t1.getClass());

    factory = newFactory();
    parser = factory.createJsonParser(POLYMORPHIC_NUMERIC_TYPE_2);
    PolymorphicWithNumericType t2 = parser.parse(PolymorphicWithNumericType.class);
    assertEquals(NumericTypedSubclass2.class, t2.getClass());
  }

  public static class PolymorphicWithNumericValueType {
    @Key
    @JsonPolymorphicTypeMap(
        typeDefinitions = {
          @TypeDef(key = "1", ref = NumericValueTypedSubclass1.class),
          @TypeDef(key = "2", ref = NumericValueTypedSubclass2.class)
        })
    int type;
  }

  public static class NumericValueTypedSubclass1 extends PolymorphicWithNumericValueType {}

  public static class NumericValueTypedSubclass2 extends PolymorphicWithNumericValueType {}

  public static final String POLYMORPHIC_NUMERIC_UNSPECIFIED_TYPE = "{\"foo\":\"bar\"}";

  public void testParser_heterogeneousSchema_numericValueType() throws Exception {
    JsonFactory factory = newFactory();
    JsonParser parser = factory.createJsonParser(POLYMORPHIC_NUMERIC_TYPE_1);
    PolymorphicWithNumericValueType t1 = parser.parse(PolymorphicWithNumericValueType.class);
    assertEquals(NumericValueTypedSubclass1.class, t1.getClass());

    factory = newFactory();
    parser = factory.createJsonParser(POLYMORPHIC_NUMERIC_TYPE_2);
    PolymorphicWithNumericValueType t2 = parser.parse(PolymorphicWithNumericValueType.class);
    assertEquals(NumericValueTypedSubclass2.class, t2.getClass());

    factory = newFactory();
    parser = factory.createJsonParser(POLYMORPHIC_NUMERIC_UNSPECIFIED_TYPE);
    try {
      parser.parse(PolymorphicWithNumericValueType.class);
    } catch (IllegalArgumentException e) {
      return; // expected
    }
    fail("IllegalArgumentException expected on heterogeneous schema without type field specified");
  }

  public static class PolymorphicWithIllegalValueType {
    @Key
    @JsonPolymorphicTypeMap(
        typeDefinitions = {
          @TypeDef(key = "foo", ref = Object.class),
          @TypeDef(key = "bar", ref = Object.class)
        })
    Object type;
  }

  public void testParser_heterogeneousSchema_illegalValueType() throws Exception {
    JsonFactory factory = newFactory();
    JsonParser parser = factory.createJsonParser(POLYMORPHIC_NUMERIC_TYPE_1);
    try {
      parser.parse(PolymorphicWithIllegalValueType.class);
    } catch (IllegalArgumentException e) {
      return; // expected
    }
    fail("Expected IllegalArgumentException on class with illegal @JsonPolymorphicTypeMap type");
  }

  public static class PolymorphicWithDuplicateTypeKeys {
    @Key
    @JsonPolymorphicTypeMap(
        typeDefinitions = {
          @TypeDef(key = "foo", ref = Object.class),
          @TypeDef(key = "foo", ref = Object.class)
        })
    String type;
  }

  public void testParser_polymorphicClass_duplicateTypeKeys() throws Exception {
    JsonFactory factory = newFactory();
    JsonParser parser = factory.createJsonParser(EMPTY_OBJECT);
    try {
      parser.parse(PolymorphicWithDuplicateTypeKeys.class);
    } catch (IllegalArgumentException e) {
      return; // expected
    }
    fail("Expected IllegalArgumentException on class with duplicate typeDef keys");
  }

  public static final String POLYMORPHIC_WITH_UNKNOWN_KEY =
      "{\"legCount\":4,\"name\":\"Fido\",\"tricksKnown\":3,\"type\":\"unknown\"}";

  public void testParser_polymorphicClass_noMatchingTypeKey() throws Exception {
    JsonFactory factory = newFactory();
    JsonParser parser = factory.createJsonParser(POLYMORPHIC_WITH_UNKNOWN_KEY);
    try {
      parser.parse(Animal.class);
    } catch (IllegalArgumentException e) {
      return; // expected
    }
    fail("Expected IllegalArgumentException when provided with unknown typeDef key");
  }

  public static class PolymorphicSelfReferencing {
    @Key
    @JsonPolymorphicTypeMap(
        typeDefinitions = {@TypeDef(key = "self", ref = PolymorphicSelfReferencing.class)})
    String type;

    @Key String info;
  }

  public static final String POLYMORPHIC_SELF_REFERENCING = "{\"info\":\"blah\",\"type\":\"self\"}";

  public void testParser_polymorphicClass_selfReferencing() throws Exception {
    JsonFactory factory = newFactory();
    JsonParser parser = factory.createJsonParser(POLYMORPHIC_SELF_REFERENCING);
    PolymorphicSelfReferencing p = parser.parse(PolymorphicSelfReferencing.class);
    assertEquals(PolymorphicSelfReferencing.class, p.getClass());
    assertEquals(POLYMORPHIC_SELF_REFERENCING, factory.toString(p));
    assertEquals("self", p.type);
    assertEquals("blah", p.info);
  }

  public static class HumanWithPets extends Human {
    @Key Map<String, Animal> pets;
  }

  public static final String HUMAN_WITH_PETS =
      "{\"bestFriend\":"
          + DOG
          + ",\"legCount\":2,\"name\":\"Joe\",\"pets\":{\"first\":"
          + CENTIPEDE
          + ",\"second\":{\"type\":\"dog\"}},\"type\":\"human with pets\",\"unused\":\"foo\"}";

  public static final String HUMAN_WITH_PETS_PARSED =
      "{\"bestFriend\":"
          + DOG
          + ",\"legCount\":2,\"name\":\"Joe\",\"pets\":{\"first\":"
          + CENTIPEDE
          + ",\"second\":{\"legCount\":0,\"tricksKnown\":0,\"type\":\"dog\"}},"
          + "\"type\":\"human with pets\"}";

  public void testParser_polymorphicClass_mapOfPolymorphicClasses() throws Exception {
    JsonFactory factory = newFactory();
    JsonParser parser = factory.createJsonParser(HUMAN_WITH_PETS);
    Animal human = parser.parse(Animal.class);
    assertEquals(HumanWithPets.class, human.getClass());
    assertEquals(HUMAN_WITH_PETS_PARSED, factory.toString(human));
    assertEquals(2, human.numberOfLegs);
    assertEquals("human with pets", human.type);
    HumanWithPets humanWithPets = (HumanWithPets) human;
    assertEquals("Fido", humanWithPets.bestFriend.name);
    assertEquals(3, humanWithPets.bestFriend.tricksKnown);
    assertEquals("Mr. Icky", humanWithPets.pets.get("first").name);
    assertEquals("bug", humanWithPets.pets.get("first").type);
    assertEquals(68, humanWithPets.pets.get("first").numberOfLegs);
    assertEquals("green", ((Centipede) humanWithPets.pets.get("first")).color);
    assertEquals("dog", humanWithPets.pets.get("second").type);
    assertEquals(0, ((Dog) humanWithPets.pets.get("second")).tricksKnown);
    assertEquals(2, humanWithPets.pets.size());
  }
}
