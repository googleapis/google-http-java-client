/*
 * Copyright (c) 2012 Google Inc.
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

package com.google.api.client.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;

/**
 * Utilities for I/O streams.
 *
 * @author Yaniv Inbar
 * @since 1.14
 */
public class IOUtils {

  /**
   * Writes the content provided by the given source input stream into the given destination output
   * stream.
   *
   * <p>The input stream is guaranteed to be closed at the end of this method.
   *
   * <p>Sample use:
   *
   * <pre>
   * static void copy(InputStream inputStream, File file) throws IOException {
   * FileOutputStream out = new FileOutputStream(file);
   * try {
   * IOUtils.copy(inputStream, out);
   * } finally {
   * out.close();
   * }
   * }
   * </pre>
   *
   * @param inputStream source input stream
   * @param outputStream destination output stream
   */
  public static void copy(InputStream inputStream, OutputStream outputStream) throws IOException {
    copy(inputStream, outputStream, true);
  }

  /**
   * Writes the content provided by the given source input stream into the given destination output
   * stream, optionally closing the input stream.
   *
   * <p>Sample use:
   *
   * <pre>
   * static void copy(InputStream inputStream, File file) throws IOException {
   * FileOutputStream out = new FileOutputStream(file);
   * try {
   * IOUtils.copy(inputStream, out, true);
   * } finally {
   * out.close();
   * }
   * }
   * </pre>
   *
   * @param inputStream source input stream
   * @param outputStream destination output stream
   * @param closeInputStream whether the input stream should be closed at the end of this method
   */
  public static void copy(
      InputStream inputStream, OutputStream outputStream, boolean closeInputStream)
      throws IOException {
    try {
      ByteStreams.copy(inputStream, outputStream);
    } finally {
      if (closeInputStream) {
        inputStream.close();
      }
    }
  }

  /**
   * Computes and returns the byte content length for a streaming content by calling {@link
   * StreamingContent#writeTo(OutputStream)} on a fake output stream that only counts bytes written.
   *
   * @param content streaming content
   */
  public static long computeLength(StreamingContent content) throws IOException {
    ByteCountingOutputStream countingStream = new ByteCountingOutputStream();
    try {
      content.writeTo(countingStream);
    } finally {
      countingStream.close();
    }
    return countingStream.count;
  }

  /**
   * Serializes the given object value to a newly allocated byte array.
   *
   * @param value object value to serialize
   * @since 1.16
   */
  public static byte[] serialize(Object value) throws IOException {
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    serialize(value, out);
    return out.toByteArray();
  }

  /**
   * Serializes the given object value to an output stream, and close the output stream.
   *
   * @param value object value to serialize
   * @param outputStream output stream to serialize into
   * @since 1.16
   */
  public static void serialize(Object value, OutputStream outputStream) throws IOException {
    try {
      new ObjectOutputStream(outputStream).writeObject(value);
    } finally {
      outputStream.close();
    }
  }

  /**
   * Deserializes the given byte array into to a newly allocated object.
   *
   * @param bytes byte array to deserialize or {@code null} for {@code null} result
   * @return new allocated object or {@code null} for {@code null} input
   * @since 1.16
   */
  public static <S extends Serializable> S deserialize(byte[] bytes) throws IOException {
    if (bytes == null) {
      return null;
    }
    return deserialize(new ByteArrayInputStream(bytes));
  }

  /**
   * Deserializes the given input stream into to a newly allocated object, and close the input
   * stream.
   *
   * @param inputStream input stream to deserialize
   * @since 1.16
   */
  @SuppressWarnings("unchecked")
  public static <S extends Serializable> S deserialize(InputStream inputStream) throws IOException {
    try {
      return (S) new ObjectInputStream(inputStream).readObject();
    } catch (ClassNotFoundException exception) {
      IOException ioe = new IOException("Failed to deserialize object");
      ioe.initCause(exception);
      throw ioe;
    } finally {
      inputStream.close();
    }
  }

  /**
   * Returns whether the given file is a symbolic link.
   *
   * @since 1.16
   */
  public static boolean isSymbolicLink(File file) throws IOException {
    // first try using Java 7
    try {
      // use reflection here
      Class<?> filesClass = Class.forName("java.nio.file.Files");
      Class<?> pathClass = Class.forName("java.nio.file.Path");
      Object path = File.class.getMethod("toPath").invoke(file);
      return ((Boolean) filesClass.getMethod("isSymbolicLink", pathClass).invoke(null, path))
          .booleanValue();
    } catch (InvocationTargetException exception) {
      Throwable cause = exception.getCause();
      Throwables.propagateIfPossible(cause, IOException.class);
      // shouldn't reach this point, but just in case...
      throw new RuntimeException(cause);
    } catch (ClassNotFoundException exception) {
      // handled below
    } catch (IllegalArgumentException exception) {
      // handled below
    } catch (SecurityException exception) {
      // handled below
    } catch (IllegalAccessException exception) {
      // handled below
    } catch (NoSuchMethodException exception) {
      // handled below
    }
    // backup option compatible with earlier Java
    // this won't work on Windows, which is where separator char is '\\'
    if (File.separatorChar == '\\') {
      return false;
    }
    File canonical = file;
    if (file.getParent() != null) {
      canonical = new File(file.getParentFile().getCanonicalFile(), file.getName());
    }
    return !canonical.getCanonicalFile().equals(canonical.getAbsoluteFile());
  }
}
