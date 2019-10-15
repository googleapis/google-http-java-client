/**
 * Copyright 2019 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.api.client.http;

import io.opencensus.contrib.http.util.HttpTraceAttributeConstants;
import io.opencensus.trace.AttributeValue;
import javax.annotation.Nullable;

/**
 * A thin wrapper around an OpenCensus Span with some convenience methods.
 */
final class OpenCensusSpan implements Span {
  private final io.opencensus.trace.Span span;

  OpenCensusSpan() {
    this.span = OpenCensusUtils.getTracer()
        .spanBuilder(OpenCensusUtils.SPAN_NAME_HTTP_REQUEST_EXECUTE)
        .setRecordEvents(OpenCensusUtils.isRecordEvent())
        .startSpan();
  }

  @Override
  public final void addCommonHTTPAttributes(String requestMethod, String host, String path, String url) {
    addSpanAttribute(HttpTraceAttributeConstants.HTTP_METHOD, requestMethod);
    addSpanAttribute(HttpTraceAttributeConstants.HTTP_HOST, host);
    addSpanAttribute(HttpTraceAttributeConstants.HTTP_PATH, path);
    addSpanAttribute(HttpTraceAttributeConstants.HTTP_URL, url);
  }

  @Override
  public final void addUserAgent(String value) {
    addSpanAttribute(HttpTraceAttributeConstants.HTTP_USER_AGENT, value);
  }

  @Override
  public final void addAnnotation(String description) {
    span.addAnnotation(description);
  }

  @Override
  public final void addHeaders(HttpHeaders headers) {
    OpenCensusUtils.propagateTracingContext(span, headers);
  }

  @Override
  public final void end(@Nullable Integer statusCode) {
    span.end(OpenCensusUtils.getEndSpanOptions(statusCode));
  }

  @Override
  public final void recordSentMessageEvent(long contentLength) {
    OpenCensusUtils.recordSentMessageEvent(span, contentLength);
  }

  @Override
  public final void recordReceivedMessageEvent(long contentLength) {
    OpenCensusUtils.recordReceivedMessageEvent(span, contentLength);
  }

  io.opencensus.trace.Span getSpan() {
    return this.span;
  }

  private void addSpanAttribute(String key, String value) {
    if (value != null) {
      span.putAttribute(key, AttributeValue.stringAttributeValue(value));
    }
  }
}
