/*
 * Copyright (c) 2018 Google Inc.
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

package com.google.api.client.util;

import com.google.api.client.http.HttpHeaders;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpStatusCodes;
import com.google.common.annotations.VisibleForTesting;

import io.opencensus.contrib.http.util.HttpPropagationUtil;
import io.opencensus.trace.EndSpanOptions;
import io.opencensus.trace.NetworkEvent;
import io.opencensus.trace.NetworkEvent.Type;
import io.opencensus.trace.Span;
import io.opencensus.trace.SpanContext;
import io.opencensus.trace.Status;
import io.opencensus.trace.Tracer;
import io.opencensus.trace.Tracing;
import io.opencensus.trace.propagation.TextFormat;

import java.util.Collections;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Nullable;

/**
 * Utilities for Census monitoring and tracing.
 *
 * @author Hailong Wen
 * @since 1.24
 */
public class OpenCensusUtils {

  private static final Logger LOGGER = Logger.getLogger(OpenCensusUtils.class.getName());

  /**
   * Span name for tracing {@link HttpRequest#execute()}.
   */
  public static final String SPAN_NAME_HTTP_REQUEST_EXECUTE =
      "Sent." + HttpRequest.class.getName() + ".execute";

  /**
   * OpenCensus tracing component. When no OpenCensus implementation is provided, it will return a
   * no-op tracer.
   */
  private static Tracer tracer = Tracing.getTracer();

  /**
   * Whether spans should be recorded locally. Defaults to true.
   */
  private static volatile boolean isRecordEvent = true;

  /**
   * {@link TextFormat} used in tracing context propagation.
   */
  @Nullable
  @VisibleForTesting
  static volatile TextFormat propagationTextFormat = null;

  /**
   * {@link TextFormat.Setter} for {@link #propagationTextFormat}.
   */
  @Nullable
  @VisibleForTesting
  static volatile TextFormat.Setter propagationTextFormatSetter = null;

  /**
   * Sets the {@link TextFormat} used in context propagation.
   *
   * <p>This API allows users of google-http-client to specify other text format, or disable context
   * propagation by setting it to {@code null}. It should be used along with {@link
   * #setPropagationTextFormatSetter} for setting purpose. </p>
   *
   * @param textFormat the text format.
   */
  public static void setPropagationTextFormat(@Nullable TextFormat textFormat) {
    propagationTextFormat = textFormat;
  }

  /**
   * Sets the {@link TextFormat.Setter} used in context propagation.
   *
   * <p>This API allows users of google-http-client to specify other text format setter, or disable
   * context propagation by setting it to {@code null}. It should be used along with {@link
   * #setPropagationTextFormat} for setting purpose. </p>
   *
   * @param textFormatSetter the {@code TextFormat.Setter} for the text format.
   */
  public static void setPropagationTextFormatSetter(@Nullable TextFormat.Setter textFormatSetter) {
    propagationTextFormatSetter = textFormatSetter;
  }

  /**
   * Sets whether spans should be recorded locally.
   *
   * <p> This API allows users of google-http-client to turn on/off local span collection. </p>
   *
   * @param recordEvent record span locally if true.
   */
  public static void setIsRecordEvent(boolean recordEvent) {
    isRecordEvent = recordEvent;
  }

  /**
   * Returns the tracing component of OpenCensus.
   *
   * @return the tracing component of OpenCensus.
   */
  public static Tracer getTracer() {
    return tracer;
  }

  /**
   * Returns whether spans should be recorded locally.
   *
   * @return whether spans should be recorded locally.
   */
  public static boolean isRecordEvent() {
    return isRecordEvent;
  }

  /**
   * Propagate information of a given tracing context. This information will be injected into HTTP
   * header.
   *
   * @param spanContext the spanContext to be propagated.
   * @param headers the headers used in propagation.
   */
  public static void propagateTracingContext(SpanContext spanContext, HttpHeaders headers) {
    Preconditions.checkArgument(spanContext != null, "spanContext should not be null.");
    Preconditions.checkArgument(headers != null, "headers should not be null.");
    if (propagationTextFormat != null && propagationTextFormatSetter != null) {
      if (!spanContext.equals(SpanContext.INVALID)) {
        propagationTextFormat.inject(spanContext, headers, propagationTextFormatSetter);
      }
    }
  }

  /**
   * Returns an {@link EndSpanOptions} to end a http span according to the status code.
   *
   * @param statusCode the status code, can be null to represent no valid response is returned.
   * @return an {@code EndSpanOptions} that best suits the status code.
   */
  public static EndSpanOptions getEndSpanOptions(@Nullable Integer statusCode) {
    // Always sample the span, but optionally export it.
    EndSpanOptions.Builder builder = EndSpanOptions.builder();
    if (statusCode == null) {
      builder.setStatus(Status.UNKNOWN);
    } else if (!HttpStatusCodes.isSuccess(statusCode)) {
      switch (statusCode) {
        case HttpStatusCodes.STATUS_CODE_BAD_REQUEST:
          builder.setStatus(Status.INVALID_ARGUMENT);
          break;
        case HttpStatusCodes.STATUS_CODE_UNAUTHORIZED:
          builder.setStatus(Status.UNAUTHENTICATED);
          break;
        case HttpStatusCodes.STATUS_CODE_FORBIDDEN:
          builder.setStatus(Status.PERMISSION_DENIED);
          break;
        case HttpStatusCodes.STATUS_CODE_NOT_FOUND:
          builder.setStatus(Status.NOT_FOUND);
          break;
        case HttpStatusCodes.STATUS_CODE_PRECONDITION_FAILED:
          builder.setStatus(Status.FAILED_PRECONDITION);
          break;
        case HttpStatusCodes.STATUS_CODE_SERVER_ERROR:
          builder.setStatus(Status.UNAVAILABLE);
          break;
        default:
          builder.setStatus(Status.UNKNOWN);
      }
    } else {
      builder.setStatus(Status.OK);
    }
    return builder.build();
  }

  /**
   * Records a new message event which contains the size of the request content. Note that the size
   * represents the message size in application layer, i.e., content-length.
   *
   * @param span The {@code span} in which the send event occurs.
   * @param id The id for the message, It is unique within an {@link HttpRequest}.
   * @param size Size of the request.
   */
  public static void recordSentMessageEvent(Span span, long id, long size) {
    recordMessageEvent(span, id, size, Type.SENT);
  }

  /**
   * Records a new message event which contains the size of the response content. Note that the size
   * represents the message size in application layer, i.e., content-length.
   *
   * @param span The {@code span} in which the receive event occurs.
   * @param id The id for the message. It is unique within an {@link HttpRequest}.
   * @param size Size of the response.
   */
  public static void recordReceivedMessageEvent(Span span, long id, long size) {
    recordMessageEvent(span, id, size, Type.RECV);
  }

  /**
   * Records a message event of a certain {@link NetowrkEvent.Type}. This method is package
   * protected since {@link NetworkEvent} might be deprecated in future releases.
   *
   * @param span The {@code span} in which the event occurs.
   * @param id The id for the message.
   * @param size Size of the message.
   * @param eventType The {@code NetworkEvent.Type} of the message event.
   */
  @VisibleForTesting
  static void recordMessageEvent(Span span, long id, long size, Type eventType) {
    Preconditions.checkArgument(span != null, "span should not be null.");
    if (size < 0) {
      size = 0;
    }
    NetworkEvent event = NetworkEvent
        .builder(eventType, id)
        .setUncompressedMessageSize(size)
        .build();
    span.addNetworkEvent(event);
  }

  static {
    try {
      propagationTextFormat = HttpPropagationUtil.getCloudTraceFormat();
      propagationTextFormatSetter = new TextFormat.Setter<HttpHeaders>() {
        @Override
        public void put(HttpHeaders carrier, String key, String value) {
          carrier.set(key, value);
        }
      };
    } catch (Exception e) {
      LOGGER.log(
          Level.WARNING, "Cannot initialize default OpenCensus HTTP propagation text format.", e);
    }

    try {
      Tracing.getExportComponent().getSampledSpanStore().registerSpanNamesForCollection(
          Collections.<String>singletonList(SPAN_NAME_HTTP_REQUEST_EXECUTE));
    } catch (Exception e) {
      LOGGER.log(
          Level.WARNING, "Cannot register default OpenCensus span names for collection.", e);
    }
  }

  private OpenCensusUtils() {}
}
