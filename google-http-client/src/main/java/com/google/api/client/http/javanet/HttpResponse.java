package com.google.api.client.http.javanet;

import java.util.List;

public interface HttpResponse {
    int getResponseCode();
    String getResponseMessage();
    List<String> getHeaderNames();
    List<String> getHeaderValues();
}