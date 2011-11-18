/*
 * Copyright (c) 2011 Google Inc.
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

package com.google.api.client.http;

import java.io.IOException;

/**
 * Basic authentication HTTP request initializer as specified in <a
 * href="http://tools.ietf.org/html/rfc2617#section-2">Basic Authentication Scheme</a>
 *
 * <p>
 * Implementation is immutable and thread-safe.
 * </p>
 *
 * @since 1.7
 * @author Yaniv Inbar
 */
public final class BasicAuthentication implements HttpRequestInitializer {

  private final String username;

  private final String password;

  public BasicAuthentication(String username, String password) {
    this.username = username;
    this.password = password;
  }

  /**
   * Implemented using {@link HttpHeaders#setBasicAuthentication(String, String)}.
   */
  public void initialize(HttpRequest request) throws IOException {
    request.getHeaders().setBasicAuthentication(username, password);
  }

  /** Returns the username. */
  public String getUsername() {
    return username;
  }

  /** Returns the password. */
  public String getPassword() {
    return password;
  }
}
