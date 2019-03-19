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

import com.google.api.client.http.HttpMediaType;
import com.google.api.client.http.LowLevelHttpRequest;
import com.google.api.client.http.LowLevelHttpResponse;
import com.google.api.client.util.Beta;
import com.google.api.client.util.Charsets;
import com.google.api.client.util.IOUtils;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.zip.GZIPInputStream;

/**
 * {@link Beta} <br>
 * Mock for {@link LowLevelHttpRequest}.
 *
 * <p>Implementation is not thread-safe.
 *
 * @author Yaniv Inbar
 * @since 1.3
 */
@Beta
public class MockLowLevelHttpRequest extends LowLevelHttpRequest {

  /** Request URL or {@code null} for none. */
  private String url;

  /** Map of lowercase header name to values. */
  private final Map<String, List<String>> headersMap = new HashMap<String, List<String>>();

  /**
   * HTTP response to return from {@link #execute()}.
   *
   * <p>By default this is a new instance of {@link MockLowLevelHttpResponse}.
   */
  private MockLowLevelHttpResponse response = new MockLowLevelHttpResponse();

  public MockLowLevelHttpRequest() {}

  /**
   * @param url Request URL or {@code null} for none
   * @since 1.4
   */
  public MockLowLevelHttpRequest(String url) {
    this.url = url;
  }

  @Override
  public void addHeader(String name, String value) throws IOException {
    name = name.toLowerCase(Locale.US);
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

  /**
   * Returns the request URL or {@code null} for none.
   *
   * @since 1.5
   */
  public String getUrl() {
    return url;
  }

  /**
   * Returns an unmodifiable view of the map of lowercase header name to values.
   *
   * <p>Note that unlike this method, {@link #getFirstHeaderValue(String)} and {@link
   * #getHeaderValues(String)} are not case sensitive with respect to the input header name.
   *
   * @since 1.5
   */
  public Map<String, List<String>> getHeaders() {
    return Collections.unmodifiableMap(headersMap);
  }

  /**
   * Returns the value of the first header of the given name or {@code null} for none.
   *
   * @param name header name (may be any case)
   * @since 1.13
   */
  public String getFirstHeaderValue(String name) {
    List<String> values = headersMap.get(name.toLowerCase(Locale.US));
    return values == null ? null : values.get(0);
  }

  /**
   * Returns the unmodifiable list of values of the headers of the given name (may be empty).
   *
   * @param name header name (may be any case)
   * @since 1.13
   */
  public List<String> getHeaderValues(String name) {
    List<String> values = headersMap.get(name.toLowerCase(Locale.US));
    return values == null ? Collections.<String>emptyList() : Collections.unmodifiableList(values);
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
   * Returns HTTP content as a string, taking care of any encodings of the content if necessary.
   *
   * <p>Returns an empty string if there is no HTTP content.
   *
   * @since 1.12
   */
  public String getContentAsString() throws IOException {
    if (getStreamingContent() == null) {
      return "";
    }
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
    Charset charset =
        mediaType == null || mediaType.getCharsetParameter() == null
            ? Charsets.ISO_8859_1
            : mediaType.getCharsetParameter();
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
   * <p>By default this is a new instance of {@link MockLowLevelHttpResponse}.
   */
  public MockLowLevelHttpRequest setResponse(MockLowLevelHttpResponse response) {
    this.response = response;
    return this;
  }
}
