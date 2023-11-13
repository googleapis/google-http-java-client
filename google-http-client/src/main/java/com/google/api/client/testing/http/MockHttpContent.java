/*
 * Copyright 2010 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.api.client.testing.http;

import com.google.api.client.http.HttpContent;
import com.google.api.client.util.Beta;
import com.google.api.client.util.Preconditions;
import java.io.IOException;
import java.io.OutputStream;

/**
 * {@link Beta} <br>
 * Mock for {@link HttpContent}.
 *
 * <p>Implementation is not thread-safe.
 *
 * @author Yaniv Inbar
 * @since 1.3
 */
@Beta
public class MockHttpContent implements HttpContent {

  /** HTTP content length or {@code -1} by default. */
  private long length = -1;

  /** HTTP content type or {@code null} by default. */
  private String type;

  /** HTTP content or an empty byte array by default. */
  private byte[] content = new byte[0];

  public long getLength() throws IOException {
    return length;
  }

  public String getType() {
    return type;
  }

  public void writeTo(OutputStream out) throws IOException {
    out.write(content);
    out.flush();
  }

  public boolean retrySupported() {
    return true;
  }

  /**
   * Returns the HTTP content.
   *
   * @since 1.5
   */
  public final byte[] getContent() {
    return content;
  }

  /**
   * Sets the HTTP content.
   *
   * <p>Default value is an empty byte array.
   *
   * @since 1.5
   */
  public MockHttpContent setContent(byte[] content) {
    this.content = Preconditions.checkNotNull(content);
    return this;
  }

  /**
   * Returns the HTTP content length or {@code -1} for unknown.
   *
   * <p>Default value is {@code -1}.
   *
   * @since 1.5
   */
  public MockHttpContent setLength(long length) {
    Preconditions.checkArgument(length >= -1);
    this.length = length;
    return this;
  }

  /**
   * Sets the HTTP content type or {@code null} for none.
   *
   * @since 1.5
   */
  public MockHttpContent setType(String type) {
    this.type = type;
    return this;
  }
}
