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

package com.google.api.client.util;

import com.google.common.collect.ImmutableMap;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.AbstractList;
import java.util.AbstractMap;
import java.util.AbstractQueue;
import java.util.AbstractSet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentMap;
import junit.framework.TestCase;

/**
 * Tests {@link Data}.
 *
 * @author Yaniv Inbar
 */
public class DataTest extends TestCase {

  public void testNullOf() {
    assertEquals("java.lang.Object", Data.nullOf(Object.class).getClass().getName());
    assertEquals("java.lang.String", Data.nullOf(String.class).getClass().getName());
    assertEquals("java.lang.Integer", Data.nullOf(Integer.class).getClass().getName());
    assertEquals("[[[[Ljava.lang.String;", Data.nullOf(String[][][][].class).getClass().getName());
    assertEquals("[[[I", Data.nullOf(int[][][].class).getClass().getName());
    assertNotNull(Data.nullOf(Object.class));
    assertEquals(Data.<Object>nullOf(Object.class), Data.<Object>nullOf(Object.class));
    assertFalse(Data.nullOf(String.class).equals(Data.nullOf(Object.class)));
    try {
      Data.nullOf(int.class);
      fail("expected " + IllegalArgumentException.class);
    } catch (IllegalArgumentException e) {
    }
    try {
      Data.nullOf(List.class);
      fail("expected " + IllegalArgumentException.class);
    } catch (IllegalArgumentException e) {
    }
  }

  public void testNullOfTemplateTypes() {
    String nullValue = Data.nullOf(String.class);
    Map<String, String> nullField = ImmutableMap.of("v", nullValue);
    assertEquals(nullValue, nullField.get("v"));
  }

  public void testIsNull() {
    assertTrue(Data.isNull(Data.NULL_BOOLEAN));
    assertTrue(Data.isNull(Data.NULL_STRING));
    assertTrue(Data.isNull(Data.NULL_CHARACTER));
    assertTrue(Data.isNull(Data.NULL_BYTE));
    assertTrue(Data.isNull(Data.NULL_SHORT));
    assertTrue(Data.isNull(Data.NULL_INTEGER));
    assertTrue(Data.isNull(Data.NULL_LONG));
    assertTrue(Data.isNull(Data.NULL_FLOAT));
    assertTrue(Data.isNull(Data.NULL_DOUBLE));
    assertTrue(Data.isNull(Data.NULL_BIG_INTEGER));
    assertTrue(Data.isNull(Data.NULL_BIG_DECIMAL));
    assertTrue(Data.isNull(Data.NULL_DATE_TIME));
    assertFalse(Data.isNull(null));
    assertFalse(Data.isNull(""));
    assertFalse(Data.isNull(false));
    assertFalse(Data.isNull(true));
    assertFalse(Data.isNull((byte) 0));
    assertFalse(Data.isNull((short) 0));
    assertFalse(Data.isNull(0));
    assertFalse(Data.isNull(0L));
    assertFalse(Data.isNull((float) 0));
    assertFalse(Data.isNull((double) 0));
    assertFalse(Data.isNull(BigDecimal.ZERO));
    assertFalse(Data.isNull(BigInteger.ZERO));
  }

  public void testClone_array() {
    String[] orig = new String[] {"a", "b", "c"};
    String[] result = Data.clone(orig);
    assertTrue(orig != result);
    assertTrue(Arrays.equals(orig, result));
  }

  public void testClone_intArray() {
    int[] orig = new int[] {1, 2, 3};
    int[] result = Data.clone(orig);
    assertTrue(orig != result);
    assertTrue(Arrays.equals(orig, result));
  }

  public void testClone_arrayMap() {
    ArrayMap<String, Integer> map = ArrayMap.of();
    map.add("a", 1);
    assertEquals(map, Data.clone(map));
  }

  public void testClone_ArraysAsList() {
    {
      List<Object> orig = Arrays.<Object>asList("a", "b", "c", new ArrayList<Object>());
      List<Object> result = Data.clone(orig);
      assertTrue(orig != result);
      assertEquals(orig, result);
      assertTrue(orig.get(3) != result.get(3));
    }

    {
      List<String> orig = Arrays.asList(new String[] {"a", "b", "c"});
      List<String> result = Data.clone(orig);
      assertTrue(orig != result);
      assertEquals(orig, result);
    }
  }

  public void testNewCollectionInstance() throws Exception {
    assertEquals(ArrayList.class, Data.newCollectionInstance(null).getClass());
    assertEquals(ArrayList.class, Data.newCollectionInstance(String[].class).getClass());
    assertEquals(ArrayList.class, Data.newCollectionInstance(Object.class).getClass());
    assertEquals(ArrayList.class, Data.newCollectionInstance(List.class).getClass());
    assertEquals(ArrayList.class, Data.newCollectionInstance(AbstractList.class).getClass());
    assertEquals(ArrayList.class, Data.newCollectionInstance(ArrayList.class).getClass());
    assertEquals(LinkedList.class, Data.newCollectionInstance(LinkedList.class).getClass());
    assertEquals(HashSet.class, Data.newCollectionInstance(Set.class).getClass());
    assertEquals(HashSet.class, Data.newCollectionInstance(AbstractSet.class).getClass());
    assertEquals(HashSet.class, Data.newCollectionInstance(HashSet.class).getClass());
    assertEquals(TreeSet.class, Data.newCollectionInstance(SortedSet.class).getClass());
    assertEquals(TreeSet.class, Data.newCollectionInstance(TreeSet.class).getClass());
    try {
      Data.newMapInstance(AbstractQueue.class);
      fail("expected " + IllegalArgumentException.class);
    } catch (IllegalArgumentException e) {
      // expected
    }
    try {
      Data.newMapInstance(String.class);
      fail("expected " + ClassCastException.class);
    } catch (ClassCastException e) {
      // expected
    }
    TypeVariable<?> tTypeVar = (TypeVariable<?>) Resolve.class.getField("t").getGenericType();
    try {
      assertNull(Data.newCollectionInstance(tTypeVar));
      fail("expected " + IllegalArgumentException.class);
    } catch (IllegalArgumentException e) {
      // expected
    }
  }

  public void testNewMapInstance() {
    assertEquals(ArrayMap.class, Data.newMapInstance(Object.class).getClass());
    assertEquals(ArrayMap.class, Data.newMapInstance(Map.class).getClass());
    assertEquals(ArrayMap.class, Data.newMapInstance(Cloneable.class).getClass());
    assertEquals(ArrayMap.class, Data.newMapInstance(AbstractMap.class).getClass());
    assertEquals(ArrayMap.class, Data.newMapInstance(ArrayMap.class).getClass());
    assertEquals(TreeMap.class, Data.newMapInstance(SortedMap.class).getClass());
    assertEquals(TreeMap.class, Data.newMapInstance(TreeMap.class).getClass());
    assertEquals(HashMap.class, Data.newMapInstance(HashMap.class).getClass());
    try {
      Data.newMapInstance(ConcurrentMap.class);
      fail("expected " + IllegalArgumentException.class);
    } catch (IllegalArgumentException e) {
      // expected
    }
    try {
      Data.newMapInstance(String.class);
      fail("expected " + ClassCastException.class);
    } catch (ClassCastException e) {
      // expected
    }
  }

  public void testIsPrimitive() {
    assertFalse(Data.isPrimitive(null));
    assertTrue(Data.isPrimitive(int.class));
    assertTrue(Data.isPrimitive(Integer.class));
  }

  public void testParsePrimitiveValue() {
    assertNull(Data.parsePrimitiveValue(Boolean.class, null));
    assertEquals("abc", Data.parsePrimitiveValue(null, "abc"));
    assertEquals("abc", Data.parsePrimitiveValue(String.class, "abc"));
    assertEquals("abc", Data.parsePrimitiveValue(Object.class, "abc"));
    assertEquals('a', Data.parsePrimitiveValue(Character.class, "a"));
    assertEquals(true, Data.parsePrimitiveValue(boolean.class, "true"));
    assertEquals(true, Data.parsePrimitiveValue(Boolean.class, "true"));
    assertEquals(
        new Byte(Byte.MAX_VALUE),
        Data.parsePrimitiveValue(Byte.class, String.valueOf(Byte.MAX_VALUE)));
    assertEquals(
        new Byte(Byte.MAX_VALUE),
        Data.parsePrimitiveValue(byte.class, String.valueOf(Byte.MAX_VALUE)));
    assertEquals(
        new Short(Short.MAX_VALUE),
        Data.parsePrimitiveValue(Short.class, String.valueOf(Short.MAX_VALUE)));
    assertEquals(
        new Short(Short.MAX_VALUE),
        Data.parsePrimitiveValue(short.class, String.valueOf(Short.MAX_VALUE)));
    assertEquals(
        new Integer(Integer.MAX_VALUE),
        Data.parsePrimitiveValue(Integer.class, String.valueOf(Integer.MAX_VALUE)));
    assertEquals(
        new Integer(Integer.MAX_VALUE),
        Data.parsePrimitiveValue(int.class, String.valueOf(Integer.MAX_VALUE)));
    assertEquals(
        new Long(Long.MAX_VALUE),
        Data.parsePrimitiveValue(Long.class, String.valueOf(Long.MAX_VALUE)));
    assertEquals(
        new Long(Long.MAX_VALUE),
        Data.parsePrimitiveValue(long.class, String.valueOf(Long.MAX_VALUE)));
    assertEquals(
        new Float(Float.MAX_VALUE),
        Data.parsePrimitiveValue(Float.class, String.valueOf(Float.MAX_VALUE)));
    assertEquals(
        new Float(Float.MAX_VALUE),
        Data.parsePrimitiveValue(float.class, String.valueOf(Float.MAX_VALUE)));
    assertEquals(
        new Double(Double.MAX_VALUE),
        Data.parsePrimitiveValue(Double.class, String.valueOf(Double.MAX_VALUE)));
    assertEquals(
        new Double(Double.MAX_VALUE),
        Data.parsePrimitiveValue(double.class, String.valueOf(Double.MAX_VALUE)));
    BigInteger bigint = BigInteger.valueOf(Long.MAX_VALUE);
    assertEquals(
        bigint, Data.parsePrimitiveValue(BigInteger.class, String.valueOf(Long.MAX_VALUE)));
    BigDecimal bigdec = BigDecimal.valueOf(Double.MAX_VALUE);
    assertEquals(
        bigdec, Data.parsePrimitiveValue(BigDecimal.class, String.valueOf(Double.MAX_VALUE)));
    DateTime now = new DateTime(new Date());
    assertEquals(now, Data.parsePrimitiveValue(DateTime.class, now.toStringRfc3339()));
    try {
      Data.parsePrimitiveValue(char.class, "abc");
      fail();
    } catch (IllegalArgumentException e) {
      // expected
    }
    assertNull(Data.parsePrimitiveValue(Void.class, "abc"));
    assertNull(Data.parsePrimitiveValue(Enum.class, null));
  }

  private enum MyEnum {
    A("a");
    private final String s;

    MyEnum(String s) {
      this.s = s;
    }
  }

  public void testParsePrimitiveValueWithUnknownEnum() {
    try {
      Data.parsePrimitiveValue(MyEnum.class, "foo");
      fail("expected " + IllegalArgumentException.class);
    } catch (IllegalArgumentException e) {
      // expected
    }
  }

  static class Resolve<X, T extends Number> {
    public T t;
    public X x;
  }

  static class IntegerResolve extends Resolve<Boolean, Integer> {}

  static class MedResolve<T extends Number> extends Resolve<Boolean, T> {}

  static class DoubleResolve extends MedResolve<Double> {}

  static class Med2Resolve<T extends Number> extends MedResolve<T> {}

  static class LongResolve extends Med2Resolve<Long> {}

  static class ArrayResolve extends Resolve<Boolean[], Integer> {}

  static class ParameterizedResolve extends Resolve<Collection<Integer>, Integer> {}

  static class MedXResolve<Y extends Number, X extends Y> extends Resolve<X, Integer> {}

  public void testResolveWildcardTypeOrTypeVariable() throws Exception {
    // t
    TypeVariable<?> tTypeVar = (TypeVariable<?>) Resolve.class.getField("t").getGenericType();
    assertEquals(Number.class, resolveWildcardTypeOrTypeVariable(Object.class, tTypeVar));
    assertEquals(Number.class, resolveWildcardTypeOrTypeVariable(Resolve.class, tTypeVar));
    assertEquals(Integer.class, resolveWildcardTypeOrTypeVariable(IntegerResolve.class, tTypeVar));
    assertEquals(Long.class, resolveWildcardTypeOrTypeVariable(LongResolve.class, tTypeVar));
    assertEquals(Double.class, resolveWildcardTypeOrTypeVariable(DoubleResolve.class, tTypeVar));
    // partially resolved
    assertEquals(Number.class, resolveWildcardTypeOrTypeVariable(MedResolve.class, tTypeVar));
    // x
    TypeVariable<?> xTypeVar = (TypeVariable<?>) Resolve.class.getField("x").getGenericType();
    assertEquals(Object.class, resolveWildcardTypeOrTypeVariable(Object.class, xTypeVar));
    assertEquals(
        Boolean.class,
        Types.getArrayComponentType(
            resolveWildcardTypeOrTypeVariable(ArrayResolve.class, xTypeVar)));
    assertEquals(
        Collection.class,
        Types.getRawClass(
            (ParameterizedType)
                resolveWildcardTypeOrTypeVariable(ParameterizedResolve.class, xTypeVar)));
    assertEquals(Number.class, resolveWildcardTypeOrTypeVariable(MedXResolve.class, xTypeVar));
  }

  private static Type resolveWildcardTypeOrTypeVariable(
      Type context, TypeVariable<?> typeVariable) {
    return Data.resolveWildcardTypeOrTypeVariable(Arrays.asList(context), typeVariable);
  }
}
