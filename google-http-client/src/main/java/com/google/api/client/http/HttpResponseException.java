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

package com.google.api.client.http;

import com.google.api.client.util.Preconditions;
import com.google.api.client.util.StringUtils;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.io.ByteStreams;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;

/**
 * Exception thrown when an error status code is detected in an HTTP response.
 *
 * <p>
 * Implementation is not thread safe.
 * </p>
 *
 * @since 1.0
 * @author Yaniv Inbar
 */
public class HttpResponseException extends IOException {

  private static final long serialVersionUID = -1875819453475890043L;

  /** HTTP status code. */
  private final int statusCode;

  /** Status message or {@code null}. */
  private final String statusMessage;

  /** HTTP headers. */
  private final transient HttpHeaders headers;

  /** HTTP response content or {@code null}. for none. */
  private final byte[] content;

  /** HTTP response charset or {@code null} for none. */
  private final Charset charset;

  /**
   * Constructor that constructs a detail message from the given HTTP response that includes the
   * status code, status message and HTTP response content.
   *
   * <p>
   * Callers of this constructor should call {@link HttpResponse#disconnect} after
   * {@link HttpResponseException} is instantiated. Example usage:
   * </p>
   *
   * <pre>
     try {
       throw new HttpResponseException(response);
     } finally {
       response.disconnect();
     }
   * </pre>
   *
   * @param response HTTP response
   */
  public HttpResponseException(HttpResponse response) {
    this(new Builder(response));
  }

  /**
   * @param builder builder
   * @since 1.14
   */
  protected HttpResponseException(Builder builder) {
    super(builder.message);
    statusCode = builder.statusCode;
    statusMessage = builder.statusMessage;
    headers = builder.headers;
    content = builder.content;
    charset = builder.charset;
  }

  /**
   * Returns whether received a successful HTTP status code {@code >= 200 && < 300} (see
   * {@link #getStatusCode()}).
   *
   * @since 1.7
   */
  public final boolean isSuccessStatusCode() {
    return HttpStatusCodes.isSuccess(statusCode);
  }

  /**
   * Returns the HTTP status code or {@code 0} for none.
   *
   * @since 1.7
   */
  public final int getStatusCode() {
    return statusCode;
  }

  /**
   * Returns the HTTP status message or {@code null} for none.
   *
   * @since 1.9
   */
  public final String getStatusMessage() {
    return statusMessage;
  }

  /**
   * Returns the HTTP response headers.
   *
   * @since 1.7
   */
  public HttpHeaders getHeaders() {
    return headers;
  }

  /**
   * Returns the HTTP response content or {@code null} for none.
   *
   * @since 1.14
   */
  public final String getContent() {
    if (content == null) {
      return null;
    }
    try {
      return new String(content, charset.name());
    } catch (UnsupportedEncodingException exception) {
      // it would be bad to throw an exception while throwing an exception
    }
    return null;
  }

  /**
   * Returns the HTTP response or {@code null} for none as raw bytes.
   *
   * @since 1.28
   */
  public final byte[] getRawContent() {
    return content;
  }

  /**
   * Builder.
   *
   * <p>
   * Implementation is not thread safe.
   * </p>
   *
   *
   * @since 1.14
   */
  public static class Builder {

    /** HTTP status code. */
    int statusCode;

    /** Status message or {@code null}. */
    String statusMessage;

    /** HTTP headers. */
    HttpHeaders headers;

    /** Response content or {@code null} for none. */
    byte[] content;

    /** Detail message to use or {@code null} for none. */
    String message;

    /** Response charset or {@code null} for none. */
    Charset charset;

    /**
     * @param statusCode HTTP status code
     * @param statusMessage status message or {@code null}
     * @param headers HTTP headers
     */
    public Builder(int statusCode, String statusMessage, HttpHeaders headers) {
      setStatusCode(statusCode);
      setStatusMessage(statusMessage);
      setHeaders(headers);
    }

    /**
     * @param response HTTP response
     */
    public Builder(HttpResponse response) {
      this(response.getStatusCode(), response.getStatusMessage(), response.getHeaders());
      // content
      try {
        // reads the stream to make sure that connection goes back to the pool regardless of whether
        // user reads the stream or not
        InputStream inputStream = response.getContent();
        if (inputStream != null) {
          content = ByteStreams.toByteArray(inputStream);
        }
      } catch (IOException exception) {
        // it would be bad to throw an exception while throwing an exception
        exception.printStackTrace();
      } catch (IllegalArgumentException exception) {
        exception.printStackTrace();
      }
      // message
      StringBuilder builder = computeMessageBuffer(response);
      if (content != null) {
        try {
          // charset
          charset = response.getContentCharset();
          String strContent = getContent();
          if (strContent != null) {
            builder.append(StringUtils.LINE_SEPARATOR).append(strContent);
          }
        } catch (IOException exception) {
          // it would be bad to throw an exception while throwing an exception
          builder
              .append(StringUtils.LINE_SEPARATOR)
              .append("Content could not be retrieved")
              .append(StringUtils.LINE_SEPARATOR)
              .append(exception.getMessage());
        } catch (IllegalArgumentException exception) {
          builder
              .append(StringUtils.LINE_SEPARATOR)
              .append("Content could not be retrieved")
              .append(StringUtils.LINE_SEPARATOR)
              .append(exception.getMessage());
        }
      }
      message = builder.toString();
    }

    /** Returns the detail message to use or {@code null} for none. */
    public final String getMessage() {
      return message;
    }

    /**
     * Sets the detail message to use or {@code null} for none.
     *
     * <p>
     * Overriding is only supported for the purpose of calling the super implementation and changing
     * the return type, but nothing else.
     * </p>
     */
    public Builder setMessage(String message) {
      this.message = message;
      return this;
    }

    /** Returns the HTTP status code or {@code 0} for none. */
    public final int getStatusCode() {
      return statusCode;
    }

    /**
     * Sets the HTTP status code or {@code 0} for none.
     *
     * <p>
     * Overriding is only supported for the purpose of calling the super implementation and changing
     * the return type, but nothing else.
     * </p>
     */
    public Builder setStatusCode(int statusCode) {
      Preconditions.checkArgument(statusCode >= 0);
      this.statusCode = statusCode;
      return this;
    }

    /** Returns the HTTP status message or {@code null} for none. */
    public final String getStatusMessage() {
      return statusMessage;
    }

    /**
     * Sets the HTTP status message or {@code null} for none.
     *
     * <p>
     * Overriding is only supported for the purpose of calling the super implementation and changing
     * the return type, but nothing else.
     * </p>
     */
    public Builder setStatusMessage(String statusMessage) {
      this.statusMessage = statusMessage;
      return this;
    }

    /** Returns the HTTP response headers. */
    public HttpHeaders getHeaders() {
      return headers;
    }

    /**
     * Sets the HTTP response headers.
     *
     * <p>Overriding is only supported for the purpose of calling the super implementation and
     * changing the return type, but nothing else.
     */
    public Builder setHeaders(HttpHeaders headers) {
      this.headers = Preconditions.checkNotNull(headers);
      return this;
    }

    /** Returns the HTTP response content or {@code null} for none. */
    public final String getContent() throws IOException {
      if (content == null) {
        return null;
      }
      return new String(content, charset.name());
    }

    /**
     * Sets the HTTP response content or {@code null} for none.
     *
     * <p>Overriding is only supported for the purpose of calling the super implementation and
     * changing the return type, but nothing else.
     */
    public Builder setContent(String content) {
      if (content != null) {
        this.content = content.getBytes();
      }
      return this;
    }

    /**
     * Sets the HTTP response charset or {@code null} for none.
     *
     * <p>Overriding is only supported for the purpose of calling the super implementation and
     * changing the return type, but nothing else.
     */
    @VisibleForTesting
    Builder setCharset(Charset charset) {
      if (charset != null) {
        this.charset = charset;
      }
      return this;
    }

    /** Returns a new instance of {@link HttpResponseException} based on this builder. */
    public HttpResponseException build() {
      return new HttpResponseException(this);
    }
  }

  /**
   * Returns an exception message string builder to use for the given HTTP response.
   *
   * @since 1.7
   */
  public static StringBuilder computeMessageBuffer(HttpResponse response) {
    StringBuilder builder = new StringBuilder();
    int statusCode = response.getStatusCode();
    if (statusCode != 0) {
      builder.append(statusCode);
    }
    String statusMessage = response.getStatusMessage();
    if (statusMessage != null) {
      if (statusCode != 0) {
        builder.append(' ');
      }
      builder.append(statusMessage);
    }
    return builder;
  }
}
