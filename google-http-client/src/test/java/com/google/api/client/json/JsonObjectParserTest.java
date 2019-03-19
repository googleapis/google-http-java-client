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

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.google.common.base.Charsets;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.lang.reflect.Type;
import java.nio.charset.Charset;
import junit.framework.TestCase;

/**
 * Tests for the {@link JsonObjectParser} class.
 *
 * @author Matthias Linder (mlinder)
 * @since 1.10
 */
public class JsonObjectParserTest extends TestCase {

  public void testConstructor_null() {
    try {
      new JsonObjectParser((JsonFactory) null);
      fail("Did not throw NullPointerException");
    } catch (NullPointerException expected) {
    }
  }

  public void testParse_InputStream() throws Exception {
    InputStream in = new ByteArrayInputStream(new byte[256]);
    Charset utf8 = Charsets.UTF_8;
    Type type = Integer[].class;
    Integer[] parsed = new Integer[1];

    JsonParser mockJsonParser = mock(JsonParser.class);
    when(mockJsonParser.parse(type, true)).thenReturn(parsed);

    JsonFactory mockJsonFactory = mock(JsonFactory.class);
    when(mockJsonFactory.createJsonParser(in, utf8)).thenReturn(mockJsonParser);

    // Test the JsonObjectParser
    JsonObjectParser jop = new JsonObjectParser(mockJsonFactory);
    assertEquals(parsed, jop.parseAndClose(in, utf8, type));
  }

  public void testParse_Reader() throws Exception {
    Reader in = new StringReader("something");
    Type type = Integer[].class;
    Integer[] parsed = new Integer[1];

    JsonParser mockJsonParser = mock(JsonParser.class);
    when(mockJsonParser.parse(type, true)).thenReturn(parsed);

    JsonFactory mockJsonFactory = mock(JsonFactory.class);
    when(mockJsonFactory.createJsonParser(in)).thenReturn(mockJsonParser);

    // Test the JsonObjectParser
    JsonObjectParser jop = new JsonObjectParser(mockJsonFactory);
    assertEquals(parsed, jop.parseAndClose(in, type));
  }
}
