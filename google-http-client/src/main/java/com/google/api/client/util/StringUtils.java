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

import java.io.UnsupportedEncodingException;
import com.google.api.client.util.Charsets;

/**
 * Utilities for strings.
 *
 * @since 1.8
 * @author Yaniv Inbar
 */
public class StringUtils {

  /**
   * Line separator to use for this OS, i.e. {@code "\n"} or {@code "\r\n"}.
   *
   * @since 1.8
   */
  public static final String LINE_SEPARATOR = System.getProperty("line.separator");

  /**
   * Encodes the given string into a sequence of bytes using the UTF-8 charset, storing the result
   * into a new byte array.
   *
   * @param string the String to encode, may be <code>null</code>
   * @return encoded bytes, or <code>null</code> if the input string was <code>null</code>
   * @throws IllegalStateException Thrown when the charset is missing, which should be never
   *     according the Java specification.
   * @see <a href="http://download.oracle.com/javase/7/docs/api/java/nio/charset/Charset.html"
   *     >Standard charsets</a>
   * @since 1.8
   */
  public static byte[] getBytesUtf8(String string) {
    if (string == null) {
      return null;
    }
    return string.getBytes(Charsets.UTF_8);
  }

  /**
   * Constructs a new <code>String</code> by decoding the specified array of bytes using the UTF-8
   * charset.
   *
   * @param bytes The bytes to be decoded into characters
   * @return A new <code>String</code> decoded from the specified array of bytes using the UTF-8
   *     charset, or <code>null</code> if the input byte array was <code>null</code>.
   * @throws IllegalStateException Thrown when a {@link UnsupportedEncodingException} is caught,
   *     which should never happen since the charset is required.
   * @since 1.8
   */
  public static String newStringUtf8(byte[] bytes) {
    if (bytes == null) {
      return null;
    }
    return new String(bytes, Charsets.UTF_8);
  }

  private StringUtils() {}
}
