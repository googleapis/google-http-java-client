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
import java.io.IOException;

/**
 * {@link Beta} <br>
 * Strategy interface to control back off between retry attempts.
 *
 * @since 1.7
 * @author Ravi Mistry
 * @deprecated (scheduled to be removed in 1.18) Use {@link HttpBackOffUnsuccessfulResponseHandler}
 *     instead.
 */
@Deprecated
@Beta
public interface BackOffPolicy {

  /** Value indicating that no more retries should be made, see {@link #getNextBackOffMillis()}. */
  public static final long STOP = -1L;

  /**
   * Determines if back off is required based on the specified status code.
   *
   * <p>Implementations may want to back off on server or product-specific errors.
   *
   * @param statusCode HTTP status code
   */
  public boolean isBackOffRequired(int statusCode);

  /** Reset Back off counters (if any) in an implementation-specific fashion. */
  public void reset();

  /**
   * Gets the number of milliseconds to wait before retrying an HTTP request. If {@link #STOP} is
   * returned, no retries should be made.
   *
   * <p>This method should be used as follows:
   *
   * <pre>
   *  long backoffTime = backoffPolicy.getNextBackoffMs();
   *  if (backoffTime == BackoffPolicy.STOP) {
   *    // Stop retrying.
   *  } else {
   *    // Retry after backoffTime.
   *  }
   * </pre>
   *
   * @return the number of milliseconds to wait when backing off requests, or {@link #STOP} if no
   *     more retries should be made
   */
  public long getNextBackOffMillis() throws IOException;
}
