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
import static org.mockito.Matchers.isA;
import static org.mockito.Matchers.same;
import static org.mockito.Mockito.verify;

import io.opencensus.trace.EndSpanOptions;
import io.opencensus.trace.MessageEvent;
import io.opencensus.trace.Span;
import io.opencensus.trace.SpanContext;
import io.opencensus.trace.SpanId;
import io.opencensus.trace.Status;
import io.opencensus.trace.TraceId;
import io.opencensus.trace.TraceOptions;
import io.opencensus.trace.propagation.TextFormat;
import java.util.Random;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

/**
 * Tests {@link OpenCensusUtils}.
 *
 * @author Hailong Wen
 */
@RunWith(JUnit4.class)
public class OpenCensusUtilsTest {

  @Rule public final ExpectedException thrown = ExpectedException.none();

  @Mock TextFormat mockTextFormat;
  @Mock TextFormat.Setter mockTextFormatSetter;
  @Mock Span mockSpan;
  TextFormat originTextFormat = OpenCensusUtils.propagationTextFormat;
  TextFormat.Setter originTextFormatSetter = OpenCensusUtils.propagationTextFormatSetter;
  HttpHeaders headers = new HttpHeaders();
  Random random = new Random(1234);
  SpanContext spanContext = SpanContext.create(
      TraceId.generateRandomId(random), SpanId.generateRandomId(random), TraceOptions.DEFAULT);

  @Before
  public void setUp() {
    MockitoAnnotations.initMocks(this);
  }

  @After
  public void tearDown() {
    OpenCensusUtils.setPropagationTextFormat(originTextFormat);
    OpenCensusUtils.setPropagationTextFormatSetter(originTextFormatSetter);
  }

  @Test
  public void testInitializatoin() {
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
    OpenCensusUtils.propagateTracingContext(spanContext, headers);
    verify(mockTextFormat).inject(same(spanContext), same(headers), same(originTextFormatSetter));
  }

  @Test
  public void testPropagateTracingContextHeader() {
    OpenCensusUtils.setPropagationTextFormatSetter(mockTextFormatSetter);
    OpenCensusUtils.propagateTracingContext(spanContext, headers);
    verify(mockTextFormatSetter).put(same(headers), isA(String.class), isA(String.class));
  }

  @Test
  public void testPropagateTracingContextNullSpan() {
    OpenCensusUtils.setPropagationTextFormat(mockTextFormat);
    thrown.expect(IllegalArgumentException.class);
    thrown.expectMessage("spanContext should not be null.");
    OpenCensusUtils.propagateTracingContext(null, headers);
  }

  @Test
  public void testPropagateTracingContextNullHeaders() {
    OpenCensusUtils.setPropagationTextFormat(mockTextFormat);
    thrown.expect(IllegalArgumentException.class);
    thrown.expectMessage("headers should not be null.");
    OpenCensusUtils.propagateTracingContext(spanContext, null);
  }

  @Test
  public void testPropagateTracingContextInvalidSpan() {
    OpenCensusUtils.setPropagationTextFormat(mockTextFormat);
    // No injection. No exceptions should be thrown.
    OpenCensusUtils.propagateTracingContext(SpanContext.INVALID, headers);
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
    EndSpanOptions expected = EndSpanOptions.builder().setStatus(Status.FAILED_PRECONDITION)
        .build();
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
    thrown.expect(IllegalArgumentException.class);
    thrown.expectMessage("span should not be null.");
    OpenCensusUtils.recordMessageEvent(null, 0, 0, MessageEvent.Type.SENT);
  }

  @Test
  public void testRecordMessageEvent() {
    OpenCensusUtils.recordMessageEvent(mockSpan, 0, 0, MessageEvent.Type.SENT);
    verify(mockSpan).addMessageEvent(isA(MessageEvent.class));
  }
}
