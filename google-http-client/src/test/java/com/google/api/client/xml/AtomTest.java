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

package com.google.api.client.xml;

import com.google.api.client.http.HttpHeaders;
import com.google.api.client.xml.atom.Atom;

import junit.framework.TestCase;
import org.junit.Assert;

import java.util.List;

/**
 * Tests {@link Atom}.
 *
 * @author Yaniv Inbar
 */
public class AtomTest extends TestCase {

  @SuppressWarnings("unchecked")
  public void testSetSlugHeader() {
    HttpHeaders headers = new HttpHeaders();
    assertNull(headers.get("Slug"));
    subtestSetSlugHeader(headers, "value", "value");
    subtestSetSlugHeader(
        headers, " !\"#$&'()*+,-./:;<=>?@[\\]^_`{|}~", " !\"#$&'()*+,-./:;<=>?@[\\]^_`{|}~");
    subtestSetSlugHeader(headers, "%D7%99%D7%A0%D7%99%D7%91", "יניב");
    subtestSetSlugHeader(headers, null, null);
  }

  @SuppressWarnings("unchecked")
  public void subtestSetSlugHeader(HttpHeaders headers, String expectedValue, String value) {
    Atom.setSlugHeader(headers, value);
    if (value == null) {
      assertNull(headers.get("Slug"));
    } else {
      Assert.assertArrayEquals(
          new String[] {expectedValue}, ((List<String>) headers.get("Slug")).toArray());
    }
  }
}
