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

package com.google.api.client.testing.util;

import com.google.api.client.util.Beta;
import com.google.api.client.util.Lists;
import java.util.List;
import java.util.logging.Handler;
import java.util.logging.LogRecord;

/**
 * {@link Beta} <br>
 * Logging handler that stores log records.
 *
 * @author Yaniv Inbar
 * @since 1.14
 */
@Beta
public class LogRecordingHandler extends Handler {

  /** Stored records. */
  private final List<LogRecord> records = Lists.newArrayList();

  @Override
  public void publish(LogRecord record) {
    records.add(record);
  }

  @Override
  public void flush() {}

  @Override
  public void close() throws SecurityException {}

  /** Returns a new instance of a list of published record messages. */
  public List<String> messages() {
    List<String> result = Lists.newArrayList();
    for (LogRecord record : records) {
      result.add(record.getMessage());
    }
    return result;
  }
}
