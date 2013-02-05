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

import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

/**
 * Static utility methods pertaining to {@link Set} instances.
 *
 * <p>
 * NOTE: proxy for the Guava implementation of {@link com.google.common.collect.Sets}.
 * </p>
 *
 * @since 1.14
 * @author Yaniv Inbar
 */
public final class Sets {

  /** Returns a new mutable, empty {@code HashSet} instance. */
  public static <E> HashSet<E> newHashSet() {
    return com.google.common.collect.Sets.newHashSet();
  }

  /**
   * Returns a new mutable {@code HashSet} instance containing the given elements in unspecified
   * order.
   *
   * @param elements the elements that the set should contain
   * @return a new {@code HashSet} containing those elements (minus duplicates)
   */
  public static <E> HashSet<E> newHashSet(Iterable<? extends E> elements) {
    return com.google.common.collect.Sets.newHashSet(elements);
  }

  /**
   * Returns a new mutable, empty {@code TreeSet} instance sorted by the natural sort ordering of
   * its elements.
   */
  public static <E extends Comparable<?>> TreeSet<E> newTreeSet() {
    return com.google.common.collect.Sets.newTreeSet();
  }

  private Sets() {
  }
}
