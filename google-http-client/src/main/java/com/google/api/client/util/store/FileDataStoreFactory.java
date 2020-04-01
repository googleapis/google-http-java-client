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

package com.google.api.client.util.store;

import com.google.api.client.util.IOUtils;
import com.google.api.client.util.Maps;

import com.google.common.base.StandardSystemProperty;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.AclEntry;
import java.nio.file.attribute.AclEntryPermission;
import java.nio.file.attribute.AclEntryType;
import java.nio.file.attribute.AclFileAttributeView;
import java.nio.file.attribute.FileOwnerAttributeView;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.UserPrincipal;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.logging.Logger;

/**
 * Thread-safe file implementation of a credential store.
 *
 * <p>For security purposes, the file's permissions are set such that the
 * file is only accessible by the file's owner.
 *
 * @since 1.16
 * @author Yaniv Inbar
 */
public class FileDataStoreFactory extends AbstractDataStoreFactory {

  private static final Logger LOGGER = Logger.getLogger(FileDataStoreFactory.class.getName());

  private static final boolean IS_WINDOWS = StandardSystemProperty.OS_NAME.value()
      .toLowerCase(Locale.ENGLISH).startsWith("windows");

  /** Directory to store data. */
  private final File dataDirectory;

  /** @param dataDirectory data directory */
  public FileDataStoreFactory(File dataDirectory) throws IOException {
    dataDirectory = dataDirectory.getCanonicalFile();
    this.dataDirectory = dataDirectory;
    // error if it is a symbolic link
    if (IOUtils.isSymbolicLink(dataDirectory)) {
      throw new IOException("unable to use a symbolic link: " + dataDirectory);
    }
    // create parent directory (if necessary)
    if (!dataDirectory.exists() && !dataDirectory.mkdirs()) {
      throw new IOException("unable to create directory: " + dataDirectory);
    }

    if (IS_WINDOWS) {
      setPermissionsToOwnerOnlyWindows(dataDirectory);
    } else {
      setPermissionsToOwnerOnly(dataDirectory);
    }
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
   * @throws IOException if the permissions can't be set
   */
  private static void setPermissionsToOwnerOnly(File file) throws IOException {
    Set permissions = new HashSet<PosixFilePermission>();
    permissions.add(PosixFilePermission.OWNER_READ);
    permissions.add(PosixFilePermission.OWNER_WRITE);
    permissions.add(PosixFilePermission.OWNER_EXECUTE);
    try {
      Files.setPosixFilePermissions(Paths.get(file.getAbsolutePath()), permissions);
    } catch (RuntimeException exception) {
      throw new IOException("Unable to set permissions for " + file, exception);
    }
  }

  private static void setPermissionsToOwnerOnlyWindows(File file) throws IOException {
    Path path = Paths.get(file.getAbsolutePath());
    FileOwnerAttributeView fileAttributeView = Files.getFileAttributeView(path, FileOwnerAttributeView.class);
    UserPrincipal owner = fileAttributeView.getOwner();

    // get view
    AclFileAttributeView view = Files.getFileAttributeView(path, AclFileAttributeView.class);

    // All available entries
    Set<AclEntryPermission> permissions = ImmutableSet.of(
        AclEntryPermission.APPEND_DATA,
        AclEntryPermission.DELETE,
        AclEntryPermission.DELETE_CHILD,
        AclEntryPermission.READ_ACL,
        AclEntryPermission.READ_ATTRIBUTES,
        AclEntryPermission.READ_DATA,
        AclEntryPermission.READ_NAMED_ATTRS,
        AclEntryPermission.SYNCHRONIZE,
        AclEntryPermission.WRITE_ACL,
        AclEntryPermission.WRITE_ATTRIBUTES,
        AclEntryPermission.WRITE_DATA,
        AclEntryPermission.WRITE_NAMED_ATTRS,
        AclEntryPermission.WRITE_OWNER
    );

    // create ACL to give owner everything
    AclEntry entry = AclEntry.newBuilder()
        .setType(AclEntryType.ALLOW)
        .setPrincipal(owner)
        .setPermissions(permissions)
        .build();

    // Overwrite the ACL with only this permission
    try {
      view.setAcl(ImmutableList.of(entry));
    } catch (SecurityException ex) {
      throw new IOException("Unable to set permissions for " + file, ex);
    }

  }
}
