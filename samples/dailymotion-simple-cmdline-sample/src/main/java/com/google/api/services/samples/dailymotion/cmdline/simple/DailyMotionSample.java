/*
 * Copyright (c) 2011 Google Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package com.google.api.services.samples.dailymotion.cmdline.simple;

import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestFactory;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.HttpResponseException;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.JsonObjectParser;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.Key;

import java.util.List;

/**
 * Simple example for the <a href="http://www.dailymotion.com/doc/api/graph-api.html">Dailymotion
 * Graph API</a>.
 * 
 * @author Yaniv Inbar
 */
public class DailyMotionSample {

  static final HttpTransport HTTP_TRANSPORT = new NetHttpTransport();
  static final JsonFactory JSON_FACTORY = new JacksonFactory();

  /** Represents a video feed. */
  public static class VideoFeed {
    @Key
    public List<Video> list;

    @Key("has_more")
    public boolean hasMore;
  }

  /** Represents a video. */
  public static class Video {
    @Key
    public String id;

    @Key
    public List<String> tags;

    @Key
    public String title;

    @Key
    public String url;
  }

  /** URL for Dailymotion API. */
  public static class DailyMotionUrl extends GenericUrl {

    public DailyMotionUrl(String encodedUrl) {
      super(encodedUrl);
    }

    @Key
    public String fields;
  }

  private static void run() throws Exception {
    HttpRequestFactory requestFactory =
        HTTP_TRANSPORT.createRequestFactory(new HttpRequestInitializer() {
            @Override
          public void initialize(HttpRequest request) {
            request.setParser(new JsonObjectParser(JSON_FACTORY));
          }
        });
    DailyMotionUrl url = new DailyMotionUrl("https://api.dailymotion.com/videos/");
    url.fields = "id,tags,title,url";
    HttpRequest request = requestFactory.buildGetRequest(url);
    VideoFeed videoFeed = request.execute().parseAs(VideoFeed.class);
    if (videoFeed.list.isEmpty()) {
      System.out.println("No videos found.");
    } else {
      if (videoFeed.hasMore) {
        System.out.print("First ");
      }
      System.out.println(videoFeed.list.size() + " videos found:");
      for (Video video : videoFeed.list) {
        System.out.println();
        System.out.println("-----------------------------------------------");
        System.out.println("ID: " + video.id);
        System.out.println("Title: " + video.title);
        System.out.println("Tags: " + video.tags);
        System.out.println("URL: " + video.url);
      }
    }
  }

  public static void main(String[] args) {
    try {
      try {
        run();
        return;
      } catch (HttpResponseException e) {
        System.err.println(e.getMessage());
      }
    } catch (Throwable t) {
      t.printStackTrace();
    }
    System.exit(1);
  }
}
