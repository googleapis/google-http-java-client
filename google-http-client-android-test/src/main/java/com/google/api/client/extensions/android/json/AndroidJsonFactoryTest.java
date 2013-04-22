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
package com.google.api.client.extensions.android.json;

import com.google.api.client.json.JsonFactory;
import com.google.api.client.test.json.AbstractJsonFactoryTest;

import java.util.ArrayList;

/**
 * Tests {@link AndroidJsonFactory}.
 *
 * @author Yaniv Inbar
 */
public class AndroidJsonFactoryTest extends AbstractJsonFactoryTest {
  
  private static final String GSON_LINE_SEPARATOR = "\n";

  private static final String JSON_ENTRY_PRETTY =
      "{" + GSON_LINE_SEPARATOR + "  \"title\": \"foo\"" + GSON_LINE_SEPARATOR + "}";
  private static final String JSON_FEED_PRETTY = "{" + GSON_LINE_SEPARATOR + "  \"entries\": ["
      + GSON_LINE_SEPARATOR + "    {" + GSON_LINE_SEPARATOR + "      \"title\": \"foo\""
      + GSON_LINE_SEPARATOR + "    }," + GSON_LINE_SEPARATOR + "    {"
      + GSON_LINE_SEPARATOR + "      \"title\": \"bar\"" + GSON_LINE_SEPARATOR + "    }"
      + GSON_LINE_SEPARATOR + "  ]" + GSON_LINE_SEPARATOR + "}";

  public AndroidJsonFactoryTest(String name) {
    super(name);
  }

  @Override
  protected JsonFactory newFactory() {
    return new AndroidJsonFactory();
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

}
