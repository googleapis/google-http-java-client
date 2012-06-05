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
import com.google.api.client.util.StringUtils;

import junit.framework.TestCase;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
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
  private static final String SAMPLE2 = "123abc";

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

  public void testParseAsString_noContentType() throws IOException {
    HttpTransport transport = new MockHttpTransport() {
        @Override
      public LowLevelHttpRequest buildGetRequest(String url) throws IOException {
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
    final DisconnectLowLevelHttpResponse result = new DisconnectLowLevelHttpResponse();
    HttpTransport transport = new MockHttpTransport() {
      @Override
      public LowLevelHttpRequest buildGetRequest(final String url) throws IOException {
        return new MockLowLevelHttpRequest() {
          @Override
          public LowLevelHttpResponse execute() throws IOException {
            if (url.equals(HttpTesting.SIMPLE_URL)) {
              result.setContentType(null);
            } else {
              result.setContentType(url.substring(HttpTesting.SIMPLE_URL.length()));
            }
            result.setContent(SAMPLE);
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
      assertTrue(result.content.closeCalled);
    }
    result.clear();
    try {
      // Content-Type is specified by an URL suffix in this test
      transport.createRequestFactory()
          .buildGetRequest(new GenericUrl(HttpTesting.SIMPLE_URL + "some/thing")).execute()
          .parseAs(Object.class);
      fail("expected " + IllegalArgumentException.class);
    } catch (IllegalArgumentException e) {
      assertEquals(e.getMessage(), "No parser defined for Content-Type: some/thing");
      assertTrue(result.content.closeCalled);
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

  class DisconnectLowLevelHttpResponse extends MockLowLevelHttpResponse {
    boolean disconnectCalled;
    DisconnectByteArrayInputStream content;

    @Override
    public MockLowLevelHttpResponse setContent(String stringContent) {
      content = stringContent == null ? null : new DisconnectByteArrayInputStream(StringUtils
          .getBytesUtf8(stringContent));
      return this;
    }

    @Override
    public InputStream getContent() throws IOException {
      return content;
    }

    @Override
    public void disconnect() {
      disconnectCalled = true;
    }

    void clear() {
      disconnectCalled = false;
      content.clear();
    }
  }

  class DisconnectByteArrayInputStream extends ByteArrayInputStream {
    boolean closeCalled;

    public DisconnectByteArrayInputStream(byte[] buf) {
      super(buf);
    }

    @Override
    public void close() throws IOException {
      closeCalled = true;
    }

    void clear() {
      closeCalled = false;
    }
  }

  public void testDisconnectWithContent() throws IOException {
    final DisconnectLowLevelHttpResponse lowLevelHttpResponse =
        new DisconnectLowLevelHttpResponse();

    HttpTransport transport = new MockHttpTransport() {
        @Override
      public LowLevelHttpRequest buildGetRequest(String url) throws IOException {
        return new MockLowLevelHttpRequest() {
            @Override
          public LowLevelHttpResponse execute() throws IOException {
            lowLevelHttpResponse.setContentType("application/json; charset=UTF-8");
            lowLevelHttpResponse.setContent(SAMPLE);
            return lowLevelHttpResponse;
          }
        };
      }
    };
    HttpRequest request =
        transport.createRequestFactory().buildGetRequest(HttpTesting.SIMPLE_GENERIC_URL);
    HttpResponse response = request.execute();

    assertFalse(lowLevelHttpResponse.disconnectCalled);
    assertFalse(lowLevelHttpResponse.content.closeCalled);
    response.disconnect();
    assertTrue(lowLevelHttpResponse.disconnectCalled);
    assertTrue(lowLevelHttpResponse.content.closeCalled);
  }

  public void testDisconnectWithNoContent() throws IOException {
    final DisconnectLowLevelHttpResponse lowLevelHttpResponse =
        new DisconnectLowLevelHttpResponse();

    HttpTransport transport = new MockHttpTransport() {
        @Override
      public LowLevelHttpRequest buildGetRequest(String url) throws IOException {
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

    assertFalse(lowLevelHttpResponse.disconnectCalled);
    response.disconnect();
    assertTrue(lowLevelHttpResponse.disconnectCalled);
  }

  public void testContentLoggingLimitWithLoggingEnabledAndDisabled() throws IOException {
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
      boolean loggingEnabled, String... expectedMessages) throws IOException {
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
    request.setLoggingEnabled(loggingEnabled);
    HttpResponse response = request.execute();
    assertEquals(loggingEnabled, response.isLoggingEnabled());

    response.setContentLoggingLimit(contentLoggingLimit);
    Recorder recorder = new Recorder();
    HttpTransport.LOGGER.addHandler(recorder);
    response.parseAsString();
    recorder.assertMessages(expectedMessages);
  }
}
