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
import com.google.api.client.testing.util.SecurityTestUtils;

import junit.framework.TestCase;

import java.security.interfaces.RSAPrivateKey;

/**
 * Tests {@link JsonWebSignature}.
 *
 * @author Yaniv Inbar
 */
public class JsonWebSignatureTest extends TestCase {

  public void testSign() throws Exception {
    JsonWebSignature.Header header = new JsonWebSignature.Header();
    header.setAlgorithm("RS256");
    header.setType("JWT");
    JsonWebToken.Payload payload = new JsonWebToken.Payload();
    payload.setIssuer("issuer")
        .setAudience("audience").setIssuedAtTimeSeconds(0L).setExpirationTimeSeconds(3600L);
    RSAPrivateKey privateKey = SecurityTestUtils.newRsaPrivateKey();
    assertEquals(
        "..kDmKaHNYByLmqAi9ROeLcFmZM7W_emsceKvDZiEGAo-ineCunC6_Nb0HEpAuzIidV-LYTMHS3BvI49KFz9gi6hI3"
        + "ZndDL5EzplpFJo1ZclVk1_hLn94P2OTAkZ4ydsTfus6Bl98EbCkInpF_2t5Fr8OaHxCZCDdDU7W5DSnOsx4",
        JsonWebSignature.signUsingRsaSha256(privateKey, new MockJsonFactory(), header, payload));
  }
}
