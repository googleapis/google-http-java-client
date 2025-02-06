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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/**
 * Tests {@link IOUtils}.
 *
 * @author Yaniv Inbar
 */
@RunWith(JUnit4.class)
public class IOUtilsTest {

  static final String VALUE = "abc";

  public void testSerialize() throws IOException {
    byte[] bytes = IOUtils.serialize(VALUE);
    assertEquals(VALUE, IOUtils.deserialize(bytes));
  }

  @Test
  public void testDeserialize() throws IOException {
    assertNull(IOUtils.deserialize((byte[]) null));
  }

  @Test
  public void testIsSymbolicLink_false() throws IOException {
    File file = File.createTempFile("tmp", null);
    file.deleteOnExit();
    assertFalse(IOUtils.isSymbolicLink(file));
  }

  @Test
  public void testIsSymbolicLink_true() throws IOException {
    File file = File.createTempFile("tmp", null);
    file.deleteOnExit();
    File file2 = new File(file.getCanonicalPath() + "2");
    file2.deleteOnExit();
    Files.createSymbolicLink(file2.toPath(), file.toPath());

    assertTrue(IOUtils.isSymbolicLink(file2));
  }
}
