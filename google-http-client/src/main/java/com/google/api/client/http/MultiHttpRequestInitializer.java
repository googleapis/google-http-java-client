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

import java.io.IOException;
import java.util.Collection;

/**
 * HTTP request initializer that is compose of a sequence of initializers.
 *
 * <p>
 * Each of the HTTP request initializers are run in the order input initializers. Any {@code null}
 * initializer will be ignored.
 * </p>
 * <p>
 * A copy of the input initializers parameters will be made in the constructor so the order and
 * value of the input initializers cannot be changed. Thus, if all the initializers are thread-safe,
 * this implementation is thread-safe. Also, if all the initializers are immutable, this
 * implementation is immutable.
 * </p>
 *
 * @since 1.7
 * @author Yaniv Inbar
 */
public class MultiHttpRequestInitializer implements HttpRequestInitializer {

  private final HttpRequestInitializer[] initializers;

  /**
   * @param initializers HTTP request initializers
   */
  public MultiHttpRequestInitializer(HttpRequestInitializer... initializers) {
    this.initializers = initializers.clone();
  }

  /**
   * @param initializers HTTP request initializers
   */
  public MultiHttpRequestInitializer(Collection<? extends HttpRequestInitializer> initializers) {
    this.initializers = initializers.toArray(new HttpRequestInitializer[initializers.size()]);
  }

  public void initialize(HttpRequest request) throws IOException {
    for (HttpRequestInitializer initializer : initializers) {
      if (initializer != null) {
        initializer.initialize(request);
      }
    }
  }
}
