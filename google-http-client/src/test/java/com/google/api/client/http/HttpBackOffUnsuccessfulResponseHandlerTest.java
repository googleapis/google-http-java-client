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

import com.google.api.client.http.HttpBackOffUnsuccessfulResponseHandler.BackOffRequired;
import com.google.api.client.testing.util.MockBackOff;
import com.google.api.client.testing.util.MockSleeper;
import com.google.api.client.util.BackOff;
import com.google.api.client.util.Sleeper;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;
import junit.framework.TestCase;

/**
 * Test {@link HttpBackOffUnsuccessfulResponseHandler}.
 *
 * @author Eyal Peled
 */
public class HttpBackOffUnsuccessfulResponseHandlerTest extends TestCase {

  public void testHandleResponse_retryFalse() throws IOException {
    subsetHandleResponse(0, 0, false, new MockBackOff(), BackOffRequired.ALWAYS);
  }

  public void testHandleResponse_requiredFalse() throws IOException {
    subsetHandleResponse(
        0,
        0,
        true,
        new MockBackOff(),
        new BackOffRequired() {
          public boolean isRequired(HttpResponse response) {
            return false;
          }
        });
  }

  public void testHandleResponse_requiredTrue() throws IOException {
    BackOff backOff = new MockBackOff().setBackOffMillis(4).setMaxTries(7);
    subsetHandleResponse(7, 4, true, backOff, BackOffRequired.ALWAYS);
    backOff = new MockBackOff().setBackOffMillis(2).setMaxTries(10);
    subsetHandleResponse(10, 2, true, backOff, BackOffRequired.ALWAYS);
  }

  private void subsetHandleResponse(
      int count, int millis, boolean retry, BackOff backOff, BackOffRequired backOffRequired)
      throws IOException {
    // create the handler
    MockSleeper sleeper = new MockSleeper();
    HttpBackOffUnsuccessfulResponseHandler handler =
        new HttpBackOffUnsuccessfulResponseHandler(backOff)
            .setSleeper(sleeper)
            .setBackOffRequired(backOffRequired);

    while (handler.handleResponse(null, null, retry)) {
      assertEquals(millis, sleeper.getLastMillis());
    }
    assertEquals(count, sleeper.getCount());
  }

  public void testHandleResponse_returnsFalseAndThreadRemainsInterrupted_whenSleepIsInterrupted()
      throws Exception {
    final AtomicBoolean stillInterrupted = new AtomicBoolean(false);
    Thread runningThread =
        new Thread() {
          @Override
          public void run() {
            HttpBackOffUnsuccessfulResponseHandler testTarget =
                new HttpBackOffUnsuccessfulResponseHandler(
                        new MockBackOff()
                            .setBackOffMillis(Long.MAX_VALUE) // Sleep until we interrupt it.
                            .setMaxTries(1))
                    .setSleeper(
                        Sleeper.DEFAULT) // Needs to be a real sleeper so we can interrupt it.
                    .setBackOffRequired(BackOffRequired.ALWAYS);

            try {
              testTarget.handleResponse(null, null, /* retrySupported= */ true);
            } catch (Exception ignored) {
            }
            stillInterrupted.set(Thread.currentThread().isInterrupted());
          }
        };
    runningThread.start();
    // Give runningThread some time to start.
    Thread.sleep(500L);
    runningThread.interrupt();
    runningThread.join();

    assertTrue(stillInterrupted.get());
  }
}
