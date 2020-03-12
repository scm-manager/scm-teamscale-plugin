package com.cloudogu.scm.teamscale.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.repository.Repository;

import javax.inject.Inject;
import java.util.Optional;

public class ConfigurationProvider {

  private static final Logger LOG = LoggerFactory.getLogger(ConfigurationProvider.class);

  private final ConfigStore configStore;

  @Inject
  public ConfigurationProvider(ConfigStore configStore) {
    this.configStore = configStore;
  }

  public Optional<Configuration> evaluateConfiguration(Repository repository) {
    GlobalConfiguration globalConfiguration = configStore.getGlobalConfiguration();
    Configuration repoConfig = configStore.getConfiguration(repository);

    if (!globalConfiguration.isDisableRepositoryConfiguration() && repoConfig.isValid()) {
      return Optional.of(repoConfig);
    } else if (globalConfiguration.isValid()) {
      return Optional.of(globalConfiguration);
    } else {
      LOG.debug("Could not send push notification to teamscale since no valid configuration exists.");
      return Optional.empty();
    }
  }
}
