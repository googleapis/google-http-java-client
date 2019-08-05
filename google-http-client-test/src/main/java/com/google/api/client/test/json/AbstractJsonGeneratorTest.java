/*
 * Copyright (c) 2018 Google Inc.
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

package com.google.api.client.test.json;

import com.google.api.client.json.JsonGenerator;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import junit.framework.TestCase;

public abstract class AbstractJsonGeneratorTest extends TestCase {

  protected abstract JsonGenerator newGenerator(Writer writer) throws IOException;

  class IterableMap extends HashMap<String, String> implements Iterable<Map.Entry<String, String>> {
    @Override
    public Iterator<Map.Entry<String, String>> iterator() {
      return entrySet().iterator();
    }
  }

  public void testSerialize_simpleMap() throws Exception {
    StringWriter writer = new StringWriter();
    JsonGenerator generator = newGenerator(writer);

    Map<String, String> map = new HashMap<String, String>();
    map.put("a", "b");

    generator.serialize(map);
    generator.close();
    assertEquals("{\"a\":\"b\"}", writer.toString());
  }

  public void testSerialize_iterableMap() throws Exception {
    StringWriter writer = new StringWriter();
    JsonGenerator generator = newGenerator(writer);

    Map<String, String> map = new IterableMap();
    map.put("a", "b");

    generator.serialize(map);
    generator.close();
    assertEquals("{\"a\":\"b\"}", writer.toString());
  }
}
