/*
 * Copyright (c) 2013 Google Inc.
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

package com.google.api.client.findbugs.test;

import com.google.api.client.util.Beta;

/** A class which contains {@link Beta} methods. */
public class ClassWithBetaMethod {

  @Beta
  int betaField = 10;

  @Beta
  public void betaMethod() {
    BetaClass2 exp2 = new BetaClass2();
    exp2.foo();

    betaField = 20;
  }

  public void method() {}

  @Beta
  public static void staticBetaMethod() {}

  public static void staticMethod() {}

  @Override
  public String toString() {
    return "BetaField: " + betaField;
  }
}
