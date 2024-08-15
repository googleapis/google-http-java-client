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
import org.apache.hc.core5.http.Header;

final class Apache5HttpResponse extends LowLevelHttpResponse {

  private final Header[] headers;
  private final int code;
  private final InputStream content;
  private final long contentLength;
  private final String contentType;
  private final String contentEncoding;
  private final String reasonPhrase;
  private final String statusLine;

  Apache5HttpResponse(
      Header[] headers,
      int code,
      InputStream content,
      long contentLength,
      String contentType,
      String contentEncoding,
      String reasonPhrase,
      String statusLine) {
    this.headers = headers;
    this.code = code;
    this.content = content;
    this.contentLength = contentLength;
    this.contentType = contentType;
    this.contentEncoding = contentEncoding;
    this.reasonPhrase = reasonPhrase;
    this.statusLine = statusLine;
  }

  @Override
  public int getStatusCode() {
    return code;
  }

  @Override
  public InputStream getContent() throws IOException {
    return content;
  }

  @Override
  public String getContentEncoding() {
    return contentEncoding;
  }

  @Override
  public long getContentLength() {
    return contentLength;
    //            HttpEntity entity = response.getEntity();
    //    return entity == null ? -1 : entity.getContentLength();
  }

  @Override
  public String getContentType() {
    return contentType;
  }

  @Override
  public String getReasonPhrase() {
    return reasonPhrase;
  }

  @Override
  public String getStatusLine() {
    return statusLine;
  }

  @Override
  public int getHeaderCount() {
    return headers.length;
  }

  @Override
  public String getHeaderName(int index) {
    return headers[index].getName();
  }

  @Override
  public String getHeaderValue(int index) {
    return headers[index].getValue();
  }
}
