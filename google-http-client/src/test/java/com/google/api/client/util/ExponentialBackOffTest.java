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

package com.google.api.client.util;

import junit.framework.TestCase;

/**
 * Tests {@link ExponentialBackOff}.
 *
 * @author Ravi Mistry
 */
public class ExponentialBackOffTest extends TestCase {

  public ExponentialBackOffTest(String name) {
    super(name);
  }

  public void testConstructor() {
    ExponentialBackOff backOffPolicy = new ExponentialBackOff();
    assertEquals(
        ExponentialBackOff.DEFAULT_INITIAL_INTERVAL_MILLIS,
        backOffPolicy.getInitialIntervalMillis());
    assertEquals(
        ExponentialBackOff.DEFAULT_INITIAL_INTERVAL_MILLIS,
        backOffPolicy.getCurrentIntervalMillis());
    assertEquals(
        ExponentialBackOff.DEFAULT_RANDOMIZATION_FACTOR, backOffPolicy.getRandomizationFactor());
    assertEquals(ExponentialBackOff.DEFAULT_MULTIPLIER, backOffPolicy.getMultiplier());
    assertEquals(
        ExponentialBackOff.DEFAULT_MAX_INTERVAL_MILLIS, backOffPolicy.getMaxIntervalMillis());
    assertEquals(
        ExponentialBackOff.DEFAULT_MAX_ELAPSED_TIME_MILLIS,
        backOffPolicy.getMaxElapsedTimeMillis());
  }

  public void testBuilder() {
    ExponentialBackOff backOffPolicy = new ExponentialBackOff.Builder().build();
    assertEquals(
        ExponentialBackOff.DEFAULT_INITIAL_INTERVAL_MILLIS,
        backOffPolicy.getInitialIntervalMillis());
    assertEquals(
        ExponentialBackOff.DEFAULT_INITIAL_INTERVAL_MILLIS,
        backOffPolicy.getCurrentIntervalMillis());
    assertEquals(
        ExponentialBackOff.DEFAULT_RANDOMIZATION_FACTOR, backOffPolicy.getRandomizationFactor());
    assertEquals(ExponentialBackOff.DEFAULT_MULTIPLIER, backOffPolicy.getMultiplier());
    assertEquals(
        ExponentialBackOff.DEFAULT_MAX_INTERVAL_MILLIS, backOffPolicy.getMaxIntervalMillis());
    assertEquals(
        ExponentialBackOff.DEFAULT_MAX_ELAPSED_TIME_MILLIS,
        backOffPolicy.getMaxElapsedTimeMillis());

    int testInitialInterval = 1;
    double testRandomizationFactor = 0.1;
    double testMultiplier = 5.0;
    int testMaxInterval = 10;
    int testMaxElapsedTime = 900000;

    backOffPolicy =
        new ExponentialBackOff.Builder()
            .setInitialIntervalMillis(testInitialInterval)
            .setRandomizationFactor(testRandomizationFactor)
            .setMultiplier(testMultiplier)
            .setMaxIntervalMillis(testMaxInterval)
            .setMaxElapsedTimeMillis(testMaxElapsedTime)
            .build();
    assertEquals(testInitialInterval, backOffPolicy.getInitialIntervalMillis());
    assertEquals(testInitialInterval, backOffPolicy.getCurrentIntervalMillis());
    assertEquals(testRandomizationFactor, backOffPolicy.getRandomizationFactor());
    assertEquals(testMultiplier, backOffPolicy.getMultiplier());
    assertEquals(testMaxInterval, backOffPolicy.getMaxIntervalMillis());
    assertEquals(testMaxElapsedTime, backOffPolicy.getMaxElapsedTimeMillis());
  }

  public void testBackOff() throws Exception {
    int testInitialInterval = 500;
    double testRandomizationFactor = 0.1;
    double testMultiplier = 2.0;
    int testMaxInterval = 5000;
    int testMaxElapsedTime = 900000;

    ExponentialBackOff backOffPolicy =
        new ExponentialBackOff.Builder()
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
      long actualInterval = backOffPolicy.nextBackOffMillis();
      assertTrue(minInterval <= actualInterval && actualInterval <= maxInterval);
    }
  }

  public void testGetRandomizedInterval() {
    // 33% chance of being 1.
    assertEquals(1, ExponentialBackOff.getRandomValueFromInterval(0.5, 0, 2));
    assertEquals(1, ExponentialBackOff.getRandomValueFromInterval(0.5, 0.33, 2));
    // 33% chance of being 2.
    assertEquals(2, ExponentialBackOff.getRandomValueFromInterval(0.5, 0.34, 2));
    assertEquals(2, ExponentialBackOff.getRandomValueFromInterval(0.5, 0.66, 2));
    // 33% chance of being 3.
    assertEquals(3, ExponentialBackOff.getRandomValueFromInterval(0.5, 0.67, 2));
    assertEquals(3, ExponentialBackOff.getRandomValueFromInterval(0.5, 0.99, 2));
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

  public void testGetElapsedTimeMillis() {
    ExponentialBackOff backOffPolicy =
        new ExponentialBackOff.Builder().setNanoClock(new MyNanoClock()).build();
    long elapsedTimeMillis = backOffPolicy.getElapsedTimeMillis();
    assertEquals("elapsedTimeMillis=" + elapsedTimeMillis, 1000, elapsedTimeMillis);
  }

  public void testMaxElapsedTime() throws Exception {
    ExponentialBackOff backOffPolicy =
        new ExponentialBackOff.Builder().setNanoClock(new MyNanoClock(10000)).build();
    assertTrue(backOffPolicy.nextBackOffMillis() != BackOff.STOP);
    // Change the currentElapsedTimeMillis to be 0 ensuring that the elapsed time will be greater
    // than the max elapsed time.
    backOffPolicy.startTimeNanos = 0;
    assertEquals(BackOff.STOP, backOffPolicy.nextBackOffMillis());
  }

  public void testBackOffOverflow() throws Exception {
    int testInitialInterval = Integer.MAX_VALUE / 2;
    double testMultiplier = 2.1;
    int testMaxInterval = Integer.MAX_VALUE;
    ExponentialBackOff backOffPolicy =
        new ExponentialBackOff.Builder()
            .setInitialIntervalMillis(testInitialInterval)
            .setMultiplier(testMultiplier)
            .setMaxIntervalMillis(testMaxInterval)
            .build();
    backOffPolicy.nextBackOffMillis();
    // Assert that when an overflow is possible the current interval is set to the max interval.
    assertEquals(testMaxInterval, backOffPolicy.getCurrentIntervalMillis());
  }
}
