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

package com.google.api.client.testing.util;

import com.google.api.client.util.Beta;
import com.google.api.client.util.Sleeper;

/**
 * {@link Beta} <br>
 * Mock for {@link Sleeper}.
 *
 * <p>Implementation is not thread-safe.
 *
 * @author Yaniv Inbar
 * @since 1.15
 */
@Beta
public class MockSleeper implements Sleeper {

  /** Number of times {@link #sleep(long)} was called. */
  private int count;

  /**
   * Value of {@code millis} parameter when {@link #sleep(long)} was last called or {@code 0} if not
   * called.
   */
  private long lastMillis;

  public void sleep(long millis) throws InterruptedException {
    count++;
    lastMillis = millis;
  }

  /** Returns the number of times {@link #sleep(long)} was called. */
  public final int getCount() {
    return count;
  }

  /**
   * Returns the value of {@code millis} parameter when {@link #sleep(long)} was last called or
   * {@code 0} if not called.
   */
  public final long getLastMillis() {
    return lastMillis;
  }
}
