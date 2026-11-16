/*
 * Copyright (c) 2023 Google Inc.
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

import io.opencensus.common.Scope;
import io.opencensus.contrib.http.util.HttpTraceAttributeConstants;
import io.opencensus.trace.AttributeValue;
import io.opencensus.trace.Span;
import io.opencensus.trace.Tracer;

import java.io.IOException;
import java.util.Map;

/**
 * TODO: Write some docs
 *
 * <p>Implementations should normally be thread-safe.
 *
 * @since 1.43
 * @author Marcin Grzejszczak
 */
public class OpenCensusHttpInterceptor implements HttpInterceptor {

    /** OpenCensus tracing component. */
    private final Tracer tracer = OpenCensusUtils.getTracer();

    @Override
    public void beforeAllExecutions(Map<Object, Object> context, HttpRequest httpRequest) {
        Span span =
                tracer
                        .spanBuilder(OpenCensusUtils.SPAN_NAME_HTTP_REQUEST_EXECUTE)
                        .setRecordEvents(OpenCensusUtils.isRecordEvent())
                        .startSpan();
        context.put(Span.class, span);
    }

    @SuppressWarnings("unchecked")
    private <T> T getRequired(Object key, Map<Object, Object> context) {
        if (!context.containsKey(key)) {
            throw new IllegalStateException("Object with key <" + key + "> was not found in <" + context
             + ">");
        }
        return (T) context.get(key);
    }

    @Override
    public void beforeSingleExecutionStart(Map<Object, Object> context, HttpRequest httpRequest, int numRetries, int retriesRemaining) {
        Span span = getSpan(context);
        span.addAnnotation("retry #" + (numRetries - retriesRemaining));
    }

    @Override
    public void beforeSingleExecutionRequestBuilding(Map<Object, Object> context, HttpRequest httpRequest, String urlString) {
        Span span = getSpan(context);
        String requestMethod = httpRequest.getRequestMethod();
        GenericUrl url = httpRequest.getUrl();
        addSpanAttribute(span, HttpTraceAttributeConstants.HTTP_METHOD, requestMethod);
        addSpanAttribute(span, HttpTraceAttributeConstants.HTTP_HOST, url.getHost());
        addSpanAttribute(span, HttpTraceAttributeConstants.HTTP_PATH, url.getRawPath());
        addSpanAttribute(span, HttpTraceAttributeConstants.HTTP_URL, urlString);
    }

    private Span getSpan(Map<Object, Object> context) {
        return getRequired(Span.class, context);
    }

    private Scope getScope(Map<Object, Object> context) {
        return getRequired(Scope.class, context);
    }

    @Override
    public void beforeSingleExecutionHeadersSerialization(Map<Object, Object> context, HttpRequest httpRequest, String originalUserAgent) {
        HttpHeaders headers = httpRequest.getHeaders();
        Span span = getSpan(context);
        if (!httpRequest.getSuppressUserAgentSuffix()) {
            if (originalUserAgent == null) {
                addSpanAttribute(span, HttpTraceAttributeConstants.HTTP_USER_AGENT, HttpRequest.USER_AGENT_SUFFIX);
            } else {
                addSpanAttribute(span, HttpTraceAttributeConstants.HTTP_USER_AGENT, headers.getUserAgent());
            }
        }
        OpenCensusUtils.propagateTracingContext(span, headers);
    }

    @Override
    public void beforeSingleExecutionBytesSending(Map<Object, Object> context, HttpRequest httpRequest, LowLevelHttpRequest lowLevelHttpRequest) {
        // switch tracing scope to current span
        Span span = getSpan(context);
        @SuppressWarnings("MustBeClosedChecker")
        Scope ws = tracer.withSpan(span);
        OpenCensusUtils.recordSentMessageEvent(span, lowLevelHttpRequest.getContentLength());
        context.put(Scope.class, ws);
    }

    @Override
    public void afterSingleExecutionResponseReceived(Map<Object, Object> context, HttpRequest httpRequest, LowLevelHttpResponse lowLevelHttpResponse) throws IOException {
        Span span = getSpan(context);
        if (lowLevelHttpResponse != null) {
            OpenCensusUtils.recordReceivedMessageEvent(span, lowLevelHttpResponse.getContentLength());
            span.putAttribute(
                    HttpTraceAttributeConstants.HTTP_STATUS_CODE,
                    AttributeValue.longAttributeValue(lowLevelHttpResponse.getStatusCode()));
        }
    }

    @Override
    public void afterSingleExecutionExceptionHappened(Map<Object, Object> context, Throwable throwable) {
        Span span = getSpan(context);
        // static analysis shows response is always null here
        span.end(OpenCensusUtils.getEndSpanOptions(null));
    }

    @Override
    public void afterSingleExecutionOnFinally(Map<Object, Object> context, HttpRequest httpRequest, LowLevelHttpResponse lowLevelHttpResponse) {
        getScope(context).close();
    }

    @Override
    public void afterAllExecutions(Map<Object, Object> context, HttpRequest httpRequest, HttpResponse response) {
        Span span = getSpan(context);
        span.end(OpenCensusUtils.getEndSpanOptions(response == null ? null : response.getStatusCode()));
    }

    private static void addSpanAttribute(Span span, String key, String value) {
        if (value != null) {
            span.putAttribute(key, AttributeValue.stringAttributeValue(value));
        }
    }
}
