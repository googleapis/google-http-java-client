/**
 * Copyright 2019 Google LLC
 *
 * <p>Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of the License at
 *
 * <p>https://www.apache.org/licenses/LICENSE-2.0
 *
 * <p>Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.google.api.client.http;

import javax.annotation.Nullable;

class SpanFactory {
  /** This is used to check if the optional OpenCensus deps are on the classpath */
  private static final boolean HAS_OPEN_CENSUS_CLASSES_ON_CLASSPATH =
      hasClasses(
          "io.opencensus.trace.Span",
          "io.opencensus.implcore.trace.RecordEventsSpanImpl",
          "io.opencensus.contrib.http.util.HttpPropagationUtil");

  private static final NoopSpan noopSpan = new NoopSpan();

  static Span getSpan() {
    if (HAS_OPEN_CENSUS_CLASSES_ON_CLASSPATH) {
      return new OpenCensusSpan();
    } else {
      return noopSpan;
    }
  }

  private static boolean hasClasses(String... classNames) {
    for (String className : classNames) {
      try {
        Class.forName(className, false, ScopeFactory.class.getClassLoader());
      } catch (ClassNotFoundException t) {
        return false;
      }
    }
    return true;
  }

  private static class NoopSpan implements Span {
    @Override
    public void addCommonHttpAttributes(
        String requestMethod, String host, String path, String url) {}

    @Override
    public void addUserAgent(String value) {}

    @Override
    public void addAnnotation(String description) {}

    @Override
    public void addHeaders(HttpHeaders headers) {}

    @Override
    public void end(@Nullable Integer statusCode) {}

    @Override
    public void recordSentMessageEvent(long contentLength) {}

    @Override
    public void recordReceivedMessageEvent(long contentLength) {}
  }

  private SpanFactory() {}
}
