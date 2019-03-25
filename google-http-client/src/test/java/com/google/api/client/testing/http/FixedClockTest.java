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

import junit.framework.TestCase;

/**
 * Tests for the {@link FixedClock}.
 *
 * @author mlinder@google.com (Matthias Linder)
 */
public class FixedClockTest extends TestCase {
  /** Tests that the {@link FixedClock#currentTimeMillis()} method will return the mocked values. */
  public void testCurrentTimeMillis() {
    // Check that the initial value is set properly.
    FixedClock clock = new FixedClock(100);
    assertEquals(100, clock.currentTimeMillis());

    // Check that the clock returns the new value once set.
    clock.setTime(500);
    assertEquals(500, clock.currentTimeMillis());
  }
}
