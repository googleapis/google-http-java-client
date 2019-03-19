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

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Thread-safe input stream wrapper that forwards all reads to a given input stream, while logging
 * all reads to a {@link LoggingByteArrayOutputStream}.
 *
 * @since 1.9
 * @author Yaniv Inbar
 */
public class LoggingInputStream extends FilterInputStream {

  /** Log stream. */
  private final LoggingByteArrayOutputStream logStream;

  /**
   * @param inputStream input stream to forward all reads to
   * @param logger logger
   * @param loggingLevel logging level
   * @param contentLoggingLimit maximum number of bytes to log (may be {@code 0} to avoid logging
   *     content)
   */
  public LoggingInputStream(
      InputStream inputStream, Logger logger, Level loggingLevel, int contentLoggingLimit) {
    super(inputStream);
    logStream = new LoggingByteArrayOutputStream(logger, loggingLevel, contentLoggingLimit);
  }

  @Override
  public int read() throws IOException {
    int read = super.read();
    logStream.write(read);
    return read;
  }

  @Override
  public int read(byte[] b, int off, int len) throws IOException {
    int read = super.read(b, off, len);
    if (read > 0) {
      logStream.write(b, off, read);
    }
    return read;
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
