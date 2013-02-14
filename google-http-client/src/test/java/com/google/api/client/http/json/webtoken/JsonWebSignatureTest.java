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

package com.google.api.client.http.json.webtoken;

import com.google.api.client.json.webtoken.JsonWebSignature;
import com.google.api.client.json.webtoken.JsonWebToken;
import com.google.api.client.testing.http.FixedClock;
import com.google.api.client.testing.http.json.MockJsonFactory;
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
    FixedClock clock = new FixedClock(0L);
    JsonWebSignature.Header header = new JsonWebSignature.Header();
    header.setAlgorithm("RS256");
    header.setType("JWT");
    JsonWebToken.Payload payload = new JsonWebToken.Payload(clock);
    payload.setIssuer("issuer")
        .setAudience("audience").setIssuedAtTimeSeconds(0L).setExpirationTimeSeconds(3600L);
    RSAPrivateKey privateKey = SecurityTestUtils.newRsaPrivateKey();
    assertEquals(
        "..LvqmJGrq1nip7PL0H5D7YyIGFP1EbAjOxLb2SZhUHzx5FtGanoZE8LEV7ectBy0yikWN_McLcjnZxNzy8D9FNdFb"
        + "bu1fsbeLUnAgjY1AY4o_3UZL29QlN3xT0VqbXZpIB_pqh_u7TR5npZJtp__EgEO59d4KV25P2eHWJwJqL08",
        JsonWebSignature.signUsingRsaSha256(privateKey, new MockJsonFactory(), header, payload));
  }
}
