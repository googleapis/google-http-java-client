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

import junit.framework.TestCase;

/**
 * Tests {@link FieldInfo}.
 *
 * @author Yaniv Inbar
 */
public class FieldInfoTest extends TestCase {

  public enum E {
    @Value
    VALUE,
    @Value("other")
    OTHER_VALUE,
    @NullValue
    NULL,
    IGNORED_VALUE
  }

  public void testOf_enum() throws Exception {
    assertEquals(E.class.getField("VALUE"), FieldInfo.of(E.VALUE).getField());
    assertEquals(E.class.getField("OTHER_VALUE"), FieldInfo.of(E.OTHER_VALUE).getField());
    assertEquals(E.class.getField("NULL"), FieldInfo.of(E.NULL).getField());
    try {
      FieldInfo.of(E.IGNORED_VALUE);
      fail("expected IllegalArgumentException");
    } catch (IllegalArgumentException e) {
    }
  }

  public void testEnumValue() {
    assertEquals(E.VALUE, FieldInfo.of(E.VALUE).<E>enumValue());
    assertEquals(E.OTHER_VALUE, FieldInfo.of(E.OTHER_VALUE).<E>enumValue());
    assertEquals(E.NULL, FieldInfo.of(E.NULL).<E>enumValue());
  }
}
