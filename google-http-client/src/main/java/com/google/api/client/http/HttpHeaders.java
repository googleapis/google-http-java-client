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

package com.google.api.client.http;

import com.google.api.client.util.Base64;
import com.google.api.client.util.ClassInfo;
import com.google.api.client.util.GenericData;
import com.google.api.client.util.Key;
import com.google.api.client.util.Strings;

import java.util.HashMap;

/**
 * Stores HTTP headers used in an HTTP request or response, as defined in <a
 * href="http://tools.ietf.org/html/rfc2616#section-14">Header Field Definitions</a>.
 *
 * <p>
 * {@code null} is not allowed as a name or value of a header. Names are case-insensitive.
 * </p>
 *
 * <p>
 * Implementation is not thread-safe.
 * </p>
 *
 * @since 1.0
 * @author Yaniv Inbar
 */
public class HttpHeaders extends GenericData {

  /** {@code "Accept"} header. */
  @Key("Accept")
  private String accept;

  /** {@code "Accept-Encoding"} header. */
  @Key("Accept-Encoding")
  private String acceptEncoding = "gzip";

  /** {@code "Authorization"} header. */
  @Key("Authorization")
  private String authorization;

  /** {@code "Cache-Control"} header. */
  @Key("Cache-Control")
  private String cacheControl;

  /** {@code "Content-Encoding"} header. */
  @Key("Content-Encoding")
  private String contentEncoding;

  /** {@code "Content-Length"} header. */
  @Key("Content-Length")
  private String contentLength;

  /** {@code "Content-MD5"} header. */
  @Key("Content-MD5")
  private String contentMD5;

  /** {@code "Content-Range"} header. */
  @Key("Content-Range")
  private String contentRange;

  /** {@code "Content-Type"} header. */
  @Key("Content-Type")
  private String contentType;

  /** {@code "Cookie"} header. */
  @Key("Cookie")
  private String cookie;

  /** {@code "Date"} header. */
  @Key("Date")
  private String date;

  /** {@code "ETag"} header. */
  @Key("ETag")
  private String etag;

  /** {@code "Expires"} header. */
  @Key("Expires")
  private String expires;

  /** {@code "If-Modified-Since"} header. */
  @Key("If-Modified-Since")
  private String ifModifiedSince;

  /** {@code "If-Match"} header. */
  @Key("If-Match")
  private String ifMatch;

  /** {@code "If-None-Match"} header. */
  @Key("If-None-Match")
  private String ifNoneMatch;

  /** {@code "If-Unmodified-Since"} header. */
  @Key("If-Unmodified-Since")
  private String ifUnmodifiedSince;

  /** {@code "Last-Modified"} header. */
  @Key("Last-Modified")
  private String lastModified;

  /** {@code "Location"} header. */
  @Key("Location")
  private String location;

  /** {@code "MIME-Version"} header. */
  @Key("MIME-Version")
  private String mimeVersion;

  /** {@code "Range"} header. */
  @Key("Range")
  private String range;

  /** {@code "Retry-After"} header. */
  @Key("Retry-After")
  private String retryAfter;

  /** {@code "User-Agent"} header. */
  @Key("User-Agent")
  private String userAgent;

  /** {@code "WWW-Authenticate"} header. */
  @Key("WWW-Authenticate")
  private String authenticate;

  @Override
  public HttpHeaders clone() {
    return (HttpHeaders) super.clone();
  }

  /**
   * Returns the {@code "Accept"} header or {@code null} for none.
   *
   * @since 1.5
   */
  public final String getAccept() {
    return accept;
  }

  /**
   * Sets the {@code "Accept"} header or {@code null} for none.
   *
   * @since 1.5
   */
  public final void setAccept(String accept) {
    this.accept = accept;
  }

  /**
   * Returns the {@code "Accept-Encoding"} header or {@code null} for none.
   *
   * @since 1.5
   */
  public final String getAcceptEncoding() {
    return acceptEncoding;
  }

  /**
   * Sets the {@code "Accept-Encoding"} header or {@code null} for none.
   *
   * <p>
   * By default, this is {@code "gzip"}.
   * </p>
   *
   * @since 1.5
   */
  public final void setAcceptEncoding(String acceptEncoding) {
    this.acceptEncoding = acceptEncoding;
  }

  /**
   * Returns the {@code "Authorization"} header or {@code null} for none.
   *
   * @since 1.5
   */
  public final String getAuthorization() {
    return authorization;
  }

  /**
   * Sets the {@code "Authorization"} header or {@code null} for none.
   *
   * @since 1.5
   */
  public final void setAuthorization(String authorization) {
    this.authorization = authorization;
  }

  /**
   * Returns the {@code "Cache-Control"} header or {@code null} for none.
   *
   * @since 1.5
   */
  public final String getCacheControl() {
    return cacheControl;
  }

  /**
   * Sets the {@code "Cache-Control"} header or {@code null} for none.
   *
   * @since 1.5
   */
  public final void setCacheControl(String cacheControl) {
    this.cacheControl = cacheControl;
  }

  /**
   * Returns the {@code "Content-Encoding"} header or {@code null} for none.
   *
   * @since 1.5
   */
  public final String getContentEncoding() {
    return contentEncoding;
  }

  /**
   * Sets the {@code "Content-Encoding"} header or {@code null} for none.
   *
   * @since 1.5
   */
  public final void setContentEncoding(String contentEncoding) {
    this.contentEncoding = contentEncoding;
  }

  /**
   * Returns the {@code "Content-Length"} header or {@code null} for none.
   *
   * @since 1.5
   */
  public final String getContentLength() {
    return contentLength;
  }

  /**
   * Sets the {@code "Content-Length"} header or {@code null} for none.
   *
   * @since 1.5
   */
  public final void setContentLength(String contentLength) {
    this.contentLength = contentLength;
  }

  /**
   * Returns the {@code "Content-MD5"} header or {@code null} for none.
   *
   * @since 1.5
   */
  public final String getContentMD5() {
    return contentMD5;
  }

  /**
   * Sets the {@code "Content-MD5"} header or {@code null} for none.
   *
   * @since 1.5
   */
  public final void setContentMD5(String contentMD5) {
    this.contentMD5 = contentMD5;
  }

  /**
   * Returns the {@code "Content-Range"} header or {@code null} for none.
   *
   * @since 1.5
   */
  public final String getContentRange() {
    return contentRange;
  }

  /**
   * Sets the {@code "Content-Range"} header or {@code null} for none.
   *
   * @since 1.5
   */
  public final void setContentRange(String contentRange) {
    this.contentRange = contentRange;
  }

  /**
   * Returns the {@code "Content-Type"} header or {@code null} for none.
   *
   * @since 1.5
   */
  public final String getContentType() {
    return contentType;
  }

  /**
   * Sets the {@code "Content-Type"} header or {@code null} for none.
   *
   * @since 1.5
   */
  public final void setContentType(String contentType) {
    this.contentType = contentType;
  }

  /**
   * Returns the {@code "Cookie"} header or {@code null} for none.
   * <a href='http://tools.ietf.org/html/rfc6265'>See Cookie Specification.</a>
   * @since 1.6
   */
  public final String getCookie() {
    return cookie;
  }

  /**
   * Sets the {@code "Cookie"} header or {@code null} for none.
   *
   * @since 1.6
   */
  public final void setCookie(String cookie) {
    this.cookie = cookie;
  }

  /**
   * Returns the {@code "Date"} header or {@code null} for none.
   *
   * @since 1.5
   */
  public final String getDate() {
    return date;
  }

  /**
   * Sets the {@code "Date"} header or {@code null} for none.
   *
   * @since 1.5
   */
  public final void setDate(String date) {
    this.date = date;
  }

  /**
   * Returns the {@code "ETag"} header or {@code null} for none.
   *
   * @since 1.5
   */
  public final String getETag() {
    return etag;
  }

  /**
   * Sets the {@code "ETag"} header or {@code null} for none.
   *
   * @since 1.5
   */
  public final void setETag(String etag) {
    this.etag = etag;
  }

  /**
   * Returns the {@code "Expires"} header or {@code null} for none.
   *
   * @since 1.5
   */
  public final String getExpires() {
    return expires;
  }

  /**
   * Sets the {@code "Expires"} header or {@code null} for none.
   *
   * @since 1.5
   */
  public final void setExpires(String expires) {
    this.expires = expires;
  }

  /**
   * Returns the {@code "If-Modified-Since"} header or {@code null} for none.
   *
   * @since 1.5
   */
  public final String getIfModifiedSince() {
    return ifModifiedSince;
  }

  /**
   * Sets the {@code "If-Modified-Since"} header or {@code null} for none.
   *
   * @since 1.5
   */
  public final void setIfModifiedSince(String ifModifiedSince) {
    this.ifModifiedSince = ifModifiedSince;
  }

  /**
   * Returns the {@code "If-Match"} header or {@code null} for none.
   *
   * @since 1.5
   */
  public final String getIfMatch() {
    return ifMatch;
  }

  /**
   * Sets the {@code "If-Match"} header or {@code null} for none.
   *
   * @since 1.5
   */
  public final void setIfMatch(String ifMatch) {
    this.ifMatch = ifMatch;
  }

  /**
   * Returns the {@code "If-None-Match"} header or {@code null} for none.
   *
   * @since 1.5
   */
  public final String getIfNoneMatch() {
    return ifNoneMatch;
  }

  /**
   * Sets the {@code "If-None-Match"} header or {@code null} for none.
   *
   * @since 1.5
   */
  public final void setIfNoneMatch(String ifNoneMatch) {
    this.ifNoneMatch = ifNoneMatch;
  }

  /**
   * Returns the {@code "If-Unmodified-Since"} header or {@code null} for none.
   *
   * @since 1.5
   */
  public final String getIfUnmodifiedSince() {
    return ifUnmodifiedSince;
  }

  /**
   * Sets the {@code "If-Unmodified-Since"} header or {@code null} for none.
   *
   * @since 1.5
   */
  public final void setIfUnmodifiedSince(String ifUnmodifiedSince) {
    this.ifUnmodifiedSince = ifUnmodifiedSince;
  }

  /**
   * Returns the {@code "Last-Modified"} header or {@code null} for none.
   *
   * @since 1.5
   */
  public final String getLastModified() {
    return lastModified;
  }

  /**
   * Sets the {@code "Last-Modified"} header or {@code null} for none.
   *
   * @since 1.5
   */
  public final void setLastModified(String lastModified) {
    this.lastModified = lastModified;
  }

  /**
   * Returns the {@code "Location"} header or {@code null} for none.
   *
   * @since 1.5
   */
  public final String getLocation() {
    return location;
  }

  /**
   * Sets the {@code "Location"} header or {@code null} for none.
   *
   * @since 1.5
   */
  public final void setLocation(String location) {
    this.location = location;
  }

  /**
   * Returns the {@code "MIME-Version"} header or {@code null} for none.
   *
   * @since 1.5
   */
  public final String getMimeVersion() {
    return mimeVersion;
  }

  /**
   * Sets the {@code "MIME-Version"} header or {@code null} for none.
   *
   * @since 1.5
   */
  public final void setMimeVersion(String mimeVersion) {
    this.mimeVersion = mimeVersion;
  }

  /**
   * Returns the {@code "Range"} header or {@code null} for none.
   *
   * @since 1.5
   */
  public final String getRange() {
    return range;
  }

  /**
   * Sets the {@code "Range"} header or {@code null} for none.
   *
   * @since 1.5
   */
  public final void setRange(String range) {
    this.range = range;
  }

  /**
   * Returns the {@code "Retry-After"} header or {@code null} for none.
   *
   * @since 1.5
   */
  public final String getRetryAfter() {
    return retryAfter;
  }

  /**
   * Sets the {@code "Retry-After"} header or {@code null} for none.
   *
   * @since 1.5
   */
  public final void setRetryAfter(String retryAfter) {
    this.retryAfter = retryAfter;
  }

  /**
   * Returns the {@code "User-Agent"} header or {@code null} for none.
   *
   * @since 1.5
   */
  public final String getUserAgent() {
    return userAgent;
  }

  /**
   * Sets the {@code "User-Agent"} header or {@code null} for none.
   *
   * @since 1.5
   */
  public final void setUserAgent(String userAgent) {
    this.userAgent = userAgent;
  }

  /**
   * Returns the {@code "WWW-Authenticate"} header or {@code null} for none.
   *
   * @since 1.5
   */
  public final String getAuthenticate() {
    return authenticate;
  }

  /**
   * Sets the {@code "WWW-Authenticate"} header or {@code null} for none.
   *
   * @since 1.5
   */
  public final void setAuthenticate(String authenticate) {
    this.authenticate = authenticate;
  }

  /**
   * Sets the {@link #authorization} header as specified in <a
   * href="http://tools.ietf.org/html/rfc2617#section-2">Basic Authentication Scheme</a>.
   *
   * @since 1.2
   */
  public final void setBasicAuthentication(String username, String password) {
    String encoded =
        Strings.fromBytesUtf8(Base64.encode(Strings.toBytesUtf8(username + ":" + password)));
    authorization = "Basic " + encoded;
  }

  /**
   * Returns the map from lower-case field name to field name used to allow for case insensitive
   * HTTP headers for the given HTTP headers class.
   */
  static HashMap<String, String> getFieldNameMap(Class<? extends HttpHeaders> headersClass) {
    HashMap<String, String> fieldNameMap = new HashMap<String, String>();
    for (String keyName : ClassInfo.of(headersClass).getNames()) {
      fieldNameMap.put(keyName.toLowerCase(), keyName);
    }
    return fieldNameMap;
  }

  // TODO(yanivi): override equals and hashCode
}
