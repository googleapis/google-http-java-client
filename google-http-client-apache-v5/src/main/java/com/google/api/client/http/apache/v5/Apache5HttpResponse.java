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
import java.util.logging.Logger;
import org.apache.hc.client5.http.classic.methods.HttpUriRequestBase;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.Header;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.message.StatusLine;

final class Apache5HttpResponse extends LowLevelHttpResponse {

  private static final Logger LOGGER = Logger.getLogger(Apache5HttpResponse.class.getName());
  private final HttpUriRequestBase request;
  private final ClassicHttpResponse response;
  private final Header[] allHeaders;
  private final HttpEntity entity;

  Apache5HttpResponse(HttpUriRequestBase request, ClassicHttpResponse response) {
    this.request = request;
    this.response = response;
    this.allHeaders = response.getHeaders();
    this.entity = response.getEntity();
  }

  @Override
  public int getStatusCode() {
    return response.getCode();
  }

  @Override
  public InputStream getContent() throws IOException {
    HttpEntity entity = response.getEntity();
    InputStream content = entity == null ? null : entity.getContent();
    return new Apache5ResponseContent(content, response);
  }

  @Override
  public String getContentEncoding() {
    return entity != null ? entity.getContentEncoding() : null;
  }

  @Override
  public long getContentLength() {
    return entity == null ? -1 : entity.getContentLength();
  }

  @Override
  public String getContentType() {
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
  public void disconnect() throws IOException {
    request.abort();
    response.close();
  }
}
