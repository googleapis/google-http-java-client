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

import com.google.api.client.http.LowLevelHttpResponse;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.hc.client5.http.async.methods.SimpleHttpResponse;
import org.apache.hc.core5.http.Header;
import org.apache.hc.core5.http.HttpRequest;
import org.apache.hc.core5.http.message.StatusLine;

final class ApacheHttpResponse extends LowLevelHttpResponse {

  private final HttpRequest request;
  private final SimpleHttpResponse response;
  private final Header[] allHeaders;

  ApacheHttpResponse(HttpRequest request, SimpleHttpResponse response) {
    this.request = request;
    this.response = response;
    allHeaders = response.getHeaders();
  }

  @Override
  public int getStatusCode() {
    StatusLine statusLine = new StatusLine(response);
    return statusLine == null ? 0 : statusLine.getStatusCode();
  }

  @Override
  public InputStream getContent() throws IOException {
    return new ByteArrayInputStream(response.getBodyBytes());
  }

  @Override
  public String getContentEncoding() {
    return response.getFirstHeader("Content-Encoding").getValue();
  }

  @Override
  public long getContentLength() {
    return response.getBodyText().length();
  }

  @Override
  public String getContentType() {
    return response.getContentType().toString();
  }

  @Override
  public String getReasonPhrase() {
    return response.getReasonPhrase();
  }

  @Override
  public String getStatusLine() {
    return new StatusLine(response).toString();
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
}
