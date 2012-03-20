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

/**
 * Proxy for version 1.6 (or newer) of the Apache Commons Codec {@link Base64} implementation.
 *
 * <p>
 * This is needed in order to support platforms like Android which already include an older version
 * of the Apache Commons Codec (Android includes version 1.3). To avoid a dependency library
 * conflict, this library includes a reduced private copy of version 1.6 (or newer) of the Apache
 * Commons Codec (using a tool like jarjar).
 * </p>
 *
 * @since 1.8
 * @author Yaniv Inbar
 */
public class Base64 {

  /**
   * Encodes binary data using the base64 algorithm but does not chunk the output.
   *
   * @param binaryData binary data to encode
   * @return byte[] containing Base64 characters in their UTF-8 representation.
   * @see org.apache.commons.codec.binary.Base64#encodeBase64(byte[])
   * @since 1.8
   */
  public static byte[] encodeBase64(byte[] binaryData) {
    return org.apache.commons.codec.binary.Base64.encodeBase64(binaryData);
  }

  /**
   * Encodes binary data using the base64 algorithm but does not chunk the output.
   *
   * @param binaryData binary data to encode
   * @return String containing Base64 characters.
   * @see org.apache.commons.codec.binary.Base64#encodeBase64String(byte[])
   * @since 1.8
   */
  public static String encodeBase64String(byte[] binaryData) {
    return org.apache.commons.codec.binary.Base64.encodeBase64String(binaryData);
  }


  /**
   * Encodes binary data using a URL-safe variation of the base64 algorithm but does not chunk the
   * output. The url-safe variation emits - and _ instead of + and / characters.
   *
   * @param binaryData binary data to encode
   * @return byte[] containing Base64 characters in their UTF-8 representation.
   * @see org.apache.commons.codec.binary.Base64#encodeBase64URLSafe(byte[])
   * @since 1.8
   */
  public static byte[] encodeBase64URLSafe(byte[] binaryData) {
    return org.apache.commons.codec.binary.Base64.encodeBase64URLSafe(binaryData);
  }

  /**
   * Encodes binary data using a URL-safe variation of the base64 algorithm but does not chunk the
   * output. The url-safe variation emits - and _ instead of + and / characters.
   *
   * @param binaryData binary data to encode
   * @return String containing Base64 characters
   * @see org.apache.commons.codec.binary.Base64#encodeBase64URLSafeString(byte[])
   * @since 1.8
   */
  public static String encodeBase64URLSafeString(byte[] binaryData) {
    return org.apache.commons.codec.binary.Base64.encodeBase64URLSafeString(binaryData);
  }

  /**
   * Decodes Base64 data into octets.
   *
   * @param base64Data Byte array containing Base64 data
   * @return Array containing decoded data.
   * @see org.apache.commons.codec.binary.Base64#decodeBase64(byte[])
   * @since 1.8
   */
  public static byte[] decodeBase64(byte[] base64Data) {
    return org.apache.commons.codec.binary.Base64.decodeBase64(base64Data);
  }

  /**
   * Decodes a Base64 String into octets.
   *
   * @param base64String String containing Base64 data
   * @return Array containing decoded data.
   * @see org.apache.commons.codec.binary.Base64#decodeBase64(String)
   * @since 1.8
   */
  public static byte[] decodeBase64(String base64String) {
    return org.apache.commons.codec.binary.Base64.decodeBase64(base64String);
  }

  private Base64() {
  }
}
