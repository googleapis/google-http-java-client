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

package com.google.api.client.extensions.appengine.datastore;

import com.google.api.client.util.IOUtils;
import com.google.api.client.util.Lists;
import com.google.api.client.util.Maps;
import com.google.api.client.util.Preconditions;
import com.google.api.client.util.Sets;
import com.google.api.client.util.store.AbstractDataStore;
import com.google.api.client.util.store.AbstractDataStoreFactory;
import com.google.api.client.util.store.DataStore;
import com.google.api.client.util.store.DataStoreUtils;
import com.google.appengine.api.datastore.Blob;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.memcache.Expiration;
import com.google.appengine.api.memcache.MemcacheService;
import com.google.appengine.api.memcache.MemcacheServiceFactory;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Thread-safe Google App Engine implementation of a data store factory that directly uses the App
 * Engine Data Store API.
 *
 * <p>For convenience, a default global instance is provided in {@link #getDefaultInstance()}.
 *
 * <p>By default, it uses the Memcache API as an in-memory data cache. To disable it, call {@link
 * Builder#setDisableMemcache(boolean)}. The Memcache is only read to check if a key already has a
 * value inside {@link DataStore#get(String)}. The values in the Memcache are updated in the {@link
 * DataStore#get(String)}, {@link DataStore#set(String, Serializable)}, {@link
 * DataStore#delete(String)}, {@link DataStore#values()}, and {@link DataStore#clear()} methods.
 *
 * @since 1.16
 * @author Yaniv Inbar
 */
public class AppEngineDataStoreFactory extends AbstractDataStoreFactory {

  /** Whether to disable the memcache (which is enabled by default). */
  final boolean disableMemcache;

  /** Memcache expiration policy on puts. */
  final Expiration memcacheExpiration;

  @Override
  protected <V extends Serializable> DataStore<V> createDataStore(String id) throws IOException {
    return new AppEngineDataStore<V>(this, id);
  }

  public AppEngineDataStoreFactory() {
    this(new Builder());
  }

  /** @param builder builder */
  public AppEngineDataStoreFactory(Builder builder) {
    disableMemcache = builder.disableMemcache;
    memcacheExpiration = builder.memcacheExpiration;
  }

  /** Returns whether to disable the memcache (which is enabled by default). */
  public boolean getDisableMemcache() {
    return disableMemcache;
  }

  /**
   * Returns a global thread-safe instance based on the default constructor {@link
   * #AppEngineDataStoreFactory()}.
   */
  public static AppEngineDataStoreFactory getDefaultInstance() {
    return InstanceHolder.INSTANCE;
  }

  /** Holder for the result of {@link #getDefaultInstance()}. */
  static class InstanceHolder {
    static final AppEngineDataStoreFactory INSTANCE = new AppEngineDataStoreFactory();
  }

  static class AppEngineDataStore<V extends Serializable> extends AbstractDataStore<V> {

    /** Lock on access to the store. */
    private final Lock lock = new ReentrantLock();

    /** Name of the field in which the value is stored. */
    private static final String FIELD_VALUE = "value";

    /** The service instance used to access the Memcache API. */
    private final MemcacheService memcache;

    /** Data store service. */
    private final DatastoreService dataStoreService;

    /** Memcache expiration policy on puts. */
    final Expiration memcacheExpiration;

    AppEngineDataStore(AppEngineDataStoreFactory dataStoreFactory, String id) {
      super(dataStoreFactory, id);
      memcache =
          dataStoreFactory.disableMemcache ? null : MemcacheServiceFactory.getMemcacheService(id);
      memcacheExpiration = dataStoreFactory.memcacheExpiration;
      dataStoreService = DatastoreServiceFactory.getDatastoreService();
    }

    /** Deserializes the specified object from a Blob using an {@link ObjectInputStream}. */
    private V deserialize(Entity entity) throws IOException {
      Blob blob = (Blob) entity.getProperty(FIELD_VALUE);
      return IOUtils.deserialize(blob.getBytes());
    }

    @Override
    public Set<String> keySet() throws IOException {
      lock.lock();
      try {
        // NOTE: not possible with memcache
        Set<String> result = Sets.newHashSet();
        for (Entity entity : query(true)) {
          result.add(entity.getKey().getName());
        }
        return Collections.unmodifiableSet(result);
      } finally {
        lock.unlock();
      }
    }

    @Override
    public Collection<V> values() throws IOException {
      lock.lock();
      try {
        // Unfortunately no getKeys() method on MemcacheService, so the only option is to clear all
        // and re-populate the memcache from scratch. This is clearly inefficient.
        if (memcache != null) {
          memcache.clearAll();
        }
        List<V> result = Lists.newArrayList();
        Map<String, V> map = memcache != null ? Maps.<String, V>newHashMap() : null;
        for (Entity entity : query(false)) {
          V value = deserialize(entity);
          result.add(value);
          if (map != null) {
            map.put(entity.getKey().getName(), value);
          }
        }
        if (memcache != null) {
          memcache.putAll(map, memcacheExpiration);
        }
        return Collections.unmodifiableList(result);
      } finally {
        lock.unlock();
      }
    }

    @Override
    public V get(String key) throws IOException {
      if (key == null) {
        return null;
      }
      lock.lock();
      try {
        if (memcache != null && memcache.contains(key)) {
          @SuppressWarnings("unchecked")
          V result = (V) memcache.get(key);
          return result;
        }
        Key dataKey = KeyFactory.createKey(getId(), key);
        Entity entity;
        try {
          entity = dataStoreService.get(dataKey);
        } catch (EntityNotFoundException exception) {
          if (memcache != null) {
            memcache.delete(key);
          }
          return null;
        }
        V result = deserialize(entity);
        if (memcache != null) {
          memcache.put(key, result, memcacheExpiration);
        }
        return result;
      } finally {
        lock.unlock();
      }
    }

    @Override
    public AppEngineDataStore<V> set(String key, V value) throws IOException {
      Preconditions.checkNotNull(key);
      Preconditions.checkNotNull(value);
      lock.lock();
      try {
        Entity entity = new Entity(getId(), key);
        entity.setUnindexedProperty(FIELD_VALUE, new Blob(IOUtils.serialize(value)));
        dataStoreService.put(entity);
        if (memcache != null) {
          memcache.put(key, value, memcacheExpiration);
        }
      } finally {
        lock.unlock();
      }
      return this;
    }

    @Override
    public DataStore<V> delete(String key) throws IOException {
      if (key == null) {
        return this;
      }
      lock.lock();
      try {
        dataStoreService.delete(KeyFactory.createKey(getId(), key));
        if (memcache != null) {
          memcache.delete(key);
        }
      } finally {
        lock.unlock();
      }
      return this;
    }

    @Override
    public AppEngineDataStore<V> clear() throws IOException {
      lock.lock();
      try {
        if (memcache != null) {
          memcache.clearAll();
        }
        // no clearAll() method on DataStoreService so have to query all keys & delete them
        List<Key> keys = Lists.newArrayList();
        for (Entity entity : query(true)) {
          keys.add(entity.getKey());
        }
        dataStoreService.delete(keys);
      } finally {
        lock.unlock();
      }
      return this;
    }

    @Override
    public AppEngineDataStoreFactory getDataStoreFactory() {
      return (AppEngineDataStoreFactory) super.getDataStoreFactory();
    }

    @Override
    public String toString() {
      return DataStoreUtils.toString(this);
    }

    /**
     * Query on all of the keys for the data store ID.
     *
     * @param keysOnly whether to call {@link Query#setKeysOnly()}
     * @return iterable over the entities in the query result
     */
    private Iterable<Entity> query(boolean keysOnly) {
      Query query = new Query(getId());
      if (keysOnly) {
        query.setKeysOnly();
      }
      return dataStoreService.prepare(query).asIterable();
    }
  }

  /**
   * App Engine data store factory builder.
   *
   * <p>Implementation is not thread-safe.
   *
   * @since 1.16
   */
  public static class Builder {

    /** Whether to disable the memcache. */
    boolean disableMemcache;

    /** Memcache expiration policy on puts. */
    Expiration memcacheExpiration;

    /** Returns whether to disable the memcache. */
    public final boolean getDisableMemcache() {
      return disableMemcache;
    }

    /**
     * Sets whether to disable the memcache ({@code false} by default).
     *
     * <p>Overriding is only supported for the purpose of calling the super implementation and
     * changing the return type, but nothing else.
     */
    public Builder setDisableMemcache(boolean disableMemcache) {
      this.disableMemcache = disableMemcache;
      return this;
    }

    /** Returns the Memcache expiration policy on puts. */
    public final Expiration getMemcacheExpiration() {
      return memcacheExpiration;
    }

    /**
     * Sets the Memcache expiration policy on puts.
     *
     * <p>Overriding is only supported for the purpose of calling the super implementation and
     * changing the return type, but nothing else.
     */
    public Builder setMemcacheExpiration(Expiration memcacheExpiration) {
      this.memcacheExpiration = memcacheExpiration;
      return this;
    }

    /** Returns a new App Engine data store factory instance. */
    public AppEngineDataStoreFactory build() {
      return new AppEngineDataStoreFactory(this);
    }
  }
}
