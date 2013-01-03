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

import com.google.api.client.http.HttpContent;
import com.google.api.client.http.HttpMediaType;
import com.google.api.client.http.LowLevelHttpRequest;
import com.google.api.client.http.LowLevelHttpResponse;
import com.google.api.client.util.IOUtils;
import com.google.common.base.Charsets;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
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
  @Deprecated
  private HttpContent content;

  /** Map of header name to values. */
  @Deprecated
  private final Map<String, List<String>> headersMap = new HashMap<String, List<String>>();

  /** List multimap of lower-case header name to its values. */
  private final ListMultimap<String, String> headersMultimap = ArrayListMultimap.create();

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
    headersMultimap.put(name.toLowerCase(), value);
  }

  @Override
  public LowLevelHttpResponse execute() throws IOException {
    return response;
  }

  @Override
  @Deprecated
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
   * @deprecated (scheduled in 1.14 to have the return type changed to ListMultimap<String, String>
   *             with lowercase header names) Use {@link #getFirstHeaderValue(String)} or
   *             {@link #getHeaderValues(String)}
   */
  @Deprecated
  public Map<String, List<String>> getHeaders() {
    return headersMap;
  }

  /**
   * Returns the value of the first header of the given name or {@code null} for none.
   *
   * @param name header name (may be any case)
   * @since 1.13
   */
  public String getFirstHeaderValue(String name) {
    List<String> values = getHeaderValues(name);
    return values.isEmpty() ? null : values.get(0);
  }

  /**
   * Returns the unmodifiable list of values of the headers of the given name (may be empty).
   *
   * @param name header name (may be any case)
   * @since 1.13
   */
  public List<String> getHeaderValues(String name) {
    return Collections.unmodifiableList(headersMultimap.get(name.toLowerCase()));
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
   * @deprecated (scheduled to be removed in 1.15) Use {@link #getContentLength()},
   *             {@link #getContentEncoding()}, {@link #getContentType()}, or
   *             {@link #getStreamingContent()} instead.
   */
  @Deprecated
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
    getStreamingContent().writeTo(out);
    // determine gzip encoding
    String contentEncoding = getContentEncoding();
    if (contentEncoding != null && contentEncoding.contains("gzip")) {
      InputStream contentInputStream =
          new GZIPInputStream(new ByteArrayInputStream(out.toByteArray()));
      out = new ByteArrayOutputStream();
      IOUtils.copy(contentInputStream, out);
    }
    // determine charset parameter from content type
    String contentType = getContentType();
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
