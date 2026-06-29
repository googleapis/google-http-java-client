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
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.Socket;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.Provider;
import java.security.Security;
import java.security.cert.CertificateFactory;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import org.conscrypt.Conscrypt;

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

  private static final Logger logger = Logger.getLogger(NetHttpTransport.class.getName());

  private static final String[] PQC_GROUPS = new String[] {"X25519MLKEM768", "X25519"};

  private final ConnectionFactory connectionFactory;

  /** SSL socket factory or {@code null} for the default. */
  private final SSLSocketFactory sslSocketFactory;

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
     * Security provider to use for SSL context, or {@code null} for the default fallback. If not
     * set, {@link NetHttpTransport} defaults to using Conscrypt (if available) and falls back to
     * the default JDK provider.
     */
    private Provider securityProvider;

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
      SSLContext sslContext = SslUtils.getTlsSslContext();
      SslUtils.initSslContext(sslContext, trustStore, SslUtils.getPkixTrustManagerFactory());
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
      SSLContext sslContext = SslUtils.getTlsSslContext();
      SslUtils.initSslContext(
          sslContext,
          trustStore,
          SslUtils.getPkixTrustManagerFactory(),
          mtlsKeyStore,
          mtlsKeyStorePassword,
          SslUtils.getDefaultKeyManagerFactory());
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

    /** Sets the SSL socket factory or {@code null} for the default. */
    public Builder setSslSocketFactory(SSLSocketFactory sslSocketFactory) {
      this.sslSocketFactory = sslSocketFactory;
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
     * Sets the security provider to use for SSL context.
     *
     * <p>By default, {@link NetHttpTransport} will attempt to use Conscrypt as the security
     * provider. If Conscrypt is not available on the system, it will fall back to the default JDK
     * provider. Configuring a custom security provider here will override this default behavior and
     * take precedence.
     *
     * @param securityProvider security provider to use
     * @since 1.39
     */
    public Builder setSecurityProvider(Provider securityProvider) {
      this.securityProvider = securityProvider;
      return this;
    }

    /**
     * Resolves the {@link SSLSocketFactory} to use, prioritizing user-configured factory, then
     * custom security provider, defaulting to Conscrypt, and falling back to JDK.
     */
    private SSLSocketFactory resolveSslSocketFactory() {
      SSLSocketFactory resolvedFactory = sslSocketFactory;
      if (resolvedFactory == null) {
        try {
          SSLContext sslContext = null;
          // 1. If a custom security provider is configured, use it
          if (securityProvider != null) {
            sslContext = SSLContext.getInstance("TLS", securityProvider);
          } else {
            // 2. Default: Try Conscrypt (assumed to be available as part of SDK)
            try {
              if (Security.getProvider("Conscrypt") == null) {
                Security.insertProviderAt(Conscrypt.newProvider(), 1);
              }
              sslContext = SSLContext.getInstance("TLS", "Conscrypt");
            } catch (NoClassDefFoundError | Exception e) {
              logger.log(
                  Level.WARNING,
                  "Conscrypt security provider not available. Falling back to JDK default.",
                  e);
            }
          }

          if (sslContext == null) {
            // 3. Fallback to standard JDK
            sslContext = SSLContext.getInstance("TLS");
          }

          // Initialize SSLContext:
          // - First param (KeyManager[]): null (use default JDK key managers)
          // - Second param (TrustManager[]): null (use default JDK trust managers)
          // - Third param (SecureRandom): null (use default JDK secure random)
          sslContext.init(null, null, null);
          resolvedFactory = sslContext.getSocketFactory();
        } catch (Exception e) {
          // If SSLContext initialization fails entirely, fall back to the JVM's default
          // system-wide SSLSocketFactory.
          resolvedFactory = (SSLSocketFactory) SSLSocketFactory.getDefault();
        }
      }

      // Wrap factory to enforce PQC hybrid groups
      return new PqcEnforcingSSLSocketFactory(resolvedFactory, PQC_GROUPS);
    }

    /** Returns a new instance of {@link NetHttpTransport} based on the options. */
    public NetHttpTransport build() {
      if (System.getProperty(SHOULD_USE_PROXY_FLAG) != null) {
        setProxy(defaultProxy());
      }
      SSLSocketFactory resolvedSslSocketFactory = resolveSslSocketFactory();
      return this.proxy == null
          ? new NetHttpTransport(
              connectionFactory, resolvedSslSocketFactory, hostnameVerifier, isMtls)
          : new NetHttpTransport(this.proxy, resolvedSslSocketFactory, hostnameVerifier, isMtls);
    }
  }
  /**
   * An {@link SSLSocketFactory} wrapper that enforces Post-Quantum Cryptography (PQC) hybrid named
   * groups (such as X25519MLKEM768) on compatible sockets.
   *
   * <p>This wrapper is applied to the final resolved {@link SSLSocketFactory} before the transport
   * is built. This ensures that even if the factory was configured and initialized via custom trust
   * stores (e.g. {@link Builder#trustCertificates} which internally invokes {@code
   * SslUtils.initSslContext}), the resulting sockets are still intercepted and configured to
   * request PQC hybrid groups.
   *
   * <p>If the socket is detected as a Conscrypt socket, it configures the requested named groups
   * using Conscrypt's direct APIs. If the socket or provider does not support these groups, it
   * falls back to the default TLS negotiation of the delegate factory.
   */
  private static class PqcEnforcingSSLSocketFactory extends SSLSocketFactory {
    private final SSLSocketFactory delegate;
    private final String[] groups;

    PqcEnforcingSSLSocketFactory(SSLSocketFactory delegate, String[] groups) {
      this.delegate = delegate;
      this.groups = groups;
    }

    private Socket configure(Socket socket) {
      if (socket instanceof SSLSocket) {
        SSLSocket sslSocket = (SSLSocket) socket;
        try {
          if (Conscrypt.isConscrypt(sslSocket)) {
            Conscrypt.setNamedGroups(sslSocket, groups);
          }
        } catch (NoClassDefFoundError | Exception ignored) {
          // Fall back silently if Conscrypt classes are not loaded or the socket is not Conscrypt
        }
      }
      return socket;
    }

    @Override
    public String[] getDefaultCipherSuites() {
      return delegate.getDefaultCipherSuites();
    }

    @Override
    public String[] getSupportedCipherSuites() {
      return delegate.getSupportedCipherSuites();
    }

    @Override
    public Socket createSocket(Socket s, String host, int port, boolean autoClose)
        throws IOException {
      return configure(delegate.createSocket(s, host, port, autoClose));
    }

    @Override
    public Socket createSocket() throws IOException {
      return configure(delegate.createSocket());
    }

    @Override
    public Socket createSocket(String host, int port) throws IOException {
      return configure(delegate.createSocket(host, port));
    }

    @Override
    public Socket createSocket(String host, int port, InetAddress localHost, int localPort)
        throws IOException {
      return configure(delegate.createSocket(host, port, localHost, localPort));
    }

    @Override
    public Socket createSocket(InetAddress host, int port) throws IOException {
      return configure(delegate.createSocket(host, port));
    }

    @Override
    public Socket createSocket(
        InetAddress address, int port, InetAddress localAddress, int localPort) throws IOException {
      return configure(delegate.createSocket(address, port, localAddress, localPort));
    }
  }
}
