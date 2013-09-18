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

package com.google.api.client.http.json;

import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;

/**
 * HTTP JSON context which contains the transport layer, the JSON factory and the user agent header
 * which are going to be used in the different requests.
 *
 * @author Nick Miceli
 * @author Eyal Peled
 * @since 1.18
 */
public interface HttpJsonContext {

  /** Returns the transport layer. */
  HttpTransport getTransport();

  /** Returns the JSON factory. */
  JsonFactory getJsonFactory();

  /** Returns the user-agent header which is going to be used in every HTTP request. */
  String getUserAgent();
}
