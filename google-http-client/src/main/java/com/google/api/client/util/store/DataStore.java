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

package com.google.api.client.util.store;

import java.io.IOException;
import java.io.Serializable;
import java.util.Collection;
import java.util.Set;

/**
 * Stores and manages serializable data of a specific type, where the key is a string and the value
 * is a {@link Serializable} object.
 *
 * <p>{@code null} keys or values are not allowed. Implementation should be thread-safe.
 *
 * @param <V> serializable type of the mapped value
 * @author Yaniv Inbar
 * @since 1.16
 */
public interface DataStore<V extends Serializable> {

  /** Returns the data store factory. */
  DataStoreFactory getDataStoreFactory();

  /** Returns the data store ID. */
  String getId();

  /** Returns the number of stored keys. */
  int size() throws IOException;

  /** Returns whether there are any stored keys. */
  boolean isEmpty() throws IOException;

  /** Returns whether the store contains the given key. */
  boolean containsKey(String key) throws IOException;

  /** Returns whether the store contains the given value. */
  boolean containsValue(V value) throws IOException;

  /**
   * Returns the unmodifiable set of all stored keys.
   *
   * <p>Order of the keys is not specified.
   */
  Set<String> keySet() throws IOException;

  /** Returns the unmodifiable collection of all stored values. */
  Collection<V> values() throws IOException;

  /**
   * Returns the stored value for the given key or {@code null} if not found.
   *
   * @param key key or {@code null} for {@code null} result
   */
  V get(String key) throws IOException;

  /**
   * Stores the given value for the given key (replacing any existing value).
   *
   * @param key key
   * @param value value object
   */
  DataStore<V> set(String key, V value) throws IOException;

  /** Deletes all of the stored keys and values. */
  DataStore<V> clear() throws IOException;

  /**
   * Deletes the stored key and value based on the given key, or ignored if the key doesn't already
   * exist.
   *
   * @param key key or {@code null} to ignore
   */
  DataStore<V> delete(String key) throws IOException;

  // TODO(yanivi): implement entrySet()?
}
