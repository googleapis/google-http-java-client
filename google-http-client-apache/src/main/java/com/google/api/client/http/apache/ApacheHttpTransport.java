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

package com.google.api.client.http.apache;

import com.google.api.client.http.HttpMethods;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.util.Preconditions;
import com.google.api.client.util.SecurityUtils;
import com.google.api.client.util.SslUtils;
import java.io.IOException;
import java.io.InputStream;
import java.net.ProxySelector;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.cert.CertificateFactory;
import java.util.concurrent.TimeUnit;
import javax.net.ssl.SSLContext;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.client.methods.HttpOptions;
import org.apache.http.client.methods.HttpPatch;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.methods.HttpTrace;
import org.apache.http.config.SocketConfig;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.impl.conn.SystemDefaultRoutePlanner;

/**
 * Thread-safe HTTP transport based on the Apache HTTP Client library.
 *
 * <p>
 * Implementation is thread-safe, as long as any parameter modification to the
 * {@link #getHttpClient() Apache HTTP Client} is only done at initialization time. For maximum
 * efficiency, applications should use a single globally-shared instance of the HTTP transport.
 * </p>
 *
 * <p>
 * Default settings are specified in {@link #newDefaultHttpClient()}. Use the
 * {@link #ApacheHttpTransport(HttpClient)} constructor to override the Apache HTTP Client used.
 * Alternatively, use {@link #ApacheHttpTransport()} and change the {@link #getHttpClient()}. Please
 * read the <a
 * href="http://hc.apache.org/httpcomponents-client-ga/tutorial/html/connmgmt.html">Apache HTTP
 * Client connection management tutorial</a> for more complex configuration options.
 * </p>
 *
 * @since 1.0
 * @author Yaniv Inbar
 */
public final class ApacheHttpTransport extends HttpTransport {

  /** Apache HTTP client. */
  private final HttpClient httpClient;

  /**
   * Constructor that uses {@link #newDefaultHttpClient()} for the Apache HTTP client.
   *
   * <p>
   * Use {@link Builder} to modify HTTP client options.
   * </p>
   *
   * @since 1.3
   */
  public ApacheHttpTransport() {
    this(newDefaultHttpClient());
  }

  /**
   * Constructor that allows an alternative Apache HTTP client to be used.
   *
   * <p>
   * Note that in the previous version, we tried overrode several settings, however, we are no
   * longer able to do so.
   * </p>
   *
   * <p>If you choose to provide your own Apache HttpClient implementation, be sure that</p>
   * <ul>
   * <li>HTTP version is set to 1.1.</li>
   * <li>Redirects are disabled (google-http-client handles redirects).</li>
   * </ul>
   *
   * <p>
   * Use {@link Builder} for a more user-friendly way to modify the HTTP client options.
   * </p>
   *
   * @param httpClient Apache HTTP client to use
   *
   * @since 1.6
   */
  public ApacheHttpTransport(HttpClient httpClient) {
    this.httpClient = httpClient;
  }

  /**
   * Creates a new instance of the Apache HTTP client that is used by the
   * {@link #ApacheHttpTransport()} constructor.
   *
   * <p>
   * Use this constructor if you want to customize the default Apache HTTP client. Settings:
   * </p>
   * <ul>
   * <li>The client connection manager is set to {@link PoolingHttpClientConnectionManager}.</li>
   * <li>The socket buffer size is set to 8192 using {@link SocketConfig}.</li>
   * <li><The retry mechanism is turned off using
   * {@link HttpClientBuilder#disableRedirectHandling}.</li>
   * <li>The route planner uses {@link SystemDefaultRoutePlanner} with
   * {@link ProxySelector#getDefault()}, which uses the proxy settings from <a
   * href="http://docs.oracle.com/javase/7/docs/api/java/net/doc-files/net-properties.html">system
   * properties</a>.</li>
   * </ul>
   *
   * @return new instance of the Apache HTTP client
   * @since 1.6
   */
  public static HttpClient newDefaultHttpClient() {
    return new Builder().buildClient();
  }

  @Override
  public boolean supportsMethod(String method) {
    return true;
  }

  @Override
  protected ApacheHttpRequest buildRequest(String method, String url) {
    HttpRequestBase requestBase;
    if (method.equals(HttpMethods.DELETE)) {
      requestBase = new HttpDelete(url);
    } else if (method.equals(HttpMethods.GET)) {
      requestBase = new HttpGet(url);
    } else if (method.equals(HttpMethods.HEAD)) {
      requestBase = new HttpHead(url);
    } else if (method.equals(HttpMethods.PATCH)) {
      requestBase = new HttpPatch(url);
    } else if (method.equals(HttpMethods.POST)) {
      requestBase = new HttpPost(url);
    } else if (method.equals(HttpMethods.PUT)) {
      requestBase = new HttpPut(url);
    } else if (method.equals(HttpMethods.TRACE)) {
      requestBase = new HttpTrace(url);
    } else if (method.equals(HttpMethods.OPTIONS)) {
      requestBase = new HttpOptions(url);
    } else {
      requestBase = new HttpExtensionMethod(method, url);
    }
    return new ApacheHttpRequest(httpClient, requestBase);
  }

  /**
   * Shuts down the connection manager and releases allocated resources. This includes closing all
   * connections, whether they are currently used or not.
   *
   * @since 1.4
   */
  @Override
  public void shutdown() {
    if (httpClient instanceof CloseableHttpClient) {
      try {
        ((CloseableHttpClient) httpClient).close();
      } catch (IOException e) {
        // ignore
      }
    }
  }

  /**
   * Returns the Apache HTTP client.
   *
   * @since 1.5
   */
  public HttpClient getHttpClient() {
    return httpClient;
  }

  /**
   * Builder for {@link ApacheHttpTransport}.
   *
   * <p>
   * Implementation is not thread-safe.
   * </p>
   *
   * @since 1.13
   */
  public static final class Builder {

    /** SSL socket factory. */
    private SSLConnectionSocketFactory socketFactory =
        SSLConnectionSocketFactory.getSocketFactory();

    /** HTTP proxy selector to use {@link SystemDefaultRoutePlanner}. */
    private ProxySelector proxySelector = ProxySelector.getDefault();

    /**
     * Sets the HTTP proxy selector to use {@link SystemDefaultRoutePlanner}.
     *
     * <p>
     * By default it is {@link ProxySelector#getDefault()} which uses the proxy settings from <a
     * href="http://docs.oracle.com/javase/7/docs/api/java/net/doc-files/net-properties.html">system
     * properties</a>.
     * </p>
     */
    public Builder setProxySelector(ProxySelector proxySelector) {
      this.proxySelector = proxySelector;
      return this;
    }

    /**
     * Sets the SSL socket factory based on root certificates in a Java KeyStore.
     *
     * <p>
     * Example usage:
     * </p>
     *
     * <pre>
    trustCertificatesFromJavaKeyStore(new FileInputStream("certs.jks"), "password");
     * </pre>
     *
     * @param keyStoreStream input stream to the key store (closed at the end of this method in a
     *        finally block)
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
     * <p>
     * Example usage:
     * </p>
     *
     * <pre>
    trustCertificatesFromStream(new FileInputStream("certs.pem"));
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
     *        or {@link SecurityUtils#loadKeyStoreFromCertificates})
     *
     * @since 1.14
     */
    public Builder trustCertificates(KeyStore trustStore) throws GeneralSecurityException {
      SSLContext sslContext = SslUtils.getTlsSslContext();
      SslUtils.initSslContext(sslContext, trustStore, SslUtils.getPkixTrustManagerFactory());
      return setSocketFactory(new SSLConnectionSocketFactory(sslContext));
    }

    /**
     * Sets the SSL socket factory ({@link SSLConnectionSocketFactory#getSocketFactory()} by
     * default).
     */
    public Builder setSocketFactory(SSLConnectionSocketFactory socketFactory) {
      this.socketFactory = Preconditions.checkNotNull(socketFactory);
      return this;
    }

    HttpClient buildClient() {
      SocketConfig socketConfig =
          SocketConfig.custom()
              .setRcvBufSize(8192)
              .setSndBufSize(8192)
              .build();

      PoolingHttpClientConnectionManager connectionManager =
          new PoolingHttpClientConnectionManager(-1, TimeUnit.MILLISECONDS);
      // Disable the stale connection check (previously configured in the HttpConnectionParams
      connectionManager.setValidateAfterInactivity(-1);

      return HttpClientBuilder.create()
          .useSystemProperties()
          .setSSLSocketFactory(socketFactory)
          .setDefaultSocketConfig(socketConfig)
          .setMaxConnTotal(200)
          .setMaxConnPerRoute(20)
          .setRoutePlanner(new SystemDefaultRoutePlanner(proxySelector))
          .setConnectionManager(connectionManager)
          .disableRedirectHandling()
          .disableAutomaticRetries()
          .build();
    }

    /** Returns a new instance of {@link ApacheHttpTransport} based on the options. */
    public ApacheHttpTransport build() {
      return new ApacheHttpTransport(buildClient());
    }
  }
}
