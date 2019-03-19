/*
 * Copyright (c) 2010 Google Inc.
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

import com.google.api.client.http.LowLevelHttpResponse;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

final class NetHttpResponse extends LowLevelHttpResponse {

  private final HttpURLConnection connection;
  private final int responseCode;
  private final String responseMessage;
  private final ArrayList<String> headerNames = new ArrayList<String>();
  private final ArrayList<String> headerValues = new ArrayList<String>();

  NetHttpResponse(HttpURLConnection connection) throws IOException {
    this.connection = connection;
    int responseCode = connection.getResponseCode();
    this.responseCode = responseCode == -1 ? 0 : responseCode;
    responseMessage = connection.getResponseMessage();
    List<String> headerNames = this.headerNames;
    List<String> headerValues = this.headerValues;
    for (Map.Entry<String, List<String>> entry : connection.getHeaderFields().entrySet()) {
      String key = entry.getKey();
      if (key != null) {
        for (String value : entry.getValue()) {
          if (value != null) {
            headerNames.add(key);
            headerValues.add(value);
          }
        }
      }
    }
  }

  @Override
  public int getStatusCode() {
    return responseCode;
  }

  /**
   * {@inheritDoc}
   *
   * <p>Returns {@link HttpURLConnection#getInputStream} when it doesn't throw {@link IOException},
   * otherwise it returns {@link HttpURLConnection#getErrorStream}.
   *
   * <p>Upgrade warning: in prior version 1.16 {@link #getContent()} returned {@link
   * HttpURLConnection#getInputStream} only when the status code was successful. Starting with
   * version 1.17 it returns {@link HttpURLConnection#getInputStream} when it doesn't throw {@link
   * IOException}, otherwise it returns {@link HttpURLConnection#getErrorStream}
   *
   * <p>Upgrade warning: in versions prior to 1.20 {@link #getContent()} returned {@link
   * HttpURLConnection#getInputStream()} or {@link HttpURLConnection#getErrorStream()}, both of
   * which silently returned -1 for read() calls when the connection got closed in the middle of
   * receiving a response. This is highly likely a bug from JDK's {@link HttpURLConnection}. Since
   * version 1.20, the bytes read off the wire will be checked and an {@link IOException} will be
   * thrown if the response is not fully delivered when the connection is closed by server for
   * whatever reason, e.g., server restarts. Note though that this is a best-effort check: when the
   * response is chunk encoded, we have to rely on the underlying HTTP library to behave correctly.
   */
  @Override
  public InputStream getContent() throws IOException {
    InputStream in = null;
    try {
      in = connection.getInputStream();
    } catch (IOException ioe) {
      in = connection.getErrorStream();
    }
    return in == null ? null : new SizeValidatingInputStream(in);
  }

  @Override
  public String getContentEncoding() {
    return connection.getContentEncoding();
  }

  @Override
  public long getContentLength() {
    String string = connection.getHeaderField("Content-Length");
    return string == null ? -1 : Long.parseLong(string);
  }

  @Override
  public String getContentType() {
    return connection.getHeaderField("Content-Type");
  }

  @Override
  public String getReasonPhrase() {
    return responseMessage;
  }

  @Override
  public String getStatusLine() {
    String result = connection.getHeaderField(0);
    return result != null && result.startsWith("HTTP/1.") ? result : null;
  }

  @Override
  public int getHeaderCount() {
    return headerNames.size();
  }

  @Override
  public String getHeaderName(int index) {
    return headerNames.get(index);
  }

  @Override
  public String getHeaderValue(int index) {
    return headerValues.get(index);
  }

  /**
   * Closes the connection to the HTTP server.
   *
   * @since 1.4
   */
  @Override
  public void disconnect() {
    connection.disconnect();
  }

  /**
   * A wrapper arround the base {@link InputStream} that validates EOF returned by the read calls.
   *
   * @since 1.20
   */
  private final class SizeValidatingInputStream extends FilterInputStream {

    private long bytesRead = 0;

    public SizeValidatingInputStream(InputStream in) {
      super(in);
    }

    /**
     * java.io.InputStream#read(byte[], int, int) swallows IOException thrown from read() so we have
     * to override it.
     *
     * @see
     *     "http://grepcode.com/file/repository.grepcode.com/java/root/jdk/openjdk/8-b132/java/io/InputStream.java#185"
     */
    @Override
    public int read(byte[] b, int off, int len) throws IOException {
      int n = in.read(b, off, len);
      if (n == -1) {
        throwIfFalseEOF();
      } else {
        bytesRead += n;
      }
      return n;
    }

    @Override
    public int read() throws IOException {
      int n = in.read();
      if (n == -1) {
        throwIfFalseEOF();
      } else {
        bytesRead++;
      }
      return n;
    }

    @Override
    public long skip(long len) throws IOException {
      long n = in.skip(len);
      bytesRead += n;
      return n;
    }

    // Throws an IOException if gets an EOF in the middle of a response.
    private void throwIfFalseEOF() throws IOException {
      long contentLength = getContentLength();
      if (contentLength == -1) {
        // If a Content-Length header is missing, there's nothing we can do.
        return;
      }
      // According to RFC2616, message-body is prohibited in responses to certain requests, e.g.,
      // HEAD. Nevertheless an entity-header (possibly with non-zero Content-Length) may be present.
      // Thus we exclude the case where bytesRead == 0.
      //
      // See http://www.w3.org/Protocols/rfc2616/rfc2616-sec4.html#sec4.4 for details.
      if (bytesRead != 0 && bytesRead < contentLength) {
        throw new IOException(
            "Connection closed prematurely: bytesRead = "
                + bytesRead
                + ", Content-Length = "
                + contentLength);
      }
    }
  }
}
