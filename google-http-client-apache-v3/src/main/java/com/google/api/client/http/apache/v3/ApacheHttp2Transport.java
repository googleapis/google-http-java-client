package com.google.api.client.http.apache.v3;

import java.io.IOException;
import java.net.ProxySelector;
import java.util.concurrent.TimeUnit;

import org.apache.hc.client5.http.async.HttpAsyncClient;
import org.apache.hc.client5.http.async.methods.SimpleRequestBuilder;
import org.apache.hc.client5.http.config.ConnectionConfig;
import org.apache.hc.client5.http.config.TlsConfig;
import org.apache.hc.client5.http.impl.async.CloseableHttpAsyncClient;
import org.apache.hc.client5.http.impl.async.HttpAsyncClientBuilder;
import org.apache.hc.client5.http.impl.async.HttpAsyncClients;
import org.apache.hc.client5.http.impl.nio.PoolingAsyncClientConnectionManagerBuilder;
import org.apache.hc.client5.http.impl.routing.SystemDefaultRoutePlanner;
import org.apache.hc.client5.http.nio.AsyncClientConnectionManager;
import org.apache.hc.core5.http.config.Http1Config;
import org.apache.hc.core5.http2.HttpVersionPolicy;
import org.apache.hc.core5.http2.config.H2Config;
import org.apache.hc.core5.reactor.IOReactorConfig;

import com.google.api.client.http.HttpTransport;

public final class ApacheHttp2Transport extends HttpTransport{

    public final CloseableHttpAsyncClient httpAsyncClient;

    public ApacheHttp2Transport() {
        this(newDefaultHttpAsyncClient(false));
    }

    public ApacheHttp2Transport(Boolean useCustom) {
        this(newDefaultHttpAsyncClient(useCustom));
    }

    public ApacheHttp2Transport(CloseableHttpAsyncClient httpAsyncClient) {
        this.httpAsyncClient = httpAsyncClient;
        httpAsyncClient.start();
    }

    public static CloseableHttpAsyncClient newDefaultHttpAsyncClient(Boolean useCustom) {
        if (useCustom) {
            return defaultHttpAsyncClientBuilder().build();
        }
        return HttpAsyncClients.createHttp2System();
    }

    public static HttpAsyncClientBuilder defaultHttpAsyncClientBuilder() {
        return HttpAsyncClientBuilder.create()
                .setH2Config(H2Config.DEFAULT)
                .setHttp1Config(Http1Config.DEFAULT)
                .setIOReactorConfig(IOReactorConfig.DEFAULT)
                .setConnectionManager(defaultAsyncClientConnectionManager())
                .setRoutePlanner(new SystemDefaultRoutePlanner(ProxySelector.getDefault()))
                .disableRedirectHandling()
                .disableAutomaticRetries();
    }

    public static AsyncClientConnectionManager defaultAsyncClientConnectionManager() {
        return defaultPoolingAsyncClientConnectionManagerBuilder()
                .build();
    }

    public static PoolingAsyncClientConnectionManagerBuilder defaultPoolingAsyncClientConnectionManagerBuilder() {
        return PoolingAsyncClientConnectionManagerBuilder
                .create()
                .useSystemProperties()
                // .setConnectionConfigResolver(null)
                .setDefaultConnectionConfig(defaultConnectionConfig())
                // .setTlsConfigResolver(null)
                .setDefaultTlsConfig(defaultTlsConfig())
                .setTlsStrategy(null)
                .setMaxConnTotal(200)
                .setMaxConnPerRoute(20);
    }

    public static TlsConfig defaultTlsConfig() {
        return TlsConfig.custom().setVersionPolicy(HttpVersionPolicy.FORCE_HTTP_2).build();
    }

    public static ConnectionConfig defaultConnectionConfig() {
        return ConnectionConfig.custom()
                // .setConnectTimeout(null)
                // .setSocketTimeout(null)
                .setTimeToLive(-1, TimeUnit.MILLISECONDS)
                .build();
    }

    @Override
    public boolean supportsMethod(String method) {
        return true;
    }

    @Override
    protected ApacheHttp2Request buildRequest(String method, String url) {
        SimpleRequestBuilder requestBuilder = SimpleRequestBuilder.create(method).setUri(url);
        return new ApacheHttp2Request(httpAsyncClient, requestBuilder);
    }

    @Override
    public void shutdown() throws IOException {
        if (httpAsyncClient instanceof CloseableHttpAsyncClient) {
            ((CloseableHttpAsyncClient) httpAsyncClient).close();
        }
    }

    public HttpAsyncClient getHttpClient() {
        return httpAsyncClient;
    }
}
