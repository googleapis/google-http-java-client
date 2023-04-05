package com.google.api.client.http.javanet;

import java.util.Collections;
import java.util.List;

public class NotFoundHttpResponse implements HttpResponse {
    @Override
    public int getResponseCode() {
        return 404;
    }

    @Override
    public String getResponseMessage() {
        return "Not Found";
    }

    @Override
    public List<String> getHeaderNames() {
        return Collections.emptyList();
    }

    @Override
    public List<String> getHeaderValues() {
        return Collections.emptyList();
    }
}