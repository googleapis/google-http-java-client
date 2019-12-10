/*
 * Copyright 2019 Google LLC
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

package com.google.api.client.http.apache.v2;

import com.google.api.client.http.HttpMethods;
import com.google.api.client.http.HttpTransport;
import java.io.IOException;
import java.net.ProxySelector;
import java.util.concurrent.TimeUnit;
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
 * Please read the <a
 * href="http://hc.apache.org/httpcomponents-client-ga/tutorial/html/connmgmt.html">Apache HTTP
 * Client connection management tutorial</a> for more complex configuration options.
 * </p>
 *
 * @since 1.30
 * @author Yaniv Inbar
 */
public final class ApacheHttpTransport extends HttpTransport {

  /** Apache HTTP client. */
  private final HttpClient httpClient;

  /**
   * Constructor that uses {@link #newDefaultHttpClient()} for the Apache HTTP client.
   *
   * @since 1.30
   */
  public ApacheHttpTransport() {
    this(newDefaultHttpClient());
  }

  /**
   * Constructor that allows an alternative Apache HTTP client to be used.
   *
   * <p>
   * Note that in the previous version, we overrode several settings. However, we are no longer able
   * to do so.
   * </p>
   *
   * <p>If you choose to provide your own Apache HttpClient implementation, be sure that</p>
   * <ul>
   * <li>HTTP version is set to 1.1.</li>
   * <li>Redirects are disabled (google-http-client handles redirects).</li>
   * <li>Retries are disabled (google-http-client handles retries).</li>
   * </ul>
   *
   * @param httpClient Apache HTTP client to use
   *
   * @since 1.30
   */
  public ApacheHttpTransport(HttpClient httpClient) {
    this.httpClient = httpClient;
  }

  /**
   * Creates a new instance of the Apache HTTP client that is used by the
   * {@link #ApacheHttpTransport()} constructor.
   *
   * <p>
   * Settings:
   * </p>
   * <ul>
   * <li>The client connection manager is set to {@link PoolingHttpClientConnectionManager}.</li>
   * <li><The retry mechanism is turned off using
   * {@link HttpClientBuilder#disableRedirectHandling}.</li>
   * <li>The route planner uses {@link SystemDefaultRoutePlanner} with
   * {@link ProxySelector#getDefault()}, which uses the proxy settings from <a
   * href="https://docs.oracle.com/javase/7/docs/api/java/net/doc-files/net-properties.html">system
   * properties</a>.</li>
   * </ul>
   *
   * @return new instance of the Apache HTTP client
   * @since 1.30
   */
  public static HttpClient newDefaultHttpClient() {
    return newDefaultHttpClientBuilder().build();
  }

  /**
   * Creates a new Apache HTTP client builder that is used by the
   * {@link #ApacheHttpTransport()} constructor.
   *
   * <p>
   * Settings:
   * </p>
   * <ul>
   * <li>The client connection manager is set to {@link PoolingHttpClientConnectionManager}.</li>
   * <li><The retry mechanism is turned off using
   * {@link HttpClientBuilder#disableRedirectHandling}.</li>
   * <li>The route planner uses {@link SystemDefaultRoutePlanner} with
   * {@link ProxySelector#getDefault()}, which uses the proxy settings from <a
   * href="http://docs.oracle.com/javase/7/docs/api/java/net/doc-files/net-properties.html">system
   * properties</a>.</li>
   * </ul>
   *
   * @return new instance of the Apache HTTP client
   * @since 1.31
   */
  public static HttpClientBuilder newDefaultHttpClientBuilder() {

    return HttpClientBuilder.create()
            .useSystemProperties()
            .setSSLSocketFactory(SSLConnectionSocketFactory.getSocketFactory())
            .setMaxConnTotal(200)
            .setMaxConnPerRoute(20)
            .setConnectionTimeToLive(-1, TimeUnit.MILLISECONDS)
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
}
