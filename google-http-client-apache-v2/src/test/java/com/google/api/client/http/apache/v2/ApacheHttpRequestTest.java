package com.google.api.client.http.apache.v2;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.google.api.client.http.ByteArrayContent;
import com.google.api.client.http.HttpContent;
import com.google.api.client.http.InputStreamContent;
import com.google.api.client.testing.http.apache.MockHttpClient;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import org.junit.Test;

public class ApacheHttpRequestTest {

  @Test
  public void testContentLengthSet() throws Exception {
    HttpExtensionMethod base = new HttpExtensionMethod("POST", "http://www.google.com");
    ApacheHttpRequest request = new ApacheHttpRequest(new MockHttpClient(), base);
    HttpContent content =
        new ByteArrayContent("text/plain", "sample".getBytes(StandardCharsets.UTF_8));
    request.setStreamingContent(content);
    request.setContentLength(content.getLength());
    request.execute();

    assertFalse(base.getEntity().isChunked());
    assertEquals(6, base.getEntity().getContentLength());
  }

  @Test
  public void testChunked() throws Exception {
    byte[] buf = new byte[300];
    Arrays.fill(buf, (byte) ' ');
    HttpExtensionMethod base = new HttpExtensionMethod("POST", "http://www.google.com");
    ApacheHttpRequest request = new ApacheHttpRequest(new MockHttpClient(), base);
    HttpContent content = new InputStreamContent("text/plain", new ByteArrayInputStream(buf));
    request.setStreamingContent(content);
    request.execute();

    assertTrue(base.getEntity().isChunked());
    assertEquals(-1, base.getEntity().getContentLength());
  }
}
