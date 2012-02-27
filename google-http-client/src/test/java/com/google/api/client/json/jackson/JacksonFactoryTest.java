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

package com.google.api.client.json.jackson;

import com.google.api.client.json.JsonFactory;
import com.google.api.client.testing.json.AbstractJsonFactoryTest;
import com.google.api.client.util.Strings;

import java.util.ArrayList;

/**
 * Tests {@link JacksonFactory}.
 *
 * @author Yaniv Inbar
 */
public class JacksonFactoryTest extends AbstractJsonFactoryTest {

  private static final String JSON_ENTRY_PRETTY =
      "{" + Strings.LINE_SEPARATOR + "  \"title\" : \"foo\"" + Strings.LINE_SEPARATOR + "}";
  private static final String JSON_FEED_PRETTY = "{" + Strings.LINE_SEPARATOR
      + "  \"entries\" : [ {" + Strings.LINE_SEPARATOR + "    \"title\" : \"foo\""
      + Strings.LINE_SEPARATOR + "  }, {" + Strings.LINE_SEPARATOR + "    \"title\" : \"bar\""
      + Strings.LINE_SEPARATOR + "  } ]" + Strings.LINE_SEPARATOR + "}";

  public JacksonFactoryTest(String name) {
    super(name);
  }

  @Override
  protected JsonFactory newFactory() {
    return new JacksonFactory();
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
