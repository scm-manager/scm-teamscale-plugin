package com.cloudogu.scm.teamscale;

import com.cloudogu.scm.teamscale.config.ConfigStore;
import com.cloudogu.scm.teamscale.config.Configuration;
import com.cloudogu.scm.teamscale.config.GlobalConfiguration;
import com.google.common.collect.ImmutableList;
import com.google.inject.Provider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.ConfigurationException;
import sonia.scm.config.ScmConfiguration;
import sonia.scm.net.ahc.AdvancedHttpClient;
import sonia.scm.net.ahc.AdvancedHttpRequestWithBody;
import sonia.scm.repository.PostReceiveRepositoryHookEvent;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryTestData;
import sonia.scm.repository.api.HookBranchProvider;
import sonia.scm.repository.api.HookContext;
import sonia.scm.repository.api.HookFeature;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NotifierTest {

  private Repository REPOSITORY = RepositoryTestData.createHeartOfGold();

  private String SCM_BASE_URL = "http://www.scm-manager.org/scm";
  private String TEAMSCALE_INSTANCE_URL = "http://teamscale.scm-manager.org/";
  private String TEAMSCALE_HOOK_URL = TEAMSCALE_INSTANCE_URL + "/scm-manager-hook";

  @Mock
  private Provider<AdvancedHttpClient> httpClientProvider;
  @Mock
  private AdvancedHttpClient httpClient;
  @Mock(answer = Answers.RETURNS_SELF)
  private AdvancedHttpRequestWithBody body;
  @Mock
  private ConfigStore configStore;
  @Mock
  private ScmConfiguration scmConfiguration;
  @Mock
  private PostReceiveRepositoryHookEvent event;
  @Mock
  private HookContext hookContext;
  @Mock
  private HookBranchProvider branchProvider;

  @Captor
  private ArgumentCaptor<Notification> jsonCaptor;

  @InjectMocks
  private Notifier notifier;

  @BeforeEach
  void initConfiguration() {
    lenient().when(scmConfiguration.getBaseUrl()).thenReturn("http://www.scm-manager.org/scm");
  }

  @BeforeEach
  void initHttpClient() {
    lenient().when(httpClientProvider.get()).thenReturn(httpClient);
    lenient().when(httpClient.post(any())).thenReturn(body);
    lenient().when(body.jsonContent(jsonCaptor.capture())).thenReturn(body);
  }

  @BeforeEach
  void initEvent() {
    lenient().when(event.getRepository()).thenReturn(REPOSITORY);
    lenient().when(event.getContext()).thenReturn(hookContext);
  }

  @Nested
  class withValidConfigurations {

    @BeforeEach
    void initConfigStore() {
      when(configStore.getGlobalConfiguration()).thenReturn(createGlobalConfiguration(TEAMSCALE_INSTANCE_URL));
      when(configStore.getConfiguration(REPOSITORY)).thenReturn(createConfiguration(TEAMSCALE_INSTANCE_URL));
    }

    @Nested
    class WithBranchProvider {

      @BeforeEach
      void initBranchProvider() {
        when(hookContext.isFeatureSupported(HookFeature.BRANCH_PROVIDER)).thenReturn(true);
        when(hookContext.getBranchProvider()).thenReturn(branchProvider);
      }

      @Test
      void shouldSendNotificationsForMultipleBranches() {
        when(branchProvider.getCreatedOrModified()).thenReturn(ImmutableList.of("master", "develop", "feature/awesome"));

        notifier.sendCommitNotification(event);

        verify(httpClient, times(3)).post(TEAMSCALE_HOOK_URL);
      }

      @Test
      void shouldSendNotificationWithRepositoryData() {
        String branchName = "feature/awesome";
        String nameSpaceAndName = REPOSITORY.getNamespace() + "/" + REPOSITORY.getName();
        when(branchProvider.getCreatedOrModified()).thenReturn(ImmutableList.of(branchName));

        notifier.sendCommitNotification(event);

        Notification jsonValue = jsonCaptor.getValue();
        assertThat(jsonValue.getBranchName()).isEqualTo(branchName);
        assertThat(jsonValue.getRepositoryUrl()).isEqualTo(SCM_BASE_URL + "/repo/" + nameSpaceAndName);
        assertThat(jsonValue.getRepositoryId()).isEqualTo(nameSpaceAndName);
      }
    }

    @Test
    void shouldSendNotificationsWithoutBranchProvider() {
      notifier.sendCommitNotification(event);

      verify(httpClient).post(TEAMSCALE_HOOK_URL);
    }

    @Test
    void shouldSendNotificationWithoutBranchname() {
      String nameSpaceAndName = REPOSITORY.getNamespace() + "/" + REPOSITORY.getName();

      notifier.sendCommitNotification(event);

      Notification jsonValue = jsonCaptor.getValue();
      assertThat(jsonValue.getRepositoryUrl()).isEqualTo(SCM_BASE_URL + "/repo/" + nameSpaceAndName);
      assertThat(jsonValue.getRepositoryId()).isEqualTo(nameSpaceAndName);
    }
  }

  @Test
  void shouldUseRepoConfigIfValid() {
    when(configStore.getGlobalConfiguration()).thenReturn(createGlobalConfiguration(TEAMSCALE_INSTANCE_URL));
    when(configStore.getConfiguration(REPOSITORY)).thenReturn(createConfiguration(TEAMSCALE_INSTANCE_URL));

    notifier.sendCommitNotification(event);

    verify(httpClient).post(TEAMSCALE_HOOK_URL);
  }

  @Test
  void shouldUseGlobalConfigIfRepoConfigIsInvalid() {
    String globalUrl = TEAMSCALE_INSTANCE_URL + "global";
    when(configStore.getGlobalConfiguration()).thenReturn(createGlobalConfiguration(globalUrl));
    when(configStore.getConfiguration(REPOSITORY)).thenReturn(createConfiguration(""));

    notifier.sendCommitNotification(event);

    verify(httpClient).post(globalUrl + "/scm-manager-hook");
  }

  @Test
  void shouldThrowConfigurationExceptionIfNoValidConfigExsits() {
    when(configStore.getGlobalConfiguration()).thenReturn(createGlobalConfiguration(""));
    when(configStore.getConfiguration(REPOSITORY)).thenReturn(createConfiguration(""));

    assertThrows(ConfigurationException.class, () -> notifier.sendCommitNotification(event));
  }

  private GlobalConfiguration createGlobalConfiguration(String url) {
    GlobalConfiguration configuration = new GlobalConfiguration();
    configuration.setUrl(url);
    configuration.setUsername("trillian");
    configuration.setPassword("secret");
    configuration.setDisableRepositoryConfiguration(false);
    return configuration;
  }

  private Configuration createConfiguration(String url) {
    Configuration configuration = new Configuration();
    configuration.setUrl(url);
    configuration.setUsername("arthur");
    configuration.setPassword("secret");
    return configuration;
  }
}
