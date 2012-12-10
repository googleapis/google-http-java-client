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

import com.google.api.client.http.HttpMethods;
import com.google.api.client.http.HttpTransport;
import com.google.common.base.Preconditions;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.security.GeneralSecurityException;
import java.util.Arrays;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSocketFactory;

/**
 * Thread-safe HTTP low-level transport based on the {@code java.net} package.
 *
 * <p>
 * Users should consider modifying the keep alive property on {@link NetHttpTransport} to control
 * whether the socket should be returned to a pool of connected sockets. More information is
 * available <a
 * href='http://docs.oracle.com/javase/7/docs/technotes/guides/net/http-keepalive.html'>here</a>.
 * </p>
 *
 * <p>
 * Implementation is thread-safe. For maximum efficiency, applications should use a single
 * globally-shared instance of the HTTP transport.
 * </p>
 *
 * @since 1.0
 * @author Yaniv Inbar
 */
public final class NetHttpTransport extends HttpTransport {

  /**
   * All valid request methods as specified in {@link HttpURLConnection#setRequestMethod}, sorted in
   * ascending alphabetical order.
   */
  private static final String[] SUPPORTED_METHODS = {HttpMethods.DELETE,
      HttpMethods.GET,
      HttpMethods.HEAD,
      HttpMethods.OPTIONS,
      HttpMethods.POST,
      HttpMethods.PUT,
      HttpMethods.TRACE};
  static {
    Arrays.sort(SUPPORTED_METHODS);
  }

  /** SSL socket factory. */
  private final SSLSocketFactory sslSocketFactory;

  /** Host name verifier. */
  private final HostnameVerifier hostnameVerifier;

  /**
   * Constructor with the default behavior.
   *
   * <p>
   * Instead use {@link Builder} to modify behavior.
   * </p>
   */
  public NetHttpTransport() {
    this(HttpsURLConnection.getDefaultSSLSocketFactory(), HttpsURLConnection
        .getDefaultHostnameVerifier());
  }

  /**
   * @param sslSocketFactory SSL socket factory
   * @param hostnameVerifier host name verifier
   */
  NetHttpTransport(SSLSocketFactory sslSocketFactory, HostnameVerifier hostnameVerifier) {
    this.sslSocketFactory = sslSocketFactory;
    this.hostnameVerifier = hostnameVerifier;
  }

  @Override
  public boolean supportsMethod(String method) {
    return Arrays.binarySearch(SUPPORTED_METHODS, method) >= 0;
  }

  @Override
  protected NetHttpRequest buildRequest(String method, String url) throws IOException {
    Preconditions.checkArgument(supportsMethod(method), "HTTP method %s not supported", method);
    return new NetHttpRequest(sslSocketFactory, hostnameVerifier, method, url);
  }

  @Deprecated
  @Override
  public boolean supportsHead() {
    return true;
  }

  @Deprecated
  @Override
  public NetHttpRequest buildDeleteRequest(String url) throws IOException {
    return buildRequest("DELETE", url);
  }

  @Deprecated
  @Override
  public NetHttpRequest buildGetRequest(String url) throws IOException {
    return buildRequest("GET", url);
  }

  @Deprecated
  @Override
  public NetHttpRequest buildHeadRequest(String url) throws IOException {
    return buildRequest("HEAD", url);
  }

  @Deprecated
  @Override
  public NetHttpRequest buildPostRequest(String url) throws IOException {
    return buildRequest("POST", url);
  }

  @Deprecated
  @Override
  public NetHttpRequest buildPutRequest(String url) throws IOException {
    return buildRequest("PUT", url);
  }

  /**
   * Builder for {@link NetHttpTransport}.
   *
   * <p>
   * Implementation is not thread-safe.
   * </p>
   *
   * @since 1.13
   */
  public static final class Builder {

    /** SSL socket factory. */
    private SSLSocketFactory sslSocketFactory = HttpsURLConnection.getDefaultSSLSocketFactory();

    /** Host name verifier. */
    private HostnameVerifier hostnameVerifier = HttpsURLConnection.getDefaultHostnameVerifier();

    /**
     * Disables validating server SSL certificates by setting the SSL socket factory using
     * {@link NetHttpUtils#trustAllSSLContext()} for the SSL context and
     * {@link AllowAllHostnameVerifier} for the host name verifier.
     *
     * <p>
     * Be careful! Disabling certificate validation is dangerous and should only be done in testing
     * environments.
     * </p>
     */
    public Builder doNotValidateCertificate() throws GeneralSecurityException {
      hostnameVerifier = new AllowAllHostnameVerifier();
      sslSocketFactory = NetHttpUtils.trustAllSSLContext().getSocketFactory();
      return this;
    }

    /** Returns the SSL socket factory. */
    public SSLSocketFactory getSslSocketFactory() {
      return sslSocketFactory;
    }

    /**
     * Sets the SSL socket factory ({@link HttpsURLConnection#getDefaultSSLSocketFactory()} by
     * default).
     */
    public Builder setSslSocketFactory(SSLSocketFactory sslSocketFactory) {
      this.sslSocketFactory = Preconditions.checkNotNull(sslSocketFactory);
      return this;
    }

    /** Returns the host name verifier. */
    public HostnameVerifier getHostnameVerifier() {
      return hostnameVerifier;
    }

    /**
     * Sets the host name verifier ({@link HttpsURLConnection#getDefaultHostnameVerifier()} by
     * default).
     */
    public Builder setHostnameVerifier(HostnameVerifier hostnameVerifier) {
      this.hostnameVerifier = Preconditions.checkNotNull(hostnameVerifier);
      return this;
    }

    /** Returns a new instance of {@link NetHttpTransport} based on the options. */
    public NetHttpTransport build() {
      return new NetHttpTransport(sslSocketFactory, hostnameVerifier);
    }
  }
}
