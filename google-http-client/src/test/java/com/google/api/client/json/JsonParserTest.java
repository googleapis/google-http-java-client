/*
 * Copyright (c) 2013 Google Inc.
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.google.api.client.testing.json.MockJsonFactory;
import com.google.api.client.testing.json.MockJsonParser;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/**
 * Tests {@link JsonParser}.
 *
 * @author Yaniv Inbar
 */
@RunWith(JUnit4.class)
public class JsonParserTest {

  @Test
  public void testParseAndClose_noInput() throws Exception {
    MockJsonParser parser = (MockJsonParser) new MockJsonFactory().createJsonParser("");
    try {
      parser.parseAndClose(Object.class);
    } catch (IllegalArgumentException e) {
      assertEquals("no JSON input found", e.getMessage());
      assertTrue(parser.isClosed());
    }
  }

  @Test
  public void testParseAndClose_noInputVoid() throws Exception {
    MockJsonParser parser = (MockJsonParser) new MockJsonFactory().createJsonParser("");
    parser.parseAndClose(Void.class);
    assertTrue(parser.isClosed());
  }
}
