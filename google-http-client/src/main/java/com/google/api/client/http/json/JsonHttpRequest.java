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

package com.google.api.client.http.json;

import com.google.api.client.http.HttpMethod;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.util.GenericData;
import com.google.common.base.Preconditions;

import java.io.IOException;

/**
 * JSON HTTP request to {@link JsonHttpClient}.
 *
 * @since 1.6
 * @author Ravi Mistry
 */
public class JsonHttpRequest extends GenericData {

  private final JsonHttpClient client;
  private final HttpMethod method;
  private final String uriTemplate;
  private final Object content;

  /**
   * Builds an instance of {@link JsonHttpRequest}.
   *
   * @param client The JSON HTTP client which handles this request
   * @param method HTTP Method type
   * @param uriTemplate URI template
   * @param content A POJO that can be serialized into JSON or {@code null} for none
   */
  public JsonHttpRequest(
      JsonHttpClient client, HttpMethod method, String uriTemplate, Object content) {
    this.client = Preconditions.checkNotNull(client);
    this.method = Preconditions.checkNotNull(method);
    this.uriTemplate = Preconditions.checkNotNull(uriTemplate);
    this.content = content;
  }

  /**
   * @return the {@link JsonHttpClient} which handles this request.
   */
  public final JsonHttpClient getClient() {
    return client;
  }

  /**
   * Sends the request to the server and returns the raw {@link HttpResponse}.
   *
   * @return the {@link HttpResponse}
   * @throws IOException if the request fails
   */
  public final HttpResponse executeUnparsed() throws IOException {
    return client.execute(method, uriTemplate, content, this);
  }
}
