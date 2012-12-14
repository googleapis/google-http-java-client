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
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.util.SslUtils;
import com.google.common.base.Preconditions;

import org.apache.http.HttpHost;
import org.apache.http.HttpVersion;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.client.methods.HttpOptions;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.methods.HttpTrace;
import org.apache.http.client.params.ClientPNames;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.params.ConnManagerParams;
import org.apache.http.conn.params.ConnPerRouteBean;
import org.apache.http.conn.params.ConnRouteParams;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.DefaultHttpRequestRetryHandler;
import org.apache.http.impl.conn.DefaultHttpRoutePlanner;
import org.apache.http.impl.conn.ProxySelectorRoutePlanner;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;

import java.net.ProxySelector;
import java.security.GeneralSecurityException;

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
   * <p>
   * Upgrade warning: in prior version 1.12 the route planner was {@link DefaultHttpRoutePlanner}
   * but starting with version 1.13 the route planner is {@link ProxySelectorRoutePlanner}.
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
   * Note that a few settings are overridden:
   * </p>
   * <ul>
   * <li>HTTP version is set to 1.1 using {@link HttpProtocolParams#setVersion} with
   * {@link HttpVersion#HTTP_1_1}.</li>
   * <li>Redirects are disabled using {@link ClientPNames#HANDLE_REDIRECTS}.</li>
   * <li>{@link ConnManagerParams#setTimeout} and {@link HttpConnectionParams#setConnectionTimeout}
   * are set on each request based on {@link HttpRequest#getConnectTimeout()}.</li>
   * <li>{@link HttpConnectionParams#setSoTimeout} is set on each request based on
   * {@link HttpRequest#getReadTimeout()}.</li>
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
    HttpParams params = httpClient.getParams();
    HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
    params.setBooleanParameter(ClientPNames.HANDLE_REDIRECTS, false);
  }

  /**
   * Creates a new instance of the Apache HTTP client that is used by the
   * {@link #ApacheHttpTransport()} constructor.
   *
   * <p>
   * Use this constructor if you want to customize the default Apache HTTP client. Settings:
   * </p>
   * <ul>
   * <li>The client connection manager is set to {@link ThreadSafeClientConnManager}.</li>
   * <li>The socket buffer size is set to 8192 using
   * {@link HttpConnectionParams#setSocketBufferSize}.</li>
   * <li><The retry mechanism is turned off by setting
   * {@code new DefaultHttpRequestRetryHandler(0, false)}.</li>
   * <li>The route planner uses {@link ProxySelectorRoutePlanner} with
   * {@link ProxySelector#getDefault()}, which uses the proxy settings from <a
   * href="http://docs.oracle.com/javase/7/docs/api/java/net/doc-files/net-properties.html">system
   * properties</a>.</li>
   * </ul>
   *
   * <p>
   * Upgrade warning: in prior version 1.12 the route planner was {@link DefaultHttpRoutePlanner}
   * but starting with version 1.13 the route planner is {@link ProxySelectorRoutePlanner}.
   * </p>
   *
   * @return new instance of the Apache HTTP client
   * @since 1.6
   */
  public static DefaultHttpClient newDefaultHttpClient() {
    return newDefaultHttpClient(
        SSLSocketFactory.getSocketFactory(), newDefaultHttpParams(), ProxySelector.getDefault());
  }

  /** Returns a new instance of the default HTTP parameters we use. */
  static HttpParams newDefaultHttpParams() {
    HttpParams params = new BasicHttpParams();
    // Turn off stale checking. Our connections break all the time anyway,
    // and it's not worth it to pay the penalty of checking every time.
    HttpConnectionParams.setStaleCheckingEnabled(params, false);
    HttpConnectionParams.setSocketBufferSize(params, 8192);
    ConnManagerParams.setMaxTotalConnections(params, 200);
    ConnManagerParams.setMaxConnectionsPerRoute(params, new ConnPerRouteBean(20));
    return params;
  }

  /**
   * Creates a new instance of the Apache HTTP client that is used by the
   * {@link #ApacheHttpTransport()} constructor.
   *
   * @param socketFactory SSL socket factory
   * @param params HTTP parameters
   * @param proxySelector HTTP proxy selector to use {@link ProxySelectorRoutePlanner} or
   *        {@code null} for {@link DefaultHttpRoutePlanner}
   * @return new instance of the Apache HTTP client
   */
  static DefaultHttpClient newDefaultHttpClient(
      SSLSocketFactory socketFactory, HttpParams params, ProxySelector proxySelector) {
    // See http://hc.apache.org/httpcomponents-client-ga/tutorial/html/connmgmt.html
    SchemeRegistry registry = new SchemeRegistry();
    registry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
    registry.register(new Scheme("https", socketFactory, 443));
    ClientConnectionManager connectionManager = new ThreadSafeClientConnManager(params, registry);
    DefaultHttpClient defaultHttpClient = new DefaultHttpClient(connectionManager, params);
    defaultHttpClient.setHttpRequestRetryHandler(new DefaultHttpRequestRetryHandler(0, false));
    if (proxySelector != null) {
      defaultHttpClient.setRoutePlanner(new ProxySelectorRoutePlanner(registry, proxySelector));
    }
    return defaultHttpClient;
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

  @Deprecated
  @Override
  public boolean supportsHead() {
    return true;
  }

  @Deprecated
  @Override
  public boolean supportsPatch() {
    return true;
  }

  @Deprecated
  @Override
  public ApacheHttpRequest buildDeleteRequest(String url) {
    return buildRequest("DELETE", url);
  }

  @Deprecated
  @Override
  public ApacheHttpRequest buildGetRequest(String url) {
    return buildRequest("GET", url);
  }

  @Deprecated
  @Override
  public ApacheHttpRequest buildHeadRequest(String url) {
    return buildRequest("HEAD", url);
  }

  @Deprecated
  @Override
  public ApacheHttpRequest buildPatchRequest(String url) {
    return buildRequest("PATCH", url);
  }

  @Deprecated
  @Override
  public ApacheHttpRequest buildPostRequest(String url) {
    return buildRequest("POST", url);
  }

  @Deprecated
  @Override
  public ApacheHttpRequest buildPutRequest(String url) {
    return buildRequest("PUT", url);
  }

  /**
   * Shuts down the connection manager and releases allocated resources. This includes closing all
   * connections, whether they are currently used or not.
   *
   * @since 1.4
   */
  @Override
  public void shutdown() {
    httpClient.getConnectionManager().shutdown();
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
    private SSLSocketFactory socketFactory = SSLSocketFactory.getSocketFactory();

    /** HTTP parameters. */
    private HttpParams params = newDefaultHttpParams();

    /**
     * HTTP proxy selector to use {@link ProxySelectorRoutePlanner} or {@code null} for
     * {@link DefaultHttpRoutePlanner}.
     */
    private ProxySelector proxySelector = ProxySelector.getDefault();

    /**
     * Sets the HTTP proxy to use {@link DefaultHttpRoutePlanner} or {@code null} to use
     * {@link #setProxySelector(ProxySelector)} with {@link ProxySelector#getDefault()}.
     *
     * <p>
     * By default it is {@code null}, which uses the proxy settings from <a
     * href="http://docs.oracle.com/javase/7/docs/api/java/net/doc-files/net-properties.html">system
     * properties</a>.
     * </p>
     *
     * <p>
     * For example:
     * </p>
     *
     * <pre>
       setProxy(new HttpHost("127.0.0.1", 8080))
     * </pre>
     */
    public Builder setProxy(HttpHost proxy) {
      ConnRouteParams.setDefaultProxy(params, proxy);
      if (proxy != null) {
        proxySelector = null;
      }
      return this;
    }

    /**
     * Sets the HTTP proxy selector to use {@link ProxySelectorRoutePlanner} or {@code null} for
     * {@link DefaultHttpRoutePlanner}.
     *
     * <p>
     * By default it is {@link ProxySelector#getDefault()} which uses the proxy settings from <a
     * href="http://docs.oracle.com/javase/7/docs/api/java/net/doc-files/net-properties.html">system
     * properties</a>.
     * </p>
     */
    public Builder setProxySelector(ProxySelector proxySelector) {
      this.proxySelector = proxySelector;
      if (proxySelector != null) {
        ConnRouteParams.setDefaultProxy(params, null);
      }
      return this;
    }

    /**
     * Disables validating server SSL certificates by setting the SSL socket factory using
     * {@link SslUtils#trustAllSSLContext()} for the SSL context and
     * {@link SSLSocketFactory#ALLOW_ALL_HOSTNAME_VERIFIER} for the host name verifier.
     *
     * <p>
     * Be careful! Disabling certificate validation is dangerous and should only be done in testing
     * environments.
     * </p>
     */
    public Builder doNotValidateCertificate() throws GeneralSecurityException {
      socketFactory = new TrustAllSSLSocketFactory();
      return this;
    }

    /** Sets the SSL socket factory ({@link SSLSocketFactory#getSocketFactory()} by default). */
    public Builder setSocketFactory(SSLSocketFactory socketFactory) {
      this.socketFactory = Preconditions.checkNotNull(socketFactory);
      return this;
    }

    /** Returns the SSL socket factory ({@link SSLSocketFactory#getSocketFactory()} by default). */
    public SSLSocketFactory getSSLSocketFactory() {
      return socketFactory;
    }

    /** Returns the HTTP parameters. */
    public HttpParams getHttpParams() {
      return params;
    }

    /** Returns a new instance of {@link ApacheHttpTransport} based on the options. */
    public ApacheHttpTransport build() {
      return new ApacheHttpTransport(newDefaultHttpClient(socketFactory, params, proxySelector));
    }
  }
}
