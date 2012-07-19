/**
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


/**
 * JSON HTTP request initializer.
 *
 * <p>
 * For example, this might be used to set prettyPrint and api key:
 * </p>
 *
 * <pre>
   public class BooksRequestInitializer implements JsonHttpRequestInitializer {
     public void initialize(JsonHttpRequest request) {
       BooksRequest booksRequest = (BooksRequest)request;
       booksRequest.setPrettyPrint(true);
       booksRequest.setKey(API_KEY);
     }
   }
 * </pre>
 *
 * <p>
 * Implementations should normally be thread-safe.
 * </p>
 *
 * @since 1.6
 * @author Ravi Mistry
 */
public interface JsonHttpRequestInitializer {

  /**
   * Initializes a {@link JsonHttpRequest}.
   *
   * <p>
   * Upgrade warning: this method now throws an {@link Exception}.  In prior version 1.10 it threw
   * an {@link java.io.IOException}.
   * </p>
   *
   * @param request Remote request.
   */
  void initialize(JsonHttpRequest request) throws Exception;
}
