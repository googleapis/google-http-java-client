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
import java.io.IOException;
import junit.framework.TestCase;

/**
 * Tests {@link MockBackOff}.
 *
 * @author Yaniv Inbar
 */
public class MockBackOffTest extends TestCase {

  public void testNextBackOffMillis() throws IOException {
    subtestNextBackOffMillis(0, new MockBackOff());
    subtestNextBackOffMillis(BackOff.STOP, new MockBackOff().setBackOffMillis(BackOff.STOP));
    subtestNextBackOffMillis(42, new MockBackOff().setBackOffMillis(42));
  }

  public void subtestNextBackOffMillis(long expectedValue, BackOff backOffPolicy)
      throws IOException {
    for (int i = 0; i < 10; i++) {
      assertEquals(expectedValue, backOffPolicy.nextBackOffMillis());
    }
  }
}
