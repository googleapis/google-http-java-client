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

  static final String ETAG_VALUE = "\"abc\"";

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
            result.addHeader("ETAG", ETAG_VALUE);
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
    assertEquals(ETAG_VALUE, response.getHeaders().getETag());
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
      transport.createRequestFactory()
          .buildGetRequest(HttpTesting.SIMPLE_GENERIC_URL).execute().parseAs(Object.class);
      fail("expected " + IllegalArgumentException.class);
    } catch (IllegalArgumentException e) {
      assertEquals(e.getMessage(), "Missing Content-Type header in response");
    }
    try {
      transport.createRequestFactory()
          .buildGetRequest(new GenericUrl(HttpTesting.SIMPLE_URL + "something")).execute()
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
    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    response.download(outputStream);
    assertEquals(SAMPLE, outputStream.toString("UTF-8"));
  }

  public void testContentLoggingLimit() throws IOException {
    subtestContentLoggingLimit("ABC" + '\0' + "DEF", 20, "Total: 7 bytes", "ABC DEF");
    subtestContentLoggingLimit("", 2);
    subtestContentLoggingLimit("A", 2, "Total: 1 byte", "A");
    try {
      subtestContentLoggingLimit("ABC", -1);
      fail("Expected: " + IllegalArgumentException.class);
    } catch (IllegalArgumentException e) {
      // Expected.
    }
    subtestContentLoggingLimit("ABC", 0, "Total: 3 bytes");
    subtestContentLoggingLimit("ABC", 2, "Total: 3 bytes (logging first 2 bytes)", "AB");
    subtestContentLoggingLimit("ABC", 3, "Total: 3 bytes", "ABC");
    subtestContentLoggingLimit("ABC", 4, "Total: 3 bytes", "ABC");
    char[] a = new char[18000];
    Arrays.fill(a, 'x');
    String big = new String(a);
    subtestContentLoggingLimit(big, Integer.MAX_VALUE, "Total: 18,000 bytes", big);
    subtestContentLoggingLimit(big, 4, "Total: 18,000 bytes (logging first 4 bytes)", "xxxx");
  }

  public void subtestContentLoggingLimit(
      final String content, int contentLoggingLimit, String... expectedMessages)
      throws IOException {
    HttpTransport transport = new MockHttpTransport() {
      @Override
      public LowLevelHttpRequest buildGetRequest(final String url) throws IOException {
        return new MockLowLevelHttpRequest() {
          @Override
          public LowLevelHttpResponse execute() throws IOException {
            MockLowLevelHttpResponse result = new MockLowLevelHttpResponse();
            result.setContent(content);
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

    response.setContentLoggingLimit(contentLoggingLimit);
    Recorder recorder = new Recorder();
    HttpTransport.LOGGER.addHandler(recorder);
    response.parseAsString();
    recorder.assertMessages(expectedMessages);
  }
}
