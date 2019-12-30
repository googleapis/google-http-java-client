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
import com.google.api.client.util.Charsets;
import com.google.api.client.util.StringUtils;
import java.io.ByteArrayOutputStream;
import junit.framework.TestCase;

/**
 * Tests {@link MultipartContent}.
 *
 * @author Yaniv Inbar
 */
public class MultipartContentTest extends TestCase {

  private static final String BOUNDARY = "__END_OF_PART__";
  private static final String CRLF = "\r\n";
  private static final String CONTENT_TYPE = Json.MEDIA_TYPE;
  private static final String HEADERS = headers("application/json; charset=UTF-8", "foo");

  private static String headers(String contentType, String value) {
      return "Content-Length: " + value.length() + CRLF
          + "Content-Type: " + contentType + CRLF
          + "content-transfer-encoding: binary" + CRLF;
  }

  public void testRandomContent() throws Exception {
    MultipartContent content = new MultipartContent();
    String boundaryString = content.getBoundary();
    assertNotNull(boundaryString);
    assertTrue(boundaryString.startsWith(BOUNDARY));
    assertTrue(boundaryString.endsWith("__"));
    assertEquals("multipart/related; boundary=" + boundaryString, content.getType());

    final String[][] VALUES = new String[][] {
            {"Hello world", "text/plain"},
            {"<xml>Hi</xml>", "application/xml"},
            {"{x:1,y:2}", "application/json"}
    };
    StringBuilder expectedStringBuilder = new StringBuilder();
    for (String[] valueTypePair: VALUES) {
      String contentValue = valueTypePair[0];
      String contentType = valueTypePair[1];
      content.addPart(new MultipartContent.Part(ByteArrayContent.fromString(contentType, contentValue)));
      expectedStringBuilder.append("--").append(boundaryString).append(CRLF)
              .append(headers(contentType, contentValue)).append(CRLF)
              .append(contentValue).append(CRLF);
    }
    expectedStringBuilder.append("--").append(boundaryString).append("--").append(CRLF);
    // write to string
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    content.writeTo(out);
    String expectedContent = expectedStringBuilder.toString();
    assertEquals(expectedContent, out.toString(Charsets.UTF_8.name()));
    assertEquals(StringUtils.getBytesUtf8(expectedContent).length, content.getLength());
  }

  public void testContent() throws Exception {
    subtestContent("--" + BOUNDARY + "--" + CRLF, null);
    subtestContent(
        "--" + BOUNDARY + CRLF
                + HEADERS + CRLF
                + "foo" + CRLF
                + "--" + BOUNDARY + "--" + CRLF,
            null,
        "foo");
    subtestContent(
        "--" + BOUNDARY + CRLF
            + HEADERS + CRLF
            + "foo" + CRLF
            + "--" + BOUNDARY + CRLF
            + HEADERS + CRLF
            + "bar" + CRLF
            + "--" + BOUNDARY + "--" + CRLF,
            null,
        "foo",
        "bar");
    subtestContent(
        "--myboundary" + CRLF
            + HEADERS + CRLF
            + "foo" + CRLF
            + "--myboundary" + CRLF
            + HEADERS + CRLF
            + "bar" + CRLF
            + "--myboundary--" + CRLF,
        "myboundary",
        "foo",
        "bar");
  }

  private void subtestContent(String expectedContent, String boundaryString, String... contents)
      throws Exception {
    // multipart content
    MultipartContent content = new MultipartContent(boundaryString == null ? BOUNDARY : boundaryString);
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
    assertEquals(expectedContent, out.toString(Charsets.UTF_8.name()));
    assertEquals(StringUtils.getBytesUtf8(expectedContent).length, content.getLength());
    assertEquals(
        boundaryString == null
            ? "multipart/related; boundary=" + BOUNDARY
            : "multipart/related; boundary=" + boundaryString,
        content.getType());
  }
}
