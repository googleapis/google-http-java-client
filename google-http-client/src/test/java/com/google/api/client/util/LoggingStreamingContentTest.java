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

import junit.framework.TestCase;
import org.junit.After;
import org.junit.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.event.Level;
import uk.org.lidalia.slf4jtest.LoggingEvent;
import uk.org.lidalia.slf4jtest.TestLogger;
import uk.org.lidalia.slf4jtest.TestLoggerFactory;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Tests {@link LoggingStreamingContent}.
 *
 * @author Yaniv Inbar
 */
public class LoggingStreamingContentTest extends TestCase {

  static final Logger LOGGER = LoggerFactory.getLogger(LoggingStreamingContentTest.class);

  // For capturing slf4j logs.
  private TestLogger logInterceptor;

  private static final byte[] SAMPLE_UTF8 =
      new byte[] {49, 50, 51, -41, -103, -41, -96, -41, -103, -41, -111};
  private static final String SAMPLE = "123\u05D9\u05e0\u05D9\u05D1";

  @Before
  public void setUp() {
    logInterceptor = TestLoggerFactory.getTestLogger(LoggingStreamingContentTest.class);
    logInterceptor.clearAll();
  }

  @After
  public void clearLoggers() {
    TestLoggerFactory.clear();
  }

  /**
   * Test method for {@link LoggingStreamingContent#writeTo(java.io.OutputStream)}.
   */
  public void testWriteTo() throws Exception {
    logInterceptor.clear();
    LoggingStreamingContent logContent = new LoggingStreamingContent(
        new ByteArrayStreamingContent(SAMPLE_UTF8), LOGGER, Level.TRACE, Integer.MAX_VALUE);
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    logContent.writeTo(out);
    out.close();

    List<String> actualMessages = new ArrayList<String>();

    for (LoggingEvent loggingEvent : logInterceptor.getAllLoggingEvents()) {
      actualMessages.add(loggingEvent.getMessage());
    }

    assertEquals(Arrays.asList("Total: 11 bytes", SAMPLE), actualMessages);
  }

  public void testContentLoggingLimit() throws Exception {
    // Set the content logging limit to be equal to the length of the content.
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    LoggingStreamingContent logContent = new LoggingStreamingContent(
        new ByteArrayStreamingContent(SAMPLE_UTF8), LOGGER, Level.TRACE, SAMPLE_UTF8.length);
    logContent.writeTo(out);

    List<String> actualMessages = new ArrayList<String>();

    for (LoggingEvent loggingEvent : logInterceptor.getAllLoggingEvents()) {
      actualMessages.add(loggingEvent.getMessage());
    }

    assertEquals(Arrays.asList("Total: 11 bytes", SAMPLE), actualMessages);

    logInterceptor.clearAll();

    // Set the content logging limit to be less than the length of the content.
    logContent = new LoggingStreamingContent(
        new ByteArrayStreamingContent(SAMPLE_UTF8), LOGGER, Level.TRACE, SAMPLE_UTF8.length - 1);
    logContent.writeTo(new ByteArrayOutputStream());

    actualMessages = new ArrayList<String>();

    for (LoggingEvent loggingEvent : logInterceptor.getLoggingEvents()) {
      actualMessages.add(loggingEvent.getMessage());
    }

    assertEquals(
        Arrays.asList("Total: 11 bytes (logging first 10 bytes)", "123\u05D9\u05e0\u05D9\ufffd"),
        actualMessages);

    logInterceptor.clearAll();

    // Set the content logging limit to 0 to disable content logging.
    logContent = new LoggingStreamingContent(
        new ByteArrayStreamingContent(SAMPLE_UTF8), LOGGER, Level.TRACE, 0);
    logContent.writeTo(new ByteArrayOutputStream());

    actualMessages = new ArrayList<String>();

    for (LoggingEvent loggingEvent : logInterceptor.getAllLoggingEvents()) {
      actualMessages.add(loggingEvent.getMessage());
    }

    assertEquals(Arrays.asList("Total: 11 bytes"), actualMessages);

    logInterceptor.clearAll();

    // writeTo should behave as expected even if content length is specified to be -1.
    logContent = new LoggingStreamingContent(
        new ByteArrayStreamingContent(SAMPLE_UTF8), LOGGER, Level.TRACE, SAMPLE_UTF8.length);
    logContent.writeTo(new ByteArrayOutputStream());

    actualMessages = new ArrayList<String>();

    for (LoggingEvent loggingEvent : logInterceptor.getAllLoggingEvents()) {
      actualMessages.add(loggingEvent.getMessage());
    }

    assertEquals(Arrays.asList("Total: 11 bytes", SAMPLE), actualMessages);

    logInterceptor.clearAll();

    // Assert that an exception is thrown if content logging limit < 0.
    try {
      logContent = new LoggingStreamingContent(
          new ByteArrayStreamingContent(SAMPLE_UTF8), LOGGER, Level.TRACE, -1);
      logContent.writeTo(new ByteArrayOutputStream());
      fail("Expected: " + IllegalArgumentException.class);
    } catch (IllegalArgumentException e) {
      // Expected.
    }
  }
}
