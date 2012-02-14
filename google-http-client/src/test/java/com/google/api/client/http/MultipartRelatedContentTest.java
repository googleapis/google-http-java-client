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
import com.google.api.client.testing.http.MockHttpTransport;

import junit.framework.TestCase;
import org.apache.commons.codec.binary.StringUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * Tests {@link MultipartRelatedContent}.
 *
 * @author Yaniv Inbar
 */
public class MultipartRelatedContentTest extends TestCase {

  private static final String CRLF = "\r\n";
  private static final String CONTENT_TYPE = Json.CONTENT_TYPE;

  public void testContent() throws IOException {
    subtestContent(
        "--END_OF_PART" + CRLF + "Content-Type: application/json" + CRLF + CRLF + "foo" + CRLF
            + "--END_OF_PART--", null, "foo");
    subtestContent(
        "--END_OF_PART" + CRLF + "Content-Type: application/json" + CRLF + CRLF + "foo" + CRLF
            + "--END_OF_PART" + CRLF + "Content-Type: application/json" + CRLF + CRLF + "bar" + CRLF
            + "--END_OF_PART--", null, "foo", "bar");
    subtestContent(
        "--myboundary" + CRLF + "Content-Type: application/json" + CRLF + CRLF + "foo" + CRLF
            + "--myboundary" + CRLF + "Content-Type: application/json" + CRLF + CRLF + "bar" + CRLF
            + "--myboundary--", "myboundary", "foo", "bar");
  }

  private void subtestContent(
      String expectedContent, String boundaryString, String firstContent, String... otherContents)
      throws IOException {
    // multipart content
    HttpContent firstPart = ByteArrayContent.fromString(CONTENT_TYPE, firstContent);
    HttpContent[] otherParts = new HttpContent[otherContents.length];
    for (int i = 0; i < otherContents.length; i++) {
      otherParts[i] = ByteArrayContent.fromString(CONTENT_TYPE, otherContents[i]);
    }
    MultipartRelatedContent content = new MultipartRelatedContent(firstPart, otherParts);
    if (boundaryString != null) {
      content.setBoundary(boundaryString);
    }
    // write to string
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    content.writeTo(out);
    assertEquals(expectedContent, out.toString());
    assertEquals(StringUtils.getBytesUtf8(expectedContent).length, content.getLength());
  }

  public void testForRequest() throws IOException {
    MultipartRelatedContent content =
        new MultipartRelatedContent(ByteArrayContent.fromString(CONTENT_TYPE, "foo"));
    HttpRequest request = new MockHttpTransport().createRequestFactory().buildGetRequest(
        new GenericUrl("http://google.com"));
    content.forRequest(request);
    assertEquals(content, request.getContent());
    assertEquals("1.0", request.getHeaders().getMimeVersion());
  }
}
