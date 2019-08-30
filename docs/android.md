---
title: Using the Google HTTP Client Library for Java on Android
---

# Using the Google HTTP Client Library for Java on Android

If you are developing for Android and the Google API you want to use is included in the 
[Google Play Services library](https://developer.android.com/google/play-services/index.html), use 
that library for the best performance and experience. If the Google API you want to use with Android 
is not part of the Google Play Services library, you can use the Google HTTP Client Library for 
Java, which supports Android 1.5 (or higher), and which is described here.

## Beta

Android support for the Google HTTP Client Library for Java is `@Beta`.

## Installation

Follow the download instructions on the [setup][setup] page, and pay special attention to the 
Android instructions for [ProGuard][proguard]. Using ProGuard or a similar tool to remove unused 
code and compress it is critical for minimizing application size. For example, for the 
[tasks-android-sample][tasks-android-sample], ProGuard reduces the application size ~88%, from 
777KB to 93KB.

Note that ProGuard only runs when preparing your application for release; it does not run when 
preparing it for debugging, to make it easier to develop. However, be sure to test your application 
in release mode, because if ProGuard is misconfigured it can cause problems that are sometimes a 
challenge to debug.

**Warning:** For Android, you MUST place the jar files in a directory named "libs" so that the APK 
packager can find them. Otherwise, you will get a `NoClassDefFoundError` error at runtime.

## Data models

### JSON

You have a choice of three [pluggable streaming JSON libraries][json]. Options include 
[`JacksonFactory`][jackson-factory] for maximum efficiency, or 
[`AndroidJsonFactory`][android-json-factory] for the smallest application size on Honeycomb 
(SDK 3.0) or higher.

### XML (`@Beta`)

The [XML data model][xml] (`@Beta`) is optimized for efficient memory usage that minimizes parsing 
and serialization time. Only the fields you need are actually parsed when processing an XML 
response.

Android already has an efficient, native, built-in XML full parser implementation, so no separate 
library is needed or advised.

## Authentication

The best practice on Android (since the 2.1 SDK) is to use the [`AccountManager`][account-manager] 
class (@Beta) for centralized identity management and credential token storage. We recommend against 
using your own solution for storing user identities and credentials.

For details about using the AccountManager with the HTTP service that you need, read the 
documentation for that service.

## HTTP transport

If your application is targeted at Android 2.3 (Gingerbread) or higher, use the 
[`NetHttpTransport`][net-http-transport] class. This class isbased on `HttpURLConnection`, which is 
built into the Android SDK and is found in all Java SDKs.

In prior Android SDKs, however, the implementation of `HttpURLConnection` was buggy, and the Apache 
HTTP client was preferred. For those SDKs, use the [`ApacheHttpTransport`][apache-http-transport] 
class.

If your Android application needs to work with all Android SDKs, call 
[`AndroidHttp.newCompatibleTransport()`][android-transport] (@Beta), and it will decide which of the 
two HTTP transport classes to use, based on the Android SDK level.

## Logging

To enable logging of HTTP requests and responses, including URL, headers, and content:

```java
Logger.getLogger(HttpTransport.class.getName()).setLevel(Level.CONFIG);
```

When you use `Level.CONFIG`, the value of the Authorization header is not shown. To show the 
Authorization header, use `Level.ALL`.

Furthermore, you must enable logging on your device as follows:

```java
adb shell setprop log.tag.HttpTransport DEBUG
```

[setup]: https://googleapis.github.io/google-http-java-client/setup.html
[proguard]: https://googleapis.github.io/google-http-java-client/setup.html#proguard
[tasks-android-sample]: https://github.com/google/google-api-java-client-samples/tree/master/tasks-android-sample
[json]: https://googleapis.github.io/google-http-java-client/json.html
[jackson-factory]: https://googleapis.dev/java/google-http-client/latest/index.html?com/google/api/client/json/jackson2/JacksonFactory.html
[android-json-factory]: https://googleapis.dev/java/google-http-client/latest/index.html?com/google/api/client/extensions/android/json/AndroidJsonFactory.html
[xml]: https://googleapis.dev/java/google-http-client/latest/index.html?com/google/api/client/xml/package-summary.html
[account-manager]: http://developer.android.com/reference/android/accounts/AccountManager.html
[net-http-transport]: https://googleapis.dev/java/google-http-client/latest/index.html?com/google/api/client/http/javanet/NetHttpTransport.html
[apache-http-transport]: https://googleapis.dev/java/google-http-client/latest/index.html?com/google/api/client/http/apache/ApacheHttpTransport.html
[android-transport]: https://googleapis.dev/java/google-http-client/latest/index.html?com/google/api/client/extensions/android/http/AndroidHttp.html#newCompatibleTransport--