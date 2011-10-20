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

package com.google.api.client.testing.http;

import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.HttpUnsuccessfulResponseHandler;

import java.io.IOException;

/**
 * Mock for {@link HttpUnsuccessfulResponseHandler}.
 *
 * <p>
 * Contains an {@link #isCalled} method that returns true if {@link #handleResponse} is called.
 * </p>
 *
 * @author Ravi Mistry
 * @since 1.6
 */
public class MockHttpUnsuccessfulResponseHandler implements HttpUnsuccessfulResponseHandler {

  private boolean isCalled;
  private boolean successfullyHandleResponse;

  /**
   * Create an instance of {@code MockHttpUnsuccessfulResponseHandler}.
   *
   * @param successfullyHandleResponse This will be the return value of {@link #handleResponse}
   */
  public MockHttpUnsuccessfulResponseHandler(boolean successfullyHandleResponse) {
    this.successfullyHandleResponse = successfullyHandleResponse;
  }

  /**
   * Returns whether the {@link #handleResponse} method was called or not.
   */
  public boolean isCalled() {
    return isCalled;
  }

  public boolean handleResponse(HttpRequest request, HttpResponse response, boolean retrySupported)
      throws IOException {
    isCalled = true;
    return successfullyHandleResponse;
  }
}
