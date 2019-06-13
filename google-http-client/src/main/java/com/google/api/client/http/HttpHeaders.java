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

import com.google.api.client.util.ArrayValueMap;
import com.google.api.client.util.Base64;
import com.google.api.client.util.ClassInfo;
import com.google.api.client.util.Data;
import com.google.api.client.util.FieldInfo;
import com.google.api.client.util.GenericData;
import com.google.api.client.util.Key;
import com.google.api.client.util.Preconditions;
import com.google.api.client.util.StringUtils;
import com.google.api.client.util.Throwables;
import com.google.api.client.util.Types;
import java.io.IOException;
import java.io.Writer;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Stores HTTP headers used in an HTTP request or response, as defined in <a
 * href="http://tools.ietf.org/html/rfc2616#section-14">Header Field Definitions</a>.
 *
 * <p>{@code null} is not allowed as a name or value of a header. Names are case-insensitive.
 *
 * <p>Implementation is not thread-safe.
 *
 * @since 1.0
 * @author Yaniv Inbar
 */
public class HttpHeaders extends GenericData {

  public HttpHeaders() {
    super(EnumSet.of(Flags.IGNORE_CASE));
  }

  /** {@code "Accept"} header. */
  @Key("Accept")
  private List<String> accept;

  /** {@code "Accept-Encoding"} header. */
  @Key("Accept-Encoding")
  private List<String> acceptEncoding = new ArrayList<String>(Collections.singleton("gzip"));

  /** {@code "Authorization"} header. */
  @Key("Authorization")
  private List<String> authorization;

  /** {@code "Cache-Control"} header. */
  @Key("Cache-Control")
  private List<String> cacheControl;

  /** {@code "Content-Encoding"} header. */
  @Key("Content-Encoding")
  private List<String> contentEncoding;

  /** {@code "Content-Length"} header. */
  @Key("Content-Length")
  private List<Long> contentLength;

  /** {@code "Content-MD5"} header. */
  @Key("Content-MD5")
  private List<String> contentMD5;

  /** {@code "Content-Range"} header. */
  @Key("Content-Range")
  private List<String> contentRange;

  /** {@code "Content-Type"} header. */
  @Key("Content-Type")
  private List<String> contentType;

  /** {@code "Cookie"} header. */
  @Key("Cookie")
  private List<String> cookie;

  /** {@code "Date"} header. */
  @Key("Date")
  private List<String> date;

  /** {@code "ETag"} header. */
  @Key("ETag")
  private List<String> etag;

  /** {@code "Expires"} header. */
  @Key("Expires")
  private List<String> expires;

  /** {@code "If-Modified-Since"} header. */
  @Key("If-Modified-Since")
  private List<String> ifModifiedSince;

  /** {@code "If-Match"} header. */
  @Key("If-Match")
  private List<String> ifMatch;

  /** {@code "If-None-Match"} header. */
  @Key("If-None-Match")
  private List<String> ifNoneMatch;

  /** {@code "If-Unmodified-Since"} header. */
  @Key("If-Unmodified-Since")
  private List<String> ifUnmodifiedSince;

  /** {@code "If-Range"} header. */
  @Key("If-Range")
  private List<String> ifRange;

  /** {@code "Last-Modified"} header. */
  @Key("Last-Modified")
  private List<String> lastModified;

  /** {@code "Location"} header. */
  @Key("Location")
  private List<String> location;

  /** {@code "MIME-Version"} header. */
  @Key("MIME-Version")
  private List<String> mimeVersion;

  /** {@code "Range"} header. */
  @Key("Range")
  private List<String> range;

  /** {@code "Retry-After"} header. */
  @Key("Retry-After")
  private List<String> retryAfter;

  /** {@code "User-Agent"} header. */
  @Key("User-Agent")
  private List<String> userAgent;

  /** {@code "Warning"} header. */
  @Key("Warning")
  private List<String> warning;

  /** {@code "WWW-Authenticate"} header. */
  @Key("WWW-Authenticate")
  private List<String> authenticate;

  /** {@code "Age"} header. */
  @Key("Age")
  private List<Long> age;

  @Override
  public HttpHeaders clone() {
    return (HttpHeaders) super.clone();
  }

  @Override
  public HttpHeaders set(String fieldName, Object value) {
    return (HttpHeaders) super.set(fieldName, value);
  }

  /**
   * Returns the first {@code "Accept"} header or {@code null} for none.
   *
   * @since 1.5
   */
  public final String getAccept() {
    return getFirstHeaderValue(accept);
  }

  /**
   * Sets the {@code "Accept"} header or {@code null} for none.
   *
   * <p>Overriding is only supported for the purpose of calling the super implementation and
   * changing the return type, but nothing else.
   *
   * @since 1.5
   */
  public HttpHeaders setAccept(String accept) {
    this.accept = getAsList(accept);
    return this;
  }

  /**
   * Returns the first {@code "Accept-Encoding"} header or {@code null} for none.
   *
   * @since 1.5
   */
  public final String getAcceptEncoding() {
    return getFirstHeaderValue(acceptEncoding);
  }

  /**
   * Sets the {@code "Accept-Encoding"} header or {@code null} for none.
   *
   * <p>By default, this is {@code "gzip"}.
   *
   * @since 1.5
   */
  public HttpHeaders setAcceptEncoding(String acceptEncoding) {
    this.acceptEncoding = getAsList(acceptEncoding);
    return this;
  }

  /**
   * Returns the first {@code "Authorization"} header or {@code null} for none.
   *
   * @since 1.5
   */
  public final String getAuthorization() {
    return getFirstHeaderValue(authorization);
  }

  /**
   * Returns all {@code "Authorization"} headers or {@code null} for none.
   *
   * @since 1.13
   */
  public final List<String> getAuthorizationAsList() {
    return authorization;
  }

  /**
   * Sets the {@code "Authorization"} header or {@code null} for none.
   *
   * <p>Overriding is only supported for the purpose of calling the super implementation and
   * changing the return type, but nothing else.
   *
   * @since 1.5
   */
  public HttpHeaders setAuthorization(String authorization) {
    return setAuthorization(getAsList(authorization));
  }

  /**
   * Sets the {@code "Authorization"} header or {@code null} for none.
   *
   * <p>Overriding is only supported for the purpose of calling the super implementation and
   * changing the return type, but nothing else.
   *
   * @since 1.13
   */
  public HttpHeaders setAuthorization(List<String> authorization) {
    this.authorization = authorization;
    return this;
  }

  /**
   * Returns the first {@code "Cache-Control"} header or {@code null} for none.
   *
   * @since 1.5
   */
  public final String getCacheControl() {
    return getFirstHeaderValue(cacheControl);
  }

  /**
   * Sets the {@code "Cache-Control"} header or {@code null} for none.
   *
   * <p>Overriding is only supported for the purpose of calling the super implementation and
   * changing the return type, but nothing else.
   *
   * @since 1.5
   */
  public HttpHeaders setCacheControl(String cacheControl) {
    this.cacheControl = getAsList(cacheControl);
    return this;
  }

  /**
   * Returns the first {@code "Content-Encoding"} header or {@code null} for none.
   *
   * @since 1.5
   */
  public final String getContentEncoding() {
    return getFirstHeaderValue(contentEncoding);
  }

  /**
   * Sets the {@code "Content-Encoding"} header or {@code null} for none.
   *
   * <p>Overriding is only supported for the purpose of calling the super implementation and
   * changing the return type, but nothing else.
   *
   * @since 1.5
   */
  public HttpHeaders setContentEncoding(String contentEncoding) {
    this.contentEncoding = getAsList(contentEncoding);
    return this;
  }

  /**
   * Returns the first {@code "Content-Length"} header or {@code null} for none.
   *
   * @since 1.5
   */
  public final Long getContentLength() {
    return getFirstHeaderValue(contentLength);
  }

  /**
   * Sets the {@code "Content-Length"} header or {@code null} for none.
   *
   * <p>Overriding is only supported for the purpose of calling the super implementation and
   * changing the return type, but nothing else.
   *
   * @since 1.5
   */
  public HttpHeaders setContentLength(Long contentLength) {
    this.contentLength = getAsList(contentLength);
    return this;
  }

  /**
   * Returns the first {@code "Content-MD5"} header or {@code null} for none.
   *
   * @since 1.5
   */
  public final String getContentMD5() {
    return getFirstHeaderValue(contentMD5);
  }

  /**
   * Sets the {@code "Content-MD5"} header or {@code null} for none.
   *
   * <p>Overriding is only supported for the purpose of calling the super implementation and
   * changing the return type, but nothing else.
   *
   * @since 1.5
   */
  public HttpHeaders setContentMD5(String contentMD5) {
    this.contentMD5 = getAsList(contentMD5);
    return this;
  }

  /**
   * Returns the first {@code "Content-Range"} header or {@code null} for none.
   *
   * @since 1.5
   */
  public final String getContentRange() {
    return getFirstHeaderValue(contentRange);
  }

  /**
   * Sets the {@code "Content-Range"} header or {@code null} for none.
   *
   * <p>Overriding is only supported for the purpose of calling the super implementation and
   * changing the return type, but nothing else.
   *
   * @since 1.5
   */
  public HttpHeaders setContentRange(String contentRange) {
    this.contentRange = getAsList(contentRange);
    return this;
  }

  /**
   * Returns the first {@code "Content-Type"} header or {@code null} for none.
   *
   * @since 1.5
   */
  public final String getContentType() {
    return getFirstHeaderValue(contentType);
  }

  /**
   * Sets the {@code "Content-Type"} header or {@code null} for none.
   *
   * <p>Overriding is only supported for the purpose of calling the super implementation and
   * changing the return type, but nothing else.
   *
   * @since 1.5
   */
  public HttpHeaders setContentType(String contentType) {
    this.contentType = getAsList(contentType);
    return this;
  }

  /**
   * Returns the first {@code "Cookie"} header or {@code null} for none.
   *
   * <p>See <a href='http://tools.ietf.org/html/rfc6265'>Cookie Specification.</a>
   *
   * @since 1.6
   */
  public final String getCookie() {
    return getFirstHeaderValue(cookie);
  }

  /**
   * Sets the {@code "Cookie"} header or {@code null} for none.
   *
   * <p>Overriding is only supported for the purpose of calling the super implementation and
   * changing the return type, but nothing else.
   *
   * @since 1.6
   */
  public HttpHeaders setCookie(String cookie) {
    this.cookie = getAsList(cookie);
    return this;
  }

  /**
   * Returns the first {@code "Date"} header or {@code null} for none.
   *
   * @since 1.5
   */
  public final String getDate() {
    return getFirstHeaderValue(date);
  }

  /**
   * Sets the {@code "Date"} header or {@code null} for none.
   *
   * <p>Overriding is only supported for the purpose of calling the super implementation and
   * changing the return type, but nothing else.
   *
   * @since 1.5
   */
  public HttpHeaders setDate(String date) {
    this.date = getAsList(date);
    return this;
  }

  /**
   * Returns the first {@code "ETag"} header or {@code null} for none.
   *
   * @since 1.5
   */
  public final String getETag() {
    return getFirstHeaderValue(etag);
  }

  /**
   * Sets the {@code "ETag"} header or {@code null} for none.
   *
   * <p>Overriding is only supported for the purpose of calling the super implementation and
   * changing the return type, but nothing else.
   *
   * @since 1.5
   */
  public HttpHeaders setETag(String etag) {
    this.etag = getAsList(etag);
    return this;
  }

  /**
   * Returns the first {@code "Expires"} header or {@code null} for none.
   *
   * @since 1.5
   */
  public final String getExpires() {
    return getFirstHeaderValue(expires);
  }

  /**
   * Sets the {@code "Expires"} header or {@code null} for none.
   *
   * <p>Overriding is only supported for the purpose of calling the super implementation and
   * changing the return type, but nothing else.
   *
   * @since 1.5
   */
  public HttpHeaders setExpires(String expires) {
    this.expires = getAsList(expires);
    return this;
  }

  /**
   * Returns the first {@code "If-Modified-Since"} header or {@code null} for none.
   *
   * @since 1.5
   */
  public final String getIfModifiedSince() {
    return getFirstHeaderValue(ifModifiedSince);
  }

  /**
   * Sets the {@code "If-Modified-Since"} header or {@code null} for none.
   *
   * <p>Overriding is only supported for the purpose of calling the super implementation and
   * changing the return type, but nothing else.
   *
   * @since 1.5
   */
  public HttpHeaders setIfModifiedSince(String ifModifiedSince) {
    this.ifModifiedSince = getAsList(ifModifiedSince);
    return this;
  }

  /**
   * Returns the first {@code "If-Match"} header or {@code null} for none.
   *
   * @since 1.5
   */
  public final String getIfMatch() {
    return getFirstHeaderValue(ifMatch);
  }

  /**
   * Sets the {@code "If-Match"} header or {@code null} for none.
   *
   * <p>Overriding is only supported for the purpose of calling the super implementation and
   * changing the return type, but nothing else.
   *
   * @since 1.5
   */
  public HttpHeaders setIfMatch(String ifMatch) {
    this.ifMatch = getAsList(ifMatch);
    return this;
  }

  /**
   * Returns the first {@code "If-None-Match"} header or {@code null} for none.
   *
   * @since 1.5
   */
  public final String getIfNoneMatch() {
    return getFirstHeaderValue(ifNoneMatch);
  }

  /**
   * Sets the {@code "If-None-Match"} header or {@code null} for none.
   *
   * <p>Overriding is only supported for the purpose of calling the super implementation and
   * changing the return type, but nothing else.
   *
   * @since 1.5
   */
  public HttpHeaders setIfNoneMatch(String ifNoneMatch) {
    this.ifNoneMatch = getAsList(ifNoneMatch);
    return this;
  }

  /**
   * Returns the first {@code "If-Unmodified-Since"} header or {@code null} for none.
   *
   * @since 1.5
   */
  public final String getIfUnmodifiedSince() {
    return getFirstHeaderValue(ifUnmodifiedSince);
  }

  /**
   * Sets the {@code "If-Unmodified-Since"} header or {@code null} for none.
   *
   * <p>Overriding is only supported for the purpose of calling the super implementation and
   * changing the return type, but nothing else.
   *
   * @since 1.5
   */
  public HttpHeaders setIfUnmodifiedSince(String ifUnmodifiedSince) {
    this.ifUnmodifiedSince = getAsList(ifUnmodifiedSince);
    return this;
  }

  /**
   * Returns the first {@code "If-Range"} header or {@code null} for none.
   *
   * @since 1.14
   */
  public final String getIfRange() {
    return getFirstHeaderValue(ifRange);
  }

  /**
   * Sets the {@code "If-Range"} header or {@code null} for none.
   *
   * <p>Overriding is only supported for the purpose of calling the super implementation and
   * changing the return type, but nothing else.
   *
   * @since 1.14
   */
  public HttpHeaders setIfRange(String ifRange) {
    this.ifRange = getAsList(ifRange);
    return this;
  }

  /**
   * Returns the first {@code "Last-Modified"} header or {@code null} for none.
   *
   * @since 1.5
   */
  public final String getLastModified() {
    return getFirstHeaderValue(lastModified);
  }

  /**
   * Sets the {@code "Last-Modified"} header or {@code null} for none.
   *
   * <p>Overriding is only supported for the purpose of calling the super implementation and
   * changing the return type, but nothing else.
   *
   * @since 1.5
   */
  public HttpHeaders setLastModified(String lastModified) {
    this.lastModified = getAsList(lastModified);
    return this;
  }

  /**
   * Returns the first {@code "Location"} header or {@code null} for none.
   *
   * @since 1.5
   */
  public final String getLocation() {
    return getFirstHeaderValue(location);
  }

  /**
   * Sets the {@code "Location"} header or {@code null} for none.
   *
   * <p>Overriding is only supported for the purpose of calling the super implementation and
   * changing the return type, but nothing else.
   *
   * @since 1.5
   */
  public HttpHeaders setLocation(String location) {
    this.location = getAsList(location);
    return this;
  }

  /**
   * Returns the first {@code "MIME-Version"} header or {@code null} for none.
   *
   * @since 1.5
   */
  public final String getMimeVersion() {
    return getFirstHeaderValue(mimeVersion);
  }

  /**
   * Sets the {@code "MIME-Version"} header or {@code null} for none.
   *
   * <p>Overriding is only supported for the purpose of calling the super implementation and
   * changing the return type, but nothing else.
   *
   * @since 1.5
   */
  public HttpHeaders setMimeVersion(String mimeVersion) {
    this.mimeVersion = getAsList(mimeVersion);
    return this;
  }

  /**
   * Returns the first {@code "Range"} header or {@code null} for none.
   *
   * @since 1.5
   */
  public final String getRange() {
    return getFirstHeaderValue(range);
  }

  /**
   * Sets the {@code "Range"} header or {@code null} for none.
   *
   * <p>Overriding is only supported for the purpose of calling the super implementation and
   * changing the return type, but nothing else.
   *
   * @since 1.5
   */
  public HttpHeaders setRange(String range) {
    this.range = getAsList(range);
    return this;
  }

  /**
   * Returns the first {@code "Retry-After"} header or {@code null} for none.
   *
   * @since 1.5
   */
  public final String getRetryAfter() {
    return getFirstHeaderValue(retryAfter);
  }

  /**
   * Sets the {@code "Retry-After"} header or {@code null} for none.
   *
   * <p>Overriding is only supported for the purpose of calling the super implementation and
   * changing the return type, but nothing else.
   *
   * @since 1.5
   */
  public HttpHeaders setRetryAfter(String retryAfter) {
    this.retryAfter = getAsList(retryAfter);
    return this;
  }

  /**
   * Returns the first {@code "User-Agent"} header or {@code null} for none.
   *
   * @since 1.5
   */
  public final String getUserAgent() {
    return getFirstHeaderValue(userAgent);
  }

  /**
   * Sets the {@code "User-Agent"} header or {@code null} for none.
   *
   * <p>Overriding is only supported for the purpose of calling the super implementation and
   * changing the return type, but nothing else.
   *
   * @since 1.5
   */
  public HttpHeaders setUserAgent(String userAgent) {
    this.userAgent = getAsList(userAgent);
    return this;
  }

  /**
   * Returns the first {@code "WWW-Authenticate"} header or {@code null} for none.
   *
   * @since 1.5
   */
  public final String getAuthenticate() {
    return getFirstHeaderValue(authenticate);
  }

  /**
   * Returns all {@code "WWW-Authenticate"} headers or {@code null} for none.
   *
   * @since 1.16
   */
  public final List<String> getAuthenticateAsList() {
    return authenticate;
  }

  /**
   * Sets the {@code "WWW-Authenticate"} header or {@code null} for none.
   *
   * <p>Overriding is only supported for the purpose of calling the super implementation and
   * changing the return type, but nothing else.
   *
   * @since 1.5
   */
  public HttpHeaders setAuthenticate(String authenticate) {
    this.authenticate = getAsList(authenticate);
    return this;
  }

  /**
   * Adds the {@code "Warning"} header or {@code null} for none.
   *
   * <p>Overriding is only supported for the purpose of calling the super implementation and
   * changing the return type, but nothing else.
   *
   * @since 1.28
   */
  public HttpHeaders addWarning(String warning) {
    if (warning == null) {
      return this;
    }
    if (this.warning == null) {
      this.warning = getAsList(warning);
    } else {
      this.warning.add(warning);
    }
    return this;
  }

  /**
   * Returns all {@code "Warning"} headers or {@code null} for none.
   *
   * @since 1.28
   */
  public final List<String> getWarning() {
    return warning == null ? null : new ArrayList<>(warning);
  }

  /**
   * Returns the first {@code "Age"} header or {@code null} for none.
   *
   * @since 1.14
   */
  public final Long getAge() {
    return getFirstHeaderValue(age);
  }

  /**
   * Sets the {@code "Age"} header or {@code null} for none.
   *
   * <p>Overriding is only supported for the purpose of calling the super implementation and
   * changing the return type, but nothing else.
   *
   * @since 1.14
   */
  public HttpHeaders setAge(Long age) {
    this.age = getAsList(age);
    return this;
  }

  /**
   * Sets the {@link #authorization} header as specified in <a
   * href="http://tools.ietf.org/html/rfc2617#section-2">Basic Authentication Scheme</a>.
   *
   * <p>Overriding is only supported for the purpose of calling the super implementation and
   * changing the return type, but nothing else.
   *
   * @since 1.2
   */
  public HttpHeaders setBasicAuthentication(String username, String password) {
    String userPass =
        Preconditions.checkNotNull(username) + ":" + Preconditions.checkNotNull(password);
    String encoded = Base64.encodeBase64String(StringUtils.getBytesUtf8(userPass));
    return setAuthorization("Basic " + encoded);
  }

  private static void addHeader(
      Logger logger,
      StringBuilder logbuf,
      StringBuilder curlbuf,
      LowLevelHttpRequest lowLevelHttpRequest,
      String name,
      Object value,
      Writer writer)
      throws IOException {
    // ignore nulls
    if (value == null || Data.isNull(value)) {
      return;
    }
    // compute value
    String stringValue = toStringValue(value);
    // log header
    String loggedStringValue = stringValue;
    if (("Authorization".equalsIgnoreCase(name) || "Cookie".equalsIgnoreCase(name))
        && (logger == null || !logger.isLoggable(Level.ALL))) {
      loggedStringValue = "<Not Logged>";
    }
    if (logbuf != null) {
      logbuf.append(name).append(": ");
      logbuf.append(loggedStringValue);
      logbuf.append(StringUtils.LINE_SEPARATOR);
    }
    if (curlbuf != null) {
      curlbuf.append(" -H '").append(name).append(": ").append(loggedStringValue).append("'");
    }
    // add header to lowLevelHttpRequest
    if (lowLevelHttpRequest != null) {
      lowLevelHttpRequest.addHeader(name, stringValue);
    }
    // add header to the writer
    if (writer != null) {
      writer.write(name);
      writer.write(": ");
      writer.write(stringValue);
      writer.write("\r\n");
    }
  }

  /** Returns the string header value for the given header value as an object. */
  private static String toStringValue(Object headerValue) {
    return headerValue instanceof Enum<?>
        ? FieldInfo.of((Enum<?>) headerValue).getName()
        : headerValue.toString();
  }

  /**
   * Serializes headers to an {@link LowLevelHttpRequest}.
   *
   * @param headers HTTP headers
   * @param logbuf log buffer or {@code null} for none
   * @param curlbuf log buffer for logging curl requests or {@code null} for none
   * @param logger logger or {@code null} for none. Logger must be specified if log buffer is
   *     specified
   * @param lowLevelHttpRequest low level HTTP request where HTTP headers will be serialized to or
   *     {@code null} for none
   */
  static void serializeHeaders(
      HttpHeaders headers,
      StringBuilder logbuf,
      StringBuilder curlbuf,
      Logger logger,
      LowLevelHttpRequest lowLevelHttpRequest)
      throws IOException {
    serializeHeaders(headers, logbuf, curlbuf, logger, lowLevelHttpRequest, null);
  }

  static void serializeHeaders(
      HttpHeaders headers,
      StringBuilder logbuf,
      StringBuilder curlbuf,
      Logger logger,
      LowLevelHttpRequest lowLevelHttpRequest,
      Writer writer)
      throws IOException {
    HashSet<String> headerNames = new HashSet<String>();
    for (Map.Entry<String, Object> headerEntry : headers.entrySet()) {
      String name = headerEntry.getKey();
      Preconditions.checkArgument(
          headerNames.add(name),
          "multiple headers of the same name (headers are case insensitive): %s",
          name);
      Object value = headerEntry.getValue();
      if (value != null) {
        // compute the display name from the declared field name to fix capitalization
        String displayName = name;
        FieldInfo fieldInfo = headers.getClassInfo().getFieldInfo(name);
        if (fieldInfo != null) {
          displayName = fieldInfo.getName();
        }
        Class<? extends Object> valueClass = value.getClass();
        if (value instanceof Iterable<?> || valueClass.isArray()) {
          for (Object repeatedValue : Types.iterableOf(value)) {
            addHeader(
                logger, logbuf, curlbuf, lowLevelHttpRequest, displayName, repeatedValue, writer);
          }
        } else {
          addHeader(logger, logbuf, curlbuf, lowLevelHttpRequest, displayName, value, writer);
        }
      }
    }
    if (writer != null) {
      writer.flush();
    }
  }

  /**
   * Serializes headers to an {@link Writer} for Multi-part requests.
   *
   * @param headers HTTP headers
   * @param logbuf log buffer or {@code null} for none
   * @param logger logger or {@code null} for none. Logger must be specified if log buffer is
   *     specified
   * @param writer Writer where HTTP headers will be serialized to or {@code null} for none
   * @since 1.9
   */
  public static void serializeHeadersForMultipartRequests(
      HttpHeaders headers, StringBuilder logbuf, Logger logger, Writer writer) throws IOException {
    serializeHeaders(headers, logbuf, null, logger, null, writer);
  }

  /**
   * Puts all headers of the {@link LowLevelHttpResponse} into this {@link HttpHeaders} object.
   *
   * @param response Response from which the headers are copied
   * @param logger {@link StringBuilder} to which logging output is added or {@code null} to disable
   *     logging
   * @since 1.10
   */
  public final void fromHttpResponse(LowLevelHttpResponse response, StringBuilder logger)
      throws IOException {
    clear();
    ParseHeaderState state = new ParseHeaderState(this, logger);
    int headerCount = response.getHeaderCount();
    for (int i = 0; i < headerCount; i++) {
      parseHeader(response.getHeaderName(i), response.getHeaderValue(i), state);
    }
    state.finish();
  }

  /** LowLevelHttpRequest which will call the .parseHeader() method for every header added. */
  private static class HeaderParsingFakeLevelHttpRequest extends LowLevelHttpRequest {
    private final HttpHeaders target;
    private final ParseHeaderState state;

    HeaderParsingFakeLevelHttpRequest(HttpHeaders target, ParseHeaderState state) {
      this.target = target;
      this.state = state;
    }

    @Override
    public void addHeader(String name, String value) {
      target.parseHeader(name, value, state);
    }

    @Override
    public LowLevelHttpResponse execute() throws IOException {
      throw new UnsupportedOperationException();
    }
  }

  /** Returns the first header value based on the given internal list value. */
  private <T> T getFirstHeaderValue(List<T> internalValue) {
    return internalValue == null ? null : internalValue.get(0);
  }

  /** Returns the list value to use for the given parameter passed to the setter method. */
  private <T> List<T> getAsList(T passedValue) {
    if (passedValue == null) {
      return null;
    }
    List<T> result = new ArrayList<T>();
    result.add(passedValue);
    return result;
  }

  /**
   * Returns the first header string value for the given header name.
   *
   * @param name header name (may be any case)
   * @return first header string value or {@code null} if not found
   * @since 1.13
   */
  public String getFirstHeaderStringValue(String name) {
    Object value = get(name.toLowerCase(Locale.US));
    if (value == null) {
      return null;
    }
    Class<? extends Object> valueClass = value.getClass();
    if (value instanceof Iterable<?> || valueClass.isArray()) {
      for (Object repeatedValue : Types.iterableOf(value)) {
        return toStringValue(repeatedValue);
      }
    }
    return toStringValue(value);
  }

  /**
   * Returns an unmodifiable list of the header string values for the given header name.
   *
   * @param name header name (may be any case)
   * @return header string values or empty if not found
   * @since 1.13
   */
  public List<String> getHeaderStringValues(String name) {
    Object value = get(name.toLowerCase(Locale.US));
    if (value == null) {
      return Collections.emptyList();
    }
    Class<? extends Object> valueClass = value.getClass();
    if (value instanceof Iterable<?> || valueClass.isArray()) {
      List<String> values = new ArrayList<String>();
      for (Object repeatedValue : Types.iterableOf(value)) {
        values.add(toStringValue(repeatedValue));
      }
      return Collections.unmodifiableList(values);
    }
    return Collections.singletonList(toStringValue(value));
  }

  /**
   * Puts all headers of the {@link HttpHeaders} object into this {@link HttpHeaders} object.
   *
   * @param headers {@link HttpHeaders} from where the headers are taken
   * @since 1.10
   */
  public final void fromHttpHeaders(HttpHeaders headers) {
    try {
      ParseHeaderState state = new ParseHeaderState(this, null);
      serializeHeaders(
          headers, null, null, null, new HeaderParsingFakeLevelHttpRequest(this, state));
      state.finish();
    } catch (IOException ex) {
      // Should never occur as we are dealing with a FakeLowLevelHttpRequest
      throw Throwables.propagate(ex);
    }
  }

  /** State container for {@link #parseHeader(String, String, ParseHeaderState)}. */
  private static final class ParseHeaderState {
    /** Target map where parsed values are stored. */
    final ArrayValueMap arrayValueMap;

    /** Logger if logging is enabled or {@code null} otherwise. */
    final StringBuilder logger;

    /** ClassInfo of the HttpHeaders. */
    final ClassInfo classInfo;

    /** List of types in the header context. */
    final List<Type> context;

    /**
     * Initializes a new ParseHeaderState.
     *
     * @param headers HttpHeaders object for which the headers are being parsed
     * @param logger Logger if logging is enabled or {@code null}
     */
    public ParseHeaderState(HttpHeaders headers, StringBuilder logger) {
      Class<? extends HttpHeaders> clazz = headers.getClass();
      this.context = Arrays.<Type>asList(clazz);
      this.classInfo = ClassInfo.of(clazz, true);
      this.logger = logger;
      this.arrayValueMap = new ArrayValueMap(headers);
    }

    /** Finishes the parsing-process by setting all array-values. */
    void finish() {
      arrayValueMap.setValues();
    }
  }

  /** Parses the specified case-insensitive header pair into this HttpHeaders instance. */
  void parseHeader(String headerName, String headerValue, ParseHeaderState state) {
    List<Type> context = state.context;
    ClassInfo classInfo = state.classInfo;
    ArrayValueMap arrayValueMap = state.arrayValueMap;
    StringBuilder logger = state.logger;

    if (logger != null) {
      logger.append(headerName + ": " + headerValue).append(StringUtils.LINE_SEPARATOR);
    }
    // use field information if available
    FieldInfo fieldInfo = classInfo.getFieldInfo(headerName);
    if (fieldInfo != null) {
      Type type = Data.resolveWildcardTypeOrTypeVariable(context, fieldInfo.getGenericType());
      // type is now class, parameterized type, or generic array type
      if (Types.isArray(type)) {
        // array that can handle repeating values
        Class<?> rawArrayComponentType =
            Types.getRawArrayComponentType(context, Types.getArrayComponentType(type));
        arrayValueMap.put(
            fieldInfo.getField(),
            rawArrayComponentType,
            parseValue(rawArrayComponentType, context, headerValue));
      } else if (Types.isAssignableToOrFrom(
          Types.getRawArrayComponentType(context, type), Iterable.class)) {
        // iterable that can handle repeating values
        @SuppressWarnings("unchecked")
        Collection<Object> collection = (Collection<Object>) fieldInfo.getValue(this);
        if (collection == null) {
          collection = Data.newCollectionInstance(type);
          fieldInfo.setValue(this, collection);
        }
        Type subFieldType = type == Object.class ? null : Types.getIterableParameter(type);
        collection.add(parseValue(subFieldType, context, headerValue));
      } else {
        // parse value based on field type
        fieldInfo.setValue(this, parseValue(type, context, headerValue));
      }
    } else {
      // store header values in an array list
      @SuppressWarnings("unchecked")
      ArrayList<String> listValue = (ArrayList<String>) this.get(headerName);
      if (listValue == null) {
        listValue = new ArrayList<String>();
        this.set(headerName, listValue);
      }
      listValue.add(headerValue);
    }
  }

  private static Object parseValue(Type valueType, List<Type> context, String value) {
    Type resolved = Data.resolveWildcardTypeOrTypeVariable(context, valueType);
    return Data.parsePrimitiveValue(resolved, value);
  }

  // TODO(yanivi): override equals and hashCode
}
