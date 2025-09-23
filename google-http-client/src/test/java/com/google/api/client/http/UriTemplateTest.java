/*
 * Copyright (c) 2011 Google Inc.
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
import static org.junit.Assert.assertTrue;

import com.google.api.client.util.Value;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/**
 * Tests {@link UriTemplate}.
 *
 * @author Ravi Mistry
 */
@RunWith(JUnit4.class)
public class UriTemplateTest {

  public void testExpandTemplates_initialization() {
    SortedMap<String, Object> map = Maps.newTreeMap();
    map.put("id", Arrays.asList("a", "b", "c"));

    // Make sure the UriTemplate.COMPOSITE_PREFIXES map is initialized correctly.
    assertEquals("/a/b/c", UriTemplate.expand("{/id*}", map, false));
    assertEquals("/a/b/c", UriTemplate.expand("{/id*}", map, false));
  }

  @Test
  public void testExpandTemplates_basic() {
    SortedMap<String, Object> requestMap = Maps.newTreeMap();
    requestMap.put("abc", "xyz");
    requestMap.put("def", "123");
    requestMap.put("unused", "unused parameter");

    // Assert with addUnusedParamsAsQueryParams = false.
    assertEquals("foo/xyz/bar/123", UriTemplate.expand("foo/{abc}/bar/{def}", requestMap, false));
    // Assert with addUnusedParamsAsQueryParams = true.
    assertEquals(
        "foo/xyz/bar/123?unused=unused%20parameter",
        UriTemplate.expand("foo/{abc}/bar/{def}", requestMap, true));
    // Assert the map has not changed.
    assertEquals(3, requestMap.size());
    assertTrue(requestMap.containsKey("abc"));
    assertTrue(requestMap.containsKey("def"));
    assertTrue(requestMap.containsKey("unused"));
  }

  @Test
  public void testExpandTemplates_basicEncodeValue() {
    SortedMap<String, Object> requestMap = Maps.newTreeMap();
    requestMap.put("abc", "xyz;def");
    assertEquals(";abc=xyz%3Bdef", UriTemplate.expand("{;abc}", requestMap, false));
    assertEquals("xyz;def", UriTemplate.expand("{+abc}", requestMap, false));
  }

  @Test
  public void testExpandTemplates_encodeSpace() {
    SortedMap<String, Object> requestMap = Maps.newTreeMap();
    requestMap.put("abc", "xyz def");
    assertEquals(";abc=xyz%20def", UriTemplate.expand("{;abc}", requestMap, false));
  }

  @Test
  public void testExpandTemplates_noExpansionsWithQueryParams() {
    SortedMap<String, Object> requestMap = Maps.newTreeMap();
    requestMap.put("abc", "xyz");
    requestMap.put("def", "123");
    assertEquals(
        "foo/xyz/bar/123?abc=xyz&def=123", UriTemplate.expand("foo/xyz/bar/123", requestMap, true));
  }

  @Test
  public void testExpandTemplates_noExpansionsWithoutQueryParams() {
    SortedMap<String, Object> requestMap = Maps.newTreeMap();
    requestMap.put("abc", "xyz");
    requestMap.put("def", "123");
    assertEquals("foo/xyz/bar/123", UriTemplate.expand("foo/xyz/bar/123", requestMap, false));
  }

  @Test
  public void testExpandTemplates_missingParameter() {
    SortedMap<String, Object> requestMap = Maps.newTreeMap();
    requestMap.put("abc", "xyz");
    assertEquals("foo/xyz/bar/", UriTemplate.expand("foo/{abc}/bar/{def}", requestMap, true));
  }

  @Test
  public void testExpandTemplates_nullValue() {
    SortedMap<String, Object> requestMap = Maps.newTreeMap();
    requestMap.put("abc", "xyz");
    requestMap.put("def", null);
    assertEquals("foo/xyz/bar/", UriTemplate.expand("foo/{abc}/bar/{def}", requestMap, true));
  }

  @Test
  public void testExpandTemplates_emptyAndNullRequestMap() {
    SortedMap<String, Object> requestMap = Maps.newTreeMap();
    assertEquals("foo//bar/", UriTemplate.expand("foo/{abc}/bar/{def}", requestMap, true));
    assertEquals("foo//bar/", UriTemplate.expand("foo/{abc}/bar/{def}", null, true));
  }

  private Iterable<String> getListIterable() {
    return Arrays.asList("red", "green", "blue");
  }

  // template, expected output.
  private static final String[][] LIST_TESTS = {
    {"{d}", "red,green,blue"},
    {"{d*}", "red,green,blue"},
    {"{+d}", "red,green,blue"},
    {"{+d*}", "red,green,blue"},
    {"{#d}", "#red,green,blue"},
    {"{#d*}", "#red,green,blue"},
    {"X{.d}", "X.red,green,blue"},
    {"X{.d*}", "X.red.green.blue"},
    {"{/d}", "/red,green,blue"},
    {"{/d*}", "/red/green/blue"},
    {"{;d}", ";d=red,green,blue"},
    {"{;d*}", ";d=red;d=green;d=blue"},
    {"{?d}", "?d=red,green,blue"},
    {"{?d*}", "?d=red&d=green&d=blue"},
    {"{&d}", "&d=red,green,blue"},
    {"{&d*}", "&d=red&d=green&d=blue"},
  };

  @Test
  public void testExpandTemplates_explodeIterator() {
    for (String[] test : LIST_TESTS) {
      SortedMap<String, Object> requestMap = Maps.newTreeMap();
      requestMap.put("d", getListIterable().iterator());
      assertEquals(test[1], UriTemplate.expand(test[0], requestMap, true));
    }
  }

  @Test
  public void testExpandTemplates_explodeIterable() {
    for (String[] test : LIST_TESTS) {
      SortedMap<String, Object> requestMap = Maps.newTreeMap();
      requestMap.put("d", getListIterable());
      assertEquals(test[1], UriTemplate.expand(test[0], requestMap, true));
    }
  }

  enum testEnum {
    @Value
    ONE
  }

  @Test
  public void testExpandTemplates_explodeEnum() {
    SortedMap<String, Object> requestMap = Maps.newTreeMap();
    requestMap.put("d", testEnum.ONE);
    assertEquals(testEnum.ONE.toString(), UriTemplate.expand("{d}", requestMap, true));
  }

  @Test
  public void testExpandTemplates_missingCompositeParameter() {
    SortedMap<String, Object> requestMap = Maps.newTreeMap();
    requestMap.put("abc", "xyz");
    assertEquals("", UriTemplate.expand("{d}", requestMap, false));
    assertEquals("?abc=xyz", UriTemplate.expand("{d}", requestMap, true));
  }

  @Test
  public void testExpandTemplates_emptyListParameter() {
    SortedMap<String, Object> requestMap = Maps.newTreeMap();
    requestMap.put("d", Lists.newArrayList());
    assertEquals("", UriTemplate.expand("{d}", requestMap, true));
  }

  private Map<String, String> getMapParams() {
    Map<String, String> params = Maps.newLinkedHashMap();
    params.put("semi", ";");
    params.put("dot", ".");
    params.put("comma", ",");
    return params;
  }

  // template, expected output.
  private static final String[][] MAP_TESTS = {
    {"{d}", "semi,%3B,dot,.,comma,%2C"},
    {"{d*}", "semi=%3B,dot=.,comma=%2C"},
    {"{+d}", "semi,;,dot,.,comma,,"},
    {"{+d*}", "semi=;,dot=.,comma=,"},
    {"{#d}", "#semi,;,dot,.,comma,,"},
    {"{#d*}", "#semi=;,dot=.,comma=,"},
    {"X{.d}", "X.semi,%3B,dot,.,comma,%2C"},
    {"X{.d*}", "X.semi=%3B.dot=..comma=%2C"},
    {"{/d}", "/semi,%3B,dot,.,comma,%2C"},
    {"{/d*}", "/semi=%3B/dot=./comma=%2C"},
    {"{;d}", ";d=semi,%3B,dot,.,comma,%2C"},
    {"{;d*}", ";semi=%3B;dot=.;comma=%2C"},
    {"{?d}", "?d=semi,%3B,dot,.,comma,%2C"},
    {"{?d*}", "?semi=%3B&dot=.&comma=%2C"},
    {"{&d}", "&d=semi,%3B,dot,.,comma,%2C"},
    {"{&d*}", "&semi=%3B&dot=.&comma=%2C"},
  };

  @Test
  public void testExpandTemplates_explodeMap() {
    for (String[] test : MAP_TESTS) {
      SortedMap<String, Object> requestMap = Maps.newTreeMap();
      requestMap.put("d", getMapParams());
      assertEquals(test[1], UriTemplate.expand(test[0], requestMap, true));
    }
  }

  @Test
  public void testExpandTemplates_emptyMapParameter() {
    SortedMap<String, Object> requestMap = Maps.newTreeMap();
    requestMap.put("d", Maps.newTreeMap());
    assertEquals("", UriTemplate.expand("{d}", requestMap, true));
  }

  @Test
  public void testExpandTemplates_unusedQueryParametersEncoding() {
    Map<String, Object> requestMap = Maps.newLinkedHashMap();
    // Add unused params.
    requestMap.put("unused1", "abc!1234?");
    requestMap.put("unused2", "56$7 8");
    requestMap.put("unused3", "9=&/:@.");
    assertEquals(
        "?unused1=abc!1234?&unused2=56$7%208&unused3=9%3D%26/:@.",
        UriTemplate.expand("", requestMap, true));
  }

  @Test
  public void testExpandTemplates_unusedListQueryParameters() {
    Map<String, Object> requestMap = Maps.newLinkedHashMap();
    // Add unused params.
    List<String> params = Lists.newArrayList();
    params.add("value1");
    params.add("value2");
    requestMap.put("unused1", params);
    requestMap.put("unused2", "56$7 8");
    requestMap.put("unused3", "9=&/:@.");
    assertEquals(
        "?unused1=value1&unused1=value2&unused2=56$7%208&unused3=9%3D%26/:@.",
        UriTemplate.expand("", requestMap, true));
  }

  @Test
  public void testExpandTemplates_mixedBagParameters() {
    Map<String, Object> requestMap = Maps.newLinkedHashMap();
    // Add list params.
    requestMap.put("iterator", getListIterable().iterator());
    requestMap.put("iterable", getListIterable());
    // Add map params.
    requestMap.put("map", getMapParams());
    // Add enum param.
    requestMap.put("enum", testEnum.ONE);
    // Add normal params.
    requestMap.put("abc", "xyz");
    // Add unused params.
    requestMap.put("unused1", "unused param");
    requestMap.put("unused2", "unused=param");
    assertEquals(
        "foo/xyz/red/green/blue&iterable=red&iterable=green&iterable=blue&map=semi,%3B,dot,.,comma"
            + ",%2C&enum=ONE?unused1=unused%20param&unused2=unused%3Dparam",
        UriTemplate.expand("foo/{abc}{/iterator*}{&iterable*}{&map}{&enum}", requestMap, true));
    // Assert the map has not changed.
    assertEquals(7, requestMap.size());
    assertTrue(requestMap.containsKey("iterator"));
    assertTrue(requestMap.containsKey("iterable"));
    assertTrue(requestMap.containsKey("map"));
    assertTrue(requestMap.containsKey("abc"));
    assertTrue(requestMap.containsKey("unused1"));
    assertTrue(requestMap.containsKey("unused2"));
  }

  @Test
  public void testExpandTemplates_withBaseUrl() {
    SortedMap<String, Object> requestMap = Maps.newTreeMap();
    requestMap.put("abc", "xyz");
    requestMap.put("def", "123");

    // Expand with URI template not starting with "/".
    assertEquals(
        "https://test/base/path/xyz/123/bar/",
        UriTemplate.expand("https://test/base/path/", "{abc}/{def}/bar/", requestMap, true));
    // Expand with URI template starting with "/".
    assertEquals(
        "https://test/xyz/123/bar/",
        UriTemplate.expand("https://test/base/path/", "/{abc}/{def}/bar/", requestMap, true));
    // Expand with URI template as a full URL.
    assertEquals(
        "http://test3/xyz/123/bar/",
        UriTemplate.expand(
            "https://test/base/path/", "http://test3/{abc}/{def}/bar/", requestMap, true));
  }

  @Test
  public void testExpandNonReservedNonComposite() {
    SortedMap<String, Object> requestMap = Maps.newTreeMap();
    requestMap.put("abc", "xyz");
    requestMap.put("def", "a/b?c");
    assertEquals(
        "foo/xyz/bar/a%2Fb%3Fc", UriTemplate.expand("foo/{abc}/bar/{def}", requestMap, false));
    assertEquals(
        "foo/xyz/bar/a/b?c", UriTemplate.expand("foo/{abc}/bar/{+def}", requestMap, false));
  }

  @Test
  public void testExpandSeveralTemplates() {
    SortedMap<String, Object> map = Maps.newTreeMap();
    map.put("id", "a");
    map.put("uid", "b");

    assertEquals("?id=a&uid=b", UriTemplate.expand("{?id,uid}", map, false));
  }

  @Test
  public void testExpandSeveralTemplatesUnusedParameterInMiddle() {
    SortedMap<String, Object> map = Maps.newTreeMap();
    map.put("id", "a");
    map.put("uid", "b");

    assertEquals("?id=a&uid=b", UriTemplate.expand("{?id,foo,bar,uid}", map, false));
  }

  @Test
  public void testExpandSeveralTemplatesFirstParameterUnused() {
    SortedMap<String, Object> map = Maps.newTreeMap();
    map.put("id", "a");
    map.put("uid", "b");

    assertEquals("?id=a&uid=b", UriTemplate.expand("{?foo,id,uid}", map, false));
  }

  @Test
  public void testExpandSeveralTemplatesNoParametersUsed() {
    SortedMap<String, Object> map = Maps.newTreeMap();
    assertEquals("", UriTemplate.expand("{?id,uid}", map, false));
  }

  @Test
  public void testExpandTemplates_reservedExpansion_mustNotEscapeReservedCharSet() {

    String reservedSet = ":/?#[]@!$&'()*+,;=";

    SortedMap<String, Object> requestMap = Maps.newTreeMap();
    requestMap.put("var", reservedSet);

    assertEquals(
        "Reserved expansion must not escape chars from reserved set according to rfc6570#section-3.2.3",
        reservedSet,
        UriTemplate.expand("{+var}", requestMap, false));
  }

  @Test
  public void testExpandTemplates_reservedExpansion_mustNotEscapeUnreservedCharSet() {

    String unReservedSet = "-._~";

    SortedMap<String, Object> requestMap = Maps.newTreeMap();
    requestMap.put("var", unReservedSet);

    assertEquals(
        "Reserved expansion must not escape chars from unreserved set according to rfc6570#section-3.2.3",
        unReservedSet,
        UriTemplate.expand("{+var}", requestMap, false));
  }

  @Test
  // These tests are from the uri-template test suite
  // https://github.com/uri-templates/uritemplate-test/blob/master/extended-tests.json
  public void testExpandTemplates_reservedExpansion_alreadyEncodedInput() {
    Map<String, Object> variables = Maps.newLinkedHashMap();
    variables.put("id", "admin%2F");
    assertEquals("admin%252F", UriTemplate.expand("{id}", variables, false));
    assertEquals("admin%2F", UriTemplate.expand("{+id}", variables, false));
    assertEquals("#admin%2F", UriTemplate.expand("{#id}", variables, false));
  }

  @Test
  // These tests are from the uri-template test suite
  // https://github.com/uri-templates/uritemplate-test/blob/master/extended-tests.json
  public void testExpandTemplates_reservedExpansion_notEncodedInput() {
    Map<String, Object> variables = Maps.newLinkedHashMap();
    variables.put("not_pct", "%foo");
    assertEquals("%25foo", UriTemplate.expand("{not_pct}", variables, false));
    assertEquals("%25foo", UriTemplate.expand("{+not_pct}", variables, false));
    assertEquals("#%25foo", UriTemplate.expand("{#not_pct}", variables, false));
  }

  @Test
  // These tests are from the uri-template test suite
  // https://github.com/uri-templates/uritemplate-test/blob/master/extended-tests.json
  public void testExpandTemplates_reservedExpansion_listExpansionWithMixedEncodedInput() {
    Map<String, Object> variables = Maps.newLinkedHashMap();
    variables.put("list", Arrays.asList("red%25", "%2Fgreen", "blue "));
    assertEquals("red%2525,%252Fgreen,blue%20", UriTemplate.expand("{list}", variables, false));
    assertEquals("red%25,%2Fgreen,blue%20", UriTemplate.expand("{+list}", variables, false));
    assertEquals("#red%25,%2Fgreen,blue%20", UriTemplate.expand("{#list}", variables, false));
  }

  @Test
  // These tests are from the uri-template test suite
  // https://github.com/uri-templates/uritemplate-test/blob/master/extended-tests.json with an
  // additional map entry
  public void testExpandTemplates_reservedExpansion_mapWithMixedEncodedInput() {
    Map<String, Object> variables = Maps.newLinkedHashMap();
    Map<String, String> keys = Maps.newLinkedHashMap();
    keys.put("key1", "val1%2F");
    keys.put("key2", "val2%2F");
    keys.put("key3", "val ");
    variables.put("keys", keys);
    assertEquals(
        "key1,val1%252F,key2,val2%252F,key3,val%20",
        UriTemplate.expand("{keys}", variables, false));
    assertEquals(
        "key1,val1%2F,key2,val2%2F,key3,val%20", UriTemplate.expand("{+keys}", variables, false));
    assertEquals(
        "#key1,val1%2F,key2,val2%2F,key3,val%20", UriTemplate.expand("{#keys}", variables, false));
  }
}
