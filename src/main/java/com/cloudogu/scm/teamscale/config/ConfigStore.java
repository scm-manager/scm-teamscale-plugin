/*
 * MIT License
 *
 * Copyright (c) 2020-present Cloudogu GmbH and Contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.cloudogu.scm.teamscale.config;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import sonia.scm.repository.Repository;
import sonia.scm.store.ConfigurationStore;
import sonia.scm.store.ConfigurationStoreFactory;

import static com.cloudogu.scm.teamscale.Constants.NAME;

@Singleton
class ConfigStore {

  private final ConfigurationStoreFactory storeFactory;

  @Inject
  public ConfigStore(ConfigurationStoreFactory storeFactory) {
    this.storeFactory = storeFactory;
  }

  public void storeConfiguration(Configuration configuration, Repository repository) {
    storeConfiguration(configuration, repository.getId());
  }

  public void storeConfiguration(Configuration configuration, String repositoryId) {
    createStore(repositoryId).set(configuration);
  }

  public void storeGlobalConfiguration(GlobalConfiguration globalConfiguration) {
    createGlobalStore().set(globalConfiguration);
  }

  public Configuration getConfiguration(Repository repository) {
    return createStore(repository.getId()).getOptional().orElse(new Configuration());
  }

  public GlobalConfiguration getGlobalConfiguration() {
    return createGlobalStore().getOptional().orElse(new GlobalConfiguration());
  }

  private ConfigurationStore<Configuration> createStore(String repositoryId) {
    return storeFactory.withType(Configuration.class).withName(NAME).forRepository(repositoryId).build();
  }

  private ConfigurationStore<GlobalConfiguration> createGlobalStore() {
    return storeFactory.withType(GlobalConfiguration.class).withName(NAME).build();
  }
}
