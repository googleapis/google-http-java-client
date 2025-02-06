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

import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.nio.charset.StandardCharsets;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/**
 * Tests {@link Base64}.
 *
 * @author Jeff Ching
 */
@RunWith(JUnit4.class)
public class Base64Test {

  @Test
  public void test_decodeBase64_withPadding() {
    String encoded = "Zm9vOmJhcg==";
    assertEquals("foo:bar", new String(Base64.decodeBase64(encoded), StandardCharsets.UTF_8));
  }

  @Test
  public void test_decodeBase64_withoutPadding() {
    String encoded = "Zm9vOmJhcg";
    assertEquals("foo:bar", new String(Base64.decodeBase64(encoded), StandardCharsets.UTF_8));
  }

  @Test
  public void test_decodeBase64_withTrailingWhitespace() {
    // Some internal use cases append extra space characters that apache-commons base64 decoding
    // previously handled.
    String encoded = "Zm9vOmJhcg==\r\n";
    assertEquals("foo:bar", new String(Base64.decodeBase64(encoded), StandardCharsets.UTF_8));
  }

  @Test
  public void test_decodeBase64_withNullBytes_shouldReturnNull() {
    byte[] encoded = null;
    assertNull(Base64.decodeBase64(encoded));
  }

  @Test
  public void test_decodeBase64_withNull_shouldReturnNull() {
    String encoded = null;
    assertNull(Base64.decodeBase64(encoded));
  }

  @Test
  public void test_encodeBase64URLSafeString_withNull_shouldReturnNull() {
    assertNull(Base64.encodeBase64URLSafeString(null));
  }

  @Test
  public void test_encodeBase64URLSafe_withNull_shouldReturnNull() {
    assertNull(Base64.encodeBase64URLSafe(null));
  }

  @Test
  public void test_encodeBase64_withNull_shouldReturnNull() {
    assertNull(Base64.encodeBase64(null));
  }

  @Test
  public void test_decodeBase64_newline_character_invalid_length() {
    // The RFC 4648 (https://datatracker.ietf.org/doc/html/rfc4648#section-3.3) states that a
    // specification referring to the Base64 encoding may state that it ignores characters outside
    // the base alphabet.

    // In Base64 encoding, 3 characters (24 bits) are converted to 4 of 6-bits, each of which is
    // converted to a byte (a character).
    // Base64encode("abc") => "YWJj" (4 characters)
    // Base64encode("def") => "ZGVm" (4 characters)
    // Adding a new line character between them. This should be discarded.
    String encodedString = "YWJj\nZGVm";

    // This is a reference implementation by Apache Commons Codec. It discards the new line
    // characters.
    // assertEquals(
    //    "abcdef",
    //    new String(
    //        org.apache.commons.codec.binary.Base64.decodeBase64(encodedString),
    //        StandardCharsets.UTF_8));

    // This is our implementation. Before the
    // https://github.com/googleapis/google-http-java-client/pull/1941/, it was throwing
    // IllegalArgumentException("Invalid length 9").
    assertEquals("abcdef", new String(Base64.decodeBase64(encodedString), StandardCharsets.UTF_8));
  }

  @Test
  public void test_decodeBase64_newline_character() {
    // In Base64 encoding, 2 characters (16 bits) are converted to 3 of 6-bits plus the padding
    // character ('=").
    // Base64encode("ab") => "YWI=" (3 characters + padding character)
    // Adding a new line character that should be discarded between them
    String encodedString = "YW\nI=";

    // This is a reference implementation by Apache Commons Codec. It discards the new line
    // characters.
    // assertEquals(
    //    "ab",
    //    new String(
    //        org.apache.commons.codec.binary.Base64.decodeBase64(encodedString),
    //        StandardCharsets.UTF_8));

    // This is our implementation. Before the fix
    // https://github.com/googleapis/google-http-java-client/pull/1941/, it was throwing
    // IllegalArgumentException("Unrecognized character: 0xa").
    assertEquals("ab", new String(Base64.decodeBase64(encodedString), StandardCharsets.UTF_8));
  }

  @Test
  public void test_decodeBase64_plus_and_newline_characters() {
    // The plus sign is 62 in the Base64 table. So it's a valid character in encoded strings.
    // https://datatracker.ietf.org/doc/html/rfc4648#section-4
    String encodedString = "+\nw==";

    byte[] actual = Base64.decodeBase64(encodedString);
    // Before the fix https://github.com/googleapis/google-http-java-client/pull/1941/, it was
    // throwing IllegalArgumentException("Unrecognized character: +").
    assertThat(actual).isEqualTo(new byte[] {(byte) 0xfb});
  }

  @Test
  public void test_decodeBase64_slash_and_newline_characters() {
    // The slash sign is 63 in the Base64 table. So it's a valid character in encoded strings.
    // https://datatracker.ietf.org/doc/html/rfc4648#section-4
    String encodedString = "/\nw==";

    byte[] actual = Base64.decodeBase64(encodedString);
    // Before the fix https://github.com/googleapis/google-http-java-client/pull/1941/, it was
    // throwing IllegalArgumentException("Unrecognized character: /").
    assertThat(actual).isEqualTo(new byte[] {(byte) 0xff});
  }
}
