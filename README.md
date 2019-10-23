# Google HTTP Client Library for Java

## Description
Written by Google, the Google HTTP Client Library for Java is a flexible, efficient, and powerful
Java library for accessing any resource on the web via HTTP. The library has the following
features:

- Pluggable HTTP transport abstraction that allows you to use any low-level library such as
java.net.HttpURLConnection, Apache HTTP Client, or URL Fetch on Google App Engine.
- Efficient JSON and XML data models for parsing and serialization of HTTP response and request
content. The JSON and XML libraries are also fully pluggable, and they include support for
[Jackson](https://github.com/FasterXML/jackson) and Android's GSON libraries for JSON.

The library supports the following Java environments:

- Java 7 (or higher)
- Android 4.0 (Ice Cream Sandwich) (or higher)
- GoogleAppEngine Google App Engine

The following related projects are built on the Google HTTP Client Library for Java:

- [Google OAuth Client Library for Java][google-oauth-client], for the OAuth 2.0 and OAuth 1.0a
authorization standards.
- [Google APIs Client Library for Java][google-api-client], for access to Google APIs.

This is an open-source library, and [contributions][contributions] are welcome.

## Beta Features

Features marked with the `@Beta` annotation at the class or method level are subject to change. They
might be modified in any way, or even removed, in any major release. You should not use beta
features if your code is a library itself (that is, if your code is used on the `CLASSPATH` of users
outside your own control).

## Deprecated Features

Deprecated non-beta features will be removed eighteen months after the release in which they are
first deprecated. You must fix your usages before this time. If you don't, any type of breakage
might result, and you are not guaranteed a compilation error.

## Documentation

- [Developer's Guide](https://googleapis.github.io/google-http-java-client/)
- [Setup Instructions](https://googleapis.github.io/google-http-java-client/setup.html)
- [JavaDoc](https://googleapis.dev/java/google-http-client/latest/)
- [Release Notes](https://github.com/googleapis/google-http-java-client/releases)
- [Support (Questions, Bugs)](https://developers.google.com/api-client-library/java/google-http-java-client/support)

## CI Status

Java Version | Status
------------ | ------
Java 7 | [![Kokoro CI](https://storage.googleapis.com/cloud-devrel-public/java/badges/google-http-java-client/java7.svg)](https://storage.googleapis.com/cloud-devrel-public/java/badges/google-http-java-client/java7.html)
Java 8 | [![Kokoro CI](https://storage.googleapis.com/cloud-devrel-public/java/badges/google-http-java-client/java8.svg)](https://storage.googleapis.com/cloud-devrel-public/java/badges/google-http-java-client/java8.html)
Java 11 | [![Kokoro CI](https://storage.googleapis.com/cloud-devrel-public/java/badges/google-http-java-client/java11.svg)](https://storage.googleapis.com/cloud-devrel-public/java/badges/google-http-java-client/java11.html)


[google-oauth-client]: https://github.com/googleapis/google-oauth-java-client
[google-api-client]: https://github.com/googleapis/google-api-java-client
[contributions]: CONTRIBUTING.md
