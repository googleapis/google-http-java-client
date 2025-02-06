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

package com.google.api.client.http;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import com.google.api.client.util.ArrayMap;
import com.google.api.client.util.GenericData;
import com.google.api.client.util.Key;
import com.google.api.client.util.Objects;
import com.google.api.client.util.Value;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/**
 * Tests {@link UrlEncodedParser}.
 *
 * @author Yaniv Inbar
 */
@RunWith(JUnit4.class)
public class UrlEncodedParserTest {

  public static class Simple {

    @Key Void v;

    @Key String a;

    @Key String b;

    @Key String c;

    @Key List<String> q;

    @Key String[] r;

    @Key Object o;

    @Override
    public boolean equals(Object obj) {
      Simple other = (Simple) obj;
      return Objects.equal(a, other.a)
          && Objects.equal(b, other.b)
          && Objects.equal(c, other.c)
          && Objects.equal(q, other.q)
          && Arrays.equals(r, other.r)
          && Objects.equal(o, other.o);
    }

    public Simple() {}

    @Override
    public String toString() {
      return Objects.toStringHelper(this)
          .add("a", a)
          .add("b", b)
          .add("c", c)
          .add("q", q)
          .add("r", Arrays.asList(r))
          .add("o", o)
          .toString();
    }
  }

  public static class Generic extends GenericData {
    @Key String a;

    @Key String b;

    @Key String c;

    @Key List<String> q;

    @Key Object o;

    @Override
    public Generic set(String fieldName, Object value) {
      return (Generic) super.set(fieldName, value);
    }
  }

  @Test
  public void testParse_simple() {
    Simple actual = new Simple();
    UrlEncodedParser.parse(
        "v=ignore&v=ignore2&q=1&a=x=&b=y&c=z&q=2&undeclared=0&o=object&r=a1&r=a2", actual);
    Simple expected = new Simple();
    expected.a = "x=";
    expected.b = "y";
    expected.c = "z";
    expected.q = new ArrayList<String>(Arrays.asList("1", "2"));
    expected.r = new String[] {"a1", "a2"};
    expected.o = new ArrayList<String>(Arrays.asList("object"));
    assertEquals(expected, actual);
    assertNull(expected.v);
  }

  @Test
  public void testParse_generic() {
    Generic actual = new Generic();
    UrlEncodedParser.parse("p=4&q=1&a=x&p=3&b=y&c=z&d=v&q=2&p=5&o=object", actual);
    Generic expected = new Generic();
    expected.a = "x";
    expected.b = "y";
    expected.c = "z";
    expected.q = new ArrayList<String>(Arrays.asList("1", "2"));
    expected.o = new ArrayList<String>(Arrays.asList("object"));
    expected.set("d", Collections.singletonList("v")).set("p", Arrays.asList("4", "3", "5"));
    assertEquals(expected, actual);
    assertEquals(ArrayList.class, actual.get("d").getClass());
  }

  @Test
  public void testParse_map() {
    ArrayMap<String, Object> actual = new ArrayMap<String, Object>();
    UrlEncodedParser.parse("p=4&q=1&a=x&p=3&b=y&c=z&d=v&q=2&p=5&noval1&noval2=", actual);
    ArrayMap<String, Object> expected = ArrayMap.create();
    expected.add("p", Arrays.asList("4", "3", "5"));
    expected.add("q", Arrays.asList("1", "2"));
    expected.add("a", Collections.singletonList("x"));
    expected.add("b", Collections.singletonList("y"));
    expected.add("c", Collections.singletonList("z"));
    expected.add("d", Collections.singletonList("v"));
    expected.add("noval1", Collections.singletonList(""));
    expected.add("noval2", Collections.singletonList(""));
    assertEquals(expected, actual);
    assertEquals(ArrayList.class, actual.get("a").getClass());
  }

  @Test
  public void testParse_encoding() {
    ArrayMap<String, Object> actual = new ArrayMap<String, Object>();
    UrlEncodedParser.parse("q=%20", actual);
    ArrayMap<String, Object> expected = ArrayMap.create();
    expected.add("q", Collections.singletonList(" "));
    assertEquals(expected, actual);
  }

  @Test
  public void testParse_null() {
    ArrayMap<String, Object> actual = new ArrayMap<String, Object>();
    UrlEncodedParser.parse((String) null, actual);
    assertTrue(actual.isEmpty());
  }

  public enum E {
    @Value
    VALUE,
    @Value("other")
    OTHER_VALUE,
  }

  public static class EnumValue extends GenericData {
    @Key public E value;
    @Key public E otherValue;

    @Override
    public EnumValue set(String fieldName, Object value) {
      return (EnumValue) super.set(fieldName, value);
    }
  }

  static final String ENUM_VALUE = "otherValue=other&value=VALUE";

  @Test
  public void testParse_enum() throws IOException {
    EnumValue actual = new EnumValue();
    UrlEncodedParser.parse(ENUM_VALUE, actual);
    EnumValue expected = new EnumValue();
    expected.value = E.VALUE;
    expected.otherValue = E.OTHER_VALUE;
    assertEquals(expected, actual);
    subtestWriteTo(ENUM_VALUE, actual);
  }

  private void subtestWriteTo(String expected, Object data) throws IOException {
    UrlEncodedContent content = new UrlEncodedContent(data);
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    content.writeTo(out);
    assertEquals(expected, out.toString());
  }
}
