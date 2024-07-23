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

import com.google.api.client.http.HttpTransport;
import com.google.api.client.util.Beta;
import org.apache.hc.client5.http.async.methods.SimpleHttpRequest;
import org.apache.hc.client5.http.classic.methods.HttpUriRequestBase;
import org.apache.hc.client5.http.impl.async.CloseableHttpAsyncClient;
import org.apache.hc.client5.http.impl.async.HttpAsyncClientBuilder;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;
import org.apache.hc.client5.http.impl.nio.PoolingAsyncClientConnectionManager;
import org.apache.hc.client5.http.impl.routing.SystemDefaultRoutePlanner;
import org.apache.hc.client5.http.config.ConnectionConfig;
import org.apache.hc.core5.http.HttpRequest;

import java.io.IOException;
import java.net.ProxySelector;
import java.net.URI;
import java.util.concurrent.TimeUnit;

/**
 * Thread-safe HTTP transport based on the Apache HTTP Client library.
 *
 * <p>Implementation is thread-safe, as long as any parameter modification to the {@link
 * #getHttpClient() Apache HTTP Client} is only done at initialization time. For maximum efficiency,
 * applications should use a single globally-shared instance of the HTTP transport.
 *
 * <p>Default settings are specified in {@link #newDefaultHttpClient()}. Use the {@link
 * #ApacheHttpTransport(CloseableHttpAsyncClient)} constructor to override the Apache HTTP Client used. Please
 * read the <a
 * href="https://hc.apache.org/httpcomponents-client-4.5.x/current/tutorial/pdf/httpclient-tutorial.pdf">
 * Apache HTTP Client connection management tutorial</a> for more complex configuration options.
 *
 * @author Yaniv Inbar
 * @since 1.30
 */
public final class ApacheHttpTransport extends HttpTransport {

    /**
     * Apache HTTP client.
     */
    private final HttpAsyncClientBuilder httpClient;

    /**
     * If the HTTP client uses mTLS channel.
     */
    private final boolean isMtls;

    /**
     * Constructor that uses {@link #newDefaultHttpClient()} for the Apache HTTP client.
     *
     * @since 1.30
     */
    public ApacheHttpTransport() {
        this(newDefaultHttpClientBuilder(), false);
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
     *   <li>HTTP version is set to 1.1.
     *   <li>Redirects are disabled (google-http-client handles redirects).
     *   <li>Retries are disabled (google-http-client handles retries).
     * </ul>
     *
     * @param httpClient Apache HTTP client to use
     * @since 1.30
     */
    public ApacheHttpTransport(HttpAsyncClientBuilder httpClient) {
        this.httpClient = httpClient;
        this.isMtls = false;
    }

    /**
     * {@link Beta} <br>
     * Constructor that allows an alternative Apache HTTP client to be used.
     *
     * <p>Note that in the previous version, we overrode several settings. However, we are no longer
     * able to do so.
     *
     * <p>If you choose to provide your own Apache HttpClient implementation, be sure that
     *
     * <ul>
     *   <li>HTTP version is set to 1.1.
     *   <li>Redirects are disabled (google-http-client handles redirects).
     *   <li>Retries are disabled (google-http-client handles retries).
     * </ul>
     *
     * @param httpClient Apache HTTP client to use
     * @param isMtls     If the HTTP client is mutual TLS
     * @since 1.38
     */
    @Beta
    public ApacheHttpTransport(HttpAsyncClientBuilder httpClient, boolean isMtls) {
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
     *       CloseableHttpAsyncClientBuilder#disableRedirectHandling}.
     *   <li>The route planner uses {@link SystemDefaultRoutePlanner} with {@link
     *       ProxySelector#getDefault()}, which uses the proxy settings from <a
     *       href="https://docs.oracle.com/javase/7/docs/api/java/net/doc-files/net-properties.html">system
     *       properties</a>.
     * </ul>
     *
     * @return new instance of the Apache HTTP client
     * @since 1.30
     */
    public static CloseableHttpAsyncClient newDefaultHttpClient() {
        return newDefaultHttpClientBuilder().build();
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
    public static HttpAsyncClientBuilder newDefaultHttpClientBuilder() {

        ConnectionConfig connectionConfig = ConnectionConfig.custom()
                .setTimeToLive(-1, TimeUnit.MILLISECONDS)
                .build();
        PoolingAsyncClientConnectionManager connectionManager = new PoolingAsyncClientConnectionManager();
        connectionManager.setMaxTotal(200);
        connectionManager.setDefaultMaxPerRoute(20);
        connectionManager.setDefaultConnectionConfig(connectionConfig);

        return HttpAsyncClientBuilder.create()
                .useSystemProperties()
                .setConnectionManager(connectionManager)
                // socket factories are not configurable in the async client
                //.setSSLSocketFactory(SSLConnectionSocketFactory.getSocketFactory())
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
        SimpleHttpRequest request = new SimpleHttpRequest(method, URI.create(url));
        return new ApacheHttpRequest(httpClient, request);
    }

    /**
     * Shuts down the connection manager and releases allocated resources. This closes all
     * connections, whether they are currently used or not.
     *
     * @since 1.30
     */
    @Override
    public void shutdown() throws IOException {
        // no-op: we create short-lived clients in the requests
    }

    /**
     * Returns the Apache HTTP client.
     *
     * @since 1.30
     */
    public HttpAsyncClientBuilder getHttpClientBuilder() {
        return httpClient;
    }

    /**
     * Returns if the underlying HTTP client is mTLS.
     */
    @Override
    public boolean isMtls() {
        return isMtls;
    }
}
