/**
 * Copyright 2019 Google LLC
 *
 * <p>Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of the License at
 *
 * <p>https://www.apache.org/licenses/LICENSE-2.0
 *
 * <p>Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.google.api.client.json.gson;

import com.google.api.client.json.GenericJson;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.JsonObjectParser;
import com.google.api.client.test.json.AbstractJsonParserTest;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;

public class GsonParserTest extends AbstractJsonParserTest {

  @Override
  protected JsonFactory newJsonFactory() {
    return new GsonFactory();
  }

  public void testParse_leniency() throws IOException {
    GsonFactory factory = GsonFactory.getDefaultInstance();
    JsonObjectParser parser = new JsonObjectParser(factory);
    InputStream inputStream =
        new ByteArrayInputStream(
            ")]}'\n{\"bigDecimalValue\": 1559341956102}".getBytes(StandardCharsets.UTF_8));
    GenericJson json = parser.parseAndClose(inputStream, StandardCharsets.UTF_8, GenericJson.class);
    assertEquals(new BigDecimal("1559341956102"), json.get("bigDecimalValue"));
  }
}
