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

import com.google.api.client.util.GenericData.Flags;

import junit.framework.TestCase;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import org.junit.Assert;

/**
 * Tests {@link GenericData}.
 *
 * @author Yaniv Inbar
 */
public class GenericDataTest extends TestCase {
  private class MyData extends GenericData {
    public MyData() {
      super(EnumSet.of(Flags.IGNORE_CASE));
    }

    @Key("FieldA")
    public String fieldA;

    @Key("FieldB")
    public List<String> fieldB;

    public void setFieldB(String fieldB) {
      this.fieldB = Lists.newArrayList();
      this.fieldB.add(fieldB);
    }

    public void setFieldB(List<String> fieldB) {
      this.fieldB = fieldB;
    }
  }

  private class GenericData1 extends GenericData {
    public GenericData1() {
      super(EnumSet.of(Flags.IGNORE_CASE));
    }

    @Key("FieldA")
    public String fieldA;
  }

  private class GenericData2 extends GenericData {
    public GenericData2() {
      super(EnumSet.of(Flags.IGNORE_CASE));
    }

    @Key("FieldA")
    public String fieldA;
  }

  public void testEquals_Symmetric() {
    GenericData actual = new GenericData1();
    actual.set("fieldA", "bar");
    GenericData expected = new GenericData2();
    // Test that objects are equal.
    expected.set("fieldA", "bar");
    assertNotSame(expected, actual);
    assertTrue(expected.equals(expected) && actual.equals(actual));
    // Test that objects not are equal.
    expected.set("fieldA", "far");
    assertFalse(expected.equals(actual) || actual.equals(expected));
    assertFalse(expected.hashCode() == actual.hashCode());
  }

  public void testEquals_SymmetricWithSameClass() {
    GenericData actual = new MyData();
    actual.set("fieldA", "bar");
    GenericData expected = new MyData();
    // Test that objects are equal.
    expected.set("fieldA", "bar");
    assertNotSame(expected, actual);
    assertTrue(expected.equals(expected) && actual.equals(actual));
    assertTrue(expected.hashCode() == expected.hashCode());
  }

  public void testNotEquals_SymmetricWithSameClass() {
    GenericData actual = new MyData();
    actual.set("fieldA", "bar");
    GenericData expected = new MyData();
    // Test that objects are not equal.
    expected.set("fieldA", "far");
    assertNotSame(expected, actual);
    assertFalse(expected.equals(actual) || actual.equals(expected));
    assertFalse(expected.hashCode() == actual.hashCode());
  }

  public void testClone_changingEntrySet() {
    GenericData data = new GenericData();
    assertEquals("GenericData{classInfo=[], {}}", data.toString());
    GenericData clone = data.clone();
    clone.set("foo", "bar");
    assertEquals("GenericData{classInfo=[], {foo=bar}}", clone.toString());
  }

  public void testSetIgnoreCase_unknownKey() {
    GenericData data = new GenericData(EnumSet.of(Flags.IGNORE_CASE));
    data.set("Foobar", "oldValue");
    assertEquals("oldValue", data.get("Foobar"));
    assertEquals(1, data.getUnknownKeys().size());

    // Test the collision case.
    data.set("fooBAR", "newValue");
    assertEquals("newValue", data.get("Foobar"));
    assertEquals(1, data.getUnknownKeys().size());
  }

  public void testSetIgnoreCase_class() {
    MyData data = new MyData();
    data.set("FIELDA", "someValue");
    assertEquals("someValue", data.fieldA);
    assertEquals(0, data.getUnknownKeys().size());
  }

  public void testPutIgnoreCase_class() {
    MyData data = new MyData();
    data.fieldA = "123";
    assertEquals("123", data.put("FIELDA", "someValue"));
    assertEquals("someValue", data.fieldA);
    assertEquals(0, data.getUnknownKeys().size());
  }

  public void testGetIgnoreCase_class() {
    MyData data = new MyData();
    data.fieldA = "someValue";
    assertEquals("someValue", data.get("FIELDA"));
  }

  public void testRemoveIgnoreCase_class() {
    MyData data = new MyData();
    data.fieldA = "someValue";
    try {
      data.remove("FIELDA");
      Assert.fail("Tried to remove known field from class");
    } catch (UnsupportedOperationException expected) {
    }
  }

  public void testPutIgnoreCase_unknownKey() {
    GenericData data = new GenericData(EnumSet.of(Flags.IGNORE_CASE));
    assertEquals(null, data.put("fooBAR", "oldValue"));
    assertEquals("oldValue", data.get("fooBAR"));
    assertEquals(1, data.getUnknownKeys().size());

    // Test the collision case.
    assertEquals("oldValue", data.put("fOObar", "newValue"));
    assertEquals("newValue", data.get("fooBAR"));
    assertEquals(1, data.getUnknownKeys().size());
  }

  public void testGetIgnoreCase_unknownKey() {
    GenericData data = new GenericData(EnumSet.of(Flags.IGNORE_CASE));
    data.set("One", 1);
    assertEquals(1, data.get("ONE"));

    data.set("one", 2);
    assertEquals(2, data.get("ONE"));

    assertEquals(null, data.get("unknownKey"));
  }

  public void testRemoveIgnoreCase_unknownKey() {
    GenericData data = new GenericData(EnumSet.of(Flags.IGNORE_CASE));
    data.set("One", 1);
    assertEquals(1, data.remove("OnE"));
    assertEquals(0, data.getUnknownKeys().size());

    data.set("testA", 1).set("testa", 2);
    assertEquals(2, data.remove("TESTA"));
    assertEquals(null, data.remove("TESTA"));
  }

  public void testPutShouldUseSetter() {
    MyData data = new MyData();
    data.put("fieldB", "value1");
    assertEquals("value1", data.fieldB.get(0));
    List<String> list = new ArrayList<>();
    list.add("value2");
    data.put("fieldB", list);
    assertEquals(list, data.fieldB);
  }
}
