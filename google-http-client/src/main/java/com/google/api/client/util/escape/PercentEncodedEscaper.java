package com.google.api.client.util.escape;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * An {@link Escaper} implementation that preserves percent-encoded sequences in the input string.
 *
 * <p>This escaper applies the provided {@link Escaper} to all parts of the input string except for
 * valid percent-encoded sequences (e.g., <code>%20</code>), which are left unchanged.
 */
final class PercentEncodedEscaper extends Escaper {

  /** Pattern to match valid percent-encoded sequences (e.g., %20). */
  static final Pattern PCT_ENCODE_PATTERN = Pattern.compile("%[0-9A-Fa-f]{2}");

  private final Escaper escaper;

  public PercentEncodedEscaper(Escaper escaper) {
    if (escaper == null) {
      throw new NullPointerException("Escaper cannot be null");
    }
    this.escaper = escaper;
  }

  /**
   * Escapes the input string using the provided {@link Escaper}, preserving valid percent-encoded
   * sequences.
   *
   * @param string the input string to escape
   * @return the escaped string with percent-encoded sequences left unchanged
   */
  @Override
  public String escape(String string) {
    if (string == null || string.isEmpty()) {
      return string;
    }

    Matcher matcher = PCT_ENCODE_PATTERN.matcher(string);
    StringBuilder sb = new StringBuilder();

    int lastEnd = 0;
    while (matcher.find()) {
      sb.append(escaper.escape(string.substring(lastEnd, matcher.start())));

      sb.append(string.substring(matcher.start(), matcher.end()));

      lastEnd = matcher.end();
    }

    if (lastEnd < string.length()) {
      sb.append(escaper.escape(string.substring(lastEnd)));
    }

    return sb.toString();
  }
}
