package com.google.api.client.http.javanet;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.Proxy;
import java.net.URL;

import static com.google.api.client.http.javanet.NetHttpTransport.defaultProxy;

/**
 * Default implementation of {@link ConnectionFactory}, which simply attempts to open the connection
 * with an optional {@link Proxy}.
 */
public class DefaultConnectionFactory implements ConnectionFactory {
  ConnectionFactory connectionFactory;
  private final Proxy proxy;
  private static final String SHOULD_USE_PROXY_FLAG = "com.google.api.client.should_use_proxy";

  public DefaultConnectionFactory() {
    this(null);
  }

  /**
   * @param proxy HTTP proxy or {@code null} to use the proxy settings from <a
   *     href="http://docs.oracle.com/javase/7/docs/api/java/net/doc-files/net-properties.html">
   *     system properties</a>
   */
  public DefaultConnectionFactory(Proxy proxy) {
    this.proxy = proxy;
  }

  @Override
  public HttpURLConnection openConnection(URL url) throws IOException {
    return (HttpURLConnection) (proxy == null ? url.openConnection() : url.openConnection(proxy));
  }

  public ConnectionFactory getConnectionFactory(ConnectionFactory connectionFactory) {
    if (connectionFactory == null) {
      if (System.getProperty(SHOULD_USE_PROXY_FLAG) != null) {
        return new DefaultConnectionFactory(defaultProxy());
      }
      return new DefaultConnectionFactory();
    }
    return connectionFactory;
  }
}
