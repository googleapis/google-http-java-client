/*
 * Copyright 2010 Google Inc.
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

import com.google.api.client.json.GenericJson;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.JsonObjectParser;
import com.google.api.client.json.JsonParser;
import com.google.api.client.test.json.AbstractJsonFactoryTest;
import com.google.gson.stream.MalformedJsonException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
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
  private static final String JSON_FEED_PRETTY =
      "{"
          + GSON_LINE_SEPARATOR
          + "  \"entries\": ["
          + GSON_LINE_SEPARATOR
          + "    {"
          + GSON_LINE_SEPARATOR
          + "      \"title\": \"foo\""
          + GSON_LINE_SEPARATOR
          + "    },"
          + GSON_LINE_SEPARATOR
          + "    {"
          + GSON_LINE_SEPARATOR
          + "      \"title\": \"bar\""
          + GSON_LINE_SEPARATOR
          + "    }"
          + GSON_LINE_SEPARATOR
          + "  ]"
          + GSON_LINE_SEPARATOR
          + "}";

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

  public final void testParse_directValue() throws IOException {
    JsonParser parser = newFactory().createJsonParser("123");
    assertEquals(123, parser.parse(Integer.class, true));
  }

  public final void testGetByteValue() throws IOException {
    JsonParser parser = newFactory().createJsonParser("123");

    try {
      parser.getByteValue();
      fail("should throw IOException");
    } catch (IOException ex) {
      assertNotNull(ex.getMessage());
    }
  }

  public final void testReaderLeniency_lenient() throws IOException {
    JsonObjectParser parser =
        new JsonObjectParser(GsonFactory.builder().setReadLeniency(true).build());

    // This prefix in JSON body is used to prevent Cross-site script inclusion (XSSI).
    InputStream inputStream =
        new ByteArrayInputStream((")]}'\n" + JSON_ENTRY_PRETTY).getBytes(StandardCharsets.UTF_8));
    GenericJson json = parser.parseAndClose(inputStream, StandardCharsets.UTF_8, GenericJson.class);

    assertEquals("foo", json.get("title"));
  }

  public final void testReaderLeniency_not_lenient_by_default() throws IOException {
    JsonObjectParser parser = new JsonObjectParser(GsonFactory.getDefaultInstance());

    try {
      // This prefix in JSON body is used to prevent Cross-site script inclusion (XSSI).
      InputStream inputStream =
          new ByteArrayInputStream((")]}'\n" + JSON_ENTRY_PRETTY).getBytes(StandardCharsets.UTF_8));
      parser.parseAndClose(inputStream, StandardCharsets.UTF_8, GenericJson.class);
      fail("The read leniency should fail the JSON input with XSSI prefix.");
    } catch (MalformedJsonException ex) {
      assertNotNull(ex.getMessage());
    }
  }
}
