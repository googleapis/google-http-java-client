/*
 * Copyright 2019 Google LLC
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

import static com.google.api.client.http.OpenCensusUtils.SPAN_NAME_HTTP_REQUEST_EXECUTE;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import com.google.api.client.testing.http.MockHttpTransport;
import com.google.api.client.testing.http.MockLowLevelHttpRequest;
import com.google.api.client.testing.http.MockLowLevelHttpResponse;
import io.opencensus.common.Functions;
import io.opencensus.testing.export.TestHandler;
import io.opencensus.trace.AttributeValue;
import io.opencensus.trace.MessageEvent;
import io.opencensus.trace.Status;
import io.opencensus.trace.Tracing;
import io.opencensus.trace.config.TraceParams;
import io.opencensus.trace.export.SpanData;
import io.opencensus.trace.samplers.Samplers;
import java.io.IOException;
import java.util.List;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class HttpRequestTracingTest {
  private static final TestHandler testHandler = new TestHandler();

  @Before
  public void setupTestTracer() {
    Tracing.getExportComponent().getSpanExporter().registerHandler("test", testHandler);
    TraceParams params =
        Tracing.getTraceConfig().getActiveTraceParams().toBuilder()
            .setSampler(Samplers.alwaysSample())
            .build();
    Tracing.getTraceConfig().updateActiveTraceParams(params);
  }

  @After
  public void teardownTestTracer() {
    Tracing.getExportComponent().getSpanExporter().unregisterHandler("test");
  }

  @Test(timeout = 20_000L)
  public void executeCreatesSpan() throws IOException {
    MockLowLevelHttpResponse mockResponse = new MockLowLevelHttpResponse().setStatusCode(200);
    HttpTransport transport =
        new MockHttpTransport.Builder().setLowLevelHttpResponse(mockResponse).build();
    HttpRequest request =
        new HttpRequestFactory(transport, null)
            .buildGetRequest(new GenericUrl("https://google.com/"));
    request.execute();

    // This call blocks - we set a timeout on this test to ensure we don't wait forever
    List<SpanData> spans = testHandler.waitForExport(1);
    assertEquals(1, spans.size());
    SpanData span = spans.get(0);

    // Ensure the span name is set
    assertEquals(SPAN_NAME_HTTP_REQUEST_EXECUTE, span.getName());

    // Ensure we have basic span attributes
    assertAttributeEquals(span, "http.path", "/");
    assertAttributeEquals(span, "http.host", "google.com");
    assertAttributeEquals(span, "http.url", "https://google.com/");
    assertAttributeEquals(span, "http.method", "GET");
    assertAttributeEquals(span, "http.status_code", "200");

    // Ensure we have a single annotation for starting the first attempt
    assertEquals(1, span.getAnnotations().getEvents().size());

    // Ensure we have 2 message events, SENT and RECEIVED
    assertEquals(2, span.getMessageEvents().getEvents().size());
    assertEquals(
        MessageEvent.Type.SENT, span.getMessageEvents().getEvents().get(0).getEvent().getType());
    assertEquals(
        MessageEvent.Type.RECEIVED,
        span.getMessageEvents().getEvents().get(1).getEvent().getType());

    // Ensure we record the span status as OK
    assertEquals(Status.OK, span.getStatus());
  }

  @Test(timeout = 20_000L)
  public void executeExceptionCreatesSpan() throws IOException {
    HttpTransport transport =
        new MockHttpTransport.Builder()
            .setLowLevelHttpRequest(
                new MockLowLevelHttpRequest() {
                  @Override
                  public LowLevelHttpResponse execute() throws IOException {
                    throw new IOException("some IOException");
                  }
                })
            .build();
    HttpRequest request =
        new HttpRequestFactory(transport, null)
            .buildGetRequest(new GenericUrl("https://google.com/"));

    try {
      request.execute();
      fail("expected to throw an IOException");
    } catch (IOException expected) {
    }

    // This call blocks - we set a timeout on this test to ensure we don't wait forever
    List<SpanData> spans = testHandler.waitForExport(1);
    assertEquals(1, spans.size());
    SpanData span = spans.get(0);

    // Ensure the span name is set
    assertEquals(SPAN_NAME_HTTP_REQUEST_EXECUTE, span.getName());

    // Ensure we have basic span attributes
    assertAttributeEquals(span, "http.path", "/");
    assertAttributeEquals(span, "http.host", "google.com");
    assertAttributeEquals(span, "http.url", "https://google.com/");
    assertAttributeEquals(span, "http.method", "GET");

    // Ensure we have a single annotation for starting the first attempt
    assertEquals(1, span.getAnnotations().getEvents().size());

    // Ensure we have 2 message events, SENT and RECEIVED
    assertEquals(1, span.getMessageEvents().getEvents().size());
    assertEquals(
        MessageEvent.Type.SENT, span.getMessageEvents().getEvents().get(0).getEvent().getType());

    // Ensure we record the span status as UNKNOWN
    assertEquals(Status.UNKNOWN, span.getStatus());
  }

  void assertAttributeEquals(SpanData span, String attributeName, String expectedValue) {
    Object attributeValue = span.getAttributes().getAttributeMap().get(attributeName);
    assertNotNull("expected span to contain attribute: " + attributeName, attributeValue);
    assertTrue(attributeValue instanceof AttributeValue);
    String value =
        ((AttributeValue) attributeValue)
            .match(
                Functions.returnToString(),
                Functions.returnToString(),
                Functions.returnToString(),
                Functions.returnToString(),
                Functions.</*@Nullable*/ String>returnNull());
    assertEquals(expectedValue, value);
  }
}
