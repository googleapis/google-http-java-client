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

package com.google.api.client.http;

import com.google.api.client.json.Json;
import com.google.api.client.util.StringUtils;

import junit.framework.TestCase;

import java.io.ByteArrayOutputStream;

/**
 * Tests {@link MultipartContent}.
 *
 * @author Yaniv Inbar
 */
public class MultipartContentTest extends TestCase {

  private static final String CRLF = "\r\n";
  private static final String CONTENT_TYPE = Json.MEDIA_TYPE;
  private static final String HEADERS = "Content-Length: 3" + CRLF
      + "Content-Type: application/json; charset=UTF-8" + CRLF + "content-transfer-encoding: binary"
      + CRLF;

  public void testContent() throws Exception {
    subtestContent("--__END_OF_PART__--" + CRLF, null);
    subtestContent(
        "--__END_OF_PART__" + CRLF + HEADERS + CRLF + "foo" + CRLF + "--__END_OF_PART__--" + CRLF,
        null, "foo");
    subtestContent("--__END_OF_PART__" + CRLF + HEADERS + CRLF + "foo" + CRLF + "--__END_OF_PART__"
        + CRLF + HEADERS + CRLF + "bar" + CRLF + "--__END_OF_PART__--" + CRLF, null, "foo", "bar");
    subtestContent("--myboundary" + CRLF + HEADERS + CRLF + "foo" + CRLF + "--myboundary" + CRLF
        + HEADERS + CRLF + "bar" + CRLF + "--myboundary--" + CRLF, "myboundary", "foo", "bar");
  }

  private void subtestContent(String expectedContent, String boundaryString, String... contents)
      throws Exception {
    // multipart content
    MultipartContent content = new MultipartContent();
    for (String contentValue : contents) {
      content.addPart(
          new MultipartContent.Part(ByteArrayContent.fromString(CONTENT_TYPE, contentValue)));
    }
    if (boundaryString != null) {
      content.setBoundary(boundaryString);
    }
    // write to string
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    content.writeTo(out);
    assertEquals(expectedContent, out.toString());
    assertEquals(StringUtils.getBytesUtf8(expectedContent).length, content.getLength());
    assertEquals(boundaryString == null ? "multipart/related; boundary=__END_OF_PART__" :
        "multipart/related; boundary=" + boundaryString, content.getType());
  }
}
