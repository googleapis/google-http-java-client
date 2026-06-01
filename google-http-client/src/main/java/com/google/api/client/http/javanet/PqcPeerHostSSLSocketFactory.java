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

package com.google.api.client.http.javanet;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import javax.net.ssl.SSLSocketFactory;

/**
 * A custom {@link SSLSocketFactory} wrapper designed to ensure that the peer hostname is preserved
 * during connection establishment.
 *
 * <p>When secure connections are initiated via Java's default {@code HttpURLConnection}, some
 * socket-creation flows only provide an {@link InetAddress} instead of the DNS hostname. Under
 * hybrid TLS configurations—such as Post-Quantum Cryptography (PQC)—underlying JSSE security
 * providers (Conscrypt or Bouncy Castle JSSE) rely on the peer hostname string to enable proper
 * Server Name Indication (SNI) extensions, negotiate PQC cipher suites, and perform endpoint
 * identification.
 *
 * <p>This wrapper intercepts socket creation requests, manually establishes the TCP socket
 * connection to the target address, and wraps it using the delegate's hostname-aware factory
 * method.
 */
class PqcPeerHostSSLSocketFactory extends SSLSocketFactory {

  private final SSLSocketFactory delegate;
  private final String host;

  /**
   * Constructs a new {@link PqcPeerHostSSLSocketFactory} wrapping the provided delegate.
   *
   * @param delegate the underlying {@link SSLSocketFactory}
   * @param host the peer hostname to propagate to the delegate socket factory
   */
  PqcPeerHostSSLSocketFactory(SSLSocketFactory delegate, String host) {
    this.delegate = delegate;
    this.host = host;
  }

  @Override
  public String[] getDefaultCipherSuites() {
    return delegate.getDefaultCipherSuites();
  }

  @Override
  public String[] getSupportedCipherSuites() {
    return delegate.getSupportedCipherSuites();
  }

  @Override
  public Socket createSocket(Socket s, String host, int port, boolean autoClose)
      throws IOException {
    return configureSocket(delegate.createSocket(s, host, port, autoClose));
  }

  @Override
  public Socket createSocket() throws IOException {
    return configureSocket(delegate.createSocket());
  }

  @Override
  public Socket createSocket(String host, int port) throws IOException, UnknownHostException {
    return configureSocket(delegate.createSocket(host, port));
  }

  @Override
  public Socket createSocket(String host, int port, InetAddress localAddress, int localPort)
      throws IOException, UnknownHostException {
    return configureSocket(delegate.createSocket(host, port, localAddress, localPort));
  }

  @Override
  public Socket createSocket(InetAddress address, int port) throws IOException {
    Socket plainSocket = new Socket();
    plainSocket.connect(new InetSocketAddress(address, port));
    return configureSocket(delegate.createSocket(plainSocket, this.host, port, true));
  }

  @Override
  public Socket createSocket(InetAddress address, int port, InetAddress localAddress, int localPort)
      throws IOException {
    Socket plainSocket = new Socket();
    plainSocket.bind(new InetSocketAddress(localAddress, localPort));
    plainSocket.connect(new InetSocketAddress(address, port));
    return configureSocket(delegate.createSocket(plainSocket, this.host, port, true));
  }

  @org.codehaus.mojo.animal_sniffer.IgnoreJRERequirement
  private Socket configureSocket(Socket socket) throws IOException {
    if (socket instanceof javax.net.ssl.SSLSocket) {
      javax.net.ssl.SSLSocket sslSocket = (javax.net.ssl.SSLSocket) socket;
      javax.net.ssl.SSLParameters params = sslSocket.getSSLParameters();
      if (params != null) {
        java.util.List<javax.net.ssl.SNIServerName> serverNames = new java.util.ArrayList<>();
        serverNames.add(new javax.net.ssl.SNIHostName(this.host));
        params.setServerNames(serverNames);
        sslSocket.setSSLParameters(params);
      }
      configureConscryptNamedGroups(sslSocket);
    }
    return socket;
  }

  private void configureConscryptNamedGroups(javax.net.ssl.SSLSocket socket) throws IOException {
    try {
      Class<?> fileDescClass = Class.forName("org.conscrypt.ConscryptFileDescriptorSocket");
      Class<?> engineSocketClass = Class.forName("org.conscrypt.ConscryptEngineSocket");
      Object sslParametersImpl = null;

      if (fileDescClass.isInstance(socket)) {
        java.lang.reflect.Field f = fileDescClass.getDeclaredField("sslParameters");
        f.setAccessible(true);
        sslParametersImpl = f.get(socket);
      } else if (engineSocketClass.isInstance(socket)) {
        java.lang.reflect.Field fEngine = engineSocketClass.getDeclaredField("engine");
        fEngine.setAccessible(true);
        Object engine = fEngine.get(socket);
        if (engine != null) {
          Class<?> engineClass = Class.forName("org.conscrypt.ConscryptEngine");
          java.lang.reflect.Field fParams = engineClass.getDeclaredField("sslParameters");
          fParams.setAccessible(true);
          sslParametersImpl = fParams.get(engine);
        }
      } else {
        Class<?> current = socket.getClass();
        while (current != null && current != Object.class) {
          try {
            java.lang.reflect.Field f = current.getDeclaredField("sslParameters");
            f.setAccessible(true);
            sslParametersImpl = f.get(socket);
            break;
          } catch (NoSuchFieldException e) {
            current = current.getSuperclass();
          }
        }
      }

      if (sslParametersImpl != null) {
        Class<?> paramsClass = Class.forName("org.conscrypt.SSLParametersImpl");
        java.lang.reflect.Method setNamedGroupsMethod = paramsClass.getDeclaredMethod("setNamedGroups", String[].class);
        setNamedGroupsMethod.setAccessible(true);
        setNamedGroupsMethod.invoke(sslParametersImpl, (Object) new String[] {
          "X25519MLKEM768",
          "x25519_mlkem768",
          "X25519Kyber768Draft00",
          "x25519_kyber768",
          "x25519",
          "secp256r1"
        });
      }
    } catch (Exception e) {
			 throw new IOException(e);
    }
  }
}
