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

package com.google.api.client.testing.http.javanet;

import com.google.api.client.util.Experimental;
import com.google.api.client.util.Preconditions;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.UnknownServiceException;

/**
 * {@link Experimental} <br/>
 * Mock for {@link HttpURLConnection}.
 *
 * <p>
 * Implementation is not thread-safe.
 * </p>
 *
 * @since 1.11
 * @author Yaniv Inbar
 */
@Experimental
public class MockHttpURLConnection extends HttpURLConnection {

  /** Whether {@link #doOutput} was called. */
  private boolean doOutputCalled;

  /**
   * Output stream or {@code null} to throw an {@link UnknownServiceException} when
   * {@link #getOutputStream()} is called.
   */
  private OutputStream outputStream = new ByteArrayOutputStream(0);

  /**
   * @param u the URL or {@code null} for none
   */
  public MockHttpURLConnection(URL u) {
    super(u);
  }

  @Override
  public void disconnect() {
  }

  @Override
  public boolean usingProxy() {
    return false;
  }

  @Override
  public void connect() throws IOException {
  }

  @Override
  public int getResponseCode() throws IOException {
    return responseCode;
  }

  @Override
  public void setDoOutput(boolean dooutput) {
    doOutputCalled = true;
  }

  @Override
  public OutputStream getOutputStream() throws IOException {
    if (outputStream != null) {
      return outputStream;
    }
    return super.getOutputStream();
  }

  /** Returns whether {@link #doOutput} was called. */
  public final boolean doOutputCalled() {
    return doOutputCalled;
  }

  /**
   * Sets the output stream or {@code null} to throw an {@link UnknownServiceException} when
   * {@link #getOutputStream()} is called.
   *
   * <p>
   * By default it is {@code null}.
   * </p>
   */
  public MockHttpURLConnection setOutputStream(OutputStream outputStream) {
    this.outputStream = outputStream;
    return this;
  }

  /** Sets the HTTP response status code. */
  public MockHttpURLConnection setResponseCode(int responseCode) {
    Preconditions.checkArgument(responseCode >= -1);
    this.responseCode = responseCode;
    return this;
  }
}
