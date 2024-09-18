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

import com.google.common.collect.ImmutableList;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.repository.PostReceiveRepositoryHookEvent;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryTestData;
import sonia.scm.repository.api.HookBranchProvider;
import sonia.scm.repository.api.HookContext;
import sonia.scm.repository.api.HookFeature;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RepositoryPushNotifyHookTest {

  private final Repository REPOSITORY = RepositoryTestData.createHeartOfGold();
  private final String SCM_BASE_URL = "http://www.scm-manager.org/scm";
  private final String NAMESPACE_AND_NAME = REPOSITORY.getNamespace() + "/" + REPOSITORY.getName();
  private final String REPOSITORY_URL = SCM_BASE_URL + "/repo/" + NAMESPACE_AND_NAME;

  @Mock
  private Notifier notifier;
  @Mock
  private PostReceiveRepositoryHookEvent event;
  @Mock
  private HookContext hookContext;
  @Mock
  private HookBranchProvider branchProvider;
  @Captor
  private ArgumentCaptor<PushNotification> pushNotificationCaptor;

  @InjectMocks
  private RepositoryPushNotifyHook hook;

  @BeforeEach
  void initEvent() {
    lenient().when(event.getContext()).thenReturn(hookContext);
    lenient().when(hookContext.getBranchProvider()).thenReturn(branchProvider);
    lenient().when(event.getRepository()).thenReturn(REPOSITORY);
    lenient().when(notifier.createRepositoryId(REPOSITORY)).thenReturn(NAMESPACE_AND_NAME);
    lenient().when(notifier.createRepositoryUrl(REPOSITORY)).thenReturn(REPOSITORY_URL);
  }

  @Test
  void shouldNotNotifyIfTeamscaleNotConfigured() {
    when(notifier.isTeamscaleConfigured(REPOSITORY)).thenReturn(false);

    hook.notify(event);

    verify(notifier, never()).notifyViaHttp(any(Repository.class), pushNotificationCaptor.capture(), anyString());
  }

  @Test
  void shouldNotifyWithoutBranchIfBranchProviderNotSupported() {
    when(notifier.isTeamscaleConfigured(REPOSITORY)).thenReturn(true);
    when(hookContext.isFeatureSupported(HookFeature.BRANCH_PROVIDER)).thenReturn(false);

    hook.notify(event);

    verify(notifier).notifyViaHttp(any(Repository.class), pushNotificationCaptor.capture(), anyString());

    PushNotification notification = pushNotificationCaptor.getValue();
    assertThat(notification.getRepositoryId()).isEqualTo(NAMESPACE_AND_NAME);
    assertThat(notification.getRepositoryUrl()).isEqualTo(REPOSITORY_URL);
  }

  @Test
  void shouldNotTriggerNotificationOnHookWithBranchIfNoBranchesProvided() {
    when(notifier.isTeamscaleConfigured(REPOSITORY)).thenReturn(true);
    when(hookContext.isFeatureSupported(HookFeature.BRANCH_PROVIDER)).thenReturn(true);
    when(branchProvider.getCreatedOrModified()).thenReturn(ImmutableList.of());

    hook.notify(event);

    verify(notifier, never()).notifyViaHttp(any(Repository.class), any(PushNotification.class), anyString());
  }

  @Test
  void shouldTriggerNotificationOnHookWithBranch() {
    String branchName = "feature/teamscaleConfig";
    when(notifier.isTeamscaleConfigured(REPOSITORY)).thenReturn(true);
    when(hookContext.isFeatureSupported(HookFeature.BRANCH_PROVIDER)).thenReturn(true);
    when(branchProvider.getCreatedOrModified()).thenReturn(ImmutableList.of(branchName));

    hook.notify(event);

    verify(notifier).notifyViaHttp(any(Repository.class), pushNotificationCaptor.capture(), anyString());

    PushNotification notification = pushNotificationCaptor.getValue();
    assertThat(notification.getRepositoryId()).isEqualTo(NAMESPACE_AND_NAME);
    assertThat(notification.getRepositoryUrl()).isEqualTo(REPOSITORY_URL);
    assertThat(notification.getBranchName()).isEqualTo(branchName);
  }
}
