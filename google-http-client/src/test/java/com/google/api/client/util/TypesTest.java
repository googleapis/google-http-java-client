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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.TreeMap;
import java.util.Vector;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/**
 * Tests {@link Types}.
 *
 * @author Yaniv Inbar
 */
@RunWith(JUnit4.class)
public class TypesTest {

  @Test
  public void testIsAssignableToOrFrom() {
    assertTrue(Types.isAssignableToOrFrom(String.class, Object.class));
    assertTrue(Types.isAssignableToOrFrom(String.class, String.class));
    assertTrue(Types.isAssignableToOrFrom(Object.class, String.class));
    assertFalse(Types.isAssignableToOrFrom(String.class, List.class));
  }

  static class Foo {}

  @Test
  public void testNewInstance() {
    assertEquals(Object.class, Types.newInstance(Object.class).getClass());
    assertEquals(String.class, Types.newInstance(String.class).getClass());
    assertEquals(Foo.class, Types.newInstance(Foo.class).getClass());
    try {
      Types.newInstance(int.class);
      fail("expected " + IllegalArgumentException.class);
    } catch (IllegalArgumentException e) {
    }
    try {
      Types.newInstance(String[].class);
      fail("expected " + IllegalArgumentException.class);
    } catch (IllegalArgumentException e) {
    }
    try {
      Types.newInstance(Void.class);
      fail("expected " + IllegalArgumentException.class);
    } catch (IllegalArgumentException e) {
    }
  }

  @SuppressWarnings("serial")
  static class IntegerList extends ArrayList<Integer> {}

  static class WildcardBounds {
    public Collection<?> any;
    public Collection<? extends Number> upper;
    public Collection<? super Integer> lower;
  }

  @Test
  public void testGetBound() throws Exception {
    subtestGetBound(Object.class, "any");
    subtestGetBound(Number.class, "upper");
    subtestGetBound(Integer.class, "lower");
  }

  public void subtestGetBound(Type expectedBound, String fieldName) throws Exception {
    ParameterizedType collectionType =
        (ParameterizedType) WildcardBounds.class.getField(fieldName).getGenericType();
    WildcardType wildcardType = (WildcardType) collectionType.getActualTypeArguments()[0];
    assertEquals(expectedBound, Types.getBound(wildcardType));
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

  @Test
  public void testResolveTypeVariable() throws Exception {
    // t
    TypeVariable<?> tTypeVar = (TypeVariable<?>) Resolve.class.getField("t").getGenericType();
    assertNull(resolveTypeVariable(Object.class, tTypeVar));
    assertNull(resolveTypeVariable(Resolve.class, tTypeVar));
    assertEquals(Integer.class, resolveTypeVariable(IntegerResolve.class, tTypeVar));
    assertEquals(Long.class, resolveTypeVariable(LongResolve.class, tTypeVar));
    assertEquals(Double.class, resolveTypeVariable(DoubleResolve.class, tTypeVar));
    // partially resolved
    assertEquals(
        MedResolve.class,
        ((TypeVariable<?>) resolveTypeVariable(MedResolve.class, tTypeVar))
            .getGenericDeclaration());
    // x
    TypeVariable<?> xTypeVar = (TypeVariable<?>) Resolve.class.getField("x").getGenericType();
    assertNull(resolveTypeVariable(Object.class, xTypeVar));
    assertEquals(
        Boolean.class,
        Types.getArrayComponentType(resolveTypeVariable(ArrayResolve.class, xTypeVar)));
    assertEquals(
        Collection.class,
        Types.getRawClass(
            (ParameterizedType) resolveTypeVariable(ParameterizedResolve.class, xTypeVar)));
  }

  private static Type resolveTypeVariable(Type context, TypeVariable<?> typeVariable) {
    return Types.resolveTypeVariable(Arrays.asList(context), typeVariable);
  }

  public class A<T> {
    public Iterable<String> i;
    public ArrayList<String> a;

    @SuppressWarnings({"unchecked", "rawtypes"})
    public ArrayList aNoType;

    public Stack<? extends Number> wild;
    public Vector<Integer[]> arr;
    public Vector<T[]> tarr;
    public LinkedList<ArrayList<Boolean>> list;
    public Iterable<T> tv;
    public ArrayList<T> atv;
  }

  public class B extends A<DateTime> {}

  @Test
  public void testGetIterableParameter() throws Exception {
    assertEquals(
        "T",
        ((TypeVariable<?>) Types.getIterableParameter(A.class.getField("tv").getGenericType()))
            .getName());
    assertEquals(
        "T",
        ((TypeVariable<?>) Types.getIterableParameter(A.class.getField("atv").getGenericType()))
            .getName());
    assertEquals(String.class, Types.getIterableParameter(A.class.getField("i").getGenericType()));
    assertEquals(String.class, Types.getIterableParameter(A.class.getField("a").getGenericType()));
    assertEquals(
        "E",
        ((TypeVariable<?>) Types.getIterableParameter(A.class.getField("aNoType").getGenericType()))
            .getName());
    assertEquals(
        Integer.class,
        Types.getArrayComponentType(
            Types.getIterableParameter(A.class.getField("arr").getGenericType())));
    assertEquals(
        "T",
        ((GenericArrayType) Types.getIterableParameter(A.class.getField("tarr").getGenericType()))
            .getGenericComponentType()
            .toString());
    assertEquals(
        ArrayList.class,
        ((ParameterizedType) Types.getIterableParameter(A.class.getField("list").getGenericType()))
            .getRawType());
    assertEquals(
        Number.class,
        ((WildcardType) Types.getIterableParameter(A.class.getField("wild").getGenericType()))
            .getUpperBounds()[0]);
  }

  public class C<T> {
    public Map<String, String> i;
    public ArrayMap<String, String> a;

    @SuppressWarnings({"unchecked", "rawtypes"})
    public ArrayMap aNoType;

    public TreeMap<String, ? extends Number> wild;
    public Vector<Integer[]> arr;
    public HashMap<String, T[]> tarr;
    public ImmutableMap<String, ArrayList<Boolean>> list;
    public Map<String, T> tv;
    public ArrayMap<String, T> atv;
  }

  public class D extends C<DateTime> {}

  @Test
  public void testGetMapParameter() throws Exception {
    assertEquals(
        "T",
        ((TypeVariable<?>) Types.getMapValueParameter(C.class.getField("tv").getGenericType()))
            .getName());
    assertEquals(
        "T",
        ((TypeVariable<?>) Types.getMapValueParameter(C.class.getField("atv").getGenericType()))
            .getName());
    assertEquals(String.class, Types.getMapValueParameter(C.class.getField("i").getGenericType()));
    assertEquals(String.class, Types.getMapValueParameter(C.class.getField("a").getGenericType()));
    assertEquals(
        "V",
        ((TypeVariable<?>) Types.getMapValueParameter(C.class.getField("aNoType").getGenericType()))
            .getName());
    assertEquals(
        Integer.class,
        Types.getArrayComponentType(
            Types.getIterableParameter(A.class.getField("arr").getGenericType())));
    assertEquals(
        "T",
        ((GenericArrayType) Types.getMapValueParameter(C.class.getField("tarr").getGenericType()))
            .getGenericComponentType()
            .toString());
    assertEquals(
        ArrayList.class,
        ((ParameterizedType) Types.getMapValueParameter(C.class.getField("list").getGenericType()))
            .getRawType());
    assertEquals(
        Number.class,
        ((WildcardType) Types.getMapValueParameter(C.class.getField("wild").getGenericType()))
            .getUpperBounds()[0]);
  }

  @Test
  public void testIterableOf() {
    List<String> list = ImmutableList.of("a");
    assertEquals(list, Types.iterableOf(list));
    assertEquals(list, Types.iterableOf(new String[] {"a"}));
    assertTrue(Iterables.elementsEqual(ImmutableList.of(1), Types.iterableOf(new int[] {1})));
  }

  @Test
  public void testToArray() {
    assertTrue(
        Arrays.equals(
            new String[] {"a", "b"},
            (String[]) Types.toArray(ImmutableList.of("a", "b"), String.class)));
    assertTrue(
        Arrays.equals(
            new Integer[] {1, 2},
            (Integer[]) Types.toArray(ImmutableList.of(1, 2), Integer.class)));
    assertTrue(
        Arrays.equals(new int[] {1, 2}, (int[]) Types.toArray(ImmutableList.of(1, 2), int.class)));
    int[][] arr = (int[][]) Types.toArray(ImmutableList.of(new int[] {1, 2}), int[].class);
    assertEquals(1, arr.length);
    assertTrue(Arrays.equals(new int[] {1, 2}, arr[0]));
  }
}
