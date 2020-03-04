package com.cloudogu.scm.teamscale.config;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import sonia.scm.repository.Repository;
import sonia.scm.store.ConfigurationStore;
import sonia.scm.store.ConfigurationStoreFactory;

@Singleton
public class ConfigStore {

  public static final String NAME = "teamscale";

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

  public Configuration getConfiguration(Repository repository) {
    return createStore(repository.getId()).getOptional().orElse(new Configuration());
  }

  private ConfigurationStore<Configuration> createStore(String repositoryId) {
    return storeFactory.withType(Configuration.class).withName(NAME).forRepository(repositoryId).build();
  }

}
