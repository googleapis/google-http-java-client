/*
 * Copyright 2020 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.json;

import com.google.api.client.http.HttpResponse;
import com.google.api.client.util.Key;
import java.io.IOException;
import java.util.List;
import java.util.Map;

public class YouTubeSample {
  public static class ListResponse {
    @Key("items")
    private List<SearchResult> searchResults;

    @Key private PageInfo pageInfo;

    public List<SearchResult> getSearchResults() {
      return searchResults;
    }

    public PageInfo getPageInfo() {
      return pageInfo;
    }
  }

  public static class PageInfo {
    @Key private long totalResults;

    @Key private long resultsPerPage;

    public long getTotalResults() {
      return totalResults;
    }

    public long getResultsPerPage() {
      return resultsPerPage;
    }
  }

  public static class SearchResult {
    @Key private String kind;

    @Key("id")
    private VideoId videoId;

    @Key private Snippet snippet;

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
    @Key private String kind;

    @Key private String videoId;

    public String getKind() {
      return kind;
    }

    public String getVideoId() {
      return videoId;
    }
  }

  public static class Snippet {
    @Key private String publishedAt;

    @Key private String channelId;

    @Key private String title;

    @Key private String description;

    @Key private Map<String, Thumbnail> thumbnails;

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
    @Key private String url;

    @Key private long width;

    @Key private long height;

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
}
