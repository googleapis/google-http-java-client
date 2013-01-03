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

import com.google.common.io.ByteStreams;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Utilities for I/O streams.
 *
 * @author Yaniv Inbar
 * @since 1.14
 */
public class IOUtils {

  /**
   * Writes the content provided by the given source input stream into the given destination output
   * stream.
   *
   * <p>
   * The input stream is guaranteed to be closed at the end of this method.
   * </p>
   *
   * <p>
   * Sample use:
   * </p>
   *
   * <pre>
  static void copy(InputStream inputStream, File file) throws IOException {
    FileOutputStream out = new FileOutputStream(file);
    try {
      IOUtils.copy(inputStream, out);
    } finally {
      out.close();
    }
  }
   * </pre>
   *
   * @param inputStream source input stream
   * @param outputStream destination output stream
   */
  public static void copy(InputStream inputStream, OutputStream outputStream) throws IOException {
    copy(inputStream, outputStream, true);
  }


  /**
   * Writes the content provided by the given source input stream into the given destination output
   * stream, optionally closing the input stream.
   *
   * <p>
   * Sample use:
   * </p>
   *
   * <pre>
  static void copy(InputStream inputStream, File file) throws IOException {
    FileOutputStream out = new FileOutputStream(file);
    try {
      IOUtils.copy(inputStream, out, true);
    } finally {
      out.close();
    }
  }
   * </pre>
   *
   * @param inputStream source input stream
   * @param outputStream destination output stream
   * @param closeInputStream whether the input stream should be closed at the end of this method
   */
  public static void copy(
      InputStream inputStream, OutputStream outputStream, boolean closeInputStream)
      throws IOException {
    try {
      ByteStreams.copy(inputStream, outputStream);
    } finally {
      if (closeInputStream) {
        inputStream.close();
      }
    }
  }


  /**
   * Computes and returns the byte content length for a streaming content by calling
   * {@link StreamingContent#writeTo(OutputStream)} on a fake output stream that only counts bytes
   * written.
   *
   * @param content streaming content
   */
  public static long computeLength(StreamingContent content) throws IOException {
    ByteCountingOutputStream countingStream = new ByteCountingOutputStream();
    try {
      content.writeTo(countingStream);
    } finally {
      countingStream.close();
    }
    return countingStream.count;
  }
}
