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

import com.google.api.client.http.LowLevelHttpRequest;
import com.google.api.client.http.LowLevelHttpResponse;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import org.apache.hc.client5.http.async.methods.SimpleHttpRequest;
import org.apache.hc.client5.http.async.methods.SimpleHttpResponse;
import org.apache.hc.client5.http.async.methods.SimpleRequestProducer;
import org.apache.hc.client5.http.async.methods.SimpleResponseConsumer;
import org.apache.hc.client5.http.classic.methods.HttpUriRequestBase;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.async.CloseableHttpAsyncClient;
import org.apache.hc.client5.http.impl.async.HttpAsyncClientBuilder;
import org.apache.hc.core5.concurrent.FutureCallback;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.HttpException;
import org.apache.hc.core5.http.HttpResponse;
import org.apache.hc.core5.http.nio.AsyncEntityProducer;
import org.apache.hc.core5.http.nio.AsyncRequestProducer;
import org.apache.hc.core5.http.nio.entity.BasicAsyncEntityProducer;
import org.apache.hc.core5.http.nio.support.AbstractAsyncResponseConsumer;
import org.apache.hc.core5.http.nio.support.BasicRequestProducer;
import org.apache.hc.core5.http.protocol.HttpContext;
import org.apache.hc.core5.reactor.IOReactorConfig;
import org.apache.hc.core5.util.Timeout;
import org.apache.http.HttpEntity;

/**
 * @author Yaniv Inbar
 */
final class ApacheHttpRequest extends LowLevelHttpRequest {
    private final HttpAsyncClientBuilder httpClientBuilder;
    private final SimpleHttpRequest request;

    private RequestConfig.Builder requestConfig;

    ApacheHttpRequest(HttpAsyncClientBuilder httpClientBuilder, SimpleHttpRequest request) {
        this.httpClientBuilder = httpClientBuilder;
        this.request = request;
        // disable redirects as google-http-client handles redirects
        this.requestConfig =
                RequestConfig.custom()
                        .setRedirectsEnabled(false)
        // TODO: enable set these somewhere down the call
//            .setNormalizeUri(false)
//            .setStaleConnectionCheckEnabled(false)
        ;
    }

    @Override
    public void addHeader(String name, String value) {
        request.addHeader(name, value);
    }

    @Override
    public void setTimeout(int connectTimeout, int readTimeout) throws IOException {
        IOReactorConfig newConfig = IOReactorConfig.custom()
                .setSoTimeout(Timeout.ofMilliseconds(readTimeout))
                .build();
        requestConfig.setConnectTimeout(Timeout.ofMilliseconds(connectTimeout));
        httpClientBuilder.setIOReactorConfig(newConfig);
    }

    @Override
    public LowLevelHttpResponse execute() throws IOException {
    ApacheHttpRequestEntityProducer entityProducer = new ApacheHttpRequestEntityProducer(this);

        request.setConfig(requestConfig.build());
        final CompletableFuture<SimpleHttpResponse> responseFuture = new CompletableFuture<>();
        try {
            CloseableHttpAsyncClient client = httpClientBuilder.build();
            client.start();
            client.execute(
              new BasicRequestProducer(request, entityProducer),
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
                    }
            );
            final SimpleHttpResponse response = responseFuture.get();
            return new ApacheHttpResponse(request, response);
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
            throw new IOException("Error making request", e);
        }
    }

}
