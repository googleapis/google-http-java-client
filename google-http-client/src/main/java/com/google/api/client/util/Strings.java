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

/**
 * Static utility methods pertaining to {@code String} instances.
 *
 * <p>
 * NOTE: proxy for the Guava implementation of {@link com.google.common.base.Strings}.
 * </p>
 *
 * @since 1.14
 * @author Yaniv Inbar
 */
public final class Strings {

  /**
   * Returns {@code true} if the given string is null or is the empty string.
   *
   * @param string a string reference to check (may be {@code null})
   * @return {@code true} if the string is null or is the empty string
   */
  public static boolean isNullOrEmpty(String string) {
    return com.google.common.base.Strings.isNullOrEmpty(string);
  }

  private Strings() {
  }
}
