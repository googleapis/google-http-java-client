package com.google.api.client.http.apache.v3;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import org.apache.hc.client5.http.async.methods.SimpleHttpRequest;
import org.apache.hc.client5.http.async.methods.SimpleHttpResponse;
import org.apache.hc.client5.http.async.methods.SimpleRequestBuilder;
import org.apache.hc.client5.http.async.methods.SimpleRequestProducer;
import org.apache.hc.client5.http.async.methods.SimpleResponseConsumer;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.async.CloseableHttpAsyncClient;
import org.apache.hc.core5.concurrent.FutureCallback;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.util.Timeout;

import com.google.api.client.http.LowLevelHttpRequest;
import com.google.api.client.http.LowLevelHttpResponse;

@SuppressWarnings("deprecation")
final class ApacheHttp2Request extends LowLevelHttpRequest {
    private final CloseableHttpAsyncClient httpAsyncClient;
    private final SimpleRequestBuilder requestBuilder;
    private SimpleHttpRequest request;
    private final RequestConfig.Builder requestConfig;

    ApacheHttp2Request(CloseableHttpAsyncClient httpAsyncClient, SimpleRequestBuilder requestBuilder) {
        this.httpAsyncClient = httpAsyncClient;
        this.requestBuilder = requestBuilder;

        this.requestConfig = RequestConfig.custom()
                .setRedirectsEnabled(false);
    }

    @Override
    public void addHeader(String name, String value) {
        requestBuilder.addHeader(name, value);
    }

    @Override
    public void setTimeout(int connectionTimeout, int readTimeout) throws IOException {
        requestConfig
                .setConnectTimeout(Timeout.ofMilliseconds(connectionTimeout))
                .setResponseTimeout(Timeout.ofMilliseconds(readTimeout));
        // .setConnectionRequestTimeout(connectionTimeout)
        // .setResponseTimeout();
    }

    @Override
    public LowLevelHttpResponse execute() throws IOException {
        // Convert StreamingContent to bytes to set request body
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        getStreamingContent().writeTo(baos);
        byte[] bytes = baos.toByteArray();
        requestBuilder.setBody(bytes, ContentType.parse(getContentType()));

        // Set request configs
        requestBuilder.setRequestConfig(requestConfig.build());

        // Build and execute request
        request = requestBuilder.build();
        final CompletableFuture<SimpleHttpResponse> responseFuture = new CompletableFuture<>();
        try {
            httpAsyncClient.execute(
                    SimpleRequestProducer.create(request),
                    SimpleResponseConsumer.create(),
                    new FutureCallback<SimpleHttpResponse>() {
                        @Override
                        public void completed(final SimpleHttpResponse response) {
                            responseFuture.complete(response);
                        }

                        @Override
                        public void failed(final Exception exception) {
                            responseFuture.completeExceptionally(exception);
                        }

                        @Override
                        public void cancelled() {
                            responseFuture.cancel(false);
                        }
                    });
            final SimpleHttpResponse response = responseFuture.get();
            return new ApacheHttp2Response(request, response);
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
            throw new IOException("Error making request", e);
        }
    }
}
