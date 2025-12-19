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

package com.google.api.client.http.apache.v5;

import com.google.api.client.http.LowLevelHttpRequest;
import com.google.api.client.http.LowLevelHttpResponse;
import java.io.IOException;
import java.util.concurrent.TimeUnit;
import org.apache.hc.client5.http.ClientProtocolException;
import org.apache.hc.client5.http.classic.HttpClient;
import org.apache.hc.client5.http.classic.methods.HttpUriRequestBase;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.routing.RoutingSupport;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.HttpException;
import org.apache.hc.core5.http.HttpHost;
import org.apache.hc.core5.util.Timeout;

public final class Apache5HttpRequest extends LowLevelHttpRequest {

  private final HttpUriRequestBase request;

  private final RequestConfig.Builder requestConfig;

  private final HttpClient httpClient;

  Apache5HttpRequest(HttpClient httpClient, HttpUriRequestBase request) {
    this.httpClient = httpClient;
    this.request = request;
    // disable redirects as google-http-client handles redirects
    this.requestConfig = RequestConfig.custom().setRedirectsEnabled(false);
  }

  @Override
  public void addHeader(String name, String value) {
    request.addHeader(name, value);
  }

  /**
   * Configures the connection, connection request and read timeout of the request.
   *
   * @param connectTimeout timeout in milliseconds to establish a connection or {@code 0} for an
   *     infinite timeout. This parameter will also set the underlying request configuration's
   *     connection request timeout. See {@link
   *     org.apache.hc.client5.http.config.RequestConfig.Builder#setConnectionRequestTimeout(long,
   *     TimeUnit)} for more details.
   * @param readTimeout Timeout in milliseconds to read data from an established connection or
   *     {@code 0} for an infinite timeout
   * @throws IOException
   */
  @Override
  public void setTimeout(int connectTimeout, int readTimeout) throws IOException {
    requestConfig
        .setConnectTimeout(Timeout.of(connectTimeout, TimeUnit.MILLISECONDS))
        .setConnectionRequestTimeout(connectTimeout, TimeUnit.MILLISECONDS)
        // ResponseTimeout behaves the same as 4.x's SocketTimeout
        .setResponseTimeout(Timeout.of(readTimeout, TimeUnit.MILLISECONDS));
  }

  @Override
  public LowLevelHttpResponse execute() throws IOException {
    if (getStreamingContent() != null) {
      Apache5ContentEntity entity =
          new Apache5ContentEntity(
              getContentLength(), getStreamingContent(), getContentType(), getContentEncoding());
      request.setEntity(entity);
    }
    request.setConfig(requestConfig.build());
    HttpHost target;
    try {
      target = RoutingSupport.determineHost(request);
    } catch (HttpException e) {
      throw new ClientProtocolException("The request's host is invalid.", e);
    }
    // we use a null context so the client creates the default one internally
    ClassicHttpResponse httpResponse = httpClient.executeOpen(target, request, null);
    return new Apache5HttpResponse(request, httpResponse);
  }
}
