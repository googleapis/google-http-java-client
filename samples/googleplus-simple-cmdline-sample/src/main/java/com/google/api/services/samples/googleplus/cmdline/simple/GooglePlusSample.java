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

package com.google.api.services.samples.googleplus.cmdline.simple;

import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestFactory;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.HttpResponseException;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.GenericJson;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.JsonObjectParser;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.Key;

import java.io.IOException;
import java.util.List;

/**
 * Simple example that demonstrates how to use <a
 * href="code.google.com/p/google-http-java-client/">Google HTTP Client Library for Java</a> with
 * the <a href="https://developers.google.com/+/api/">Google+ API</a>.
 *
 * <p>
 * Note that in the case of the Google+ API, there is a much better custom library built on top of
 * this HTTP library that is much easier to use and hides most of these details for you. See <a
 * href="http://code.google.com/p/google-api-java-client/wiki/APIs#Google+_API">Google+ API for
 * Java</a>.
 * </p>
 *
 * @author Yaniv Inbar
 */
public class GooglePlusSample {

  private static final String API_KEY =
      "Enter API Key from https://code.google.com/apis/console/?api=plus into API_KEY";

  private static final String USER_ID = "116899029375914044550";
  private static final int MAX_RESULTS = 3;

  static final HttpTransport HTTP_TRANSPORT = new NetHttpTransport();
  static final JsonFactory JSON_FACTORY = new JacksonFactory();

  /** Feed of Google+ activities. */
  public static class ActivityFeed {

    /** List of Google+ activities. */
    @Key("items")
    private List<Activity> activities;

    public List<Activity> getActivities() {
      return activities;
    }
  }

  /** Google+ activity. */
  public static class Activity extends GenericJson {

    /** Activity URL. */
    @Key
    private String url;

    public String getUrl() {
      return url;
    }

    /** Activity object. */
    @Key("object")
    private ActivityObject activityObject;

    public ActivityObject getActivityObject() {
      return activityObject;
    }
  }

  /** Google+ activity object. */
  public static class ActivityObject {

    /** HTML-formatted content. */
    @Key
    private String content;

    public String getContent() {
      return content;
    }

    /** People who +1'd this activity. */
    @Key
    private PlusOners plusoners;

    public PlusOners getPlusOners() {
      return plusoners;
    }
  }

  /** People who +1'd an activity. */
  public static class PlusOners {

    /** Total number of people who +1'd this activity. */
    @Key
    private long totalItems;

    public long getTotalItems() {
      return totalItems;
    }
  }

  /** Google+ URL. */
  public static class PlusUrl extends GenericUrl {

    public PlusUrl(String encodedUrl) {
      super(encodedUrl);
    }

    @SuppressWarnings("unused")
    @Key
    private final String key = API_KEY;

    /** Maximum number of results. */
    @Key
    private int maxResults;

    public int getMaxResults() {
      return maxResults;
    }

    public PlusUrl setMaxResults(int maxResults) {
      this.maxResults = maxResults;
      return this;
    }

    /** Lists the public activities for the given Google+ user ID. */
    public static PlusUrl listPublicActivities(String userId) {
      return new PlusUrl(
          "https://www.googleapis.com/plus/v1/people/" + userId + "/activities/public");
    }
  }

  private static void parseResponse(HttpResponse response) throws IOException {
    ActivityFeed feed = response.parseAs(ActivityFeed.class);
    if (feed.getActivities().isEmpty()) {
      System.out.println("No activities found.");
    } else {
      if (feed.getActivities().size() == MAX_RESULTS) {
        System.out.print("First ");
      }
      System.out.println(feed.getActivities().size() + " activities found:");
      for (Activity activity : feed.getActivities()) {
        System.out.println();
        System.out.println("-----------------------------------------------");
        System.out.println("HTML Content: " + activity.getActivityObject().getContent());
        System.out.println("+1's: " + activity.getActivityObject().getPlusOners().getTotalItems());
        System.out.println("URL: " + activity.getUrl());
        System.out.println("ID: " + activity.get("id"));
      }
    }
  }

  private static void run() throws IOException {
    HttpRequestFactory requestFactory =
        HTTP_TRANSPORT.createRequestFactory(new HttpRequestInitializer() {
            @Override
          public void initialize(HttpRequest request) {
            request.setParser(new JsonObjectParser(JSON_FACTORY));
          }
        });
    PlusUrl url = PlusUrl.listPublicActivities(USER_ID).setMaxResults(MAX_RESULTS);
    url.put("fields", "items(id,url,object(content,plusoners/totalItems))");
    HttpRequest request = requestFactory.buildGetRequest(url);
    parseResponse(request.execute());
  }

  public static void main(String[] args) {
    if (API_KEY.startsWith("Enter ")) {
      System.err.println(API_KEY);
      System.exit(1);
    }
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
