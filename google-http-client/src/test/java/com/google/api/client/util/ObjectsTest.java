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

package com.google.api.client.util;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/**
 * Tests {@link Objects}.
 *
 * @author Yaniv Inbar
 */
@RunWith(JUnit4.class)
public class ObjectsTest {
  @Test
  public void testToStringHelper() {
    String toTest = Objects.toStringHelper(new TestClass()).add("hello", "world").toString();
    assertEquals("TestClass{hello=world}", toTest);
  }

  @Test
  public void testConstructor_innerClass() {
    String toTest = Objects.toStringHelper(new TestClass()).toString();
    assertEquals("TestClass{}", toTest);
  }

  @Test
  public void testToString_oneIntegerField() {
    String toTest =
        Objects.toStringHelper(new TestClass()).add("field1", Integer.valueOf(42)).toString();
    assertEquals("TestClass{field1=42}", toTest);
  }

  @Test
  public void testToStringOmitNullValues_oneField() {
    String toTest =
        Objects.toStringHelper(new TestClass()).omitNullValues().add("field1", null).toString();
    assertEquals("TestClass{}", toTest);
  }

  private static class TestClass {}
}
