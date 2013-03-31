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

/** A class which contains {@link Beta} fields. */
public class ClassWithBetaField {

  @Beta
  public int betaField;
  public int field;

  @Beta
  public static final int betaStaticField = 10;
  public static final int staticField = 20;

  public ClassWithBetaField() {
    betaField = 1;
    field = 2;
  }

  @Override
  public String toString() {
    return "Field: " + field + ", Beta Field: " + betaField;
  }
}
