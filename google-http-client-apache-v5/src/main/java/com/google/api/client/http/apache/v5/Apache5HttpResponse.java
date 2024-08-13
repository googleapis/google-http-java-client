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

import com.google.api.client.http.LowLevelHttpResponse;
import java.io.IOException;
import java.io.InputStream;
import org.apache.hc.client5.http.classic.methods.HttpUriRequestBase;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.Header;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.HttpResponse;
import org.apache.hc.core5.http.message.StatusLine;

final class Apache5HttpResponse extends LowLevelHttpResponse {

  private final HttpUriRequestBase request;
  private final HttpResponse response;
  private final Header[] allHeaders;

  Apache5HttpResponse(HttpUriRequestBase request, HttpResponse response) {
    this.request = request;
    this.response = response;
    allHeaders = response.getHeaders();
  }

  @Override
  public int getStatusCode() {
    return response.getCode();
  }

  @Override
  public InputStream getContent() throws IOException {
    if (!(response instanceof ClassicHttpResponse)) {
      return null;
    }
    HttpEntity entity = ((ClassicHttpResponse) response).getEntity();
    return entity == null ? null : entity.getContent();
  }

  @Override
  public String getContentEncoding() {
    if (!(response instanceof ClassicHttpResponse)) {
      return null;
    }
    HttpEntity entity = ((ClassicHttpResponse) response).getEntity();
    if (entity != null) {
      return entity.getContentEncoding();
    }
    return null;
  }

  @Override
  public long getContentLength() {
    if (!(response instanceof ClassicHttpResponse)) {
      return -1;
    }
    HttpEntity entity = ((ClassicHttpResponse) response).getEntity();
    return entity == null ? -1 : entity.getContentLength();
  }

  @Override
  public String getContentType() {
    if (!(response instanceof ClassicHttpResponse)) {
      return null;
    }
    HttpEntity entity = ((ClassicHttpResponse) response).getEntity();
    return entity == null ? null : entity.getContentType();
  }

  @Override
  public String getReasonPhrase() {
    return response.getReasonPhrase();
  }

  @Override
  public String getStatusLine() {
    return new StatusLine(response).toString();
  }

  public String getHeaderValue(String name) {
    return response.getLastHeader(name).getValue();
  }

  @Override
  public int getHeaderCount() {
    return allHeaders.length;
  }

  @Override
  public String getHeaderName(int index) {
    return allHeaders[index].getName();
  }

  @Override
  public String getHeaderValue(int index) {
    return allHeaders[index].getValue();
  }

  /** Aborts execution of the request. */
  @Override
  public void disconnect() {
    request.abort();
  }
}
