/*
 * Copyright (c) 2013 Google Inc.
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

package com.google.api.client.test.util.store;

import com.google.api.client.util.Beta;
import com.google.api.client.util.store.DataStore;
import com.google.api.client.util.store.DataStoreFactory;
import com.google.common.collect.Sets;
import java.util.Arrays;
import java.util.Collection;
import java.util.Set;
import junit.framework.TestCase;

/**
 * Tests {@link DataStoreFactory}.
 *
 * @author Yaniv Inbar
 */
@Beta
public abstract class AbstractDataStoreFactoryTest extends TestCase {

  private static final String STRING_ID = "String";
  private static final String BOOLEAN_ID = "Boolean";
  DataStoreFactory dataStore;
  DataStore<String> stringTyped;
  DataStore<Boolean> boolTyped;

  /** Returns a new instance of the data store factory to test. */
  protected abstract DataStoreFactory newDataStoreFactory() throws Exception;

  @Override
  public void setUp() throws Exception {
    dataStore = newDataStoreFactory();
    stringTyped = dataStore.getDataStore(STRING_ID);
    boolTyped = dataStore.getDataStore(BOOLEAN_ID);
  }

  @Override
  public void tearDown() throws Exception {
    stringTyped.clear();
    assertTrue(stringTyped.values().isEmpty());
    boolTyped.clear();
    assertTrue(boolTyped.values().isEmpty());
  }

  private static void assertContentsAnyOrder(Collection<?> c, Object... elts) {
    assertEquals(Sets.newHashSet(c), Sets.newHashSet(Arrays.asList(elts)));
  }

  public void testId() throws Exception {
    subtestIdNoException("1");
    subtestIdNoException("123456789012345678901234567890");
    subtestIdNoException("abcdefghijklmnopqrstuvwxyz");
    subtestIdNoException("ABCDEFGHIJKLMNOPQRSTUVWXYZ");
    subtestIdException("");
    subtestIdException(".");
    subtestIdException(" ");
    subtestIdException("1234567890123456789012345678901");
  }

  private void subtestIdException(String id) throws Exception {
    try {
      subtestIdNoException(id);
      fail("expected " + IllegalArgumentException.class);
    } catch (IllegalArgumentException e) {
      // expected
    }
  }

  private void subtestIdNoException(String id) throws Exception {
    newDataStoreFactory().getDataStore(id);
  }

  public void testSet() throws Exception {
    // get null
    assertNull(stringTyped.get(null));
    // before stored
    assertNull(stringTyped.get("k"));
    // store with basic values
    stringTyped.set("k", "v");
    assertEquals("v", stringTyped.get("k"));
    stringTyped = dataStore.getDataStore(STRING_ID);
    assertEquals("v", stringTyped.get("k"));
    stringTyped = dataStore.getDataStore(STRING_ID);
    assertEquals("v", stringTyped.get("k"));
    // store with new value
    stringTyped.set("k", "new");
    assertEquals("new", stringTyped.get("k"));
    // store on another key
    stringTyped.set("k2", "other");
    assertEquals("new", stringTyped.get("k"));
    assertEquals("other", stringTyped.get("k2"));
    // set null
    try {
      stringTyped.set("k", null);
      fail("expected " + NullPointerException.class);
    } catch (NullPointerException e) {
      // expected
    }
    try {
      stringTyped.set(null, "v");
      fail("expected " + NullPointerException.class);
    } catch (NullPointerException e) {
      // expected
    }
    // boolean
    stringTyped.set("k", "v");
    assertNull(boolTyped.get("k"));
    boolTyped.set("k", true);
    assertEquals("v", stringTyped.get("k"));
    assertTrue(boolTyped.get("k"));
  }

  public void testValues() throws Exception {
    // before
    assertTrue(stringTyped.values().isEmpty());
    // store keys
    stringTyped.set("k", "new");
    stringTyped.set("k2", "other");
    // check values
    assertContentsAnyOrder(stringTyped.values(), "new", "other");
    // delete one
    stringTyped.delete("k");
    assertNull(stringTyped.get("k"));
    assertContentsAnyOrder(stringTyped.values(), "other");
    stringTyped.delete("k2");
    // boolean
    stringTyped.set("k", "v");
    assertTrue(boolTyped.values().isEmpty());
    boolTyped.set("k", true);
    assertContentsAnyOrder(stringTyped.values(), "v");
    assertContentsAnyOrder(boolTyped.values(), true);
  }

  public void testKeySet() throws Exception {
    // before
    assertTrue(stringTyped.keySet().isEmpty());
    // store a key
    stringTyped.set("k", "new");
    Set<String> expected = Sets.newTreeSet();
    expected.add("k");
    assertEquals(expected, Sets.newTreeSet(stringTyped.keySet()));
    // store another key
    stringTyped.set("k2", "other");
    expected.add("k2");
    assertEquals(expected, Sets.newTreeSet(stringTyped.keySet()));
    // clear after
    stringTyped.delete("k2");
    expected.remove("k2");
    assertEquals(expected, Sets.newTreeSet(stringTyped.keySet()));
    stringTyped.delete("k");
    assertTrue(stringTyped.keySet().isEmpty());
  }

  public void testDelete() throws Exception {
    // store with basic values
    stringTyped.set("k", "v").set("k2", "v2");
    assertFalse(stringTyped.isEmpty());
    assertEquals(2, stringTyped.size());
    // delete one key
    stringTyped.delete("k2");
    assertNull(stringTyped.get("k2"));
    stringTyped.delete("k2");
    assertNull(stringTyped.get("k2"));
    assertEquals("v", stringTyped.get("k"));
    assertFalse(stringTyped.isEmpty());
    assertEquals(1, stringTyped.size());
    // delete another key
    stringTyped.delete("k");
    assertNull(stringTyped.get("k"));
    // set null key
    stringTyped.delete(null);
    assertTrue(stringTyped.isEmpty());
    assertEquals(0, stringTyped.size());
  }

  public void testClear() throws Exception {
    // store with basic values
    stringTyped.set("k", "v").set("k2", "v2");
    // clear
    stringTyped.clear();
    assertTrue(stringTyped.isEmpty());
    assertEquals(0, stringTyped.size());
    // clear again
    stringTyped.clear();
    assertTrue(stringTyped.isEmpty());
    assertEquals(0, stringTyped.size());
  }

  public void testLarge() throws Exception {
    // TODO(yanivi): size = 1000? need to speed up JdoDataStoreTest first
    int size = 100;
    for (int i = 0; i < size; i++) {
      stringTyped.set(String.valueOf(i), "hello" + i);
    }
    assertEquals(size, stringTyped.size());
    int mid = size / 2;
    assertEquals("hello" + mid, stringTyped.get(String.valueOf(mid)));
  }

  public void testContainsKeyAndValue() throws Exception {
    // before
    assertFalse(stringTyped.containsKey("k"));
    assertFalse(stringTyped.containsValue("new"));
    assertFalse(stringTyped.containsKey("k2"));
    assertFalse(stringTyped.containsValue("other"));
    // store a key
    stringTyped.set("k", "new");
    assertTrue(stringTyped.containsKey("k"));
    assertFalse(stringTyped.containsKey("k2"));
    assertTrue(stringTyped.containsValue("new"));
    assertFalse(stringTyped.containsValue("other"));
    // store another key
    stringTyped.set("k2", "other");
    assertTrue(stringTyped.containsKey("k"));
    assertTrue(stringTyped.containsKey("k2"));
    assertTrue(stringTyped.containsValue("new"));
    assertTrue(stringTyped.containsValue("other"));
    // clear after
    stringTyped.delete("k");
    assertFalse(stringTyped.containsKey("k"));
    assertTrue(stringTyped.containsKey("k2"));
    assertFalse(stringTyped.containsValue("new"));
    assertTrue(stringTyped.containsValue("other"));
    stringTyped.clear();
    assertFalse(stringTyped.containsKey("k"));
    assertFalse(stringTyped.containsKey("k2"));
    assertFalse(stringTyped.containsValue("new"));
    assertFalse(stringTyped.containsValue("other"));
  }
}
