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

import javax.net.ssl.SSLSocket;

/**
 * A callback interface allowing users to programmatically configure active {@link SSLSocket}
 * parameters (e.g., named groups, cipher suites, application protocols) before the TLS handshake
 * starts.
 *
 * <p>Exposing this hook allows users to customize advanced TLS capabilities that are not
 * configurable via standard JVM system properties, or require custom security provider APIs (e.g.,
 * Conscrypt or BouncyCastle). Typical use cases include:
 *
 * <ul>
 *   <li>Enabling Post-Quantum Cryptography (PQC) hybrid curves (e.g., X25519MLKEM768).
 *   <li>Configuring Application-Layer Protocol Negotiation (ALPN) for HTTP/2.
 *   <li>Restricting cipher suites or protocol versions dynamically based on environment.
 * </ul>
 *
 * <h3>JDK Version and Provider Compatibility Matrix for PQC</h3>
 *
 * <ul>
 *   <li><b>JDK 27+</b>: PQC algorithms are supported natively by default. No custom configurator or
 *       security provider is required.
 *   <li><b>JDK 20-26</b>: Callers can configure named groups natively using standard JRE JSSE APIs
 *       (via {@code SSLParameters.setNamedGroups(String[])}), as shown in the JDK 20+ example
 *       below.
 *   <li><b>JDK 8-19</b>: The standard JRE does not expose APIs to set named groups. Callers must
 *       register a custom security provider (like Conscrypt JNI or BouncyCastle) and configure
 *       sockets using the provider-specific APIs demonstrated in the examples below.
 * </ul>
 *
 * <h3>Conscrypt Configuration Example</h3>
 *
 * <pre>{@code
 * builder.setSslSocketConfigurator(new SslSocketConfigurator() {
 *   @Override
 *   public void configure(SSLSocket socket) {
 *     if (org.conscrypt.Conscrypt.isConscrypt(socket)) {
 *       org.conscrypt.Conscrypt.setNamedGroups(socket, new String[] {"X25519MLKEM768", "X25519"});
 *     }
 *   }
 * });
 * }</pre>
 *
 * <h3>BouncyCastle Configuration Example</h3>
 *
 * <pre>{@code
 * builder.setSslSocketConfigurator(new SslSocketConfigurator() {
 *   @Override
 *   public void configure(SSLSocket socket) {
 *     if (socket instanceof org.bouncycastle.jsse.BCSSLSocket) {
 *       org.bouncycastle.jsse.BCSSLSocket bcSocket = (org.bouncycastle.jsse.BCSSLSocket) socket;
 *       org.bouncycastle.jsse.BCSSLParameters bcParams = bcSocket.getParameters();
 *       bcParams.setNamedGroups(new String[] {"X25519MLKEM768", "X25519"});
 *       bcSocket.setParameters(bcParams);
 *     }
 *   }
 * });
 * }</pre>
 *
 * <h3>JDK 20+ Standard JSSE Configuration Example</h3>
 *
 * <p>Note: This example requires compilation and runtime environment running JDK 20+.
 *
 * <pre>{@code
 * builder.setSslSocketConfigurator(new SslSocketConfigurator() {
 *   @Override
 *   public void configure(SSLSocket socket) {
 *     javax.net.ssl.SSLParameters parameters = socket.getSSLParameters();
 *     parameters.setNamedGroups(new String[] {"X25519MLKEM768", "X25519"});
 *     socket.setSSLParameters(parameters);
 *   }
 * });
 * }</pre>
 *
 * @since 2.1.2
 */
public interface SslSocketConfigurator {
  /**
   * Configures the active TLS socket parameters before the handshake starts.
   *
   * @param sslSocket the newly created SSLSocket connection
   */
  void configure(SSLSocket sslSocket);
}
