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

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;

/**
 * Static utility methods pertaining to {@link Map} instances.
 *
 * <p>NOTE: this is a copy of a subset of Guava's {@link com.google.common.collect.Maps}. The
 * implementation must match as closely as possible to Guava's implementation.
 *
 * @since 1.14
 * @author Yaniv Inbar
 */
public final class Maps {

  /** Returns a new mutable, empty {@code HashMap} instance. */
  public static <K, V> HashMap<K, V> newHashMap() {
    return new HashMap<K, V>();
  }

  /** Returns a new mutable, empty, insertion-ordered {@code LinkedHashMap} instance. */
  public static <K, V> LinkedHashMap<K, V> newLinkedHashMap() {
    return new LinkedHashMap<K, V>();
  }

  /**
   * Returns a new mutable, empty {@code TreeMap} instance using the natural ordering of its
   * elements.
   */
  public static <K extends Comparable<?>, V> TreeMap<K, V> newTreeMap() {
    return new TreeMap<K, V>();
  }

  private Maps() {}
}
