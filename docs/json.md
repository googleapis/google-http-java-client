---
title: JSON
---

# JSON

## Pluggable streaming library

A fully pluggable JSON streaming library abstraction allows you to take advantage of the native
platform's built-in JSON library support (for example the JSON library that is built into Android
Honeycomb). The streaming library enables you to write optimized code for efficient memory usage
that minimizes parsing and serialization time.

A big advantage of this JSON library is that the choice of low-level streaming library is fully
pluggable. There are three built-in choices, all of which extend [`JsonFactory`][json-factory]. You
can easily plug in your own implementation.

* [`JacksonFactory`][jackson-factory]: Based on the popular [Jackson][jackson] library, which is
considered the fastest in terms of parsing/serialization speed. Our JSON library provides
`JsonFactory` implementations based on Jackson 2.
* [`GsonFactory`][gson-factory]: Based on the [Google GSON][gson] library, which is a lighter-weight
option (small size) that is also fairly fast, though not as fast as Jackson.
* [`AndroidJsonFactory`][android-json-factory] (`@Beta`): Based on the JSON library built into
Android Honeycomb (SDK 3.0) and higher, and that is identical to the Google GSON library.

## User-defined JSON data models

User-defined JSON data models allow you to define Plain Old Java Objects (POJOs) and define how the
library parses and serializes them to and from JSON. The code snippets below are part of a more
complete example, [YouTube sample][youtube-sample], which demonstrates these
concepts.

### Example

The following JSON snippet shows the relevant fields of a typical [YouTube video search][youtube-search]:

```json
{
 "kind": "youtube#searchListResponse",
 "pageInfo": {
  "totalResults": 1000000,
  "resultsPerPage": 5
 },
 "items": [
  {
   "kind": "youtube#searchResult",
   "id": {
    "kind": "youtube#video",
    "videoId": "e6Tudp5lqt8"
   },
   "snippet": {
    "publishedAt": "2020-06-25T23:18:43Z",
    "channelId": "UCKwGZZMrhNYKzucCtTPY2Nw",
    "title": "Video 1 Title",
    "description": "Video 1 Description",
    "thumbnails": {
     "default": {
      "url": "https://i.ytimg.com/vi/e6Tudp5lqt8/default.jpg",
      "width": 120,
      "height": 90
     },
     "medium": {
      "url": "https://i.ytimg.com/vi/e6Tudp5lqt8/mqdefault.jpg",
      "width": 320,
      "height": 180
     },
     "high": {
      "url": "https://i.ytimg.com/vi/e6Tudp5lqt8/hqdefault.jpg",
      "width": 480,
      "height": 360
     }
    }
   }
  },
  {
   "kind": "youtube#searchResult",
   "id": {
    "kind": "youtube#video",
    "videoId": "o-NtLpiMpw0"
   },
   "snippet": {
    "publishedAt": "2020-06-25T17:28:52Z",
    "channelId": "UClljAz6ZKy0XeViKsohdjqA",
    "title": "Video Title 2",
    "description": "Video 2 Description",
    "thumbnails": {
     "default": {
      "url": "https://i.ytimg.com/vi/o-NtLpiMpw0/default.jpg",
      "width": 120,
      "height": 90
     },
     "medium": {
      "url": "https://i.ytimg.com/vi/o-NtLpiMpw0/mqdefault.jpg",
      "width": 320,
      "height": 180
     },
     "high": {
      "url": "https://i.ytimg.com/vi/o-NtLpiMpw0/hqdefault.jpg",
      "width": 480,
      "height": 360
     }
    }
   }
  },
  {
   "kind": "youtube#searchResult",
   "id": {
    "kind": "youtube#video",
    "videoId": "TPAahzXZFZo"
   },
   "snippet": {
    "publishedAt": "2020-06-26T15:45:00Z",
    "channelId": "UCR4Yfr8HAZJd9X24dwuAt1Q",
    "title": "Video 3 Title",
    "description": "Video 3 Description",
    "thumbnails": {
     "default": {
      "url": "https://i.ytimg.com/vi/TPAahzXZFZo/default.jpg",
      "width": 120,
      "height": 90
     },
     "medium": {
      "url": "https://i.ytimg.com/vi/TPAahzXZFZo/mqdefault.jpg",
      "width": 320,
      "height": 180
     },
     "high": {
      "url": "https://i.ytimg.com/vi/TPAahzXZFZo/hqdefault.jpg",
      "width": 480,
      "height": 360
     }
    }
   }
  },
  {
   "kind": "youtube#searchResult",
   "id": {
    "kind": "youtube#video",
    "videoId": "gBL-AelsdFk"
   },
   "snippet": {
    "publishedAt": "2020-06-24T15:24:06Z",
    "channelId": "UCFHZHhZaH7Rc_FOMIzUziJA",
    "title": "Video 4 Title",
    "description": "Video 4 Description",
    "thumbnails": {
     "default": {
      "url": "https://i.ytimg.com/vi/gBL-AelsdFk/default.jpg",
      "width": 120,
      "height": 90
     },
     "medium": {
      "url": "https://i.ytimg.com/vi/gBL-AelsdFk/mqdefault.jpg",
      "width": 320,
      "height": 180
     },
     "high": {
      "url": "https://i.ytimg.com/vi/gBL-AelsdFk/hqdefault.jpg",
      "width": 480,
      "height": 360
     }
    }
   }
  },
  {
   "kind": "youtube#searchResult",
   "id": {
    "kind": "youtube#video",
    "videoId": "9ofe8axKjH0"
   },
   "snippet": {
    "publishedAt": "2020-06-26T11:59:32Z",
    "channelId": "UCtNpbO2MtsVY4qW23WfnxGg",
    "title": "Video 5 Title",
    "description": "Video 5 Description",
    "thumbnails": {
     "default": {
      "url": "https://i.ytimg.com/vi/9ofe8axKjH0/default.jpg",
      "width": 120,
      "height": 90
     },
     "medium": {
      "url": "https://i.ytimg.com/vi/9ofe8axKjH0/mqdefault.jpg",
      "width": 320,
      "height": 180
     },
     "high": {
      "url": "https://i.ytimg.com/vi/9ofe8axKjH0/hqdefault.jpg",
      "width": 480,
      "height": 360
     }
    }
   }
  }
 ]
}

```

Here's one possible way to design the Java data classes to represent this:

```java
public static class ListResponse {
  @Key("items")
  private List<SearchResult> searchResults;

  @Key
  private PageInfo pageInfo;

  public List<SearchResult> getSearchResults() {
    return searchResults;
  }

  public PageInfo getPageInfo() {
    return pageInfo;
  }
}

public static class PageInfo {
  @Key
  private long totalResults;

  @Key
  private long resultsPerPage;

  public long getTotalResults() {
    return totalResults;
  }

  public long getResultsPerPage() {
    return resultsPerPage;
  }
}

public static class SearchResult {
  @Key
  private String kind;

  @Key("id")
  private VideoId videoId;

  @Key
  private Snippet snippet;

  public String getKind() {
    return kind;
  }

  public VideoId getId() {
    return videoId;
  }

  public Snippet getSnippet() {
    return snippet;
  }
}

public static class VideoId {
  @Key
  private String kind;

  @Key
  private String videoId;

  public String getKind() {
    return kind;
  }

  public String getVideoId() {
    return videoId;
  }
}

public static class Snippet {
  @Key
  private String publishedAt;

  @Key
  private String channelId;

  @Key
  private String title;

  @Key
  private String description;

  public String getPublishedAt() {
    return publishedAt;
  }

  public String getChannelId() {
    return channelId;
  }

  public String getTitle() {
    return title;
  }

  public String getDescription() {
    return description;
  }

  public Map<String, Thumbnail> getThumbnails() {
    return thumbnails;
  }
}

public static class Thumbnail {
  @Key
  private String url;

  @Key
  private long width;

  @Key
  private long height;

  public String getUrl() {
    return url;
  }

  public long getWidth() {
    return width;
  }

  public long getHeight() {
    return height;
  }
}
```

A fully supported [HTTP JSON parser][json-parser] makes it easy to parse HTTP responses to objects
of these user defined classes:

```java
public static ListResponse parseJson(HttpResponse httpResponse) throws IOException {
  ListResponse listResponse = httpResponse.parseAs(ListResponse.class);
  if (listResponse.getSearchResults().isEmpty()) {
    System.out.println("No results found.");
  } else {
    for (SearchResult searchResult : listResponse.getSearchResults()) {
      System.out.println();
      System.out.println("-----------------------------------------------");
      System.out.println("Kind: " + searchResult.getKind());
      System.out.println("Video ID: " + searchResult.getId().getVideoId());
      System.out.println("Title: " + searchResult.getSnippet().getTitle());
      System.out.println("Description: " + searchResult.getSnippet().getDescription());
    }
  }
  return listResponse;
}
```

### Key annotation

Use the [`@Key`][key-annotation] annotation to indicate fields that need to be parsed from or
serialized to JSON. By default, the `@Key` annotation uses the Java field name as the JSON key. To
override this, specify the value of the `@Key` annotation.

Fields that don't have the `@Key` annotation are considered internal data and are not parsed from or
serialized to JSON.

### Visibility

Visibility of the fields does not matter, nor does the existence of the getter or setter methods. So
for example, the following alternative representation for `VideoId` would work in the example
given above:

```java
public static class VideoId {
  @Key
  public String kind;

  @Key
  public String videoId;
}
```

### GenericJson

Normally only the fields you declare are parsed when a JSON response is parsed. The actual Google+
activity feed response contains a lot of content that we are not using in our example. The JSON
parser skips that other content when parsing the response from Google+.

To retain the other content, declare your class to extend [`GenericJson`][generic-json]. Notice that
`GenericJson` implements [`Map`][map], so we can use the `get` and `put` methods to set JSON
content. See [`Youtube sample`][youtube-sample] for an example of how it was
used in the `Snippet` class above.

### Map

The JSON library supports any implementation of `Map`, which works similarly to `GenericJson`. The
downside, of course, is that you lose the static type information for the fields.

### JSON null

One advantage of this JSON library is its ability to support JSON nulls and distinguish them from
undeclared JSON keys. Although JSON nulls are relatively rare, when they do occur they often cause
confusion.

Google+ doesn't use JSON null values, so the following example uses fictitious JSON data to
illustrate what can happen:

```json
{
 "items": [
  {
   "id": "1",
   "value": "some value"
  },
  {
   "id": "2",
   "value": null
  }
  {
   "id": "3"
  }
 ]
}
```

We might represent each item as follows:

```java
public class Item {
  @Key
  public String id;
  @Key
  public String value;
}
```

For items 2 and 3, what should be in the value field? The problem is that there is no obvious way in
Java to distinguish between a JSON key that is undeclared and a JSON key whose value is JSON null.
This JSON library solves the problem by using Java null for the common case of an undeclared JSON
key, and a special "magic" instance of String ([`Data.NULL_STRING`][null-string]) to identify it as
a JSON null rather than a normal value.

The following example shows how you might take advantage of this functionality:

```java
private static void show(List<Item> items) {
  for (Item item : items) {
    System.out.println("ID: " + item.id);
    if (item.value == null) {
      System.out.println("No Value");
    } else if (Data.isNull(item.value)) {
      System.out.print("Null Value");
    } else {
      System.out.println("Value: '" + item.value + "'");
    }
  }
}
```

[json-factory]: https://googleapis.dev/java/google-http-client/latest/index.html?com/google/api/client/json/JsonFactory.html
[jackson-factory]: https://googleapis.dev/java/google-http-client/latest/index.html?com/google/api/client/json/jackson2/JacksonFactory.html
[jackson]: https://github.com/FasterXML/jackson
[gson-factory]: https://googleapis.dev/java/google-http-client/latest/index.html?com/google/api/client/json/gson/GsonFactory.html
[gson]: https://github.com/google/gson
[android-json-factory]: https://googleapis.dev/java/google-http-client/latest/index.html?com/google/api/client/extensions/android/json/AndroidJsonFactory.html
[youtube-sample]: https://github.com/googleapis/google-http-java-client/tree/master/samples/snippets/src/main/java/com/example/json/YouTubeSample.java
[youtube-search]: https://developers.google.com/youtube/v3/docs/search/list
[json-parser]: https://googleapis.dev/java/google-http-client/latest/index.html?com/google/api/client/json/JsonParser.html
[key-annotation]: https://googleapis.dev/java/google-http-client/latest/index.html?com/google/api/client/util/Key.html
[generic-json]: https://googleapis.dev/java/google-http-client/latest/index.html?com/google/api/client/json/GenericJson.html
[map]: https://docs.oracle.com/javase/7/docs/api/java/util/Map.html
[null-string]: https://googleapis.dev/java/google-http-client/latest/index.html?com/google/api/client/util/Data.html#NULL_STRING
