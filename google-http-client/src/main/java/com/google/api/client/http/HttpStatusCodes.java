/*
 * Copyright (c) 2011 Google Inc.
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

package com.google.api.client.http;

/**
 * Constants enumerating the HTTP status codes. Includes status codes specified in <a
 * href="http://tools.ietf.org/html/rfc2616#section-10.3">RFC2616</a> (HTTP/1.1).
 *
 * @since 1.6
 * @author Ravi Mistry
 */
public class HttpStatusCodes {

  /** Status code for a successful request. */
  public static final int STATUS_CODE_OK = 200;

  /** Status code for a successful request that has been fulfilled to create a new resource. */
  public static final int STATUS_CODE_CREATED = 201;

  /** Status code for a successful request that has been received but not yet acted upon. */
  public static final int STATUS_CODE_ACCEPTED = 202;

  /**
   * Status code for a successful request with no content information.
   *
   * @since 1.11
   */
  public static final int STATUS_CODE_NO_CONTENT = 204;

  /** Status code for a resource corresponding to any one of a set of representations. */
  public static final int STATUS_CODE_MULTIPLE_CHOICES = 300;

  /** Status code for a resource that has permanently moved to a new URI. */
  public static final int STATUS_CODE_MOVED_PERMANENTLY = 301;

  /** Status code for a resource that has temporarily moved to a new URI. */
  public static final int STATUS_CODE_FOUND = 302;

  /** Status code for a resource that has moved to a new URI and should be retrieved using GET. */
  public static final int STATUS_CODE_SEE_OTHER = 303;

  /** Status code for a resource that access is allowed but the document has not been modified. */
  public static final int STATUS_CODE_NOT_MODIFIED = 304;

  /** Status code for a resource that has temporarily moved to a new URI. */
  public static final int STATUS_CODE_TEMPORARY_REDIRECT = 307;

  /** Status code for a resource that has permanently moved to a new URI. */
  private static final int STATUS_CODE_PERMANENT_REDIRECT = 308;

  /** Status code for a request that could not be understood by the server. */
  public static final int STATUS_CODE_BAD_REQUEST = 400;

  /** Status code for a request that requires user authentication. */
  public static final int STATUS_CODE_UNAUTHORIZED = 401;

  /** Status code for a server that understood the request, but is refusing to fulfill it. */
  public static final int STATUS_CODE_FORBIDDEN = 403;

  /** Status code for a server that has not found anything matching the Request-URI. */
  public static final int STATUS_CODE_NOT_FOUND = 404;

  /**
   * Status code for a method specified in the Request-Line is not allowed for the resource
   * identified by the Request-URI.
   */
  public static final int STATUS_CODE_METHOD_NOT_ALLOWED = 405;

  /** Status code for a request that could not be completed due to a resource conflict. */
  public static final int STATUS_CODE_CONFLICT = 409;

  /** Status code for a request for which one of the conditions it was made under has failed. */
  public static final int STATUS_CODE_PRECONDITION_FAILED = 412;

  /**
   * Status code for a request for which the content-type and the request's syntax were correct but
   * server was not able to process entity.
   */
  public static final int STATUS_CODE_UNPROCESSABLE_ENTITY = 422;

  /** Status code for an internal server error. */
  public static final int STATUS_CODE_SERVER_ERROR = 500;

  /**
   * Status code for a bad gateway.
   *
   * @since 1.16
   */
  public static final int STATUS_CODE_BAD_GATEWAY = 502;

  /** Status code for a service that is unavailable on the server. */
  public static final int STATUS_CODE_SERVICE_UNAVAILABLE = 503;

  /**
   * Returns whether the given HTTP response status code is a success code {@code >= 200 and < 300}.
   */
  public static boolean isSuccess(int statusCode) {
    return statusCode >= STATUS_CODE_OK && statusCode < STATUS_CODE_MULTIPLE_CHOICES;
  }

  /**
   * Returns whether the given HTTP response status code is a redirect code {@code 301, 302, 303,
   * 307, 308}.
   *
   * @since 1.11
   */
  public static boolean isRedirect(int statusCode) {
    switch (statusCode) {
      case HttpStatusCodes.STATUS_CODE_MOVED_PERMANENTLY: // 301
      case HttpStatusCodes.STATUS_CODE_FOUND: // 302
      case HttpStatusCodes.STATUS_CODE_SEE_OTHER: // 303
      case HttpStatusCodes.STATUS_CODE_TEMPORARY_REDIRECT: // 307
      case HttpStatusCodes.STATUS_CODE_PERMANENT_REDIRECT: // 308
        return true;
      default:
        return false;
    }
  }
}
