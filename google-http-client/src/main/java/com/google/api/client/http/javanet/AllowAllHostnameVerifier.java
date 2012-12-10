/*
 * Copyright (c) 2012 Google Inc.
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

package com.google.api.client.http.javanet;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSession;

/**
 * Allows all host names.
 *
 * <p>
 * Be careful! Disabling certificate validation is dangerous and should only be done in testing
 * environments.
 * </p>
 *
 * @author Yaniv Inbar
 * @since 1.13
 */
public final class AllowAllHostnameVerifier implements HostnameVerifier {

  public boolean verify(String hostname, SSLSession session) {
    return true;
  }
}
