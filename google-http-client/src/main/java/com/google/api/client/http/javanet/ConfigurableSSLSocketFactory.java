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
import java.net.Socket;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

/**
 * An {@link SSLSocketFactory} wrapper that intercepts all socket creation entrypoints (both default
 * and bound socket creations) and applies the custom user-provided {@link SslSocketConfigurator}
 * callback to the socket before returning it.
 *
 * <p>This factory delegates all standard socket actions to the underlying default or custom {@link
 * SSLSocketFactory} instance. Sockets are intercepted and cast to {@link SSLSocket} for callback
 * execution.
 *
 * @since 2.1.2
 */
final class ConfigurableSSLSocketFactory extends SSLSocketFactory {
  private final SSLSocketFactory delegate;
  private final SslSocketConfigurator configurator;

  ConfigurableSSLSocketFactory(SSLSocketFactory delegate, SslSocketConfigurator configurator) {
    this.delegate = delegate;
    this.configurator = configurator;
  }

  private Socket configure(Socket socket) {
    if (socket instanceof SSLSocket && configurator != null) {
      configurator.configure((SSLSocket) socket);
    }
    return socket;
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
  public Socket createSocket() throws IOException {
    return configure(delegate.createSocket());
  }

  @Override
  public Socket createSocket(Socket s, String host, int port, boolean autoClose)
      throws IOException {
    return configure(delegate.createSocket(s, host, port, autoClose));
  }

  @Override
  public Socket createSocket(String host, int port) throws IOException {
    return configure(delegate.createSocket(host, port));
  }

  @Override
  public Socket createSocket(String host, int port, InetAddress localHost, int localPort)
      throws IOException {
    return configure(delegate.createSocket(host, port, localHost, localPort));
  }

  @Override
  public Socket createSocket(InetAddress host, int port) throws IOException {
    return configure(delegate.createSocket(host, port));
  }

  @Override
  public Socket createSocket(InetAddress address, int port, InetAddress localAddress, int localPort)
      throws IOException {
    return configure(delegate.createSocket(address, port, localAddress, localPort));
  }
}
