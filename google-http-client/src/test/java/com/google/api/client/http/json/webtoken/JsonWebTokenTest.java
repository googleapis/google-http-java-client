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

import com.google.api.client.json.webtoken.JsonWebToken;
import com.google.api.client.json.webtoken.JsonWebToken.Payload;
import com.google.api.client.testing.http.FixedClock;

import junit.framework.TestCase;

/**
 * Tests {@link JsonWebToken}.
 *
 * @author Yaniv Inbar
 */
public class JsonWebTokenTest extends TestCase {

  public void testPayloadIsValidTime() {
    FixedClock clock = new FixedClock();

    // Test the payload.isValidTime() method
    Payload payload = new Payload(clock);
    payload.setExpirationTimeSeconds(8L); // seconds
    payload.setIssuedAtTimeSeconds(2L); // seconds

    clock.setTime(0); // Time before the token becomes valid
    assertFalse(payload.isValidTime(0));
    assertTrue(payload.isValidTime(2));

    clock.setTime(2000); // Token just became valid
    assertTrue(payload.isValidTime(0));

    clock.setTime(8000); // Token is about to become invalid
    assertTrue(payload.isValidTime(0));

    clock.setTime(9000); // Token is invalid
    assertFalse(payload.isValidTime(0));
    assertTrue(payload.isValidTime(1));
  }
}
