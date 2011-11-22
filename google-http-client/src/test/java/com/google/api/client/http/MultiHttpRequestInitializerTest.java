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

import com.google.api.client.testing.http.HttpTesting;
import com.google.api.client.testing.http.MockHttpTransport;

import junit.framework.TestCase;

import java.io.IOException;
import java.util.Arrays;

/**
 * Tests {@link MultiHttpRequestInitializer}.
 *
 * @author Yaniv Inbar
 */
public class MultiHttpRequestInitializerTest extends TestCase {

  static class StringHttpRequestInitializer implements HttpRequestInitializer {
    private final StringBuilder buf;
    private final char c;

    StringHttpRequestInitializer(StringBuilder buf, char c) {
      this.buf = buf;
      this.c = c;
    }

    public void initialize(HttpRequest request) throws IOException {
      buf.append(c);
    }
  }

  public void testInitialize() throws IOException {
    HttpRequest request =
        new MockHttpTransport().createRequestFactory().buildGetRequest(
            HttpTesting.SIMPLE_GENERIC_URL);
    MultiHttpRequestInitializer initializer = new MultiHttpRequestInitializer();
    initializer.initialize(request);
    // one
    StringBuilder buf = new StringBuilder();
    initializer = new MultiHttpRequestInitializer(new StringHttpRequestInitializer(buf, 'a'));
    initializer.initialize(request);
    assertEquals("a", buf.toString());
    // two
    buf = new StringBuilder();
    initializer =
        new MultiHttpRequestInitializer(new StringHttpRequestInitializer(buf, 'a'),
            new StringHttpRequestInitializer(buf, 'b'));
    initializer.initialize(request);
    assertEquals("ab", buf.toString());
    // three with a null
    buf = new StringBuilder();
    initializer =
        new MultiHttpRequestInitializer(new StringHttpRequestInitializer(buf, 'a'),
            new StringHttpRequestInitializer(buf, 'b'), null, new StringHttpRequestInitializer(buf,
                'c'));
    initializer.initialize(request);
    assertEquals("abc", buf.toString());
    // three with a null, but using a collection
    buf = new StringBuilder();
    initializer =
        new MultiHttpRequestInitializer(Arrays.asList(
            new StringHttpRequestInitializer(buf, 'a'), new StringHttpRequestInitializer(buf, 'b'),
            null, new StringHttpRequestInitializer(buf, 'c')));
    initializer.initialize(request);
    assertEquals("abc", buf.toString());
    // just nulls
    buf = new StringBuilder();
    initializer = new MultiHttpRequestInitializer(null, null, null, null);
    initializer.initialize(request);
    assertEquals("", buf.toString());
  }
}
