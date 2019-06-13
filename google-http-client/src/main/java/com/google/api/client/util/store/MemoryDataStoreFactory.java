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
 * Thread-safe in-memory implementation of a data store factory.
 *
 * <p>For convenience, a default global instance is provided in {@link #getDefaultInstance()}.
 *
 * @since 1.16
 * @author Yaniv Inbar
 */
public class MemoryDataStoreFactory extends AbstractDataStoreFactory {

  @Override
  protected <V extends Serializable> DataStore<V> createDataStore(String id) throws IOException {
    return new MemoryDataStore<V>(this, id);
  }

  /** Returns a global thread-safe instance. */
  public static MemoryDataStoreFactory getDefaultInstance() {
    return InstanceHolder.INSTANCE;
  }

  /** Holder for the result of {@link #getDefaultInstance()}. */
  static class InstanceHolder {
    static final MemoryDataStoreFactory INSTANCE = new MemoryDataStoreFactory();
  }

  static class MemoryDataStore<V extends Serializable> extends AbstractMemoryDataStore<V> {

    MemoryDataStore(MemoryDataStoreFactory dataStore, String id) {
      super(dataStore, id);
    }

    @Override
    public MemoryDataStoreFactory getDataStoreFactory() {
      return (MemoryDataStoreFactory) super.getDataStoreFactory();
    }
  }
}
