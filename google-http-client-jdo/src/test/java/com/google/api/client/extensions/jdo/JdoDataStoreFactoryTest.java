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

package com.google.api.client.extensions.jdo;

import com.google.api.client.test.util.store.AbstractDataStoreFactoryTest;
import com.google.api.client.util.store.DataStoreFactory;
import com.google.api.client.util.store.MemoryDataStoreFactory;

import java.io.IOException;
import java.util.Properties;

import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManagerFactory;

/**
 * Tests {@link JdoDataStoreFactory}.
 *
 * @author Yaniv Inbar
 */
public class JdoDataStoreFactoryTest extends AbstractDataStoreFactoryTest {

  /**
   * This test is *disabled* by default just because you need to run special set up steps first.
   * Specifically on Linux:
   *
   * <pre>
sudo apt-get install mysql-server
mysql -u root -p
create database mytest;
   * </pre>
   *
   * Then update ConnectionUserName and ConnectionPassword below.
   */
  static boolean ENABLE_TEST = false;

  static class PersistenceManagerFactoryHolder {
    static PersistenceManagerFactory pmf;

    public static PersistenceManagerFactory get() {
      return pmf;
    }

    static {
      Properties properties = new Properties();
      properties.setProperty("javax.jdo.PersistenceManagerFactoryClass",
          "org.datanucleus.api.jdo.JDOPersistenceManagerFactory");
      properties.setProperty("javax.jdo.option.ConnectionDriverName", "com.mysql.jdbc.Driver");
      properties.setProperty(
          "javax.jdo.option.ConnectionURL", "jdbc:mysql://localhost:3306/mytest");
      properties.setProperty("javax.jdo.option.ConnectionUserName", "root");
      properties.setProperty("javax.jdo.option.ConnectionPassword", "");
      properties.setProperty("datanucleus.autoCreateSchema", "true");
      pmf = JDOHelper.getPersistenceManagerFactory(properties);
    }
  }

  @Override
  protected DataStoreFactory newDataStoreFactory() throws IOException {
    // hack: if test not enabled run memory data store which we know works
    return ENABLE_TEST
        ? new JdoDataStoreFactory(PersistenceManagerFactoryHolder.get()) : new MemoryDataStoreFactory();
  }
}
