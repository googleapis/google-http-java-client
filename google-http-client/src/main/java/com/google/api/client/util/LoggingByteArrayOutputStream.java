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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.NumberFormat;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Thread-safe byte array output stream that logs what was written to it when the stream is closed.
 *
 * <p>Use this as a safe way to log a limited amount of content. As content is written to the
 * stream, it is stored as a byte array, up to the maximum number of bytes limit that was set in the
 * constructor. Note that if the maximum limit is set too high, it risks an {@link OutOfMemoryError}
 * on low-memory devices. This class also keeps track of the total number of bytes written,
 * regardless of whether they were logged. On {@link #close()}, it then logs two records to the
 * specified logger and logging level: the total number of bytes written, and the bounded content
 * logged (assuming charset "UTF-8"). Any control characters are stripped out of the content.
 *
 * @since 1.9
 * @author Yaniv Inbar
 */
public class LoggingByteArrayOutputStream extends ByteArrayOutputStream {

  /** Bytes written to the stream (may or may not have been logged). */
  private int bytesWritten;

  /** Maximum number of bytes to log (may be {@code 0} to avoid logging content). */
  private final int maximumBytesToLog;

  /** Whether the stream has already been closed. */
  private boolean closed;

  /** Logging level. */
  private final Level loggingLevel;

  /** Logger. */
  private final Logger logger;

  /**
   * @param logger logger
   * @param loggingLevel logging level
   * @param maximumBytesToLog maximum number of bytes to log (may be {@code 0} to avoid logging
   *     content)
   */
  public LoggingByteArrayOutputStream(Logger logger, Level loggingLevel, int maximumBytesToLog) {
    this.logger = Preconditions.checkNotNull(logger);
    this.loggingLevel = Preconditions.checkNotNull(loggingLevel);
    Preconditions.checkArgument(maximumBytesToLog >= 0);
    this.maximumBytesToLog = maximumBytesToLog;
  }

  @Override
  public synchronized void write(int b) {
    Preconditions.checkArgument(!closed);
    bytesWritten++;
    if (count < maximumBytesToLog) {
      super.write(b);
    }
  }

  @Override
  public synchronized void write(byte[] b, int off, int len) {
    Preconditions.checkArgument(!closed);
    bytesWritten += len;
    if (count < maximumBytesToLog) {
      int end = count + len;
      if (end > maximumBytesToLog) {
        len += maximumBytesToLog - end;
      }
      super.write(b, off, len);
    }
  }

  @Override
  public synchronized void close() throws IOException {
    // circumvent double close
    if (!closed) {
      // log the response
      if (bytesWritten != 0) {
        // log response size
        StringBuilder buf = new StringBuilder().append("Total: ");
        LoggingByteArrayOutputStream.appendBytes(buf, bytesWritten);
        if (count != 0 && count < bytesWritten) {
          buf.append(" (logging first ");
          LoggingByteArrayOutputStream.appendBytes(buf, count);
          buf.append(")");
        }
        logger.config(buf.toString());
        // log response content
        if (count != 0) {
          // strip out some unprintable control chars
          logger.log(
              loggingLevel,
              toString("UTF-8").replaceAll("[\\x00-\\x09\\x0B\\x0C\\x0E-\\x1F\\x7F]", " "));
        }
      }
      closed = true;
    }
  }

  /** Returns the maximum number of bytes to log (may be {@code 0} to avoid logging content). */
  public final int getMaximumBytesToLog() {
    return maximumBytesToLog;
  }

  /** Returns the bytes written to the stream (may or may not have been logged). */
  public final synchronized int getBytesWritten() {
    return bytesWritten;
  }

  private static void appendBytes(StringBuilder buf, int x) {
    if (x == 1) {
      buf.append("1 byte");
    } else {
      buf.append(NumberFormat.getInstance().format(x)).append(" bytes");
    }
  }
}
