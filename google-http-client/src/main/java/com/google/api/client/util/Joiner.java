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

package com.google.api.client.util;

import java.util.Map;

/**
 * An object which joins pieces of text (specified as an array, {@link Iterable}, varargs or even a
 * {@link Map}) with a separator.
 *
 * <p>NOTE: proxy for the Guava implementation of {@link com.google.common.base.Joiner}.
 *
 * @since 1.14
 * @author Yaniv Inbar
 */
public final class Joiner {

  /** Wrapped joiner. */
  private final com.google.common.base.Joiner wrapped;

  /** Returns a joiner which automatically places {@code separator} between consecutive elements. */
  public static Joiner on(char separator) {
    return new Joiner(com.google.common.base.Joiner.on(separator));
  }

  /** @param wrapped wrapped joiner */
  private Joiner(com.google.common.base.Joiner wrapped) {
    this.wrapped = wrapped;
  }

  /**
   * Returns a string containing the string representation of each of {@code parts}, using the
   * previously configured separator between each.
   */
  public final String join(Iterable<?> parts) {
    return wrapped.join(parts);
  }
}
