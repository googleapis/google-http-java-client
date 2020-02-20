/*
 * Copyright (c) 2013 Google Inc.
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

package com.google.api.client.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * {@link Beta} <br>
 * PEM file reader.
 *
 * <p>Supports reading any PEM stream that contains Base64 encoded content stored inside {@code
 * "-----BEGIN ...-----"} and {@code "-----END ...-----"} tags. Each call to {@link
 * #readNextSection()} parses the next section in the PEM file. If you need a section of a certain
 * title use {@link #readNextSection(String)}, for example {@code readNextSection("PRIVATE KEY")}.
 * To ensure that the stream is closed properly, call {@link #close()} in a finally block.
 *
 * <p>As a convenience, use {@link #readFirstSectionAndClose(Reader)} or {@link
 * #readFirstSectionAndClose(Reader, String)} for the common case of only a single section in a PEM
 * file (or only a single section of a given title).
 *
 * <p>Limitations:
 *
 * <p><ul>
 *   <li>Assumes the PEM file section content is not encrypted and cannot handle the case of any
 *       headers inside the BEGIN and END tag.
 *   <li>It also ignores any attributes associated with any PEM file section.
 * </ul>
 *
 * @since 1.14
 * @author Yaniv Inbar
 */
@Beta
public final class PemReader {

  private static final Pattern BEGIN_PATTERN = Pattern.compile("-----BEGIN ([A-Z ]+)-----");
  private static final Pattern END_PATTERN = Pattern.compile("-----END ([A-Z ]+)-----");

  /** Reader. */
  private BufferedReader reader;

  /** @param reader reader */
  public PemReader(Reader reader) {
    this.reader = new BufferedReader(reader);
  }

  /** Reads the next section in the PEM file or {@code null} for end of file. */
  public Section readNextSection() throws IOException {
    return readNextSection(null);
  }

  /**
   * Reads the next section in the PEM file, optionally based on a title to look for.
   *
   * @param titleToLookFor title to look for or {@code null} for any title
   * @return next section or {@code null} for end of file
   */
  public Section readNextSection(String titleToLookFor) throws IOException {
    String title = null;
    StringBuilder keyBuilder = null;
    while (true) {
      String line = reader.readLine();
      if (line == null) {
        Preconditions.checkArgument(title == null, "missing end tag (%s)", title);
        return null;
      }
      if (keyBuilder == null) {
        Matcher m = BEGIN_PATTERN.matcher(line);
        if (m.matches()) {
          String curTitle = m.group(1);
          if (titleToLookFor == null || curTitle.equals(titleToLookFor)) {
            keyBuilder = new StringBuilder();
            title = curTitle;
          }
        }
      } else {
        Matcher m = END_PATTERN.matcher(line);
        if (m.matches()) {
          String endTitle = m.group(1);
          Preconditions.checkArgument(
              endTitle.equals(title), "end tag (%s) doesn't match begin tag (%s)", endTitle, title);
          return new Section(title, Base64.decodeBase64(keyBuilder.toString()));
        }
        keyBuilder.append(line);
      }
    }
  }

  /**
   * Reads the first section in the PEM file, and then closes the reader.
   *
   * @param reader reader
   * @return first section found or {@code null} for none found
   */
  public static Section readFirstSectionAndClose(Reader reader) throws IOException {
    return readFirstSectionAndClose(reader, null);
  }

  /**
   * Reads the first section in the PEM file, optionally based on a title to look for, and then
   * closes the reader.
   *
   * @param titleToLookFor title to look for or {@code null} for any title
   * @param reader reader
   * @return first section found or {@code null} for none found
   */
  public static Section readFirstSectionAndClose(Reader reader, String titleToLookFor)
      throws IOException {
    PemReader pemReader = new PemReader(reader);
    try {
      return pemReader.readNextSection(titleToLookFor);
    } finally {
      pemReader.close();
    }
  }

  /**
   * Closes the reader.
   *
   * <p>To ensure that the stream is closed properly, call {@link #close()} in a finally block.
   */
  public void close() throws IOException {
    reader.close();
  }

  /** Section in the PEM file. */
  public static final class Section {

    /** Title. */
    private final String title;

    /** Base64-decoded bytes. */
    private final byte[] base64decodedBytes;

    /**
     * @param title title
     * @param base64decodedBytes base64-decoded bytes
     */
    Section(String title, byte[] base64decodedBytes) {
      this.title = Preconditions.checkNotNull(title);
      this.base64decodedBytes = Preconditions.checkNotNull(base64decodedBytes);
    }

    /** Returns the title. */
    public String getTitle() {
      return title;
    }

    /** Returns the base64-decoded bytes (modifiable array). */
    public byte[] getBase64DecodedBytes() {
      return base64decodedBytes;
    }
  }
}
