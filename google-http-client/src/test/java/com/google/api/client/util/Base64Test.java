/*
 * Copyright (c) 2017 Google Inc.
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

import java.util.Arrays;

/**
 * Tests {@link Base64}.
 *
 * @author mwhisenhunt@google.com (Matt Whisenhunt)
 */
public class Base64Test extends TestCase {

  public void testEmptyValue() {
    byte[] emptyValue = new byte[0];

    assertEquals(Base64.encodeBase64(emptyValue).length, 0);
    assertEquals(Base64.encodeBase64String(emptyValue).length(), 0);
    assertEquals(Base64.encodeBase64URLSafe(emptyValue).length, 0);
    assertEquals(Base64.encodeBase64URLSafeString(emptyValue).length(), 0);
    assertEquals(Base64.decodeBase64(emptyValue).length, 0);
    assertEquals(Base64.decodeBase64(emptyValue).length, 0);
  }

  public void testNulls() {
    assertNull(Base64.encodeBase64(null));
    assertNull(Base64.encodeBase64String(null));
    assertNull(Base64.encodeBase64URLSafe(null));
    assertNull(Base64.encodeBase64URLSafeString(null));
    assertNull(Base64.decodeBase64((byte[]) null));
    assertNull(Base64.decodeBase64((String) null));
  }

  public void testEncode() {
    assertEquals("Zm9vYmFy", new String(Base64.encodeBase64("foobar".getBytes())));
    assertEquals("Zm9vYmFy", Base64.encodeBase64String("foobar".getBytes()));

    assertEquals("Zm9vYmFy", new String(Base64.encodeBase64URLSafe("foobar".getBytes())));
    assertEquals("Zm9vYmFy", Base64.encodeBase64URLSafeString("foobar".getBytes()));
  }

  public void testDecode() {
    String value = new String(Base64.decodeBase64(Base64.encodeBase64("foobar".getBytes())));
    assertEquals("foobar", value);
  }
}
