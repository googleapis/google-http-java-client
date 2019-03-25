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

package com.google.api.client.http.apache;

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import org.apache.http.conn.ssl.SSLSocketFactory;

/**
 * Implementation of SSL socket factory that extends Apache's implementation to provide
 * functionality missing from the Android SDK that is available in Apache HTTP Client.
 *
 * @author Yaniv Inbar
 */
final class SSLSocketFactoryExtension extends SSLSocketFactory {

  /** Wrapped Java SSL socket factory. */
  private final javax.net.ssl.SSLSocketFactory socketFactory;

  /** @param sslContext SSL context */
  SSLSocketFactoryExtension(SSLContext sslContext)
      throws KeyManagementException, UnrecoverableKeyException, NoSuchAlgorithmException,
          KeyStoreException {
    super((KeyStore) null);
    socketFactory = sslContext.getSocketFactory();
  }

  @Override
  public Socket createSocket() throws IOException {
    return socketFactory.createSocket();
  }

  @Override
  public Socket createSocket(Socket socket, String host, int port, boolean autoClose)
      throws IOException, UnknownHostException {
    SSLSocket sslSocket = (SSLSocket) socketFactory.createSocket(socket, host, port, autoClose);
    getHostnameVerifier().verify(host, sslSocket);
    return sslSocket;
  }
}
