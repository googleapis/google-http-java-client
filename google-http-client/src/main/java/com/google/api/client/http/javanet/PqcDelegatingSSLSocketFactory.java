package com.google.api.client.http.javanet;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

// Custom SSLSocketFactory that wraps created sockets to configure PQC named groups.
final class PqcDelegatingSSLSocketFactory extends SSLSocketFactory {
  // The real Bouncy Castle JJSSE socket factory we delegate to.
  private final SSLSocketFactory delegate;

  // Constructor to store the delegate factory.
  PqcDelegatingSSLSocketFactory(SSLSocketFactory delegate) {
    this.delegate = delegate;
  }

  // Internal helper to apply PQC named groups to created SSLSocket.
  private Socket configureSocket(Socket socket) {
    // BCSSLSocket extends SSLSocket, so checking this implicitly verifies it is an SSLSocket.
    if (socket instanceof org.bouncycastle.jsse.BCSSLSocket) {
      // Cast to BCSSLSocket safely to edit specific JJSSE parameters.
      org.bouncycastle.jsse.BCSSLSocket bcSocket = (org.bouncycastle.jsse.BCSSLSocket) socket;
      // Retrieve parameters to edit them.
      org.bouncycastle.jsse.BCSSLParameters bcParams = bcSocket.getParameters();
      // Set named groups to prefer PQC hybrid and pure groups with classical fallback:
      // 1. X25519MLKEM768 (codepoint 4588): Hybrid classical (X25519) + post-quantum (ML-KEM-768) key exchange.
      //    Provides defense-in-depth: if ML-KEM is compromised, security reverts to classical strength of X25519.
      // 2. MLKEM768 (codepoint 1896): Pure post-quantum key exchange using ML-KEM-768.
      // 3. X25519 (codepoint 29): Classical elliptic curve Diffie-Hellman key exchange, used as a fallback.
      bcParams.setNamedGroups(new String[]{"X25519MLKEM768", "MLKEM768", "X25519"});
      // Apply parameters back to the socket scope-specifically.
      bcSocket.setParameters(bcParams);
    }
    // Return the configured socket.
    return socket;
  }

  // Delegate default cipher suites query.
  @Override
  public String[] getDefaultCipherSuites() {
    return delegate.getDefaultCipherSuites();
  }

  // Delegate supported cipher suites query.
  @Override
  public String[] getSupportedCipherSuites() {
    return delegate.getSupportedCipherSuites();
  }

  // Intercept and configure sockets created via various factory methods.
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
    return configureSocket(delegate.createSocket(address, port));
  }

  @Override
  public Socket createSocket(InetAddress address, int port, InetAddress localAddress, int localPort)
      throws IOException {
    return configureSocket(delegate.createSocket(address, port, localAddress, localPort));
  }
}
