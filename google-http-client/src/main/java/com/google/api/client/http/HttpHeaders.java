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
import com.google.api.client.util.StringUtils;
import com.google.api.client.util.Types;
import com.google.common.base.Preconditions;

import java.io.IOException;
import java.io.Writer;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

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

  public HttpHeaders() {
    super(EnumSet.of(Flags.IGNORE_CASE));
  }

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
  private Long contentLength;

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
   * <p>
   * Upgrade warning: in prior version content length was represented as a String, but now it is
   * represented as a Long.
   * </p>
   *
   * @since 1.5
   */
  public final Long getContentLength() {
    return contentLength;
  }

  /**
   * Sets the {@code "Content-Length"} header or {@code null} for none.
   *
   * <p>
   * Upgrade warning: in prior version content length was represented as a String, but now it is
   * represented as a Long.
   * </p>
   *
   * @since 1.5
   */
  public final void setContentLength(Long contentLength) {
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
    String userPass =
        Preconditions.checkNotNull(username) + ":" + Preconditions.checkNotNull(password);
    String encoded = Base64.encodeBase64String(StringUtils.getBytesUtf8(userPass));
    authorization = "Basic " + encoded;
  }

  private static void addHeader(Logger logger,
      StringBuilder logbuf,
      LowLevelHttpRequest lowLevelHttpRequest,
      String name,
      Object value,
      Writer writer) throws IOException {
    // ignore nulls
    if (value == null || Data.isNull(value)) {
      return;
    }
    // compute value
    String stringValue =
        value instanceof Enum<?> ? FieldInfo.of((Enum<?>) value).getName() : value.toString();
    // log header
    if (logbuf != null) {
      logbuf.append(name).append(": ");
      if ("Authorization".equalsIgnoreCase(name) && !logger.isLoggable(Level.ALL)) {
        logbuf.append("<Not Logged>");
      } else {
        logbuf.append(stringValue);
      }
      logbuf.append(StringUtils.LINE_SEPARATOR);
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

  /**
   * Serializes headers to an @{link LowLevelHttpRequest}.
   *
   * @param headers HTTP headers
   * @param logbuf log buffer or {@code null} for none
   * @param logger logger or {@code null} for none. Logger must be specified if log buffer is
   *        specified
   * @param lowLevelHttpRequest low level HTTP request where HTTP headers will be serialized to or
   *        {@code null} for none
   *
   * @since 1.9
   */
  public static void serializeHeaders(HttpHeaders headers, StringBuilder logbuf, Logger logger,
      LowLevelHttpRequest lowLevelHttpRequest) throws IOException {
    serializeHeaders(headers, logbuf, logger, lowLevelHttpRequest, null);
  }

  /**
   * Serializes headers to an {@link Writer} for Multi-part requests.
   *
   * @param headers HTTP headers
   * @param logbuf log buffer or {@code null} for none
   * @param logger logger or {@code null} for none. Logger must be specified if log buffer is
   *        specified
   * @param writer Writer where HTTP headers will be serialized to or {@code null} for none
   *
   * @since 1.9
   */
  public static void serializeHeadersForMultipartRequests(
      HttpHeaders headers, StringBuilder logbuf, Logger logger, Writer writer) throws IOException {
    serializeHeaders(headers, logbuf, logger, null, writer);
  }

  private static void serializeHeaders(HttpHeaders headers, StringBuilder logbuf, Logger logger,
      LowLevelHttpRequest lowLevelHttpRequest, Writer writer) throws IOException {
    HashSet<String> headerNames = new HashSet<String>();
    for (Map.Entry<String, Object> headerEntry : headers.entrySet()) {
      String name = headerEntry.getKey();
      Preconditions.checkArgument(headerNames.add(name),
          "multiple headers of the same name (headers are case insensitive): %s", name);
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
            addHeader(logger, logbuf, lowLevelHttpRequest, displayName, repeatedValue, writer);
          }
        } else {
          addHeader(logger, logbuf, lowLevelHttpRequest, displayName, value, writer);
        }
      }
    }
    if (writer != null) {
      writer.flush();
    }
  }

  /**
   * Puts all headers of the {@link LowLevelHttpResponse} into this {@link HttpHeaders} object.
   *
   * @param response Response from which the headers are copied
   * @param logger {@link StringBuilder} to which logging output is added or {@code null} to disable
   *        logging
   * @since 1.10
   */
  public final void fromHttpResponse(LowLevelHttpResponse response, StringBuilder logger) {
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
    public void setContent(HttpContent content) throws IOException {
      throw new UnsupportedOperationException();
    }

    @Override
    public LowLevelHttpResponse execute() throws IOException {
      throw new UnsupportedOperationException();
    }
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
      serializeHeaders(headers, null, null, new HeaderParsingFakeLevelHttpRequest(this, state));
      state.finish();
    } catch (IOException ex) {
      // Should never occur as we are dealing with a FakeLowLevelHttpRequest
      throw new IllegalStateException(ex);
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

    /**
     * Finishes the parsing-process by setting all array-values.
     */
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
        Class<?> rawArrayComponentType = Types.getRawArrayComponentType(
            context, Types.getArrayComponentType(type));
        arrayValueMap.put(fieldInfo.getField(), rawArrayComponentType,
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
