/*
 * Copyright (c) 2013 Google Inc.
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

import junit.framework.TestCase;

import java.io.File;
import java.io.IOException;

/**
 * Tests {@link IOUtils}.
 *
 * @author Yaniv Inbar
 */
public class IOUtilsTest extends TestCase {

  static final String VALUE = "abc";

  public void testSerialize() throws IOException {
    byte[] bytes = IOUtils.serialize(VALUE);
    assertEquals(VALUE, IOUtils.deserialize(bytes));
  }

  public void testDeserialize() throws IOException {
    assertNull(IOUtils.deserialize((byte[]) null));
  }

  public void testIsSymbolicLink_false() throws IOException {
    File file = File.createTempFile("tmp", null);
    file.deleteOnExit();
    assertFalse(IOUtils.isSymbolicLink(file));
  }

  public void testIsSymbolicLink_true() throws IOException, InterruptedException {
    File file = File.createTempFile("tmp", null);
    file.deleteOnExit();
    File file2 = new File(file.getCanonicalPath() + "2");
    file2.deleteOnExit();
    try {
      Process process = Runtime.getRuntime()
          .exec(new String[] {"ln", "-s", file.getCanonicalPath(), file2.getCanonicalPath()});
      process.waitFor();
      process.destroy();
    } catch (IOException e) {
      // ignore because ln command may not be defined
      return;
    }
    assertTrue(IOUtils.isSymbolicLink(file2));
  }
}
