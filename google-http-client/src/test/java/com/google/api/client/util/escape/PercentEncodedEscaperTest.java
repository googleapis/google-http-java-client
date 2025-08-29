package com.google.api.client.util.escape;

import junit.framework.TestCase;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class PercentEncodedEscaperTest extends TestCase {
  @Test
  public void testEscape() {
    PercentEncodedEscaper escaper =
        new PercentEncodedEscaper(
            new PercentEscaper(PercentEscaper.SAFE_PLUS_RESERVED_CHARS_URLENCODER));
    String input = "Hello%20World+/?#[]";

    String actual = escaper.escape(input);
    assertEquals(input, actual); // No change expected since it's already percent-encoded
  }

  @Test
  public void testEscapeEncode() {
    PercentEncodedEscaper escaper =
        new PercentEncodedEscaper(
            new PercentEscaper(PercentEscaper.SAFE_PLUS_RESERVED_CHARS_URLENCODER));
    String input = "Hello World%";
    String expected = "Hello%20World%25";

    String actual = escaper.escape(input);
    assertEquals(expected, actual);
  }
}
