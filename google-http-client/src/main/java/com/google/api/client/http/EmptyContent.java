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

package com.google.api.client.http;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Empty HTTP content of length zero just to force {@link HttpRequest#execute()} to add the header
 * {@code Content-Length: 0}.
 *
 * <p>Note that there is no {@code Content-Length} header if the HTTP request content is {@code
 * null} . However, when making a request like PUT or POST without a {@code Content-Length} header,
 * some servers may respond with a {@code 411 Length Required} error. Specifying the {@code
 * Content-Length: 0} header may work around that problem.
 *
 * @since 1.11
 * @author Yaniv Inbar
 */
public class EmptyContent implements HttpContent {

  public long getLength() throws IOException {
    return 0;
  }

  public String getType() {
    return null;
  }

  public void writeTo(OutputStream out) throws IOException {
    out.flush();
  }

  public boolean retrySupported() {
    return true;
  }
}
