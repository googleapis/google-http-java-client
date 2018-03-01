/*
 * Copyright (c) 2015 Dictanova SAS
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
package com.google.api.client.json.jackson2;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.api.client.util.ObjectParser;
import com.google.api.client.util.Preconditions;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.lang.reflect.Type;
import java.nio.charset.Charset;

/**
 * JSON serializer implementation based on Jackson <i>databinding</i> module.
 * <p>
 * <p>Usage:
 * <code><pre>{@code
 * HttpRequest request = requestFactory.buildGetRequest(url);
 * request.setParser(new JacksonMapperParser(new ObjectMapper()));
 * HttpResponse response = request.execute();
 * MyDataType data = response.parseAs(MyDataType.class);
 * }
 * </pre></code></p>
 * <p>
 * Implementation is not thread-safe.
 * </p>
 *
 * @author Damien Raude-Morvan
 */
public final class JacksonMapperParser implements ObjectParser {

    /**
     * Jackson {@link ObjectMapper} that will perform databinding.
     */
    private final ObjectMapper objectMapper;

    /**
     * @param objectMapper Jackson databinder
     */
    public JacksonMapperParser(final ObjectMapper objectMapper) {
        this.objectMapper = Preconditions.checkNotNull(objectMapper);
    }

    public <T> T parseAndClose(final InputStream in, final Charset charset,
                               final Class<T> dataClass) throws IOException {
        return objectMapper.readValue(in, dataClass);
    }

    public Object parseAndClose(final InputStream in, final Charset charset,
                                final Type dataType) throws IOException {
        JavaType javaType = objectMapper.getTypeFactory().constructType(dataType);
        return objectMapper.readValue(in, javaType);
    }

    public <T> T parseAndClose(final Reader reader, final Class<T> dataClass) throws IOException {
        return objectMapper.readValue(reader, dataClass);
    }

    public Object parseAndClose(final Reader reader, final Type dataType) throws IOException {
        JavaType javaType = objectMapper.getTypeFactory().constructType(dataType);
        return objectMapper.readValue(reader, javaType);
    }
}
