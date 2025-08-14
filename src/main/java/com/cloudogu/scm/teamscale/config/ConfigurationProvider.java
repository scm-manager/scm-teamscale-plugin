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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.repository.Repository;

import jakarta.inject.Inject;
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
