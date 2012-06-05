/*
 * Copyright (c) 2012 Google Inc.
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

package com.google.api.client.json;

import com.google.api.client.json.gson.GsonFactory;
import com.google.common.collect.ImmutableSet;

import junit.framework.TestCase;

/**
 * Tests for the {@link JsonParser} class.
 *
 * @author Matthias Linder (mlinder)
 * @since 1.10
 */
public class JsonParserTest extends TestCase {
  private static final String JSON_THREE_ELEMENTS =
      "{" + "  \"one\": { \"num\": 1 }" +
            ", \"two\": { \"num\": 2 }" +
            ", \"three\": { \"num\": 3 }" + "}";

  /** Returns a JsonParser which parses the specified string. */
  private JsonParser createParser(String json) {
    return new GsonFactory().createJsonParser(json);
  }

  public void testSkipToKey_firstKey() throws Exception {
    JsonParser parser = createParser(JSON_THREE_ELEMENTS);
    assertEquals("one", parser.skipToKey(ImmutableSet.of("one")));
    parser.skipToKey("num");
    assertEquals(1, parser.getIntValue());
  }

  public void testSkipToKey_lastKey() throws Exception {
    JsonParser parser = createParser(JSON_THREE_ELEMENTS);
    assertEquals("three", parser.skipToKey(ImmutableSet.of("three")));
    parser.skipToKey("num");
    assertEquals(3, parser.getIntValue());
  }

  public void testSkipToKey_multipleKeys() throws Exception {
    JsonParser parser = createParser(JSON_THREE_ELEMENTS);
    assertEquals("two", parser.skipToKey(ImmutableSet.of("foo", "three", "two")));
    parser.skipToKey("num");
    assertEquals(2, parser.getIntValue());
  }

  public void testSkipToKey_noMatch() throws Exception {
    JsonParser parser = createParser(JSON_THREE_ELEMENTS);
    assertEquals(null, parser.skipToKey(ImmutableSet.of("foo", "bar", "num")));
    assertEquals(JsonToken.END_OBJECT, parser.getCurrentToken());
  }
}
