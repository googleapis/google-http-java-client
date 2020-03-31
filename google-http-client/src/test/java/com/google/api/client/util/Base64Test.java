/*
 * Copyright 2019 Google LLC
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

import com.google.api.client.util.Charsets;
import junit.framework.TestCase;

/**
 * Tests {@link Base64}.
 *
 * @author Jeff Ching
 */
public class Base64Test extends TestCase {

  public void test_decodeBase64_withPadding() {
    String encoded = "Zm9vOmJhcg==";
    assertEquals("foo:bar", new String(Base64.decodeBase64(encoded), Charsets.UTF_8));
  }

  public void test_decodeBase64_withoutPadding() {
    String encoded = "Zm9vOmJhcg";
    assertEquals("foo:bar", new String(Base64.decodeBase64(encoded), Charsets.UTF_8));
  }

  public void test_decodeBase64_withTrailingWhitespace() {
    // Some internal use cases append extra space characters that apache-commons base64 decoding
    // previously handled.
    String encoded = "Zm9vOmJhcg==\r\n";
    assertEquals("foo:bar", new String(Base64.decodeBase64(encoded), Charsets.UTF_8));
  }

  public void test_decodeBase64_withNullBytes_shouldReturnNull() {
    byte[] encoded = null;
    assertNull(Base64.decodeBase64(encoded));
  }

  public void test_decodeBase64_withNull_shouldReturnNull() {
    String encoded = null;
    assertNull(Base64.decodeBase64(encoded));
  }

  public void test_encodeBase64URLSafeString_withNull_shouldReturnNull() {
    assertNull(Base64.encodeBase64URLSafeString(null));
  }

  public void test_encodeBase64URLSafe_withNull_shouldReturnNull() {
    assertNull(Base64.encodeBase64URLSafe(null));
  }

  public void test_encodeBase64_withNull_shouldReturnNull() {
    assertNull(Base64.encodeBase64(null));
  }
}
