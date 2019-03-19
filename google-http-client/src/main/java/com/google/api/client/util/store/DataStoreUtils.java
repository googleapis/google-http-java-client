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

/**
 * Utilities for data stores.
 *
 * @author Yaniv Inbar
 * @since 1.16
 */
public final class DataStoreUtils {

  /**
   * Returns a debug string for the given data store to be used as an implementation of {@link
   * Object#toString()}.
   *
   * <p>Implementation iterates over {@link DataStore#keySet()}, calling {@link
   * DataStore#get(String)} on each key.
   *
   * @param dataStore data store
   * @return debug string
   */
  public static String toString(DataStore<?> dataStore) {
    try {
      StringBuilder sb = new StringBuilder();
      sb.append('{');
      boolean first = true;
      for (String key : dataStore.keySet()) {
        if (first) {
          first = false;
        } else {
          sb.append(", ");
        }
        sb.append(key).append('=').append(dataStore.get(key));
      }
      return sb.append('}').toString();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private DataStoreUtils() {}
}
