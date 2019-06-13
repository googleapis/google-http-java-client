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
import java.security.KeyStore;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

/**
 * SSL utilities.
 *
 * @since 1.13
 * @author Yaniv Inbar
 */
public final class SslUtils {

  /**
   * Returns the SSL context for "SSL" algorithm.
   *
   * @since 1.14
   */
  public static SSLContext getSslContext() throws NoSuchAlgorithmException {
    return SSLContext.getInstance("SSL");
  }

  /**
   * Returns the SSL context for "TLS" algorithm.
   *
   * @since 1.14
   */
  public static SSLContext getTlsSslContext() throws NoSuchAlgorithmException {
    return SSLContext.getInstance("TLS");
  }

  /**
   * Returns the default trust manager factory.
   *
   * @since 1.14
   */
  public static TrustManagerFactory getDefaultTrustManagerFactory()
      throws NoSuchAlgorithmException {
    return TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
  }

  /**
   * Returns the PKIX trust manager factory.
   *
   * @since 1.14
   */
  public static TrustManagerFactory getPkixTrustManagerFactory() throws NoSuchAlgorithmException {
    return TrustManagerFactory.getInstance("PKIX");
  }

  /**
   * Returns the default key manager factory.
   *
   * @since 1.14
   */
  public static KeyManagerFactory getDefaultKeyManagerFactory() throws NoSuchAlgorithmException {
    return KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
  }

  /**
   * Returns the PKIX key manager factory.
   *
   * @since 1.14
   */
  public static KeyManagerFactory getPkixKeyManagerFactory() throws NoSuchAlgorithmException {
    return KeyManagerFactory.getInstance("PKIX");
  }

  /**
   * Initializes the SSL context to the trust managers supplied by the trust manager factory for the
   * given trust store.
   *
   * @param sslContext SSL context (for example {@link SSLContext#getInstance})
   * @param trustStore key store for certificates to trust (for example {@link
   *     SecurityUtils#getJavaKeyStore()})
   * @param trustManagerFactory trust manager factory (for example {@link
   *     #getPkixTrustManagerFactory()})
   * @since 1.14
   */
  public static SSLContext initSslContext(
      SSLContext sslContext, KeyStore trustStore, TrustManagerFactory trustManagerFactory)
      throws GeneralSecurityException {
    trustManagerFactory.init(trustStore);
    sslContext.init(null, trustManagerFactory.getTrustManagers(), null);
    return sslContext;
  }

  /**
   * {@link Beta} <br>
   * Returns an SSL context in which all X.509 certificates are trusted.
   *
   * <p>Be careful! Disabling SSL certificate validation is dangerous and should only be done in
   * testing environments.
   */
  @Beta
  public static SSLContext trustAllSSLContext() throws GeneralSecurityException {
    TrustManager[] trustAllCerts =
        new TrustManager[] {
          new X509TrustManager() {

            public void checkClientTrusted(X509Certificate[] chain, String authType)
                throws CertificateException {}

            public void checkServerTrusted(X509Certificate[] chain, String authType)
                throws CertificateException {}

            public X509Certificate[] getAcceptedIssuers() {
              return null;
            }
          }
        };
    SSLContext context = getTlsSslContext();
    context.init(null, trustAllCerts, null);
    return context;
  }

  /**
   * {@link Beta} <br>
   * Returns a verifier that trusts all host names.
   *
   * <p>Be careful! Disabling host name verification is dangerous and should only be done in testing
   * environments.
   */
  @Beta
  public static HostnameVerifier trustAllHostnameVerifier() {
    return new HostnameVerifier() {

      public boolean verify(String arg0, SSLSession arg1) {
        return true;
      }
    };
  }

  private SslUtils() {}
}
