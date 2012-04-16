/*
 * Copyright (c) 2010 Google Inc.
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

import com.google.api.client.http.LogContentTest.Recorder;
import com.google.api.client.testing.http.HttpTesting;
import com.google.api.client.testing.http.MockHttpTransport;
import com.google.api.client.testing.http.MockLowLevelHttpRequest;
import com.google.api.client.testing.http.MockLowLevelHttpResponse;
import com.google.api.client.util.Key;

import junit.framework.TestCase;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.logging.Level;

/**
 * Tests {@link HttpResponse}.
 *
 * @author Yaniv Inbar
 */
public class HttpResponseTest extends TestCase {

  public HttpResponseTest() {
  }

  public HttpResponseTest(String name) {
    super(name);
  }

  public void testParseAsString_none() throws IOException {
    HttpTransport transport = new MockHttpTransport();
    HttpRequest request =
        transport.createRequestFactory().buildGetRequest(HttpTesting.SIMPLE_GENERIC_URL);
    HttpResponse response = request.execute();
    assertEquals("", response.parseAsString());
  }

  private static final String SAMPLE = "123\u05D9\u05e0\u05D9\u05D1";

  public void testParseAsString_utf8() throws IOException {
    HttpTransport transport = new MockHttpTransport() {
      @Override
      public LowLevelHttpRequest buildGetRequest(String url) throws IOException {
        return new MockLowLevelHttpRequest() {
          @Override
          public LowLevelHttpResponse execute() throws IOException {
            MockLowLevelHttpResponse result = new MockLowLevelHttpResponse();
            result.setContentType("application/json; charset=UTF-8");
            result.setContent(SAMPLE);
            return result;
          }
        };
      }
    };
    HttpRequest request =
        transport.createRequestFactory().buildGetRequest(HttpTesting.SIMPLE_GENERIC_URL);
    HttpResponse response = request.execute();
    assertEquals(SAMPLE, response.parseAsString());
  }

  public static class MyHeaders extends HttpHeaders {

    @Key
    public String foo;

    @Key
    public Object obj;

    @Key
    String[] r;
  }

  public void testHeaderParsing() throws IOException {
    HttpTransport transport = new MockHttpTransport() {
      @Override
      public LowLevelHttpRequest buildGetRequest(String url) throws IOException {
        return new MockLowLevelHttpRequest() {
          @Override
          public LowLevelHttpResponse execute() throws IOException {
            MockLowLevelHttpResponse result = new MockLowLevelHttpResponse();
            result.addHeader("accept", "value");
            result.addHeader("foo", "bar");
            result.addHeader("goo", "car");
            result.addHeader("hoo", "dar");
            result.addHeader("hoo", "far");
            result.addHeader("obj", "o");
            result.addHeader("r", "a1");
            result.addHeader("r", "a2");
            return result;
          }
        };
      }
    };
    HttpRequest request =
        transport.createRequestFactory().buildGetRequest(HttpTesting.SIMPLE_GENERIC_URL);
    request.setResponseHeaders(new MyHeaders());
    HttpResponse response = request.execute();
    assertEquals("value", response.getHeaders().getAccept());
    assertEquals("bar", ((MyHeaders) response.getHeaders()).foo);
    assertEquals(Arrays.asList("o"), ((MyHeaders) response.getHeaders()).obj);
    assertEquals(Arrays.asList("a1", "a2"), Arrays.asList(((MyHeaders) response.getHeaders()).r));
    assertEquals(Arrays.asList("car"), response.getHeaders().get("goo"));
    assertEquals(Arrays.asList("dar", "far"), response.getHeaders().get("hoo"));
  }

  public void testParseAs_noParser() throws IOException {
    HttpTransport transport = new MockHttpTransport() {
      @Override
      public LowLevelHttpRequest buildGetRequest(final String url) throws IOException {
        return new MockLowLevelHttpRequest() {
          @Override
          public LowLevelHttpResponse execute() throws IOException {
            MockLowLevelHttpResponse result = new MockLowLevelHttpResponse();
            if (!url.equals(HttpTesting.SIMPLE_URL)) {
              result.setContentType(url.substring(HttpTesting.SIMPLE_URL.length()));
            }
            return result;
          }
        };
      }
    };
    try {
      transport
          .createRequestFactory()
          .buildGetRequest(HttpTesting.SIMPLE_GENERIC_URL)
          .execute()
          .parseAs(Object.class);
      fail("expected " + IllegalArgumentException.class);
    } catch (IllegalArgumentException e) {
      assertEquals(e.getMessage(), "Missing Content-Type header in response");
    }
    try {
      transport
          .createRequestFactory()
          .buildGetRequest(new GenericUrl(HttpTesting.SIMPLE_URL + "something"))
          .execute()
          .parseAs(Object.class);
      fail("expected " + IllegalArgumentException.class);
    } catch (IllegalArgumentException e) {
      assertEquals(e.getMessage(), "No parser defined for Content-Type: something");
    }
  }

  public void testDownload() throws IOException {
    HttpTransport transport = new MockHttpTransport() {
      @Override
      public LowLevelHttpRequest buildGetRequest(String url) throws IOException {
        return new MockLowLevelHttpRequest() {
          @Override
          public LowLevelHttpResponse execute() throws IOException {
            MockLowLevelHttpResponse result = new MockLowLevelHttpResponse();
            result.setContentType("application/json; charset=UTF-8");
            result.setContent(SAMPLE);
            return result;
          }
        };
      }
    };
    HttpRequest request =
        transport.createRequestFactory().buildGetRequest(HttpTesting.SIMPLE_GENERIC_URL);
    HttpResponse response = request.execute();
    OutputStream outputStream = new ByteArrayOutputStream();
    response.download(outputStream);
    assertEquals(SAMPLE, outputStream.toString());
  }

  public void testContentLoggingLimit() throws IOException {
    HttpTransport transport = new MockHttpTransport() {
      @Override
      public LowLevelHttpRequest buildGetRequest(final String url) throws IOException {
        return new MockLowLevelHttpRequest() {
          @Override
          public LowLevelHttpResponse execute() throws IOException {
            MockLowLevelHttpResponse result = new MockLowLevelHttpResponse();
            result.setContent("ABC");
            result.setContentType("text/plain");
            return result;
          }
        };
      }
    };
    HttpTransport.LOGGER.setLevel(Level.CONFIG);

    HttpRequest request =
        transport.createRequestFactory().buildGetRequest(HttpTesting.SIMPLE_GENERIC_URL);
    HttpResponse response = request.execute();

    // Set the content logging limit to be equal to the length of the content.
    response.setContentLoggingLimit(3);
    Recorder recorder = new Recorder();
    HttpTransport.LOGGER.addHandler(recorder);
    response.getContent();
    assertEquals("ABC", recorder.record.getMessage());

    // Set the content logging limit to be less than the length of the content.
    response.setContentLoggingLimit(2);
    recorder = new Recorder();
    HttpTransport.LOGGER.addHandler(recorder);
    response.getContent();
    assertTrue(recorder.record == null);

    // Set the content logging limit to 0 to disable content logging.
    response.setContentLoggingLimit(0);
    recorder = new Recorder();
    HttpTransport.LOGGER.addHandler(recorder);
    response.getContent();
    assertTrue(recorder.record == null);

    // Assert that an exception is thrown if content logging limit < 0.
    try {
      response.setContentLoggingLimit(-1);
      fail("Expected: " + IllegalArgumentException.class);
    } catch (IllegalArgumentException e) {
      // Expected.
    }
  }
}
