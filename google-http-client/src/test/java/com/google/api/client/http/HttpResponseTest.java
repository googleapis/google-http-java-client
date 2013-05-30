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

import com.google.api.client.json.Json;
import com.google.api.client.testing.http.HttpTesting;
import com.google.api.client.testing.http.MockHttpTransport;
import com.google.api.client.testing.http.MockLowLevelHttpRequest;
import com.google.api.client.testing.http.MockLowLevelHttpResponse;
import com.google.api.client.testing.util.LogRecordingHandler;
import com.google.api.client.testing.util.TestableByteArrayInputStream;
import com.google.api.client.util.Key;

import junit.framework.TestCase;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.Type;
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

  public void testParseAsString_none() throws Exception {
    HttpTransport transport = new MockHttpTransport();
    HttpRequest request =
        transport.createRequestFactory().buildGetRequest(HttpTesting.SIMPLE_GENERIC_URL);
    HttpResponse response = request.execute();
    assertEquals("", response.parseAsString());
  }

  private static final String SAMPLE = "123\u05D9\u05e0\u05D9\u05D1";
  private static final String SAMPLE2 = "123abc";

  public void testParseAsString_utf8() throws Exception {
    HttpTransport transport = new MockHttpTransport() {
      @Override
      public LowLevelHttpRequest buildRequest(String method, String url) throws IOException {
        return new MockLowLevelHttpRequest() {
          @Override
          public LowLevelHttpResponse execute() throws IOException {
            MockLowLevelHttpResponse result = new MockLowLevelHttpResponse();
            result.setContentType(Json.MEDIA_TYPE);
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

  public void testParseAsString_noContentType() throws Exception {
    HttpTransport transport = new MockHttpTransport() {
      @Override
      public LowLevelHttpRequest buildRequest(String method, String url) throws IOException {
        return new MockLowLevelHttpRequest() {
          @Override
          public LowLevelHttpResponse execute() throws IOException {
            MockLowLevelHttpResponse result = new MockLowLevelHttpResponse();
            result.setContent(SAMPLE2);
            return result;
          }
        };
      }
    };
    HttpRequest request =
        transport.createRequestFactory().buildGetRequest(HttpTesting.SIMPLE_GENERIC_URL);
    HttpResponse response = request.execute();
    assertEquals(SAMPLE2, response.parseAsString());
  }

  public void testStatusCode_negative_dontThrowException() throws Exception {
    subtestStatusCode_negative(false);
  }

  public void testStatusCode_negative_throwException() throws Exception {
    subtestStatusCode_negative(true);
  }

  private void subtestStatusCode_negative(boolean throwExceptionOnExecuteError) throws Exception {
    HttpTransport transport = new MockHttpTransport() {
      @Override
      public LowLevelHttpRequest buildRequest(String method, String url) throws IOException {
        return new MockLowLevelHttpRequest().setResponse(
            new MockLowLevelHttpResponse().setStatusCode(-1));
      }
    };
    HttpRequest request =
        transport.createRequestFactory().buildGetRequest(HttpTesting.SIMPLE_GENERIC_URL);
    request.setThrowExceptionOnExecuteError(throwExceptionOnExecuteError);
    try {
      // HttpResponse converts a negative status code to zero
      HttpResponse response = request.execute();
      assertEquals(0, response.getStatusCode());
      assertFalse(throwExceptionOnExecuteError);
    } catch (HttpResponseException e) {
      // exception should be thrown only if throwExceptionOnExecuteError is true
      assertTrue(throwExceptionOnExecuteError);
      assertEquals(0, e.getStatusCode());
    }
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

  public void testHeaderParsing() throws Exception {
    HttpTransport transport = new MockHttpTransport() {
      @Override
      public LowLevelHttpRequest buildRequest(String method, String url) throws IOException {
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

  public void testParseAs_noParser() throws Exception {
    try {
      new MockHttpTransport().createRequestFactory()
          .buildGetRequest(HttpTesting.SIMPLE_GENERIC_URL).execute().parseAs(Object.class);
      fail("expected " + NullPointerException.class);
    } catch (NullPointerException e) {
      // expected
    }
  }

  public void testParseAs_classNoContent() throws Exception {
    final MockLowLevelHttpResponse result = new MockLowLevelHttpResponse();

    for (final int status : new int[] {
        HttpStatusCodes.STATUS_CODE_NO_CONTENT, HttpStatusCodes.STATUS_CODE_NOT_MODIFIED, 102}) {
      HttpTransport transport = new MockHttpTransport() {
        @Override
        public LowLevelHttpRequest buildRequest(String method, final String url) throws IOException {
          return new MockLowLevelHttpRequest() {
            @Override
            public LowLevelHttpResponse execute() throws IOException {
              result.setStatusCode(status);
              result.setContentType(null);
              result.setContent(new ByteArrayInputStream(new byte[0]));
              return result;
            }
          };
        }
      };

      // Confirm that 'null' is returned when getting the response object of a
      // request with no message body.
      Object parsed = transport.createRequestFactory()
          .buildGetRequest(HttpTesting.SIMPLE_GENERIC_URL)
          .setThrowExceptionOnExecuteError(false)
          .execute()
          .parseAs(Object.class);
      assertNull(parsed);
    }
  }

  public void testParseAs_typeNoContent() throws Exception {
    final MockLowLevelHttpResponse result = new MockLowLevelHttpResponse();

    for (final int status : new int[] {
        HttpStatusCodes.STATUS_CODE_NO_CONTENT, HttpStatusCodes.STATUS_CODE_NOT_MODIFIED, 102}) {
      HttpTransport transport = new MockHttpTransport() {
        @Override
        public LowLevelHttpRequest buildRequest(String method, final String url) throws IOException {
          return new MockLowLevelHttpRequest() {
            @Override
            public LowLevelHttpResponse execute() throws IOException {
              result.setStatusCode(status);
              result.setContentType(null);
              result.setContent(new ByteArrayInputStream(new byte[0]));
              return result;
            }
          };
        }
      };

      // Confirm that 'null' is returned when getting the response object of a
      // request with no message body.
      Object parsed = transport.createRequestFactory()
          .buildGetRequest(HttpTesting.SIMPLE_GENERIC_URL)
          .setThrowExceptionOnExecuteError(false)
          .execute()
          .parseAs((Type) Object.class);
      assertNull(parsed);
    }
  }

  public void testDownload() throws Exception {
    HttpTransport transport = new MockHttpTransport() {
      @Override
      public LowLevelHttpRequest buildRequest(String method, String url) throws IOException {
        return new MockLowLevelHttpRequest() {
          @Override
          public LowLevelHttpResponse execute() throws IOException {
            MockLowLevelHttpResponse result = new MockLowLevelHttpResponse();
            result.setContentType(Json.MEDIA_TYPE);
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

  public void testDisconnectWithContent() throws Exception {
    final MockLowLevelHttpResponse lowLevelHttpResponse =
        new MockLowLevelHttpResponse();

    HttpTransport transport = new MockHttpTransport() {
      @Override
      public LowLevelHttpRequest buildRequest(String method, String url) throws IOException {
        return new MockLowLevelHttpRequest() {
          @Override
          public LowLevelHttpResponse execute() throws IOException {
            lowLevelHttpResponse.setContentType(Json.MEDIA_TYPE);
            lowLevelHttpResponse.setContent(SAMPLE);
            return lowLevelHttpResponse;
          }
        };
      }
    };
    HttpRequest request =
        transport.createRequestFactory().buildGetRequest(HttpTesting.SIMPLE_GENERIC_URL);
    HttpResponse response = request.execute();

    assertFalse(lowLevelHttpResponse.isDisconnected());
    TestableByteArrayInputStream content =
        (TestableByteArrayInputStream) lowLevelHttpResponse.getContent();
    assertFalse(content.isClosed());
    response.disconnect();
    assertTrue(lowLevelHttpResponse.isDisconnected());
    assertTrue(content.isClosed());
  }

  public void testDisconnectWithNoContent() throws Exception {
    final MockLowLevelHttpResponse lowLevelHttpResponse =
        new MockLowLevelHttpResponse();

    HttpTransport transport = new MockHttpTransport() {
      @Override
      public LowLevelHttpRequest buildRequest(String method, String url) throws IOException {
        return new MockLowLevelHttpRequest() {
          @Override
          public LowLevelHttpResponse execute() throws IOException {
            return lowLevelHttpResponse;
          }
        };
      }
    };
    HttpRequest request =
        transport.createRequestFactory().buildGetRequest(HttpTesting.SIMPLE_GENERIC_URL);
    HttpResponse response = request.execute();

    assertFalse(lowLevelHttpResponse.isDisconnected());
    response.disconnect();
    assertTrue(lowLevelHttpResponse.isDisconnected());
  }

  public void testContentLoggingLimitWithLoggingEnabledAndDisabled() throws Exception {
    subtestContentLoggingLimit("", 2, false);
    subtestContentLoggingLimit("A", 2, false);
    subtestContentLoggingLimit("ABC" + '\0' + "DEF", 20, true, "Total: 7 bytes", "ABC DEF");
    subtestContentLoggingLimit("A", 2, true, "Total: 1 byte", "A");
    try {
      subtestContentLoggingLimit("ABC", -1, true);
      fail("Expected: " + IllegalArgumentException.class);
    } catch (IllegalArgumentException e) {
      // Expected.
    }
    subtestContentLoggingLimit("ABC", 0, true, "Total: 3 bytes");
    subtestContentLoggingLimit("ABC", 2, true, "Total: 3 bytes (logging first 2 bytes)", "AB");
    subtestContentLoggingLimit("ABC", 3, true, "Total: 3 bytes", "ABC");
    subtestContentLoggingLimit("ABC", 4, true, "Total: 3 bytes", "ABC");
    char[] a = new char[18000];
    Arrays.fill(a, 'x');
    String big = new String(a);
    subtestContentLoggingLimit(big, Integer.MAX_VALUE, true, "Total: 18,000 bytes", big);
    subtestContentLoggingLimit(big, 4, true, "Total: 18,000 bytes (logging first 4 bytes)", "xxxx");
  }

  public void subtestContentLoggingLimit(final String content, int contentLoggingLimit,
      boolean loggingEnabled, String... expectedMessages) throws Exception {
    HttpTransport transport = new MockHttpTransport() {
      @Override
      public LowLevelHttpRequest buildRequest(String method, final String url) throws IOException {
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
    request.setLoggingEnabled(loggingEnabled);
    HttpResponse response = request.execute();
    assertEquals(loggingEnabled, response.isLoggingEnabled());

    response.setContentLoggingLimit(contentLoggingLimit);
    LogRecordingHandler recorder = new LogRecordingHandler();
    HttpTransport.LOGGER.addHandler(recorder);
    response.parseAsString();
    assertEquals(Arrays.asList(expectedMessages), recorder.messages());
  }

  public void testGetContent_gzipNoContent() throws IOException {
    HttpTransport transport = new MockHttpTransport() {
      @Override
      public LowLevelHttpRequest buildRequest(String method, final String url) throws IOException {
        return new MockLowLevelHttpRequest() {
          @Override
          public LowLevelHttpResponse execute() throws IOException {
            MockLowLevelHttpResponse result = new MockLowLevelHttpResponse();
            result.setContent("");
            result.setContentEncoding("gzip");
            result.setContentType("text/plain");
            return result;
          }
        };
      }
    };
    HttpRequest request =
        transport.createRequestFactory().buildHeadRequest(HttpTesting.SIMPLE_GENERIC_URL);
    request.execute().getContent();
  }
}
