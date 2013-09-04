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

import junit.framework.TestCase;

import java.util.Date;
import java.util.TimeZone;

/**
 * Tests {@link DateTime}.
 *
 * @author Yaniv Inbar
 */
public class DateTimeTest extends TestCase {

  private TimeZone originalTimeZone;

  public DateTimeTest() {
  }

  public DateTimeTest(String testName) {
    super(testName);
  }

  @Override
  protected void setUp() throws Exception {
    originalTimeZone = TimeZone.getDefault();
  }

  @Override
  protected void tearDown() throws Exception {
    TimeZone.setDefault(originalTimeZone);
  }

  public void testToStringRfc3339() {
    TimeZone.setDefault(TimeZone.getTimeZone("GMT-4"));

    assertEquals("Check with explicit Date and Timezone.",
        "2012-11-06T12:10:44.000-08:00",
        new DateTime(new Date(1352232644000L), TimeZone.getTimeZone("GMT-8")).toStringRfc3339());

    assertEquals("Check with explicit Date but no explicit Timezone.",
        "2012-11-06T16:10:44.000-04:00",
        new DateTime(new Date(1352232644000L)).toStringRfc3339());

    assertEquals("Check with explicit Date and Timezone-Shift.",
        "2012-11-06T17:10:44.000-03:00",
        new DateTime(1352232644000L, -180).toStringRfc3339());

    assertEquals("Check with explicit Date and Zulu Timezone Offset.",
        "2012-11-06T20:10:44.000Z",
        new DateTime(1352232644000L, 0).toStringRfc3339());

    TimeZone.setDefault(TimeZone.getTimeZone("UTC"));

    assertEquals("Check with explicit Date but no explicit Timezone.",
        "2012-11-06T20:10:44.000Z",
        new DateTime(new Date(1352232644000L)).toStringRfc3339());
  }

  public void testToStringRfc3339_dateOnly() {
    for (String timeZoneString : new String[]{"GMT-4", "UTC", "UTC-7"}) {
      TimeZone.setDefault(TimeZone.getTimeZone(timeZoneString));
      assertEquals(
          "2012-11-06",
          new DateTime(true, 1352232644000L, 1).toStringRfc3339());
      assertEquals(
          "2012-11-06",
          new DateTime(true, 1352232644000L, null).toStringRfc3339());
      assertEquals("2000-01-01", new DateTime("2000-01-01").toStringRfc3339());
    }
  }

  public void testEquals() throws InterruptedException {
    assertFalse("Check equals with two different tz specified.",
        new DateTime(1234567890L).equals(new DateTime(1234567890L, 120)));
    assertTrue("Check equals with two identical tz specified.",
        new DateTime(1234567890L, -240).equals(new DateTime(1234567890L, -240)));
    assertFalse("Check equals with two different tz specified.",
        new DateTime(1234567890L, 60).equals(new DateTime(1234567890L, 240)));

    assertFalse("Check not equal.", new DateTime(1234567890L).equals(new DateTime(9876543210L)));
    assertFalse("Check not equal with tz.",
        new DateTime(1234567890L, 120).equals(new DateTime(9876543210L, 120)));
    assertFalse(
        "Check not equal with Date.", new DateTime(1234567890L).equals(new Date(9876543210L)));

    DateTime dateTime1 = new DateTime("2011-01-01");
    Thread.sleep(10);
    DateTime dateTime2 = new DateTime("2011-01-01");
    assertEquals(dateTime1, dateTime2);
  }

  public void testParseRfc3339() {
    expectExceptionForParseRfc3339("");
    expectExceptionForParseRfc3339("abc");
    expectExceptionForParseRfc3339("2013-01-01 09:00:02");
    expectExceptionForParseRfc3339("2013-01-01T"); // missing time
    expectExceptionForParseRfc3339("1937--3-55T12:00:27+00:20"); // invalid month
    expectExceptionForParseRfc3339("2013-01-01Z"); // can't have time zone shift without time

    DateTime value = DateTime.parseRfc3339("2007-06-01");
    assertTrue(value.isDateOnly());
    value = DateTime.parseRfc3339("2007-06-01T10:11:30.057");
    assertFalse(value.isDateOnly());
    value = DateTime.parseRfc3339("2007-06-01T10:11:30Z");
    assertEquals(0, value.getValue() % 100);
    value = DateTime.parseRfc3339("1997-01-01T12:00:27.87+00:20");
    assertFalse(value.isDateOnly());
    assertEquals(87, value.getValue() % 1000); // check milliseconds

    value = new DateTime("2007-06-01");
    assertTrue(value.isDateOnly());
    value = new DateTime("2007-06-01T10:11:30.057");
    assertFalse(value.isDateOnly());
    value = new DateTime("2007-06-01T10:11:30Z");
    assertEquals(0, value.getValue() % 100);

    // From the RFC3339 Standard
    assertEquals(DateTime.parseRfc3339("1996-12-19T16:39:57-08:00").getValue(),
        DateTime.parseRfc3339("1996-12-20T00:39:57Z").getValue()); // from Section 5.8 Examples
    assertEquals(DateTime.parseRfc3339("1990-12-31T23:59:60Z").getValue(),
        DateTime.parseRfc3339("1990-12-31T15:59:60-08:00").getValue()); // from Section 5.8 Examples
    assertEquals(DateTime.parseRfc3339("2007-06-01t18:50:00-04:00").getValue(),
        DateTime.parseRfc3339("2007-06-01t22:50:00Z").getValue()); // from Section 4.2 Local Offsets
  }

  private void expectExceptionForParseRfc3339(String input) {
    try {
      DateTime.parseRfc3339(input);
      fail("expected NumberFormatException");
    } catch (NumberFormatException e) {
      // expected
    }
  }
}
