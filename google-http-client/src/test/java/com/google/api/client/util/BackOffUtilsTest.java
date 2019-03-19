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

import com.google.api.client.testing.util.MockBackOff;
import com.google.api.client.testing.util.MockSleeper;
import junit.framework.TestCase;

/**
 * Tests {@link BackOffUtils}.
 *
 * @author Yaniv Inbar
 */
public class BackOffUtilsTest extends TestCase {

  public void testNext() throws Exception {
    subtestNext(7);
    subtestNext(0);
    subtestNext(BackOff.STOP);
  }

  public void subtestNext(long millis) throws Exception {
    MockSleeper sleeper = new MockSleeper();
    MockBackOff backOff = new MockBackOff().setBackOffMillis(millis);
    if (millis == BackOff.STOP) {
      backOff.setMaxTries(0);
    }
    int retries = 0;
    while (retries <= backOff.getMaxTries() + 1) {
      boolean next = BackOffUtils.next(sleeper, backOff);
      assertEquals(retries < backOff.getMaxTries(), next);
      if (next) {
        retries++;
      }
      assertEquals(retries, sleeper.getCount());
      assertEquals(retries, backOff.getNumberOfTries());
      if (!next) {
        break;
      }
      assertEquals(millis, sleeper.getLastMillis());
    }
  }
}
