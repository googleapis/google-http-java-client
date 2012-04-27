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

import com.google.common.collect.Lists;

import junit.framework.TestCase;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;

/**
 * Tests {@link LogContent}.
 *
 * @author Yaniv Inbar
 */
public class LogContentTest extends TestCase {

  private static final byte[] SAMPLE_UTF8 =
      new byte[] {49, 50, 51, -41, -103, -41, -96, -41, -103, -41, -111};
  private static final String SAMPLE = "123\u05D9\u05e0\u05D9\u05D1";

  static class Recorder extends Handler {
    List<String> messages = Lists.newArrayList();
    List<LogRecord> records = Lists.newArrayList();

    @Override
    public void close() throws SecurityException {
    }

    @Override
    public void flush() {
    }

    @Override
    public void publish(LogRecord record) {
      records.add(record);
      messages.add(record.getMessage());
    }

    void assertMessages(String... expectedMessages) {
      int size = messages.size();
      if (expectedMessages.length != size) {
        assertEquals(Arrays.asList(expectedMessages), messages);
      }
      for (int i = 0; i < expectedMessages.length; i++) {
        String expectedMessage = expectedMessages[i];
        String actualMessage = messages.get(i);
        if (!expectedMessage.equals(actualMessage)) {
          assertEquals(expectedMessage, actualMessage);
        }
      }
    }
  }

  /**
   * Test method for {@link LogContent#writeTo(java.io.OutputStream)}.
   */
  public void testWriteTo() throws IOException {
    LogContent logContent = new LogContent(
        new ByteArrayContent(null, SAMPLE_UTF8), null, null, SAMPLE_UTF8.length, Integer.MAX_VALUE);
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    HttpTransport.LOGGER.setLevel(Level.CONFIG);
    Recorder recorder = new Recorder();
    HttpTransport.LOGGER.addHandler(recorder);
    logContent.writeTo(out);
    out.close();
    recorder.assertMessages("Total: 11 bytes", SAMPLE);
  }

  public void testContentLoggingLimit() throws IOException {
    HttpTransport.LOGGER.setLevel(Level.CONFIG);

    // Set the content logging limit to be equal to the length of the content.
    Recorder recorder = new Recorder();
    HttpTransport.LOGGER.addHandler(recorder);
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    LogContent logContent = new LogContent(
        new ByteArrayContent(null, SAMPLE_UTF8), null, null, SAMPLE_UTF8.length,
        SAMPLE_UTF8.length);
    logContent.writeTo(out);
    recorder.assertMessages("Total: 11 bytes", SAMPLE);

    // Set the content logging limit to be less than the length of the content.
    recorder = new Recorder();
    HttpTransport.LOGGER.addHandler(recorder);
    logContent = new LogContent(new ByteArrayContent(null, SAMPLE_UTF8), null, null,
        SAMPLE_UTF8.length, SAMPLE_UTF8.length - 1);
    logContent.writeTo(new ByteArrayOutputStream());
    recorder.assertMessages(
        "Total: 11 bytes (logging first 10 bytes)", "123\u05D9\u05e0\u05D9\ufffd");

    // Set the content logging limit to 0 to disable content logging.
    recorder = new Recorder();
    HttpTransport.LOGGER.addHandler(recorder);
    logContent =
        new LogContent(new ByteArrayContent(null, SAMPLE_UTF8), null, null, SAMPLE_UTF8.length, 0);
    logContent.writeTo(new ByteArrayOutputStream());
    recorder.assertMessages("Total: 11 bytes");

    // writeTo should behave as expected even if content length is specified to be -1.
    recorder = new Recorder();
    HttpTransport.LOGGER.addHandler(recorder);
    logContent =
        new LogContent(new ByteArrayContent(null, SAMPLE_UTF8), null, null, -1, SAMPLE_UTF8.length);
    logContent.writeTo(new ByteArrayOutputStream());
    recorder.assertMessages("Total: 11 bytes", SAMPLE);

    // Assert that an exception is thrown if content logging limit < 0.
    try {
      logContent = new LogContent(
          new ByteArrayContent(null, SAMPLE_UTF8), null, null, SAMPLE_UTF8.length, -1);
      logContent.writeTo(new ByteArrayOutputStream());
      fail("Expected: " + IllegalArgumentException.class);
    } catch (IllegalArgumentException e) {
      // Expected.
    }
  }
}
