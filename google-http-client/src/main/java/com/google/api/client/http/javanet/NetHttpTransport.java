/*
 * Copyright (c) 2010 Google Inc.
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

import com.google.api.client.http.HttpMethods;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.util.Beta;
import com.google.api.client.util.Preconditions;
import com.google.api.client.util.SecurityUtils;
import com.google.api.client.util.SslUtils;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.Provider;
import java.security.cert.CertificateFactory;
import java.util.Arrays;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;

/**
 * Thread-safe HTTP low-level transport based on the {@code java.net} package.
 *
 * <p>Users should consider modifying the keep alive property on {@link NetHttpTransport} to control
 * whether the socket should be returned to a pool of connected sockets. More information is
 * available <a
 * href='http://docs.oracle.com/javase/7/docs/technotes/guides/net/http-keepalive.html'>here</a>.
 *
 * <p>We honor the default global caching behavior. To change the default behavior use {@link
 * HttpURLConnection#setDefaultUseCaches(boolean)}.
 *
 * <p>Implementation is thread-safe. For maximum efficiency, applications should use a single
 * globally-shared instance of the HTTP transport.
 *
 * @since 1.0
 * @author Yaniv Inbar
 */
public final class NetHttpTransport extends HttpTransport {
  private static Proxy defaultProxy() {
    return new Proxy(
        Proxy.Type.HTTP,
        new InetSocketAddress(
            System.getProperty("https.proxyHost"),
            Integer.parseInt(System.getProperty("https.proxyPort"))));
  }

  /**
   * All valid request methods as specified in {@link HttpURLConnection#setRequestMethod}, sorted in
   * ascending alphabetical order.
   */
  private static final String[] SUPPORTED_METHODS = {
    HttpMethods.DELETE,
    HttpMethods.GET,
    HttpMethods.HEAD,
    HttpMethods.OPTIONS,
    HttpMethods.POST,
    HttpMethods.PUT,
    HttpMethods.TRACE
  };

  static {
    Arrays.sort(SUPPORTED_METHODS);
  }

  private static final String SHOULD_USE_PROXY_FLAG = "com.google.api.client.should_use_proxy";

  private final ConnectionFactory connectionFactory;

  /** SSL socket factory or {@code null} for the default. */
  final SSLSocketFactory sslSocketFactory;

  /** Host name verifier or {@code null} for the default. */
  private final HostnameVerifier hostnameVerifier;

  /** Whether the transport is mTLS. Default value is {@code false}. */
  private final boolean isMtls;

  /**
   * Constructor with the default behavior.
   *
   * <p>Instead use {@link Builder} to modify behavior.
   */
  public NetHttpTransport() {
    this((ConnectionFactory) null, null, null, false);
  }

  /**
   * @param proxy HTTP proxy or {@code null} to use the proxy settings from <a
   *     href="http://docs.oracle.com/javase/7/docs/api/java/net/doc-files/net-properties.html">
   *     system properties</a>
   * @param sslSocketFactory SSL socket factory or {@code null} for the default
   * @param hostnameVerifier host name verifier or {@code null} for the default
   * @param isMtls Whether the transport is mTLS. Default value is {@code false}
   * @since 1.38
   */
  NetHttpTransport(
      Proxy proxy,
      SSLSocketFactory sslSocketFactory,
      HostnameVerifier hostnameVerifier,
      boolean isMtls) {
    this(new DefaultConnectionFactory(proxy), sslSocketFactory, hostnameVerifier, isMtls);
  }

  /**
   * @param connectionFactory factory to produce connections from {@link URL}s; if {@code null} then
   *     {@link DefaultConnectionFactory} is used
   * @param sslSocketFactory SSL socket factory or {@code null} for the default
   * @param hostnameVerifier host name verifier or {@code null} for the default
   * @param isMtls Whether the transport is mTLS. Default value is {@code false}
   * @since 1.38
   */
  NetHttpTransport(
      ConnectionFactory connectionFactory,
      SSLSocketFactory sslSocketFactory,
      HostnameVerifier hostnameVerifier,
      boolean isMtls) {
    this.connectionFactory = getConnectionFactory(connectionFactory);
    this.sslSocketFactory = sslSocketFactory;
    this.hostnameVerifier = hostnameVerifier;
    this.isMtls = isMtls;
  }

  private ConnectionFactory getConnectionFactory(ConnectionFactory connectionFactory) {
    if (connectionFactory == null) {
      if (System.getProperty(SHOULD_USE_PROXY_FLAG) != null) {
        return new DefaultConnectionFactory(defaultProxy());
      }
      return new DefaultConnectionFactory();
    }
    return connectionFactory;
  }

  @Override
  public boolean supportsMethod(String method) {
    return Arrays.binarySearch(SUPPORTED_METHODS, method) >= 0;
  }

  @Override
  public boolean isMtls() {
    return this.isMtls;
  }

  @Override
  protected NetHttpRequest buildRequest(String method, String url) throws IOException {
    Preconditions.checkArgument(supportsMethod(method), "HTTP method %s not supported", method);
    // connection with proxy settings
    URL connUrl = new URL(url);
    HttpURLConnection connection = connectionFactory.openConnection(connUrl);
    connection.setRequestMethod(method);
    // SSL settings
    if (connection instanceof HttpsURLConnection) {
      HttpsURLConnection secureConnection = (HttpsURLConnection) connection;
      if (hostnameVerifier != null) {
        secureConnection.setHostnameVerifier(hostnameVerifier);
      }
      if (sslSocketFactory != null) {
        secureConnection.setSSLSocketFactory(sslSocketFactory);
      }
    }
    return new NetHttpRequest(connection);
  }

  /**
   * Builder for {@link NetHttpTransport}.
   *
   * <p>Implementation is not thread-safe.
   *
   * @since 1.13
   */
  public static final class Builder {

    /** SSL socket factory or {@code null} for the default. */
    private SSLSocketFactory sslSocketFactory;

    /** Security provider to use or {@code null} for default. */
    private Provider securityProvider;

    /** Custom SSLSocket configurator or {@code null} to disable callback configuration. */
    SslSocketConfigurator sslSocketConfigurator;

    /** Host name verifier or {@code null} for the default. */
    private HostnameVerifier hostnameVerifier;

    /**
     * HTTP proxy or {@code null} to use the proxy settings from <a
     * href="http://docs.oracle.com/javase/7/docs/api/java/net/doc-files/net-properties.html">system
     * properties</a>.
     */
    private Proxy proxy;

    /**
     * {@link ConnectionFactory} or {@code null} to use a DefaultConnectionFactory. This value is
     * only used if proxy is unset.
     */
    private ConnectionFactory connectionFactory;

    /** Whether the transport is mTLS. Default value is {@code false}. */
    private boolean isMtls;

    /**
     * Sets the HTTP proxy or {@code null} to use the proxy settings from <a
     * href="http://docs.oracle.com/javase/7/docs/api/java/net/doc-files/net-properties.html">system
     * properties</a>.
     *
     * <p>For example:
     *
     * <pre>
     * setProxy(new Proxy(Proxy.Type.HTTP, new InetSocketAddress("127.0.0.1", 8080)))
     * </pre>
     */
    public Builder setProxy(Proxy proxy) {
      this.proxy = proxy;
      return this;
    }

    /**
     * Sets the {@link ConnectionFactory} or {@code null} to use a {@link DefaultConnectionFactory}.
     * <b>This value is ignored if the {@link #setProxy} has been called with a non-null value.</b>
     *
     * <p>If you wish to use a {@link Proxy}, it should be included in your {@link
     * ConnectionFactory} implementation.
     *
     * @since 1.20
     */
    public Builder setConnectionFactory(ConnectionFactory connectionFactory) {
      this.connectionFactory = connectionFactory;
      return this;
    }

    /**
     * Sets the SSL socket factory based on root certificates in a Java KeyStore.
     *
     * <p>Example usage:
     *
     * <pre>
     * trustCertificatesFromJavaKeyStore(new FileInputStream("certs.jks"), "password");
     * </pre>
     *
     * @param keyStoreStream input stream to the key store (closed at the end of this method in a
     *     finally block)
     * @param storePass password protecting the key store file
     * @since 1.14
     */
    public Builder trustCertificatesFromJavaKeyStore(InputStream keyStoreStream, String storePass)
        throws GeneralSecurityException, IOException {
      KeyStore trustStore = SecurityUtils.getJavaKeyStore();
      SecurityUtils.loadKeyStore(trustStore, keyStoreStream, storePass);
      return trustCertificates(trustStore);
    }

    /**
     * Sets the SSL socket factory based root certificates generated from the specified stream using
     * {@link CertificateFactory#generateCertificates(InputStream)}.
     *
     * <p>Example usage:
     *
     * <pre>
     * trustCertificatesFromStream(new FileInputStream("certs.pem"));
     * </pre>
     *
     * @param certificateStream certificate stream
     * @since 1.14
     */
    public Builder trustCertificatesFromStream(InputStream certificateStream)
        throws GeneralSecurityException, IOException {
      KeyStore trustStore = SecurityUtils.getJavaKeyStore();
      trustStore.load(null, null);
      SecurityUtils.loadKeyStoreFromCertificates(
          trustStore, SecurityUtils.getX509CertificateFactory(), certificateStream);
      return trustCertificates(trustStore);
    }

    /**
     * Sets the SSL socket factory based on a root certificate trust store.
     *
     * @param trustStore certificate trust store (use for example {@link SecurityUtils#loadKeyStore}
     *     or {@link SecurityUtils#loadKeyStoreFromCertificates})
     * @since 1.14
     */
    public Builder trustCertificates(KeyStore trustStore) throws GeneralSecurityException {
      SSLContext sslContext = SslUtils.getTlsSslContext(securityProvider);
      SslUtils.initSslContext(
          sslContext, trustStore, SslUtils.getPkixTrustManagerFactory(securityProvider));
      return setSslSocketFactory(sslContext.getSocketFactory());
    }

    /**
     * {@link Beta} <br>
     * Sets the SSL socket factory based on a root certificate trust store and a client certificate
     * key store. The client certificate key store will be used to establish mutual TLS.
     *
     * @param trustStore certificate trust store (use for example {@link SecurityUtils#loadKeyStore}
     *     or {@link SecurityUtils#loadKeyStoreFromCertificates})
     * @param mtlsKeyStore key store for client certificate and key to establish mutual TLS. (use
     *     for example {@link SecurityUtils#createMtlsKeyStore(InputStream)})
     * @param mtlsKeyStorePassword password for mtlsKeyStore parameter
     * @since 1.38
     */
    @Beta
    public Builder trustCertificates(
        KeyStore trustStore, KeyStore mtlsKeyStore, String mtlsKeyStorePassword)
        throws GeneralSecurityException {
      if (mtlsKeyStore != null && mtlsKeyStore.size() > 0) {
        this.isMtls = true;
      }
      SSLContext sslContext = SslUtils.getTlsSslContext(securityProvider);
      SslUtils.initSslContext(
          sslContext,
          trustStore,
          SslUtils.getPkixTrustManagerFactory(securityProvider),
          mtlsKeyStore,
          mtlsKeyStorePassword,
          SslUtils.getDefaultKeyManagerFactory(securityProvider));
      return setSslSocketFactory(sslContext.getSocketFactory());
    }

    /**
     * {@link Beta} <br>
     * Disables validating server SSL certificates by setting the SSL socket factory using {@link
     * SslUtils#trustAllSSLContext()} for the SSL context and {@link
     * SslUtils#trustAllHostnameVerifier()} for the host name verifier.
     *
     * <p>Be careful! Disabling certificate validation is dangerous and should only be done in
     * testing environments.
     */
    @Beta
    public Builder doNotValidateCertificate() throws GeneralSecurityException {
      hostnameVerifier = SslUtils.trustAllHostnameVerifier();
      sslSocketFactory = SslUtils.trustAllSSLContext().getSocketFactory();
      return this;
    }

    /** Returns the SSL socket factory. */
    public SSLSocketFactory getSslSocketFactory() {
      return sslSocketFactory;
    }

    /**
     * Sets the SSL socket factory or {@code null} for the default.
     *
     * <p>Note: If a custom {@link SslSocketConfigurator} is also provided, it will wrap and apply
     * its configuration callback to all sockets created by this factory.
     */
    public Builder setSslSocketFactory(SSLSocketFactory sslSocketFactory) {
      this.sslSocketFactory = sslSocketFactory;
      return this;
    }

    /**
     * Sets the custom security provider or {@code null} to use the default JRE provider.
     *
     * <p>When enabling Post-Quantum Cryptography (PQC) transport:
     *
     * <ul>
     *   <li>On JDK 8-19: A custom JCA provider (such as Conscrypt or BouncyCastle) must be
     *       configured via this method, in addition to configuring a custom {@link
     *       SslSocketConfigurator} callback to select the hybrid/PQC curves using provider-specific
     *       APIs.
     *   <li>On JDK 20-26: A custom provider (like Conscrypt) is recommended, but a custom {@link
     *       SslSocketConfigurator} invoking {@code SSLParameters.setNamedGroups(String[])} directly
     *       can be used natively without a custom JCA provider if standard JSSE supports the
     *       curves.
     *   <li>On JDK 27+: Neither a custom provider nor a configurator is required as PQC algorithms
     *       are negotiated natively by default.
     * </ul>
     *
     * @param securityProvider provider to use
     */
    public Builder setSecurityProvider(Provider securityProvider) {
      this.securityProvider = securityProvider;
      return this;
    }

    /**
     * Sets the custom {@link SslSocketConfigurator} callback to configure active SSLSockets.
     *
     * <p>If both a custom {@link SSLSocketFactory} (via {@link
     * #setSslSocketFactory(SSLSocketFactory)}) and a custom configurator are set, the configurator
     * callback will be applied to all sockets created by the custom socket factory. If no custom
     * factory is provided, the configurator will wrap and apply to sockets created by the default
     * resolved socket factory.
     *
     * <p>When enabling Post-Quantum Cryptography (PQC) transport:
     *
     * <ul>
     *   <li>On JDK 20-26: Callers can configure a custom {@link SslSocketConfigurator} that sets
     *       the named groups (such as {@code X25519MLKEM768}) directly on {@code SSLParameters}.
     *   <li>On JDK 8-19: Callers must provide a custom {@link SslSocketConfigurator} implementation
     *       to inspect the socket types and invoke provider-specific API extensions (e.g. Conscrypt
     *       JNI interfaces) to configure the curves.
     * </ul>
     *
     * @param configurator the callback configurator
     * @since 2.1.2
     */
    public Builder setSslSocketConfigurator(SslSocketConfigurator configurator) {
      this.sslSocketConfigurator = configurator;
      return this;
    }

    /** Returns the host name verifier or {@code null} for the default. */
    public HostnameVerifier getHostnameVerifier() {
      return hostnameVerifier;
    }

    /** Sets the host name verifier or {@code null} for the default. */
    public Builder setHostnameVerifier(HostnameVerifier hostnameVerifier) {
      this.hostnameVerifier = hostnameVerifier;
      return this;
    }

    /**
     * Resolves the {@link SSLSocketFactory} to be used by the transport.
     *
     * <p>If a custom factory has been set via {@link #setSslSocketFactory(SSLSocketFactory)}, it
     * will be returned. Otherwise, a default SSL socket factory will be constructed via {@link
     * #createDefaultSslSocketFactory()}.
     *
     * @return the resolved {@link SSLSocketFactory}
     */
    SSLSocketFactory resolveSslSocketFactory() {
      if (securityProvider == null && sslSocketConfigurator == null) {
        return sslSocketFactory;
      }
      SSLSocketFactory factory =
          sslSocketFactory != null ? sslSocketFactory : createDefaultSslSocketFactory();
      if (sslSocketConfigurator != null) {
        return new ConfigurableSSLSocketFactory(factory, sslSocketConfigurator);
      }
      return factory;
    }

    /**
     * Constructs a default {@link SSLSocketFactory} configured with the specified {@link Provider}.
     *
     * <p>This method initializes an {@link SSLContext} and resolves its {@link TrustManagerFactory}
     * using the same security provider (if provided), ensuring compatibility for TLS handshakes
     * when using custom providers (such as Conscrypt).
     *
     * @return the initialized default {@link SSLSocketFactory}
     */
    SSLSocketFactory createDefaultSslSocketFactory() {
      try {
        SSLContext sslContext = SslUtils.getTlsSslContext(securityProvider);
        TrustManager[] trustManagers = null;
        if (securityProvider != null) {
          TrustManagerFactory tmf = SslUtils.getDefaultTrustManagerFactory(securityProvider);
          tmf.init((KeyStore) null);
          trustManagers = tmf.getTrustManagers();
        }
        sslContext.init(null, trustManagers, null);
        return sslContext.getSocketFactory();
      } catch (GeneralSecurityException e) {
        // Halt execution because the SSLContext cannot be initialized with the requested
        // configuration.
        throw new IllegalStateException("Failed to initialize SSLSocketFactory.", e);
      }
    }

    /** Returns a new instance of {@link NetHttpTransport} based on the options. */
    public NetHttpTransport build() {
      if (System.getProperty(SHOULD_USE_PROXY_FLAG) != null) {
        setProxy(defaultProxy());
      }
      SSLSocketFactory resolvedFactory = resolveSslSocketFactory();
      return this.proxy == null
          ? new NetHttpTransport(connectionFactory, resolvedFactory, hostnameVerifier, isMtls)
          : new NetHttpTransport(this.proxy, resolvedFactory, hostnameVerifier, isMtls);
    }
  }
}
