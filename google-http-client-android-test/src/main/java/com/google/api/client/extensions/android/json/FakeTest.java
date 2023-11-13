/*
 * Copyright 2012 Google LLC
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
package com.google.api.client.extensions.android.json;

import junit.framework.TestCase;

/**
 * Fake test case to force Maven test runner to run the tests on device.
 *
 * @author Yaniv Inbar
 */
public class FakeTest extends TestCase {

  public FakeTest(String name) {
    super(name);
  }

  public final void test() throws Exception {}
}
