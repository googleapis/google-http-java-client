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

/**
 * Factory for a store that manages serializable data, where the key is a string and the value is a
 * {@link Serializable} object.
 *
 * <p>Users should keep a single globally shared instance of the data store factory. Otherwise, some
 * implementations may not share the internal copies of the data, and you'll end up with multiple
 * data stores by the same IDs, each living in a separate implementation. Some implementations may
 * also have some overhead, or may have caching implemented, and so multiple instances may defeat
 * that. Finally, have multiple instances may defeat the thread-safety guarantee for some
 * implementations.
 *
 * <p>Implementation should store the data in a persistent storage such as a database.
 * Implementation should be thread-safe. Read the JavaDoc of the implementation for those details.
 *
 * @see MemoryDataStoreFactory
 * @see FileDataStoreFactory
 * @author Yaniv Inbar
 * @since 1.16
 */
public interface DataStoreFactory {

  /**
   * Returns a type-specific data store based on the given unique ID.
   *
   * <p>If a data store by that ID does not already exist, it should be created now, stored for
   * later access, and returned. Otherwise, if there is already a data store by that ID, it should
   * be returned. The {@link DataStore#getId()} must match the {@code id} parameter from this
   * method.
   *
   * <p>The ID must be at least 1 and at most 30 characters long, and must contain only alphanumeric
   * or underscore characters.
   *
   * @param id unique ID to refer to typed data store
   * @param <V> serializable type of the mapped value
   */
  <V extends Serializable> DataStore<V> getDataStore(String id) throws IOException;
}
