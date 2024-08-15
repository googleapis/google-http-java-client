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
import com.google.api.client.util.IOUtils;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.Header;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.HttpException;
import org.apache.hc.core5.http.io.HttpClientResponseHandler;
import org.apache.hc.core5.http.message.StatusLine;

public class Apache5ResponseHandler implements HttpClientResponseHandler<LowLevelHttpResponse> {
  @Override
  public LowLevelHttpResponse handleResponse(ClassicHttpResponse response)
      throws HttpException, IOException {
    Header[] headers = response.getHeaders();
    int code = response.getCode();
    InputStream content = null;
    long contentLength = -1;
    String contentType = null;
    String contentEncoding = null;
    String reasonPhrase = response.getReasonPhrase();
    String statusLine = new StatusLine(response).toString();
    HttpEntity entity = response.getEntity();
    if (entity != null) {
      // we copy the content input stream because it will be automatically closed after this
      // function
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      IOUtils.copy(entity.getContent(), baos);
      content = new ByteArrayInputStream(baos.toByteArray());
      contentLength = entity.getContentLength();
      contentType = entity.getContentType();
      contentEncoding = entity.getContentEncoding();
    }
    return new Apache5HttpResponse(
        headers,
        code,
        content,
        contentLength,
        contentType,
        contentEncoding,
        reasonPhrase,
        statusLine);
  }
}
