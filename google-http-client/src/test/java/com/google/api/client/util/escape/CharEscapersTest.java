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

package com.google.api.client.util.escape;

import junit.framework.TestCase;

public class CharEscapersTest extends TestCase {

  public void testDecodeUriPath() {
    subtestDecodeUriPath(null, null);
    subtestDecodeUriPath("", "");
    subtestDecodeUriPath("abc", "abc");
    subtestDecodeUriPath("a+b%2Bc", "a+b+c");
    subtestDecodeUriPath("Go%3D%23%2F%25%26%20?%3Co%3Egle", "Go=#/%& ?<o>gle");
  }

  private void subtestDecodeUriPath(String input, String expected) {
    String actual = CharEscapers.decodeUriPath(input);
    assertEquals(expected, actual);
  }

  public void testDecodeUri_IllegalArgumentException() {
    subtestDecodeUri_IllegalArgumentException("abc%-1abc");
    subtestDecodeUri_IllegalArgumentException("%JJ");
    subtestDecodeUri_IllegalArgumentException("abc%0");
  }

  private void subtestDecodeUri_IllegalArgumentException(String input) {
    try {
      CharEscapers.decodeUriPath(input);
      fail();
    } catch (IllegalArgumentException expected) {
      assertNotNull(expected.getMessage());
    }
  }
}
