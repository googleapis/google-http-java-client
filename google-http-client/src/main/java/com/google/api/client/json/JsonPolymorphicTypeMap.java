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

package com.google.api.client.json;

import com.google.api.client.util.Beta;
import com.google.api.client.util.Data;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Type;

/**
 * {@link Beta} <br>
 * Declares that the data type enclosing this field is polymorphic, and that the value of this field
 * in a heterogeneous JSON schema will determine what type the data should be parsed into.
 *
 * <p>A data structure must have no more than one field with this annotation present. The annotated
 * field's type must be considered "primitive" by {@link Data#isPrimitive(Type)}. The field's value
 * will be compared against the {@link TypeDef#key()} using {@link Object#toString()}.
 *
 * @author Nick Miceli
 * @since 1.16
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Beta
public @interface JsonPolymorphicTypeMap {

  /** The list of mappings from key value to a referenced {@link Class}. */
  TypeDef[] typeDefinitions();

  /** Declares a mapping between a key value and a referenced class. */
  @Target(ElementType.FIELD)
  @Retention(RetentionPolicy.RUNTIME)
  public @interface TypeDef {

    /** The string value to represent a specific type. */
    String key();

    /** The {@link Class} that is referenced by this key value. */
    Class<?> ref();
  }
}
