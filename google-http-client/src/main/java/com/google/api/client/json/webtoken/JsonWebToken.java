/*
 * Copyright 2012 Google LLC
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

package com.google.api.client.json.webtoken;

import com.google.api.client.json.GenericJson;
import com.google.api.client.util.Key;
import com.google.api.client.util.Objects;
import com.google.api.client.util.Preconditions;
import java.util.Collections;
import java.util.List;

/**
 * <a href="https://tools.ietf.org/html/rfc7519">JSON Web Token (JWT)</a>.
 *
 * <p>Implementation is not thread-safe.
 *
 * @since 1.14 (since 1.7 as com.google.api.client.auth.jsontoken.JsonWebToken)
 * @author Yaniv Inbar
 */
public class JsonWebToken {

  /** Header. */
  private final Header header;

  /** Payload. */
  private final Payload payload;

  /**
   * @param header header
   * @param payload payload
   */
  public JsonWebToken(Header header, Payload payload) {
    this.header = Preconditions.checkNotNull(header);
    this.payload = Preconditions.checkNotNull(payload);
  }

  /**
   * Header as specified in 
   * <a href="https://tools.ietf.org/html/rfc7519#section-5">JWT Header</a>.
   */
  public static class Header extends GenericJson {

    /** Type header parameter used to declare the type of this object or {@code null} for none. */
    @Key("typ")
    private String type;

    /**
     * Content type header parameter used to declare structural information about the JWT or {@code
     * null} for none.
     */
    @Key("cty")
    private String contentType;

    /**
     * Returns the type header parameter used to declare the type of this object or {@code null} for
     * none.
     */
    public final String getType() {
      return type;
    }

    /**
     * Sets the type header parameter used to declare the type of this object or {@code null} for
     * none.
     *
     * <p>Overriding is only supported for the purpose of calling the super implementation and
     * changing the return type, but nothing else.
     */
    public Header setType(String type) {
      this.type = type;
      return this;
    }

    /**
     * Returns the content type header parameter used to declare structural information about the
     * JWT or {@code null} for none.
     */
    public final String getContentType() {
      return contentType;
    }

    /**
     * Sets the content type header parameter used to declare structural information about the JWT
     * or {@code null} for none.
     *
     * <p>Overriding is only supported for the purpose of calling the super implementation and
     * changing the return type, but nothing else.
     */
    public Header setContentType(String contentType) {
      this.contentType = contentType;
      return this;
    }

    @Override
    public Header set(String fieldName, Object value) {
      return (Header) super.set(fieldName, value);
    }

    @Override
    public Header clone() {
      return (Header) super.clone();
    }
  }

  /**
   * Payload as specified in 
   * <a href="https://tools.ietf.org/html/rfc7519#section-4.1">Reserved Claim
   * Names</a>.
   */
  public static class Payload extends GenericJson {

    /**
     * Expiration time claim that identifies the expiration time (in seconds) on or after which the
     * token MUST NOT be accepted for processing or {@code null} for none.
     */
    @Key("exp")
    private Long expirationTimeSeconds;

    /**
     * Not before claim that identifies the time (in seconds) before which the token MUST NOT be
     * accepted for processing or {@code null} for none.
     */
    @Key("nbf")
    private Long notBeforeTimeSeconds;

    /**
     * Issued at claim that identifies the time (in seconds) at which the JWT was issued or {@code
     * null} for none.
     */
    @Key("iat")
    private Long issuedAtTimeSeconds;

    /** Issuer claim that identifies the principal that issued the JWT or {@code null} for none. */
    @Key("iss")
    private String issuer;

    /**
     * Audience claim that identifies the audience that the JWT is intended for (should either be a
     * {@code String} or a {@code List<String>}) or {@code null} for none.
     */
    @Key("aud")
    private Object audience;

    /** JWT ID claim that provides a unique identifier for the JWT or {@code null} for none. */
    @Key("jti")
    private String jwtId;

    /**
     * Type claim that is used to declare a type for the contents of this JWT Claims Set or {@code
     * null} for none.
     */
    @Key("typ")
    private String type;

    /**
     * Subject claim identifying the principal that is the subject of the JWT or {@code null} for
     * none.
     */
    @Key("sub")
    private String subject;

    /**
     * Returns the expiration time (in seconds) claim that identifies the expiration time on or
     * after which the token MUST NOT be accepted for processing or {@code null} for none.
     */
    public final Long getExpirationTimeSeconds() {
      return expirationTimeSeconds;
    }

    /**
     * Sets the expiration time claim that identifies the expiration time (in seconds) on or after
     * which the token MUST NOT be accepted for processing or {@code null} for none.
     *
     * <p>Overriding is only supported for the purpose of calling the super implementation and
     * changing the return type, but nothing else.
     */
    public Payload setExpirationTimeSeconds(Long expirationTimeSeconds) {
      this.expirationTimeSeconds = expirationTimeSeconds;
      return this;
    }

    /**
     * Returns the not before claim that identifies the time (in seconds) before which the token
     * MUST NOT be accepted for processing or {@code null} for none.
     */
    public final Long getNotBeforeTimeSeconds() {
      return notBeforeTimeSeconds;
    }

    /**
     * Sets the not before claim that identifies the time (in seconds) before which the token MUST
     * NOT be accepted for processing or {@code null} for none.
     *
     * <p>Overriding is only supported for the purpose of calling the super implementation and
     * changing the return type, but nothing else.
     */
    public Payload setNotBeforeTimeSeconds(Long notBeforeTimeSeconds) {
      this.notBeforeTimeSeconds = notBeforeTimeSeconds;
      return this;
    }

    /**
     * Returns the issued at claim that identifies the time (in seconds) at which the JWT was issued
     * or {@code null} for none.
     */
    public final Long getIssuedAtTimeSeconds() {
      return issuedAtTimeSeconds;
    }

    /**
     * Sets the issued at claim that identifies the time (in seconds) at which the JWT was issued or
     * {@code null} for none.
     *
     * <p>Overriding is only supported for the purpose of calling the super implementation and
     * changing the return type, but nothing else.
     */
    public Payload setIssuedAtTimeSeconds(Long issuedAtTimeSeconds) {
      this.issuedAtTimeSeconds = issuedAtTimeSeconds;
      return this;
    }

    /**
     * Returns the issuer claim that identifies the principal that issued the JWT or {@code null}
     * for none.
     */
    public final String getIssuer() {
      return issuer;
    }

    /**
     * Sets the issuer claim that identifies the principal that issued the JWT or {@code null} for
     * none.
     *
     * <p>Overriding is only supported for the purpose of calling the super implementation and
     * changing the return type, but nothing else.
     */
    public Payload setIssuer(String issuer) {
      this.issuer = issuer;
      return this;
    }

    /**
     * Returns the audience claim that identifies the audience that the JWT is intended for (should
     * either be a {@code String} or a {@code List<String>}) or {@code null} for none.
     */
    public final Object getAudience() {
      return audience;
    }

    /**
     * Returns the list of audience claim that identifies the audience that the JWT is intended for
     * or empty for none.
     */
    @SuppressWarnings("unchecked")
    public final List<String> getAudienceAsList() {
      if (audience == null) {
        return Collections.emptyList();
      }
      if (audience instanceof String) {
        return Collections.singletonList((String) audience);
      }
      return (List<String>) audience;
    }

    /**
     * Sets the audience claim that identifies the audience that the JWT is intended for (should
     * either be a {@code String} or a {@code List<String>}) or {@code null} for none.
     *
     * <p>Overriding is only supported for the purpose of calling the super implementation and
     * changing the return type, but nothing else.
     */
    public Payload setAudience(Object audience) {
      this.audience = audience;
      return this;
    }

    /**
     * Returns the JWT ID claim that provides a unique identifier for the JWT or {@code null} for
     * none.
     */
    public final String getJwtId() {
      return jwtId;
    }

    /**
     * Sets the JWT ID claim that provides a unique identifier for the JWT or {@code null} for none.
     *
     * <p>Overriding is only supported for the purpose of calling the super implementation and
     * changing the return type, but nothing else.
     */
    public Payload setJwtId(String jwtId) {
      this.jwtId = jwtId;
      return this;
    }

    /**
     * Returns the type claim that is used to declare a type for the contents of this JWT Claims Set
     * or {@code null} for none.
     */
    public final String getType() {
      return type;
    }

    /**
     * Sets the type claim that is used to declare a type for the contents of this JWT Claims Set or
     * {@code null} for none.
     *
     * <p>Overriding is only supported for the purpose of calling the super implementation and
     * changing the return type, but nothing else.
     */
    public Payload setType(String type) {
      this.type = type;
      return this;
    }

    /**
     * Returns the subject claim identifying the principal that is the subject of the JWT or {@code
     * null} for none.
     */
    public final String getSubject() {
      return subject;
    }

    /**
     * Sets the subject claim identifying the principal that is the subject of the JWT or {@code
     * null} for none.
     *
     * <p>Overriding is only supported for the purpose of calling the super implementation and
     * changing the return type, but nothing else.
     */
    public Payload setSubject(String subject) {
      this.subject = subject;
      return this;
    }

    @Override
    public Payload set(String fieldName, Object value) {
      return (Payload) super.set(fieldName, value);
    }

    @Override
    public Payload clone() {
      return (Payload) super.clone();
    }
  }

  @Override
  public String toString() {
    return Objects.toStringHelper(this).add("header", header).add("payload", payload).toString();
  }

  /**
   * Returns the header.
   *
   * <p>Overriding is only supported for the purpose of calling the super implementation and
   * changing the return type, but nothing else.
   */
  public Header getHeader() {
    return header;
  }

  /**
   * Returns the payload.
   *
   * <p>Overriding is only supported for the purpose of calling the super implementation and
   * changing the return type, but nothing else.
   */
  public Payload getPayload() {
    return payload;
  }
}
