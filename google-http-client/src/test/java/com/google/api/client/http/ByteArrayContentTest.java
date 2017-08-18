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

package com.google.api.client.http;

import com.google.api.client.util.IOUtils;
import com.google.api.client.util.StringUtils;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import junit.framework.TestCase;

/**
 * Tests {@link ByteArrayContent}.
 *
 * @author Yaniv Inbar
 */
public class ByteArrayContentTest extends TestCase {
  private static final byte[] FOO = StringUtils.getBytesUtf8("foo");

  public void testConstructor() throws IOException {
    subtestConstructor(new ByteArrayContent("type", FOO), "foo");
    subtestConstructor(new ByteArrayContent("type", FOO, 0, 3), "foo");
    subtestConstructor(new ByteArrayContent("type", FOO, 1, 2), "oo");
    subtestConstructor(new ByteArrayContent("type", FOO, 0, 0), "");
    try {
      new ByteArrayContent(null, FOO, -1, 2);
      fail("expected " + IllegalArgumentException.class);
    } catch (IllegalArgumentException e) {
      // expected
      assertEquals("offset -1, length 2, array length 3", e.getMessage());
    }
    try {
      new ByteArrayContent(null, FOO, 2, 2);
      fail("expected " + IllegalArgumentException.class);
    } catch (IllegalArgumentException e) {
      // expected
      assertEquals("offset 2, length 2, array length 3", e.getMessage());
    }
    try {
      new ByteArrayContent(null, FOO, 3, 1);
      fail("expected " + IllegalArgumentException.class);
    } catch (IllegalArgumentException e) {
      // expected
      assertEquals("offset 3, length 1, array length 3", e.getMessage());
    }
  }

  public void subtestConstructor(ByteArrayContent content, String expected) throws IOException {
    assertEquals("type", content.getType());
    assertTrue(content.retrySupported());
    assertEquals(expected.length(), content.getLength());
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    IOUtils.copy(content.getInputStream(), out);
    assertEquals(expected, out.toString());
  }
}
