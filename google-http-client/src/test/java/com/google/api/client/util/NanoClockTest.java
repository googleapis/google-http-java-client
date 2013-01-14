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

import junit.framework.TestCase;

/**
 * Tests {@link NanoClock}.
 *
 * @author Yaniv Inbar
 */
public class NanoClockTest extends TestCase {

  public void testSystemClock() {
    assertNotNull(NanoClock.SYSTEM);

    // Confirm that the value returned here is similar to the system value.
    final long ERROR_MARGIN = 1000000000;
    long systemValue = System.nanoTime();
    long clockValue = NanoClock.SYSTEM.nanoTime();
    assertTrue(
        clockValue + " != " + systemValue, Math.abs(clockValue - systemValue) <= ERROR_MARGIN);
  }
}
