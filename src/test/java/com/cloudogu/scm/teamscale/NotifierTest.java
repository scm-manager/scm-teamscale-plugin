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

package com.cloudogu.scm.teamscale;

import com.cloudogu.scm.review.pullrequest.service.PullRequest;
import com.cloudogu.scm.teamscale.config.Configuration;
import com.cloudogu.scm.teamscale.config.ConfigurationProvider;
import com.cloudogu.scm.teamscale.pullrequest.PullRequestCreatedNotification;
import com.google.inject.Provider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.config.ScmConfiguration;
import sonia.scm.net.ahc.AdvancedHttpClient;
import sonia.scm.net.ahc.AdvancedHttpRequestWithBody;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryTestData;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NotifierTest {

  private final Repository REPOSITORY = RepositoryTestData.createHeartOfGold();
  private final String EVENT_TYPE = "SCM-X-Event";
  private final String SCM_BASE_URL = "http://www.scm-manager.org/scm";
  private final String TEAMSCALE_INSTANCE_URL = "http://teamscale.scm-manager.org";
  private final String NAMESPACE_AND_NAME = REPOSITORY.getNamespace() + "/" + REPOSITORY.getName();
  private final String REPOSITORY_URL = SCM_BASE_URL + "/repo/" + NAMESPACE_AND_NAME;

  @Mock
  private Provider<AdvancedHttpClient> httpClientProvider;
  @Mock
  private AdvancedHttpClient httpClient;
  @Mock(answer = Answers.RETURNS_SELF)
  private AdvancedHttpRequestWithBody body;
  @Mock
  private ScmConfiguration scmConfiguration;
  @Mock
  private ConfigurationProvider configurationProvider;

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

  @Test
  void shouldSendPushNotificationWithBranchName() {
    when(configurationProvider.evaluateConfiguration(REPOSITORY)).thenReturn(createConfiguration(TEAMSCALE_INSTANCE_URL));

    String branchName = "feature/awesome";

    PushNotification notification = new PushNotification(REPOSITORY_URL, NAMESPACE_AND_NAME, branchName);

    notifier.notifyViaHttp(REPOSITORY, notification, EVENT_TYPE);

    PushNotification jsonValue = (PushNotification) jsonCaptor.getValue();
    assertThat(jsonValue.getBranchName()).isEqualTo(branchName);
    assertThat(jsonValue.getRepositoryUrl()).isEqualTo(SCM_BASE_URL + "/repo/" + NAMESPACE_AND_NAME);
    assertThat(jsonValue.getRepositoryId()).isEqualTo(NAMESPACE_AND_NAME);
  }

  @Test
  void shouldSendPushNotificationWithoutBranchname() {
    when(configurationProvider.evaluateConfiguration(REPOSITORY)).thenReturn(createConfiguration(TEAMSCALE_INSTANCE_URL));

    PushNotification notification = new PushNotification(REPOSITORY_URL, NAMESPACE_AND_NAME);

    notifier.notifyViaHttp(REPOSITORY, notification, EVENT_TYPE);

    Notification jsonValue = jsonCaptor.getValue();
    assertThat(jsonValue.getRepositoryUrl()).isEqualTo(SCM_BASE_URL + "/repo/" + NAMESPACE_AND_NAME);
    assertThat(jsonValue.getRepositoryId()).isEqualTo(NAMESPACE_AND_NAME);
  }

  @Test
  void shouldSendPullRequestCreatedNotification() {
    when(configurationProvider.evaluateConfiguration(REPOSITORY)).thenReturn(createConfiguration(TEAMSCALE_INSTANCE_URL));

    PullRequest pullRequest = createPullRequest();
    PullRequestCreatedNotification notification =
      new PullRequestCreatedNotification(REPOSITORY_URL, NAMESPACE_AND_NAME, pullRequest.getId(), pullRequest.getSource(), pullRequest.getTarget());

    notifier.notifyViaHttp(REPOSITORY, notification, EVENT_TYPE);

    PullRequestCreatedNotification jsonValue = (PullRequestCreatedNotification) jsonCaptor.getValue();
    assertThat(jsonValue.getRepositoryUrl()).isEqualTo(SCM_BASE_URL + "/repo/" + NAMESPACE_AND_NAME);
    assertThat(jsonValue.getRepositoryId()).isEqualTo(NAMESPACE_AND_NAME);
    assertThat(jsonValue.getPullRequestId()).isEqualTo(pullRequest.getId());
    assertThat(jsonValue.getSourceBranch()).isEqualTo(pullRequest.getSource());
    assertThat(jsonValue.getTargetBranch()).isEqualTo(pullRequest.getTarget());
  }

  private PullRequest createPullRequest() {
    PullRequest pullRequest = new PullRequest();
    pullRequest.setId("pr-1");
    pullRequest.setAuthor("trillian");
    pullRequest.setSource("develop");
    pullRequest.setTarget("master");
    return pullRequest;
  }

  @Test
  void shouldNotSendNotificationIfNoValidConfigExist() {
    when(configurationProvider.evaluateConfiguration(REPOSITORY)).thenReturn(Optional.empty());

    PushNotification notification = new PushNotification(REPOSITORY_URL, NAMESPACE_AND_NAME);

    notifier.notifyViaHttp(REPOSITORY, notification, EVENT_TYPE);

    verify(httpClient, never()).post(anyString());
  }

  private Optional<Configuration> createConfiguration(String url) {
    return Optional.of(new Configuration(url));
  }
}
