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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.List;
import javax.net.ssl.X509TrustManager;

/**
 * Utilities related to Java security.
 *
 * @since 1.14
 * @author Yaniv Inbar
 */
public final class SecurityUtils {

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
   * <p>Example usage:
   *
   * <pre>
   * KeyStore keyStore = SecurityUtils.getJavaKeyStore();
   * SecurityUtils.loadKeyStore(keyStore, new FileInputStream("certs.jks"), "password");
   * </pre>
   *
   * @param keyStore key store
   * @param keyStream input stream to the key store stream (closed at the end of this method in a
   *     finally block)
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
   *     block)
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

  /** Returns the RSA key factory. */
  public static KeyFactory getRsaKeyFactory() throws NoSuchAlgorithmException {
    return KeyFactory.getInstance("RSA");
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

  /**
   * Verifies the signature of signed content based on a public key.
   *
   * @param signatureAlgorithm signature algorithm
   * @param publicKey public key
   * @param signatureBytes signature bytes
   * @param contentBytes content bytes
   * @return whether the signature was verified
   */
  public static boolean verify(
      Signature signatureAlgorithm, PublicKey publicKey, byte[] signatureBytes, byte[] contentBytes)
      throws InvalidKeyException, SignatureException {
    signatureAlgorithm.initVerify(publicKey);
    signatureAlgorithm.update(contentBytes);
    // SignatureException may be thrown if we are tring the wrong key.
    try {
      return signatureAlgorithm.verify(signatureBytes);
    } catch (SignatureException e) {
      return false;
    }
  }

  /**
   * Verifies the signature of signed content based on a certificate chain.
   *
   * @param signatureAlgorithm signature algorithm
   * @param trustManager trust manager used to verify the certificate chain
   * @param certChainBase64 Certificate chain used for verification. The certificates must be base64
   *     encoded DER, the leaf certificate must be the first element.
   * @param signatureBytes signature bytes
   * @param contentBytes content bytes
   * @return The signature certificate if the signature could be verified, null otherwise.
   * @since 1.19.1.
   */
  public static X509Certificate verify(
      Signature signatureAlgorithm,
      X509TrustManager trustManager,
      List<String> certChainBase64,
      byte[] signatureBytes,
      byte[] contentBytes)
      throws InvalidKeyException, SignatureException {
    CertificateFactory certificateFactory;
    try {
      certificateFactory = getX509CertificateFactory();
    } catch (CertificateException e) {
      return null;
    }
    X509Certificate[] certificates = new X509Certificate[certChainBase64.size()];
    int currentCert = 0;
    for (String certBase64 : certChainBase64) {
      byte[] certDer = Base64.decodeBase64(certBase64);
      ByteArrayInputStream bis = new ByteArrayInputStream(certDer);
      try {
        Certificate cert = certificateFactory.generateCertificate(bis);
        if (!(cert instanceof X509Certificate)) {
          return null;
        }
        certificates[currentCert++] = (X509Certificate) cert;
      } catch (CertificateException e) {
        return null;
      }
    }
    try {
      trustManager.checkServerTrusted(certificates, "RSA");
    } catch (CertificateException e) {
      return null;
    }
    PublicKey pubKey = certificates[0].getPublicKey();
    if (verify(signatureAlgorithm, pubKey, signatureBytes, contentBytes)) {
      return certificates[0];
    }
    return null;
  }

  /** Returns the X.509 certificate factory. */
  public static CertificateFactory getX509CertificateFactory() throws CertificateException {
    return CertificateFactory.getInstance("X.509");
  }

  /**
   * Loads a key store with certificates generated from the specified stream using {@link
   * CertificateFactory#generateCertificates(InputStream)}.
   *
   * <p>For each certificate, {@link KeyStore#setCertificateEntry(String, Certificate)} is called
   * with an alias that is the string form of incrementing non-negative integers starting with 0 (0,
   * 1, 2, 3, ...).
   *
   * <p>Example usage:
   *
   * <pre>
   * KeyStore keyStore = SecurityUtils.getJavaKeyStore();
   * SecurityUtils.loadKeyStoreFromCertificates(keyStore, SecurityUtils.getX509CertificateFactory(),
   * new FileInputStream(pemFile));
   * </pre>
   *
   * @param keyStore key store (for example {@link #getJavaKeyStore()})
   * @param certificateFactory certificate factory (for example {@link
   *     #getX509CertificateFactory()})
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

  private SecurityUtils() {}
}
