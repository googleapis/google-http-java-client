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

/** A Test class which uses {@link Beta} code. */
public class Test {

  public static void test() {
    // When using google-http-client-findbugs plugin - Error usage of a class [CLASS_NAME], which
    // annotated a Beta
    BetaClass exp = new BetaClass();

    exp.method();
    exp.betaMethod();

    exp.field = 10;
    exp.betaField = 10;

    ClassWithBetaField classField = new ClassWithBetaField();
    // When using google-http-client-findbugs plugin - Error usage of a field [FIELD_NAME], which
    // annotated as Beta
    @SuppressWarnings("unused")
    int n = ClassWithBetaField.betaStaticField;
    n = classField.betaField;
    classField.betaField = 20;

    // Won't produce errors
    n = ClassWithBetaField.staticField;
    n = classField.field;
    classField.field = 20;

    ClassWithBetaMethod classMethod = new ClassWithBetaMethod();
    // When using google-http-client-findbugs plugin - Error usage of a field [METHOD_NAME], which
    // annotated as Beta
    ClassWithBetaMethod.staticBetaMethod();
    classMethod.betaMethod();

    // Won't produce errors
    ClassWithBetaMethod.staticMethod();
    classMethod.method();
  }
}
