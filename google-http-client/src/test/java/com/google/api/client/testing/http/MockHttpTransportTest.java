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

package com.google.api.client.testing.http;

import static com.google.common.truth.Truth.assertThat;

import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestFactory;
import junit.framework.TestCase;

/**
 * Tests {@link MockHttpTransport}.
 *
 * @author Paweł Zuzelski
 */
public final class MockHttpTransportTest extends TestCase {
  // The purpose of this test is to verify, that the actual lowLevelHttpRequest used is preserved
  // so that the content of the request can be verified in tests.
  public void testBuildGetRequest_preservesLoLevelHttpRequest() throws Exception {
    MockHttpTransport httpTransport = new MockHttpTransport();
    GenericUrl url = new GenericUrl("http://example.org");
    HttpRequestFactory requestFactory = httpTransport.createRequestFactory();
    HttpRequest request = requestFactory.buildGetRequest(url);
    request.getHeaders().set("foo", "bar");
    Object unusedOnlyInspectingSideEffects = request.execute();
    MockLowLevelHttpRequest actualRequest = httpTransport.getLowLevelHttpRequest();
    assertThat(actualRequest.getHeaders()).containsKey("foo");
    assertThat(actualRequest.getHeaders().get("foo")).containsExactly("bar");
  }
}
