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
 * Sleeper interface to use for requesting the current thread to sleep as specified in {@link
 * Thread#sleep(long)}.
 *
 * <p>The default implementation can be accessed at {@link #DEFAULT}. Primarily used for testing.
 *
 * @since 1.14
 * @author Yaniv Inbar
 */
public interface Sleeper {

  /**
   * Causes the currently executing thread to sleep (temporarily cease execution) for the specified
   * number of milliseconds as specified in {@link Thread#sleep(long)}.
   *
   * @param millis length of time to sleep in milliseconds
   * @throws InterruptedException if any thread has interrupted the current thread
   */
  void sleep(long millis) throws InterruptedException;

  /** Provides the default implementation based on {@link Thread#sleep(long)}. */
  Sleeper DEFAULT =
      new Sleeper() {

        public void sleep(long millis) throws InterruptedException {
          Thread.sleep(millis);
        }
      };
}
