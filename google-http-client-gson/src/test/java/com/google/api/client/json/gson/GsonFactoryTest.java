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

package com.google.api.client.json.gson;

import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.JsonParser;
import com.google.api.client.util.Key;
import com.google.common.base.Charsets;

import java.io.ByteArrayInputStream;
import java.lang.reflect.Type;
import java.util.ArrayList;

/**
 * Tests {@link GsonFactory}.
 *
 * @author Yaniv Inbar
 */
public class GsonFactoryTest extends AbstractJsonFactoryTest {

  private static final String GSON_LINE_SEPARATOR = "\n";

  private static final String JSON_ENTRY_PRETTY =
      "{" + GSON_LINE_SEPARATOR + "  \"title\": \"foo\"" + GSON_LINE_SEPARATOR + "}";
  private static final String JSON_FEED_PRETTY = "{" + GSON_LINE_SEPARATOR + "  \"entries\": ["
      + GSON_LINE_SEPARATOR + "    {" + GSON_LINE_SEPARATOR + "      \"title\": \"foo\""
      + GSON_LINE_SEPARATOR + "    }," + GSON_LINE_SEPARATOR + "    {"
      + GSON_LINE_SEPARATOR + "      \"title\": \"bar\"" + GSON_LINE_SEPARATOR + "    }"
      + GSON_LINE_SEPARATOR + "  ]" + GSON_LINE_SEPARATOR + "}";

  public GsonFactoryTest(String name) {
    super(name);
  }

  @Override
  protected JsonFactory newFactory() {
    return new GsonFactory();
  }

  public final void testToPrettyString_entry() throws Exception {
    Entry entry = new Entry();
    entry.title = "foo";
    assertEquals(JSON_ENTRY_PRETTY, newFactory().toPrettyString(entry));
  }

  public final void testToPrettyString_Feed() throws Exception {
    Feed feed = new Feed();
    Entry entryFoo = new Entry();
    entryFoo.title = "foo";
    Entry entryBar = new Entry();
    entryBar.title = "bar";
    feed.entries = new ArrayList<Entry>();
    feed.entries.add(entryFoo);
    feed.entries.add(entryBar);
    assertEquals(JSON_FEED_PRETTY, newFactory().toPrettyString(feed));
  }

  public final void testGson_ASCII() throws Exception {
    byte[] asciiJson = Charsets.US_ASCII.encode("{ \"foo\": 123 }").array();
    JsonParser jp =
        newFactory().createJsonParser(new ByteArrayInputStream(asciiJson), Charsets.US_ASCII);
    assertEquals(com.google.api.client.json.JsonToken.START_OBJECT, jp.nextToken());
    assertEquals(com.google.api.client.json.JsonToken.FIELD_NAME, jp.nextToken());
    assertEquals(com.google.api.client.json.JsonToken.VALUE_NUMBER_INT, jp.nextToken());
    assertEquals(123, jp.getIntValue());
    assertEquals(com.google.api.client.json.JsonToken.END_OBJECT, jp.nextToken());
  }

  public final void testParse_directValue() throws Exception {
    byte[] jsonData = Charsets.UTF_8.encode("123").array();
    JsonParser jp =
        newFactory().createJsonParser(new ByteArrayInputStream(jsonData), Charsets.US_ASCII);
    assertEquals(123, jp.parse(Integer.class, true, null));
  }

  public final void testParse_array() throws Exception {
    byte[] jsonData = Charsets.UTF_8.encode("[ 123, 456 ]").array();
    JsonParser jp =
        newFactory().createJsonParser(new ByteArrayInputStream(jsonData), Charsets.US_ASCII);
    Type myType = Integer[].class;
    Integer[] array = (Integer[]) jp.parse(myType, true, null);
    assertNotNull(array);
    assertEquals((Integer)123, array[0]);
    assertEquals((Integer)456, array[1]);
  }

  public static class TestClass {
    public TestClass() {}

    @Key("foo")
    public int foo;
  }

  public final void testParse_class() throws Exception {
    byte[] jsonData = Charsets.UTF_8.encode("{ \"foo\": 123 }").array();
    JsonParser jp =
        newFactory().createJsonParser(new ByteArrayInputStream(jsonData), Charsets.US_ASCII);
    Type myType = TestClass.class;
    TestClass instance = (TestClass) jp.parse(myType, true, null);
    assertNotNull(instance);
    assertEquals(123, instance.foo);
  }

  public final void testCreateJsonParser_nullCharset() throws Exception {
    byte[] jsonData = Charsets.UTF_8.encode("{ \"foo\": 123 }").array();
    JsonParser jp =
        newFactory().createJsonParser(new ByteArrayInputStream(jsonData), null);
    Type myType = TestClass.class;
    TestClass instance = (TestClass) jp.parse(myType, true, null);
    assertNotNull(instance);
    assertEquals(123, instance.foo);
  }
}
