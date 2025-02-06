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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Iterator;
import java.util.Map;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/**
 * Tests {@link ArrayMap}.
 *
 * @author Yaniv Inbar
 */
@RunWith(JUnit4.class)
public class ArrayMapTest {

  @Test
  public void testOf_zero() {
    ArrayMap<String, Integer> map = ArrayMap.of();
    assertTrue(map.isEmpty());
  }

  @Test
  public void testOf_one() {
    ArrayMap<String, Integer> map = ArrayMap.of("a", 1);
    assertEquals(1, map.size());
    assertEquals("a", map.getKey(0));
    assertEquals((Integer) 1, map.getValue(0));
  }

  @Test
  public void testOf_two() {
    ArrayMap<String, Integer> map = ArrayMap.of("a", 1, "b", 2);
    assertEquals(2, map.size());
    assertEquals("a", map.getKey(0));
    assertEquals((Integer) 1, map.getValue(0));
    assertEquals("b", map.getKey(1));
    assertEquals((Integer) 2, map.getValue(1));
  }

  @Test
  public void testRemove1() {
    ArrayMap<String, Integer> map = ArrayMap.of("a", 1, "b", 2);
    map.remove("b");
    assertEquals(ArrayMap.of("a", 1), map);
  }

  @Test
  public void testRemove2() {
    ArrayMap<String, Integer> map = ArrayMap.of("a", 1, "b", 2);
    map.remove("a");
    assertEquals(ArrayMap.of("b", 2), map);
  }

  @Test
  public void testRemove3() {
    ArrayMap<String, Integer> map = ArrayMap.of("a", 1);
    map.remove("a");
    assertEquals(ArrayMap.of(), map);
  }

  @Test
  public void testRemove4() {
    ArrayMap<String, Integer> map = ArrayMap.of("a", 1, "b", 2, "c", 3);
    map.remove("b");
    assertEquals(ArrayMap.of("a", 1, "c", 3), map);
  }

  @Test
  public void testClone_changingEntrySet() {
    ArrayMap<String, String> map = ArrayMap.of();
    assertEquals("{}", map.toString());
    ArrayMap<String, String> clone = map.clone();
    clone.add("foo", "bar");
    assertEquals("{foo=bar}", clone.toString());
  }

  @Test
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

  @Test
  public void testHashCode() {
    ArrayMap<String, Integer> map = ArrayMap.of();
    map.set(0, "a", null);
    map.set(1, null, 1);
    map.set(2, null, null);

    assertTrue(map.hashCode() > 0);
  }

  @Test
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

  @Test
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

  @Test
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
