package com.google.api.client.http.javanet;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

/** Given a {@link URL} instance, produces an {@link HttpURLConnection}. */
public interface ConnectionFactory {

  /**
   * Creates a new {@link HttpURLConnection} from the given {@code url}.
   *
   * @param url the URL to which the connection will be made
   * @return the created connection object, which will still be in the pre-connected state
   * @throws IOException if there was a problem producing the connection
   * @throws ClassCastException if the URL is not for an HTTP endpoint
   */
  HttpURLConnection openConnection(URL url) throws IOException, ClassCastException;
}
