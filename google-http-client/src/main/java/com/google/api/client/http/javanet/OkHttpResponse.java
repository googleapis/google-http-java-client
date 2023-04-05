package com.google.api.client.http.javanet;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class OkHttpResponse implements HttpResponse {
    private final int responseCode;
    private final String responseMessage;
    private final List<String> headerNames;
    private final List<String> headerValues;

    public OkHttpResponse(HttpURLConnection connection) throws IOException {
        responseCode = connection.getResponseCode();
        responseMessage = connection.getResponseMessage();
        headerNames = new ArrayList<>();
        headerValues = new ArrayList<>();
        for (Map.Entry<String, List<String>> entry : connection.getHeaderFields().entrySet()) {
            String key = entry.getKey();
            if (key != null) {
                for (String value : entry.getValue()) {
                    if (value != null) {
                        headerNames.add(key);
                        headerValues.add(value);
                    }
                }
            }
        }
    }

    @Override
    public int getResponseCode() {
        return responseCode;
    }

    @Override
    public String getResponseMessage() {
        return responseMessage;
    }

    @Override
    public List<String> getHeaderNames() {
        return headerNames;
    }

    @Override
    public List<String> getHeaderValues() {
        return headerValues;
    }
}

