/*
 * Copyright 2024 Google LLC
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

import java.io.IOException;
import java.io.InputStream;
import org.junit.Test;

public class Apache5ResponseContentTest {
    @Test
    public void testNullResponseContent_doesNotThrowExceptionOnClose() throws Exception {
        Apache5ResponseContent response =
                new Apache5ResponseContent(
                        new InputStream() {
                            @Override
                            public int read() throws IOException {
                                return 0;
                            }
                        },
                        null);

        response.close();
    }

    @Test
    public void testNullWrappedContent_doesNotThrowExceptionOnClose() throws Exception {
        MockClassicHttpResponse mockResponse = new MockClassicHttpResponse();
        Apache5ResponseContent response =
                new Apache5ResponseContent(
                        null,
                        mockResponse);

        response.close();
    }
}
