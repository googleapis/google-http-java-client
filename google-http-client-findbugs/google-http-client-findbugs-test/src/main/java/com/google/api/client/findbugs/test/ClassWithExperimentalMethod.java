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

import com.google.api.client.util.Experimental;

/** A class which contains {@link Experimental} methods. */
public class ClassWithExperimentalMethod {

  @Experimental
  int experimentalField = 10;

  @Experimental
  public void experimentalMethod() {
    ExperimentalClass2 exp2 = new ExperimentalClass2();
    exp2.foo();

    experimentalField = 20;
  }

  public void method() {}

  @Experimental
  public static void staticExperimentalMethod() {}

  public static void staticMethod() {}

  @Override
  public String toString() {
    return "experimentalField: " + experimentalField;
  }
}
