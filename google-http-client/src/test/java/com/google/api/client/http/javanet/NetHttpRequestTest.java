package com.google.api.client.http.javanet;

import com.google.api.client.http.HttpContent;
import com.google.api.client.http.InputStreamContent;
import com.google.api.client.http.javanet.NetHttpRequest.OutputWriter;
import com.google.api.client.testing.http.HttpTesting;
import com.google.api.client.testing.http.javanet.MockHttpURLConnection;
import com.google.api.client.util.StreamingContent;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.concurrent.TimeoutException;
import org.junit.Test;

public class NetHttpRequestTest {

  static class SleepingOutputWriter implements OutputWriter {
    private long sleepTimeInMs;
    public SleepingOutputWriter(long sleepTimeInMs) {
      this.sleepTimeInMs = sleepTimeInMs;
    }
    @Override
    public void write(OutputStream outputStream, StreamingContent content) throws IOException {
      try {
        Thread.sleep(sleepTimeInMs);
      } catch (InterruptedException e) {
        throw new IOException("sleep interrupted", e);
      }
    }
  }

  @Test
  public void testHangingWrite() throws InterruptedException {
    Thread thread = new Thread() {
      @Override
      public void run() {
        try {
          postWithTimeout(0);
        } catch (IOException e) {
          // expected to be interrupted
          assertEquals(e.getCause().getClass(), InterruptedException.class);
          return;
        } catch (Exception e) {
          fail();
        }
        fail("should be interrupted before here");
      }
    };

    thread.start();
    Thread.sleep(1000);
    assertTrue(thread.isAlive());
    thread.interrupt();
  }

  @Test(timeout = 1000)
  public void testOutputStreamWriteTimeout() throws Exception {
    try {
      postWithTimeout(100);
      fail("should have timed out");
    } catch (IOException e) {
      assertEquals(e.getCause().getClass(), TimeoutException.class);
    } catch (Exception e) {
      fail("Expected an IOException not a " + e.getCause().getClass().getName());
    }
  }

  private static void postWithTimeout(int timeout) throws Exception {
    MockHttpURLConnection connection = new MockHttpURLConnection(new URL(HttpTesting.SIMPLE_URL));
    connection.setRequestMethod("POST");
    NetHttpRequest request = new NetHttpRequest(connection);
    InputStream is = NetHttpRequestTest.class.getClassLoader().getResourceAsStream("file.txt");
    HttpContent content = new InputStreamContent("text/plain", is);
    request.setStreamingContent(content);
    request.setWriteTimeout(timeout);
    request.execute(new SleepingOutputWriter(5000L));
  }

}
