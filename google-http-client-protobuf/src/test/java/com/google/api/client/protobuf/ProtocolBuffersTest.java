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

package com.google.api.client.protobuf;

import java.io.ByteArrayInputStream;
import junit.framework.TestCase;

/**
 * Tests {@link ProtocolBuffers}.
 *
 * @author Yaniv Inbar
 */
public class ProtocolBuffersTest extends TestCase {

  public void testParseAndClose() throws Exception {
    SimpleProto.TestMessage mockResponse =
        SimpleProto.TestMessage.newBuilder()
            .setStatus(SimpleProto.TestStatus.SUCCESS)
            .setName("This is a test!")
            .setValue(123454321)
            .build();
    // Create the parser and test it with our mock response
    SimpleProto.TestMessage parsedResponse =
        ProtocolBuffers.parseAndClose(
            new ByteArrayInputStream(mockResponse.toByteArray()), SimpleProto.TestMessage.class);
    // Validate the parser properly parsed the response
    // (i.e. it matches the original mock response)
    assertEquals(mockResponse.getSerializedSize(), parsedResponse.getSerializedSize());
    assertEquals(mockResponse.getStatus(), parsedResponse.getStatus());
    assertEquals(mockResponse.getName(), parsedResponse.getName());
    assertEquals(mockResponse.getValue(), parsedResponse.getValue());
  }
}
