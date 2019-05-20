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

import java.nio.charset.StandardCharsets;
import junit.framework.TestCase;

/**
 * Tests {@link Base64}.
 *
 * @author Jeff Ching
 */
public class Base64Test extends TestCase {

  public void testHandlesPadding() {
    String encoded = "Zm9vOmJhcg==";
    assertEquals("foo:bar", new String(Base64.decodeBase64(encoded), StandardCharsets.UTF_8));
  }

  public void testHandlesWithoutPadding() {
    String encoded = "Zm9vOmJhcg";
    assertEquals("foo:bar", new String(Base64.decodeBase64(encoded), StandardCharsets.UTF_8));
  }

  public void testHandlesWithExtraWhitespace() {
    // Some internal use cases append extra space characters that apache-commons base64 decoding
    // previously handled.
    String encoded = "Zm9vOmJhcg==\r\n";
    assertEquals("foo:bar", new String(Base64.decodeBase64(encoded), StandardCharsets.UTF_8));
  }

  public void testNullInput() {
    String encoded = null;
    assertNull(Base64.decodeBase64(encoded));
  }
}
