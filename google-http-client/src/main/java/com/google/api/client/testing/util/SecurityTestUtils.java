/*
 * Copyright (c) 2013 Google Inc.
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

package com.google.api.client.testing.util;

import com.google.api.client.util.Beta;
import com.google.api.client.util.SecurityUtils;
import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.KeySpec;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

/**
 * {@link Beta} <br>
 * Utilities and constants related to testing the library {@code util} package.
 *
 * @since 1.14
 * @author Yaniv Inbar
 */
@Beta
public final class SecurityTestUtils {

  private static final byte[] ENCODED_PRIVATE_KEY = {
    48, -126, 2, 118, 2, 1, 0, 48, 13, 6, 9, 42, -122, 72, -122, -9, 13, 1, 1, 1, 5, 0, 4, -126, 2,
    96, 48, -126, 2, 92, 2, 1, 0, 2, -127, -127, 0, -89, 33, 8, -124, 110, -60, 89, 8, -62, 69, 120,
    95, -59, -43, 13, -18, 123, 29, -31, 13, -80, -76, 109, -62, -79, 2, 104, -94, 76, 59, -73, -26,
    99, 123, -57, -92, -100, 116, 50, -25, 96, 53, 124, 95, 76, -59, -84, 70, 27, 0, 72, -63, 84,
    -77, -2, -107, -66, -32, -119, 27, -95, 54, -44, -89, 1, 71, 44, 7, -55, 126, 5, -78, 87, -105,
    -114, 65, -19, 58, -78, -95, 0, 118, 83, 76, -88, 2, -21, 127, 64, 74, -103, -114, -127, -70,
    -81, -127, 125, -37, 21, 113, 20, -102, 46, -37, -111, -97, 97, -127, 32, 87, -80, 105, 18, -19,
    107, -73, -50, -97, 11, -23, -59, -107, -107, 83, -25, 15, -93, -21, 2, 3, 1, 0, 1, 2, -127,
    -128, 45, -34, -104, 26, -40, -41, -44, -29, -35, -123, -7, -110, -73, -106, 80, -5, -118, 24,
    -38, 66, -54, -93, -54, -104, 43, -62, -48, 122, -14, -41, 85, 18, -53, 109, 22, -113, 44, 77,
    -116, 7, 10, -43, -61, 43, -40, -61, 76, 19, -11, -89, 47, 80, -72, 113, -86, 70, -23, 27, 113,
    37, -1, 42, 48, 84, -80, 30, 86, 36, -124, -22, 79, -44, 87, -40, 31, -41, -44, -16, -74, 85,
    61, -122, -22, 10, -31, 78, 92, -123, -77, 12, -80, 62, -52, 68, -46, -17, 67, 124, -78, -23,
    -105, -77, -2, 89, -16, -12, -56, -51, 26, 102, 46, 39, -61, -13, -79, -65, -5, 126, 70, 29, 31,
    104, -109, 65, -23, -69, 23, -7, 2, 65, 0, -42, 18, 101, 10, -21, 37, 107, -3, -114, -29, -40,
    76, 107, -122, 40, 8, -58, -32, -12, 55, -4, -61, -66, 91, -56, -50, 78, -124, 11, -49, -62,
    -121, -56, 70, -92, 90, 32, -112, 49, 26, -99, 113, 44, 26, 42, -99, -40, -123, 17, 93, 114,
    125, 35, -118, -32, 125, -64, 61, 58, -58, -105, -105, -39, 93, 2, 65, 0, -57, -36, -22, -107,
    -42, -79, 0, -118, 121, -76, 120, 52, 110, 127, 115, 68, -86, -4, 96, -50, 72, -60, -57, 125,
    57, 21, -81, -44, 25, 112, -75, 83, 57, -55, 61, 24, 28, -112, -103, -8, 120, 110, -52, -108,
    -41, -76, -96, 87, -117, 69, 0, 64, 26, 4, 122, 13, 6, -106, 112, -51, -1, 79, 117, -25, 2, 64,
    127, 68, 60, 81, -5, 110, 41, -1, 122, 93, -74, -113, -24, 52, -65, -60, 72, 8, 32, -24, -48,
    26, -57, 38, -26, 0, -48, -24, -21, -28, -66, 47, -33, 63, 48, 34, 108, -51, -116, -125, -40,
    42, 26, 32, 12, 73, -1, 25, 77, 51, -109, 7, 22, -124, 79, -26, 50, -51, -76, 13, -80, -66, 19,
    -7, 2, 65, 0, -90, 99, -20, 68, -4, -84, -11, -105, 83, -123, -124, -63, -103, -16, -81, 101,
    78, -72, -72, 91, 100, -57, -74, -111, 49, 18, 54, 4, -19, 125, 32, -24, 125, -26, 100, -33,
    -117, 0, 115, -65, 33, 124, -107, 3, -95, -91, 118, 12, 12, 29, 80, -3, 12, -20, 7, 52, -118,
    -12, 122, 75, 117, -81, -112, -89, 2, 64, 93, -21, -52, -110, -54, -9, 79, -123, 105, 125, -56,
    75, -77, -26, 125, -123, -69, 62, -2, 79, 8, 72, -76, -67, 5, 33, -121, 1, -42, -17, 29, 69,
    -20, -68, -26, -23, 95, -7, -70, -50, -10, 58, 16, -15, -89, -24, -121, -14, -72, -127, -89,
    -63, 66, 7, 77, -89, -54, -95, -90, 45, -44, -118, 69, -1
  };

  private static final byte[] ENCODED_PUBLIC_KEY = {
    48, -127, -97, 48, 13, 6, 9, 42, -122, 72, -122, -9, 13, 1, 1, 1, 5, 0, 3, -127, -115, 0, 48,
    -127, -119, 2, -127, -127, 0, -89, 33, 8, -124, 110, -60, 89, 8, -62, 69, 120, 95, -59, -43, 13,
    -18, 123, 29, -31, 13, -80, -76, 109, -62, -79, 2, 104, -94, 76, 59, -73, -26, 99, 123, -57,
    -92, -100, 116, 50, -25, 96, 53, 124, 95, 76, -59, -84, 70, 27, 0, 72, -63, 84, -77, -2, -107,
    -66, -32, -119, 27, -95, 54, -44, -89, 1, 71, 44, 7, -55, 126, 5, -78, 87, -105, -114, 65, -19,
    58, -78, -95, 0, 118, 83, 76, -88, 2, -21, 127, 64, 74, -103, -114, -127, -70, -81, -127, 125,
    -37, 21, 113, 20, -102, 46, -37, -111, -97, 97, -127, 32, 87, -80, 105, 18, -19, 107, -73, -50,
    -97, 11, -23, -59, -107, -107, 83, -25, 15, -93, -21, 2, 3, 1, 0, 1
  };

  /**
   * Returns a new copy of a sample encoded RSA private key that matches {@link
   * #newEncodedRsaPublicKeyBytes()}.
   */
  public static byte[] newEncodedRsaPrivateKeyBytes() {
    return ENCODED_PRIVATE_KEY.clone();
  }

  /**
   * Returns a new copy of a sample encoded public key that matches {@link
   * #newEncodedRsaPrivateKeyBytes()}.
   */
  public static byte[] newEncodedRsaPublicKeyBytes() {
    return ENCODED_PUBLIC_KEY.clone();
  }

  /** Returns a new sample RSA private key that matches {@link #newRsaPublicKey()}. */
  public static RSAPrivateKey newRsaPrivateKey() throws GeneralSecurityException {
    KeyFactory keyFactory = SecurityUtils.getRsaKeyFactory();
    KeySpec keySpec = new PKCS8EncodedKeySpec(ENCODED_PRIVATE_KEY);
    return (RSAPrivateKey) keyFactory.generatePrivate(keySpec);
  }

  /** Returns a new sample RSA public key that matches {@link #newRsaPrivateKey()}. */
  public static RSAPublicKey newRsaPublicKey() throws GeneralSecurityException {
    KeyFactory keyFactory = SecurityUtils.getRsaKeyFactory();
    KeySpec keySpec = new X509EncodedKeySpec(ENCODED_PUBLIC_KEY);
    return (RSAPublicKey) keyFactory.generatePublic(keySpec);
  }

  private SecurityTestUtils() {}
}
