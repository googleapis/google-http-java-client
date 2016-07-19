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

package com.google.api.client.http;

import com.google.api.client.testing.util.MockBackOff;
import com.google.api.client.testing.util.MockSleeper;
import com.google.api.client.util.BackOff;
import java.io.IOException;
import junit.framework.TestCase;

/**
 * Tests {@link HttpBackOffIOExceptionHandler}.
 *
 * @author Eyal Peled
 */
public class HttpBackOffIOExpcetionHandlerTest extends TestCase {

  public void testHandle() throws IOException {
    subsetHandle(0, 0, true, BackOff.STOP_BACKOFF);
    subsetHandle(0, 0, false, new MockBackOff().setBackOffMillis(0).setMaxTries(5));
    subsetHandle(5, 0, true, new MockBackOff().setBackOffMillis(0).setMaxTries(5));
    subsetHandle(7, 10, true, new MockBackOff().setBackOffMillis(10).setMaxTries(7));
  }

  public void subsetHandle(long count, long millis, boolean retrySupported, BackOff backOff)
      throws IOException {
    // create the handler
    MockSleeper sleeper = new MockSleeper();
    HttpBackOffIOExceptionHandler handler =
        new HttpBackOffIOExceptionHandler(backOff).setSleeper(sleeper);

    while (handler.handleIOException(null, retrySupported)) {
      assertEquals(millis, sleeper.getLastMillis());
    }
    assertEquals(count, sleeper.getCount());
  }
}
