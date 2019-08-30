---
title: Using the Google HTTP Client Library for Java on Google App Engine
---

# Using the Google HTTP Client Library for Java on Google App Engine

Google App Engine is one of the supported Java environments for the Google HTTP Client Library for Java.

## Data models

### JSON

The [JSON data model][json-package] is optimized for efficient memory usage that minimizes parsing
and serialization time. Only the fields you need are actually parsed when processing a JSON
response.

For your JSON parser, we recommend [`JacksonFactory`][jackson-factory], which is based on the
popular Jackson library. It is considered the fastest in terms of parsing/serialization. You can
also use [`GsonFactory'][gson-factory], which is based on the [Google GSON][gson] library. It is a
lighter-weight option (smaller size) that is fairly fast, but it is not quite as fast as Jackson.

### XML (@Beta)

The [XML datamodel][xml-package] (`@Beta`) is optimized for efficient memory usage that minimizes
parsing and serialization time. Only the fields you need are actually parsed when processing an XML
response.

## HTTP transport

If you have configured Google App Engine to use [`urlfetch` as the stream handler][url-fetch], then
you will use the [`UrlFetchTransport`][url-fetch-transport] provided by
`google-http-client-appengine`.

If you are not using `urlfetch`, then you can use any of the provided
[HttpTransport][http-transport] adapters.

[json-package]: https://googleapis.dev/java/google-http-client/latest/index.html?com/google/api/client/json/package-summary.html
[jackson-factory]: https://googleapis.dev/java/google-http-client/latest/index.html?com/google/api/client/json/jackson2/JacksonFactory.html
[gson-factory]: https://googleapis.dev/java/google-http-client/latest/index.html?com/google/api/client/json/gson/GsonFactory.html
[gson]: https://github.com/google/gson
[xml-package]: https://googleapis.dev/java/google-http-client/latest/index.html?com/google/api/client/xml/package-summary.html
[url-fetch]: https://cloud.google.com/appengine/docs/standard/java/issue-requests#using_urlfetch_in_a_java_8_app
[url-fetch-transport]: https://googleapis.dev/java/google-http-client/latest/index.html?com/google/api/client/extensions/appengine/http/UrlFetchTransport.html
[http-transport]: https://googleapis.github.io/google-http-java-client/http-transport.html