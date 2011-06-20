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

import com.google.api.client.protobuf.ProtocolBuffers;
import com.google.api.client.protobuf.SimpleProto;

import junit.framework.TestCase;

/**
 * Tests {@link ProtoHttpContent}.
 *
 * @author Yaniv Inbar
 */
public class ProtoHttpContentTest extends TestCase {

  private static final String FAKE_TYPE = "abc";

  public void test() {
    SimpleProto.TestMessage message = SimpleProto.TestMessage
        .newBuilder()
        .setStatus(SimpleProto.TestStatus.SUCCESS)
        .setName("This is a test!")
        .setValue(123454321)
        .build();
    final ProtoHttpContent content = new ProtoHttpContent(message);
    assertEquals(message, content.getMessage());
    assertEquals(ProtocolBuffers.CONTENT_TYPE, content.getType());
    assertEquals(FAKE_TYPE, content.setType(FAKE_TYPE).getType());
  }
}
