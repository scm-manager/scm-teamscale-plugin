package com.cloudogu.scm.teamscale.config;

import com.cloudogu.scm.teamscale.Constants;
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
    Configuration configuration = getRepositoryConfigurationOrEmpty(repository);
    return mapper.map(configuration, repository);
  }

  public void updateConfig(String namespace, String name, ConfigurationDto updatedConfig) {
    Repository repository = loadRepository(namespace, name);
    RepositoryPermissions.custom(Constants.NAME, repository).check();
    configStore.storeConfiguration(mapper.map(updatedConfig, getRepositoryConfigurationOrEmpty(repository)), repository);
  }

  private Configuration getRepositoryConfigurationOrEmpty(Repository repository) {
    Configuration configuration = configStore.getConfiguration(repository);
    if (configuration == null) {
      return new Configuration();
    }
    return configuration;
  }

  private Repository loadRepository(String namespace, String name) {
    Repository repository = repositoryManager.get(new NamespaceAndName(namespace, name));
    if (repository == null) {
      throw notFound(entity(new NamespaceAndName(namespace, name)));
    }
    return repository;
  }
}
