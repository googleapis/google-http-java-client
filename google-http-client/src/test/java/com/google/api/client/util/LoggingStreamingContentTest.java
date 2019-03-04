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

import com.google.api.client.testing.util.LogRecordingHandler;
import java.io.ByteArrayOutputStream;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import junit.framework.TestCase;

/**
 * Tests {@link LoggingStreamingContent}.
 *
 * @author Yaniv Inbar
 */
public class LoggingStreamingContentTest extends TestCase {

  static final Logger LOGGER = Logger.getLogger(LoggingStreamingContentTest.class.getName());

  private static final byte[] SAMPLE_UTF8 =
      new byte[] {49, 50, 51, -41, -103, -41, -96, -41, -103, -41, -111};
  private static final String SAMPLE = "123\u05D9\u05e0\u05D9\u05D1";

  /** Test method for {@link LoggingStreamingContent#writeTo(java.io.OutputStream)}. */
  public void testWriteTo() throws Exception {
    LoggingStreamingContent logContent =
        new LoggingStreamingContent(
            new ByteArrayStreamingContent(SAMPLE_UTF8), LOGGER, Level.CONFIG, Integer.MAX_VALUE);
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    LOGGER.setLevel(Level.CONFIG);
    LogRecordingHandler recorder = new LogRecordingHandler();
    LOGGER.addHandler(recorder);
    logContent.writeTo(out);
    out.close();
    assertEquals(Arrays.asList("Total: 11 bytes", SAMPLE), recorder.messages());
  }

  public void testContentLoggingLimit() throws Exception {
    LOGGER.setLevel(Level.CONFIG);

    // Set the content logging limit to be equal to the length of the content.
    LogRecordingHandler recorder = new LogRecordingHandler();
    LOGGER.addHandler(recorder);
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    LoggingStreamingContent logContent =
        new LoggingStreamingContent(
            new ByteArrayStreamingContent(SAMPLE_UTF8), LOGGER, Level.CONFIG, SAMPLE_UTF8.length);
    logContent.writeTo(out);
    assertEquals(Arrays.asList("Total: 11 bytes", SAMPLE), recorder.messages());

    // Set the content logging limit to be less than the length of the content.
    recorder = new LogRecordingHandler();
    LOGGER.addHandler(recorder);
    logContent =
        new LoggingStreamingContent(
            new ByteArrayStreamingContent(SAMPLE_UTF8),
            LOGGER,
            Level.CONFIG,
            SAMPLE_UTF8.length - 1);
    logContent.writeTo(new ByteArrayOutputStream());
    assertEquals(
        Arrays.asList("Total: 11 bytes (logging first 10 bytes)", "123\u05D9\u05e0\u05D9\ufffd"),
        recorder.messages());

    // Set the content logging limit to 0 to disable content logging.
    recorder = new LogRecordingHandler();
    LOGGER.addHandler(recorder);
    logContent =
        new LoggingStreamingContent(
            new ByteArrayStreamingContent(SAMPLE_UTF8), LOGGER, Level.CONFIG, 0);
    logContent.writeTo(new ByteArrayOutputStream());
    assertEquals(Arrays.asList("Total: 11 bytes"), recorder.messages());

    // writeTo should behave as expected even if content length is specified to be -1.
    recorder = new LogRecordingHandler();
    LOGGER.addHandler(recorder);
    logContent =
        new LoggingStreamingContent(
            new ByteArrayStreamingContent(SAMPLE_UTF8), LOGGER, Level.CONFIG, SAMPLE_UTF8.length);
    logContent.writeTo(new ByteArrayOutputStream());
    assertEquals(Arrays.asList("Total: 11 bytes", SAMPLE), recorder.messages());

    // Assert that an exception is thrown if content logging limit < 0.
    try {
      logContent =
          new LoggingStreamingContent(
              new ByteArrayStreamingContent(SAMPLE_UTF8), LOGGER, Level.CONFIG, -1);
      logContent.writeTo(new ByteArrayOutputStream());
      fail("Expected: " + IllegalArgumentException.class);
    } catch (IllegalArgumentException e) {
      // Expected.
    }
  }
}
