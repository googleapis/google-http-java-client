/*
 * Copyright (c) 2010 Google Inc.
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

import java.io.IOException;
import java.io.OutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Wraps another streaming content without modifying the content, but also logging content using
 * {@link LoggingOutputStream}.
 *
 * <p>Implementation is not thread-safe.
 *
 * @author Yaniv Inbar
 * @since 1.14
 */
public final class LoggingStreamingContent implements StreamingContent {

  /** Streaming content. */
  private final StreamingContent content;

  /** Maximum number of bytes to log or {@code 0} to avoid logging content. */
  private final int contentLoggingLimit;

  /** Logging level. */
  private final Level loggingLevel;

  /** Logger. */
  private final Logger logger;

  /**
   * @param content streaming content
   * @param logger logger
   * @param loggingLevel logging level
   * @param contentLoggingLimit maximum number of bytes to log or {@code 0} to avoid logging content
   */
  public LoggingStreamingContent(
      StreamingContent content, Logger logger, Level loggingLevel, int contentLoggingLimit) {
    this.content = content;
    this.logger = logger;
    this.loggingLevel = loggingLevel;
    this.contentLoggingLimit = contentLoggingLimit;
  }

  public void writeTo(OutputStream out) throws IOException {
    LoggingOutputStream loggableOutputStream =
        new LoggingOutputStream(out, logger, loggingLevel, contentLoggingLimit);
    try {
      content.writeTo(loggableOutputStream);
    } finally {
      // force the log stream to close
      loggableOutputStream.getLogStream().close();
    }
    out.flush();
  }
}
