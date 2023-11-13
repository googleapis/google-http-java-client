/*
 * Copyright 2012 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.api.client.extensions.appengine.http;

import com.google.api.client.http.LowLevelHttpRequest;
import com.google.api.client.http.LowLevelHttpResponse;
import com.google.appengine.api.urlfetch.FetchOptions;
import com.google.appengine.api.urlfetch.HTTPHeader;
import com.google.appengine.api.urlfetch.HTTPMethod;
import com.google.appengine.api.urlfetch.HTTPRequest;
import com.google.appengine.api.urlfetch.HTTPResponse;
import com.google.appengine.api.urlfetch.URLFetchService;
import com.google.appengine.api.urlfetch.URLFetchServiceFactory;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URL;

/** @author Yaniv Inbar */
final class UrlFetchRequest extends LowLevelHttpRequest {

  private final HTTPRequest request;

  UrlFetchRequest(FetchOptions fetchOptions, HTTPMethod method, String url) throws IOException {
    request = new HTTPRequest(new URL(url), method, fetchOptions);
  }

  @Override
  public void addHeader(String name, String value) {
    request.addHeader(new HTTPHeader(name, value));
  }

  @Override
  public void setTimeout(int connectTimeout, int readTimeout) {
    request
        .getFetchOptions()
        .setDeadline(
            connectTimeout == 0 || readTimeout == 0
                ? Double.MAX_VALUE
                : (connectTimeout + readTimeout) / 1000.0);
  }

  @Override
  public LowLevelHttpResponse execute() throws IOException {
    // write content
    if (getStreamingContent() != null) {
      String contentType = getContentType();
      if (contentType != null) {
        addHeader("Content-Type", contentType);
      }
      String contentEncoding = getContentEncoding();
      if (contentEncoding != null) {
        addHeader("Content-Encoding", contentEncoding);
      }
      ByteArrayOutputStream out = new ByteArrayOutputStream();
      getStreamingContent().writeTo(out);
      byte[] payload = out.toByteArray();
      if (payload.length != 0) {
        request.setPayload(payload);
      }
    }
    // connect
    URLFetchService service = URLFetchServiceFactory.getURLFetchService();
    HTTPResponse response = service.fetch(request);
    return new UrlFetchResponse(response);
  }
}
