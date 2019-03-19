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

import java.nio.charset.Charset;

/**
 * Contains constant definitions for some standard {@link Charset} instances that are guaranteed to
 * be supported by all Java platform implementations.
 *
 * <p>NOTE: this is a copy of a subset of Guava's {@link com.google.common.base.Charsets}. The
 * implementation must match as closely as possible to Guava's implementation.
 *
 * @since 1.14
 * @author Yaniv Inbar
 */
public final class Charsets {

  /** UTF-8 charset. */
  public static final Charset UTF_8 = Charset.forName("UTF-8");

  /** ISO-8859-1 charset. */
  public static final Charset ISO_8859_1 = Charset.forName("ISO-8859-1");

  private Charsets() {}
}
