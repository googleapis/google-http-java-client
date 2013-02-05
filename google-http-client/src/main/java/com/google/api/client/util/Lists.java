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

import com.google.common.collect.ImmutableList;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Static utility methods pertaining to {@link List} instances.
 *
 * <p>
 * NOTE: proxy for the Guava implementation of {@link com.google.common.collect.Lists}.
 * </p>
 *
 * @since 1.14
 * @author Yaniv Inbar
 */
public final class Lists {

  /** Returns a new mutable, empty {@code ArrayList} instance. */
  public static <E> ArrayList<E> newArrayList() {
    return com.google.common.collect.Lists.newArrayList();
  }

  /**
   * Returns a new mutable {@code ArrayList} instance containing the given elements.
   *
   * @param elements the elements that the list should contain, in order
   * @return a new {@code ArrayList} containing those elements
   */
  public static <E> ArrayList<E> newArrayList(Iterable<? extends E> elements) {
    return com.google.common.collect.Lists.newArrayList(elements);
  }

  /**
   * Creates an {@code ArrayList} instance backed by an array of the <i>exact</i> size specified;
   * equivalent to {@link ArrayList#ArrayList(int)}.
   *
   * <p>
   * <b>Note:</b> if you know the exact size your list will be, consider using a fixed-size list (
   * {@link Arrays#asList(Object[])}) or an {@link ImmutableList} instead of a growable
   * {@link ArrayList}.
   *
   * @param initialArraySize the exact size of the initial backing array for the returned array list
   *        ({@code ArrayList} documentation calls this value the "capacity")
   * @return a new, empty {@code ArrayList} which is guaranteed not to resize itself unless its size
   *         reaches {@code initialArraySize + 1}
   * @throws IllegalArgumentException if {@code initialArraySize} is negative
   */
  public static <E> ArrayList<E> newArrayListWithCapacity(int initialArraySize) {
    return com.google.common.collect.Lists.newArrayListWithCapacity(initialArraySize);
  }

  private Lists() {
  }
}
