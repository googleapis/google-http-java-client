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

package com.google.api.client.test.util.store;

import static java.nio.file.Files.createTempDirectory;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.google.api.client.util.store.DataStore;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.common.collect.ImmutableSet;
import java.io.File;
import java.io.IOException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/**
 * Tests {@link FileDataStoreFactory}.
 *
 * @author Yaniv Inbar
 */
@RunWith(JUnit4.class)
public class FileDataStoreFactoryTest extends AbstractDataStoreFactoryTest {

  @Override
  protected FileDataStoreFactory newDataStoreFactory() throws IOException {
    File dataDir = createTempDirectory("temp").toFile();
    dataDir.deleteOnExit();
    return new FileDataStoreFactory(dataDir);
  }

  @Test
  public void testSave() throws IOException {
    FileDataStoreFactory factory = newDataStoreFactory();
    DataStore<String> store = factory.getDataStore("foo");
    store.set("k", "v");
    assertEquals(
        ImmutableSet.of("k"),
        new FileDataStoreFactory(factory.getDataDirectory()).getDataStore("foo").keySet());
    store.clear();
    assertTrue(new FileDataStoreFactory(factory.getDataDirectory()).getDataStore("foo").isEmpty());
  }
}
