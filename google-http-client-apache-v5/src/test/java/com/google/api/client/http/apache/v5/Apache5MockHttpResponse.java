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

public class Apache5MockHttpResponse extends LowLevelHttpResponse {
  Apache5HttpResponse wrappedResponse;

  public Apache5MockHttpResponse() {
    wrappedResponse = new Apache5HttpResponse(new Header[0], 200, null, -1, null, null, null, null);
  }

  @Override
  public InputStream getContent() throws IOException {
    return wrappedResponse.getContent();
  }

  @Override
  public String getContentEncoding() throws IOException {
    return wrappedResponse.getContentEncoding();
  }

  @Override
  public long getContentLength() throws IOException {
    return wrappedResponse.getContentLength();
  }

  @Override
  public String getContentType() throws IOException {
    return wrappedResponse.getContentType();
  }

  @Override
  public String getStatusLine() throws IOException {
    return wrappedResponse.getStatusLine();
  }

  @Override
  public int getStatusCode() throws IOException {
    return wrappedResponse.getStatusCode();
  }

  @Override
  public String getReasonPhrase() throws IOException {
    return wrappedResponse.getReasonPhrase();
  }

  @Override
  public int getHeaderCount() throws IOException {
    return wrappedResponse.getHeaderCount();
  }

  @Override
  public String getHeaderName(int index) throws IOException {
    return wrappedResponse.getHeaderName(index);
  }

  @Override
  public String getHeaderValue(int index) throws IOException {
    return wrappedResponse.getHeaderValue(index);
  }
}
