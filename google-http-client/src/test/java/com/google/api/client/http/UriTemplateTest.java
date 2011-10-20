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

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import junit.framework.TestCase;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Tests {@link UriTemplate}.
 *
 * @author Ravi Mistry
 */
public class UriTemplateTest extends TestCase {

  public void testExpandTemplates_basic() {
    Map<String, Object> requestMap = Maps.newHashMap();
    requestMap.put("abc", "xyz");
    requestMap.put("def", "123");
    requestMap.put("unused", "unused parameter");

    // Assert with addUnusedParamsAsQueryParams = false.
    assertEquals("foo/xyz/bar/123", UriTemplate.expand("foo/{abc}/bar/{def}", requestMap, false));
    // Assert with addUnusedParamsAsQueryParams = true.
    assertEquals("foo/xyz/bar/123?unused=unused%20parameter",
        UriTemplate.expand("foo/{abc}/bar/{def}", requestMap, true));
    // Assert the map has not changed.
    assertEquals(3, requestMap.size());
    assertTrue(requestMap.containsKey("abc"));
    assertTrue(requestMap.containsKey("def"));
    assertTrue(requestMap.containsKey("unused"));
  }

  public void testExpandTemplates_noExpansionsWithQueryParams() {
    Map<String, Object> requestMap = Maps.newHashMap();
    requestMap.put("abc", "xyz");
    requestMap.put("def", "123");
    assertEquals("foo/xyz/bar/123?abc=xyz&def=123",
        UriTemplate.expand("foo/xyz/bar/123", requestMap, true));
  }

  public void testExpandTemplates_noExpansionsWithoutQueryParams() {
    Map<String, Object> requestMap = Maps.newHashMap();
    requestMap.put("abc", "xyz");
    requestMap.put("def", "123");
    assertEquals("foo/xyz/bar/123", UriTemplate.expand("foo/xyz/bar/123", requestMap, false));
  }

  public void testExpandTemplates_missingParameter() {
    Map<String, Object> requestMap = Maps.newHashMap();
    requestMap.put("abc", "xyz");
    assertEquals("foo/xyz/bar/", UriTemplate.expand("foo/{abc}/bar/{def}", requestMap, true));
  }

  public void testExpandTemplates_nullValue() {
    Map<String, Object> requestMap = Maps.newHashMap();
    requestMap.put("abc", "xyz");
    requestMap.put("def", null);
    assertEquals("foo/xyz/bar/", UriTemplate.expand("foo/{abc}/bar/{def}", requestMap, true));
  }

  public void testExpandTemplates_emptyAndNullRequestMap() {
    Map<String, Object> requestMap = Maps.newHashMap();
    assertEquals("foo//bar/", UriTemplate.expand("foo/{abc}/bar/{def}", requestMap, true));
    assertEquals("foo//bar/", UriTemplate.expand("foo/{abc}/bar/{def}", null, true));
  }

  private Iterator<String> getListParams() {
    List<String> params = Lists.newArrayList();
    params.add("red");
    params.add("green");
    params.add("blue");
    return params.iterator();
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
      {"{;d*}", ";red;green;blue"},
      {"{?d}", "?d=red,green,blue"},
      {"{?d*}", "?red&green&blue"},
      {"{&d}", "&d=red,green,blue"},
      {"{&d*}", "&red&green&blue"},
  };

  public void testExpandTemplates_explodeList() {
    for (String[] test : LIST_TESTS) {
      Map<String, Object> requestMap = Maps.newHashMap();
      requestMap.put("d", getListParams());
      assertEquals(test[1], UriTemplate.expand(test[0], requestMap, true));
    }
  }

  public void testExpandTemplates_missingCompositeParameter() {
    Map<String, Object> requestMap = Maps.newHashMap();
    requestMap.put("abc", "xyz");
    assertEquals("", UriTemplate.expand("{d}", requestMap, false));
    assertEquals("?abc=xyz", UriTemplate.expand("{d}", requestMap, true));
  }

  public void testExpandTemplates_emptyListParameter() {
    Map<String, Object> requestMap = Maps.newHashMap();
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

  public void testExpandTemplates_explodeMap() {
    for (String[] test : MAP_TESTS) {
      Map<String, Object> requestMap = Maps.newHashMap();
      requestMap.put("d", getMapParams());
      assertEquals(test[1], UriTemplate.expand(test[0], requestMap, true));
    }
  }

  public void testExpandTemplates_emptyMapParameter() {
    Map<String, Object> requestMap = Maps.newHashMap();
    requestMap.put("d", Maps.newHashMap());
    assertEquals("", UriTemplate.expand("{d}", requestMap, true));
  }

  public void testExpandTemplates_unusedQueryParametersEncoding() {
    Map<String, Object> requestMap = Maps.newLinkedHashMap();
    // Add unused params.
    requestMap.put("unused1", "abc!1234?");
    requestMap.put("unused2", "56$7 8");
    requestMap.put("unused3", "9=&/:@.");
    assertEquals("?unused1=abc!1234?&unused2=56$7%208&unused3=9%3D%26/:@.",
        UriTemplate.expand("", requestMap, true));
  }

  public void testExpandTemplates_mixedBagParameters() {
    Map<String, Object> requestMap = Maps.newLinkedHashMap();
    // Add list params.
    requestMap.put("list", getListParams());
    // Add map params.
    requestMap.put("map", getMapParams());
    // Add normal params.
    requestMap.put("abc", "xyz");
    // Add unused params.
    requestMap.put("unused1", "unused param");
    requestMap.put("unused2", "unused=param");
    assertEquals("foo/xyz/red/green/blue&map=semi,%3B,dot,.,comma,%2C?unused1=unused%20param"
        + "&unused2=unused%3Dparam",
        UriTemplate.expand("foo/{abc}{/list*}{&map}", requestMap, true));
    // Assert the map has not changed.
    assertEquals(5, requestMap.size());
    assertTrue(requestMap.containsKey("list"));
    assertTrue(requestMap.containsKey("map"));
    assertTrue(requestMap.containsKey("abc"));
    assertTrue(requestMap.containsKey("unused1"));
    assertTrue(requestMap.containsKey("unused2"));
  }
}
