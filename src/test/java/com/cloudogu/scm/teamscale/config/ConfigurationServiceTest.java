package com.cloudogu.scm.teamscale.config;

import org.apache.shiro.authz.AuthorizationException;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.util.ThreadContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.NotFoundException;
import sonia.scm.repository.NamespaceAndName;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryManager;
import sonia.scm.repository.RepositoryTestData;

import static org.assertj.core.api.Java6Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ConfigurationServiceTest {

  Repository REPOSITORY = RepositoryTestData.createHeartOfGold();

  @Mock
  private ConfigStore configStore;
  @Mock
  private RepositoryManager repositoryManager;
  @Mock
  private ConfigurationMapper mapper;
  @Mock
  Subject subject;

  @InjectMocks
  private ConfigurationService service;

  @BeforeEach
  void bindSubject() {
    ThreadContext.bind(subject);
  }

  @AfterEach
  void unbindSubject() {
    ThreadContext.unbindSubject();
  }

  @Nested
  class WithRepository {

    @BeforeEach
    void initRepository() {
      when(repositoryManager.get(new NamespaceAndName(REPOSITORY.getNamespace(), REPOSITORY.getName()))).thenReturn(REPOSITORY);
    }

    @Test
    void shouldGetEmptyConfiguration() {
      String url = "http://scm-manager.org/teamscale";
      Configuration emptyConfig = new Configuration();
      when(configStore.getConfiguration(REPOSITORY)).thenReturn(emptyConfig);
      when(mapper.map(emptyConfig, REPOSITORY)).thenReturn(new ConfigurationDto(url));
      ConfigurationDto configuration = service.getRepositoryConfiguration(REPOSITORY.getNamespace(), REPOSITORY.getName());
      assertThat(configuration.getUrl()).isEqualTo(url);
    }

    @Test
    void shouldGetRepositoryConfig() {
      Configuration newConfig = new Configuration("hitchhiker.org");
      when(configStore.getConfiguration(REPOSITORY)).thenReturn(newConfig);
      when(mapper.map(newConfig, REPOSITORY)).thenReturn(new ConfigurationDto("hitchhiker.org"));
      ConfigurationDto configuration = service.getRepositoryConfiguration(REPOSITORY.getNamespace(), REPOSITORY.getName());
      assertThat(configuration.getUrl()).isEqualTo(newConfig.getUrl());
    }

    @Test
    void shouldThrowExceptionIfNotPermittedToGetConfig() {
      doThrow(AuthorizationException.class).when(subject).checkPermission(anyString());
      assertThrows(AuthorizationException.class, () -> service.getRepositoryConfiguration(REPOSITORY.getNamespace(), REPOSITORY.getName()));
    }

    @Test
    void shouldThrowExceptionIfNotPermittedToUpdateConfig() {
      ConfigurationDto newConfigDto = new ConfigurationDto("scm-manager.org");
      doThrow(AuthorizationException.class).when(subject).checkPermission(anyString());
      assertThrows(AuthorizationException.class, () -> service.updateRepositoryConfiguration(REPOSITORY.getNamespace(), REPOSITORY.getName(), newConfigDto));
    }

    @Test
    void shouldUpdateRepositoryConfig() {
      Configuration oldConfig = new Configuration("hitchhiker.org");
      when(configStore.getConfiguration(REPOSITORY)).thenReturn(oldConfig);

      ConfigurationDto newConfigDto = new ConfigurationDto("scm-manager.org");
      service.updateRepositoryConfiguration(REPOSITORY.getNamespace(), REPOSITORY.getName(), newConfigDto);

      verify(mapper).map(newConfigDto, oldConfig);
    }
  }

  @Test
  void shouldGetGlobalConfig() {
    GlobalConfiguration newConfig = createGlobalConfiguration();
    GlobalConfigurationDto globalConfigurationDto = createGlobalConfigurationDto();
    when(configStore.getGlobalConfiguration()).thenReturn(newConfig);
    when(mapper.map(newConfig)).thenReturn(globalConfigurationDto);
    GlobalConfigurationDto configuration = service.getGlobalConfiguration();

    assertThat(configuration.getUrl()).isEqualTo(newConfig.getUrl());
    assertThat(configuration.isDisableRepositoryConfiguration()).isFalse();
  }

  @Test
  void shouldUpdateGlobalConfig() {
    GlobalConfiguration oldConfig = createGlobalConfiguration();
    when(configStore.getGlobalConfiguration()).thenReturn(oldConfig);

    GlobalConfigurationDto newConfigDto = createGlobalConfigurationDto();
    service.updateGlobalConfiguration(newConfigDto);

    verify(mapper).map(newConfigDto, oldConfig);
  }

  @Test
  void shouldThrowExceptionIfRepositoryNotFound() {
    assertThrows(NotFoundException.class, () -> service.getRepositoryConfiguration("not", "found"));
  }

  private GlobalConfigurationDto createGlobalConfigurationDto() {
    GlobalConfigurationDto configuration = new GlobalConfigurationDto();
    configuration.setUrl("");
    configuration.setDisableRepositoryConfiguration(false);
    return configuration;
  }

  private GlobalConfiguration createGlobalConfiguration() {
    GlobalConfiguration configuration = new GlobalConfiguration();
    configuration.setUrl("");
    configuration.setDisableRepositoryConfiguration(false);
    return configuration;
  }
}


