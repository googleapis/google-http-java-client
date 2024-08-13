package com.google.api.client.http.apache.v5;

import java.io.IOException;
import java.util.Iterator;
import java.util.Locale;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.Header;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.HttpVersion;
import org.apache.hc.core5.http.ProtocolException;
import org.apache.hc.core5.http.ProtocolVersion;

public class Apache5MockHttpResponse implements ClassicHttpResponse {
  @Override
  public int getCode() {
    return 200;
  }

  @Override
  public void setCode(int code) {}

  @Override
  public String getReasonPhrase() {
    return null;
  }

  @Override
  public void setReasonPhrase(String reason) {}

  @Override
  public Locale getLocale() {
    return null;
  }

  @Override
  public void setLocale(Locale loc) {}

  @Override
  public void setVersion(ProtocolVersion version) {}

  @Override
  public ProtocolVersion getVersion() {
    return HttpVersion.HTTP_1_1;
  }

  @Override
  public void addHeader(Header header) {}

  @Override
  public void addHeader(String name, Object value) {}

  @Override
  public void setHeader(Header header) {}

  @Override
  public void setHeader(String name, Object value) {}

  @Override
  public void setHeaders(Header... headers) {}

  @Override
  public boolean removeHeader(Header header) {
    return true;
  }

  @Override
  public boolean removeHeaders(String name) {
    return true;
  }

  @Override
  public boolean containsHeader(String name) {
    return false;
  }

  @Override
  public int countHeaders(String name) {
    return 0;
  }

  @Override
  public Header getFirstHeader(String name) {
    return null;
  }

  @Override
  public Header getHeader(String name) throws ProtocolException {
    return null;
  }

  @Override
  public Header[] getHeaders() {
    return new Header[0];
  }

  @Override
  public Header[] getHeaders(String name) {
    return new Header[0];
  }

  @Override
  public Header getLastHeader(String name) {
    return null;
  }

  @Override
  public Iterator<Header> headerIterator() {
    return null;
  }

  @Override
  public Iterator<Header> headerIterator(String name) {
    return null;
  }

  @Override
  public void close() throws IOException {}

  @Override
  public HttpEntity getEntity() {
    return null;
  }

  @Override
  public void setEntity(HttpEntity entity) {}
}
