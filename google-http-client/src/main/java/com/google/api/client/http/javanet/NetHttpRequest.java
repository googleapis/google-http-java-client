/*
 * Copyright (c) 2010 Google Inc.
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

package com.google.api.client.http.javanet;

import com.google.api.client.http.HttpContent;
import com.google.api.client.http.LowLevelHttpRequest;
import com.google.api.client.http.LowLevelHttpResponse;
import com.google.common.base.Preconditions;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSocketFactory;

/**
 * @author Yaniv Inbar
 */
final class NetHttpRequest extends LowLevelHttpRequest {

  private final HttpURLConnection connection;
  private HttpContent content;

  /**
   * @param sslSocketFactory SSL socket factory
   * @param hostnameVerifier host name verifier
   * @param requestMethod request method
   * @param url URL
   */
  NetHttpRequest(SSLSocketFactory sslSocketFactory, HostnameVerifier hostnameVerifier,
      String requestMethod, String url) throws IOException {
    this(sslSocketFactory, hostnameVerifier, requestMethod, (HttpURLConnection) new URL(url)
        .openConnection());
  }

  /**
   * @param requestMethod request method
   * @param connection HTTP URL connection
   */
  NetHttpRequest(String requestMethod, HttpURLConnection connection) throws IOException {
    this(HttpsURLConnection.getDefaultSSLSocketFactory(), HttpsURLConnection
        .getDefaultHostnameVerifier(), requestMethod, connection);
  }

  /**
   * @param sslSocketFactory SSL socket factory
   * @param hostnameVerifier host name verifier
   * @param requestMethod request method
   * @param connection HTTP URL connection
   */
  NetHttpRequest(SSLSocketFactory sslSocketFactory, HostnameVerifier hostnameVerifier,
      String requestMethod, HttpURLConnection connection) throws IOException {
    this.connection = connection;
    connection.setRequestMethod(requestMethod);
    connection.setUseCaches(false);
    connection.setInstanceFollowRedirects(false);
    // do not validate certificate
    if (connection instanceof HttpsURLConnection) {
      HttpsURLConnection secureConnection = (HttpsURLConnection) connection;
      secureConnection.setHostnameVerifier(hostnameVerifier);
      secureConnection.setSSLSocketFactory(sslSocketFactory);
    }
  }

  @Override
  public void addHeader(String name, String value) {
    connection.addRequestProperty(name, value);
  }

  @Override
  public void setTimeout(int connectTimeout, int readTimeout) {
    connection.setReadTimeout(readTimeout);
    connection.setConnectTimeout(connectTimeout);
  }

  @Override
  public LowLevelHttpResponse execute() throws IOException {
    HttpURLConnection connection = this.connection;
    // write content
    if (content != null) {
      String contentType = content.getType();
      if (contentType != null) {
        addHeader("Content-Type", contentType);
      }
      String contentEncoding = content.getEncoding();
      if (contentEncoding != null) {
        addHeader("Content-Encoding", contentEncoding);
      }
      long contentLength = content.getLength();
      if (contentLength >= 0) {
        addHeader("Content-Length", Long.toString(contentLength));
      }
      String requestMethod = connection.getRequestMethod();
      if ("POST".equals(requestMethod) || "PUT".equals(requestMethod)) {
        connection.setDoOutput(true);
        // see http://developer.android.com/reference/java/net/HttpURLConnection.html
        if (contentLength >= 0 && contentLength <= Integer.MAX_VALUE) {
          connection.setFixedLengthStreamingMode((int) contentLength);
        } else {
          connection.setChunkedStreamingMode(0);
        }
        OutputStream out = connection.getOutputStream();
        try {
          content.writeTo(out);
        } finally {
          out.close();
        }
      } else {
        // cannot call setDoOutput(true) because it would change a GET method to POST
        // for HEAD, OPTIONS, DELETE, or TRACE it would throw an exceptions
        Preconditions.checkArgument(
            contentLength == 0, "%s with non-zero content length is not supported", requestMethod);
      }
    }
    // connect
    boolean successfulConnection = false;
    try {
      connection.connect();
      NetHttpResponse response = new NetHttpResponse(connection);
      successfulConnection = true;
      return response;
    } finally {
      if (!successfulConnection) {
        connection.disconnect();
      }
    }
  }

  @Override
  public void setContent(HttpContent content) {
    this.content = content;
  }
}
