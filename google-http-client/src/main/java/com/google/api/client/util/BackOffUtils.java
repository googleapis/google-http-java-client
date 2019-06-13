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

import java.io.IOException;

/**
 * {@link Beta} <br>
 * Utilities for {@link BackOff}.
 *
 * @since 1.15
 * @author Yaniv Inbar
 */
@Beta
public final class BackOffUtils {

  /**
   * Runs the next iteration of the back-off policy, and returns whether to continue to retry the
   * operation.
   *
   * <p>If {@code true}, it will call {@link Sleeper#sleep(long)} with the specified number of
   * milliseconds from {@link BackOff#nextBackOffMillis()}.
   *
   * @param sleeper sleeper
   * @param backOff back-off policy
   * @return whether to continue to back off; in other words, whether {@link
   *     BackOff#nextBackOffMillis()} did not return {@link BackOff#STOP}
   * @throws InterruptedException if any thread has interrupted the current thread
   */
  public static boolean next(Sleeper sleeper, BackOff backOff)
      throws InterruptedException, IOException {
    long backOffTime = backOff.nextBackOffMillis();
    if (backOffTime == BackOff.STOP) {
      return false;
    }
    sleeper.sleep(backOffTime);
    return true;
  }

  private BackOffUtils() {}
}
