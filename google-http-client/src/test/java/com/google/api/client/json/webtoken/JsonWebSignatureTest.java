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

package com.google.api.client.json.webtoken;

import com.google.api.client.testing.json.MockJsonFactory;
import com.google.api.client.testing.json.webtoken.TestCertificates;
import com.google.api.client.testing.util.SecurityTestUtils;
import com.google.api.client.util.Base64;
import com.google.api.client.util.StringUtils;
import java.io.IOException;
import java.math.BigInteger;
import java.security.AlgorithmParameters;
import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPrivateKey;
import java.security.spec.ECGenParameterSpec;
import java.security.spec.ECParameterSpec;
import java.security.spec.ECPoint;
import java.security.spec.ECPublicKeySpec;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.InvalidParameterSpecException;
import java.util.ArrayList;
import java.util.List;
import javax.net.ssl.X509TrustManager;
import org.junit.Assert;
import org.junit.Test;

/**
 * Tests {@link JsonWebSignature}.
 *
 * @author Yaniv Inbar
 */
public class JsonWebSignatureTest {

  @Test
  public void testSign() throws Exception {
    JsonWebSignature.Header header = new JsonWebSignature.Header();
    header.setAlgorithm("RS256");
    header.setType("JWT");
    JsonWebToken.Payload payload = new JsonWebToken.Payload();
    payload
        .setIssuer("issuer")
        .setAudience("audience")
        .setIssuedAtTimeSeconds(0L)
        .setExpirationTimeSeconds(3600L);
    RSAPrivateKey privateKey = SecurityTestUtils.newRsaPrivateKey();
    Assert.assertEquals(
        "..kDmKaHNYByLmqAi9ROeLcFmZM7W_emsceKvDZiEGAo-ineCunC6_Nb0HEpAuzIidV-LYTMHS3BvI49KFz9gi6hI3"
            + "ZndDL5EzplpFJo1ZclVk1_hLn94P2OTAkZ4ydsTfus6Bl98EbCkInpF_2t5Fr8OaHxCZCDdDU7W5DSnOsx4",
        JsonWebSignature.signUsingRsaSha256(privateKey, new MockJsonFactory(), header, payload));
  }

  private X509Certificate verifyX509WithCaCert(TestCertificates.CertData caCert)
      throws IOException, GeneralSecurityException {
    JsonWebSignature signature = TestCertificates.getJsonWebSignature();
    X509TrustManager trustManager = caCert.getTrustManager();
    return signature.verifySignature(trustManager);
  }

  @Test
  public void testImmutableSignatureBytes() throws IOException {
    JsonWebSignature signature = TestCertificates.getJsonWebSignature();
    byte[] bytes = signature.getSignatureBytes();
    bytes[0] = (byte) (bytes[0] + 1);
    byte[] bytes2 = signature.getSignatureBytes();
    Assert.assertNotEquals(bytes2[0], bytes[0]);
  }

  @Test
  public void testImmutableSignedContentBytes() throws IOException {
    JsonWebSignature signature = TestCertificates.getJsonWebSignature();
    byte[] bytes = signature.getSignedContentBytes();
    bytes[0] = (byte) (bytes[0] + 1);
    byte[] bytes2 = signature.getSignedContentBytes();
    Assert.assertNotEquals(bytes2[0], bytes[0]);
  }

  @Test
  public void testImmutableCertificates() throws IOException {
    JsonWebSignature signature = TestCertificates.getJsonWebSignature();
    List<String> certificates = signature.getHeader().getX509Certificates();
    certificates.set(0, "foo");
    Assert.assertNotEquals("foo", signature.getHeader().getX509Certificates().get(0));
  }

  @Test
  public void testImmutableCritical() throws IOException {
    JsonWebSignature signature = TestCertificates.getJsonWebSignature();
    List<String> critical = new ArrayList<>();
    signature.getHeader().setCritical(critical);
    critical.add("bar");
    Assert.assertNull(signature.getHeader().getCritical());
  }

  @Test
  public void testCriticalNullForNone() throws IOException {
    JsonWebSignature signature = TestCertificates.getJsonWebSignature();
    Assert.assertNull(signature.getHeader().getCritical());
  }

  @Test
  public void testVerifyX509() throws Exception {
    X509Certificate signatureCert = verifyX509WithCaCert(TestCertificates.CA_CERT);
    Assert.assertNotNull(signatureCert);
    Assert.assertTrue(signatureCert.getSubjectDN().getName().startsWith("CN=foo.bar.com"));
  }

  @Test
  public void testVerifyX509WrongCa() throws Exception {
    Assert.assertNull(verifyX509WithCaCert(TestCertificates.BOGUS_CA_CERT));
  }

  private static final String ES256_CONTENT =
      "eyJhbGciOiJFUzI1NiIsInR5cCI6IkpXVCIsImtpZCI6Im1wZjBEQSJ9.eyJhdWQiOiIvcHJvamVjdHMvNjUyNTYyNzc2Nzk4L2FwcHMvY2xvdWQtc2FtcGxlcy10ZXN0cy1waHAtaWFwIiwiZW1haWwiOiJjaGluZ29yQGdvb2dsZS5jb20iLCJleHAiOjE1ODQwNDc2MTcsImdvb2dsZSI6eyJhY2Nlc3NfbGV2ZWxzIjpbImFjY2Vzc1BvbGljaWVzLzUxODU1MTI4MDkyNC9hY2Nlc3NMZXZlbHMvcmVjZW50U2VjdXJlQ29ubmVjdERhdGEiLCJhY2Nlc3NQb2xpY2llcy81MTg1NTEyODA5MjQvYWNjZXNzTGV2ZWxzL3Rlc3ROb09wIiwiYWNjZXNzUG9saWNpZXMvNTE4NTUxMjgwOTI0L2FjY2Vzc0xldmVscy9ldmFwb3JhdGlvblFhRGF0YUZ1bGx5VHJ1c3RlZCJdfSwiaGQiOiJnb29nbGUuY29tIiwiaWF0IjoxNTg0MDQ3MDE3LCJpc3MiOiJodHRwczovL2Nsb3VkLmdvb2dsZS5jb20vaWFwIiwic3ViIjoiYWNjb3VudHMuZ29vZ2xlLmNvbToxMTIxODE3MTI3NzEyMDE5NzI4OTEifQ";
  private static final String ES256_SIGNATURE =
      "yKNtdFY5EKkRboYNexBdfugzLhC3VuGyFcuFYA8kgpxMqfyxa41zkML68hYKrWu2kOBTUW95UnbGpsIi_u1fiA";

  // x, y values for keyId "mpf0DA" from https://www.gstatic.com/iap/verify/public_key-jwk
  private static final String GOOGLE_ES256_X = "fHEdeT3a6KaC1kbwov73ZwB_SiUHEyKQwUUtMCEn0aI";
  private static final String GOOGLE_ES256_Y = "QWOjwPhInNuPlqjxLQyhveXpWqOFcQPhZ3t-koMNbZI";

  private PublicKey buildEs256PublicKey(String x, String y)
      throws NoSuchAlgorithmException, InvalidParameterSpecException, InvalidKeySpecException {
    AlgorithmParameters parameters = AlgorithmParameters.getInstance("EC");
    parameters.init(new ECGenParameterSpec("secp256r1"));
    ECPublicKeySpec ecPublicKeySpec =
        new ECPublicKeySpec(
            new ECPoint(
                new BigInteger(1, Base64.decodeBase64(x)),
                new BigInteger(1, Base64.decodeBase64(y))),
            parameters.getParameterSpec(ECParameterSpec.class));
    KeyFactory keyFactory = KeyFactory.getInstance("EC");
    return keyFactory.generatePublic(ecPublicKeySpec);
  }

  @Test
  public void testVerifyES256() throws Exception {
    PublicKey publicKey = buildEs256PublicKey(GOOGLE_ES256_X, GOOGLE_ES256_Y);
    JsonWebSignature.Header header = new JsonWebSignature.Header();
    header.setAlgorithm("ES256");
    JsonWebSignature.Payload payload = new JsonWebToken.Payload();
    byte[] signatureBytes = Base64.decodeBase64(ES256_SIGNATURE);
    byte[] signedContentBytes = StringUtils.getBytesUtf8(ES256_CONTENT);
    JsonWebSignature jsonWebSignature =
        new JsonWebSignature(header, payload, signatureBytes, signedContentBytes);
    Assert.assertTrue(jsonWebSignature.verifySignature(publicKey));
  }
}
