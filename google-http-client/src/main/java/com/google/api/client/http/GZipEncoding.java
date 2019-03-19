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

package com.google.api.client.http;

import com.google.api.client.util.StreamingContent;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.zip.GZIPOutputStream;

/**
 * GZip HTTP content encoding.
 *
 * @since 1.14
 * @author Yaniv Inbar
 */
public class GZipEncoding implements HttpEncoding {

  public String getName() {
    return "gzip";
  }

  public void encode(StreamingContent content, OutputStream out) throws IOException {
    // must not close the underlying output stream
    OutputStream out2 =
        new BufferedOutputStream(out) {
          @Override
          public void close() throws IOException {
            // copy implementation of super.close(), except do not close the underlying output
            // stream
            try {
              flush();
            } catch (IOException ignored) {
            }
          }
        };
    GZIPOutputStream zipper = new GZIPOutputStream(out2);
    content.writeTo(zipper);
    // cannot call just zipper.finish() because that would cause a severe memory leak
    zipper.close();
  }
}
