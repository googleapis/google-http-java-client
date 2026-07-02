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

package com.google.api.client.http.micrometer;

import com.google.api.client.http.*;
import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationRegistry;
import io.opencensus.contrib.http.util.HttpTraceAttributeConstants;
import io.opencensus.trace.AttributeValue;
import io.opencensus.trace.MessageEvent;
import io.opencensus.trace.Span;

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
public class MicrometerObservationHttpInterceptor implements HttpInterceptor {

    private final ObservationRegistry observationRegistry;

    public MicrometerObservationHttpInterceptor(ObservationRegistry observationRegistry) {
        this.observationRegistry = observationRegistry;
    }

    @Override
    public void beforeAllExecutions(Map<Object, Object> context, HttpRequest httpRequest) {
        // TODO: Add conventions etc for customization
        GoogleClientContext clientContext = new GoogleClientContext(httpRequest);
        Observation observation = Observation.createNotStarted("http.client.duration", () -> clientContext, observationRegistry);
        context.put(Observation.class, observation);
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
        Observation observation = getObservation(context);
        observation.highCardinalityKeyValue("retry #", String.valueOf(numRetries - retriesRemaining));
    }

    @Override
    public void beforeSingleExecutionRequestBuilding(Map<Object, Object> context, HttpRequest httpRequest, String urlString) {
        Observation observation = getObservation(context);
        String requestMethod = httpRequest.getRequestMethod();
        GenericUrl url = httpRequest.getUrl();
        addHighCardinalityKey(observation, HttpTraceAttributeConstants.HTTP_METHOD, requestMethod);
        addHighCardinalityKey(observation, HttpTraceAttributeConstants.HTTP_HOST, url.getHost());
        addHighCardinalityKey(observation, HttpTraceAttributeConstants.HTTP_PATH, url.getRawPath());
        addHighCardinalityKey(observation, HttpTraceAttributeConstants.HTTP_URL, urlString);
    }

    private Observation getObservation(Map<Object, Object> context) {
        return getRequired(Observation.class, context);
    }

    private Observation.Scope getScope(Map<Object, Object> context) {
        return getRequired(Observation.Scope.class, context);
    }

    @Override
    public void beforeSingleExecutionHeadersSerialization(Map<Object, Object> context, HttpRequest httpRequest, String originalUserAgent) {
        HttpHeaders headers = httpRequest.getHeaders();
        Observation observation = getObservation(context);
        if (!httpRequest.getSuppressUserAgentSuffix()) {
            if (originalUserAgent == null) {
                addHighCardinalityKey(observation, HttpTraceAttributeConstants.HTTP_USER_AGENT, HttpRequest.USER_AGENT_SUFFIX);
            } else {
                addHighCardinalityKey(observation, HttpTraceAttributeConstants.HTTP_USER_AGENT, headers.getUserAgent());
            }
        }
        observation.contextualName(httpRequest.getRequestMethod());
        observation.start(); //propagation happens here
    }

    @Override
    public void beforeSingleExecutionBytesSending(Map<Object, Object> context, HttpRequest httpRequest, LowLevelHttpRequest lowLevelHttpRequest) {
        // switch tracing scope to current span
        Observation observation = getObservation(context);
        @SuppressWarnings("MustBeClosedChecker")
        Observation.Scope ws = observation.openScope();
        context.put(Observation.Scope.class, ws);
    }

    @Override
    public void afterSingleExecutionResponseReceived(Map<Object, Object> context, HttpRequest httpRequest, LowLevelHttpResponse lowLevelHttpResponse) throws IOException {
        Observation observation = getObservation(context);
        if (lowLevelHttpResponse != null) {
            observation.lowCardinalityKeyValue(
                    HttpTraceAttributeConstants.HTTP_STATUS_CODE,
                    String.valueOf(lowLevelHttpResponse.getStatusCode()));
        }
    }

    @Override
    public void afterSingleExecutionExceptionHappened(Map<Object, Object> context, Throwable throwable) {
        Observation observation = getObservation(context);
        observation.error(throwable);
    }

    @Override
    public void afterSingleExecutionOnFinally(Map<Object, Object> context, HttpRequest httpRequest, LowLevelHttpResponse lowLevelHttpResponse) {
        getScope(context).close();
    }

    @Override
    public void afterAllExecutions(Map<Object, Object> context, HttpRequest httpRequest, HttpResponse response) {
        Observation observation = getObservation(context);
        ((GoogleClientContext) observation.getContext()).setResponse(response);
        observation.stop();
    }

    private static void addHighCardinalityKey(Observation observation, String key, String value) {
        if (value != null) {
            observation.highCardinalityKeyValue(key, value);
        }
    }
}
