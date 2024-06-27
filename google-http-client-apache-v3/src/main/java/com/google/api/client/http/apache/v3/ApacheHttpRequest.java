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
import java.io.IOException;
import java.util.concurrent.TimeUnit;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.classic.methods.HttpUriRequestBase;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.core5.util.Timeout;

/**
 * @author Yaniv Inbar
 */
final class ApacheHttpRequest extends LowLevelHttpRequest {

  private final HttpUriRequestBase request;

  private RequestConfig.Builder requestConfig;

  private CloseableHttpClient httpClient;

  ApacheHttpRequest(CloseableHttpClient httpClient, HttpUriRequestBase request) {
    this.httpClient = httpClient;
    this.request = request;
    // disable redirects as google-http-client handles redirects
    this.requestConfig =
        RequestConfig.custom()
            .setRedirectsEnabled(false)
    ;
  }

  @Override
  public void addHeader(String name, String value) {
    request.addHeader(name, value);
  }

  @Override
  public void setTimeout(int connectTimeout, int readTimeout) throws IOException {
    // TODO: these methods are deprecated - we will need a more up-to-date way of setting timeouts
    // on existing requests. Also, since we can't control the lower level socket configuration,
    // we indirectly set a read timeout via ResponseTimeout
    requestConfig
        .setConnectTimeout(Timeout.of(connectTimeout, TimeUnit.MILLISECONDS))
        .setResponseTimeout(Timeout.of(readTimeout, TimeUnit.MILLISECONDS));
  }

  @Override
  public LowLevelHttpResponse execute() throws IOException {
    if (getStreamingContent() != null) {
      // Preconditions.checkState(
      //     request instanceof HttpEntityEnclosingRequest,
      //     "Apache HTTP client does not support %s requests with content.",
      //     request.getRequestLine().getMethod());
      ContentEntity entity = new ContentEntity(getContentLength(), getStreamingContent(),
          getContentType(), getContentEncoding());
      request.setEntity(entity);
    }
    request.setConfig(requestConfig.build());
    return new ApacheHttpResponse(request, httpClient.execute(request));
  }
}
