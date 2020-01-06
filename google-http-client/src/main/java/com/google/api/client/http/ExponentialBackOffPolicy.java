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

import com.google.api.client.util.Beta;
import com.google.api.client.util.ExponentialBackOff;
import com.google.api.client.util.NanoClock;
import java.io.IOException;

/**
 * {@link Beta} <br>
 * Implementation of {@link BackOffPolicy} that increases the back off period for each retry attempt
 * using a randomization function that grows exponentially.
 *
 * <p>{@link #getNextBackOffMillis()} is calculated using the following formula:
 *
 * <pre>
 * randomized_interval =
 *     retry_interval * (random value in range [1 - randomization_factor, 1 + randomization_factor])
 * </pre>
 *
 * <p>In other words {@link #getNextBackOffMillis()} will range between the randomization factor
 * percentage below and above the retry interval. For example, using 2 seconds as the base retry
 * interval and 0.5 as the randomization factor, the actual back off period used in the next retry
 * attempt will be between 1 and 3 seconds.
 *
 * <p><b>Note:</b> max_interval caps the retry_interval and not the randomized_interval.
 *
 * <p>If the time elapsed since an {@link ExponentialBackOffPolicy} instance is created goes past
 * the max_elapsed_time then the method {@link #getNextBackOffMillis()} starts returning {@link
 * BackOffPolicy#STOP}. The elapsed time can be reset by calling {@link #reset()}.
 *
 * <p>Example: The default retry_interval is .5 seconds, default randomization_factor is 0.5,
 * default multiplier is 1.5 and the default max_interval is 1 minute. For 10 requests the sequence
 * will be (values in seconds) and assuming we go over the max_elapsed_time on the 10th request:
 *
 * <pre>
 * request#     retry_interval     randomized_interval
 *
 * 1             0.5                [0.25,   0.75]
 * 2             0.75               [0.375,  1.125]
 * 3             1.125              [0.562,  1.687]
 * 4             1.687              [0.8435, 2.53]
 * 5             2.53               [1.265,  3.795]
 * 6             3.795              [1.897,  5.692]
 * 7             5.692              [2.846,  8.538]
 * 8             8.538              [4.269, 12.807]
 * 9            12.807              [6.403, 19.210]
 * 10           19.210              {@link BackOffPolicy#STOP}
 * </pre>
 *
 * <p>Implementation is not thread-safe.
 *
 * @since 1.7
 * @author Ravi Mistry
 * @deprecated (scheduled to be removed in 1.18). Use {@link HttpBackOffUnsuccessfulResponseHandler}
 *     with {@link ExponentialBackOff} instead.
 */
@Beta
@Deprecated
public class ExponentialBackOffPolicy implements BackOffPolicy {

  /** The default initial interval value in milliseconds (0.5 seconds). */
  public static final int DEFAULT_INITIAL_INTERVAL_MILLIS =
      ExponentialBackOff.DEFAULT_INITIAL_INTERVAL_MILLIS;

  /**
   * The default randomization factor (0.5 which results in a random period ranging between 50%
   * below and 50% above the retry interval).
   */
  public static final double DEFAULT_RANDOMIZATION_FACTOR =
      ExponentialBackOff.DEFAULT_RANDOMIZATION_FACTOR;

  /** The default multiplier value (1.5 which is 50% increase per back off). */
  public static final double DEFAULT_MULTIPLIER = ExponentialBackOff.DEFAULT_MULTIPLIER;

  /** The default maximum back off time in milliseconds (1 minute). */
  public static final int DEFAULT_MAX_INTERVAL_MILLIS =
      ExponentialBackOff.DEFAULT_MAX_INTERVAL_MILLIS;

  /** The default maximum elapsed time in milliseconds (15 minutes). */
  public static final int DEFAULT_MAX_ELAPSED_TIME_MILLIS =
      ExponentialBackOff.DEFAULT_MAX_ELAPSED_TIME_MILLIS;

  /** Exponential backoff. */
  private final ExponentialBackOff exponentialBackOff;

  /**
   * Creates an instance of ExponentialBackOffPolicy using default values. To override the defaults
   * use {@link #builder}.
   *
   * <ul>
   *   <li>{@code initialIntervalMillis} is defaulted to {@link #DEFAULT_INITIAL_INTERVAL_MILLIS}
   *   <li>{@code randomizationFactor} is defaulted to {@link #DEFAULT_RANDOMIZATION_FACTOR}
   *   <li>{@code multiplier} is defaulted to {@link #DEFAULT_MULTIPLIER}
   *   <li>{@code maxIntervalMillis} is defaulted to {@link #DEFAULT_MAX_INTERVAL_MILLIS}
   *   <li>{@code maxElapsedTimeMillis} is defaulted in {@link #DEFAULT_MAX_ELAPSED_TIME_MILLIS}
   * </ul>
   */
  public ExponentialBackOffPolicy() {
    this(new Builder());
  }

  /**
   * @param builder builder
   * @since 1.14
   */
  protected ExponentialBackOffPolicy(Builder builder) {
    exponentialBackOff = builder.exponentialBackOffBuilder.build();
  }

  /**
   * Determines if back off is required based on the specified status code.
   *
   * <p>The idea is that the servers are only temporarily unavailable, and they should not be
   * overwhelmed when they are trying to get back up.
   *
   * <p>The default implementation requires back off for 500 and 503 status codes. Subclasses may
   * override if different status codes are required.
   */
  public boolean isBackOffRequired(int statusCode) {
    switch (statusCode) {
      case HttpStatusCodes.STATUS_CODE_SERVER_ERROR: // 500
      case HttpStatusCodes.STATUS_CODE_SERVICE_UNAVAILABLE: // 503
        return true;
      default:
        return false;
    }
  }

  /** Sets the interval back to the initial retry interval and restarts the timer. */
  public final void reset() {
    exponentialBackOff.reset();
  }

  /**
   * Gets the number of milliseconds to wait before retrying an HTTP request. If {@link #STOP} is
   * returned, no retries should be made.
   *
   * <p>This method calculates the next back off interval using the formula: randomized_interval =
   * retry_interval +/- (randomization_factor * retry_interval)
   *
   * <p>Subclasses may override if a different algorithm is required.
   *
   * @return the number of milliseconds to wait when backing off requests, or {@link #STOP} if no
   *     more retries should be made
   */
  public long getNextBackOffMillis() throws IOException {
    return exponentialBackOff.nextBackOffMillis();
  }

  /** Returns the initial retry interval in milliseconds. */
  public final int getInitialIntervalMillis() {
    return exponentialBackOff.getInitialIntervalMillis();
  }

  /**
   * Returns the randomization factor to use for creating a range around the retry interval.
   *
   * <p>A randomization factor of 0.5 results in a random period ranging between 50% below and 50%
   * above the retry interval.
   */
  public final double getRandomizationFactor() {
    return exponentialBackOff.getRandomizationFactor();
  }

  /** Returns the current retry interval in milliseconds. */
  public final int getCurrentIntervalMillis() {
    return exponentialBackOff.getCurrentIntervalMillis();
  }

  /** Returns the value to multiply the current interval with for each retry attempt. */
  public final double getMultiplier() {
    return exponentialBackOff.getMultiplier();
  }

  /**
   * Returns the maximum value of the back off period in milliseconds. Once the current interval
   * reaches this value it stops increasing.
   */
  public final int getMaxIntervalMillis() {
    return exponentialBackOff.getMaxIntervalMillis();
  }

  /**
   * Returns the maximum elapsed time in milliseconds.
   *
   * <p>If the time elapsed since an {@link ExponentialBackOffPolicy} instance is created goes past
   * the max_elapsed_time then the method {@link #getNextBackOffMillis()} starts returning {@link
   * BackOffPolicy#STOP}. The elapsed time can be reset by calling {@link #reset()}.
   */
  public final int getMaxElapsedTimeMillis() {
    return exponentialBackOff.getMaxElapsedTimeMillis();
  }

  /**
   * Returns the elapsed time in milliseconds since an {@link ExponentialBackOffPolicy} instance is
   * created and is reset when {@link #reset()} is called.
   *
   * <p>The elapsed time is computed using {@link System#nanoTime()}.
   */
  public final long getElapsedTimeMillis() {
    return exponentialBackOff.getElapsedTimeMillis();
  }

  /** Returns an instance of a new builder. */
  public static Builder builder() {
    return new Builder();
  }

  /**
   * {@link Beta} <br>
   * Builder for {@link ExponentialBackOffPolicy}.
   *
   * <p>Implementation is not thread-safe.
   *
   * @since 1.7
   */
  @Beta
  @Deprecated
  public static class Builder {

    /** Exponential back-off builder. */
    final ExponentialBackOff.Builder exponentialBackOffBuilder = new ExponentialBackOff.Builder();

    protected Builder() {}

    /** Builds a new instance of {@link ExponentialBackOffPolicy}. */
    public ExponentialBackOffPolicy build() {
      return new ExponentialBackOffPolicy(this);
    }

    /**
     * Returns the initial retry interval in milliseconds. The default value is {@link
     * #DEFAULT_INITIAL_INTERVAL_MILLIS}.
     */
    public final int getInitialIntervalMillis() {
      return exponentialBackOffBuilder.getInitialIntervalMillis();
    }

    /**
     * Sets the initial retry interval in milliseconds. The default value is {@link
     * #DEFAULT_INITIAL_INTERVAL_MILLIS}. Must be {@code > 0}.
     *
     * <p>Overriding is only supported for the purpose of calling the super implementation and
     * changing the return type, but nothing else.
     */
    public Builder setInitialIntervalMillis(int initialIntervalMillis) {
      exponentialBackOffBuilder.setInitialIntervalMillis(initialIntervalMillis);
      return this;
    }

    /**
     * Returns the randomization factor to use for creating a range around the retry interval. The
     * default value is {@link #DEFAULT_RANDOMIZATION_FACTOR}.
     *
     * <p>A randomization factor of 0.5 results in a random period ranging between 50% below and 50%
     * above the retry interval.
     *
     * <p>Overriding is only supported for the purpose of calling the super implementation and
     * changing the return type, but nothing else.
     */
    public final double getRandomizationFactor() {
      return exponentialBackOffBuilder.getRandomizationFactor();
    }

    /**
     * Sets the randomization factor to use for creating a range around the retry interval. The
     * default value is {@link #DEFAULT_RANDOMIZATION_FACTOR}. Must fall in the range {@code 0 <=
     * randomizationFactor < 1}.
     *
     * <p>A randomization factor of 0.5 results in a random period ranging between 50% below and 50%
     * above the retry interval.
     *
     * <p>Overriding is only supported for the purpose of calling the super implementation and
     * changing the return type, but nothing else.
     */
    public Builder setRandomizationFactor(double randomizationFactor) {
      exponentialBackOffBuilder.setRandomizationFactor(randomizationFactor);
      return this;
    }

    /**
     * Returns the value to multiply the current interval with for each retry attempt. The default
     * value is {@link #DEFAULT_MULTIPLIER}.
     */
    public final double getMultiplier() {
      return exponentialBackOffBuilder.getMultiplier();
    }

    /**
     * Sets the value to multiply the current interval with for each retry attempt. The default
     * value is {@link #DEFAULT_MULTIPLIER}. Must be {@code >= 1}.
     *
     * <p>Overriding is only supported for the purpose of calling the super implementation and
     * changing the return type, but nothing else.
     */
    public Builder setMultiplier(double multiplier) {
      exponentialBackOffBuilder.setMultiplier(multiplier);
      return this;
    }

    /**
     * Returns the maximum value of the back off period in milliseconds. Once the current interval
     * reaches this value it stops increasing. The default value is {@link
     * #DEFAULT_MAX_INTERVAL_MILLIS}. Must be {@code >= initialInterval}.
     */
    public final int getMaxIntervalMillis() {
      return exponentialBackOffBuilder.getMaxIntervalMillis();
    }

    /**
     * Sets the maximum value of the back off period in milliseconds. Once the current interval
     * reaches this value it stops increasing. The default value is {@link
     * #DEFAULT_MAX_INTERVAL_MILLIS}.
     *
     * <p>Overriding is only supported for the purpose of calling the super implementation and
     * changing the return type, but nothing else.
     */
    public Builder setMaxIntervalMillis(int maxIntervalMillis) {
      exponentialBackOffBuilder.setMaxIntervalMillis(maxIntervalMillis);
      return this;
    }

    /**
     * Returns the maximum elapsed time in milliseconds. The default value is {@link
     * #DEFAULT_MAX_ELAPSED_TIME_MILLIS}.
     *
     * <p>If the time elapsed since an {@link ExponentialBackOffPolicy} instance is created goes
     * past the max_elapsed_time then the method {@link #getNextBackOffMillis()} starts returning
     * {@link BackOffPolicy#STOP}. The elapsed time can be reset by calling {@link #reset()}.
     */
    public final int getMaxElapsedTimeMillis() {
      return exponentialBackOffBuilder.getMaxElapsedTimeMillis();
    }

    /**
     * Sets the maximum elapsed time in milliseconds. The default value is {@link
     * #DEFAULT_MAX_ELAPSED_TIME_MILLIS}. Must be {@code > 0}.
     *
     * <p>If the time elapsed since an {@link ExponentialBackOffPolicy} instance is created goes
     * past the max_elapsed_time then the method {@link #getNextBackOffMillis()} starts returning
     * {@link BackOffPolicy#STOP}. The elapsed time can be reset by calling {@link #reset()}.
     *
     * <p>Overriding is only supported for the purpose of calling the super implementation and
     * changing the return type, but nothing else.
     */
    public Builder setMaxElapsedTimeMillis(int maxElapsedTimeMillis) {
      exponentialBackOffBuilder.setMaxElapsedTimeMillis(maxElapsedTimeMillis);
      return this;
    }

    /**
     * Returns the nano clock.
     *
     * @since 1.14
     */
    public final NanoClock getNanoClock() {
      return exponentialBackOffBuilder.getNanoClock();
    }

    /**
     * Sets the nano clock ({@link NanoClock#SYSTEM} by default).
     *
     * <p>Overriding is only supported for the purpose of calling the super implementation and
     * changing the return type, but nothing else.
     *
     * @since 1.14
     */
    public Builder setNanoClock(NanoClock nanoClock) {
      exponentialBackOffBuilder.setNanoClock(nanoClock);
      return this;
    }
  }
}
