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

package com.google.api.client.http;

import com.google.common.base.Charsets;
import com.google.common.testing.EqualsTester;
import junit.framework.TestCase;

/**
 * Tests for the {@link HttpMediaType} class.
 *
 * @author Matthias Linder (mlinder)
 * @since 1.10
 */
public class HttpMediaTypeTest extends TestCase {

  public void testBuild() {
    HttpMediaType m = new HttpMediaType("main", "sub");
    assertEquals("main/sub", m.build());
  }

  public void testBuild_star() {
    HttpMediaType m = new HttpMediaType("*", "*");
    assertEquals("*/*", m.build());
  }

  public void testBuild_parameters() {
    HttpMediaType m = new HttpMediaType("main", "sub");
    m.setParameter("bbb", ";/ ");
    m.setParameter("aaa", "1");
    assertEquals("main/sub; aaa=1; bbb=\";/ \"", m.build());
  }

  public void testBuild_json() {
    HttpMediaType m = new HttpMediaType("application/json").setCharsetParameter(Charsets.UTF_8);
    assertEquals("application/json; charset=UTF-8", m.build());
  }

  public void testBuild_multipartSpec() {
    HttpMediaType m = new HttpMediaType("main", "sub");
    m.setParameter("bbb", "foo=/bar");
    assertEquals("main/sub; bbb=\"foo=/bar\"", m.build());
  }

  public void testBuild_parametersCasing() {
    HttpMediaType m = new HttpMediaType("main", "sub");
    m.setParameter("foo", "FooBar");
    assertEquals("main/sub; foo=FooBar", m.build());
  }

  public void testFromString() {
    HttpMediaType m = new HttpMediaType("main/sub");
    assertEquals("main", m.getType());
    assertEquals("sub", m.getSubType());
  }

  public void testFromString_star() {
    HttpMediaType m = new HttpMediaType("text/*");
    assertEquals("text", m.getType());
    assertEquals("*", m.getSubType());
  }

  public void testFromString_null() {
    try {
      new HttpMediaType(null);
      fail("Method did not NullPointerException");
    } catch (NullPointerException expected) {
    }
  }

  public void testFromString_multipartSpec() {
    // Values allowed by the spec: http://www.w3.org/Protocols/rfc1341/7_2_Multipart.html
    String value = "f00'()+_,-./:=?bar";
    HttpMediaType m = new HttpMediaType("text/plain; boundary=" + value + "; foo=bar");
    assertEquals(value, m.getParameter("boundary"));
    assertEquals("bar", m.getParameter("foo"));
  }

  public void testFromString_full() {
    HttpMediaType m = new HttpMediaType("text/plain; charset=utf-8; foo=\"foo; =bar\"");
    assertEquals("text", m.getType());
    assertEquals("plain", m.getSubType());
    assertEquals("utf-8", m.getParameter("charset"));
    assertEquals("foo; =bar", m.getParameter("foo"));
    assertEquals(2, m.getParameters().size());
  }

  public void testFromString_case() {
    HttpMediaType m = new HttpMediaType("text/plain; Foo=Bar");
    assertEquals("Bar", m.getParameter("fOO"));
  }

  public void testSetMainType() {
    assertEquals("foo", new HttpMediaType("text", "plain").setType("foo").getType());
  }

  public void testSetMainType_invalid() {
    try {
      new HttpMediaType("text", "plain").setType("foo/bar");
      fail("Method did not throw IllegalArgumentException");
    } catch (IllegalArgumentException expected) {
    }
  }

  public void testSetSubType() {
    assertEquals("foo", new HttpMediaType("text", "plain").setSubType("foo").getSubType());
  }

  public void testSetSubType_invalid() {
    try {
      new HttpMediaType("text", "plain").setSubType("foo/bar");
      fail("Method did not throw IllegalArgumentException");
    } catch (IllegalArgumentException expected) {
    }
  }

  public void testSetParameter_casing() {
    HttpMediaType mt = new HttpMediaType("text", "plain");
    mt.setParameter("Foo", "Bar");
    assertEquals("Bar", mt.getParameter("FOO"));
  }

  private boolean containsInvalidChar(String str) {
    try {
      new HttpMediaType("text", "plain").setSubType(str);
      return false;
    } catch (IllegalArgumentException expected) {
      return true;
    }
  }

  private void assertFullSerialization(String str) {
    assertEquals(str, new HttpMediaType(str).build());
  }

  public void testFullSerialization() {
    assertFullSerialization("text/plain");
    assertFullSerialization("text/plain; foo=bar");
    assertFullSerialization("text/plain; bar=bar; foo=foo");
    assertFullSerialization("text/plain; bar=\"Bar Bar\"; foo=Foo");
    assertFullSerialization("text/*");
    assertFullSerialization("*/*");
    assertFullSerialization("text/*; charset=utf-8; foo=\"bar bar bar\"");
  }

  public void testInvalidCharsRegex() {
    assertEquals(false, containsInvalidChar("foo"));
    assertEquals(false, containsInvalidChar("X-Foo-Bar"));
    assertEquals(true, containsInvalidChar("foo/bar"));
    assertEquals(true, containsInvalidChar("  foo"));
    assertEquals(true, containsInvalidChar("foo;bar"));
  }

  public void testCharset() {
    HttpMediaType hmt = new HttpMediaType("foo/bar");
    assertEquals(null, hmt.getCharsetParameter());
    hmt.setCharsetParameter(Charsets.UTF_8);
    assertEquals(Charsets.UTF_8.name(), hmt.getParameter("charset"));
    assertEquals(Charsets.UTF_8, hmt.getCharsetParameter());
  }

  public void testEqualsIgnoreParameters() {
    assertEquals(
        true, new HttpMediaType("foo/bar").equalsIgnoreParameters(new HttpMediaType("Foo/bar")));
    assertEquals(
        true,
        new HttpMediaType("foo/bar")
            .equalsIgnoreParameters(new HttpMediaType("foo/bar; charset=utf-8")));
    assertEquals(
        false, new HttpMediaType("foo/bar").equalsIgnoreParameters(new HttpMediaType("bar/foo")));
    assertEquals(false, new HttpMediaType("foo/bar").equalsIgnoreParameters(null));
  }

  public void testEqualsIgnoreParameters_static() {
    assertEquals(true, HttpMediaType.equalsIgnoreParameters(null, null));
    assertEquals(false, HttpMediaType.equalsIgnoreParameters(null, "foo/bar"));
    assertEquals(true, HttpMediaType.equalsIgnoreParameters("foo/bar", "foo/bar"));
    assertEquals(true, HttpMediaType.equalsIgnoreParameters("foo/bar; a=c", "foo/bar; b=d"));
  }

  public void testEquals() {
    new EqualsTester()
        .addEqualityGroup(new HttpMediaType("foo/bar"), new HttpMediaType("foo/bar"))
        .addEqualityGroup(new HttpMediaType("foo/bar; a=c"), new HttpMediaType("foo/bar; a=c"))
        .addEqualityGroup(new HttpMediaType("foo/bar; bar=bar"))
        .testEquals();
  }
}
