/*
 * Copyright 2021 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.api.client.http;

import com.google.api.client.http.javanet.NetHttpTransport;
import java.io.IOException;
import junit.framework.TestCase;

/** Tests {@link HttpRequestFactory}. */
public class HttpRequestFactoryTest extends TestCase {

  public void testBuildRequest_urlShouldBeSet() throws IllegalArgumentException, IOException {
    HttpRequestFactory requestFactory =
        new NetHttpTransport()
            .createRequestFactory(
                new HttpRequestInitializer() {
                  @Override
                  public void initialize(HttpRequest request) {
                    // Url should be set by buildRequest method before calling initialize.
                    if (request.getUrl() == null) {
                      throw new IllegalArgumentException("url is not set in request");
                    }
                  }
                });
    GenericUrl url = new GenericUrl("https://foo.googleapis.com/");
    HttpRequest request = requestFactory.buildRequest("GET", url, null);
    assertEquals(url, request.getUrl());
  }
}
