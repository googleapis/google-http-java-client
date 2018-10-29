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

package com.google.api.client.xml;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.util.List;
import org.junit.Assert;
import org.junit.Test;
import org.xmlpull.v1.XmlPullParser;
import com.google.api.client.http.HttpHeaders;
import com.google.api.client.http.xml.atom.AtomFeedParser;
import com.google.api.client.util.Key;
import com.google.api.client.xml.atom.AbstractAtomFeedParser;
import com.google.api.client.xml.atom.Atom;

/**
 * Tests {@link Atom}.
 *
 * @author Yaniv Inbar
 * @author Gerald Madlmayr
 */
public class AtomTest {


  private static final String SAMPLE_FEED = "<?xml version=\"1.0\" encoding=\"utf-8\"?><feed " +
      "xmlns=\"http://www.w3.org/2005/Atom\">  <title>Example Feed</title>  <link href" +
      "=\"http://example.org/\"/>  <updated>2003-12-13T18:31:02Z</updated>  <author>    " +
      "<name>John Doe</name>  </author>  <id>urn:uuid:60a76c80-d399-11d9-b93C-0003939e0af6" +
      "</id>  <entry>    <title>Atom-Powered Robots Run Amok</title>    <link href=\"http" +
      "://example.org/2003/12/13/atom03\"/>   <id>urn:uuid:1225c695-cfb8-4ebb-aaaa" +
      "-80da344efa6a</id>    <updated>2003-12-13T18:30:02Z</updated>    <summary>Some text" +
      ".</summary>  </entry><entry>    <title>Atom-Powered Robots Run Amok!</title>    <link" +
      " href=\"http://example.org/2003/12/13/atom02\"/>  <id>urn:uuid:1225c695-cfb8-4ebb" +
      "-aaaa-80da344efa62</id>    <updated>2003-12-13T18:32:02Z</updated>    <summary>Some " +
      "other text.</summary>  </entry></feed>";

  /**
   * Test for checking the Slug Header
   */
  @SuppressWarnings("unchecked")
  @Test
  public void testSetSlugHeader() {
    HttpHeaders headers = new HttpHeaders();
    assertNull(headers.get("Slug"));
    subtestSetSlugHeader(headers, "value", "value");
    subtestSetSlugHeader(headers, " !\"#$&'()*+,-./:;<=>?@[\\]^_`{|}~", " !\"#$&'()*+,-./:;" +
        "<=>?@[\\]^_`{|}~");
    subtestSetSlugHeader(headers, "%D7%99%D7%A0%D7%99%D7%91", "יניב");
    subtestSetSlugHeader(headers, null, null);
  }

  @SuppressWarnings("unchecked")
  public void subtestSetSlugHeader(HttpHeaders headers, String expectedValue, String value) {
    Atom.setSlugHeader(headers, value);
    if (value == null) {
      assertNull(headers.get("Slug"));
    } else {
      Assert.assertArrayEquals(new String[]{expectedValue},
          ((List<String>) headers.get("Slug")).toArray());
    }
  }

  /**
   * This tests parses a simple Atom Feed given as a constant. All elements are evaluated, to see if
   * everything works fine. For parsing a dedicated {@link AtomFeedParser} is used.
   *
   * The purpose of this test is to test the {@link AtomFeedParser#parseFeed} and {@link
   * AtomFeedParser#parseNextEntry} and see if the mapping of the XML element to the entity classes
   * is done correctly.
   */
  @SuppressWarnings("unchecked")
  @Test
  public void testAtomFeedParser() throws Exception {
    XmlPullParser parser = Xml.createParser();
    // Wired. Both, the InputStream for the FeedParser and the XPP need to be set (?)
    parser.setInput(new StringReader(SAMPLE_FEED));
    InputStream stream = new ByteArrayInputStream(SAMPLE_FEED.getBytes());
    XmlNamespaceDictionary namespaceDictionary = new XmlNamespaceDictionary();
    AbstractAtomFeedParser atomParser = new AtomFeedParser<Feed, FeedEntry>(namespaceDictionary,
        parser, stream, Feed.class, FeedEntry.class);

    Feed feed = (Feed) atomParser.parseFeed();
    assertEquals("John Doe", feed.author.name);
    assertEquals("urn:uuid:60a76c80-d399-11d9-b93C-0003939e0af6", feed.id);
    assertEquals("2003-12-13T18:31:02Z", feed.updated);
    assertEquals("Example Feed", feed.title);
    assertEquals("http://example.org/", feed.link.href);

    FeedEntry entry1 = (FeedEntry) atomParser.parseNextEntry();
    //assertNotNull(feed.entry);
    assertEquals("urn:uuid:1225c695-cfb8-4ebb-aaaa-80da344efa6a", entry1.id);
    assertEquals("2003-12-13T18:30:02Z", entry1.updated);
    assertEquals("Some text.", entry1.summary);
    assertEquals("Atom-Powered Robots Run Amok", entry1.title);
    assertEquals("http://example.org/2003/12/13/atom03", entry1.link.href);

    FeedEntry entry2 = (FeedEntry) atomParser.parseNextEntry();
    assertEquals("urn:uuid:1225c695-cfb8-4ebb-aaaa-80da344efa62", entry2.id);
    assertEquals("2003-12-13T18:32:02Z", entry2.updated);
    assertEquals("Some other text.", entry2.summary);
    assertEquals("Atom-Powered Robots Run Amok!", entry2.title);
    assertEquals("http://example.org/2003/12/13/atom02", entry2.link.href);

    FeedEntry entry3 = (FeedEntry) atomParser.parseNextEntry();
    assertNull(entry3);

    atomParser.close();
  }

  /**
   * Manuel tests of a constant string to see if the data structure can be parsed in the regular way
   * and get the same result.
   *
   * The purpose of this test is to evaluate, if the parsed elements are the same with the {@link
   * AtomFeedParser}.
   */
  @SuppressWarnings("unchecked")
  @Test
  public void testAtomFeedParserRegualar() throws Exception {
    Feed feed = new Feed();
    XmlPullParser parser = Xml.createParser();
    parser.setInput(new StringReader(SAMPLE_FEED));
    XmlNamespaceDictionary namespaceDictionary = new XmlNamespaceDictionary();
    Xml.parseElement(parser, feed, namespaceDictionary, null);
    assertNotNull(feed);
    assertEquals(2, feed.entry.length);

    assertEquals("John Doe", feed.author.name);
    assertEquals("urn:uuid:60a76c80-d399-11d9-b93C-0003939e0af6", feed.id);
    assertEquals("2003-12-13T18:31:02Z", feed.updated);
    assertEquals("Example Feed", feed.title);
    assertEquals("http://example.org/", feed.link.href);

    FeedEntry entry1 = feed.entry[0];
    //assertNotNull(feed.entry);
    assertEquals("urn:uuid:1225c695-cfb8-4ebb-aaaa-80da344efa6a", entry1.id);
    assertEquals("2003-12-13T18:30:02Z", entry1.updated);
    assertEquals("Some text.", entry1.summary);
    assertEquals("Atom-Powered Robots Run Amok", entry1.title);
    assertEquals("http://example.org/2003/12/13/atom03", entry1.link.href);

    FeedEntry entry2 = feed.entry[1];
    assertEquals("urn:uuid:1225c695-cfb8-4ebb-aaaa-80da344efa62", entry2.id);
    assertEquals("2003-12-13T18:32:02Z", entry2.updated);
    assertEquals("Some other text.", entry2.summary);
    assertEquals("Atom-Powered Robots Run Amok!", entry2.title);
    assertEquals("http://example.org/2003/12/13/atom02", entry2.link.href);
  }

  /**
   * Reading an XML ATOM Feed from a file and valid if all the {@link FeedEntry} are present. No
   * detailed evaluation of each element
   *
   * The purpose of this test is to read a bunch of elements which contain additional elements
   * (HTML in this case), that are not part of the {@link FeedEntry} and to see if there is an issue
   * if we parse some more entries.
   */
  @SuppressWarnings("unchecked")
  @Test
  public void testHeiseFeedParser() throws Exception {
    XmlPullParser parser = Xml.createParser();
    String read = readFile("heise-atom.xml");
    parser.setInput(new StringReader(read));
    XmlNamespaceDictionary namespaceDictionary = new XmlNamespaceDictionary();
    AbstractAtomFeedParser atomParser = new AtomFeedParser<Feed, FeedEntry>(namespaceDictionary,
        parser, new ByteArrayInputStream(read.getBytes()), Feed.class, FeedEntry.class);
    Feed feed = (Feed) atomParser.parseFeed();
    assertNotNull(feed);

    int counter = 0;
    while (atomParser.parseNextEntry() != null) {
      counter++;
    }

    atomParser.close();
    assertEquals(62, counter);
  }

  /**
   * We need to method to read the file into a string
   *
   * @param file file name in the resource folder that will be parsed
   * @return content of the File as String.
   * @throws IOException in case the file was not able to be parsed
   * @see <a href="https://goo.gl/CJ4v7Z">Stackoverflow</a>
   */
  private String readFile(String file) throws IOException {
    BufferedReader reader = null;
    try {
      ClassLoader classLoader = getClass().getClassLoader();
      reader = new BufferedReader(new FileReader(classLoader.getResource(file)
          .getFile()));
      String line;
      StringBuilder stringBuilder = new StringBuilder();

      while ((line = reader.readLine()) != null) {
        stringBuilder.append(line);
      }
      return stringBuilder.toString();
    } finally {
      if (reader != null) {
        reader.close();
      }
    }
  }

  /**
   * Feed Element to map the XML to
   */
  public static class Feed {
    @Key
    private String title;
    @Key
    private Link link;
    @Key
    private String updated;
    @Key
    private Author author;
    @Key
    private String id;
    @Key
    private FeedEntry[] entry;
  }

  /**
   * Author Element as part of the {@link Feed} Element to map the XML to (as this is sub-element,
   * this needs to be public)
   */
  public static class Author {
    @Key
    private String name;
  }

  /**
   * Link Element as part of the {@link Feed} Element to map the XML to (as this is sub-element,
   * this needs to be public)
   */
  public static class Link {
    @Key("@href")
    private String href;
  }

  /**
   * Entry Element to cover the Entries of a Atom {@link Feed}  (as this is sub-element,
   * this needs to be public)
   */
  public static class FeedEntry {
    @Key
    private String title;
    @Key
    private Link link;
    @Key
    private String updated;
    @Key
    private String summary;
    @Key
    private String id;
  }
}

