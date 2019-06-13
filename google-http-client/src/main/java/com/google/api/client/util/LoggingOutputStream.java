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

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Thread-safe output stream wrapper that forwards all writes to a given output stream, while
 * logging all writes to a {@link LoggingByteArrayOutputStream}.
 *
 * @since 1.9
 * @author Yaniv Inbar
 */
public class LoggingOutputStream extends FilterOutputStream {

  /** Log stream. */
  private final LoggingByteArrayOutputStream logStream;

  /**
   * @param outputStream output stream to forward all writes to
   * @param logger logger
   * @param loggingLevel logging level
   * @param contentLoggingLimit maximum number of bytes to log (may be {@code 0} to avoid logging
   *     content)
   */
  public LoggingOutputStream(
      OutputStream outputStream, Logger logger, Level loggingLevel, int contentLoggingLimit) {
    super(outputStream);
    logStream = new LoggingByteArrayOutputStream(logger, loggingLevel, contentLoggingLimit);
  }

  @Override
  public void write(int b) throws IOException {
    out.write(b);
    logStream.write(b);
  }

  @Override
  public void write(byte[] b, int off, int len) throws IOException {
    out.write(b, off, len);
    logStream.write(b, off, len);
  }

  @Override
  public void close() throws IOException {
    logStream.close();
    super.close();
  }

  /** Returns the log stream. */
  public final LoggingByteArrayOutputStream getLogStream() {
    return logStream;
  }
}
