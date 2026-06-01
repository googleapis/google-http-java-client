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

import com.google.errorprone.annotations.CanIgnoreReturnValue;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Provider;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.List;
import java.util.function.BiFunction;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.jsse.provider.BouncyCastleJsseProvider;

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
   * Returns the SSL context for "TLS" algorithm using Bouncy Castle JJSSE provider
   * scope-specifically.
   *
   * @since 2.1.1
   */
  public static SSLContext getTlsSslContext() throws NoSuchAlgorithmException {
    BouncyCastleProvider cryptoProvider = new BouncyCastleProvider();

    BouncyCastleJsseProvider provider = new BouncyCastleJsseProvider(cryptoProvider);

    SSLContext bcContext = SSLContext.getInstance("TLS", provider);

    try {
      // 4. Initialize the Bouncy Castle SSLContext with default managers.
      bcContext.init(null, null, null);
    } catch (GeneralSecurityException e) {
      // Print diagnostic trace to help understand why Bouncy Castle JSSE failed to initialize.
      e.printStackTrace();
      // 5. Retrieve standard JJSSE default context if BC JJSSE initialization fails.
      SSLContext fallbackContext = SSLContext.getInstance("TLS");
      try {
        // Initialize the fallback context with default managers as well.
        fallbackContext.init(null, null, null);
      } catch (GeneralSecurityException ex) {
        // TODO: Log
      }
      return fallbackContext;
    }

    // 6. Return the raw Bouncy Castle SSLContext.
    return new SSLContext(
        new PqcEnforcingSSLContextSpi(bcContext),
        bcContext.getProvider(),
        bcContext.getProtocol()) {};
  }

  /**
   * Returns the SSL context for "TLS" algorithm using the specified provider.
   *
   * @since 1.39
   */
  public static SSLContext getTlsSslContext(Provider provider) throws NoSuchAlgorithmException {
    return SSLContext.getInstance("TLS", provider);
  }

  /**
   * Returns the SSL context for "TLS" algorithm using the specified provider name.
   *
   * @since 2.1.1
   */
  public static SSLContext getTlsSslContext(String providerName)
      throws NoSuchAlgorithmException, NoSuchProviderException {
    return SSLContext.getInstance("TLS", providerName);
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
  @CanIgnoreReturnValue
  public static SSLContext initSslContext(
      SSLContext sslContext, KeyStore trustStore, TrustManagerFactory trustManagerFactory)
      throws GeneralSecurityException {
    sslContext.init(
        null, getCompatibleTrustManagers(sslContext, trustStore, trustManagerFactory), null);
    return sslContext;
  }

  /**
   * {@link Beta} <br>
   * Initializes the SSL context to the trust managers supplied by the trust manager factory for the
   * given trust store, and to the key managers supplied by the key manager factory for the given
   * key store.
   *
   * @param sslContext SSL context (for example {@link SSLContext#getInstance})
   * @param trustStore key store for certificates to trust (for example {@link
   *     SecurityUtils#getJavaKeyStore()})
   * @param trustManagerFactory trust manager factory (for example {@link
   *     #getPkixTrustManagerFactory()})
   * @param mtlsKeyStore key store for client certificate and key to establish mutual TLS
   * @param mtlsKeyStorePassword password for mtlsKeyStore parameter
   * @param keyManagerFactory key manager factory (for example {@link
   *     #getDefaultKeyManagerFactory()})
   * @since 1.38
   */
  @Beta
  public static SSLContext initSslContext(
      SSLContext sslContext,
      KeyStore trustStore,
      TrustManagerFactory trustManagerFactory,
      KeyStore mtlsKeyStore,
      String mtlsKeyStorePassword,
      KeyManagerFactory keyManagerFactory)
      throws GeneralSecurityException {
    keyManagerFactory.init(mtlsKeyStore, mtlsKeyStorePassword.toCharArray());
    sslContext.init(
        keyManagerFactory.getKeyManagers(),
        getCompatibleTrustManagers(sslContext, trustStore, trustManagerFactory),
        null);
    return sslContext;
  }

  /**
   * Resolves trust managers compatible with the active security provider. If the SSLContext is
   * managed by the Bouncy Castle JJSSE provider, it retrieves Bouncy Castle's native trust managers
   * instead of standard JDK trust managers. This prevents JCA trust manager wrapping mismatches and
   * unresolved peer host certificate exceptions on strict JVMs (e.g., Java 8/21).
   */
  private static TrustManager[] getCompatibleTrustManagers(
      SSLContext sslContext, KeyStore trustStore, TrustManagerFactory trustManagerFactory)
      throws GeneralSecurityException {
    if (sslContext.getProvider() instanceof BouncyCastleJsseProvider) {
      try {
        TrustManagerFactory bcTmf =
            TrustManagerFactory.getInstance(
                trustManagerFactory.getAlgorithm(), sslContext.getProvider());
        bcTmf.init(trustStore);
        return bcTmf.getTrustManagers();
      } catch (KeyStoreException | NoSuchAlgorithmException e) {
        // Fallback to default trust managers
      }
    }
    trustManagerFactory.init(trustStore);
    return trustManagerFactory.getTrustManagers();
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

  @org.codehaus.mojo.animal_sniffer.IgnoreJRERequirement
  private static class PqcEnforcingSSLEngine extends javax.net.ssl.SSLEngine {
    private final javax.net.ssl.SSLEngine delegate;

    PqcEnforcingSSLEngine(javax.net.ssl.SSLEngine delegate) {
      this.delegate = delegate;
    }

    @Override
    public void setSSLParameters(javax.net.ssl.SSLParameters params) {
      delegate.setSSLParameters(params);
      Object objEngine = delegate;
      if (objEngine instanceof org.bouncycastle.jsse.BCSSLEngine) {
        org.bouncycastle.jsse.BCSSLEngine bcEngine = (org.bouncycastle.jsse.BCSSLEngine) objEngine;
        org.bouncycastle.jsse.BCSSLParameters bcParams = bcEngine.getParameters();
        bcParams.setNamedGroups(new String[] {"X25519MLKEM768"});
        bcEngine.setParameters(bcParams);
      }
    }

    @Override
    public void setHandshakeApplicationProtocolSelector(
        BiFunction<SSLEngine, List<String>, String> selector) {
      delegate.setHandshakeApplicationProtocolSelector(
          (engine, protocols) -> selector.apply(this, protocols));
    }

    @Override
    public BiFunction<SSLEngine, List<String>, String> getHandshakeApplicationProtocolSelector() {
      return delegate.getHandshakeApplicationProtocolSelector();
    }

    @Override
    public String getApplicationProtocol() {
      return delegate.getApplicationProtocol();
    }

    @Override
    public String getHandshakeApplicationProtocol() {
      return delegate.getHandshakeApplicationProtocol();
    }

    @Override
    public javax.net.ssl.SSLParameters getSSLParameters() {
      return delegate.getSSLParameters();
    }

    @Override
    public void beginHandshake() throws javax.net.ssl.SSLException {
      delegate.beginHandshake();
    }

    @Override
    public void closeInbound() throws javax.net.ssl.SSLException {
      delegate.closeInbound();
    }

    @Override
    public void closeOutbound() {
      delegate.closeOutbound();
    }

    @Override
    public java.lang.Runnable getDelegatedTask() {
      return delegate.getDelegatedTask();
    }

    @Override
    public java.lang.String[] getEnabledCipherSuites() {
      return delegate.getEnabledCipherSuites();
    }

    @Override
    public java.lang.String[] getEnabledProtocols() {
      return delegate.getEnabledProtocols();
    }

    @Override
    public javax.net.ssl.SSLEngineResult.HandshakeStatus getHandshakeStatus() {
      return delegate.getHandshakeStatus();
    }

    @Override
    public boolean getNeedClientAuth() {
      return delegate.getNeedClientAuth();
    }

    @Override
    public javax.net.ssl.SSLSession getSession() {
      return delegate.getSession();
    }

    @Override
    public java.lang.String[] getSupportedCipherSuites() {
      return delegate.getSupportedCipherSuites();
    }

    @Override
    public java.lang.String[] getSupportedProtocols() {
      return delegate.getSupportedProtocols();
    }

    @Override
    public boolean getUseClientMode() {
      return delegate.getUseClientMode();
    }

    @Override
    public boolean getWantClientAuth() {
      return delegate.getWantClientAuth();
    }

    @Override
    public boolean isInboundDone() {
      return delegate.isInboundDone();
    }

    @Override
    public boolean isOutboundDone() {
      return delegate.isOutboundDone();
    }

    @Override
    public void setEnabledCipherSuites(java.lang.String[] suites) {
      delegate.setEnabledCipherSuites(suites);
    }

    @Override
    public void setEnabledProtocols(java.lang.String[] protocols) {
      delegate.setEnabledProtocols(protocols);
    }

    @Override
    public void setNeedClientAuth(boolean need) {
      delegate.setNeedClientAuth(need);
    }

    @Override
    public void setUseClientMode(boolean mode) {
      delegate.setUseClientMode(mode);
    }

    @Override
    public void setWantClientAuth(boolean want) {
      delegate.setWantClientAuth(want);
    }

    @Override
    public javax.net.ssl.SSLEngineResult unwrap(
        java.nio.ByteBuffer src, java.nio.ByteBuffer[] dsts, int offset, int length)
        throws javax.net.ssl.SSLException {
      return delegate.unwrap(src, dsts, offset, length);
    }

    @Override
    public javax.net.ssl.SSLEngineResult wrap(
        java.nio.ByteBuffer[] srcs, int offset, int length, java.nio.ByteBuffer dst)
        throws javax.net.ssl.SSLException {
      return delegate.wrap(srcs, offset, length, dst);
    }

    @Override
    public boolean getEnableSessionCreation() {
      return delegate.getEnableSessionCreation();
    }

    @Override
    public void setEnableSessionCreation(boolean flag) {
      delegate.setEnableSessionCreation(flag);
    }

    @Override
    public javax.net.ssl.SSLSession getHandshakeSession() {
      return delegate.getHandshakeSession();
    }
  }

  @org.codehaus.mojo.animal_sniffer.IgnoreJRERequirement
  private static class PqcEnforcingSSLContextSpi extends javax.net.ssl.SSLContextSpi {
    private final javax.net.ssl.SSLContext delegate;

    PqcEnforcingSSLContextSpi(javax.net.ssl.SSLContext delegate) {
      this.delegate = delegate;
    }

    @Override
    protected javax.net.ssl.SSLEngine engineCreateSSLEngine() {
      return new PqcEnforcingSSLEngine(delegate.createSSLEngine());
    }

    @Override
    protected javax.net.ssl.SSLEngine engineCreateSSLEngine(java.lang.String host, int port) {
      return new PqcEnforcingSSLEngine(delegate.createSSLEngine(host, port));
    }

    @Override
    protected javax.net.ssl.SSLSessionContext engineGetClientSessionContext() {
      return delegate.getClientSessionContext();
    }

    @Override
    protected javax.net.ssl.SSLSessionContext engineGetServerSessionContext() {
      return delegate.getServerSessionContext();
    }

    @Override
    protected javax.net.ssl.SSLServerSocketFactory engineGetServerSocketFactory() {
      return delegate.getServerSocketFactory();
    }

    @Override
    protected javax.net.ssl.SSLSocketFactory engineGetSocketFactory() {
      return delegate.getSocketFactory();
    }

    @Override
    protected void engineInit(
        javax.net.ssl.KeyManager[] km,
        javax.net.ssl.TrustManager[] tm,
        java.security.SecureRandom sr)
        throws java.security.KeyManagementException {
      delegate.init(km, tm, sr);
    }
  }
}
