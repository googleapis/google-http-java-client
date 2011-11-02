/*
 * Copyright (c) 2010 Google Inc.
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

package com.google.api.client.json;

import com.google.api.client.util.GenericData;
import com.google.api.client.util.Key;

import java.util.concurrent.ConcurrentMap;

/**
 * Generic JSON data that stores all unknown key name/value pairs.
 *
 * <p>
 * Subclasses can declare fields for known data keys using the {@link Key} annotation. Each field
 * can be of any visibility (private, package private, protected, or public) and must not be static.
 * {@code null} unknown data key names are not allowed, but {@code null} data values are allowed.
 *
 * <p>
 * Implementation is not thread-safe. For a thread-safe choice instead use an implementation of
 * {@link ConcurrentMap}.
 * </p>
 *
 * @since 1.0
 * @author Yaniv Inbar
 */
public class GenericJson extends GenericData implements Cloneable {

  /**
   * JSON factory or {@code null} for none.
   *
   * @since 1.3
   * @deprecated (scheduled to be made private in 1.7) Use {@link #getFactory} or
   *             {@link #setFactory}
   */
  @Deprecated
  public JsonFactory jsonFactory;

  /**
   * Returns the JSON factory or {@code null} for none.
   *
   * @since 1.6
   */
  public final JsonFactory getFactory() {
    return jsonFactory;
  }

  /**
   * Sets the JSON factory or {@code null} for none.
   *
   * @since 1.6
   */
  public final void setFactory(JsonFactory factory) {
    this.jsonFactory = factory;
  }

  @Override
  public String toString() {
    if (jsonFactory != null) {
      return jsonFactory.toString(this);
    }
    return super.toString();
  }

  /**
   * Returns a pretty-printed serialized JSON string representation or {@link #toString()} if
   * {@link #getFactory()} is {@code null}.
   *
   * @since 1.6
   */
  public String toPrettyString() {
    if (jsonFactory != null) {
      return jsonFactory.toPrettyString(this);
    }
    return super.toString();
  }

  @Override
  public GenericJson clone() {
    return (GenericJson) super.clone();
  }
}
