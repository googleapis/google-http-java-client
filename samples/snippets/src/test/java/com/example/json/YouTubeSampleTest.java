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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.LowLevelHttpRequest;
import com.google.api.client.http.LowLevelHttpResponse;
import com.google.api.client.json.Json;
import com.google.api.client.json.JsonObjectParser;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.testing.http.HttpTesting;
import com.google.api.client.testing.http.MockHttpTransport;
import com.google.api.client.testing.http.MockLowLevelHttpRequest;
import com.google.api.client.testing.http.MockLowLevelHttpResponse;
import com.google.common.base.Preconditions;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import org.junit.Test;

public class YouTubeSampleTest {

  @Test
  public void testParsing() throws IOException {
    final InputStream contents =
        getClass().getClassLoader().getResourceAsStream("youtube-search.json");
    Preconditions.checkNotNull(contents);
    HttpTransport transport =
        new MockHttpTransport() {
          @Override
          public LowLevelHttpRequest buildRequest(String method, String url) throws IOException {
            return new MockLowLevelHttpRequest() {
              @Override
              public LowLevelHttpResponse execute() throws IOException {
                MockLowLevelHttpResponse result = new MockLowLevelHttpResponse();
                result.setContentType(Json.MEDIA_TYPE);
                result.setContent(contents);
                return result;
              }
            };
          }
        };
    HttpRequest request =
        transport.createRequestFactory().buildGetRequest(HttpTesting.SIMPLE_GENERIC_URL);
    request.setParser(new JsonObjectParser(new GsonFactory()));
    HttpResponse response = request.execute();

    YouTubeSample.ListResponse listResponse = YouTubeSample.parseJson(response);
    assertEquals(5, listResponse.getPageInfo().getResultsPerPage());
    assertEquals(1000000, listResponse.getPageInfo().getTotalResults());
    assertEquals(5, listResponse.getSearchResults().size());
    for (YouTubeSample.SearchResult searchResult : listResponse.getSearchResults()) {
      assertEquals("youtube#searchResult", searchResult.getKind());
      assertNotNull(searchResult.getId());
      assertEquals("youtube#video", searchResult.getId().getKind());
      assertNotNull(searchResult.getId().getVideoId());
      YouTubeSample.Snippet snippet = searchResult.getSnippet();
      assertNotNull(snippet);
      assertNotNull(snippet.getChannelId());
      assertNotNull(snippet.getDescription());
      assertNotNull(snippet.getTitle());
      assertNotNull(snippet.getPublishedAt());
      Map<String, YouTubeSample.Thumbnail> thumbnails = snippet.getThumbnails();
      assertNotNull(thumbnails);

      for (Map.Entry<String, YouTubeSample.Thumbnail> entry : thumbnails.entrySet()) {
        assertNotNull(entry.getKey());
        YouTubeSample.Thumbnail thumbnail = entry.getValue();
        assertNotNull(thumbnail);
        assertNotNull(thumbnail.getUrl());
        assertNotNull(thumbnail.getWidth());
        assertNotNull(thumbnail.getHeight());
      }
    }
  }
}
