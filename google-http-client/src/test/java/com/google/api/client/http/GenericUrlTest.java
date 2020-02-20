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

import com.google.api.client.util.Key;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import junit.framework.TestCase;
import org.junit.Assert;

/**
 * Tests {@link GenericUrl}.
 *
 * @author Yaniv Inbar
 */
public class GenericUrlTest extends TestCase {

  public GenericUrlTest() {}

  public GenericUrlTest(String name) {
    super(name);
  }

  private static final String MINIMAL = "http://bar";

  public void testBuild_minimal() {
    GenericUrl url = new GenericUrl();
    url.setScheme("http");
    url.setHost("bar");
    assertEquals(MINIMAL, url.build());
  }

  public void testParse_minimal() {
    GenericUrl url = new GenericUrl(MINIMAL);
    assertEquals("http", url.getScheme());
  }

  private static final String NO_PATH = "http://bar?a=b";

  public void testBuild_noPath() {
    GenericUrl url = new GenericUrl();
    url.setScheme("http");
    url.setHost("bar");
    url.set("a", "b");
    assertEquals(NO_PATH, url.build());
  }

  public void testBuild_noUserInfo() {
    GenericUrl url = new GenericUrl(NO_PATH);
    assertNull(url.getUserInfo());
  }

  public void testBuild_noScheme() {
    GenericUrl url = new GenericUrl();
    try {
      url.build();
      fail("expected " + NullPointerException.class);
    } catch (NullPointerException e) {
      // expected
    }
  }

  public void testBuild_noHost() {
    GenericUrl url = new GenericUrl();
    try {
      url.setScheme("http");
      url.build();
      fail("expected " + NullPointerException.class);
    } catch (NullPointerException e) {
      // expected
    }
  }

  public void testParse_noPath() {
    GenericUrl url = new GenericUrl(NO_PATH);
    assertEquals("http", url.getScheme());
    assertEquals("bar", url.getHost());
    assertEquals("b", url.getFirst("a"));
    assertNull(url.getPathParts());
  }

  private static final String SHORT_PATH = "http://bar/path?a=b";

  private static final List<String> SHORT_PATH_PARTS = Arrays.asList("", "path");

  public void testBuild_shortPath() {
    GenericUrl url = new GenericUrl();
    url.setScheme("http");
    url.setHost("bar");
    url.setPathParts(SHORT_PATH_PARTS);
    url.set("a", "b");
    assertEquals(SHORT_PATH, url.build());
  }

  public void testParse_shortPath() {
    GenericUrl url = new GenericUrl(SHORT_PATH);
    assertEquals("http", url.getScheme());
    assertEquals("bar", url.getHost());
    assertEquals(SHORT_PATH_PARTS, url.getPathParts());
    assertEquals("b", url.getFirst("a"));
  }

  private static final String LONG_PATH = "http://bar/path/to/resource?a=b";

  private static final List<String> LONG_PATH_PARTS = Arrays.asList("", "path", "to", "resource");

  public void testBuild_longPath() {
    GenericUrl url = new GenericUrl();
    url.setScheme("http");
    url.setHost("bar");
    url.setPathParts(LONG_PATH_PARTS);
    url.set("a", "b");
    assertEquals(LONG_PATH, url.build());
  }

  public void testParse_longPath() {
    GenericUrl url = new GenericUrl(LONG_PATH);
    assertEquals("http", url.getScheme());
    assertEquals("bar", url.getHost());
    assertEquals(LONG_PATH_PARTS, url.getPathParts());
    assertEquals("b", url.getFirst("a"));
  }

  public static class TestUrl extends GenericUrl {
    @Key String foo;

    public String hidden;

    public TestUrl() {}

    public TestUrl(String encodedUrl) {
      super(encodedUrl);
    }

    public TestUrl(String encodedUrl, boolean verbatim) {
      super(encodedUrl, verbatim);
    }
  }

  private static final String FULL =
      "https://user:%3Cpa&$w%40rd%3E@www.google.com:223/m8/feeds/contacts/"
          + "someone=%23%25&%20%3F%3Co%3E%7B%7D@gmail.com/"
          + "full?"
          + "foo=bar&"
          + "alt=json&"
          + "max-results=3&"
          + "prettyprint=true&"
          + "q=Go%3D%23/%25%26%20?%3Co%3Egle#%3CD@WNL:ADING%3E";

  private static final List<String> FULL_PARTS =
      Arrays.asList("", "m8", "feeds", "contacts", "someone=#%& ?<o>{}@gmail.com", "full");

  private static final String USER_INFO = "user:<pa&$w@rd>";
  private static final String FRAGMENT = "<D@WNL:ADING>";

  public void testBuild_full() {
    TestUrl url = new TestUrl();
    url.setScheme("https");
    url.setHost("www.google.com");
    url.setPort(223);
    url.setPathParts(FULL_PARTS);
    url.set("alt", "json")
        .set("max-results", 3)
        .set("prettyprint", true)
        .set("q", "Go=#/%& ?<o>gle");
    url.foo = "bar";
    url.hidden = "invisible";
    url.setFragment(FRAGMENT);
    url.setUserInfo(USER_INFO);
    assertEquals(FULL, url.build());
  }

  public void testParse_full() {
    TestUrl url = new TestUrl(FULL);
    subtestFull(url);
    assertNull(url.hidden);
    assertEquals("bar", url.get("foo"));
    assertEquals("bar", url.foo);
  }

  public void testParse_full_verbatim() {
    TestUrl url = new TestUrl(FULL, true);
    assertNull(url.hidden);
    assertEquals("Go%3D%23/%25%26%20?%3Co%3Egle", url.getFirst("q"));
  }

  public void testConstructor_url() throws MalformedURLException {
    GenericUrl url = new GenericUrl(new URL(FULL));
    subtestFull(url);
  }

  public void testConstructor_uri() throws URISyntaxException {
    GenericUrl url = new GenericUrl(new URI(FULL));
    subtestFull(url);
  }

  public void testConstructor_string() {
    GenericUrl url = new GenericUrl(FULL);
    subtestFull(url);
  }

  public void testConstructor_schemeToLowerCase() throws URISyntaxException, MalformedURLException {
    GenericUrl url = new GenericUrl("HTTps://www.google.com:223");
    assertEquals("https", url.getScheme());
    url = new GenericUrl(new URI("HTTPS://www.google.com:223"));
    assertEquals("https", url.getScheme());
    url = new GenericUrl(new URL("hTTPs://www.google.com:223"));
    assertEquals("https", url.getScheme());
  }

  private void subtestFull(GenericUrl url) {
    assertEquals("https", url.getScheme());
    assertEquals("www.google.com", url.getHost());
    assertEquals(223, url.getPort());
    assertEquals(FULL_PARTS, url.getPathParts());
    assertEquals("json", url.getFirst("alt"));
    assertEquals("3", url.getFirst("max-results"));
    assertEquals("true", url.getFirst("prettyprint"));
    assertEquals("Go=#/%& ?<o>gle", url.getFirst("q"));
    assertEquals("bar", url.getFirst("foo"));
    assertEquals(FRAGMENT, url.getFragment());
    assertEquals(USER_INFO, url.getUserInfo());
  }

  public static class FieldTypesUrl extends GenericUrl {

    @Key Boolean B;

    @Key Double D;

    @Key Integer I;

    @Key boolean b;

    @Key double d;

    @Key int i;

    @Key String s;

    String hidden;

    FieldTypesUrl() {}

    FieldTypesUrl(String encodedUrl) {
      super(encodedUrl);
    }

    @Override
    public FieldTypesUrl set(String fieldName, Object value) {
      return (FieldTypesUrl) super.set(fieldName, value);
    }
  }

  private static final String FIELD_TYPES =
      "http://bar?B=true&D=-3.14&I=-3&b=true&d=-3.14&i=-3&s=a&a=b";

  public void testBuild_fieldTypes() {
    FieldTypesUrl url = new FieldTypesUrl();
    url.setScheme("http");
    url.setHost("bar");
    url.set("a", "b");
    url.B = true;
    url.D = -3.14;
    url.I = -3;
    url.b = true;
    url.d = -3.14;
    url.i = -3;
    url.s = "a";
    url.hidden = "notHere";
    assertEquals(FIELD_TYPES, url.build());
  }

  public void testParse_fieldTypes() {
    FieldTypesUrl url = new FieldTypesUrl(FIELD_TYPES);
    assertEquals("http", url.getScheme());
    assertEquals("bar", url.getHost());
    assertEquals("b", url.getFirst("a"));
    assertNull(url.hidden);
    assertEquals(true, url.b);
    assertEquals(Boolean.TRUE, url.B);
    assertEquals(-3.14d, url.d, 1e-5d);
    assertEquals(-3.14d, url.D.doubleValue(), 1e-5d);
    assertEquals(-3, url.i);
    assertEquals(-3, url.I.intValue());
    assertEquals("a", url.s);
  }

  private static final String FRAGMENT1 =
      "http://bar/path/to/resource#fragme=%23/%25&%20?%3Co%3Ent";

  public void testBuild_fragment1() {
    GenericUrl url = new GenericUrl();
    url.setScheme("http");
    url.setHost("bar");
    url.setPathParts(LONG_PATH_PARTS);
    url.setFragment("fragme=#/%& ?<o>nt");
    assertEquals(FRAGMENT1, url.build());
  }

  public void testParse_fragment1() {
    GenericUrl url = new GenericUrl(FRAGMENT1);
    assertEquals("http", url.getScheme());
    assertEquals("bar", url.getHost());
    assertEquals(LONG_PATH_PARTS, url.getPathParts());
    assertEquals("fragme=#/%& ?<o>nt", url.getFragment());
  }

  private static final String FRAGMENT2 = "http://bar/path/to/resource?a=b#fragment";

  public void testBuild_fragment2() {
    GenericUrl url = new GenericUrl();
    url.setScheme("http");
    url.setHost("bar");
    url.setPathParts(LONG_PATH_PARTS);
    url.set("a", "b");
    url.setFragment("fragment");
    assertEquals(FRAGMENT2, url.build());
  }

  public void testParse_fragment2() {
    GenericUrl url = new GenericUrl(FRAGMENT2);
    assertEquals("http", url.getScheme());
    assertEquals("bar", url.getHost());
    assertEquals(LONG_PATH_PARTS, url.getPathParts());
    assertEquals("b", url.getFirst("a"));
    assertEquals("fragment", url.getFragment());
  }

  public void testBuildAuthority_exception() {
    // Test without a scheme.
    GenericUrl url = new GenericUrl();
    url.setHost("example.com");

    try {
      url.buildAuthority();
      Assert.fail("no exception was thrown");
    } catch (NullPointerException expected) {
    }

    // Test without a host.
    url = new GenericUrl();
    url.setScheme("https");

    try {
      url.buildAuthority();
      Assert.fail("no exception was thrown");
    } catch (NullPointerException expected) {
    }
  }

  public void testBuildAuthority_simple() {
    GenericUrl url = new GenericUrl();
    url.setScheme("http");
    url.setHost("example.com");
    assertEquals("http://example.com", url.buildAuthority());
  }

  public void testBuildAuthority_withPort() {
    GenericUrl url = new GenericUrl();
    url.setScheme("http");
    url.setHost("example.com");
    url.setPort(1234);
    assertEquals("http://example.com:1234", url.buildAuthority());
  }

  public void testBuildAuthority_withUserInfo() {
    GenericUrl url = new GenericUrl();
    url.setScheme("http");
    url.setHost("www.example.com");
    url.setUserInfo("first.last:pa@@");
    assertEquals("http://first.last:pa%40%40@www.example.com", url.buildAuthority());
  }

  public void testBuildRelativeUrl_empty() {
    GenericUrl url = new GenericUrl();
    url.setScheme("foo");
    url.setHost("bar");
    url.setRawPath("");
    assertEquals("", url.buildRelativeUrl());
  }

  public void testBuildRelativeUrl_simple() {
    GenericUrl url = new GenericUrl();
    url.setScheme("foo");
    url.setHost("bar");
    url.setRawPath("/example");
    assertEquals("/example", url.buildRelativeUrl());
  }

  public void testBuildRelativeUrl_simpleQuery() {
    GenericUrl url = new GenericUrl();
    url.setScheme("foo");
    url.setHost("bar");
    url.setRawPath("/example");
    url.put("key", "value");
    assertEquals("/example?key=value", url.buildRelativeUrl());
  }

  public void testBuildRelativeUrl_fragment() {
    GenericUrl url = new GenericUrl();
    url.setScheme("foo");
    url.setHost("bar");
    url.setRawPath("/example");
    url.setFragment("test");
    assertEquals("/example#test", url.buildRelativeUrl());
  }

  public void testBuildRelativeUrl_onlyQuery() {
    GenericUrl url = new GenericUrl();
    url.setScheme("foo");
    url.setHost("bar");
    url.setRawPath("");
    url.put("key", "value");
    assertEquals("?key=value", url.buildRelativeUrl());
  }

  private static final String BASE_URL = "http://google.com";
  private static final String FULL_PATH = "/some/path/someone%2Fis%2F@gmail.com/test/?one=1&two=2";

  public void testBuildRelativeUrl_full() {
    GenericUrl url = new GenericUrl(BASE_URL + FULL_PATH);
    assertEquals(FULL_PATH, url.buildRelativeUrl());
  }

  private static final String PATH_WITH_SLASH =
      "http://www.google.com/m8/feeds/contacts/someone%2Fis%2F@gmail.com/full/";

  private static final List<String> PATH_WITH_SLASH_PARTS =
      Arrays.asList("", "m8", "feeds", "contacts", "someone/is/@gmail.com", "full", "");

  public void testBuild_pathWithSlash() {
    GenericUrl url = new GenericUrl();
    url.setScheme("http");
    url.setHost("www.google.com");
    url.setPathParts(PATH_WITH_SLASH_PARTS);
    assertEquals(PATH_WITH_SLASH, url.build());
  }

  public void testConstructorUnderscore() {
    String url = "http://url_with_underscore.google.com";
    GenericUrl parsed = new GenericUrl(url);
    assertEquals("url_with_underscore.google.com", parsed.getHost());
  }

  public void testParse_pathWithSlash() {
    GenericUrl url = new GenericUrl(PATH_WITH_SLASH);
    assertEquals("http", url.getScheme());
    assertEquals("www.google.com", url.getHost());
    assertEquals(PATH_WITH_SLASH_PARTS, url.getPathParts());
  }

  public void testToPathParts() {
    subtestToPathParts(null, (String[]) null);
    subtestToPathParts(null, "");
    subtestToPathParts("/", "", "");
    subtestToPathParts("a", "a");
    subtestToPathParts("/a", "", "a");
    subtestToPathParts("/a/", "", "a", "");
    subtestToPathParts("path/to/resource", "path", "to", "resource");
    subtestToPathParts("/path/to/resource", "", "path", "to", "resource");
    subtestToPathParts("/path/to/resource/", "", "path", "to", "resource", "");
    subtestToPathParts("/Go%3D%23%2F%25%26%20?%3Co%3Egle/2nd", "", "Go=#/%& ?<o>gle", "2nd");
    subtestToPathParts("/plus+test/resource", "", "plus+test", "resource");
    subtestToPathParts("/plus%2Btest/resource", "", "plus+test", "resource");
  }

  private void subtestToPathParts(String encodedPath, String... expectedDecodedParts) {
    List<String> result = GenericUrl.toPathParts(encodedPath, false);
    if (encodedPath == null) {
      assertNull(result);
    } else {
      assertEquals(Arrays.asList(expectedDecodedParts), result);
    }
  }

  public void testAppendPath() {
    GenericUrl url = new GenericUrl("http://google.com");
    assertNull(url.getPathParts());
    url.appendRawPath(null);
    assertNull(url.getPathParts());
    url.appendRawPath("");
    assertNull(url.getPathParts());
    url.appendRawPath("/");
    assertEquals(Arrays.asList("", ""), url.getPathParts());
    url.appendRawPath("/");
    assertEquals(Arrays.asList("", "", ""), url.getPathParts());
    url.appendRawPath("/a");
    assertEquals(Arrays.asList("", "", "", "a"), url.getPathParts());
    url.appendRawPath("b");
    assertEquals(Arrays.asList("", "", "", "ab"), url.getPathParts());
    url.appendRawPath("c/d");
    assertEquals(Arrays.asList("", "", "", "abc", "d"), url.getPathParts());
    url.appendRawPath("/e");
    assertEquals(Arrays.asList("", "", "", "abc", "d", "e"), url.getPathParts());
    url.appendRawPath("/");
    assertEquals(Arrays.asList("", "", "", "abc", "d", "e", ""), url.getPathParts());
  }

  private static final String PREFIX = "https://www.googleapis.com";

  private static final String REPEATED_PARAM_PATH = "/latitude/v1/location";

  private static final List<String> REPEATED_PARAM_PATH_PARTS =
      Arrays.asList("", "latitude", "v1", "location");

  private static final String REPEATED_PARAM = PREFIX + REPEATED_PARAM_PATH + "?q=c&q=a&q=b&s=e";

  public void testRepeatedParam_build() {
    GenericUrl url = new GenericUrl();
    url.setScheme("https");
    url.setHost("www.googleapis.com");
    url.setPathParts(REPEATED_PARAM_PATH_PARTS);
    url.set("q", Arrays.asList("c", "a", "b"));
    url.set("s", "e");
    assertEquals(REPEATED_PARAM, url.build());
  }

  public void testRepeatedParam_parse() {
    GenericUrl url = new GenericUrl(REPEATED_PARAM);
    assertEquals("https", url.getScheme());
    assertEquals("www.googleapis.com", url.getHost());
    assertEquals(REPEATED_PARAM_PATH_PARTS, url.getPathParts());
    assertEquals("c", url.getFirst("q"));
    assertEquals("e", url.getFirst("s"));
    assertEquals(Arrays.asList("e"), url.get("s"));
    Collection<?> q = (Collection<?>) url.get("q");
    Iterator<?> i = q.iterator();
    assertEquals("c", i.next());
    assertEquals("a", i.next());
    assertEquals("b", i.next());
    assertFalse(i.hasNext());
    assertEquals(Arrays.asList("c", "a", "b"), new ArrayList<Object>(url.getAll("q")));
  }

  public void testBuild_noValue() {
    GenericUrl url = new GenericUrl();
    url.setScheme("https");
    url.setHost("www.googleapis.com");
    url.setRawPath(REPEATED_PARAM_PATH);
    url.set("noval", "");
    assertEquals(PREFIX + REPEATED_PARAM_PATH + "?noval", url.build());
  }

  public void testClone() {
    GenericUrl url = new GenericUrl("http://www.google.com");
    GenericUrl clone = url.clone();
    assertEquals("http://www.google.com", clone.build());
  }

  public void testToUrl_relative() {
    // relative redirect
    testRedirectUtility("http://www.google.com/test", "http://www.google.com", "/test");
    testRedirectUtility("http://www.google.com/test", "http://www.google.com/foo/bar/", "/test");

    testRedirectUtility("http://www.google.com/test", "http://www.google.com", "test");
    testRedirectUtility("http://www.google.com/test", "http://www.google.com/foo", "test");
    testRedirectUtility("http://www.google.com/foo/test", "http://www.google.com/foo/", "test");
    testRedirectUtility("http://www.google.com/foo/test", "http://www.google.com/foo/bar", "test");

    testRedirectUtility(
        "http://www.google.com/foo/test/", "http://www.google.com/foo/bar", "test/");
    testRedirectUtility(
        "http://www.google.com/foo/test/sub", "http://www.google.com/foo/bar", "test/sub");

    // absolute redirect
    testRedirectUtility(
        "https://example.com/test", "http://www.google.com/foo", "https://example.com/test");
    testRedirectUtility(
        "https://example.com/test", "http://www.google.com", "https://example.com/test");
    testRedirectUtility(
        "https://example.com/test", "http://www.google.com/", "https://example.com/test");
  }

  private void testRedirectUtility(String expectedResult, String url, String relative) {
    GenericUrl gu = new GenericUrl(url);
    GenericUrl redirectedUrl = new GenericUrl(gu.toURL(relative));
    assertEquals(expectedResult, redirectedUrl.toString());
  }
}
