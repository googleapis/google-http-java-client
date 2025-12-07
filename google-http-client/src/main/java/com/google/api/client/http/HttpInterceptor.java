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
public interface HttpInterceptor {

    void beforeAllExecutions(Map<Object, Object> context, HttpRequest httpRequest);

    void beforeSingleExecutionStart(Map<Object, Object> context, HttpRequest httpRequest, int numRetries, int retriesRemaining);

    void beforeSingleExecutionRequestBuilding(Map<Object, Object> context, HttpRequest httpRequest, String urlString);

    void beforeSingleExecutionHeadersSerialization(Map<Object, Object> context, HttpRequest httpRequest, String originalUserAgent);

    void beforeSingleExecutionBytesSending(Map<Object, Object> context, HttpRequest httpRequest, LowLevelHttpRequest lowLevelHttpRequest);

    void afterSingleExecutionResponseReceived(Map<Object, Object> context, HttpRequest httpRequest, LowLevelHttpResponse httpResponse) throws IOException;

    void afterSingleExecutionExceptionHappened(Map<Object, Object> context, Throwable throwable);

    void afterSingleExecutionOnFinally(Map<Object, Object> context, HttpRequest httpRequest, LowLevelHttpResponse lowLevelHttpResponse);

    void afterAllExecutions(Map<Object, Object> context, HttpRequest httpRequest, HttpResponse httpResponse);
}
