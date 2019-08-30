---
title: HTTP Unit Testing
---

# HTTP Unit Testing

When writing unit tests using this HTTP framework, don't make requests to a real server. Instead, mock the HTTP transport and inject fake HTTP requests and responses. The [pluggable HTTP transport layer](https://github.com/googleapis/google-http-java-client/wiki/HTTP-Transport) of the Google HTTP Client Library for Java makes this flexible and simple to do.

Also, some useful testing utilities are included in the [`com.google.api.client.testing.http`](https://googleapis.dev/java/google-http-client/latest/index.html?com/google/api/client/testing/http/package-summary.html) package (`@Beta`).

The following simple example generates a basic `HttpResponse`:

```java
HttpTransport transport = new MockHttpTransport();
HttpRequest request = transport.createRequestFactory().buildGetRequest(HttpTesting.SIMPLE_GENERIC_URL);
HttpResponse response = request.execute();
```

The following example shows how to override the implementation of the `MockHttpTransport` class:

```java
HttpTransport transport = new MockHttpTransport() {
  @Override
  public LowLevelHttpRequest buildRequest(String method, String url) throws IOException {
    return new MockLowLevelHttpRequest() {
      @Override
      public LowLevelHttpResponse execute() throws IOException {
        MockLowLevelHttpResponse response = new MockLowLevelHttpResponse();
        response.addHeader("custom_header", "value");
        response.setStatusCode(404);
        response.setContentType(Json.MEDIA_TYPE);
        response.setContent("{\"error\":\"not found\"}");
        return response;
      }
    };
  }
};
HttpRequest request = transport.createRequestFactory().buildGetRequest(HttpTesting.SIMPLE_GENERIC_URL);
HttpResponse response = request.execute();
```

For more examples, see the [`HttpResponseTest.java`](https://github.com/googleapis/google-http-java-client/blob/master/google-http-client/src/test/java/com/google/api/client/http/HttpResponseTest.java) and [`HttpRequestTest.java`](https://github.com/googleapis/google-http-java-client/blob/master/google-http-client/src/test/java/com/google/api/client/http/HttpRequestTest.java) files.