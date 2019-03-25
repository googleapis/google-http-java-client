/*
 * Copyright (c) 2012 Google Inc.
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

package com.google.api.client.extensions.appengine.http;

import com.google.api.client.http.HttpMethods;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.util.Preconditions;
import com.google.appengine.api.urlfetch.FetchOptions;
import com.google.appengine.api.urlfetch.HTTPMethod;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.Arrays;

/**
 * Thread-safe HTTP transport for Google App Engine based on <a
 * href="https://cloud.google.com/appengine/docs/standard/java/issue-requests">URL Fetch</a>.
 *
 * <p>Implementation is thread-safe. For maximum efficiency, applications should use a single
 * globally-shared instance of the HTTP transport.
 *
 * <p>URL Fetch is only available on Google App Engine (not on any other Java environment), and is
 * the underlying HTTP transport used for App Engine. Their implementation of {@link
 * HttpURLConnection} is simply an abstraction layer on top of URL Fetch. By implementing a
 * transport that directly uses URL Fetch, we can optimize the behavior slightly, and can
 * potentially take advantage of features in URL Fetch that are not available in {@link
 * HttpURLConnection}. Furthermore, there is currently a serious bug in how HTTP headers are
 * processed in the App Engine implementation of {@link HttpURLConnection}, which we are able to
 * avoid using this implementation. Therefore, this is the recommended transport to use on App
 * Engine.
 *
 * @since 1.10
 * @author Yaniv Inbar
 */
public final class UrlFetchTransport extends HttpTransport {

  /**
   * Certificate validation behavior to use with {@link FetchOptions#doNotValidateCertificate()} or
   * {@link FetchOptions#validateCertificate()}.
   */
  enum CertificateValidationBehavior {
    DEFAULT,
    VALIDATE,
    DO_NOT_VALIDATE
  }

  /**
   * All valid request methods as specified in {@link HTTPMethod}, sorted in ascending alphabetical
   * order.
   */
  private static final String[] SUPPORTED_METHODS = {
    HttpMethods.DELETE,
    HttpMethods.GET,
    HttpMethods.HEAD,
    HttpMethods.POST,
    HttpMethods.PUT,
    HttpMethods.PATCH
  };

  static {
    Arrays.sort(SUPPORTED_METHODS);
  }

  /** Certificate validation behavior. */
  private final CertificateValidationBehavior certificateValidationBehavior;

  /**
   * Constructor with the default fetch options.
   *
   * <p>Use {@link Builder} to modify fetch options.
   */
  public UrlFetchTransport() {
    this(new Builder());
  }

  /** @param builder builder */
  UrlFetchTransport(Builder builder) {
    certificateValidationBehavior = builder.certificateValidationBehavior;
  }

  /**
   * Returns a global thread-safe instance.
   *
   * @since 1.17
   */
  public static UrlFetchTransport getDefaultInstance() {
    return InstanceHolder.INSTANCE;
  }

  /** Holder for the result of {@link #getDefaultInstance()}. */
  static class InstanceHolder {
    static final UrlFetchTransport INSTANCE = new UrlFetchTransport();
  }

  @Override
  public boolean supportsMethod(String method) {
    return Arrays.binarySearch(SUPPORTED_METHODS, method) >= 0;
  }

  @Override
  protected UrlFetchRequest buildRequest(String method, String url) throws IOException {
    Preconditions.checkArgument(supportsMethod(method), "HTTP method %s not supported", method);
    HTTPMethod httpMethod;
    if (method.equals(HttpMethods.DELETE)) {
      httpMethod = HTTPMethod.DELETE;
    } else if (method.equals(HttpMethods.GET)) {
      httpMethod = HTTPMethod.GET;
    } else if (method.equals(HttpMethods.HEAD)) {
      httpMethod = HTTPMethod.HEAD;
    } else if (method.equals(HttpMethods.POST)) {
      httpMethod = HTTPMethod.POST;
    } else if (method.equals(HttpMethods.PATCH)) {
      httpMethod = HTTPMethod.PATCH;
    } else {
      httpMethod = HTTPMethod.PUT;
    }
    // fetch options
    FetchOptions fetchOptions =
        FetchOptions.Builder.doNotFollowRedirects().disallowTruncate().validateCertificate();
    switch (certificateValidationBehavior) {
      case VALIDATE:
        fetchOptions.validateCertificate();
        break;
      case DO_NOT_VALIDATE:
        fetchOptions.doNotValidateCertificate();
        break;
      default:
        break;
    }
    return new UrlFetchRequest(fetchOptions, httpMethod, url);
  }

  /**
   * Builder for {@link UrlFetchTransport}.
   *
   * <p>Implementation is not thread-safe.
   *
   * @since 1.13
   */
  public static final class Builder {

    /** Certificate validation behavior. */
    CertificateValidationBehavior certificateValidationBehavior =
        CertificateValidationBehavior.DEFAULT;

    /**
     * Sets whether to use {@link FetchOptions#doNotValidateCertificate()} ({@code false} by
     * default).
     *
     * <p>Be careful! Disabling certificate validation is dangerous and should be done in testing
     * environments only.
     */
    public Builder doNotValidateCertificate() {
      this.certificateValidationBehavior = CertificateValidationBehavior.DO_NOT_VALIDATE;
      return this;
    }

    /**
     * Sets whether to use {@link FetchOptions#validateCertificate()} ({@code false} by default).
     */
    public Builder validateCertificate() {
      this.certificateValidationBehavior = CertificateValidationBehavior.VALIDATE;
      return this;
    }

    /** Returns whether to use {@link FetchOptions#validateCertificate()}. */
    public boolean getValidateCertificate() {
      return certificateValidationBehavior == CertificateValidationBehavior.VALIDATE;
    }

    /** Returns whether to use {@link FetchOptions#validateCertificate()}. */
    public boolean getDoNotValidateCertificate() {
      return certificateValidationBehavior == CertificateValidationBehavior.DO_NOT_VALIDATE;
    }

    /** Returns a new instance of {@link UrlFetchTransport} based on the options. */
    public UrlFetchTransport build() {
      return new UrlFetchTransport(this);
    }
  }
}
