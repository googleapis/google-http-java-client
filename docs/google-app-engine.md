---
title: Using the Google HTTP Client Library for Java on Google App Engine
---

# Using the Google HTTP Client Library for Java on Google App Engine

Google App Engine is one of the supported Java environments for the Google HTTP Client Library for Java.

## Data models

### JSON

The [JSON data model](https://googleapis.dev/java/google-http-client/latest/index.html?com/google/api/client/json/package-summary.html) is optimized for efficient memory usage that minimizes parsing and serialization time. Only the fields you need are actually parsed when processing a JSON response.

For your JSON parser, we recommend [`JacksonFactory`](https://googleapis.dev/java/google-http-client/latest/index.html?com/google/api/client/json/jackson2/JacksonFactory.html), which is based on the popular Jackson library. It is considered the fastest in terms of parsing/serialization. You can also use [`GsonFactory'](https://googleapis.dev/java/google-http-client/latest/index.html?com/google/api/client/json/gson/GsonFactory.html), which is based on the [Google GSON](https://github.com/google/gson) library. It is a lighter-weight option (smaller size) that is fairly fast, but it is not quite as fast as Jackson.

### XML (@Beta)

The [XML datamodel](https://googleapis.dev/java/google-http-client/latest/index.html?com/google/api/client/xml/package-summary.html) (`@Beta`) is optimized for efficient memory usage that minimizes parsing and serialization time. Only the fields you need are actually parsed when processing an XML response.

## HTTP transport

If you have configured Google App Engine to use [`urlfetch` as the stream handler](https://cloud.google.com/appengine/docs/standard/java/issue-requests#using_urlfetch_in_a_java_8_app), then you will use the [`UrlFetchTransport`](https://googleapis.dev/java/google-http-client/latest/index.html?com/google/api/client/extensions/appengine/http/UrlFetchTransport.html) provided by `google-http-client-appengine`.

If you are not using `urlfetch`, then you can use any of the provided [HttpTransport](https://github.com/googleapis/google-http-java-client/wiki/HTTP-Transport) adapters.