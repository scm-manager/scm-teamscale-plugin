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
