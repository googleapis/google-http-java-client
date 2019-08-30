---
title: Exponential Backoff
---

# Exponential Backoff

Exponential backoff is an algorithm that retries requests to the server based on certain status codes in the server response. The retries exponentially increase the waiting time up to a certain threshold. The idea is that if the server is down temporarily, it is not overwhelmed with requests hitting at the same time when it comes back up.

The exponential backoff feature of the Google HTTP Client Library for Java provides an easy way to retry on transient failures:

* Provide an instance of [`HttpUnsuccessfulResponseHandler`](https://googleapis.dev/java/google-http-client/latest/index.html?com/google/api/client/http/HttpUnsuccessfulResponseHandler.html) to the HTTP request in question.
* Use the library's [`HttpBackOffUnsuccessfulResponseHandler`](https://googleapis.dev/java/google-http-client/latest/index.html?com/google/api/client/http/HttpBackOffUnsuccessfulResponseHandler.html) implementation to handle abnormal HTTP responses with some kind of [`BackOff`](https://googleapis.dev/java/google-http-client/latest/index.html?com/google/api/client/util/BackOff.html) policy.
* Use [`ExponentialBackOff`](https://googleapis.dev/java/google-http-client/latest/index.html?com/google/api/client/util/ExponentialBackOff.html) for this backoff policy.

Backoff is turned off by default in [`HttpRequest`](https://googleapis.dev/java/google-http-client/latest/index.html?com/google/api/client/http/HttpRequest.html). The examples below demonstrate how to turn it on.

## Examples

To set [`HttpRequest`](https://googleapis.dev/java/google-http-client/latest/index.html?com/google/api/client/http/HttpRequest.html) to use [`HttpBackOffUnsuccessfulResponseHandler`](https://googleapis.dev/java/google-http-client/latest/index.html?com/google/api/client/http/HttpBackOffUnsuccessfulResponseHandler.html) with default values:

```java
HttpRequest request = ...
request.setUnsuccessfulResponseHandler(new HttpBackOffUnsuccessfulResponseHandler(new ExponentialBackOff()));
// HttpBackOffUnsuccessfulResponseHandler is designed to work with only one HttpRequest at a time.
// As a result, you MUST create a new instance of HttpBackOffUnsuccessfulResponseHandler with a new
// instance of BackOff for each instance of HttpRequest.
HttpResponse = request.execute();
```

To alter the detailed parameters of [`ExponentialBackOff`](https://googleapis.dev/java/google-http-client/latest/index.html?com/google/api/client/util/ExponentialBackOff.html), use its [`Builder`](https://googleapis.dev/java/google-http-client/latest/index.html?com/google/api/client/util/ExponentialBackOff.Builder.html) methods:

```java
ExponentialBackOff backoff = new ExponentialBackOff.Builder()
    .setInitialIntervalMillis(500)
    .setMaxElapsedTimeMillis(900000)
    .setMaxIntervalMillis(6000)
    .setMultiplier(1.5)
    .setRandomizationFactor(0.5)
    .build();
request.setUnsuccessfulResponseHandler(new HttpBackOffUnsuccessfulResponseHandler(backoff));
```

To create your own implementation of [`BackOff`](https://googleapis.dev/java/google-http-client/latest/index.html?com/google/api/client/util/BackOff.html):

```java
class CustomBackOff implements BackOff {

  @Override
  public long nextBackOffMillis() throws IOException {
    ...
  }

  @Override
  public void reset() throws IOException {
    ...
  }
}

request.setUnsuccessfulResponseHandler(
    new HttpBackOffUnsuccessfulResponseHandler(new CustomBackOff()));
```