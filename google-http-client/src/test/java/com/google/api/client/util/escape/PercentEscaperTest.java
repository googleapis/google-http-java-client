/*
 * Copyright 2020 Google LLC
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

import org.junit.Assert;
import org.junit.Test;

public class PercentEscaperTest {

  @Test
  public void testEscapeSpace() {
    PercentEscaper escaper =
        new PercentEscaper(PercentEscaper.SAFE_PLUS_RESERVED_CHARS_URLENCODER, false);
    String actual = escaper.escape("Hello there");
    Assert.assertEquals("Hello%20there", actual);
  }

  @Test
  public void testEscapeSpaceDefault() {
    PercentEscaper escaper = new PercentEscaper(PercentEscaper.SAFE_PLUS_RESERVED_CHARS_URLENCODER);
    String actual = escaper.escape("Hello there");
    Assert.assertEquals("Hello%20there", actual);
  }
}
