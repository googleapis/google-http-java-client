/*
 * Copyright 2020 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.google.api.client.extensions.android.util.store;

import com.google.api.client.util.IOUtils;
import com.google.api.client.util.Maps;
import com.google.api.client.util.store.AbstractDataStoreFactory;
import com.google.api.client.util.store.AbstractMemoryDataStore;
import com.google.api.client.util.store.DataStore;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.logging.Logger;

/**
 * Thread-safe file implementation of a credential store.
 *
 * <p>For security purposes, the file's permissions are set to be accessible only by the file's
 * owner.
 *
 * <p>Note: This class was branched from the primary implementation in google-http-client to allow
 * the mainline implementation to support Windows file permissions.
 *
 * @since 1.36
 * @author Yaniv Inbar
 */
public class FileDataStoreFactory extends AbstractDataStoreFactory {

  private static final Logger LOGGER = Logger.getLogger(FileDataStoreFactory.class.getName());

  /** Directory to store data. */
  private final File dataDirectory;

  /** @param dataDirectory data directory */
  public FileDataStoreFactory(File dataDirectory) throws IOException {
    dataDirectory = dataDirectory.getCanonicalFile();
    // error if it is a symbolic link
    if (IOUtils.isSymbolicLink(dataDirectory)) {
      throw new IOException("unable to use a symbolic link: " + dataDirectory);
    }
    // create parent directory (if necessary)
    if (!dataDirectory.exists() && !dataDirectory.mkdirs()) {
      throw new IOException("unable to create directory: " + dataDirectory);
    }
    this.dataDirectory = dataDirectory;
    setPermissionsToOwnerOnly(dataDirectory);
  }

  /** Returns the data directory. */
  public final File getDataDirectory() {
    return dataDirectory;
  }

  @Override
  protected <V extends Serializable> DataStore<V> createDataStore(String id) throws IOException {
    return new FileDataStore<V>(this, dataDirectory, id);
  }

  /**
   * File data store that inherits from the abstract memory data store because the key-value pairs
   * are stored in a memory cache, and saved in the file (see {@link #save()} when changing values.
   *
   * @param <V> serializable type of the mapped value
   */
  static class FileDataStore<V extends Serializable> extends AbstractMemoryDataStore<V> {

    /** File to store data. */
    private final File dataFile;

    FileDataStore(FileDataStoreFactory dataStore, File dataDirectory, String id)
        throws IOException {
      super(dataStore, id);
      this.dataFile = new File(dataDirectory, id);
      // error if it is a symbolic link
      if (IOUtils.isSymbolicLink(dataFile)) {
        throw new IOException("unable to use a symbolic link: " + dataFile);
      }
      // create new file (if necessary)
      if (dataFile.createNewFile()) {
        keyValueMap = Maps.newHashMap();
        // save the credentials to create a new file
        save();
      } else {
        // load credentials from existing file
        keyValueMap = IOUtils.deserialize(new FileInputStream(dataFile));
      }
    }

    @Override
    public void save() throws IOException {
      IOUtils.serialize(keyValueMap, new FileOutputStream(dataFile));
    }

    @Override
    public FileDataStoreFactory getDataStoreFactory() {
      return (FileDataStoreFactory) super.getDataStoreFactory();
    }
  }

  /**
   * Attempts to set the given file's permissions such that it can only be read, written, and
   * executed by the file's owner.
   *
   * @param file the file's permissions to modify
   */
  static void setPermissionsToOwnerOnly(File file) {
    // Disable access by other users if O/S allows it and set file permissions to readable and
    // writable by user.
    try {
      if (!file.setReadable(false, false)
          || !file.setWritable(false, false)
          || !file.setExecutable(false, false)) {
        LOGGER.warning("unable to change permissions for everybody: " + file);
      }
      if (!file.setReadable(true, true)
          || !file.setWritable(true, true)
          || !file.setExecutable(true, true)) {
        LOGGER.warning("unable to change permissions for owner: " + file);
      }
    } catch (SecurityException exception) {
      // ignored
    } catch (IllegalArgumentException exception) {
      // ignored
    }
  }
}
