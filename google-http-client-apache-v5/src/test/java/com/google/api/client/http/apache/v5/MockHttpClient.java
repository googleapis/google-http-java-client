package com.google.api.client.http.apache.v5;

import com.google.api.client.util.Preconditions;
import java.io.IOException;
import org.apache.hc.client5.http.classic.HttpClient;
import org.apache.hc.core5.http.ClassicHttpRequest;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.HttpHost;
import org.apache.hc.core5.http.HttpResponse;
import org.apache.hc.core5.http.io.HttpClientResponseHandler;
import org.apache.hc.core5.http.protocol.HttpContext;

public class MockHttpClient implements HttpClient {

  /** HTTP response code to use. */
  int responseCode;

  /** Returns the HTTP response code to use. */
  public final int getResponseCode() {
    return responseCode;
  }

  /** Sets the HTTP response code to use. */
  public MockHttpClient setResponseCode(int responseCode) {
    Preconditions.checkArgument(responseCode >= 0);
    this.responseCode = responseCode;
    return this;
  }

  @Override
  public HttpResponse execute(ClassicHttpRequest request) throws IOException {
    return null;
  }

  @Override
  public HttpResponse execute(ClassicHttpRequest request, HttpContext context) throws IOException {
    return null;
  }

  @Override
  public ClassicHttpResponse execute(HttpHost target, ClassicHttpRequest request)
      throws IOException {
    return null;
  }

  @Override
  public HttpResponse execute(HttpHost target, ClassicHttpRequest request, HttpContext context)
      throws IOException {
    return null;
  }

  @Override
  public <T> T execute(
      ClassicHttpRequest request, HttpClientResponseHandler<? extends T> responseHandler)
      throws IOException {
    return null;
  }

  @Override
  public <T> T execute(
      ClassicHttpRequest request,
      HttpContext context,
      HttpClientResponseHandler<? extends T> responseHandler)
      throws IOException {
    return null;
  }

  @Override
  public <T> T execute(
      HttpHost target,
      ClassicHttpRequest request,
      HttpClientResponseHandler<? extends T> responseHandler)
      throws IOException {
    return null;
  }

  @Override
  public <T> T execute(
      HttpHost target,
      ClassicHttpRequest request,
      HttpContext context,
      HttpClientResponseHandler<? extends T> responseHandler)
      throws IOException {
    return null;
  }
}
