/*
 * Copyright (c) 2010 Google Inc.
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

package com.google.api.client.http;

import com.google.api.client.util.ArrayValueMap;
import com.google.api.client.util.Charsets;
import com.google.api.client.util.ClassInfo;
import com.google.api.client.util.Data;
import com.google.api.client.util.FieldInfo;
import com.google.api.client.util.GenericData;
import com.google.api.client.util.ObjectParser;
import com.google.api.client.util.Preconditions;
import com.google.api.client.util.Throwables;
import com.google.api.client.util.Types;
import com.google.api.client.util.escape.CharEscapers;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.lang.reflect.Type;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Implements support for HTTP form content encoding parsing of type {@code
 * application/x-www-form-urlencoded} as specified in the <a href=
 * "http://www.w3.org/TR/1998/REC-html40-19980424/interact/forms.html#h-17.13.4.1" >HTML 4.0
 * Specification</a>.
 *
 * <p>Implementation is thread-safe.
 *
 * <p>The data is parsed using {@link #parse(String, Object)}.
 *
 * <p>Sample usage:
 *
 * <pre>
 * static void setParser(HttpTransport transport) {
 * transport.addParser(new UrlEncodedParser());
 * }
 * </pre>
 *
 * @since 1.0
 * @author Yaniv Inbar
 */
public class UrlEncodedParser implements ObjectParser {

  /** {@code "application/x-www-form-urlencoded"} content type. */
  public static final String CONTENT_TYPE = "application/x-www-form-urlencoded";

  /**
   * {@code "application/x-www-form-urlencoded"} media type with UTF-8 encoding.
   *
   * @since 1.13
   */
  public static final String MEDIA_TYPE =
      new HttpMediaType(UrlEncodedParser.CONTENT_TYPE).setCharsetParameter(Charsets.UTF_8).build();
  /**
   * Parses the given URL-encoded content into the given data object of data key name/value pairs
   * using {@link #parse(Reader, Object)}.
   *
   * @param content URL-encoded content or {@code null} to ignore content
   * @param data data key name/value pairs
   */
  public static void parse(String content, Object data) {
    parse(content, data, true);
  }

  /**
   * Parses the given URL-encoded content into the given data object of data key name/value pairs
   * using {@link #parse(Reader, Object)}.
   *
   * @param content URL-encoded content or {@code null} to ignore content
   * @param data data key name/value pairs
   * @param decodeEnabled flag that specifies whether decoding should be enabled.
   */
  public static void parse(String content, Object data, boolean decodeEnabled) {
    if (content == null) {
      return;
    }
    try {
      parse(new StringReader(content), data, decodeEnabled);
    } catch (IOException exception) {
      // I/O exception not expected on a string
      throw Throwables.propagate(exception);
    }
  }

  /**
   * Parses the given URL-encoded content into the given data object of data key name/value pairs,
   * including support for repeating data key names.
   *
   * <p>Declared fields of a "primitive" type (as defined by {@link Data#isPrimitive(Type)} are
   * parsed using {@link Data#parsePrimitiveValue(Type, String)} where the {@link Class} parameter
   * is the declared field class. Declared fields of type {@link Collection} are used to support
   * repeating data key names, so each member of the collection is an additional data key value.
   * They are parsed the same as "primitive" fields, except that the generic type parameter of the
   * collection is used as the {@link Class} parameter.
   *
   * <p>If there is no declared field for an input parameter name, it will be ignored unless the
   * input {@code data} parameter is a {@link Map}. If it is a map, the parameter value will be
   * stored either as a string, or as a {@link ArrayList}&lt;String&gt; in the case of repeated
   * parameters.
   *
   * @param reader URL-encoded reader
   * @param data data key name/value pairs
   * @since 1.14
   */
    public static void parse(Reader reader, Object data) throws IOException {
      parse(reader, data, true);
    }
 
  /**
   * Parses the given URL-encoded content into the given data object of data key name/value pairs,
   * including support for repeating data key names.
   *
   * <p>Declared fields of a "primitive" type (as defined by {@link Data#isPrimitive(Type)} are
   * parsed using {@link Data#parsePrimitiveValue(Type, String)} where the {@link Class} parameter
   * is the declared field class. Declared fields of type {@link Collection} are used to support
   * repeating data key names, so each member of the collection is an additional data key value.
   * They are parsed the same as "primitive" fields, except that the generic type parameter of the
   * collection is used as the {@link Class} parameter.
   *
   * <p>If there is no declared field for an input parameter name, it is ignored unless the
   * input {@code data} parameter is a {@link Map}. If it is a map, the parameter value is 
   * stored either as a string, or as a {@link ArrayList}&lt;String&gt; in the case of repeated
   * parameters.
   *
   * @param reader URL-encoded reader
   * @param data data key name/value pairs
   * @param decodeEnabled flag that specifies whether data should be decoded.
   * @since 1.14
   */
  public static void parse(Reader reader, Object data, boolean decodeEnabled) throws IOException {
    Class<?> clazz = data.getClass();
    ClassInfo classInfo = ClassInfo.of(clazz);
    List<Type> context = Arrays.<Type>asList(clazz);
    GenericData genericData = GenericData.class.isAssignableFrom(clazz) ? (GenericData) data : null;
    @SuppressWarnings("unchecked")
    Map<Object, Object> map = Map.class.isAssignableFrom(clazz) ? (Map<Object, Object>) data : null;
    ArrayValueMap arrayValueMap = new ArrayValueMap(data);
    StringWriter nameWriter = new StringWriter();
    StringWriter valueWriter = new StringWriter();
    boolean readingName = true;
    mainLoop:
    while (true) {
      int read = reader.read();
      switch (read) {
        case -1:
          // falls through
        case '&':
          // parse name/value pair
          String name = decodeEnabled ?  CharEscapers.decodeUri(nameWriter.toString()) : nameWriter.toString();
          if (name.length() != 0) {
            String stringValue = decodeEnabled ? CharEscapers.decodeUri(valueWriter.toString()) : valueWriter.toString();
            // get the field from the type information
            FieldInfo fieldInfo = classInfo.getFieldInfo(name);
            if (fieldInfo != null) {
              Type type =
                  Data.resolveWildcardTypeOrTypeVariable(context, fieldInfo.getGenericType());
              // type is now class, parameterized type, or generic array type
              if (Types.isArray(type)) {
                // array that can handle repeating values
                Class<?> rawArrayComponentType =
                    Types.getRawArrayComponentType(context, Types.getArrayComponentType(type));
                arrayValueMap.put(
                    fieldInfo.getField(),
                    rawArrayComponentType,
                    parseValue(rawArrayComponentType, context, stringValue));
              } else if (Types.isAssignableToOrFrom(
                  Types.getRawArrayComponentType(context, type), Iterable.class)) {
                // iterable that can handle repeating values
                @SuppressWarnings("unchecked")
                Collection<Object> collection = (Collection<Object>) fieldInfo.getValue(data);
                if (collection == null) {
                  collection = Data.newCollectionInstance(type);
                  fieldInfo.setValue(data, collection);
                }
                Type subFieldType = type == Object.class ? null : Types.getIterableParameter(type);
                collection.add(parseValue(subFieldType, context, stringValue));
              } else {
                // parse into a field that assumes it is a single value
                fieldInfo.setValue(data, parseValue(type, context, stringValue));
              }
            } else if (map != null) {
              // parse into a map: store as an ArrayList of values
              @SuppressWarnings("unchecked")
              ArrayList<String> listValue = (ArrayList<String>) map.get(name);
              if (listValue == null) {
                listValue = new ArrayList<String>();
                if (genericData != null) {
                  genericData.set(name, listValue);
                } else {
                  map.put(name, listValue);
                }
              }
              listValue.add(stringValue);
            }
          }
          // ready to read next name/value pair
          readingName = true;
          nameWriter = new StringWriter();
          valueWriter = new StringWriter();
          if (read == -1) {
            break mainLoop;
          }
          break;
        case '=':
          if (readingName) {
            // finished with name, now read value
            readingName = false;
          } else {
            // '=' is in the value
            valueWriter.write(read);
          }
          break;
        default:
          // read one more character
          if (readingName) {
            nameWriter.write(read);
          } else {
            valueWriter.write(read);
          }
      }
    }
    arrayValueMap.setValues();
  }

  private static Object parseValue(Type valueType, List<Type> context, String value) {
    Type resolved = Data.resolveWildcardTypeOrTypeVariable(context, valueType);
    return Data.parsePrimitiveValue(resolved, value);
  }

  public <T> T parseAndClose(InputStream in, Charset charset, Class<T> dataClass)
      throws IOException {
    InputStreamReader r = new InputStreamReader(in, charset);
    return parseAndClose(r, dataClass);
  }

  public Object parseAndClose(InputStream in, Charset charset, Type dataType) throws IOException {
    InputStreamReader r = new InputStreamReader(in, charset);
    return parseAndClose(r, dataType);
  }

  @SuppressWarnings("unchecked")
  public <T> T parseAndClose(Reader reader, Class<T> dataClass) throws IOException {
    return (T) parseAndClose(reader, (Type) dataClass);
  }

  public Object parseAndClose(Reader reader, Type dataType) throws IOException {
    Preconditions.checkArgument(
        dataType instanceof Class<?>, "dataType has to be of type Class<?>");

    Object newInstance = Types.newInstance((Class<?>) dataType);
    parse(new BufferedReader(reader), newInstance);
    return newInstance;
  }
}
