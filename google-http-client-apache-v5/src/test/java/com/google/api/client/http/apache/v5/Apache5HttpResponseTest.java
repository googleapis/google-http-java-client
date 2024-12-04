/*
 * Copyright 2019 Google LLC
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

package com.google.api.client.http.apache.v5;

import com.google.api.client.http.*;
import org.apache.hc.client5.http.classic.methods.*;
import org.apache.hc.core5.http.*;
import org.apache.hc.core5.http.io.entity.*;
import org.apache.hc.core5.http.protocol.*;
import org.junit.*;

import java.io.*;
import java.nio.charset.*;
import java.util.*;
import java.util.concurrent.atomic.*;

import static org.junit.Assert.*;

public class Apache5HttpResponseTest {
    @Test
    public void testNullContent() throws Exception {
        HttpUriRequestBase base = new HttpPost("http://www.google.com");
        MockClassicHttpResponse mockResponse = new MockClassicHttpResponse();
        mockResponse.setEntity(null);
        Apache5HttpResponse response =
                new Apache5HttpResponse(
                        base,
                        mockResponse);

        InputStream content =
                response.getContent();

        assertNotNull(content);
    }

}