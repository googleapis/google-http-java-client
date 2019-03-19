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

import com.google.api.client.testing.json.webtoken.TestCertificates;
import com.google.api.client.testing.util.SecurityTestUtils;
import java.io.ByteArrayInputStream;
import java.security.PrivateKey;
import java.security.Signature;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPublicKey;
import java.util.ArrayList;
import javax.net.ssl.X509TrustManager;
import junit.framework.TestCase;
import org.junit.Assert;

/**
 * Tests {@link SecurityUtils}.
 *
 * @author Yaniv Inbar
 */
public class SecurityUtilsTest extends TestCase {

  private static final byte[] ENCODED = {
    48, -126, 2, 117, 2, 1, 0, 48, 13, 6, 9, 42, -122, 72, -122, -9, 13, 1, 1, 1, 5, 0, 4, -126, 2,
    95, 48, -126, 2, 91, 2, 1, 0, 2, -127, -127, 0, -67, 82, 117, -113, 35, 77, 69, 84, -20, -18,
    63, 94, -74, 75, 97, 60, 52, 56, -35, 101, 87, -93, 16, 68, 102, -93, 20, 49, 66, -88, 61, -123,
    119, -37, 80, 24, -75, -68, 33, -5, 113, -9, 7, -41, 123, 92, -18, 105, -24, 73, 74, 2, -36,
    -56, 44, 90, -51, -4, -119, -96, -35, -47, -120, -29, 65, -118, 16, 106, 89, -114, -99, -34, 84,
    10, -86, 72, 44, 69, 51, 89, -111, -47, 73, 89, -36, -27, -1, -96, -79, -124, -81, 108, -94, 56,
    118, -89, 74, -9, -73, 39, 109, -65, -40, -80, -85, -105, -18, -71, 98, 47, -76, -32, -19, 79,
    -33, 4, -126, -104, 97, -99, -60, 0, -86, 66, -88, 23, 124, -43, 2, 3, 1, 0, 1, 2, -127, -128,
    121, -44, 31, 92, 93, -18, 50, -120, 100, -13, 39, -118, 78, 42, -111, -58, -55, 32, 50, -80,
    45, 69, -4, -120, -41, -73, 103, -98, 15, 115, -18, 42, -2, 38, -2, 18, -8, -105, -71, 18, 114,
    -110, -15, -45, -29, 73, -71, 14, 35, -15, 77, -108, 43, -7, 16, 57, -38, -58, 0, -42, -87, 7,
    86, 91, 49, -112, 10, -2, -83, 49, -104, -123, 51, 116, -46, 3, -120, 100, -88, 77, 113, -115,
    -38, 97, 31, 118, -63, 41, 67, -11, -30, -65, 73, 114, -123, -128, 65, 60, 47, -54, -30, -58, 8,
    -92, 119, 28, -98, 20, -111, 65, 70, 101, 88, -78, -40, -108, 78, 92, 20, -46, -126, -11, -66,
    -10, -37, -87, -115, -67, 2, 65, 0, -5, 116, -123, -16, -115, 32, -35, 36, 81, -125, -128, -10,
    55, 75, -73, 30, -62, 19, -116, -110, 24, -61, -33, -28, -93, -63, -69, 51, -35, -14, -36, -75,
    127, 22, -123, -101, -45, -63, -20, 30, -6, 85, -108, 24, -104, 119, 22, -53, -54, -45, -27,
    120, -24, 44, -111, -21, -104, 101, -75, 102, -13, -2, -120, 59, 2, 65, 0, -64, -66, 114, -88,
    106, -77, -50, 25, -66, 98, 37, -7, 42, 70, -80, 56, 126, 87, -11, 76, -23, 95, -5, 95, -82,
    -88, -125, -119, -9, -113, 121, -10, 57, 36, 37, -26, -12, -126, -1, -45, -78, 16, -25, -101,
    -81, -37, 90, 127, -75, -112, -100, -91, 65, -94, -78, -128, 40, -77, -64, -4, 1, 103, -50, 47,
    2, 64, 52, 14, -21, -85, -31, -117, -20, 60, -104, -93, -95, 15, 88, 99, 84, -122, 9, -88, 2,
    114, 60, -82, 80, -84, 5, 59, 22, -122, -90, 108, -95, 68, -14, 10, -73, -98, -117, 56, -102,
    -87, -49, 41, -24, 127, 47, 17, 120, -90, -72, 87, 38, 42, -31, -26, 88, 79, 110, 61, -96, 80,
    -80, 51, 2, 1, 2, 64, 17, 56, -13, 69, -39, 66, -9, -57, -107, 27, 112, 9, 51, -99, -35, 97, 46,
    -24, -19, 34, 82, 56, 33, 94, 11, 93, 67, 99, -80, -101, 65, 106, -98, -16, 123, -14, -121, 38,
    -83, 117, 93, 19, -27, -98, 35, -72, -107, -3, -109, 91, -72, -93, -117, -103, -34, 25, 85,
    -119, -70, 84, -54, 75, 92, 65, 2, 64, 30, -82, 75, 100, -32, 123, 48, 18, -75, 26, 80, 109,
    108, -3, -33, -110, -127, -49, 30, -4, -93, 30, 4, 73, 85, -8, -36, 70, -123, -59, -124, 121,
    -101, 95, 28, -55, -1, -23, 83, -91, 111, 54, 60, -40, -100, -81, 11, -45, -118, 11, -51, 0,
    -28, 32, 4, 85, 69, 122, 111, 110, 100, -86, -73, 46
  };

  private static final byte[] SIGNED = {
    55, -20, -102, 62, -2, 115, -3, 103, -30, -45, 87, -128, -124, 39, -45, -125, -30, 19, -103, 85,
    -117, 99, 36, 44, 106, -94, -33, 51, 10, 62, 98, 81, 6, 55, 26, -123, -48, -78, 40, -83, 38,
    114, 18, 30, 12, 103, -77, 18, -10, -93, 126, -10, -99, 44, 123, -57, -98, -13, 40, 86, 90, 91,
    4, -127, -62, 10, -95, -26, 34, -23, 1, 3, 57, -70, -68, -74, 18, -107, -39, -85, 16, 87, 60,
    91, -71, 65, -43, -121, 116, -28, -75, 94, -68, -60, -24, 83, 113, -41, -47, -35, 11, 107, 117,
    -22, -112, 9, -14, 126, -90, 107, 63, -106, -118, -91, -97, -128, 31, -108, -100, 102, 0, -40,
    25, 53, -85, -112, 51, -61
  };

  private static final byte[] CONTENT_BYTES = StringUtils.getBytesUtf8("hello world");

  private static final String SECRET_P12_BASE64 =
      "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"
          + "YAqwa61lyj/OG90CAgQA";

  public void testLoadPrivateKeyFromKeyStore() throws Exception {
    byte[] secretP12 = Base64.decodeBase64(SECRET_P12_BASE64);
    ByteArrayInputStream stream = new ByteArrayInputStream(secretP12);
    PrivateKey privateKey =
        SecurityUtils.loadPrivateKeyFromKeyStore(
            SecurityUtils.getPkcs12KeyStore(), stream, "notasecret", "privateKey", "notasecret");
    assertEquals("RSA", privateKey.getAlgorithm());
    assertEquals("PKCS#8", privateKey.getFormat());
    byte[] actualEncoded = privateKey.getEncoded();
    Assert.assertArrayEquals(ENCODED, actualEncoded);
  }

  public void testSign() throws Exception {
    byte[] actualSigned =
        SecurityUtils.sign(
            SecurityUtils.getSha256WithRsaSignatureAlgorithm(),
            SecurityTestUtils.newRsaPrivateKey(),
            CONTENT_BYTES);
    Assert.assertArrayEquals(SIGNED, actualSigned);
  }

  public void testVerify() throws Exception {
    Signature signatureAlgorithm = SecurityUtils.getSha256WithRsaSignatureAlgorithm();
    RSAPublicKey publicKey = SecurityTestUtils.newRsaPublicKey();
    assertTrue(SecurityUtils.verify(signatureAlgorithm, publicKey, SIGNED, CONTENT_BYTES));
  }

  public X509Certificate verifyX509(TestCertificates.CertData caCert) throws Exception {
    Signature signatureAlgorithm = SecurityUtils.getSha256WithRsaSignatureAlgorithm();
    String jwsSignature = TestCertificates.JWS_SIGNATURE;
    int separator = jwsSignature.lastIndexOf('.');
    String data = jwsSignature.substring(0, separator);
    String signatureBase64 = jwsSignature.substring(separator + 1);
    byte[] signature = Base64.decodeBase64(signatureBase64);
    X509TrustManager trustManager = caCert.getTrustManager();
    ArrayList<String> certChain = new ArrayList<String>();
    certChain.add(TestCertificates.FOO_BAR_COM_CERT.getBase64Der());
    certChain.add(TestCertificates.CA_CERT.getBase64Der());
    return SecurityUtils.verify(
        signatureAlgorithm, trustManager, certChain, signature, data.getBytes("UTF-8"));
  }

  public void testVerifyX509() throws Exception {
    assertNotNull(verifyX509(TestCertificates.CA_CERT));
  }

  public void testVerifyX509WrongCa() throws Exception {
    assertNull(verifyX509(TestCertificates.BOGUS_CA_CERT));
  }
}
