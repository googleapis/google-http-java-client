/*
 * Copyright 2020 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.api.client.json.webtoken;

import com.google.api.client.util.Preconditions;
import java.math.BigInteger;
import java.util.Arrays;

/**
 * Utilities for re-encoding a signature byte array with DER encoding.
 *
 * <p>Note: that this is not a general purpose encoder and currently only handles 512 bit
 * signatures. ES256 verification algorithms expect the signature bytes in DER encoding.
 */
public class DerEncoder {
  private static byte DER_TAG_SIGNATURE_OBJECT = 0x30;
  private static byte DER_TAG_ASN1_INTEGER = 0x02;

  static byte[] encode(byte[] signature) {
    // expect the signature to be 64 bytes long
    Preconditions.checkState(signature.length == 64);

    byte[] int1 = new BigInteger(1, Arrays.copyOfRange(signature, 0, 32)).toByteArray();
    byte[] int2 = new BigInteger(1, Arrays.copyOfRange(signature, 32, 64)).toByteArray();
    byte[] der = new byte[6 + int1.length + int2.length];

    // Mark that this is a signature object
    der[0] = DER_TAG_SIGNATURE_OBJECT;
    der[1] = (byte) (der.length - 2);

    // Start ASN1 integer and write the first 32 bits
    der[2] = DER_TAG_ASN1_INTEGER;
    der[3] = (byte) int1.length;
    System.arraycopy(int1, 0, der, 4, int1.length);

    // Start ASN1 integer and write the second 32 bits
    int offset = int1.length + 4;
    der[offset] = DER_TAG_ASN1_INTEGER;
    der[offset + 1] = (byte) int2.length;
    System.arraycopy(int2, 0, der, offset + 2, int2.length);

    return der;
  }
}
