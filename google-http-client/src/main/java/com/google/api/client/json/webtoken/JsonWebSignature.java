/*
 * Copyright 2012 Google LLC.
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

import com.google.api.client.json.JsonFactory;
import com.google.api.client.util.Base64;
import com.google.api.client.util.Beta;
import com.google.api.client.util.Key;
import com.google.api.client.util.Preconditions;
import com.google.api.client.util.SecurityUtils;
import com.google.api.client.util.StringUtils;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

/**
 * <a href="https://tools.ietf.org/html/rfc7515">JSON Web Signature(JWS)</a>.
 *
 * <p>Sample usage:
 *
 * <pre>
 * public static void printPayload(JsonFactory jsonFactory, String tokenString) throws IOException {
 *   JsonWebSignature jws = JsonWebSignature.parse(jsonFactory, tokenString);
 *   System.out.println(jws.getPayload());
 * }
 * </pre>
 *
 * <p>Implementation is not thread-safe.
 *
 * @since 1.14 (since 1.7 as com.google.api.client.auth.jsontoken.JsonWebSignature)
 * @author Yaniv Inbar
 */
public class JsonWebSignature extends JsonWebToken {

  /** Bytes of the signature. */
  private final byte[] signatureBytes;

  /** Bytes of the signature content. */
  private final byte[] signedContentBytes;

  /**
   * @param header header
   * @param payload payload
   * @param signatureBytes bytes of the signature
   * @param signedContentBytes bytes of the signed content
   */
  public JsonWebSignature(
      Header header, Payload payload, byte[] signatureBytes, byte[] signedContentBytes) {
    super(header, payload);
    this.signatureBytes = Preconditions.checkNotNull(signatureBytes);
    this.signedContentBytes = Preconditions.checkNotNull(signedContentBytes);
  }

  /**
   * Header as specified in 
   * <a href="https://tools.ietf.org/html/rfc7515#section-4.1">Reserved
   * Header Parameter Names</a>.
   */
  public static class Header extends JsonWebToken.Header {

    /**
     * Algorithm header parameter that identifies the cryptographic algorithm used to secure the JWS
     * or {@code null} for none.
     */
    @Key("alg")
    private String algorithm;

    /**
     * JSON Web Key URL header parameter that is an absolute URL that refers to a resource for a set
     * of JSON-encoded public keys, one of which corresponds to the key that was used to digitally
     * sign the JWS or {@code null} for none.
     */
    @Key("jku")
    private String jwkUrl;

    /**
     * JSON Web Key header parameter that is a public key that corresponds to the key used to
     * digitally sign the JWS or {@code null} for none.
     */
    @Key("jwk")
    private String jwk;

    /**
     * Key ID header parameter that is a hint indicating which specific key owned by the signer
     * should be used to validate the digital signature or {@code null} for none.
     */
    @Key("kid")
    private String keyId;

    /**
     * X.509 URL header parameter that is an absolute URL that refers to a resource for the X.509
     * public key certificate or certificate chain corresponding to the key used to digitally sign
     * the JWS or {@code null} for none.
     */
    @Key("x5u")
    private String x509Url;

    /**
     * X.509 certificate thumbprint header parameter that provides a base64url encoded SHA-1
     * thumbprint (a.k.a. digest) of the DER encoding of an X.509 certificate that can be used to
     * match the certificate or {@code null} for none.
     */
    @Key("x5t")
    private String x509Thumbprint;

    /**
     * X.509 certificate chain header parameter contains the X.509 public key certificate or
     * certificate chain corresponding to the key used to digitally sign the JWS or {@code null} for
     * none.
     *
     * @since 1.19.1.
     */
    @Key("x5c")
    private ArrayList<String> x509Certificates;

    /**
     * Array listing the header parameter names that define extensions that are used in the JWS
     * header that MUST be understood and processed or {@code null} for none.
     */
    @Key("crit")
    private List<String> critical;

    @Override
    public Header setType(String type) {
      super.setType(type);
      return this;
    }

    /**
     * Returns the algorithm header parameter that identifies the cryptographic algorithm used to
     * secure the JWS or {@code null} for none.
     */
    public final String getAlgorithm() {
      return algorithm;
    }

    /**
     * Sets the algorithm header parameter that identifies the cryptographic algorithm used to
     * secure the JWS or {@code null} for none.
     *
     * <p>Overriding is only supported for the purpose of calling the super implementation and
     * changing the return type, but nothing else.
     */
    public Header setAlgorithm(String algorithm) {
      this.algorithm = algorithm;
      return this;
    }

    /**
     * Returns the JSON Web Key URL header parameter that is an absolute URL that refers to a
     * resource for a set of JSON-encoded public keys, one of which corresponds to the key that was
     * used to digitally sign the JWS or {@code null} for none.
     */
    public final String getJwkUrl() {
      return jwkUrl;
    }

    /**
     * Sets the JSON Web Key URL header parameter that is an absolute URL that refers to a resource
     * for a set of JSON-encoded public keys, one of which corresponds to the key that was used to
     * digitally sign the JWS or {@code null} for none.
     *
     * <p>Overriding is only supported for the purpose of calling the super implementation and
     * changing the return type, but nothing else.
     */
    public Header setJwkUrl(String jwkUrl) {
      this.jwkUrl = jwkUrl;
      return this;
    }

    /**
     * Returns the JSON Web Key header parameter that is a public key that corresponds to the key
     * used to digitally sign the JWS or {@code null} for none.
     */
    public final String getJwk() {
      return jwk;
    }

    /**
     * Sets the JSON Web Key header parameter that is a public key that corresponds to the key used
     * to digitally sign the JWS or {@code null} for none.
     *
     * <p>Overriding is only supported for the purpose of calling the super implementation and
     * changing the return type, but nothing else.
     */
    public Header setJwk(String jwk) {
      this.jwk = jwk;
      return this;
    }

    /**
     * Returns the key ID header parameter that is a hint indicating which specific key owned by the
     * signer should be used to validate the digital signature or {@code null} for none.
     */
    public final String getKeyId() {
      return keyId;
    }

    /**
     * Sets the key ID header parameter that is a hint indicating which specific key owned by the
     * signer should be used to validate the digital signature or {@code null} for none.
     *
     * <p>Overriding is only supported for the purpose of calling the super implementation and
     * changing the return type, but nothing else.
     */
    public Header setKeyId(String keyId) {
      this.keyId = keyId;
      return this;
    }

    /**
     * Returns the X.509 URL header parameter that is an absolute URL that refers to a resource for
     * the X.509 public key certificate or certificate chain corresponding to the key used to
     * digitally sign the JWS or {@code null} for none.
     */
    public final String getX509Url() {
      return x509Url;
    }

    /**
     * Sets the X.509 URL header parameter that is an absolute URL that refers to a resource for the
     * X.509 public key certificate or certificate chain corresponding to the key used to digitally
     * sign the JWS or {@code null} for none.
     *
     * <p>Overriding is only supported for the purpose of calling the super implementation and
     * changing the return type, but nothing else.
     */
    public Header setX509Url(String x509Url) {
      this.x509Url = x509Url;
      return this;
    }

    /**
     * Returns the X.509 certificate thumbprint header parameter that provides a base64url encoded
     * SHA-1 thumbprint (a.k.a. digest) of the DER encoding of an X.509 certificate that can be used
     * to match the certificate or {@code null} for none.
     */
    public final String getX509Thumbprint() {
      return x509Thumbprint;
    }

    /**
     * Sets the X.509 certificate thumbprint header parameter that provides a base64url encoded
     * SHA-1 thumbprint (a.k.a. digest) of the DER encoding of an X.509 certificate that can be used
     * to match the certificate or {@code null} for none.
     *
     * <p>Overriding is only supported for the purpose of calling the super implementation and
     * changing the return type, but nothing else.
     */
    public Header setX509Thumbprint(String x509Thumbprint) {
      this.x509Thumbprint = x509Thumbprint;
      return this;
    }

    /**
     * Returns the X.509 certificate chain header parameter contains the X.509 public key
     * certificate or certificate chain corresponding to the key used to digitally sign the JWS or
     * {@code null} for none.
     *
     * @since 1.19.1.
     */
    public final List<String> getX509Certificates() {
      return new ArrayList<>(x509Certificates);
    }

    /**
     * Sets the X.509 certificate chain header parameter contains the X.509 public key certificate
     * or certificate chain corresponding to the key used to digitally sign the JWS or {@code null}
     * for none.
     *
     * <p>Overriding is only supported for the purpose of calling the super implementation and
     * changing the return type, but nothing else.
     *
     * @since 1.19.1.
     */
    public Header setX509Certificates(List<String> x509Certificates) {
      this.x509Certificates = new ArrayList<>(x509Certificates);
      return this;
    }

    /**
     * Returns an array listing the header parameter names that define extensions used in
     * the JWS header that MUST be understood and processed or {@code null} for none.
     *
     * @since 1.16
     */
    public final List<String> getCritical() {
      if (critical == null || critical.isEmpty()) {
        return null;
      }
      return new ArrayList<>(critical);
    }

    /**
     * Sets the header parameter names that define extensions used in the
     * JWS header that MUST be understood and processed or {@code null} for none.
     *
     * <p>Overriding is only supported for the purpose of calling the super implementation and
     * changing the return type, but nothing else.
     *
     * @since 1.16
     */
    public Header setCritical(List<String> critical) {
      this.critical = new ArrayList<>(critical);
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

  @Override
  public Header getHeader() {
    return (Header) super.getHeader();
  }

  /**
   * Verifies the signature of the content.
   *
   * <p>Currently only {@code "RS256"} algorithm is verified, but others may be added in the future.
   * For any other algorithm it returns {@code false}.
   *
   * @param publicKey public key
   * @return whether the algorithm is recognized and it is verified
   * @throws GeneralSecurityException
   */
  public final boolean verifySignature(PublicKey publicKey) throws GeneralSecurityException {
    Signature signatureAlg = null;
    String algorithm = getHeader().getAlgorithm();
    if ("RS256".equals(algorithm)) {
      signatureAlg = SecurityUtils.getSha256WithRsaSignatureAlgorithm();
    } else {
      return false;
    }
    return SecurityUtils.verify(signatureAlg, publicKey, signatureBytes, signedContentBytes);
  }

  /**
   * {@link Beta} <br>
   * Verifies the signature of the content using the certificate chain embedded in the signature.
   *
   * <p>Currently only {@code "RS256"} algorithm is verified, but others may be added in the future.
   * For any other algorithm it returns {@code null}.
   *
   * <p>The leaf certificate of the certificate chain must be an SSL server certificate.
   *
   * @param trustManager trust manager used to verify the X509 certificate chain embedded in this
   *     message
   * @return the signature certificate if the signature could be verified, null otherwise
   * @throws GeneralSecurityException
   * @since 1.19.1
   */
  @Beta
  public final X509Certificate verifySignature(X509TrustManager trustManager)
      throws GeneralSecurityException {
    List<String> x509Certificates = getHeader().getX509Certificates();
    if (x509Certificates == null || x509Certificates.isEmpty()) {
      return null;
    }
    String algorithm = getHeader().getAlgorithm();
    Signature signatureAlg = null;
    if ("RS256".equals(algorithm)) {
      signatureAlg = SecurityUtils.getSha256WithRsaSignatureAlgorithm();
    } else {
      return null;
    }
    return SecurityUtils.verify(
        signatureAlg, trustManager, x509Certificates, signatureBytes, signedContentBytes);
  }

  /**
   * {@link Beta} <br>
   * Verifies the signature of the content using the certificate chain embedded in the signature.
   *
   * <p>Currently only {@code "RS256"} algorithm is verified, but others may be added in the future.
   * For any other algorithm it returns {@code null}.
   *
   * <p>The certificate chain is verified using the system default trust manager.
   *
   * <p>The leaf certificate of the certificate chain must be an SSL server certificate.
   *
   * @return the signature certificate if the signature could be verified, null otherwise
   * @throws GeneralSecurityException
   * @since 1.19.1.
   */
  @Beta
  public final X509Certificate verifySignature() throws GeneralSecurityException {
    X509TrustManager trustManager = getDefaultX509TrustManager();
    if (trustManager == null) {
      return null;
    }
    return verifySignature(trustManager);
  }

  private static X509TrustManager getDefaultX509TrustManager() {
    try {
      TrustManagerFactory factory =
          TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
      factory.init((KeyStore) null);
      for (TrustManager manager : factory.getTrustManagers()) {
        if (manager instanceof X509TrustManager) {
          return (X509TrustManager) manager;
        }
      }
      return null;
    } catch (NoSuchAlgorithmException e) {
      return null;
    } catch (KeyStoreException e) {
      return null;
    }
  }

  /** Returns the bytes of the signature. */
  public final byte[] getSignatureBytes() {
    return Arrays.copyOf(signatureBytes, signatureBytes.length);
  }

  /** Returns the bytes of the signature content. */
  public final byte[] getSignedContentBytes() {
    return Arrays.copyOf(signedContentBytes, signedContentBytes.length);
  }

  /**
   * Parses the given JWS token string and returns the parsed {@link JsonWebSignature}.
   *
   * @param jsonFactory JSON factory
   * @param tokenString JWS token string
   * @return parsed JWS
   */
  public static JsonWebSignature parse(JsonFactory jsonFactory, String tokenString)
      throws IOException {
    return parser(jsonFactory).parse(tokenString);
  }

  /** Returns a new instance of a JWS parser. */
  public static Parser parser(JsonFactory jsonFactory) {
    return new Parser(jsonFactory);
  }

  /**
   * JWS parser.
   *
   * <p>Implementation is not thread-safe.
   */
  public static final class Parser {

    /** JSON factory. */
    private final JsonFactory jsonFactory;

    /** Header class to use for parsing. */
    private Class<? extends Header> headerClass = Header.class;

    /** Payload class to use for parsing. */
    private Class<? extends Payload> payloadClass = Payload.class;

    /** @param jsonFactory JSON factory */
    public Parser(JsonFactory jsonFactory) {
      this.jsonFactory = Preconditions.checkNotNull(jsonFactory);
    }

    /** Returns the header class to use for parsing. */
    public Class<? extends Header> getHeaderClass() {
      return headerClass;
    }

    /** Sets the header class to use for parsing. */
    public Parser setHeaderClass(Class<? extends Header> headerClass) {
      this.headerClass = headerClass;
      return this;
    }

    /** Returns the payload class to use for parsing. */
    public Class<? extends Payload> getPayloadClass() {
      return payloadClass;
    }

    /** Sets the payload class to use for parsing. */
    public Parser setPayloadClass(Class<? extends Payload> payloadClass) {
      this.payloadClass = payloadClass;
      return this;
    }

    /** Returns the JSON factory. */
    public JsonFactory getJsonFactory() {
      return jsonFactory;
    }

    /**
     * Parses a JWS token into a parsed {@link JsonWebSignature}.
     *
     * @param tokenString JWS token string
     * @return parsed {@link JsonWebSignature}
     */
    public JsonWebSignature parse(String tokenString) throws IOException {
      // split on the dots
      int firstDot = tokenString.indexOf('.');
      Preconditions.checkArgument(firstDot != -1);
      byte[] headerBytes = Base64.decodeBase64(tokenString.substring(0, firstDot));
      int secondDot = tokenString.indexOf('.', firstDot + 1);
      Preconditions.checkArgument(secondDot != -1);
      Preconditions.checkArgument(tokenString.indexOf('.', secondDot + 1) == -1);
      // decode the bytes
      byte[] payloadBytes = Base64.decodeBase64(tokenString.substring(firstDot + 1, secondDot));
      byte[] signatureBytes = Base64.decodeBase64(tokenString.substring(secondDot + 1));
      byte[] signedContentBytes = StringUtils.getBytesUtf8(tokenString.substring(0, secondDot));
      // parse the header and payload
      Header header =
          jsonFactory.fromInputStream(new ByteArrayInputStream(headerBytes), headerClass);
      Preconditions.checkArgument(header.getAlgorithm() != null);
      Payload payload =
          jsonFactory.fromInputStream(new ByteArrayInputStream(payloadBytes), payloadClass);
      return new JsonWebSignature(header, payload, signatureBytes, signedContentBytes);
    }
  }

  /**
   * Signs a given JWS header and payload based on the given private key using RSA and SHA-256 as
   * described in <a
   * href="http://tools.ietf.org/html/draft-ietf-jose-json-web-signature-11#appendix-A.2">JWS using
   * RSA SHA-256</a>.
   *
   * @param privateKey private key
   * @param jsonFactory JSON factory
   * @param header JWS header
   * @param payload JWS payload
   * @return signed JWS string
   * @since 1.14 (since 1.7 as com.google.api.client.auth.jsontoken.RsaSHA256Signer)
   */
  public static String signUsingRsaSha256(
      PrivateKey privateKey,
      JsonFactory jsonFactory,
      JsonWebSignature.Header header,
      JsonWebToken.Payload payload)
      throws GeneralSecurityException, IOException {
    String content =
        Base64.encodeBase64URLSafeString(jsonFactory.toByteArray(header))
            + "."
            + Base64.encodeBase64URLSafeString(jsonFactory.toByteArray(payload));
    byte[] contentBytes = StringUtils.getBytesUtf8(content);
    byte[] signature =
        SecurityUtils.sign(
            SecurityUtils.getSha256WithRsaSignatureAlgorithm(), privateKey, contentBytes);
    return content + "." + Base64.encodeBase64URLSafeString(signature);
  }
}
