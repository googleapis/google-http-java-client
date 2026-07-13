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

import java.io.IOException;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.Socket;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.net.ssl.SSLParameters;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

/**
 * An {@link SSLSocketFactory} wrapper that configures named groups (curves) on SSLSocket
 * connections via JCA standard APIs or provider-specific reflection (Conscrypt).
 */
final class NamedGroupsSSLSocketFactory extends SSLSocketFactory {
  private static final Logger logger =
      Logger.getLogger(NamedGroupsSSLSocketFactory.class.getName());

  private static final boolean CAN_USE_JDK_NAMED_GROUPS_API = checkJdkNamedGroupsApiAvailability();
  private static final Method CONSCRYPT_IS_CONSCYPT_METHOD =
      getConscryptMethod("isConscrypt", SSLSocket.class);
  private static final Method CONSCRYPT_SET_NAMED_GROUPS_METHOD =
      getConscryptMethod("setNamedGroups", SSLSocket.class, String[].class);

  private static final AtomicBoolean loggedPqcWarning = new AtomicBoolean(false);
  private static final AtomicBoolean loggedReflectionWarning = new AtomicBoolean(false);

  private final SSLSocketFactory delegate;
  private final String[] groups;

  NamedGroupsSSLSocketFactory(SSLSocketFactory delegate, String[] groups) {
    this.delegate = delegate;
    this.groups = groups;
  }

  private static boolean checkJdkNamedGroupsApiAvailability() {
    try {
      SSLParameters.class.getMethod("setNamedGroups", String[].class);
      return true;
    } catch (Throwable t) {
      return false;
    }
  }

  private static Method getConscryptMethod(String methodName, Class<?>... parameterTypes) {
    try {
      Class<?> conscryptClass = Class.forName("org.conscrypt.Conscrypt");
      return conscryptClass.getMethod(methodName, parameterTypes);
    } catch (Throwable t) {
      return null;
    }
  }

  private Socket configure(Socket socket) {
    if (socket instanceof SSLSocket) {
      SSLSocket sslSocket = (SSLSocket) socket;
      try {
        // 1. Try Conscrypt-specific JNI API via reflection first (compatible with Java 8+)
        if (CONSCRYPT_IS_CONSCYPT_METHOD != null && CONSCRYPT_SET_NAMED_GROUPS_METHOD != null) {
          boolean isConscrypt = (Boolean) CONSCRYPT_IS_CONSCYPT_METHOD.invoke(null, sslSocket);
          if (isConscrypt) {
            CONSCRYPT_SET_NAMED_GROUPS_METHOD.invoke(null, sslSocket, (Object) groups);
            return socket;
          }
        }

        // 2. Fallback: Try standard JCA named groups API via reflection (Java 20+)
        if (CAN_USE_JDK_NAMED_GROUPS_API) {
          trySetNamedGroupsViaReflection(sslSocket, groups);
        } else {
          if (loggedPqcWarning.compareAndSet(false, true)) {
            logger.log(
                Level.WARNING,
                "The Java 20+ SSLParameters.setNamedGroups API is not available on this JRE, "
                    + "and Conscrypt is not active. Cannot configure PQC curve negotiation; "
                    + "falling back to the standard TLS connection configuration.");
          }
        }
      } catch (Throwable t) {
        if (loggedReflectionWarning.compareAndSet(false, true)) {
          logger.log(
              Level.WARNING,
              "Failed to configure custom named groups (PQC) via standard JSSE reflection"
                  + " parameters. Cannot configure PQC curve negotiation; falling back to the"
                  + " standard TLS connection configuration.",
              t);
        }
      }
    }
    return socket;
  }

  private static boolean trySetNamedGroupsViaReflection(SSLSocket sslSocket, String[] groups)
      throws Throwable {
    Object sslParameters = sslSocket.getClass().getMethod("getSSLParameters").invoke(sslSocket);
    if (sslParameters != null) {
      Method setNamedGroupsMethod =
          sslParameters.getClass().getMethod("setNamedGroups", String[].class);
      setNamedGroupsMethod.invoke(sslParameters, (Object) groups);
      sslSocket
          .getClass()
          .getMethod("setSSLParameters", sslParameters.getClass())
          .invoke(sslSocket, sslParameters);
      return true;
    }
    return false;
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
