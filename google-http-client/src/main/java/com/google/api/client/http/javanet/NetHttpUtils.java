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

import java.security.GeneralSecurityException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

/**
 * Utilities for the {@code java.net} package.
 *
 * @author Yaniv Inbar
 * @since 1.13
 */
public final class NetHttpUtils {

  /**
   * Returns an SSL context in which all X.509 certificates are trusted.
   *
   * <p>
   * Be careful! Using this is dangerous and should only be done in testing environments.
   * </p>
   */
  public static SSLContext trustAllSSLContext() throws GeneralSecurityException {
    TrustManager[] trustAllCerts = new TrustManager[] {new X509TrustManager() {

      public void checkClientTrusted(X509Certificate[] chain, String authType)
          throws CertificateException {
      }

      public void checkServerTrusted(X509Certificate[] chain, String authType)
          throws CertificateException {
      }

      public X509Certificate[] getAcceptedIssuers() {
        return null;
      }
    }};
    SSLContext context = SSLContext.getInstance("SSL");
    context.init(null, trustAllCerts, null);
    return context;
  }

  private NetHttpUtils() {
  }
}
