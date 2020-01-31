---
title: Component Modules
---

# Component Modules

This libraries is composed of several modules:

## google-http-client

Google HTTP Client Library for Java (google-http-client) is designed to be compatible with all
supported Java platforms, including Android.

## google-http-client-android

Android extensions to the Google HTTP Client Library for Java (`google-http-client-android`) support
Java Google Android (only for SDK >= 2.1) applications. This module depends on `google-http-client`.

## google-http-client-apache-v2

Apache extension to the Google HTTP Client Library for Java (`google-http-client-apache-v2`) that
contains an implementation of `HttpTransport` based on the Apache HTTP Client. This module depends
on `google-http-client`.

## google-http-client-appengine

Google App Engine extensions to the Google HTTP Client Library for Java
(`google-http-client-appengine`) support Java Google App Engine applications. This module depends on
`google-http-client`.

## google-http-client-gson

GSON extension to the Google HTTP Client Library for Java (`google-http-client-gson`) that contains
an implementation of `JsonFactory` based on the GSON API. This module depends on google-http-client.

## google-http-client-jackson2

Jackson2 extension to the Google HTTP Client Library for Java (`google-http-client-jackson2`) that
contains an implementation of `JsonFactory` based on the Jackson2 API. This module depends on
`google-http-client`.

## google-http-client-protobuf

[Protocol buffer][protobuf] extensions to the Google HTTP Client Library for Java
(`google-http-client-protobuf`) support protobuf data format. This module depends on `google-http-client`.

## google-http-client-xml

XML extensions to the Google HTTP Client Library for Java (`google-http-client-xml`) support the XML
data format. This module depends on `google-http-client`.

[protobuf]: https://developers.google.com/protocol-buffers/docs/overview
