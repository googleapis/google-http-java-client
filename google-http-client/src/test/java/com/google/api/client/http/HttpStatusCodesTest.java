/*
 * Copyright 2019 Google LLC
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

package com.google.api.client.http;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Tests {@link HttpStatusCodes}. */
@RunWith(JUnit4.class)
public class HttpStatusCodesTest {

  @Test
  public void testIsRedirect_3xx() {
    assertTrue(HttpStatusCodes.isRedirect(301));
    assertTrue(HttpStatusCodes.isRedirect(302));
    assertTrue(HttpStatusCodes.isRedirect(303));
    assertTrue(HttpStatusCodes.isRedirect(307));
    assertTrue(HttpStatusCodes.isRedirect(308));
  }

  @Test
  public void testIsRedirect_non3xx() {
    assertFalse(HttpStatusCodes.isRedirect(200));
    assertFalse(HttpStatusCodes.isRedirect(401));
    assertFalse(HttpStatusCodes.isRedirect(500));
  }
}
