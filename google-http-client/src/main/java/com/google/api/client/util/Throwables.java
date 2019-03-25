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
 * Static utility methods pertaining to instances of {@link Throwable}.
 *
 * <p>NOTE: proxy for the Guava implementation of {@link com.google.common.base.Throwables}.
 *
 * @since 1.14
 * @author Yaniv Inbar
 */
public final class Throwables {

  /**
   * Propagates {@code throwable} as-is if it is an instance of {@link RuntimeException} or {@link
   * Error}, or else as a last resort, wraps it in a {@code RuntimeException} then propagates.
   *
   * <p>This method always throws an exception. The {@code RuntimeException} return type is only for
   * client code to make Java type system happy in case a return value is required by the enclosing
   * method. Example usage:
   *
   * <pre>
   * T doSomething() {
   * try {
   * return someMethodThatCouldThrowAnything();
   * } catch (IKnowWhatToDoWithThisException e) {
   * return handle(e);
   * } catch (Throwable t) {
   * throw Throwables.propagate(t);
   * }
   * }
   * </pre>
   *
   * @param throwable the Throwable to propagate
   * @return nothing will ever be returned; this return type is only for your convenience, as
   *     illustrated in the example above
   */
  public static RuntimeException propagate(Throwable throwable) {
    return com.google.common.base.Throwables.propagate(throwable);
  }

  /**
   * Propagates {@code throwable} exactly as-is, if and only if it is an instance of {@link
   * RuntimeException} or {@link Error}. Example usage:
   *
   * <pre>
   * try {
   * someMethodThatCouldThrowAnything();
   * } catch (IKnowWhatToDoWithThisException e) {
   * handle(e);
   * } catch (Throwable t) {
   * Throwables.propagateIfPossible(t);
   * throw new RuntimeException("unexpected", t);
   * }
   * </pre>
   *
   * @param throwable throwable (may be {@code null})
   */
  public static void propagateIfPossible(Throwable throwable) {
    if (throwable != null) {
      com.google.common.base.Throwables.throwIfUnchecked(throwable);
    }
  }

  /**
   * Propagates {@code throwable} exactly as-is, if and only if it is an instance of {@link
   * RuntimeException}, {@link Error}, or {@code declaredType}. Example usage:
   *
   * <pre>
   * try {
   * someMethodThatCouldThrowAnything();
   * } catch (IKnowWhatToDoWithThisException e) {
   * handle(e);
   * } catch (Throwable t) {
   * Throwables.propagateIfPossible(t, OtherException.class);
   * throw new RuntimeException("unexpected", t);
   * }
   * </pre>
   *
   * @param throwable throwable (may be {@code null})
   * @param declaredType the single checked exception type declared by the calling method
   */
  public static <X extends Throwable> void propagateIfPossible(
      Throwable throwable, Class<X> declaredType) throws X {
    com.google.common.base.Throwables.propagateIfPossible(throwable, declaredType);
  }

  private Throwables() {}
}
