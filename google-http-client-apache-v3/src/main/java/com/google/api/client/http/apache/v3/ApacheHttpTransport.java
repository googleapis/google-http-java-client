/*
 * Copyright 2024 Google LLC
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

package com.google.api.client.http.apache.v3;

import com.google.api.client.http.HttpMethods;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.util.Beta;
import com.google.common.base.Preconditions;
import java.io.IOException;
import java.net.ProxySelector;
import java.net.URI;
import java.sql.Time;
import java.util.concurrent.TimeUnit;
import org.apache.hc.client5.http.classic.HttpClient;
import org.apache.hc.client5.http.classic.methods.HttpDelete;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.classic.methods.HttpHead;
import org.apache.hc.client5.http.classic.methods.HttpOptions;
import org.apache.hc.client5.http.classic.methods.HttpPatch;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.classic.methods.HttpPut;
import org.apache.hc.client5.http.classic.methods.HttpUriRequestBase;
import org.apache.hc.client5.http.classic.methods.HttpTrace;
import org.apache.hc.client5.http.config.ConnectionConfig;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder;
import org.apache.hc.client5.http.ssl.SSLConnectionSocketFactory;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;
import org.apache.hc.client5.http.impl.routing.SystemDefaultRoutePlanner;
import org.apache.hc.core5.http.io.SocketConfig;

/**
 * Thread-safe HTTP transport based on the Apache HTTP Client library.
 *
 * <p>Implementation is thread-safe, as long as any parameter modification to the {@link
 * #getHttpClient() Apache HTTP Client} is only done at initialization time. For maximum efficiency,
 * applications should use a single globally-shared instance of the HTTP transport.
 *
 * <p>Default settings are specified in {@link #newDefaultHttpClient()}. Use the {@link
 * #ApacheHttpTransport(CloseableHttpClient)} constructor to override the Apache HTTP Client used. Please
 * read the <a
 * href="https://hc.apache.org/httpcomponents-client-4.5.x/current/tutorial/pdf/httpclient-tutorial.pdf">
 * Apache HTTP Client connection management tutorial</a> for more complex configuration options.
 *
 * @since 1.30
 */
public final class ApacheHttpTransport extends HttpTransport {

  /** Apache HTTP client. */
  private final CloseableHttpClient httpClient;

  /** If the HTTP client uses mTLS channel. */
  private final boolean isMtls;

  /**
   * Constructor that uses {@link #newDefaultHttpClient()} for the Apache HTTP client.
   *
   * @since 1.30
   */
  public ApacheHttpTransport() {
    this(newDefaultHttpClient(), false);
  }

  /**
   * Constructor that allows an alternative Apache HTTP client to be used.
   *
   * <p>Note that in the previous version, we overrode several settings. However, we are no longer
   * able to do so.
   *
   * <p>If you choose to provide your own Apache HttpClient implementation, be sure that
   *
   * <ul>
   *   <li>Redirects are disabled (google-http-client handles redirects).
   *   <li>Retries are disabled (google-http-client handles retries).
   * </ul>
   *
   * @param httpClient Closeable Apache HTTP client to use
   */
  public ApacheHttpTransport(CloseableHttpClient httpClient) {
    this.httpClient = httpClient;
    this.isMtls = false;
  }

  /**
   * {@link Beta} <br>
   * Constructor that allows an alternative CLoseable Apache HTTP client to be used.
   *
   * <p>Note that in the previous version, we overrode several settings. However, we are no longer
   * able to do so.
   *
   * <p>If you choose to provide your own Apache HttpClient implementation, be sure that
   *
   * <ul>
   *   <li>Redirects are disabled (google-http-client handles redirects).
   *   <li>Retries are disabled (google-http-client handles retries).
   * </ul>
   *
   * @param httpClient Closeable Apache HTTP client to use
   * @param isMtls If the HTTP client is mutual TLS
   */
  @Beta
  public ApacheHttpTransport(CloseableHttpClient httpClient, boolean isMtls) {
    this.httpClient = httpClient;
    this.isMtls = isMtls;
  }

  /**
   * Creates a new instance of the Apache HTTP client that is used by the {@link
   * #ApacheHttpTransport()} constructor.
   *
   * <p>Settings:
   *
   * <ul>
   *   <li>The client connection manager is set to {@link PoolingHttpClientConnectionManager}.
   *   <li><The retry mechanism is turned off using {@link
   *       HttpClientBuilder#disableRedirectHandling}.
   *   <li>The route planner uses {@link SystemDefaultRoutePlanner} with {@link
   *       ProxySelector#getDefault()}, which uses the proxy settings from <a
   *       href="https://docs.oracle.com/javase/7/docs/api/java/net/doc-files/net-properties.html">system
   *       properties</a>.
   * </ul>
   *
   * @return new instance of the Apache HTTP client
   * @since 1.30
   */
  public static CloseableHttpClient newDefaultHttpClient() {
    return newDefaultCloseableHttpClientBuilder().build();
  }

  /**
   * Creates a new Apache HTTP client builder that is used by the {@link #ApacheHttpTransport()}
   * constructor.
   *
   * <p>Settings:
   *
   * <ul>
   *   <li>The client connection manager is set to {@link PoolingHttpClientConnectionManager}.
   *   <li><The retry mechanism is turned off using {@link
   *       HttpClientBuilder#disableRedirectHandling}.
   *   <li>The route planner uses {@link SystemDefaultRoutePlanner} with {@link
   *       ProxySelector#getDefault()}, which uses the proxy settings from <a
   *       href="http://docs.oracle.com/javase/7/docs/api/java/net/doc-files/net-properties.html">system
   *       properties</a>.
   * </ul>
   *
   * @return new instance of the Apache HTTP client
   * @since 1.31
   */
  public static HttpClientBuilder newDefaultCloseableHttpClientBuilder() {
    PoolingHttpClientConnectionManager connectionManager = PoolingHttpClientConnectionManagerBuilder.create()
        .setSSLSocketFactory(SSLConnectionSocketFactory.getSocketFactory())
        .setMaxConnTotal(200)
        .setMaxConnPerRoute(20)
        .setDefaultConnectionConfig(ConnectionConfig.custom()
            .setTimeToLive(-1, TimeUnit.MILLISECONDS)
            .build())
        .build();

    return HttpClients.custom()
        .useSystemProperties()
        .setConnectionManager(connectionManager)
        .setRoutePlanner(new SystemDefaultRoutePlanner(ProxySelector.getDefault()))
        .disableRedirectHandling()
        .disableAutomaticRetries();
  }

  @Override
  public boolean supportsMethod(String method) {
    return true;
  }

  @Override
  protected ApacheHttpRequest buildRequest(String method, String url) {
    HttpUriRequestBase requestBase;
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
      requestBase = new HttpUriRequestBase(Preconditions.checkNotNull(method), URI.create(url));
    }
    return new ApacheHttpRequest(httpClient, requestBase);
  }

  /**
   * Shuts down the connection manager and releases allocated resources. This closes all
   * connections, whether they are currently used or not.
   *
   * @since 1.30
   */
  @Override
  public void shutdown() throws IOException {
    if (httpClient instanceof CloseableHttpClient) {
      ((CloseableHttpClient) httpClient).close();
    }
  }

  /**
   * Returns the Apache HTTP client.
   *
   * @since 1.30
   */
  public HttpClient getHttpClient() {
    return httpClient;
  }

  /** Returns if the underlying HTTP client is mTLS. */
  @Override
  public boolean isMtls() {
    return isMtls;
  }
}
