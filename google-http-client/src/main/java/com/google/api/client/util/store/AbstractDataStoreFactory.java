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

import com.google.api.client.util.Maps;
import com.google.api.client.util.Preconditions;
import java.io.IOException;
import java.io.Serializable;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.regex.Pattern;

/**
 * Abstract data store factory implementation.
 *
 * @author Yaniv Inbar
 * @since 1.16
 */
public abstract class AbstractDataStoreFactory implements DataStoreFactory {

  /** Lock on access to the data store map. */
  private final Lock lock = new ReentrantLock();

  /** Map of data store ID to data store. */
  private final Map<String, DataStore<? extends Serializable>> dataStoreMap = Maps.newHashMap();

  /**
   * Pattern to control possible values for the {@code id} parameter of {@link
   * #getDataStore(String)}.
   */
  private static final Pattern ID_PATTERN = Pattern.compile("\\w{1,30}");

  public final <V extends Serializable> DataStore<V> getDataStore(String id) throws IOException {
    Preconditions.checkArgument(
        ID_PATTERN.matcher(id).matches(), "%s does not match pattern %s", id, ID_PATTERN);
    lock.lock();
    try {
      @SuppressWarnings("unchecked")
      DataStore<V> dataStore = (DataStore<V>) dataStoreMap.get(id);
      if (dataStore == null) {
        dataStore = createDataStore(id);
        dataStoreMap.put(id, dataStore);
      }
      return dataStore;
    } finally {
      lock.unlock();
    }
  }

  /**
   * Returns a new instance of a type-specific data store based on the given unique ID.
   *
   * <p>The {@link DataStore#getId()} must match the {@code id} parameter from this method.
   *
   * @param id unique ID to refer to typed data store
   * @param <V> serializable type of the mapped value
   */
  protected abstract <V extends Serializable> DataStore<V> createDataStore(String id)
      throws IOException;
}
