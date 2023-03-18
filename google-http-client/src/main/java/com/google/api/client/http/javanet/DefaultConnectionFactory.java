package com.google.api.client.http.javanet;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.Proxy;
import java.net.ProxySelector;
import java.net.URISyntaxException;
import java.net.URL;

/**
 * Default implementation of {@link ConnectionFactory}, which simply attempts to open the connection
 * with an optional {@link Proxy}.
 */
public class DefaultConnectionFactory implements ConnectionFactory {

  private final Proxy proxy;
  private final ProxySelector proxySelector;

  public DefaultConnectionFactory() {
    this((Proxy)null);
  }

  /**
   * @param proxy HTTP proxy or {@code null} to use the proxy settings from <a
   *     href="http://docs.oracle.com/javase/7/docs/api/java/net/doc-files/net-properties.html">
   *     system properties</a>
   */
  public DefaultConnectionFactory(Proxy proxy) {
    this(proxy, null);
  }

  public DefaultConnectionFactory(ProxySelector proxySelector) {
    this(null, proxySelector);
  }

  public DefaultConnectionFactory(Proxy proxy, ProxySelector proxySelector) {
    this.proxy = proxy;
    this.proxySelector = proxySelector;
  }

  @Override
  public HttpURLConnection openConnection(URL url) throws IOException {
    Proxy proxy = this.proxy;
    if(proxySelector != null) {
      try {
        proxy = proxySelector.select(url.toURI()).get(0);
      } catch (URISyntaxException e) {
      }
    }
    return (HttpURLConnection) (proxy == null ? url.openConnection() : url.openConnection(proxy));
  }
}
