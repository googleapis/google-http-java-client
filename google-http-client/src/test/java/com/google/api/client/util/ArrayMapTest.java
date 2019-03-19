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

package com.google.api.client.util;

import java.util.Iterator;
import java.util.Map;
import junit.framework.TestCase;

/**
 * Tests {@link ArrayMap}.
 *
 * @author Yaniv Inbar
 */
public class ArrayMapTest extends TestCase {

  public ArrayMapTest() {}

  public ArrayMapTest(String testName) {
    super(testName);
  }

  public void testOf_zero() {
    ArrayMap<String, Integer> map = ArrayMap.of();
    assertTrue(map.isEmpty());
  }

  public void testOf_one() {
    ArrayMap<String, Integer> map = ArrayMap.of("a", 1);
    assertEquals(1, map.size());
    assertEquals("a", map.getKey(0));
    assertEquals((Integer) 1, map.getValue(0));
  }

  public void testOf_two() {
    ArrayMap<String, Integer> map = ArrayMap.of("a", 1, "b", 2);
    assertEquals(2, map.size());
    assertEquals("a", map.getKey(0));
    assertEquals((Integer) 1, map.getValue(0));
    assertEquals("b", map.getKey(1));
    assertEquals((Integer) 2, map.getValue(1));
  }

  public void testRemove1() {
    ArrayMap<String, Integer> map = ArrayMap.of("a", 1, "b", 2);
    map.remove("b");
    assertEquals(ArrayMap.of("a", 1), map);
  }

  public void testRemove2() {
    ArrayMap<String, Integer> map = ArrayMap.of("a", 1, "b", 2);
    map.remove("a");
    assertEquals(ArrayMap.of("b", 2), map);
  }

  public void testRemove3() {
    ArrayMap<String, Integer> map = ArrayMap.of("a", 1);
    map.remove("a");
    assertEquals(ArrayMap.of(), map);
  }

  public void testRemove4() {
    ArrayMap<String, Integer> map = ArrayMap.of("a", 1, "b", 2, "c", 3);
    map.remove("b");
    assertEquals(ArrayMap.of("a", 1, "c", 3), map);
  }

  public void testClone_changingEntrySet() {
    ArrayMap<String, String> map = ArrayMap.of();
    assertEquals("{}", map.toString());
    ArrayMap<String, String> clone = map.clone();
    clone.add("foo", "bar");
    assertEquals("{foo=bar}", clone.toString());
  }

  public void testSet() {
    ArrayMap<String, Integer> map = ArrayMap.of();
    map.set(0, "a", 1);
    assertEquals(ArrayMap.of("a", 1), map);
    map.set(0, 2);
    assertEquals(ArrayMap.of("a", 2), map);
    try {
      map.set(-1, 1);
      fail("expected ArrayIndexOutOfBoundsException");
    } catch (IndexOutOfBoundsException e) {
    }
    try {
      map.set(1, 1);
      fail("expected ArrayIndexOutOfBoundsException");
    } catch (IndexOutOfBoundsException e) {
    }
  }

  public void testHashCode() {
    ArrayMap<String, Integer> map = ArrayMap.of();
    map.set(0, "a", null);
    map.set(1, null, 1);
    map.set(2, null, null);

    assertTrue(map.hashCode() > 0);
  }

  public void testIteratorRemove1() {
    ArrayMap<String, String> map = new ArrayMap<String, String>();
    map.put("a", "a");
    map.put("b", "b");
    map.put("c", "c");
    Iterator<Map.Entry<String, String>> iter = map.entrySet().iterator();
    while (iter.hasNext()) {
      Map.Entry<String, String> entry = iter.next();
      if (!"all".equalsIgnoreCase(entry.getKey())) {
        iter.remove();
      }
    }
    assertEquals(0, map.size());
  }

  public void testIteratorRemove2() {
    ArrayMap<String, String> map = new ArrayMap<String, String>();
    map.put("a", "a");
    map.put("b", "b");
    map.put("c", "c");
    Iterator<Map.Entry<String, String>> iter = map.entrySet().iterator();
    while (iter.hasNext()) {
      Map.Entry<String, String> entry = iter.next();
      if ("b".equalsIgnoreCase(entry.getKey())) {
        iter.remove();
      }
    }
    assertEquals(2, map.size());
    assertEquals("a", map.get("a"));
    assertEquals("c", map.get("c"));
  }

  public void testIteratorRemove3() {
    ArrayMap<String, String> map = new ArrayMap<String, String>();
    map.put("a", "a");
    map.put("b", "b");
    map.put("c", "c");
    Iterator<Map.Entry<String, String>> iter = map.entrySet().iterator();
    while (iter.hasNext()) {
      Map.Entry<String, String> entry = iter.next();
      if (!"b".equalsIgnoreCase(entry.getKey())) {
        iter.remove();
      }
    }
    assertEquals(1, map.size());
    assertEquals("b", map.get("b"));
  }
}
