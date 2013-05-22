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

/**
 * Helper functions that can operate on any {@code Object}.
 *
 * <p>
 * NOTE: proxy for the Guava implementation of {@link com.google.common.base.Objects}.
 * </p>
 *
 * @since 1.14
 * @author Yaniv Inbar
 */
public final class Objects {

  /**
   * Determines whether two possibly-null objects are equal. Returns:
   *
   * <ul>
   * <li>{@code true} if {@code a} and {@code b} are both null.</li>
   * <li>{@code true} if {@code a} and {@code b} are both non-null and they are equal according to
   * {@link Object#equals(Object)}.</li>
   * <li>{@code false} in all other situations.</li>
   * </ul>
   *
   * <p>
   * This assumes that any non-null objects passed to this function conform to the {@code equals()}
   * contract.
   * </p>
   */
  public static boolean equal(Object a, Object b) {
    return com.google.common.base.Objects.equal(a, b);
  }

  /**
   * Creates an instance of {@link ToStringHelper}.
   *
   * <p>
   * This is helpful for implementing {@link Object#toString()}. Specification by example:
   * </p>
   *
   * <pre>
   // Returns "ClassName{}"
   Objects.toStringHelper(this)
       .toString();

   // Returns "ClassName{x=1}"
   Objects.toStringHelper(this)
       .add("x", 1)
       .toString();

   // Returns "MyObject{x=1}"
   Objects.toStringHelper("MyObject")
       .add("x", 1)
       .toString();

   // Returns "ClassName{x=1, y=foo}"
   Objects.toStringHelper(this)
       .add("x", 1)
       .add("y", "foo")
       .toString();

   // Returns "ClassName{x=1}"
   Objects.toStringHelper(this)
       .omitNullValues()
       .add("x", 1)
       .add("y", null)
       .toString();
   * </pre>
   *
   * @param self the object to generate the string for (typically {@code this}), used only for its
   *        class name
   */
  public static ToStringHelper toStringHelper(Object self) {
    return new ToStringHelper(com.google.common.base.Objects.toStringHelper(self));
  }

  /** Support class for {@link Objects#toStringHelper}. */
  public static final class ToStringHelper {

    /** Wrapped object. */
    private final com.google.common.base.Objects.ToStringHelper wrapped;

    /**
     * @param wrapped wrapped object
     */
    ToStringHelper(com.google.common.base.Objects.ToStringHelper wrapped) {
      this.wrapped = wrapped;
    }

    /**
     * Configures the {@link ToStringHelper} so {@link #toString()} will ignore properties with null
     * value. The order of calling this method, relative to the {@code add()}/{@code addValue()}
     * methods, is not significant.
     */
    public ToStringHelper omitNullValues() {
      wrapped.omitNullValues();
      return this;
    }

    /**
     * Adds a name/value pair to the formatted output in {@code name=value} format. If {@code value}
     * is {@code null}, the string {@code "null"} is used, unless {@link #omitNullValues()} is
     * called, in which case this name/value pair will not be added.
     */
    public ToStringHelper add(String name, Object value) {
      wrapped.add(name, value);
      return this;
    }

    @Override
    public String toString() {
      return wrapped.toString();
    }
  }

  private Objects() {
  }
}
