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

import com.google.api.client.util.Preconditions;
import java.io.IOException;
import java.io.Serializable;
import java.util.Collection;
import java.util.Set;

/**
 * Abstract data store implementation.
 *
 * @param <V> serializable type of the mapped value
 * @author Yaniv Inbar
 * @since 1.16
 */
public abstract class AbstractDataStore<V extends Serializable> implements DataStore<V> {

  /** Data store factory. */
  private final DataStoreFactory dataStoreFactory;

  /** Data store ID. */
  private final String id;

  /**
   * @param dataStoreFactory data store factory
   * @param id data store ID
   */
  protected AbstractDataStore(DataStoreFactory dataStoreFactory, String id) {
    this.dataStoreFactory = Preconditions.checkNotNull(dataStoreFactory);
    this.id = Preconditions.checkNotNull(id);
  }

  /**
   * {@inheritDoc}
   *
   * <p>Overriding is only supported for the purpose of calling the super implementation and
   * changing the return type, but nothing else.
   */
  public DataStoreFactory getDataStoreFactory() {
    return dataStoreFactory;
  }

  public final String getId() {
    return id;
  }

  /**
   * {@inheritDoc}
   *
   * <p>Default implementation is to call {@link #get(String)} and check if it is {@code null}.
   */
  public boolean containsKey(String key) throws IOException {
    return get(key) != null;
  }

  /**
   * {@inheritDoc}
   *
   * <p>Default implementation is to call {@link Collection#contains(Object)} on {@link #values()}.
   */
  public boolean containsValue(V value) throws IOException {
    return values().contains(value);
  }

  /**
   * {@inheritDoc}
   *
   * <p>Default implementation is to check if {@link #size()} is {@code 0}.
   */
  public boolean isEmpty() throws IOException {
    return size() == 0;
  }

  /**
   * {@inheritDoc}
   *
   * <p>Default implementation is to call {@link Set#size()} on {@link #keySet()}.
   */
  public int size() throws IOException {
    return keySet().size();
  }
}
