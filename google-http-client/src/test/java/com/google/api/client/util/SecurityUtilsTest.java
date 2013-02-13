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

import com.google.api.client.testing.util.SecurityTestUtils;

import junit.framework.TestCase;
import org.junit.Assert;

import java.io.InputStream;
import java.nio.charset.Charset;
import java.security.PrivateKey;

/**
 * Tests {@link SecurityUtils}.
 *
 * @author Yaniv Inbar
 */
public class SecurityUtilsTest extends TestCase {

  private static final byte[] SIGNED = {-94, -69, 90, 80, 13, 1, 95, -118, 109, 86, -13, 19, 121,
    -99, -49, -110, -125, 120, 121, 113, -107, -124, 72, -53, 32, -15, -99, -23, -67, 9, -12, -11,
    2, -67, -87, 91, -67, 118, 120, 51, -118, -44, -61, 111, -102, -118, 5, -59, -107, 85, -77, -49,
    -70, -48, 63, -128, -124, 57, 14, -61, 85, -116, 125, -46, -35, 56, 98, 20, 79, -3, -18, 102,
    -6, -78, 99, -5, 112, 124, -75, -57, -26, -20, 19, 4, 25, 7, -109, -51, 80, 60, -117, -38, 94,
    -89, 55, 113, -9, 107, -25, 103, -79, 58, 87, 120, -59, 40, 63, 109, -81, -18, -69, 119, 70, 72
    , 102, 11, -64, -93, 98, -46, -8, -13, 74, -106, 8, 5, -53, -106};

  public void testLoadPkcs8PrivateKeyFromPem() throws Exception {
    InputStream stream =
        getClass().getClassLoader().getResourceAsStream("com/google/api/client/util/secret.pem");
    PrivateKey privateKey = SecurityUtils.loadPkcs8PrivateKeyFromPem(
        SecurityUtils.getRsaKeyFactory(), stream, Charset.defaultCharset().name());
    assertEquals("RSA", privateKey.getAlgorithm());
    assertEquals("PKCS#8", privateKey.getFormat());
    byte[] actualEncoded = privateKey.getEncoded();
    Assert.assertArrayEquals(SecurityTestUtils.newEncodedRsaPrivateKeyBytes(), actualEncoded);
  }

  public void testLoadPrivateKeyFromKeyStore() throws Exception {
    InputStream stream =
        getClass().getClassLoader().getResourceAsStream("com/google/api/client/util/secret.p12");
    PrivateKey privateKey = SecurityUtils.loadPrivateKeyFromKeyStore(
        SecurityUtils.getPkcs12KeyStore(), stream, "notasecret", "privateKey", "notasecret");
    assertEquals("RSA", privateKey.getAlgorithm());
    assertEquals("PKCS#8", privateKey.getFormat());
    byte[] actualEncoded = privateKey.getEncoded();
    Assert.assertArrayEquals(SecurityTestUtils.newEncodedRsaPrivateKeyBytes(), actualEncoded);
  }

  public void testSign() throws Exception {
    byte[] actualSigned = SecurityUtils.sign(
        SecurityUtils.getSha256WithRsaSignatureAlgorithm(), SecurityTestUtils.newRsaPrivateKey(),
        StringUtils.getBytesUtf8("hello world"));
    Assert.assertArrayEquals(SIGNED, actualSigned);
  }
}
