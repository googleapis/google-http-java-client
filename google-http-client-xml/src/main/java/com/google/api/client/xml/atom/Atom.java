/*
 * Copyright (c) 2010 Google Inc.
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

package com.google.api.client.xml.atom;

import com.google.api.client.http.HttpHeaders;
import com.google.api.client.http.HttpMediaType;
import com.google.api.client.util.Beta;
import com.google.api.client.util.Charsets;
import com.google.api.client.util.Lists;
import com.google.api.client.util.Preconditions;
import com.google.api.client.util.escape.PercentEscaper;
import com.google.api.client.xml.Xml;
import java.util.Arrays;

/**
 * {@link Beta} <br>
 * Atom Utilities.
 *
 * @since 1.0
 * @author Yaniv Inbar
 */
@Beta
public final class Atom {

  /** Atom namespace. */
  public static final String ATOM_NAMESPACE = "http://www.w3.org/2005/Atom";

  /**
   * {@code "application/atom+xml; charset=utf-8"} media type used as a default for Atom parsing.
   *
   * <p>Use {@link HttpMediaType#equalsIgnoreParameters} for comparing media types.
   *
   * @since 1.10
   */
  public static final String MEDIA_TYPE =
      new HttpMediaType("application/atom+xml").setCharsetParameter(Charsets.UTF_8).build();

  /** Escaper for the {@code Slug} header. */
  private static final PercentEscaper SLUG_ESCAPER =
      new PercentEscaper(" !\"#$&'()*+,-./:;<=>?@[\\]^_`{|}~");

  static final class StopAtAtomEntry extends Xml.CustomizeParser {

    static final StopAtAtomEntry INSTANCE = new StopAtAtomEntry();

    @Override
    public boolean stopBeforeStartTag(String namespace, String localName) {
      return "entry".equals(localName) && ATOM_NAMESPACE.equals(namespace);
    }
  }

  private Atom() {}

  /**
   * Checks the given content type matches the Atom content type specified in {@link #MEDIA_TYPE}.
   *
   * @throws IllegalArgumentException if content type doesn't match
   */
  public static void checkContentType(String contentType) {
    Preconditions.checkArgument(contentType != null); // for backwards compatibility
    Preconditions.checkArgument(
        HttpMediaType.equalsIgnoreParameters(MEDIA_TYPE, contentType),
        "Wrong content type: expected <" + MEDIA_TYPE + "> but got <%s>",
        contentType);
  }

  /**
   * Sets the {@code "Slug"} header, properly escaping the header value. See <a
   * href="http://tools.ietf.org/html/rfc5023#section-9.7">The Slug Header</a>.
   *
   * @since 1.14
   */
  public static void setSlugHeader(HttpHeaders headers, String value) {
    if (value == null) {
      headers.remove("Slug");
    } else {
      headers.set("Slug", Lists.newArrayList(Arrays.asList(SLUG_ESCAPER.escape(value))));
    }
  }
}
