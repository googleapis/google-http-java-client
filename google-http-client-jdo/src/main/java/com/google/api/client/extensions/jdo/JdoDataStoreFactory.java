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

package com.google.api.client.extensions.jdo;

import com.google.api.client.extensions.jdo.JdoDataStoreFactory.PrivateUtils.ComposedIdKey;
import com.google.api.client.util.IOUtils;
import com.google.api.client.util.Lists;
import com.google.api.client.util.Preconditions;
import com.google.api.client.util.Sets;
import com.google.api.client.util.store.AbstractDataStore;
import com.google.api.client.util.store.AbstractDataStoreFactory;
import com.google.api.client.util.store.DataStore;
import com.google.api.client.util.store.DataStoreUtils;

import java.io.IOException;
import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import javax.jdo.PersistenceManager;
import javax.jdo.PersistenceManagerFactory;
import javax.jdo.Query;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

/**
 * Thread-safe JDO implementation of a data store factory.
 *
 * @since 1.16
 * @author Yaniv Inbar
 */
public class JdoDataStoreFactory extends AbstractDataStoreFactory {

  /** Persistence manager factory. */
  private final PersistenceManagerFactory persistenceManagerFactory;

  public JdoDataStoreFactory(PersistenceManagerFactory persistenceManagerFactory) {
    this.persistenceManagerFactory = Preconditions.checkNotNull(persistenceManagerFactory);
  }

  @Override
  protected <V extends Serializable> DataStore<V> createDataStore(String id) throws IOException {
    return new JdoDataStore<V>(this, persistenceManagerFactory, id);
  }

  static class JdoDataStore<V extends Serializable> extends AbstractDataStore<V> {

    /** Lock on storing, loading and deleting a credential. */
    private final Lock lock = new ReentrantLock();

    /** Persistence manager factory. */
    private final PersistenceManagerFactory persistenceManagerFactory;

    JdoDataStore(JdoDataStoreFactory dataStore, PersistenceManagerFactory persistenceManagerFactory,
        String id) {
      super(dataStore, id);
      this.persistenceManagerFactory = persistenceManagerFactory;
    }

    public Set<String> keySet() throws IOException {
      lock.lock();
      try {
        PersistenceManager persistenceManager = persistenceManagerFactory.getPersistenceManager();
        try {
          Query query = newAllKeysQuery(persistenceManager);
          try {
            Set<String> result = Sets.newHashSet();
            for (JdoValue jdoValue : executeAllKeysQuery(query)) {
              result.add(jdoValue.getKey());
            }
            return Collections.unmodifiableSet(result);
          } finally {
            query.closeAll();
          }
        } finally {
          persistenceManager.close();
        }
      } finally {
        lock.unlock();
      }
    }

    public Collection<V> values() throws IOException {
      lock.lock();
      try {
        PersistenceManager persistenceManager = persistenceManagerFactory.getPersistenceManager();
        try {
          Query query = newAllKeysQuery(persistenceManager);
          try {
            List<V> result = Lists.newArrayList();
            for (JdoValue jdoValue : executeAllKeysQuery(query)) {
              result.add(jdoValue.<V>deserialize());
            }
            return Collections.unmodifiableList(result);
          } finally {
            query.closeAll();
          }
        } finally {
          persistenceManager.close();
        }
      } finally {
        lock.unlock();
      }
    }

    public V get(String key) throws IOException {
      if (key == null) {
        return null;
      }
      lock.lock();
      try {
        PersistenceManager persistenceManager = persistenceManagerFactory.getPersistenceManager();
        try {
          Query query = newKeyQuery(persistenceManager);
          try {
            JdoValue jdoValue = executeKeyQuery(query, key);
            return jdoValue == null ? null : jdoValue.<V>deserialize();
          } finally {
            query.closeAll();
          }
        } finally {
          persistenceManager.close();
        }
      } finally {
        lock.unlock();
      }
    }

    public JdoDataStore<V> set(String key, V value) throws IOException {
      Preconditions.checkNotNull(key);
      Preconditions.checkNotNull(value);
      lock.lock();
      try {
        PersistenceManager persistenceManager = persistenceManagerFactory.getPersistenceManager();
        try {
          Query query = newKeyQuery(persistenceManager);
          try {
            JdoValue jdoValue = executeKeyQuery(query, key);
            if (jdoValue != null) {
              jdoValue.serialize(value);
            } else {
              jdoValue = new JdoValue(getId(), key, value);
              persistenceManager.makePersistent(jdoValue);
            }
          } finally {
            query.closeAll();
          }
        } finally {
          persistenceManager.close();
        }
      } finally {
        lock.unlock();
      }
      return this;
    }

    public DataStore<V> delete(String key) throws IOException {
      if (key == null) {
        return this;
      }
      lock.lock();
      try {
        PersistenceManager persistenceManager = persistenceManagerFactory.getPersistenceManager();
        try {
          Query query = newKeyQuery(persistenceManager);
          try {
            JdoValue jdoValue = executeKeyQuery(query, key);
            if (jdoValue != null) {
              persistenceManager.deletePersistent(jdoValue);
            }
          } finally {
            query.closeAll();
          }
        } finally {
          persistenceManager.close();
        }
      } finally {
        lock.unlock();
      }
      return this;
    }

    public JdoDataStore<V> clear() throws IOException {
      lock.lock();
      try {
        PersistenceManager persistenceManager = persistenceManagerFactory.getPersistenceManager();
        try {
          Query query = newAllKeysQuery(persistenceManager);
          try {
            persistenceManager.deletePersistentAll(executeAllKeysQuery(query));
          } finally {
            query.closeAll();
          }
        } finally {
          persistenceManager.close();
        }
      } finally {
        lock.unlock();
      }
      return this;
    }

    @Override
    public JdoDataStoreFactory getDataStoreFactory() {
      return (JdoDataStoreFactory) super.getDataStoreFactory();
    }

    @Override
    public String toString() {
      return DataStoreUtils.toString(this);
    }

    /**
     * Returns a new query for all keys.
     *
     * @param persistenceManager persistence manager
     * @return new query for all keys
     */
    Query newAllKeysQuery(PersistenceManager persistenceManager) {
      Query query = persistenceManager.newQuery(JdoValue.class);
      query.setFilter("id == idParam");
      query.declareParameters("String idParam");
      return query;
    }

    /**
     * Executes the query for all keys.
     *
     * @param allKeysQuery query for all keys
     * @return query result
     */
    @SuppressWarnings("unchecked")
    Collection<JdoValue> executeAllKeysQuery(Query allKeysQuery) {
      return (Collection<JdoValue>) allKeysQuery.execute(getId());
    }

    /**
     * Returns a new query for a given key.
     *
     * @param persistenceManager persistence manager
     * @return new new query for a given key
     */
    Query newKeyQuery(PersistenceManager persistenceManager) {
      Query query = persistenceManager.newQuery(JdoValue.class);
      query.setFilter("id == idParam && key == keyParam");
      query.declareParameters("String idParam, String keyParam");
      return query;
    }

    /**
     * Executes the query for a given key, and returns the {@link JdoValue}.
     *
     * @param keyQuery query for a given key
     * @param key key
     * @return found {@link JdoValue} or {@code null} for none found
     */
    @SuppressWarnings("unchecked")
    JdoValue executeKeyQuery(Query keyQuery, String key) {
      Collection<JdoValue> queryResult = (Collection<JdoValue>) keyQuery.execute(getId(), key);
      return queryResult.isEmpty() ? null : queryResult.iterator().next();
    }
  }

  /**
   * JDO value class that contains the key-value pair, as well as the data store ID.
   */
  @PersistenceCapable(objectIdClass = ComposedIdKey.class)
  static class JdoValue {

    /** Key. */
    @PrimaryKey
    @Persistent
    private String key;

    /** Data store ID. */
    @PrimaryKey
    @Persistent
    private String id;

    /** Byte array of value. */
    @Persistent
    private byte[] bytes;

    /* Required by JDO. */
    @SuppressWarnings("unused")
    JdoValue() {
    }

    <V extends Serializable> JdoValue(String id, String key, V value) throws IOException {
      this.id = id;
      this.key = key;
      serialize(value);
    }

    <V extends Serializable> void serialize(V value) throws IOException {
      bytes = IOUtils.serialize(value);
    }

    <V extends Serializable> V deserialize() throws IOException {
      return IOUtils.deserialize(bytes);
    }

    String getKey() {
      return key;
    }
  }

  /**
   * Package private utilities class so the classes here isn't considered to be an external part of
   * the library. We need this because {@link ComposedIdKey} MUST be public because it is the
   * objectIdClass of {@link JdoValue}.
   */
  static class PrivateUtils {

    /**
     * See <a href="http://www.datanucleus.org/products/datanucleus/jdo/primary_key.html">JDO :
     * PrimaryKey Classes</a> for a reference.
     */
    public static class ComposedIdKey implements Serializable {

      private static final long serialVersionUID = 1L;

      /** Key. */
      public String key;

      /** Data store ID. */
      public String id;

      public ComposedIdKey() {
      }

      /**
       * @param value matches the result of toString()
       */
      public ComposedIdKey(String value) {
        StringTokenizer token = new StringTokenizer(value, "::");
        token.nextToken(); // className
        this.key = token.nextToken(); // key
        this.id = token.nextToken(); // id
      }

      @Override
      public boolean equals(Object obj) {
        if (obj == this) {
          return true;
        }
        if (!(obj instanceof ComposedIdKey)) {
          return false;
        }
        ComposedIdKey other = (ComposedIdKey) obj;
        return key.equals(other.key) && id.equals(other.id);
      }

      @Override
      public int hashCode() {
        return this.key.hashCode() ^ this.id.hashCode();
      }

      @Override
      public String toString() {
        return this.getClass().getName() + "::" + this.key + "::" + this.id;
      }
    }
  }
}
