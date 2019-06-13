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
 * Nano clock which can be used to measure elapsed time in nanoseconds.
 *
 * <p>The default system implementation can be accessed at {@link #SYSTEM}. Alternative
 * implementations may be used for testing.
 *
 * @since 1.14
 * @author Yaniv Inbar
 */
public interface NanoClock {

  /**
   * Returns the current value of the most precise available system timer, in nanoseconds for use to
   * measure elapsed time, to match the behavior of {@link System#nanoTime()}.
   */
  long nanoTime();

  /**
   * Provides the default System implementation of a nano clock by using {@link System#nanoTime()}.
   */
  NanoClock SYSTEM =
      new NanoClock() {
        public long nanoTime() {
          return System.nanoTime();
        }
      };
}
