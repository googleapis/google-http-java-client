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

package com.google.api.client.testing.http;

import com.google.api.client.util.Beta;
import com.google.api.client.util.Clock;
import java.util.concurrent.atomic.AtomicLong;

/**
 * {@link Beta} <br>
 * A thread-safe fixed time implementation of the Clock to be used for unit testing.
 *
 * <p>Explicitly allows you to set the time to any arbitrary value.
 *
 * @since 1.9
 * @author mlinder@google.com (Matthias Linder)
 */
@Beta
public class FixedClock implements Clock {
  private AtomicLong currentTime;

  /** Initializes the FixedClock with 0 millis as start time. */
  public FixedClock() {
    this(0L);
  }

  /**
   * Initializes the FixedClock with the specified time.
   *
   * @param startTime time in milliseconds used for initialization.
   */
  public FixedClock(long startTime) {
    currentTime = new AtomicLong(startTime);
  }

  /**
   * Changes the time value this time provider is returning.
   *
   * @param newTime New time in milliseconds.
   */
  public FixedClock setTime(long newTime) {
    currentTime.set(newTime);
    return this;
  }

  public long currentTimeMillis() {
    return currentTime.get();
  }
}
