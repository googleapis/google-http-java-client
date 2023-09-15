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
import io.micrometer.observation.transport.Kind;
import io.micrometer.observation.transport.Propagator;
import io.micrometer.observation.transport.RequestReplySenderContext;
import io.opencensus.common.Scope;
import io.opencensus.contrib.http.util.HttpTraceAttributeConstants;
import io.opencensus.trace.AttributeValue;
import io.opencensus.trace.Span;

import java.io.IOException;
import java.util.Map;
import java.util.Objects;

/**
 * TODO: Write some docs
 *
 * <p>Implementations should normally be thread-safe.
 *
 * @since 1.43
 * @author Marcin Grzejszczak
 */
public class GoogleClientContext extends RequestReplySenderContext<HttpRequest, HttpResponse> {

    public GoogleClientContext(HttpRequest httpRequest) {
        super((req, key, value) -> Objects.requireNonNull(req).getHeaders().put(key, value));
        setCarrier(httpRequest);
    }
}