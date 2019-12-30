/*
 * Copyright (c) 2013 Google Inc.
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

package com.google.api.client.http;

import com.google.api.client.util.Preconditions;
import com.google.api.client.util.StreamingContent;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.UUID;

/**
 * Serializes MIME multipart content as specified by <a
 * href="http://tools.ietf.org/html/rfc2387">RFC 2387: The MIME Multipart/Related Content-type</a>
 * and <a href="http://tools.ietf.org/html/rfc1521#section-7.2.2">RFC 2046: Multipurpose Internet
 * Mail Extensions: The Multipart/mixed (primary) subtype</a>.
 *
 * <p>By default the media type is {@code "multipart/related; boundary=__END_OF_PART__<random UUID>__"}, but this
 * may be customized by calling {@link #setMediaType(HttpMediaType)}, {@link #getMediaType()}, or
 * {@link #setBoundary(String)}.
 *
 * <p>Implementation is not thread-safe.
 *
 * @since 1.14
 * @author Yaniv Inbar
 */
public class MultipartContent extends AbstractHttpContent {

  static final String NEWLINE = "\r\n";

  private static final String TWO_DASHES = "--";

  /** Parts of the HTTP multipart request. */
  private ArrayList<Part> parts = new ArrayList<>();

  public MultipartContent() {
    this("__END_OF_PART__" + UUID.randomUUID().toString() + "__");
  }

  public MultipartContent(String boundary) {
    super(new HttpMediaType("multipart/related").setParameter("boundary", boundary));
  }

  public void writeTo(OutputStream out) throws IOException {
    Writer writer = new OutputStreamWriter(out, getCharset());
    String boundary = getBoundary();
    for (Part part : parts) {
      HttpHeaders headers = new HttpHeaders().setAcceptEncoding(null);
      if (part.headers != null) {
        headers.fromHttpHeaders(part.headers);
      }
      headers
          .setContentEncoding(null)
          .setUserAgent(null)
          .setContentType(null)
          .setContentLength(null)
          .set("Content-Transfer-Encoding", null);
      // analyze the content
      HttpContent content = part.content;
      StreamingContent streamingContent = null;
      if (content != null) {
        headers.set("Content-Transfer-Encoding", Arrays.asList("binary"));
        headers.setContentType(content.getType());
        HttpEncoding encoding = part.encoding;
        long contentLength;
        if (encoding == null) {
          contentLength = content.getLength();
          streamingContent = content;
        } else {
          headers.setContentEncoding(encoding.getName());
          streamingContent = new HttpEncodingStreamingContent(content, encoding);
          contentLength = AbstractHttpContent.computeLength(content);
        }
        if (contentLength != -1) {
          headers.setContentLength(contentLength);
        }
      }
      // write multipart-body from RFC 1521 §7.2.1
      // write encapsulation
      // write delimiter
      writer.write(TWO_DASHES);
      writer.write(boundary);
      writer.write(NEWLINE);
      // write body-part; message from RFC 822 §4.1
      // write message fields
      HttpHeaders.serializeHeadersForMultipartRequests(headers, null, null, writer);
      if (streamingContent != null) {
        writer.write(NEWLINE);
        writer.flush();
        // write message text/body
        streamingContent.writeTo(out);
      }
      // terminate encapsulation
      writer.write(NEWLINE);
    }
    // write close-delimiter
    writer.write(TWO_DASHES);
    writer.write(boundary);
    writer.write(TWO_DASHES);
    writer.write(NEWLINE);
    writer.flush();
  }

  @Override
  public boolean retrySupported() {
    for (Part part : parts) {
      if (!part.content.retrySupported()) {
        return false;
      }
    }
    return true;
  }

  @Override
  public MultipartContent setMediaType(HttpMediaType mediaType) {
    super.setMediaType(mediaType);
    return this;
  }

  /** Returns an unmodifiable view of the parts of the HTTP multipart request. */
  public final Collection<Part> getParts() {
    return Collections.unmodifiableCollection(parts);
  }

  /**
   * Adds an HTTP multipart part.
   *
   * <p>Overriding is only supported for the purpose of calling the super implementation and
   * changing the return type, but nothing else.
   */
  public MultipartContent addPart(Part part) {
    parts.add(Preconditions.checkNotNull(part));
    return this;
  }

  /**
   * Sets the parts of the HTTP multipart request.
   *
   * <p>Overriding is only supported for the purpose of calling the super implementation and
   * changing the return type, but nothing else.
   */
  public MultipartContent setParts(Collection<Part> parts) {
    this.parts = new ArrayList<>(parts);
    return this;
  }

  /**
   * Sets the HTTP content parts of the HTTP multipart request, where each part is assumed to have
   * no HTTP headers and no encoding.
   *
   * <p>Overriding is only supported for the purpose of calling the super implementation and
   * changing the return type, but nothing else.
   */
  public MultipartContent setContentParts(Collection<? extends HttpContent> contentParts) {
    this.parts = new ArrayList<>(contentParts.size());
    for (HttpContent contentPart : contentParts) {
      addPart(new Part(contentPart));
    }
    return this;
  }

  /** Returns the boundary string to use. */
  public final String getBoundary() {
    return getMediaType().getParameter("boundary");
  }

  /**
   * Sets the boundary string to use.
   *
   * <p>Defaults to {@code "END_OF_PART"}.
   *
   * <p>Overriding is only supported for the purpose of calling the super implementation and
   * changing the return type, but nothing else.
   */
  public MultipartContent setBoundary(String boundary) {
    getMediaType().setParameter("boundary", Preconditions.checkNotNull(boundary));
    return this;
  }

  /**
   * Single part of a multi-part request.
   *
   * <p>Implementation is not thread-safe.
   */
  public static final class Part {

    /** HTTP content or {@code null} for none. */
    HttpContent content;

    /** HTTP headers or {@code null} for none. */
    HttpHeaders headers;

    /** HTTP encoding or {@code null} for none. */
    HttpEncoding encoding;

    public Part() {
      this(null);
    }

    /** @param content HTTP content or {@code null} for none */
    public Part(HttpContent content) {
      this(null, content);
    }

    /**
     * @param headers HTTP headers or {@code null} for none
     * @param content HTTP content or {@code null} for none
     */
    public Part(HttpHeaders headers, HttpContent content) {
      setHeaders(headers);
      setContent(content);
    }

    /** Sets the HTTP content or {@code null} for none. */
    public Part setContent(HttpContent content) {
      this.content = content;
      return this;
    }

    /** Returns the HTTP content or {@code null} for none. */
    public HttpContent getContent() {
      return content;
    }

    /** Sets the HTTP headers or {@code null} for none. */
    public Part setHeaders(HttpHeaders headers) {
      this.headers = headers;
      return this;
    }

    /** Returns the HTTP headers or {@code null} for none. */
    public HttpHeaders getHeaders() {
      return headers;
    }

    /** Sets the HTTP encoding or {@code null} for none. */
    public Part setEncoding(HttpEncoding encoding) {
      this.encoding = encoding;
      return this;
    }

    /** Returns the HTTP encoding or {@code null} for none. */
    public HttpEncoding getEncoding() {
      return encoding;
    }
  }
}
