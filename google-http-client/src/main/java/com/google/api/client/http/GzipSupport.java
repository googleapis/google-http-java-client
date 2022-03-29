package com.google.api.client.http;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.GZIPInputStream;

final class GzipSupport {

  private GzipSupport() {}

  static GZIPInputStream newGzipInputStream(InputStream in) throws IOException {
    return new GZIPInputStream(new OptimisticAvailabilityInputStream(in));
  }

  /**
   * When {@link GZIPInputStream} completes processing an individual member it will call {@link
   * InputStream#available()} to determine if there is more stream to try and process. If the call
   * to {@code available()} returns 0 {@code GZIPInputStream} will determine it has processed the
   * entirety of the underlying stream. This is spurious, as {@link InputStream#available()} is
   * allowed to return 0 if it would require blocking in order for more bytes to be available. When
   * {@code GZIPInputStream} is reading from a {@code Transfer-Encoding: chunked} response, if the
   * chunk boundary happens to align closely enough to the member boundary {@code GZIPInputStream}
   * won't consume the whole response.
   *
   * <p>This class, provides an optimistic "estimate" (in actuality, a lie) of the number of {@code
   * available()} bytes in the underlying stream. It does this by tracking the last number of bytes
   * read. If the last number of bytes read is grater than -1, we return {@link Integer#MAX_VALUE}
   * to any call of {@link #available()}.
   *
   * <p>We're breaking the contract of available() in that we're lying about how much data we have
   * accessible without blocking, however in the case where we're weaving {@link GZIPInputStream}
   * into response processing we already know there are going to be blocking calls to read before
   * the stream is exhausted.
   *
   * <p>This scenario isn't unique to processing of chunked responses, and can be replicated
   * reliably using a {@link java.io.SequenceInputStream} with two underlying {@link
   * java.io.ByteArrayInputStream}. See the corresponding test class for a reproduction.
   *
   * <p>The need for this class has been verified for the following JVMs:
   *
   * <ol>
   *   <li>
   *       <pre>
   * openjdk version "1.8.0_292"
   * OpenJDK Runtime Environment (AdoptOpenJDK)(build 1.8.0_292-b10)
   * OpenJDK 64-Bit Server VM (AdoptOpenJDK)(build 25.292-b10, mixed mode)
   *   </pre>
   *   <li>
   *       <pre>
   * openjdk version "11.0.14.1" 2022-02-08
   * OpenJDK Runtime Environment Temurin-11.0.14.1+1 (build 11.0.14.1+1)
   * OpenJDK 64-Bit Server VM Temurin-11.0.14.1+1 (build 11.0.14.1+1, mixed mode)
   *   </pre>
   *   <li>
   *       <pre>
   * openjdk version "17" 2021-09-14
   * OpenJDK Runtime Environment Temurin-17+35 (build 17+35)
   * OpenJDK 64-Bit Server VM Temurin-17+35 (build 17+35, mixed mode, sharing)
   *   </pre>
   * </ol>
   */
  private static final class OptimisticAvailabilityInputStream extends FilterInputStream {
    private int lastRead = 0;

    OptimisticAvailabilityInputStream(InputStream delegate) {
      super(delegate);
    }

    @Override
    public int available() throws IOException {
      return lastRead > -1 ? Integer.MAX_VALUE : 0;
    }

    @Override
    public int read() throws IOException {
      return lastRead = super.read();
    }

    @Override
    public int read(byte[] b) throws IOException {
      return lastRead = super.read(b);
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
      return lastRead = super.read(b, off, len);
    }
  }
}
