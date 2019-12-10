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

import com.google.api.client.http.LowLevelHttpRequest;
import com.google.api.client.http.LowLevelHttpResponse;
import com.google.api.client.util.Preconditions;
import com.google.api.client.util.StreamingContent;
import com.google.common.annotations.VisibleForTesting;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/** @author Yaniv Inbar */
final class NetHttpRequest extends LowLevelHttpRequest {

  private final HttpURLConnection connection;
  private int writeTimeout;

  /** @param connection HTTP URL connection */
  NetHttpRequest(HttpURLConnection connection) {
    this.connection = connection;
    this.writeTimeout = 0;
    connection.setInstanceFollowRedirects(false);
  }

  @Override
  public void addHeader(String name, String value) {
    connection.addRequestProperty(name, value);
  }

  @VisibleForTesting
  String getRequestProperty(String name) {
    return connection.getRequestProperty(name);
  }

  @Override
  public void setTimeout(int connectTimeout, int readTimeout) {
    connection.setReadTimeout(readTimeout);
    connection.setConnectTimeout(connectTimeout);
  }

  @Override
  public void setWriteTimeout(int writeTimeout) throws IOException {
    this.writeTimeout = writeTimeout;
  }

  interface OutputWriter {
    void write(OutputStream outputStream, StreamingContent content) throws IOException;
  }

  static class DefaultOutputWriter implements OutputWriter {
    @Override
    public void write(OutputStream outputStream, final StreamingContent content)
        throws IOException {
      content.writeTo(outputStream);
    }
  }

  private static final OutputWriter DEFAULT_CONNECTION_WRITER = new DefaultOutputWriter();

  @Override
  public LowLevelHttpResponse execute() throws IOException {
    return execute(DEFAULT_CONNECTION_WRITER);
  }

  @VisibleForTesting
  LowLevelHttpResponse execute(final OutputWriter outputWriter) throws IOException {
    HttpURLConnection connection = this.connection;
    // write content
    if (getStreamingContent() != null) {
      String contentType = getContentType();
      if (contentType != null) {
        addHeader("Content-Type", contentType);
      }
      String contentEncoding = getContentEncoding();
      if (contentEncoding != null) {
        addHeader("Content-Encoding", contentEncoding);
      }
      long contentLength = getContentLength();
      if (contentLength >= 0) {
        connection.setRequestProperty("Content-Length", Long.toString(contentLength));
      }
      String requestMethod = connection.getRequestMethod();
      if ("POST".equals(requestMethod) || "PUT".equals(requestMethod)) {
        connection.setDoOutput(true);
        // see http://developer.android.com/reference/java/net/HttpURLConnection.html
        if (contentLength >= 0 && contentLength <= Integer.MAX_VALUE) {
          connection.setFixedLengthStreamingMode((int) contentLength);
        } else {
          connection.setChunkedStreamingMode(0);
        }
        final OutputStream out = connection.getOutputStream();

        boolean threw = true;
        try {
          writeContentToOutputStream(outputWriter, out);

          threw = false;
        } catch (IOException e) {
          // If we've gotten a response back, continue on and try to parse the response. Otherwise,
          // re-throw the IOException
          if (!hasResponse(connection)) {
            throw e;
          }
        } finally {
          try {
            out.close();
          } catch (IOException exception) {
            // When writeTo() throws an exception, chances are that the close call will also fail.
            // In such case, swallow exception from close call so that the underlying cause can
            // propagate.
            if (!threw) {
              throw exception;
            }
          }
        }
      } else {
        // cannot call setDoOutput(true) because it would change a GET method to POST
        // for HEAD, OPTIONS, DELETE, or TRACE it would throw an exceptions
        Preconditions.checkArgument(
            contentLength == 0, "%s with non-zero content length is not supported", requestMethod);
      }
    }
    // connect
    boolean successfulConnection = false;
    try {
      connection.connect();
      NetHttpResponse response = new NetHttpResponse(connection);
      successfulConnection = true;
      return response;
    } finally {
      if (!successfulConnection) {
        connection.disconnect();
      }
    }
  }

  private boolean hasResponse(HttpURLConnection connection) {
    try {
      return connection.getResponseCode() > 0;
    } catch (IOException e) {
      // There's some exception trying to parse the response
      return false;
    }
  }

  private void writeContentToOutputStream(final OutputWriter outputWriter, final OutputStream out)
      throws IOException {
    if (writeTimeout == 0) {
      outputWriter.write(out, getStreamingContent());
    } else {
      // do it with timeout
      final StreamingContent content = getStreamingContent();
      final Callable<Boolean> writeContent =
          new Callable<Boolean>() {
            @Override
            public Boolean call() throws IOException {
              outputWriter.write(out, content);
              return Boolean.TRUE;
            }
          };

      final ExecutorService executor = Executors.newSingleThreadExecutor();
      final Future<Boolean> future = executor.submit(new FutureTask<Boolean>(writeContent), null);
      executor.shutdown();

      try {
        future.get(writeTimeout, TimeUnit.MILLISECONDS);
      } catch (InterruptedException e) {
        throw new IOException("Socket write interrupted", e);
      } catch (ExecutionException e) {
        throw new IOException("Exception in socket write", e);
      } catch (TimeoutException e) {
        throw new IOException("Socket write timed out", e);
      }
      if (!executor.isTerminated()) {
        executor.shutdown();
      }
    }
  }
}
