package com.google.api.client.http;

import com.google.common.base.Preconditions;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

/**
 * Serializes MIME Multipart/Related content as specified by <a
 * href="http://tools.ietf.org/html/rfc2387">RFC 2387: The MIME Multipart/Related Content-type</a>.
 * <p>
 * Limitations:
 * <ul>
 * <li>No support of parameters other than {@code "boundary"}</li>
 * <li>No support for specifying headers for each content part</li>
 * <li>The content type of each part is required, so {@link HttpContent#getType()} must not be
 * {@code null}</li>
 * </ul>
 * </p>
 * <p>
 * Use {@link #forRequest(HttpRequest, HttpContent...)} to construct. For example:
 *
 * <pre><code>
  static void setMediaWithMetadataContent(
      HttpRequest request, AtomContent atomContent, InputStreamContent imageContent) {
    MultipartRelatedContent.forRequest(request, atomContent, imageContent);
  }
 * </code></pre>
 *
 * <p>
 * Implementation is not thread-safe.
 * </p>
 *
 * @since 1.1
 * @author Yaniv Inbar
 */
public final class MultipartRelatedContent implements HttpContent {

  /**
   * Boundary string to use. By default, it is {@code "END_OF_PART"}.
   *
   * @deprecated (scheduled to be made private in 1.6) Use {@link #getBoundary} or
   *             {@link #setBoundary}
   */
  @Deprecated
  public String boundary = "END_OF_PART";

  /**
   * Collection of HTTP content parts.
   * <p>
   * By default, it is an empty list.
   * </p>
   *
   * @deprecated (scheduled to be made private in 1.6) Use {@link #getParts} or {@link #setParts}
   */
  @Deprecated
  public Collection<HttpContent> parts = new ArrayList<HttpContent>();

  private static final byte[] CR_LF = "\r\n".getBytes();
  private static final byte[] CONTENT_TYPE = "Content-Type: ".getBytes();
  private static final byte[] CONTENT_TRANSFER_ENCODING =
      "Content-Transfer-Encoding: binary".getBytes();
  private static final byte[] TWO_DASHES = "--".getBytes();

  /**
   * @deprecated (scheduled to be made private in 1.6) Use
   *             {@link #forRequest(HttpRequest, HttpContent...)}
   */
  @Deprecated
  public MultipartRelatedContent() {
  }

  /**
   * Returns a new multi-part content serializer as the content for the given HTTP request.
   *
   * <p>
   * It also sets the {@link HttpHeaders#getMimeVersion MIME version} of
   * {@link HttpRequest#getHeaders() headers} to {@code "1.0"}.
   * </p>
   *
   * @param request HTTP request
   * @param parts HTTP content parts
   * @return new multi-part content serializer
   */
  public static MultipartRelatedContent forRequest(HttpRequest request, HttpContent... parts) {
    MultipartRelatedContent result = new MultipartRelatedContent();
    request.getHeaders().setMimeVersion("1.0");
    request.setContent(result);
    result.getParts().addAll(Arrays.asList(parts));
    return result;
  }

  public void writeTo(OutputStream out) throws IOException {
    byte[] END_OF_PART = boundary.getBytes();
    out.write(TWO_DASHES);
    out.write(END_OF_PART);
    for (HttpContent part : parts) {
      String contentType = part.getType();
      byte[] typeBytes = contentType.getBytes();
      out.write(CR_LF);
      out.write(CONTENT_TYPE);
      out.write(typeBytes);
      out.write(CR_LF);
      if (!LogContent.isTextBasedContentType(contentType)) {
        out.write(CONTENT_TRANSFER_ENCODING);
        out.write(CR_LF);
      }
      out.write(CR_LF);
      part.writeTo(out);
      out.write(CR_LF);
      out.write(TWO_DASHES);
      out.write(END_OF_PART);
    }
    out.write(TWO_DASHES);
    out.flush();
  }

  public String getEncoding() {
    return null;
  }

  public long getLength() {
    // TODO(yanivi): compute this?
    return -1;
  }

  public String getType() {
    return "multipart/related; boundary=\"" + getBoundary() + "\"";
  }

  public boolean retrySupported() {
    for (HttpContent onePart : parts) {
      if (!onePart.retrySupported()) {
        return false;
      }
    }
    return true;
  }

  /**
   * Returns the boundary string to use.
   *
   * @since 1.5
   */
  public String getBoundary() {
    return boundary;
  }

  /**
   * Sets the boundary string to use.
   *
   * <p>
   * Defaults to {@code "END_OF_PART"}.
   * </p>
   *
   * @since 1.5
   */
  public MultipartRelatedContent setBoundary(String boundary) {
    this.boundary = Preconditions.checkNotNull(boundary);
    return this;
  }

  /**
   * Returns the HTTP content parts.
   *
   * @since 1.5
   */
  public Collection<HttpContent> getParts() {
    return parts;
  }

  /**
   * Sets the HTTP content parts.
   *
   * <p>
   * By default, it is an empty list.
   * </p>
   *
   * @since 1.5
   */
  public MultipartRelatedContent setParts(Collection<HttpContent> parts) {
    this.parts = Preconditions.checkNotNull(parts);
    return this;
  }
}
