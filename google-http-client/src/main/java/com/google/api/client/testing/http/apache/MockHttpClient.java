/*
 * Copyright (c) 2013 Google Inc.
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

package com.google.api.client.testing.http.apache;

import com.google.api.client.util.Beta;
import com.google.api.client.util.Preconditions;
import java.io.IOException;
import org.apache.http.ConnectionReuseStrategy;
import org.apache.http.HttpException;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.client.AuthenticationHandler;
import org.apache.http.client.HttpClient;
import org.apache.http.client.HttpRequestRetryHandler;
import org.apache.http.client.RedirectHandler;
import org.apache.http.client.RequestDirector;
import org.apache.http.client.UserTokenHandler;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.ConnectionKeepAliveStrategy;
import org.apache.http.conn.routing.HttpRoutePlanner;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHttpResponse;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpProcessor;
import org.apache.http.protocol.HttpRequestExecutor;

/**
 * {@link Beta} <br>
 * Mock for {@link HttpClient} that does not actually make any network calls.
 *
 * <p>Implementation is not thread-safe.
 *
 * @since 1.14
 * @author Yaniv Inbar
 */
@Beta
public class MockHttpClient extends DefaultHttpClient {

  /** HTTP response code to use. */
  int responseCode;

  @Override
  protected RequestDirector createClientRequestDirector(
      HttpRequestExecutor requestExec,
      ClientConnectionManager conman,
      ConnectionReuseStrategy reustrat,
      ConnectionKeepAliveStrategy kastrat,
      HttpRoutePlanner rouplan,
      HttpProcessor httpProcessor,
      HttpRequestRetryHandler retryHandler,
      RedirectHandler redirectHandler,
      AuthenticationHandler targetAuthHandler,
      AuthenticationHandler proxyAuthHandler,
      UserTokenHandler stateHandler,
      HttpParams params) {
    return new RequestDirector() {
      @Beta
      public HttpResponse execute(HttpHost target, HttpRequest request, HttpContext context)
          throws HttpException, IOException {
        return new BasicHttpResponse(HttpVersion.HTTP_1_1, responseCode, null);
      }
    };
  }

  /** Returns the HTTP response code to use. */
  public final int getResponseCode() {
    return responseCode;
  }

  /** Sets the HTTP response code to use. */
  public MockHttpClient setResponseCode(int responseCode) {
    Preconditions.checkArgument(responseCode >= 0);
    this.responseCode = responseCode;
    return this;
  }
}
