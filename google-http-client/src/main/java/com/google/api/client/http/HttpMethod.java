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

package com.google.api.client.http;

/**
 * HTTP request method.
 *
 * @since 1.3
 * @author Yaniv Inbar
 * @deprecated (scheduled to be removed in 1.13) Use {@link HttpMethods} instead. The purpose of
 *             this deprecation is to allow any arbitrary HTTP method to be used, rather than
 *             restrict it to a small set of HTTP methods.
 */
@Deprecated
public enum HttpMethod {
  DELETE, GET, HEAD, PATCH, PUT, POST
}
