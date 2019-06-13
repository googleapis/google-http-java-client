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

import com.google.api.client.util.BackOff;
import com.google.api.client.util.Beta;
import com.google.api.client.util.Preconditions;
import java.io.IOException;

/**
 * {@link Beta} <br>
 * Mock for {@link BackOff} that always returns a fixed number.
 *
 * <p>Implementation is not thread-safe.
 *
 * @author Yaniv Inbar
 * @since 1.15
 */
@Beta
public class MockBackOff implements BackOff {

  /** Fixed back-off milliseconds. */
  private long backOffMillis;

  /** Maximum number of tries before returning {@link #STOP}. */
  private int maxTries = 10;

  /** Number of tries so far. */
  private int numTries;

  public void reset() throws IOException {
    numTries = 0;
  }

  public long nextBackOffMillis() throws IOException {
    if (numTries >= maxTries || backOffMillis == STOP) {
      return STOP;
    }
    numTries++;
    return backOffMillis;
  }

  /**
   * Sets the fixed back-off milliseconds (defaults to {@code 0}).
   *
   * <p>Overriding is only supported for the purpose of calling the super implementation and
   * changing the return type, but nothing else.
   */
  public MockBackOff setBackOffMillis(long backOffMillis) {
    Preconditions.checkArgument(backOffMillis == STOP || backOffMillis >= 0);
    this.backOffMillis = backOffMillis;
    return this;
  }

  /**
   * Sets the maximum number of tries before returning {@link #STOP} (defaults to {@code 10}).
   *
   * <p>Overriding is only supported for the purpose of calling the super implementation and
   * changing the return type, but nothing else.
   */
  public MockBackOff setMaxTries(int maxTries) {
    Preconditions.checkArgument(maxTries >= 0);
    this.maxTries = maxTries;
    return this;
  }

  /** Returns the maximum number of tries before returning {@link #STOP}. */
  public final int getMaxTries() {
    return maxTries;
  }

  /** Returns the number of tries so far. */
  public final int getNumberOfTries() {
    return numTries;
  }
}
