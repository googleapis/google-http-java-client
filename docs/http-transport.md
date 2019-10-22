---
title: Pluggable HTTP Transport
---

# Pluggable HTTP Transport

The HTTP library has a fully pluggable HTTP transport layer that allows you to build on top of the
low-level HTTP of your choice and optimize for the Java platform your application is running on.

Thanks to this abstraction, code written for one platform works across all supported platforms, from
mobile applications such as those built for Android, to installed applications, to web applications
such as those built on Google App Engine. The HTTP library provides high-level functionality that is
compatible across these platforms, but at the same time takes advantage of lower-level functionality
when necessary.

## Choosing a low-level HTTP transport library

There are three built-in low-level HTTP transports:

1. [`NetHttpTransport`][net-http-transport]: based on [`HttpURLConnection`][http-url-connection]
that is found in all Java SDKs, and thus usually the simplest choice.
1. [`ApacheHttpTransport`][apache-http-transport]: based on the popular
[Apache HttpClient][apache-http-client] that allows for more customization.
1. [`UrlFetchTransport`][url-fetch-transport]: based on the [URL Fetch Java API][url-fetch] in the
Google App Engine SDK.

## Logging

[`java.util.logging.Logger`][logger] is used for logging HTTP request and response details,
including URL, headers, and content.

Normally logging is managed using a [`logging.properties`][logging-properties] file. For example:

```properties
# Properties file which configures the operation of the JDK logging facility.
# The system will look for this config file to be specified as a system property:
# -Djava.util.logging.config.file=${project_loc:googleplus-simple-cmdline-sample}/logging.properties

# Set up the console handler (uncomment "level" to show more fine-grained messages)
handlers = java.util.logging.ConsoleHandler
java.util.logging.ConsoleHandler.level = CONFIG

# Set up logging of HTTP requests and responses (uncomment "level" to show)
com.google.api.client.http.level = CONFIG
```

The following example uses the [`ConsoleHandler`][console-handler]. Another popular choice is
[`FileHandler`][file-handler].

Example for enabling logging in code:

```java
import com.google.api.client.http.HttpTransport;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

public static void enableLogging() {
  Logger logger = Logger.getLogger(HttpTransport.class.getName());
  logger.setLevel(Level.CONFIG);
  logger.addHandler(new Handler() {

    @Override
    public void close() throws SecurityException {
    }

    @Override
    public void flush() {
    }

    @Override
    public void publish(LogRecord record) {
      // Default ConsoleHandler will print >= INFO to System.err.
      if (record.getLevel().intValue() < Level.INFO.intValue()) {
        System.out.println(record.getMessage());
      }
    }
  });
}
```

**Note:** When using `Level.CONFIG`, the value of the Authorization header is not shown. To show
that also, use `Level.ALL` instead of `Level.CONFIG`.

## Handling HTTP error responses

When an HTTP error response (an HTTP status code of 300 or higher) is received,
[`HttpRequest.execute()`][request-execute] throws an [`HttpResponseException`][response-exception].
Here's an example usage:

```java
try {
  request.execute()
} catch (HttpResponseException e) {
  System.err.println(e.getStatusMessage());
}
```

If you need to intercept error responses, it may be handy to use the
[`HttpUnsuccessfulResponseHandler`][http-unsuccessful-response-handler]. Example usage:

```java
public static class MyInitializer implements HttpRequestInitializer, HttpUnsuccessfulResponseHandler {

  @Override
  public boolean handleResponse(
      HttpRequest request, HttpResponse response, boolean retrySupported) throws IOException {
    System.out.println(response.getStatusCode() + " " + response.getStatusMessage());
    return false;
  }

  @Override
  public void initialize(HttpRequest request) throws IOException {
    request.setUnsuccessfulResponseHandler(this);
  }
}

...

HttpRequestFactory requestFactory = transport.createRequestFactory(new MyInitializer());
```

[net-http-transport]: https://googleapis.dev/java/google-http-client/latest/index.html?com/google/api/client/http/javanet/NetHttpTransport.html
[http-url-connection]: http://docs.oracle.com/javase/7/docs/api/java/net/HttpURLConnection.html
[apache-http-transport]: https://googleapis.dev/java/google-http-client/latest/index.html?com/google/api/client/http/apache/v2/ApacheHttpTransport.html
[apache-http-client]: http://hc.apache.org/httpcomponents-client-ga/index.html
[url-fetch-transport]: https://googleapis.dev/java/google-http-client/latest/index.html?com/google/api/client/extensions/appengine/http/UrlFetchTransport.html
[url-fetch]: https://cloud.google.com/appengine/docs/java/javadoc/com/google/appengine/api/urlfetch/package-summary
[logger]: https://docs.oracle.com/javase/7/docs/api/java/util/logging/Logger.html
[logging-properties]: https://github.com/google/google-http-java-client/blob/master/samples/googleplus-simple-cmdline-sample/logging.properties
[console-handler]: https://docs.oracle.com/javase/7/docs/api/java/util/logging/ConsoleHandler.html
[file-handler]: https://docs.oracle.com/javase/7/docs/api/java/util/logging/FileHandler.html
[request-execute]: https://googleapis.dev/java/google-http-client/latest/com/google/api/client/http/HttpRequest.html#execute--
[response-exception]: https://googleapis.dev/java/google-http-client/latest/index.html?com/google/api/client/http/HttpResponseException.html
[http-unsuccessful-response-handler]: https://googleapis.dev/java/google-http-client/latest/index.html?com/google/api/client/http/HttpUnsuccessfulResponseHandler.html
