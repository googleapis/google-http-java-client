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

/** A Test class which uses {@link Experimental} code. */
public class Test {

  public static void test() {
    // When using google-http-client-findbugs plugin - Error usage of a class [CLASS_NAME], which
    // annotated a Experimental
    ExperimentalClass exp = new ExperimentalClass();

    exp.method();
    exp.experimentalMethod();

    exp.field = 10;
    exp.experimentalField = 10;

    ClassWithExperimentalField classField = new ClassWithExperimentalField();
    // When using google-http-client-findbugs plugin - Error usage of a field [FIELD_NAME], which
    // annotated as Experimental
    int n = ClassWithExperimentalField.experimetnalStaticField;
    n = classField.experimentalField;
    classField.experimentalField = 20;

    // Won't produce errors
    n = ClassWithExperimentalField.staticField;
    n = classField.field;
    classField.field = 20;

    ClassWithExperimentalMethod classMethod = new ClassWithExperimentalMethod();
    // When using google-http-client-findbugs plugin - Error usage of a field [METHOD_NAME], which
    // annotated as Experimental
    ClassWithExperimentalMethod.staticExperimentalMethod();
    classMethod.experimentalMethod();

    // Won't produce errors
    ClassWithExperimentalMethod.staticMethod();
    classMethod.method();
  }
}
