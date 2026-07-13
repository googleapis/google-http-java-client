/*
 * Copyright 2026 Google LLC
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

package com.google.api.client.http.javanet;

import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import javax.net.ssl.HandshakeCompletedListener;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class NamedGroupsSSLSocketFactoryTest {

  private static final String[] TEST_GROUPS = new String[] {"X25519MLKEM768", "X25519"};

  @Test
  public void testDelegationAndNonSslSocket() throws Exception {
    Socket plainSocket = new Socket();
    FakeSSLSocketFactory delegate = new FakeSSLSocketFactory(plainSocket);
    NamedGroupsSSLSocketFactory factory = new NamedGroupsSSLSocketFactory(delegate, TEST_GROUPS);

    Socket result = factory.createSocket();
    assertSame(plainSocket, result);
    assertTrue(delegate.createSocketCalled);
  }

  @Test
  public void testConfigureNonConscryptSslSocket() throws Exception {
    FakeSSLSocket sslSocket = new FakeSSLSocket();
    FakeSSLSocketFactory delegate = new FakeSSLSocketFactory(sslSocket);
    NamedGroupsSSLSocketFactory factory = new NamedGroupsSSLSocketFactory(delegate, TEST_GROUPS);

    Socket result = factory.createSocket();
    assertSame(sslSocket, result);
    assertTrue(delegate.createSocketCalled);
  }

  @Test
  public void testConfigureConscryptSslSocket() throws Exception {
    try {
      java.security.Provider provider = org.conscrypt.Conscrypt.newProvider();
      SSLContext sslContext = SSLContext.getInstance("TLS", provider);
      sslContext.init(null, null, null);
    } catch (Throwable t) {
      org.junit.Assume.assumeNoException(
          "Conscrypt JNI library is not available on this platform", t);
    }

    java.security.Provider provider = org.conscrypt.Conscrypt.newProvider();
    SSLContext sslContext = SSLContext.getInstance("TLS", provider);
    sslContext.init(null, null, null);
    SSLSocketFactory conscryptFactory = sslContext.getSocketFactory();

    NamedGroupsSSLSocketFactory factory =
        new NamedGroupsSSLSocketFactory(conscryptFactory, TEST_GROUPS);

    Socket socket = factory.createSocket();
    try {
      if (socket instanceof SSLSocket) {
        SSLSocket sslSocket = (SSLSocket) socket;
        assertTrue(org.conscrypt.Conscrypt.isConscrypt(sslSocket));
      }
    } finally {
      socket.close();
    }
  }

  private static class FakeSSLSocketFactory extends SSLSocketFactory {
    private final Socket socketToReturn;
    boolean createSocketCalled = false;

    FakeSSLSocketFactory(Socket socketToReturn) {
      this.socketToReturn = socketToReturn;
    }

    @Override
    public String[] getDefaultCipherSuites() {
      return new String[0];
    }

    @Override
    public String[] getSupportedCipherSuites() {
      return new String[0];
    }

    @Override
    public Socket createSocket() throws IOException {
      createSocketCalled = true;
      return socketToReturn;
    }

    @Override
    public Socket createSocket(Socket s, String host, int port, boolean autoClose)
        throws IOException {
      createSocketCalled = true;
      return socketToReturn;
    }

    @Override
    public Socket createSocket(String host, int port) throws IOException {
      createSocketCalled = true;
      return socketToReturn;
    }

    @Override
    public Socket createSocket(String host, int port, InetAddress localHost, int localPort)
        throws IOException {
      createSocketCalled = true;
      return socketToReturn;
    }

    @Override
    public Socket createSocket(InetAddress host, int port) throws IOException {
      createSocketCalled = true;
      return socketToReturn;
    }

    @Override
    public Socket createSocket(
        InetAddress address, int port, InetAddress localAddress, int localPort) throws IOException {
      createSocketCalled = true;
      return socketToReturn;
    }
  }

  private static class FakeSSLSocket extends SSLSocket {
    @Override
    public String[] getSupportedCipherSuites() {
      return new String[0];
    }

    @Override
    public String[] getEnabledCipherSuites() {
      return new String[0];
    }

    @Override
    public void setEnabledCipherSuites(String[] suites) {}

    @Override
    public String[] getSupportedProtocols() {
      return new String[0];
    }

    @Override
    public String[] getEnabledProtocols() {
      return new String[0];
    }

    @Override
    public void setEnabledProtocols(String[] protocols) {}

    @Override
    public SSLSession getSession() {
      return null;
    }

    @Override
    public void addHandshakeCompletedListener(HandshakeCompletedListener listener) {}

    @Override
    public void removeHandshakeCompletedListener(HandshakeCompletedListener listener) {}

    @Override
    public void startHandshake() throws IOException {}

    @Override
    public void setUseClientMode(boolean mode) {}

    @Override
    public boolean getUseClientMode() {
      return false;
    }

    @Override
    public void setNeedClientAuth(boolean need) {}

    @Override
    public boolean getNeedClientAuth() {
      return false;
    }

    @Override
    public void setWantClientAuth(boolean want) {}

    @Override
    public boolean getWantClientAuth() {
      return false;
    }

    @Override
    public void setEnableSessionCreation(boolean flag) {}

    @Override
    public boolean getEnableSessionCreation() {
      return false;
    }
  }
}
