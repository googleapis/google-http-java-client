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

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/**
 * Tests for the {@link Clock}.
 *
 * @author mlinder@google.com (Matthias Linder)
 */
@RunWith(JUnit4.class)
public class ClockTest {
  /** Tests that the Clock.SYSTEM.currentTimeMillis() method returns useful values. */
  @Test
  public void testSystemClock() {
    assertNotNull(Clock.SYSTEM);

    // Confirm that the value returned here is similar to the system value.
    final long ERROR_MARGIN = 1000;
    long systemValue = System.currentTimeMillis();
    long clockValue = Clock.SYSTEM.currentTimeMillis();
    assertTrue(Math.abs(clockValue - systemValue) <= ERROR_MARGIN);
  }
}
