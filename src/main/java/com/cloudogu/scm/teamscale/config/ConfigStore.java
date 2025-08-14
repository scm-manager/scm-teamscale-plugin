/*
 * Copyright (c) 2020 - present Cloudogu GmbH
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, version 3.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
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
