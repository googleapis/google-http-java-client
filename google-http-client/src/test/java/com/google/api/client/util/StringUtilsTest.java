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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/**
 * Tests {@link StringUtils}.
 *
 * @author Yaniv Inbar
 */
@RunWith(JUnit4.class)
public class StringUtilsTest {

  private static final byte[] SAMPLE_UTF8;
  private static final String SAMPLE;

  static {
    SAMPLE_UTF8 = new byte[] {49, 50, 51, -41, -103, -41, -96, -41, -103, -41, -111};
    SAMPLE = "123\u05D9\u05e0\u05D9\u05D1";
  }

  @Test
  public void testLineSeparator() {
    assertNotNull(StringUtils.LINE_SEPARATOR);
  }

  @Test
  public void testToBytesUtf8() {
    Assert.assertArrayEquals(SAMPLE_UTF8, StringUtils.getBytesUtf8(SAMPLE));
  }

  @Test
  public void testToBytesUtf8Null() {
    assertNull(StringUtils.getBytesUtf8(null));
  }

  @Test
  public void testFromBytesUtf8() {
    assertEquals(SAMPLE, StringUtils.newStringUtf8(SAMPLE_UTF8));
  }

  @Test
  public void testFromBytesUtf8Null() {
    assertNull(StringUtils.newStringUtf8(null));
  }
}
