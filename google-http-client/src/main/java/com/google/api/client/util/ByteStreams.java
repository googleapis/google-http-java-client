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

package com.google.api.client.util;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Provides utility methods for working with byte arrays and I/O streams.
 *
 * <p>NOTE: this is a copy of a subset of Guava's {@link com.google.common.io.ByteStreams}. The
 * implementation must match as closely as possible to Guava's implementation.
 *
 * @since 1.14
 * @author Yaniv Inbar
 */
public final class ByteStreams {

  private static final int BUF_SIZE = 0x1000; // 4K

  /**
   * Copies all bytes from the input stream to the output stream. Does not close or flush either
   * stream.
   *
   * @param from the input stream to read from
   * @param to the output stream to write to
   * @return the number of bytes copied
   */
  public static long copy(InputStream from, OutputStream to) throws IOException {
    Preconditions.checkNotNull(from);
    Preconditions.checkNotNull(to);
    byte[] buf = new byte[BUF_SIZE];
    long total = 0;
    while (true) {
      int r = from.read(buf);
      if (r == -1) {
        break;
      }
      to.write(buf, 0, r);
      total += r;
    }
    return total;
  }

  /**
   * Wraps an input stream, limiting the number of bytes which can be read.
   *
   * @param in the input stream to be wrapped
   * @param limit the maximum number of bytes to be read
   * @return a length-limited {@link InputStream}
   */
  public static InputStream limit(InputStream in, long limit) {
    return new LimitedInputStream(in, limit);
  }

  private static final class LimitedInputStream extends FilterInputStream {

    private long left;
    private long mark = -1;

    LimitedInputStream(InputStream in, long limit) {
      super(in);
      Preconditions.checkNotNull(in);
      Preconditions.checkArgument(limit >= 0, "limit must be non-negative");
      left = limit;
    }

    @Override
    public int available() throws IOException {
      return (int) Math.min(in.available(), left);
    }

    // it's okay to mark even if mark isn't supported, as reset won't work
    @Override
    public synchronized void mark(int readLimit) {
      in.mark(readLimit);
      mark = left;
    }

    @Override
    public int read() throws IOException {
      if (left == 0) {
        return -1;
      }

      int result = in.read();
      if (result != -1) {
        --left;
      }
      return result;
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
      if (left == 0) {
        return -1;
      }

      len = (int) Math.min(len, left);
      int result = in.read(b, off, len);
      if (result != -1) {
        left -= result;
      }
      return result;
    }

    @Override
    public synchronized void reset() throws IOException {
      if (!in.markSupported()) {
        throw new IOException("Mark not supported");
      }
      if (mark == -1) {
        throw new IOException("Mark not set");
      }

      in.reset();
      left = mark;
    }

    @Override
    public long skip(long n) throws IOException {
      n = Math.min(n, left);
      long skipped = in.skip(n);
      left -= skipped;
      return skipped;
    }
  }

  /**
   * Reads some bytes from an input stream and stores them into the buffer array {@code b}.
   *
   * <p>This method blocks until {@code len} bytes of input data have been read into the array, or
   * end of file is detected. The number of bytes read is returned, possibly zero. Does not close
   * the stream.
   *
   * <p>A caller can detect EOF if the number of bytes read is less than {@code len}. All subsequent
   * calls on the same stream will return zero.
   *
   * <p>If {@code b} is null, a {@code NullPointerException} is thrown. If {@code off} is negative,
   * or {@code len} is negative, or {@code off+len} is greater than the length of the array {@code
   * b}, then an {@code IndexOutOfBoundsException} is thrown. If {@code len} is zero, then no bytes
   * are read. Otherwise, the first byte read is stored into element {@code b[off]}, the next one
   * into {@code b[off+1]}, and so on. The number of bytes read is, at most, equal to {@code len}.
   *
   * @param in the input stream to read from
   * @param b the buffer into which the data is read
   * @param off an int specifying the offset into the data
   * @param len an int specifying the number of bytes to read
   * @return the number of bytes read
   */
  public static int read(InputStream in, byte[] b, int off, int len) throws IOException {
    Preconditions.checkNotNull(in);
    Preconditions.checkNotNull(b);
    if (len < 0) {
      throw new IndexOutOfBoundsException("len is negative");
    }
    int total = 0;
    while (total < len) {
      int result = in.read(b, off + total, len - total);
      if (result == -1) {
        break;
      }
      total += result;
    }
    return total;
  }

  private ByteStreams() {}
}
