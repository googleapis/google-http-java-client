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
 * @since 1.14
 * @author Yaniv Inbar
 */
public final class Objects {

  /**
   * Determines whether two possibly-null objects are equal. Returns:
   *
   * <ul>
   *   <li>{@code true} if {@code a} and {@code b} are both null.
   *   <li>{@code true} if {@code a} and {@code b} are both non-null and they are equal according to
   *       {@link Object#equals(Object)}.
   *   <li>{@code false} in all other situations.
   * </ul>
   *
   * <p>This assumes that any non-null objects passed to this function conform to the {@code
   * equals()} contract.
   */
  public static boolean equal(Object a, Object b) {
    return com.google.common.base.Objects.equal(a, b);
  }

  /**
   * Creates an instance of {@link ToStringHelper}.
   *
   * <p>This is helpful for implementing {@link Object#toString()}. Specification by example:
   *
   * <pre>
   * // Returns "ClassName{}"
   * Objects.toStringHelper(this)
   * .toString();
   *
   * // Returns "ClassName{x=1}"
   * Objects.toStringHelper(this)
   * .add("x", 1)
   * .toString();
   *
   * // Returns "MyObject{x=1}"
   * Objects.toStringHelper("MyObject")
   * .add("x", 1)
   * .toString();
   *
   * // Returns "ClassName{x=1, y=foo}"
   * Objects.toStringHelper(this)
   * .add("x", 1)
   * .add("y", "foo")
   * .toString();
   *
   * // Returns "ClassName{x=1}"
   * Objects.toStringHelper(this)
   * .omitNullValues()
   * .add("x", 1)
   * .add("y", null)
   * .toString();
   * </pre>
   *
   * @param self the object to generate the string for (typically {@code this}), used only for its
   *     class name
   */
  public static ToStringHelper toStringHelper(Object self) {
    return new ToStringHelper(self.getClass().getSimpleName());
  }

  // TODO(ejona): Swap to wrapping MoreObjects.ToStringHelper once depending on Guava 18.
  /** Support class for {@link Objects#toStringHelper}. */
  public static final class ToStringHelper {
    private final String className;
    private ValueHolder holderHead = new ValueHolder();
    private ValueHolder holderTail = holderHead;
    private boolean omitNullValues;

    /** @param className wrapped object */
    ToStringHelper(String className) {
      this.className = className;
    }

    /**
     * Configures the {@link ToStringHelper} so {@link #toString()} will ignore properties with null
     * value. The order of calling this method, relative to the {@code add()}/{@code addValue()}
     * methods, is not significant.
     */
    public ToStringHelper omitNullValues() {
      omitNullValues = true;
      return this;
    }

    /**
     * Adds a name/value pair to the formatted output in {@code name=value} format. If {@code value}
     * is {@code null}, the string {@code "null"} is used, unless {@link #omitNullValues()} is
     * called, in which case this name/value pair will not be added.
     */
    public ToStringHelper add(String name, Object value) {
      return addHolder(name, value);
    }

    @Override
    public String toString() {
      // create a copy to keep it consistent in case value changes
      boolean omitNullValuesSnapshot = omitNullValues;
      String nextSeparator = "";
      StringBuilder builder = new StringBuilder(32).append(className).append('{');
      for (ValueHolder valueHolder = holderHead.next;
          valueHolder != null;
          valueHolder = valueHolder.next) {
        if (!omitNullValuesSnapshot || valueHolder.value != null) {
          builder.append(nextSeparator);
          nextSeparator = ", ";

          if (valueHolder.name != null) {
            builder.append(valueHolder.name).append('=');
          }
          builder.append(valueHolder.value);
        }
      }
      return builder.append('}').toString();
    }

    private ValueHolder addHolder() {
      ValueHolder valueHolder = new ValueHolder();
      holderTail = holderTail.next = valueHolder;
      return valueHolder;
    }

    private ToStringHelper addHolder(String name, Object value) {
      ValueHolder valueHolder = addHolder();
      valueHolder.value = value;
      valueHolder.name = Preconditions.checkNotNull(name);
      return this;
    }

    private static final class ValueHolder {
      String name;
      Object value;
      ValueHolder next;
    }
  }

  private Objects() {}
}
