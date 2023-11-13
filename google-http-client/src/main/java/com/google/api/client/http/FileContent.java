/*
 * Copyright 2011 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.api.client.http;

import com.google.api.client.util.Preconditions;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

/**
 * Concrete implementation of {@link AbstractInputStreamContent} that generates repeatable input
 * streams based on the contents of a file.
 *
 * <p>Sample use:
 *
 * <pre>
 * <code>
 * private static void setRequestJpegContent(HttpRequest request, File jpegFile) {
 * request.setContent(new FileContent("image/jpeg", jpegFile));
 * }
 * </code>
 * </pre>
 *
 * <p>Implementation is not thread-safe.
 *
 * @since 1.4
 * @author moshenko@google.com (Jacob Moshenko)
 */
public final class FileContent extends AbstractInputStreamContent {

  private final File file;

  /**
   * @param type Content type or {@code null} for none
   * @param file file
   * @since 1.5
   */
  public FileContent(String type, File file) {
    super(type);
    this.file = Preconditions.checkNotNull(file);
  }

  public long getLength() {
    return file.length();
  }

  public boolean retrySupported() {
    return true;
  }

  @Override
  public InputStream getInputStream() throws FileNotFoundException {
    return new FileInputStream(file);
  }

  /**
   * Returns the file.
   *
   * @since 1.5
   */
  public File getFile() {
    return file;
  }

  @Override
  public FileContent setType(String type) {
    return (FileContent) super.setType(type);
  }

  @Override
  public FileContent setCloseInputStream(boolean closeInputStream) {
    return (FileContent) super.setCloseInputStream(closeInputStream);
  }
}
