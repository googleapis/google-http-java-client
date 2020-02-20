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

package com.google.api.client.util.escape;

/**
 * A {@code UnicodeEscaper} that escapes some set of Java characters using the URI percent encoding
 * scheme. The set of safe characters (those which remain unescaped) is specified on
 * construction.
 *
 * <p>For details on escaping URIs for use in web pages, see <a
 * href="http://tools.ietf.org/html/rfc3986#section-2.4">RFC 3986 - section 2.4</a> and <a
 * href="http://tools.ietf.org/html/rfc3986#appendix-A">RFC 3986 - appendix A</a>
 *
 * <p>When encoding a String, the following rules apply:
 *
 * <ul>
 *   <li>The alphanumeric characters "a" through "z", "A" through "Z" and "0" through "9" remain the
 *       same.
 *   <li>Any additionally specified safe characters remain the same.
 *   <li>If {@code plusForSpace} is true, the space character " " is converted into a plus
 *       sign "+".
 *   <li>All other characters are converted into one or more bytes using UTF-8 encoding. Each
 *       byte is then represented by the 3-character string "%XY", where "XY" is the two-digit,
 *       uppercase, hexadecimal representation of the byte value.
 * </ul>
 *
 * <p>RFC 3986 defines the set of unreserved characters as "-", "_", "~", and "."
 * It goes on to state:
 *
 * <p><q>URIs that differ in the replacement of an unreserved character with
   its corresponding percent-encoded US-ASCII octet are equivalent: they
   identify the same resource.  However, URI comparison implementations
   do not always perform normalization prior to comparison (see Section
   6).  For consistency, percent-encoded octets in the ranges of ALPHA
   (%41-%5A and %61-%7A), DIGIT (%30-%39), hyphen (%2D), period (%2E),
   underscore (%5F), or tilde (%7E) should not be created by URI
   producers and, when found in a URI, should be decoded to their
   corresponding unreserved characters by URI normalizers.</q>
 *
 * <p><b>Note</b>: This escaper produces uppercase hexadecimal sequences. From <a
 * href="https://tools.ietf.org/html/rfc3986">RFC 3986</a>:<br>
 * <i>"URI producers and normalizers should use uppercase hexadecimal digits for all
 * percent-encodings."</i>
 *
 * @since 1.0
 */
public class PercentEscaper extends UnicodeEscaper {
  /** A string of safe characters that mimics the behavior of {@link java.net.URLEncoder}. */
  public static final String SAFECHARS_URLENCODER = "-_.*";

  /**
   * A string of characters that do not need to be encoded when used in URI path segments, as
   * specified in RFC 3986. Note that some of these characters do need to be escaped when used in
   * other parts of the URI.
   */
  public static final String SAFEPATHCHARS_URLENCODER = "-_.!~*'()@:$&,;=+";

  /**
   * Contains the safe characters plus all reserved characters. This happens to be the safe path
   * characters plus those characters which are reserved for URI segments, namely '/' and '?'.
   */
  public static final String SAFE_PLUS_RESERVED_CHARS_URLENCODER = SAFEPATHCHARS_URLENCODER + "/?";

  /**
   * A string of characters that do not need to be encoded when used in URI user info part, as
   * specified in RFC 3986. Note that some of these characters do need to be escaped when used in
   * other parts of the URI.
   *
   * @since 1.15
   */
  public static final String SAFEUSERINFOCHARS_URLENCODER = "-_.!~*'():$&,;=";

  /**
   * A string of characters that do not need to be encoded when used in URI query strings, as
   * specified in RFC 3986. Note that some of these characters do need to be escaped when used in
   * other parts of the URI.
   */
  public static final String SAFEQUERYSTRINGCHARS_URLENCODER = "-_.!~*'()@:$,;/?:";

  // In some uri escapers spaces are escaped to '+'
  private static final char[] URI_ESCAPED_SPACE = {'+'};

  private static final char[] UPPER_HEX_DIGITS = "0123456789ABCDEF".toCharArray();

  /** If true we should convert space to the {@code +} character. */
  private final boolean plusForSpace;

  /**
   * An array of flags where for any {@code char c} if {@code safeOctets[c]} is true then {@code c}
   * should remain unmodified in the output. If {@code c > safeOctets.length} then it should be
   * escaped.
   */
  private final boolean[] safeOctets;
  
  /**
   * Constructs a URI escaper with the specified safe characters. The space
   * character is escaped to %20 in accordance with the URI specification. 
   *
   * @param safeChars a non null string specifying additional safe characters for this escaper (the
   *     ranges 0..9, a..z and A..Z are always safe and should not be specified here)
   * @throws IllegalArgumentException if any of the parameters are invalid
   */
  public PercentEscaper(String safeChars) {
    this(safeChars, false);
  }

  /**
   * Constructs a URI escaper that converts all but the specified safe characters 
   * into hexadecimal percent escapes. Optionally space characters can be converted into
   * a plus sign {@code +} instead of {@code %20}. and optional handling of the space
   *
   * @param safeChars a non null string specifying additional safe characters for this escaper. The
   *     ranges 0..9, a..z and A..Z are always safe and should not be specified here.
   * @param plusForSpace true if ASCII space should be escaped to {@code +} rather than {@code %20}
   * @throws IllegalArgumentException if safeChars includes characters that are always safe or
   *     characters that must always be escaped
   * @deprecated use {@code PercentEscaper(String safeChars)} instead which is the same as invoking
   *     this method with plusForSpace set to false. Escaping spaces as plus signs does not
   *     conform to the URI specification.
   */
  @Deprecated
  public PercentEscaper(String safeChars, boolean plusForSpace) {
    // Avoid any misunderstandings about the behavior of this escaper
    if (safeChars.matches(".*[0-9A-Za-z].*")) {
      throw new IllegalArgumentException(
          "Alphanumeric ASCII characters are always 'safe' and should not be " + "escaped.");
    }
    // Avoid ambiguous parameters. Safe characters are never modified so if
    // space is a safe character then setting plusForSpace is meaningless.
    if (plusForSpace && safeChars.contains(" ")) {
      throw new IllegalArgumentException(
          "plusForSpace cannot be specified when space is a 'safe' character");
    }
    if (safeChars.contains("%")) {
      throw new IllegalArgumentException("The '%' character cannot be specified as 'safe'");
    }
    this.plusForSpace = plusForSpace;
    safeOctets = createSafeOctets(safeChars);
  }

  /**
   * Creates a boolean[] with entries corresponding to the character values for 0-9, A-Z, a-z and
   * those specified in safeChars set to true. The array is as small as is required to hold the
   * given character information.
   */
  private static boolean[] createSafeOctets(String safeChars) {
    int maxChar = 'z';
    char[] safeCharArray = safeChars.toCharArray();
    for (char c : safeCharArray) {
      maxChar = Math.max(c, maxChar);
    }
    boolean[] octets = new boolean[maxChar + 1];
    for (int c = '0'; c <= '9'; c++) {
      octets[c] = true;
    }
    for (int c = 'A'; c <= 'Z'; c++) {
      octets[c] = true;
    }
    for (int c = 'a'; c <= 'z'; c++) {
      octets[c] = true;
    }
    for (char c : safeCharArray) {
      octets[c] = true;
    }
    return octets;
  }

  /*
   * Overridden for performance. For unescaped strings this improved the performance of the uri
   * escaper from ~760ns to ~400ns as measured by {@link CharEscapersBenchmark}.
   */
  @Override
  protected int nextEscapeIndex(CharSequence csq, int index, int end) {
    for (; index < end; index++) {
      char c = csq.charAt(index);
      if (c >= safeOctets.length || !safeOctets[c]) {
        break;
      }
    }
    return index;
  }

  /*
   * Overridden for performance. For unescaped strings this improved the performance of the uri
   * escaper from ~400ns to ~170ns as measured by {@link CharEscapersBenchmark}.
   */
  @Override
  public String escape(String s) {
    int slen = s.length();
    for (int index = 0; index < slen; index++) {
      char c = s.charAt(index);
      if (c >= safeOctets.length || !safeOctets[c]) {
        return escapeSlow(s, index);
      }
    }
    return s;
  }

  /** Escapes the given Unicode code point in UTF-8. */
  @Override
  protected char[] escape(int cp) {
    // We should never get negative values here but if we do it will throw an
    // IndexOutOfBoundsException, so at least it will get spotted.
    if (cp < safeOctets.length && safeOctets[cp]) {
      return null;
    } else if (cp == ' ' && plusForSpace) {
      return URI_ESCAPED_SPACE;
    } else if (cp <= 0x7F) {
      // Single byte UTF-8 characters
      // Start with "%--" and fill in the blanks
      char[] dest = new char[3];
      dest[0] = '%';
      dest[2] = UPPER_HEX_DIGITS[cp & 0xF];
      dest[1] = UPPER_HEX_DIGITS[cp >>> 4];
      return dest;
    } else if (cp <= 0x7ff) {
      // Two byte UTF-8 characters [cp >= 0x80 && cp <= 0x7ff]
      // Start with "%--%--" and fill in the blanks
      char[] dest = new char[6];
      dest[0] = '%';
      dest[3] = '%';
      dest[5] = UPPER_HEX_DIGITS[cp & 0xF];
      cp >>>= 4;
      dest[4] = UPPER_HEX_DIGITS[0x8 | cp & 0x3];
      cp >>>= 2;
      dest[2] = UPPER_HEX_DIGITS[cp & 0xF];
      cp >>>= 4;
      dest[1] = UPPER_HEX_DIGITS[0xC | cp];
      return dest;
    } else if (cp <= 0xffff) {
      // Three byte UTF-8 characters [cp >= 0x800 && cp <= 0xffff]
      // Start with "%E-%--%--" and fill in the blanks
      char[] dest = new char[9];
      dest[0] = '%';
      dest[1] = 'E';
      dest[3] = '%';
      dest[6] = '%';
      dest[8] = UPPER_HEX_DIGITS[cp & 0xF];
      cp >>>= 4;
      dest[7] = UPPER_HEX_DIGITS[0x8 | cp & 0x3];
      cp >>>= 2;
      dest[5] = UPPER_HEX_DIGITS[cp & 0xF];
      cp >>>= 4;
      dest[4] = UPPER_HEX_DIGITS[0x8 | cp & 0x3];
      cp >>>= 2;
      dest[2] = UPPER_HEX_DIGITS[cp];
      return dest;
    } else if (cp <= 0x10ffff) {
      char[] dest = new char[12];
      // Four byte UTF-8 characters [cp >= 0xffff && cp <= 0x10ffff]
      // Start with "%F-%--%--%--" and fill in the blanks
      dest[0] = '%';
      dest[1] = 'F';
      dest[3] = '%';
      dest[6] = '%';
      dest[9] = '%';
      dest[11] = UPPER_HEX_DIGITS[cp & 0xF];
      cp >>>= 4;
      dest[10] = UPPER_HEX_DIGITS[0x8 | cp & 0x3];
      cp >>>= 2;
      dest[8] = UPPER_HEX_DIGITS[cp & 0xF];
      cp >>>= 4;
      dest[7] = UPPER_HEX_DIGITS[0x8 | cp & 0x3];
      cp >>>= 2;
      dest[5] = UPPER_HEX_DIGITS[cp & 0xF];
      cp >>>= 4;
      dest[4] = UPPER_HEX_DIGITS[0x8 | cp & 0x3];
      cp >>>= 2;
      dest[2] = UPPER_HEX_DIGITS[cp & 0x7];
      return dest;
    } else {
      // If this ever happens it is due to bug in UnicodeEscaper, not bad input.
      throw new IllegalArgumentException("Invalid unicode character value " + cp);
    }
  }
}
