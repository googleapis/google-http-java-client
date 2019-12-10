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

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPrivateKey;
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
}
