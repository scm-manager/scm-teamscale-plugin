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

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryTestData;

import java.util.Optional;

import static org.assertj.core.api.Java6Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ConfigurationProviderTest {

  private Repository REPOSITORY = RepositoryTestData.createHeartOfGold();

  private String TEST_URL = "http://www.scm-manager.org/teamscale";

  @Mock
  private ConfigStore configStore;

  @InjectMocks
  private ConfigurationProvider configurationProvider;

  @Test
  void shouldReturnGlobalConfigurationIfRepoConfigDisabled() {
    when(configStore.getGlobalConfiguration()).thenReturn(createGlobalConfiguration(TEST_URL, true));
    when(configStore.getConfiguration(REPOSITORY)).thenReturn(createConfiguration(TEST_URL));

    Optional<Configuration> configuration = configurationProvider.evaluateConfiguration(REPOSITORY);

    assertThat(configuration.isPresent()).isTrue();
    Configuration globalConfig = configuration.get();
    assertThat(globalConfig.isValid()).isTrue();
    assertThat(globalConfig.getUrl()).isEqualTo(TEST_URL);
    assertThat(globalConfig).isInstanceOf(GlobalConfiguration.class);
  }

  @Test
  void shouldReturnGlobalConfigurationIfRepoConfigInvalid() {
    when(configStore.getGlobalConfiguration()).thenReturn(createGlobalConfiguration(TEST_URL, false));
    when(configStore.getConfiguration(REPOSITORY)).thenReturn(createConfiguration(""));

    Optional<Configuration> configuration = configurationProvider.evaluateConfiguration(REPOSITORY);

    assertThat(configuration.isPresent()).isTrue();
    Configuration globalConfig = configuration.get();
    assertThat(globalConfig.getUrl()).isEqualTo(TEST_URL);
    assertThat(globalConfig).isInstanceOf(GlobalConfiguration.class);
  }

  @Test
  void shouldReturnRepoConfig() {
    when(configStore.getGlobalConfiguration()).thenReturn(createGlobalConfiguration(TEST_URL,false));
    when(configStore.getConfiguration(REPOSITORY)).thenReturn(createConfiguration(TEST_URL));

    Optional<Configuration> configuration = configurationProvider.evaluateConfiguration(REPOSITORY);

    assertThat(configuration.isPresent()).isTrue();
    Configuration repoConfig = configuration.get();
    assertThat(repoConfig.isValid()).isTrue();
    assertThat(repoConfig.getUrl()).isEqualTo(TEST_URL);
    assertThat(repoConfig).isInstanceOf(Configuration.class);
  }

  @Test
  void shouldReturnEmptyOptionalIfNoValidConfigExist() {
    when(configStore.getGlobalConfiguration()).thenReturn(createGlobalConfiguration("",false));
    when(configStore.getConfiguration(REPOSITORY)).thenReturn(createConfiguration(""));

    Optional<Configuration> configuration = configurationProvider.evaluateConfiguration(REPOSITORY);

    assertThat(configuration.isPresent()).isFalse();
  }

  private GlobalConfiguration createGlobalConfiguration(String url, boolean disabledRepoConfig) {
    return new GlobalConfiguration(url, disabledRepoConfig);
  }

  private Configuration createConfiguration(String url) {
    return new Configuration(url);
  }
}
