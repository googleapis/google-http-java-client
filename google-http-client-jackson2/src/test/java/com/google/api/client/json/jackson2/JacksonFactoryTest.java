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

package com.google.api.client.json.jackson2;

import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.JsonParser;
import com.google.api.client.test.json.AbstractJsonFactoryTest;
import com.google.api.client.util.StringUtils;
import com.google.common.base.Charsets;
import java.io.ByteArrayInputStream;
import java.util.ArrayList;

/**
 * Tests {@link JacksonFactory}.
 *
 * @author Yaniv Inbar
 */
public class JacksonFactoryTest extends AbstractJsonFactoryTest {

  private static final String JSON_ENTRY_PRETTY =
      "{" + StringUtils.LINE_SEPARATOR + "  \"title\" : \"foo\"" + StringUtils.LINE_SEPARATOR + "}";
  private static final String JSON_FEED_PRETTY =
      "{"
          + StringUtils.LINE_SEPARATOR
          + "  \"entries\" : [ {"
          + StringUtils.LINE_SEPARATOR
          + "    \"title\" : \"foo\""
          + StringUtils.LINE_SEPARATOR
          + "  }, {"
          + StringUtils.LINE_SEPARATOR
          + "    \"title\" : \"bar\""
          + StringUtils.LINE_SEPARATOR
          + "  } ]"
          + StringUtils.LINE_SEPARATOR
          + "}";

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

  public final void testParse_directValue() throws Exception {
    byte[] jsonData = Charsets.UTF_8.encode("123").array();
    JsonParser jp =
        newFactory().createJsonParser(new ByteArrayInputStream(jsonData), Charsets.UTF_8);
    assertEquals(123, jp.parse(Integer.class, true));
  }
}
