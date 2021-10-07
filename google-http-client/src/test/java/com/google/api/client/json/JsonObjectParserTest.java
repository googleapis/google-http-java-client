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

import com.google.api.client.testing.json.MockJsonFactory;
import com.google.api.client.testing.json.MockJsonParser;
import com.google.common.base.Charsets;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.lang.reflect.Type;
import java.nio.charset.Charset;
import junit.framework.TestCase;
import org.junit.Test;

/**
 * Tests for the {@link JsonObjectParser} class.
 *
 * @author Matthias Linder (mlinder)
 * @since 1.10
 */
public class JsonObjectParserTest extends TestCase {

  @Test
  public void testConstructor_null() {
    try {
      new JsonObjectParser((JsonFactory) null);
      fail("Did not throw NullPointerException");
    } catch (NullPointerException expected) {
    }
  }

  @Test
  public void testParse_InputStream() throws Exception {
    InputStream in = new ByteArrayInputStream(new byte[0]);
    Integer[] parsed = new Integer[1];

    // Test the JsonObjectParser
    JsonObjectParser jop = new JsonObjectParser(setUpMockJsonFactory(Integer[].class, parsed));
    assertEquals(parsed, jop.parseAndClose(in, Charsets.UTF_8, Integer[].class));
  }

  @Test
  public void testParse_Reader() throws Exception {
    Reader in = new StringReader("something");
    Integer[] parsed = new Integer[1];

    // Test the JsonObjectParser
    JsonObjectParser jop = new JsonObjectParser(setUpMockJsonFactory(Integer[].class, parsed));
    assertEquals(parsed, jop.parseAndClose(in, Integer[].class));
  }

  // Mockito.mock() on JsonFactory and JsonParser fails with Java 17, so set them up manually.
  private static final <T> JsonFactory setUpMockJsonFactory(
      final Class<T> clazz, final T parsedResult) {
    final MockJsonParser jsonParser =
        new MockJsonParser(null) {
          @Override
          public Object parse(Type dataType, boolean close) throws IOException {
            assertEquals(clazz, dataType);
            return parsedResult;
          }
        };

    return new MockJsonFactory() {
      @Override
      public JsonParser createJsonParser(Reader in) throws IOException {
        return jsonParser;
      }

      @Override
      public JsonParser createJsonParser(InputStream in, Charset charset) throws IOException {
        assertEquals(Charsets.UTF_8, charset);
        return jsonParser;
      }
    };
  }
}
