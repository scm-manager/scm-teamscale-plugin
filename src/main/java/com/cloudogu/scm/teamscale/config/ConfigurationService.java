package com.cloudogu.scm.teamscale.config;

import com.cloudogu.scm.teamscale.Constants;
import sonia.scm.config.ConfigurationPermissions;
import sonia.scm.repository.NamespaceAndName;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryManager;
import sonia.scm.repository.RepositoryPermissions;

import javax.inject.Inject;

import static sonia.scm.ContextEntry.ContextBuilder.entity;
import static sonia.scm.NotFoundException.notFound;

public class ConfigurationService {

  private final ConfigStore configStore;
  private final RepositoryManager repositoryManager;
  private final ConfigurationMapper mapper;

  @Inject
  public ConfigurationService(ConfigStore configStore, RepositoryManager repositoryManager, ConfigurationMapper mapper) {
    this.configStore = configStore;
    this.repositoryManager = repositoryManager;
    this.mapper = mapper;
  }

  public ConfigurationDto getConfiguration(String namespace, String name) {
    Repository repository = loadRepository(namespace, name);
    RepositoryPermissions.custom(Constants.NAME, repository).check();
    Configuration configuration = configStore.getConfiguration(repository);
    return mapper.map(configuration, repository);
  }

  public void updateConfig(String namespace, String name, ConfigurationDto updatedConfig) {
    Repository repository = loadRepository(namespace, name);
    RepositoryPermissions.custom(Constants.NAME, repository).check();
    configStore.storeConfiguration(mapper.map(updatedConfig, configStore.getConfiguration(repository)), repository);
  }

  public GlobalConfigurationDto getGlobalConfiguration() {
    ConfigurationPermissions.read(Constants.NAME).check();
    GlobalConfiguration globalConfiguration = configStore.getGlobalConfiguration();
    return mapper.map(globalConfiguration);
  }

  public void updateGlobalConfiguration(GlobalConfigurationDto updatedGlobalConfiguration) {
    ConfigurationPermissions.write(Constants.NAME).check();
    configStore.storeGlobalConfiguration(mapper.map(updatedGlobalConfiguration, configStore.getGlobalConfiguration()));
  }

  private Repository loadRepository(String namespace, String name) {
    Repository repository = repositoryManager.get(new NamespaceAndName(namespace, name));
    if (repository == null) {
      throw notFound(entity(new NamespaceAndName(namespace, name)));
    }
    return repository;
  }
}
