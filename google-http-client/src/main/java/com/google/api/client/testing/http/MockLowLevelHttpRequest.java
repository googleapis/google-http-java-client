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

package com.google.api.client.testing.http;

import com.google.api.client.http.AbstractInputStreamContent;
import com.google.api.client.http.HttpContent;
import com.google.api.client.http.HttpMediaType;
import com.google.api.client.http.LowLevelHttpRequest;
import com.google.api.client.http.LowLevelHttpResponse;
import com.google.common.base.Charsets;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPInputStream;

/**
 * Mock for {@link LowLevelHttpRequest}.
 *
 * <p>
 * Implementation is not thread-safe.
 * </p>
 *
 * @author Yaniv Inbar
 * @since 1.3
 */
public class MockLowLevelHttpRequest extends LowLevelHttpRequest {

  /** Request URL or {@code null} for none. */
  private String url;

  /** HTTP content or {@code null} for none. */
  private HttpContent content;

  /** Map of header name to values. */
  private final Map<String, List<String>> headersMap = new HashMap<String, List<String>>();

  /**
   * HTTP response to return from {@link #execute()}.
   *
   * <p>
   * By default this is a new instance of {@link MockLowLevelHttpResponse}.
   * </p>
   */
  private MockLowLevelHttpResponse response = new MockLowLevelHttpResponse();

  public MockLowLevelHttpRequest() {
  }

  /**
   * @param url Request URL or {@code null} for none
   * @since 1.4
   */
  public MockLowLevelHttpRequest(String url) {
    this.url = url;
  }

  @Override
  public void addHeader(String name, String value) throws IOException {
    List<String> values = headersMap.get(name);
    if (values == null) {
      values = new ArrayList<String>();
      headersMap.put(name, values);
    }
    values.add(value);
  }

  @Override
  public LowLevelHttpResponse execute() throws IOException {
    return response;
  }

  @Override
  public void setContent(HttpContent content) throws IOException {
    this.content = content;
  }

  /**
   * Returns the request URL or {@code null} for none.
   *
   * @since 1.5
   */
  public String getUrl() {
    return url;
  }

  /**
   * Returns the map of header name to values.
   *
   * @since 1.5
   */
  public Map<String, List<String>> getHeaders() {
    return headersMap;
  }

  /**
   * Sets the request URL or {@code null} for none.
   *
   * @since 1.5
   */
  public MockLowLevelHttpRequest setUrl(String url) {
    this.url = url;
    return this;
  }

  /**
   * Returns the HTTP content or {@code null} for none.
   *
   * @since 1.5
   */
  public HttpContent getContent() {
    return content;
  }

  /**
   * Returns HTTP content as a string, taking care of any encodings of the content if necessary.
   *
   * @since 1.12
   */
  public String getContentAsString() throws IOException {
    // write content to a byte[]
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    getContent().writeTo(out);
    // determine gzip encoding
    String contentEncoding = content.getEncoding();
    if (contentEncoding != null && contentEncoding.contains("gzip")) {
      InputStream contentInputStream =
          new GZIPInputStream(new ByteArrayInputStream(out.toByteArray()));
      out = new ByteArrayOutputStream();
      AbstractInputStreamContent.copy(contentInputStream, out);
    }
    // determine charset parameter from content type
    String contentType = content.getType();
    HttpMediaType mediaType = contentType != null ? new HttpMediaType(contentType) : null;
    Charset charset = mediaType == null || mediaType.getCharsetParameter() == null
        ? Charsets.ISO_8859_1 : mediaType.getCharsetParameter();
    return out.toString(charset.name());
  }

  /**
   * HTTP response to return from {@link #execute()}.
   *
   * @since 1.8
   */
  public MockLowLevelHttpResponse getResponse() {
    return response;
  }

  /**
   * Sets the HTTP response to return from {@link #execute()}.
   *
   * <p>
   * By default this is a new instance of {@link MockLowLevelHttpResponse}.
   * </p>
   */
  public MockLowLevelHttpRequest setResponse(MockLowLevelHttpResponse response) {
    this.response = response;
    return this;
  }
}
