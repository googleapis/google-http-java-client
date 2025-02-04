/*
 * Copyright (c) 2011 Google Inc.
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.google.api.client.util.NanoClock;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/**
 * Tests {@link ExponentialBackOffPolicy}.
 *
 * @author Ravi Mistry
 */
@Deprecated
@RunWith(JUnit4.class)
public class ExponentialBackOffPolicyTest {

  @Test
  public void testConstructor() {
    ExponentialBackOffPolicy backOffPolicy = new ExponentialBackOffPolicy();
    assertEquals(
        ExponentialBackOffPolicy.DEFAULT_INITIAL_INTERVAL_MILLIS,
        backOffPolicy.getInitialIntervalMillis());
    assertEquals(
        ExponentialBackOffPolicy.DEFAULT_INITIAL_INTERVAL_MILLIS,
        backOffPolicy.getCurrentIntervalMillis());
    assertEquals(
        ExponentialBackOffPolicy.DEFAULT_RANDOMIZATION_FACTOR,
        backOffPolicy.getRandomizationFactor(),
        0);
    assertEquals(ExponentialBackOffPolicy.DEFAULT_MULTIPLIER, backOffPolicy.getMultiplier(), 0);
    assertEquals(
        ExponentialBackOffPolicy.DEFAULT_MAX_INTERVAL_MILLIS, backOffPolicy.getMaxIntervalMillis());
    assertEquals(
        ExponentialBackOffPolicy.DEFAULT_MAX_ELAPSED_TIME_MILLIS,
        backOffPolicy.getMaxElapsedTimeMillis());
  }

  @Test
  public void testBuilder() {
    ExponentialBackOffPolicy backOffPolicy = ExponentialBackOffPolicy.builder().build();
    assertEquals(
        ExponentialBackOffPolicy.DEFAULT_INITIAL_INTERVAL_MILLIS,
        backOffPolicy.getInitialIntervalMillis());
    assertEquals(
        ExponentialBackOffPolicy.DEFAULT_INITIAL_INTERVAL_MILLIS,
        backOffPolicy.getCurrentIntervalMillis());
    assertEquals(
        ExponentialBackOffPolicy.DEFAULT_RANDOMIZATION_FACTOR,
        backOffPolicy.getRandomizationFactor(),
        0);
    assertEquals(ExponentialBackOffPolicy.DEFAULT_MULTIPLIER, backOffPolicy.getMultiplier(), 0);
    assertEquals(
        ExponentialBackOffPolicy.DEFAULT_MAX_INTERVAL_MILLIS, backOffPolicy.getMaxIntervalMillis());
    assertEquals(
        ExponentialBackOffPolicy.DEFAULT_MAX_ELAPSED_TIME_MILLIS,
        backOffPolicy.getMaxElapsedTimeMillis());

    int testInitialInterval = 1;
    double testRandomizationFactor = 0.1;
    double testMultiplier = 5.0;
    int testMaxInterval = 10;
    int testMaxElapsedTime = 900000;

    backOffPolicy =
        ExponentialBackOffPolicy.builder()
            .setInitialIntervalMillis(testInitialInterval)
            .setRandomizationFactor(testRandomizationFactor)
            .setMultiplier(testMultiplier)
            .setMaxIntervalMillis(testMaxInterval)
            .setMaxElapsedTimeMillis(testMaxElapsedTime)
            .build();
    assertEquals(testInitialInterval, backOffPolicy.getInitialIntervalMillis());
    assertEquals(testInitialInterval, backOffPolicy.getCurrentIntervalMillis());
    assertEquals(testRandomizationFactor, backOffPolicy.getRandomizationFactor(), 0);
    assertEquals(testMultiplier, backOffPolicy.getMultiplier(), 0);
    assertEquals(testMaxInterval, backOffPolicy.getMaxIntervalMillis());
    assertEquals(testMaxElapsedTime, backOffPolicy.getMaxElapsedTimeMillis());
  }

  @Test
  public void testBackOff() throws Exception {
    int testInitialInterval = 500;
    double testRandomizationFactor = 0.1;
    double testMultiplier = 2.0;
    int testMaxInterval = 5000;
    int testMaxElapsedTime = 900000;

    ExponentialBackOffPolicy backOffPolicy =
        ExponentialBackOffPolicy.builder()
            .setInitialIntervalMillis(testInitialInterval)
            .setRandomizationFactor(testRandomizationFactor)
            .setMultiplier(testMultiplier)
            .setMaxIntervalMillis(testMaxInterval)
            .setMaxElapsedTimeMillis(testMaxElapsedTime)
            .build();
    int[] expectedResults = {500, 1000, 2000, 4000, 5000, 5000, 5000, 5000, 5000, 5000};
    for (int expected : expectedResults) {
      assertEquals(expected, backOffPolicy.getCurrentIntervalMillis());
      // Assert that the next back off falls in the expected range.
      int minInterval = (int) (expected - (testRandomizationFactor * expected));
      int maxInterval = (int) (expected + (testRandomizationFactor * expected));
      long actualInterval = backOffPolicy.getNextBackOffMillis();
      assertTrue(minInterval <= actualInterval && actualInterval <= maxInterval);
    }
  }

  static class MyNanoClock implements NanoClock {

    private int i = 0;
    private long startSeconds;

    MyNanoClock() {}

    MyNanoClock(long startSeconds) {
      this.startSeconds = startSeconds;
    }

    public long nanoTime() {
      return (startSeconds + i++) * 1000000000;
    }
  }

  @Test
  public void testGetElapsedTimeMillis() {
    ExponentialBackOffPolicy backOffPolicy =
        new ExponentialBackOffPolicy.Builder().setNanoClock(new MyNanoClock()).build();
    long elapsedTimeMillis = backOffPolicy.getElapsedTimeMillis();
    assertEquals("elapsedTimeMillis=" + elapsedTimeMillis, 1000, elapsedTimeMillis);
  }

  @Test
  public void testBackOffOverflow() throws Exception {
    int testInitialInterval = Integer.MAX_VALUE / 2;
    double testMultiplier = 2.1;
    int testMaxInterval = Integer.MAX_VALUE;
    ExponentialBackOffPolicy backOffPolicy =
        ExponentialBackOffPolicy.builder()
            .setInitialIntervalMillis(testInitialInterval)
            .setMultiplier(testMultiplier)
            .setMaxIntervalMillis(testMaxInterval)
            .build();
    backOffPolicy.getNextBackOffMillis();
    // Assert that when an overflow is possible the current interval is set to the max interval.
    assertEquals(testMaxInterval, backOffPolicy.getCurrentIntervalMillis());
  }
}
