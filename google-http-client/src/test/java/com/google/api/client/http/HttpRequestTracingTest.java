package com.google.api.client.http;

import com.google.api.client.testing.http.MockHttpTransport;
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
import org.junit.*;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import static com.google.api.client.http.OpenCensusUtils.SPAN_NAME_HTTP_REQUEST_EXECUTE;
import static org.junit.Assert.*;

public class HttpRequestTracingTest {
  private static final TestHandler testHandler = new TestHandler();

  @Before
  public void setupTestTracer() {
    Tracing.getExportComponent().getSpanExporter().registerHandler("test", testHandler);
    TraceParams params =
        Tracing.getTraceConfig()
            .getActiveTraceParams()
            .toBuilder()
            .setSampler(Samplers.alwaysSample())
            .build();
    Tracing.getTraceConfig().updateActiveTraceParams(params);
  }

  @After
  public void teardownTestTracer() {
    Tracing.getExportComponent().getSpanExporter().unregisterHandler("test");
  }

  @Test(timeout = 20_000L)
  public void testExecute_spanClosureOnException() throws IOException {
    MockLowLevelHttpResponse mockResponse = new MockLowLevelHttpResponse()
        .setStatusCode(200);
    HttpTransport transport = new MockHttpTransport.Builder()
        .setLowLevelHttpResponse(mockResponse)
        .build();
    HttpRequest request = new HttpRequestFactory(transport, null)
        .buildGetRequest(new GenericUrl("https://google.com/"));
    HttpResponse response = request.execute();

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
    assertEquals(2, span.getMessageEvents().getEvents().size());
    assertEquals(MessageEvent.Type.SENT, span.getMessageEvents().getEvents().get(0).getEvent().getType());
    assertEquals(MessageEvent.Type.RECEIVED, span.getMessageEvents().getEvents().get(1).getEvent().getType());

    // Ensure we correctly record the span status as OK
    assertEquals(Status.OK, span.getStatus());
  }

  void assertAttributeEquals(SpanData span, String attributeName, String expectedValue) {
    Object attributeValue = span.getAttributes().getAttributeMap().get(attributeName);
    assertNotNull("expected span to contain attribute: " + attributeName, attributeValue);
    assertTrue(attributeValue instanceof AttributeValue);
    String value = ((AttributeValue) attributeValue).match(
        Functions.returnToString(),
        Functions.returnToString(),
        Functions.returnToString(),
        Functions.returnToString(),
        Functions.</*@Nullable*/ String>returnNull());
    assertEquals(expectedValue, value);
  }
}
