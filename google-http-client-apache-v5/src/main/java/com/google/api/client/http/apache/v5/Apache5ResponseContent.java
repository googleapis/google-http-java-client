package com.google.api.client.http.apache.v5;

import java.io.IOException;
import java.io.InputStream;
import org.apache.hc.core5.http.ClassicHttpResponse;

/**
 * Class that wraps an {@link org.apache.hc.core5.http.HttpEntity}'s content {@link InputStream}
 * along with the {@link ClassicHttpResponse} that contains this entity. The main purpose is to be
 * able to close the response as well as the content input stream when {@link #close()} is called.
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
  public void reset() throws IOException {
    wrappedStream.reset();
  }

  @Override
  public void mark(int readLimit) {
    wrappedStream.mark(readLimit);
  }

  @Override
  public void close() throws IOException {
    wrappedStream.close();
    response.close();
  }
}
