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

package com.google.api.client.util;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

/**
 * Use this annotation to indicate that a public API (class, method or field) is beta.
 *
 * <p>Beta API is subject to incompatible changes or removal in the future. It may also mean that
 * the server features it depends on are potentially subject to breakage at any time.
 *
 * <p>That API is exempt from any compatibility guarantees made by its containing library. Read
 * carefully the JavaDoc of the API bearing this annotation for better understanding of the risk.
 *
 * <p>To provide a smoother upgrade path when we make incompatible changes to beta API, whenever
 * possible we try to deprecate the old beta API in the first minor release, and then remove it in
 * the second minor release.
 *
 * <p>It is generally inadvisable for other non-beta libraries to use beta API from this library.
 * The problem is that other libraries don't have control over the version of this library being
 * used in client applications, and if the wrong version of this library is used, it has the
 * potential to break client applications.
 *
 * <p>You may use the google-http-client-findbugs plugin to find usages of API bearing this
 * annotation.
 *
 * @since 1.15
 * @author Eyal Peled
 */
@Target(
    value = {
      ElementType.ANNOTATION_TYPE,
      ElementType.CONSTRUCTOR,
      ElementType.FIELD,
      ElementType.METHOD,
      ElementType.TYPE,
      ElementType.PACKAGE
    })
@Documented
public @interface Beta {}
