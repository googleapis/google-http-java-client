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

import java.util.Collection;

/**
 * Static utility methods pertaining to {@link Collection} instances.
 *
 * <p>NOTE: this is a copy of a subset of Guava's {@link com.google.common.collect.Collections2}.
 * The implementation must match as closely as possible to Guava's implementation.
 *
 * @since 1.14
 * @author Yaniv Inbar
 */
public final class Collections2 {

  /** Used to avoid http://bugs.sun.com/view_bug.do?bug_id=6558557. */
  static <T> Collection<T> cast(Iterable<T> iterable) {
    return (Collection<T>) iterable;
  }

  private Collections2() {}
}
