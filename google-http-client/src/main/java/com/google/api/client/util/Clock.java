/*
 * Copyright (c) 2012 Google Inc.
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
 * Clock which can be used to get the amount of elapsed milliseconds in system time.
 *
 * <p>The default system implementation can be accessed at {@link #SYSTEM}. Alternative
 * implementations may be used for testing.
 *
 * @since 1.9
 * @author mlinder@google.com (Matthias Linder)
 */
public interface Clock {
  /**
   * Returns the current time in milliseconds since midnight, January 1, 1970 UTC, to match the
   * behavior of {@link System#currentTimeMillis()}.
   */
  long currentTimeMillis();

  /**
   * Provides the default System implementation of a Clock by using {@link
   * System#currentTimeMillis()}.
   */
  Clock SYSTEM =
      new Clock() {
        public long currentTimeMillis() {
          return System.currentTimeMillis();
        }
      };
}
