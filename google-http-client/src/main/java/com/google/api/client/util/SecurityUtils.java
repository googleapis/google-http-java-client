/*
 * Copyright (c) 2013 Google Inc.
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

package com.google.api.client.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.Signature;
import java.security.SignatureException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;

/**
 * Utilities related to Java security.
 *
 * @since 1.14
 * @author Yaniv Inbar
 */
public final class SecurityUtils {

  private static final String BEGIN = "-----BEGIN PRIVATE KEY-----";
  private static final String END = "-----END PRIVATE KEY-----";

  /** Returns the default key store using {@link KeyStore#getDefaultType()}. */
  public static KeyStore getDefaultKeyStore() throws KeyStoreException {
    return KeyStore.getInstance(KeyStore.getDefaultType());
  }

  /** Returns the Java KeyStore (JKS). */
  public static KeyStore getJavaKeyStore() throws KeyStoreException {
    return KeyStore.getInstance("JKS");
  }

  /** Returns the PKCS12 key store. */
  public static KeyStore getPkcs12KeyStore() throws KeyStoreException {
    return KeyStore.getInstance("PKCS12");
  }

  /**
   * Loads a key store from a stream.
   *
   *
   * <p>
   * Example usage:
   * </p>
   *
   * <pre>
    KeyStore keyStore = SecurityUtils.getJavaKeyStore();
    SecurityUtils.loadKeyStore(keyStore, new FileInputStream("certs.jks"), "password");
   * </pre>
   *
   * @param keyStore key store
   * @param keyStream input stream to the key store stream (closed at the end of this method in a
   *        finally block)
   * @param storePass password protecting the key store file
   */
  public static void loadKeyStore(KeyStore keyStore, InputStream keyStream, String storePass)
      throws IOException, GeneralSecurityException {
    try {
      keyStore.load(keyStream, storePass.toCharArray());
    } finally {
      keyStream.close();
    }
  }

  /**
   * Returns the private key from the key store.
   *
   * @param keyStore key store
   * @param alias alias under which the key is stored
   * @param keyPass password protecting the key
   * @return private key
   */
  public static PrivateKey getPrivateKey(KeyStore keyStore, String alias, String keyPass)
      throws GeneralSecurityException {
    return (PrivateKey) keyStore.getKey(alias, keyPass.toCharArray());
  }

  /**
   * Retrieves a private key from the specified key store stream and specified key store.
   *
   * @param keyStore key store
   * @param keyStream input stream to the key store (closed at the end of this method in a finally
   *        block)
   * @param storePass password protecting the key store file
   * @param alias alias under which the key is stored
   * @param keyPass password protecting the key
   * @return key from the key store
   */
  public static PrivateKey loadPrivateKeyFromKeyStore(
      KeyStore keyStore, InputStream keyStream, String storePass, String alias, String keyPass)
      throws IOException, GeneralSecurityException {
    loadKeyStore(keyStore, keyStream, storePass);
    return getPrivateKey(keyStore, alias, keyPass);
  }

  /**
   * Reads a private key from a {@code PEM} formatted stream.
   *
   * <p>
   * This supports any PEM stream if and only if it contains a DER and Base64 encoded key, and the
   * contents are enclosed by the following:
   * </p>
   *
   * <pre>
   *-----BEGIN PRIVATE KEY-----
   *-----END PRIVATE KEY-----
   *</pre>
   *
   * <p>
   * The PEM stream may contain additional content outside of the BEGIN and END tags, but it will be
   * ignored. This method does not support additional content such as headers inside the BEGIN and
   * END tags. If the file contains multiple BEGIN and END tags, only the content inside the first
   * pair will be read.
   * </p>
   *
   * <p>
   * Example usage:
   * </p>
   *
   * <pre>
    byte[] encodedKey =
        SecurityUtils.readPrivateKeyFromPem(new FileInputStream("secret.pem"), Charset
            .defaultCharset().name());
   * </pre>
   *
   * @param pemStream PEM input stream
   * @param charsetName charset for reading PEM input stream
   */
  public static byte[] readPrivateKeyFromPem(InputStream pemStream, String charsetName)
      throws IOException, GeneralSecurityException {
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    IOUtils.copy(pemStream, out);
    String str = out.toString(charsetName);
    int beginIndex = str.indexOf(BEGIN);
    int endIndex = str.indexOf(END);
    int startKeyIndex = beginIndex + BEGIN.length();
    if (beginIndex == -1 || startKeyIndex >= endIndex) {
      throw new GeneralSecurityException(
          "Missing required BEGIN PRIVATE KEY or END PRIVATE KEY tags.");
    }
    String privKey = str.substring(startKeyIndex, endIndex);
    return Base64.decodeBase64(privKey);
  }

  /** Returns the RSA key factory. */
  public static KeyFactory getRsaKeyFactory() throws NoSuchAlgorithmException {
    return KeyFactory.getInstance("RSA");
  }

  /**
   * Generates a {@code PKCS8} encoded private key.
   *
   *
   * <p>
   * Example usage:
   * </p>
   *
   * <pre>
    PrivateKey privateKey = SecurityUtils.generatePkcs8PrivateKey(
        SecurityUtils.getRsaKeyFactory(), encodedKey);
   * </pre>
   *
   * @param keyFactory key factory
   * @param encodedKeyBytes encoded key bytes
   * @return generated private key
   */
  public static PrivateKey generatePkcs8PrivateKey(KeyFactory keyFactory, byte[] encodedKeyBytes)
      throws InvalidKeySpecException {
    return keyFactory.generatePrivate(new PKCS8EncodedKeySpec(encodedKeyBytes));
  }

  /**
   * Generates a {@code PKCS8} encoded private key loaded from a {@code PEM} formatted stream.
   *
   * <p>
   * Example usage:
   * </p>
   *
   * <pre>
    PrivateKey privateKey = SecurityUtils.loadPkcs8PrivateKeyFromPem(
        SecurityUtils.getRsaKeyFactory(), new FileInputStream("secret.pem"),
        Charset.defaultCharset().name());
   * </pre>
   *
   * @param keyFactory key factory
   * @param pemStream PEM input stream
   * @param charsetName charset for reading PEM input stream
   * @return generated private key
   */
  public static PrivateKey loadPkcs8PrivateKeyFromPem(
      KeyFactory keyFactory, InputStream pemStream, String charsetName)
      throws GeneralSecurityException, IOException {
    return generatePkcs8PrivateKey(keyFactory, readPrivateKeyFromPem(pemStream, charsetName));
  }

  /** Returns the SHA-1 with RSA signature algorithm. */
  public static Signature getSha1WithRsaSignatureAlgorithm() throws NoSuchAlgorithmException {
    return Signature.getInstance("SHA1withRSA");
  }

  /** Returns the SHA-256 with RSA signature algorithm. */
  public static Signature getSha256WithRsaSignatureAlgorithm() throws NoSuchAlgorithmException {
    return Signature.getInstance("SHA256withRSA");
  }

  /**
   * Signs content using a private key.
   *
   * @param signatureAlgorithm signature algorithm
   * @param privateKey private key
   * @param contentBytes content to sign
   * @return signed content
   */
  public static byte[] sign(
      Signature signatureAlgorithm, PrivateKey privateKey, byte[] contentBytes)
      throws InvalidKeyException, SignatureException {
    signatureAlgorithm.initSign(privateKey);
    signatureAlgorithm.update(contentBytes);
    return signatureAlgorithm.sign();
  }

  /** Returns the X.509 certificate factory. */
  public static CertificateFactory getX509CertificateFactory() throws CertificateException {
    return CertificateFactory.getInstance("X.509");
  }

  /**
   * Loads a key store with certificates generated from the specified stream using
   * {@link CertificateFactory#generateCertificates(InputStream)}.
   *
   * <p>
   * For each certificate, {@link KeyStore#setCertificateEntry(String, Certificate)} is called with
   * an alias that is the string form of incrementing non-negative integers starting with 0 (0, 1,
   * 2, 3, ...).
   * </p>
   *
   * <p>
   * Example usage:
   * </p>
   *
   * <pre>
    KeyStore keyStore = SecurityUtils.getJavaKeyStore();
    SecurityUtils.loadKeyStoreFromCertificates(keyStore, SecurityUtils.getX509CertificateFactory(),
        new FileInputStream(pemFile));
   * </pre>
   *
   * @param keyStore key store (for example {@link #getJavaKeyStore()})
   * @param certificateFactory certificate factory (for example
   *        {@link #getX509CertificateFactory()})
   * @param certificateStream certificate stream
   */
  public static void loadKeyStoreFromCertificates(
      KeyStore keyStore, CertificateFactory certificateFactory, InputStream certificateStream)
      throws GeneralSecurityException {
    int i = 0;
    for (Certificate cert : certificateFactory.generateCertificates(certificateStream)) {
      keyStore.setCertificateEntry(String.valueOf(i), cert);
      i++;
    }
  }

  private SecurityUtils() {
  }
}
