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
import com.google.api.client.testing.http.MockHttpTransport;
import com.google.api.client.testing.http.MockLowLevelHttpResponse;
import io.micrometer.observation.transport.RequestReplySenderContext;
import io.micrometer.tracing.exporter.FinishedSpan;
import io.micrometer.tracing.test.SampleTestRunner;
import org.assertj.core.api.Assertions;
import org.awaitility.Awaitility;

import java.util.List;
import java.util.Objects;

/**
 * TODO: Write some docs
 *
 * <p>Implementations should normally be thread-safe.
 *
 * @since 1.43
 * @author Marcin Grzejszczak
 */
public class GoogleClientObservationTests extends SampleTestRunner {

    @Override
    public SampleTestRunnerConsumer yourCode() throws Exception {
        return (buildingBlocks, meterRegistry) -> {
            MockLowLevelHttpResponse mockResponse = new MockLowLevelHttpResponse().setStatusCode(200);
            HttpTransport transport =
                    new MockHttpTransport.Builder().setLowLevelHttpResponse(mockResponse).build();
            HttpRequest request = transport.createRequestFactory()
                            .buildGetRequest(new GenericUrl("https://google.com/"));
            request.addHttpInterceptor(new MicrometerObservationHttpInterceptor(getObservationRegistry()));
            request.execute();

            Awaitility.await().untilAsserted(() -> {
                List<FinishedSpan> finishedSpans = buildingBlocks.getFinishedSpans();

                Assertions.assertThat(finishedSpans).isNotEmpty();
            });
        };
    }
}