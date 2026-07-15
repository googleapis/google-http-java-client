/*
 * Copyright (c) 2026 Google Inc.
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.security.KeyStore;
import java.security.Provider;
import java.security.SecureRandom;
import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.KeyManagerFactorySpi;
import javax.net.ssl.ManagerFactoryParameters;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLContextSpi;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSessionContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.TrustManagerFactorySpi;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Tests {@link SslUtils}. */
@RunWith(JUnit4.class)
public class SslUtilsTest {

  private static final TrustManager MOCK_TRUST_MANAGER = new TrustManager() {};
  private static final KeyManager MOCK_KEY_MANAGER = new KeyManager() {};

  private static final SSLSocketFactory MOCK_SSL_SOCKET_FACTORY =
      new SSLSocketFactory() {
        @Override
        public String[] getDefaultCipherSuites() {
          return new String[0];
        }

        @Override
        public String[] getSupportedCipherSuites() {
          return new String[0];
        }

        @Override
        public Socket createSocket(Socket s, String host, int port, boolean autoClose)
            throws IOException {
          return null;
        }

        @Override
        public Socket createSocket(String host, int port) throws IOException {
          return null;
        }

        @Override
        public Socket createSocket(String host, int port, InetAddress localHost, int localPort)
            throws IOException {
          return null;
        }

        @Override
        public Socket createSocket(InetAddress host, int port) throws IOException {
          return null;
        }

        @Override
        public Socket createSocket(
            InetAddress address, int port, InetAddress localAddress, int localPort)
            throws IOException {
          return null;
        }
      };

  /** Mock SPI implementation for SSLContext. */
  public static class MockSslContextSpi extends SSLContextSpi {
    @Override
    protected void engineInit(KeyManager[] km, TrustManager[] tm, SecureRandom sr) {}

    @Override
    protected SSLSocketFactory engineGetSocketFactory() {
      return MOCK_SSL_SOCKET_FACTORY;
    }

    @Override
    protected SSLServerSocketFactory engineGetServerSocketFactory() {
      return null;
    }

    @Override
    protected SSLEngine engineCreateSSLEngine() {
      return null;
    }

    @Override
    protected SSLEngine engineCreateSSLEngine(String host, int port) {
      return null;
    }

    @Override
    protected SSLSessionContext engineGetServerSessionContext() {
      return null;
    }

    @Override
    protected SSLSessionContext engineGetClientSessionContext() {
      return null;
    }
  }

  /** Mock SPI implementation for TrustManagerFactory. */
  public static class MockTrustManagerFactorySpi extends TrustManagerFactorySpi {
    @Override
    protected void engineInit(KeyStore ks) {}

    @Override
    protected void engineInit(ManagerFactoryParameters mfp) {}

    @Override
    protected TrustManager[] engineGetTrustManagers() {
      return new TrustManager[] {MOCK_TRUST_MANAGER};
    }
  }

  /** Mock SPI implementation for KeyManagerFactory. */
  public static class MockKeyManagerFactorySpi extends KeyManagerFactorySpi {
    @Override
    protected void engineInit(KeyStore ks, char[] password) {}

    @Override
    protected void engineInit(ManagerFactoryParameters mfp) {}

    @Override
    protected KeyManager[] engineGetKeyManagers() {
      return new KeyManager[] {MOCK_KEY_MANAGER};
    }
  }

  // A mock security provider used to verify that SslUtils correctly delegates context and factory
  // initialization to the configured Provider instance. Using a mock provider avoids having to
  // load platform-dependent native libraries (such as Conscrypt or OpenSSL) during unit testing.
  private static final Provider mockProvider =
      new Provider("MockProvider", 1.0, "For testing") {
        private static final long serialVersionUID = 1L;

        {
          put("SSLContext.TLS", MockSslContextSpi.class.getName());
          put(
              "TrustManagerFactory." + TrustManagerFactory.getDefaultAlgorithm(),
              MockTrustManagerFactorySpi.class.getName());
          put("TrustManagerFactory.PKIX", MockTrustManagerFactorySpi.class.getName());
          put(
              "KeyManagerFactory." + KeyManagerFactory.getDefaultAlgorithm(),
              MockKeyManagerFactorySpi.class.getName());
        }
      };

  @Test
  public void testGetTlsSslContext() throws Exception {
    SSLContext context = SslUtils.getTlsSslContext();
    assertNotNull(context);
    assertEquals("TLS", context.getProtocol());
  }

  @Test
  public void testGetTlsSslContext_withCustomProvider() throws Exception {
    SSLContext context = SslUtils.getTlsSslContext(mockProvider);
    assertNotNull(context);
    assertEquals("TLS", context.getProtocol());
    assertEquals(mockProvider, context.getProvider());
    assertEquals(MOCK_SSL_SOCKET_FACTORY, context.getSocketFactory());
  }

  @Test
  public void testGetDefaultTrustManagerFactory() throws Exception {
    TrustManagerFactory tmf = SslUtils.getDefaultTrustManagerFactory();
    assertNotNull(tmf);
    assertEquals(TrustManagerFactory.getDefaultAlgorithm(), tmf.getAlgorithm());
  }

  @Test
  public void testGetDefaultTrustManagerFactory_withCustomProvider() throws Exception {
    TrustManagerFactory tmf = SslUtils.getDefaultTrustManagerFactory(mockProvider);
    assertNotNull(tmf);
    assertEquals(TrustManagerFactory.getDefaultAlgorithm(), tmf.getAlgorithm());
    assertEquals(mockProvider, tmf.getProvider());

    tmf.init((KeyStore) null);
    TrustManager[] tms = tmf.getTrustManagers();
    assertEquals(1, tms.length);
    assertEquals(MOCK_TRUST_MANAGER, tms[0]);
  }

  @Test
  public void testGetPkixTrustManagerFactory() throws Exception {
    TrustManagerFactory tmf = SslUtils.getPkixTrustManagerFactory();
    assertNotNull(tmf);
    assertEquals("PKIX", tmf.getAlgorithm());
  }

  @Test
  public void testGetPkixTrustManagerFactory_withCustomProvider() throws Exception {
    TrustManagerFactory tmf = SslUtils.getPkixTrustManagerFactory(mockProvider);
    assertNotNull(tmf);
    assertEquals("PKIX", tmf.getAlgorithm());
    assertEquals(mockProvider, tmf.getProvider());

    tmf.init((KeyStore) null);
    TrustManager[] tms = tmf.getTrustManagers();
    assertEquals(1, tms.length);
    assertEquals(MOCK_TRUST_MANAGER, tms[0]);
  }

  @Test
  public void testGetDefaultKeyManagerFactory() throws Exception {
    KeyManagerFactory kmf = SslUtils.getDefaultKeyManagerFactory();
    assertNotNull(kmf);
    assertEquals(KeyManagerFactory.getDefaultAlgorithm(), kmf.getAlgorithm());
  }

  @Test
  public void testGetDefaultKeyManagerFactory_withCustomProvider() throws Exception {
    KeyManagerFactory kmf = SslUtils.getDefaultKeyManagerFactory(mockProvider);
    assertNotNull(kmf);
    assertEquals(KeyManagerFactory.getDefaultAlgorithm(), kmf.getAlgorithm());
    assertEquals(mockProvider, kmf.getProvider());

    kmf.init((KeyStore) null, new char[0]);
    KeyManager[] kms = kmf.getKeyManagers();
    assertEquals(1, kms.length);
    assertEquals(MOCK_KEY_MANAGER, kms[0]);
  }

  @Test
  public void testGetPkixKeyManagerFactory() throws Exception {
    KeyManagerFactory kmf = SslUtils.getPkixKeyManagerFactory();
    assertNotNull(kmf);
    assertEquals("PKIX", kmf.getAlgorithm());
  }

  @Test
  public void testInitSslContext() throws Exception {
    SSLContext context = SslUtils.getTlsSslContext();
    TrustManagerFactory tmf = SslUtils.getDefaultTrustManagerFactory();
    tmf.init((KeyStore) null);
    SSLContext initializedContext = SslUtils.initSslContext(context, null, tmf);
    assertEquals(context, initializedContext);
  }
}
