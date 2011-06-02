/*
 * Copyright (c) 2011 Google Inc.
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

package com.google.api.client.http.protobuf;

import com.google.api.client.http.protobuf.ProtoHttpParser.Builder;
import com.google.api.client.protobuf.ProtocolBuffers;

import junit.framework.TestCase;

/**
 * Tests {@link ProtoHttpParser}.
 *
 * @author Yaniv Inbar
 */
public class ProtoHttpParserTest extends TestCase {

  private static final String FAKE_TYPE = "abc";

  public void testBuilder() {
    Builder builder = ProtoHttpParser.builder();
    assertEquals(ProtocolBuffers.CONTENT_TYPE, builder.getContentType());
    builder.setContentType(FAKE_TYPE);
    assertEquals(FAKE_TYPE, builder.getContentType());
    ProtoHttpParser content = builder.build();
    assertEquals(FAKE_TYPE, content.getContentType());
  }
}
