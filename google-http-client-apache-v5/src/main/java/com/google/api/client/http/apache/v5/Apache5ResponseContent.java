package com.google.api.client.http.apache.v5;

import com.google.common.annotations.VisibleForTesting;
import java.io.IOException;
import java.io.InputStream;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.HttpResponse;

/**
 * Class that wraps an {@link org.apache.hc.core5.http.HttpEntity}'s content {@link InputStream}
 * along with the {@link ClassicHttpResponse} that contains this entity. The main purpose is to be
 * able to close the response as well as the content input stream when {@link #close()} is called,
 * in order to not break the existing contract with clients using apache v4 that only required them
 * to close the input stream to clean up all resources.
 */
public class Apache5ResponseContent extends InputStream {
  private final ClassicHttpResponse response;
  private final InputStream wrappedStream;

  public Apache5ResponseContent(InputStream wrappedStream, ClassicHttpResponse response) {
    this.response = response;
    this.wrappedStream = wrappedStream;
  }

  @Override
  public int read() throws IOException {
    return wrappedStream.read();
  }

  @Override
  public int read(byte b[]) throws IOException {
    return wrappedStream.read(b);
  }

  @Override
  public int read(byte b[], int off, int len) throws IOException {
    return wrappedStream.read(b, off, len);
  }

  @Override
  public long skip(long n) throws IOException {
    return wrappedStream.skip(n);
  }

  @Override
  public int available() throws IOException {
    return wrappedStream.available();
  }

  @Override
  public synchronized void mark(int readlimit) {
    wrappedStream.mark(readlimit);
  }

  @Override
  public synchronized void reset() throws IOException {
    wrappedStream.reset();
  }

  @Override
  public void close() throws IOException {
    if (wrappedStream != null) {
      wrappedStream.close();
    }
    if (response != null) {
      response.close();
    }
  }

  @Override
  public boolean markSupported() {
    return wrappedStream.markSupported();
  }

  @VisibleForTesting
  HttpResponse getResponse() {
    return response;
  }
}
