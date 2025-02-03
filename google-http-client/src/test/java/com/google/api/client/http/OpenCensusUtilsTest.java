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

package com.google.api.client.http;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import io.opencensus.trace.Annotation;
import io.opencensus.trace.AttributeValue;
import io.opencensus.trace.BlankSpan;
import io.opencensus.trace.EndSpanOptions;
import io.opencensus.trace.Link;
import io.opencensus.trace.MessageEvent;
import io.opencensus.trace.Span;
import io.opencensus.trace.SpanContext;
import io.opencensus.trace.Status;
import io.opencensus.trace.Tracer;
import io.opencensus.trace.propagation.TextFormat;
import java.util.List;
import java.util.Map;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/**
 * Tests {@link OpenCensusUtils}.
 *
 * @author Hailong Wen
 */
@RunWith(JUnit4.class)
public class OpenCensusUtilsTest {

  TextFormat mockTextFormat;
  TextFormat.Setter mockTextFormatSetter;
  TextFormat originTextFormat;
  TextFormat.Setter originTextFormatSetter;
  Span mockSpan;
  HttpHeaders headers;
  Tracer tracer;

  @Before
  public void setUp() {
    mockTextFormat =
        new TextFormat() {
          @Override
          public List<String> fields() {
            throw new UnsupportedOperationException("TextFormat.fields");
          }

          @Override
          public <C> void inject(SpanContext spanContext, C carrier, Setter<C> setter) {
            throw new UnsupportedOperationException("TextFormat.inject");
          }

          @Override
          public <C> SpanContext extract(C carrier, Getter<C> getter) {
            throw new UnsupportedOperationException("TextFormat.extract");
          }
        };
    mockTextFormatSetter =
        new TextFormat.Setter<HttpHeaders>() {
          @Override
          public void put(HttpHeaders carrier, String key, String value) {
            throw new UnsupportedOperationException("TextFormat.Setter.put");
          }
        };
    headers = new HttpHeaders();
    tracer = OpenCensusUtils.getTracer();
    mockSpan =
        new Span(tracer.getCurrentSpan().getContext(), null) {

          @Override
          public void addAnnotation(String description, Map<String, AttributeValue> attributes) {}

          @Override
          public void addAnnotation(Annotation annotation) {}

          @Override
          public void addMessageEvent(MessageEvent event) {
            throw new UnsupportedOperationException("Span.addMessageEvent");
          }

          @Override
          public void addLink(Link link) {}

          @Override
          public void end(EndSpanOptions options) {}
        };
    originTextFormat = OpenCensusUtils.propagationTextFormat;
    originTextFormatSetter = OpenCensusUtils.propagationTextFormatSetter;
  }

  @After
  public void tearDown() {
    OpenCensusUtils.setPropagationTextFormat(originTextFormat);
    OpenCensusUtils.setPropagationTextFormatSetter(originTextFormatSetter);
  }

  @Test
  public void testInitialization() {
    assertNotNull(OpenCensusUtils.getTracer());
    assertNotNull(OpenCensusUtils.propagationTextFormat);
    assertNotNull(OpenCensusUtils.propagationTextFormatSetter);
  }

  @Test
  public void testSetPropagationTextFormat() {
    OpenCensusUtils.setPropagationTextFormat(mockTextFormat);
    assertEquals(mockTextFormat, OpenCensusUtils.propagationTextFormat);
  }

  @Test
  public void testSetPropagationTextFormatSetter() {
    OpenCensusUtils.setPropagationTextFormatSetter(mockTextFormatSetter);
    assertEquals(mockTextFormatSetter, OpenCensusUtils.propagationTextFormatSetter);
  }

  @Test
  public void testPropagateTracingContextInjection() {
    OpenCensusUtils.setPropagationTextFormat(mockTextFormat);
    try {
      OpenCensusUtils.propagateTracingContext(mockSpan, headers);
      fail("expected " + UnsupportedOperationException.class);
    } catch (UnsupportedOperationException e) {
      assertEquals(e.getMessage(), "TextFormat.inject");
    }
  }

  @Test
  public void testPropagateTracingContextHeader() {
    OpenCensusUtils.setPropagationTextFormatSetter(mockTextFormatSetter);
    try {
      OpenCensusUtils.propagateTracingContext(mockSpan, headers);
      fail("expected " + UnsupportedOperationException.class);
    } catch (UnsupportedOperationException e) {
      assertEquals(e.getMessage(), "TextFormat.Setter.put");
    }
  }

  @Test
  public void testPropagateTracingContextNullSpan() {
    OpenCensusUtils.setPropagationTextFormat(mockTextFormat);
    try {
      OpenCensusUtils.propagateTracingContext(null, headers);
      fail("expected " + IllegalArgumentException.class);
    } catch (IllegalArgumentException e) {
      assertEquals(e.getMessage(), "span should not be null.");
    }
  }

  @Test
  public void testPropagateTracingContextNullHeaders() {
    OpenCensusUtils.setPropagationTextFormat(mockTextFormat);
    try {
      OpenCensusUtils.propagateTracingContext(mockSpan, null);
      fail("expected " + IllegalArgumentException.class);
    } catch (IllegalArgumentException e) {
      assertEquals(e.getMessage(), "headers should not be null.");
    }
  }

  @Test
  public void testPropagateTracingContextInvalidSpan() {
    OpenCensusUtils.setPropagationTextFormat(mockTextFormat);
    // No injection. No exceptions should be thrown.
    OpenCensusUtils.propagateTracingContext(BlankSpan.INSTANCE, headers);
  }

  @Test
  public void testGetEndSpanOptionsNoResponse() {
    EndSpanOptions expected = EndSpanOptions.builder().setStatus(Status.UNKNOWN).build();
    assertEquals(expected, OpenCensusUtils.getEndSpanOptions(null));
  }

  @Test
  public void testGetEndSpanOptionsSuccess() {
    EndSpanOptions expected = EndSpanOptions.builder().setStatus(Status.OK).build();
    assertEquals(expected, OpenCensusUtils.getEndSpanOptions(200));
    assertEquals(expected, OpenCensusUtils.getEndSpanOptions(201));
    assertEquals(expected, OpenCensusUtils.getEndSpanOptions(202));
  }

  @Test
  public void testGetEndSpanOptionsBadRequest() {
    EndSpanOptions expected = EndSpanOptions.builder().setStatus(Status.INVALID_ARGUMENT).build();
    assertEquals(expected, OpenCensusUtils.getEndSpanOptions(400));
  }

  @Test
  public void testGetEndSpanOptionsUnauthorized() {
    EndSpanOptions expected = EndSpanOptions.builder().setStatus(Status.UNAUTHENTICATED).build();
    assertEquals(expected, OpenCensusUtils.getEndSpanOptions(401));
  }

  @Test
  public void testGetEndSpanOptionsForbidden() {
    EndSpanOptions expected = EndSpanOptions.builder().setStatus(Status.PERMISSION_DENIED).build();
    assertEquals(expected, OpenCensusUtils.getEndSpanOptions(403));
  }

  @Test
  public void testGetEndSpanOptionsNotFound() {
    EndSpanOptions expected = EndSpanOptions.builder().setStatus(Status.NOT_FOUND).build();
    assertEquals(expected, OpenCensusUtils.getEndSpanOptions(404));
  }

  @Test
  public void testGetEndSpanOptionsPreconditionFailed() {
    EndSpanOptions expected =
        EndSpanOptions.builder().setStatus(Status.FAILED_PRECONDITION).build();
    assertEquals(expected, OpenCensusUtils.getEndSpanOptions(412));
  }

  @Test
  public void testGetEndSpanOptionsServerError() {
    EndSpanOptions expected = EndSpanOptions.builder().setStatus(Status.UNAVAILABLE).build();
    assertEquals(expected, OpenCensusUtils.getEndSpanOptions(500));
  }

  @Test
  public void testGetEndSpanOptionsOther() {
    EndSpanOptions expected = EndSpanOptions.builder().setStatus(Status.UNKNOWN).build();
    // test some random unsupported statuses
    assertEquals(expected, OpenCensusUtils.getEndSpanOptions(301));
    assertEquals(expected, OpenCensusUtils.getEndSpanOptions(402));
    assertEquals(expected, OpenCensusUtils.getEndSpanOptions(501));
  }

  @Test
  public void testRecordMessageEventInNullSpan() {
    try {
      OpenCensusUtils.recordMessageEvent(null, 0, MessageEvent.Type.SENT);
      fail("expected " + IllegalArgumentException.class);
    } catch (IllegalArgumentException e) {
      assertEquals(e.getMessage(), "span should not be null.");
    }
  }

  @Test
  public void testRecordMessageEvent() {
    try {
      OpenCensusUtils.recordMessageEvent(mockSpan, 0, MessageEvent.Type.SENT);
      fail("expected " + UnsupportedOperationException.class);
    } catch (UnsupportedOperationException e) {
      assertEquals(e.getMessage(), "Span.addMessageEvent");
    }
  }
}
