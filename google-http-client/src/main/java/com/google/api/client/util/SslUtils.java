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

package com.google.api.client.util;

import java.security.GeneralSecurityException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

/**
 * SSL utilities.
 *
 * @since 1.13
 * @author Yaniv Inbar
 */
public final class SslUtils {

  /**
   * Returns an SSL context in which all X.509 certificates are trusted.
   *
   * <p>
   * Be careful! Disabling SSL certificate validation is dangerous and should only be done in
   * testing environments.
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

  /**
   * Returns a verifier that trusts all host names.
   *
   * <p>
   * Be careful! Disabling host name verification is dangerous and should only be done in testing
   * environments.
   * </p>
   */
  public static HostnameVerifier trustAllHostnameVerifier() {
    return new HostnameVerifier() {

      public boolean verify(String arg0, SSLSession arg1) {
        return true;
      }
    };
  }

  private SslUtils() {
  }
}
