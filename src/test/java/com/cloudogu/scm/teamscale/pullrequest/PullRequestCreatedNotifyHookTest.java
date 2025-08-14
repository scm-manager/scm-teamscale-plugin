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

package com.cloudogu.scm.teamscale.pullrequest;

import com.cloudogu.scm.review.pullrequest.service.PullRequest;
import com.cloudogu.scm.review.pullrequest.service.PullRequestEvent;
import com.cloudogu.scm.teamscale.Notification;
import com.cloudogu.scm.teamscale.Notifier;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.HandlerEventType;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryTestData;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PullRequestCreatedNotifyHookTest {

  private final Repository REPOSITORY = RepositoryTestData.createHeartOfGold();
  private final String NAMESPACE_AND_NAME = REPOSITORY.getNamespace() + "/" + REPOSITORY.getName();
  private final String REPOSITORY_URL = "/repo/" + NAMESPACE_AND_NAME;

  @Mock
  private Notifier notifier;

  @InjectMocks
  private PullRequestCreatedNotifyHook hook;

  @Captor
  private ArgumentCaptor<PullRequestCreatedNotification> captor;

  @Test
  void shouldNotSendNotificationIfNotCreatedEvent() {
    PullRequest pullRequest = new PullRequest();
    pullRequest.setId("pr-1");
    PullRequestEvent event = new PullRequestEvent(REPOSITORY, pullRequest, null, HandlerEventType.DELETE);

    hook.handleEvent(event);

    verify(notifier, never()).notifyViaHttp(any(Repository.class), captor.capture(), anyString());
  }

  @Test
  void shouldNotNotifyIfTeamscaleNotConfigured() {
    when(notifier.isTeamscaleConfigured(REPOSITORY)).thenReturn(false);

    PullRequest pullRequest = new PullRequest();
    PullRequestEvent event = new PullRequestEvent(REPOSITORY, pullRequest, null, HandlerEventType.CREATE);

    hook.handleEvent(event);

    verify(notifier, never()).notifyViaHttp(any(Repository.class), captor.capture(), anyString());
  }

  @Test
  void shouldSendNotificationIfPullRequestCreatedEvent() {
    when(notifier.isTeamscaleConfigured(REPOSITORY)).thenReturn(true);
    when(notifier.createRepositoryId(REPOSITORY)).thenReturn(NAMESPACE_AND_NAME);
    when(notifier.createRepositoryUrl(REPOSITORY)).thenReturn(REPOSITORY_URL);

    PullRequest pullRequest = new PullRequest();
    pullRequest.setId("pr-1");
    pullRequest.setSource("develop");
    pullRequest.setTarget("master");
    PullRequestEvent event = new PullRequestEvent(REPOSITORY, pullRequest, null, HandlerEventType.CREATE);

    hook.handleEvent(event);

    verify(notifier).notifyViaHttp(any(Repository.class), captor.capture(), anyString());
    PullRequestCreatedNotification notification = captor.getValue();

    assertThat(notification.getRepositoryId()).isEqualTo(NAMESPACE_AND_NAME);
    assertThat(notification.getRepositoryUrl()).isEqualTo(REPOSITORY_URL);
    assertThat(notification.getPullRequestId()).isEqualTo(pullRequest.getId());
    assertThat(notification.getSourceBranch()).isEqualTo(pullRequest.getSource());
    assertThat(notification.getTargetBranch()).isEqualTo(pullRequest.getTarget());
  }

}
