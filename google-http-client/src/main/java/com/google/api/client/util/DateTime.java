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

import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;

/**
 * Immutable representation of a date with an optional time and an optional time zone based on RFC
 * 3339.
 *
 * <p>
 * Implementation is immutable and therefore thread-safe.
 * </p>
 *
 * @since 1.0
 * @author Yaniv Inbar
 */
public final class DateTime implements Serializable {

  private static final long serialVersionUID = 1L;

  private static final TimeZone GMT = TimeZone.getTimeZone("GMT");

  /**
   * Date/time value expressed as the number of ms since the Unix epoch.
   *
   * <p>
   * If the time zone is specified, this value is normalized to UTC, so to format this date/time
   * value, the time zone shift has to be applied.
   * </p>
   */
  private final long value;

  /** Specifies whether this is a date-only value. */
  private final boolean dateOnly;

  /**
   * Time zone shift from UTC in minutes. If {@code null}, no time zone is set, and the time always
   * interpreted as local time.
   */
  private final int tzShift;

  /**
   * Instantiates {@link DateTime} from a {@link Date} and {@link TimeZone}.
   *
   * @param date date and time
   * @param zone time zone; if {@code null}, it is interpreted as {@code TimeZone.getDefault()}.
   */
  public DateTime(Date date, TimeZone zone) {
    this(false, date.getTime(), zone == null ? null : zone.getOffset(date.getTime()) / 60000);
  }

  /**
   * Instantiates {@link DateTime} from the number of milliseconds since the Unix epoch.
   *
   * <p>
   * The time zone is interpreted as {@code TimeZone.getDefault()}, which may vary with
   * implementation.
   * </p>
   *
   * @param value number of milliseconds since the Unix epoch (January 1, 1970, 00:00:00 GMT)
   */
  public DateTime(long value) {
    this(false, value, null);
  }

  /**
   * Instantiates {@link DateTime} from a {@link Date}.
   *
   * <p>
   * The time zone is interpreted as {@code TimeZone.getDefault()}, which may vary with
   * implementation.
   * </p>
   *
   * @param value date and time
   */
  public DateTime(Date value) {
    this(value.getTime());
  }

  /**
   * Instantiates {@link DateTime} from the number of milliseconds since the Unix epoch, and a shift
   * from UTC in minutes.
   *
   * @param value number of milliseconds since the Unix epoch (January 1, 1970, 00:00:00 GMT)
   * @param tzShift time zone, represented by the number of minutes off of UTC.
   */
  public DateTime(long value, int tzShift) {
    this(false, value, tzShift);
  }

  /**
   * Instantiates {@link DateTime}, which may represent a date-only value, from the number of
   * milliseconds since the Unix epoch, and a shift from UTC in minutes.
   *
   * @param dateOnly specifies if this should represent a date-only value
   * @param value number of milliseconds since the Unix epoch (January 1, 1970, 00:00:00 GMT)
   * @param tzShift time zone, represented by the number of minutes off of UTC, or {@code null} for
   *        {@code TimeZone.getDefault()}.
   */
  public DateTime(boolean dateOnly, long value, Integer tzShift) {
    this.dateOnly = dateOnly;
    this.value = value;
    this.tzShift = tzShift == null ? TimeZone.getDefault().getOffset(value) / 60000 : tzShift;
  }

  /**
   * Instantiates {@link DateTime} from an <a href='http://tools.ietf.org/html/rfc3339'>RFC 3339</a>
   * date/time value.
   *
   * @param value an <a href='http://tools.ietf.org/html/rfc3339'>RFC 3339</a> date/time value.
   * @since 1.11
   */
  public DateTime(String value) {
    // TODO(rmistry): Move the implementation of parseRfc3339 into this constructor. Implementation
    // of parseRfc3339 can then do "return new DateTime(str);".
    DateTime dateTime = parseRfc3339(value);
    this.dateOnly = dateTime.dateOnly;
    this.value = dateTime.value;
    this.tzShift = dateTime.tzShift;
  }

  /**
   * Returns the date/time value expressed as the number of milliseconds since the Unix epoch.
   *
   * <p>
   * If the time zone is specified, this value is normalized to UTC, so to format this date/time
   * value, the time zone shift has to be applied.
   * </p>
   *
   * @since 1.5
   */
  public long getValue() {
    return value;
  }

  /**
   * Returns whether this is a date-only value.
   *
   * @since 1.5
   */
  public boolean isDateOnly() {
    return dateOnly;
  }

  /**
   * Returns the time zone shift from UTC in minutes
   *
   * <p>
   * Upgrade warning: In the previous version this method would return a boxed {@code Integer},
   * whereas it now returns a primitive {@code int}. Before, this method would return {@code null}
   * to represent the local time zone, but it now always returns the shift of the local time zone
   * from UTC.
   * </p>
   *
   * @since 1.5
   */
  public int getTimeZoneShift() {
    return tzShift;
  }

  /** Formats the value as an RFC 3339 date/time string. */
  public String toStringRfc3339() {

    StringBuilder sb = new StringBuilder();

    Calendar dateTime = new GregorianCalendar(GMT);
    long localTime = value + (tzShift * 60000);

    dateTime.setTimeInMillis(localTime);

    appendInt(sb, dateTime.get(Calendar.YEAR), 4);
    sb.append('-');
    appendInt(sb, dateTime.get(Calendar.MONTH) + 1, 2);
    sb.append('-');
    appendInt(sb, dateTime.get(Calendar.DAY_OF_MONTH), 2);

    if (!dateOnly) {

      sb.append('T');
      appendInt(sb, dateTime.get(Calendar.HOUR_OF_DAY), 2);
      sb.append(':');
      appendInt(sb, dateTime.get(Calendar.MINUTE), 2);
      sb.append(':');
      appendInt(sb, dateTime.get(Calendar.SECOND), 2);

      if (dateTime.isSet(Calendar.MILLISECOND)) {
        sb.append('.');
        appendInt(sb, dateTime.get(Calendar.MILLISECOND), 3);
      }
    }

    if (tzShift == 0) {

      sb.append('Z');

    } else {

      int absTzShift = tzShift;
      if (tzShift > 0) {
        sb.append('+');
      } else {
        sb.append('-');
        absTzShift = -absTzShift;
      }

      int tzHours = absTzShift / 60;
      int tzMinutes = absTzShift % 60;
      appendInt(sb, tzHours, 2);
      sb.append(':');
      appendInt(sb, tzMinutes, 2);
    }

    return sb.toString();
  }

  @Override
  public String toString() {
    return toStringRfc3339();
  }

  /**
   * {@inheritDoc}
   *
   * <p>
   * A check is added that the time zone is the same. If you ONLY want to check equality of time
   * value, check equality on the {@link #getValue()}.
   * </p>
   */
  @Override
  public boolean equals(Object o) {
    if (o == this) {
      return true;
    }
    if (!(o instanceof DateTime)) {
      return false;
    }
    DateTime other = (DateTime) o;
    return dateOnly == other.dateOnly && value == other.value && tzShift == other.tzShift;
  }

  /**
   * Parses an RFC 3339 date/time value.
   *
   * <p>
   * For the date-only case, the time zone is ignored and the hourOfDay, minute, second, and
   * millisecond parameters are set to zero.
   * </p>
   */
  public static DateTime parseRfc3339(String str) throws NumberFormatException {
    try {
      int year = Integer.parseInt(str.substring(0, 4));
      int month = Integer.parseInt(str.substring(5, 7)) - 1;
      int day = Integer.parseInt(str.substring(8, 10));
      int tzIndex;
      int length = str.length();
      boolean dateOnly = length <= 10 || Character.toUpperCase(str.charAt(10)) != 'T';
      int hourOfDay = 0;
      int minute = 0;
      int second = 0;
      int milliseconds = 0;
      Integer tzShiftInteger = null;
      if (dateOnly) {
        tzIndex = Integer.MAX_VALUE;
      } else {
        hourOfDay = Integer.parseInt(str.substring(11, 13));
        minute = Integer.parseInt(str.substring(14, 16));
        second = Integer.parseInt(str.substring(17, 19));
        if (str.charAt(19) == '.') {
          milliseconds = Integer.parseInt(str.substring(20, 23));
          tzIndex = 23;
        } else {
          tzIndex = 19;
        }
      }
      Calendar dateTime = new GregorianCalendar(GMT);
      dateTime.set(year, month, day, hourOfDay, minute, second);
      dateTime.set(Calendar.MILLISECOND, milliseconds);
      long value = dateTime.getTimeInMillis();
      if (length > tzIndex) {
        int tzShift;
        if (Character.toUpperCase(str.charAt(tzIndex)) == 'Z') {
          tzShift = 0;
        } else {
          tzShift = Integer.parseInt(str.substring(tzIndex + 1, tzIndex + 3)) * 60
              + Integer.parseInt(str.substring(tzIndex + 4, tzIndex + 6));
          if (str.charAt(tzIndex) == '-') {
            tzShift = -tzShift;
          }
          value -= tzShift * 60000;
        }
        tzShiftInteger = tzShift;
      }
      return new DateTime(dateOnly, value, tzShiftInteger);
    } catch (StringIndexOutOfBoundsException e) {
      throw new NumberFormatException("Invalid date/time format.");
    }
  }

  /** Appends a zero-padded number to a string builder. */
  private static void appendInt(StringBuilder sb, int num, int numDigits) {
    if (num < 0) {
      sb.append('-');
      num = -num;
    }
    int x = num;
    while (x > 0) {
      x /= 10;
      numDigits--;
    }
    for (int i = 0; i < numDigits; i++) {
      sb.append('0');
    }
    if (num != 0) {
      sb.append(num);
    }
  }
}
