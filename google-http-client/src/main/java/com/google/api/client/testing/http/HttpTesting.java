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

package com.google.api.client.testing.http;

import com.google.api.client.http.GenericUrl;

/**
 * Utilities and constants related to testing the HTTP library.
 *
 * @author Yaniv Inbar
 * @since 1.6
 */
public class HttpTesting {

  /** A simple string URL for testing of value {@code "http://google.com/"}. */
  public static final String SIMPLE_URL = "http://google.com/";

  /** A simple generic URL for testing of value {@link #SIMPLE_URL}. */
  public static final GenericUrl SIMPLE_GENERIC_URL = new GenericUrl(SIMPLE_URL);

  private HttpTesting() {
  }
}
