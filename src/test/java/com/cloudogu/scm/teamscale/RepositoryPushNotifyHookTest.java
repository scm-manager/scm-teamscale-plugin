/**
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
package com.cloudogu.scm.teamscale;

import com.cloudogu.scm.teamscale.config.ConfigurationProvider;
import com.google.common.collect.ImmutableList;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.repository.PostReceiveRepositoryHookEvent;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryTestData;
import sonia.scm.repository.api.HookBranchProvider;
import sonia.scm.repository.api.HookContext;
import sonia.scm.repository.api.HookFeature;

import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RepositoryPushNotifyHookTest {

  private Repository REPOSITORY = RepositoryTestData.createHeartOfGold();

  private RepositoryPushNotifyHook hook;

  @Mock
  private Notifier notifier;
  @Mock
  private PostReceiveRepositoryHookEvent event;
  @Mock
  private HookContext hookContext;
  @Mock
  private HookBranchProvider branchProvider;
  @Mock
  private ConfigurationProvider configurationProvider;

  @BeforeEach
  void initHook() {
    hook = new RepositoryPushNotifyHook(notifier, configurationProvider);
  }

  @BeforeEach
  void initEvent() {
    when(event.getContext()).thenReturn(hookContext);
    lenient().when(hookContext.getBranchProvider()).thenReturn(branchProvider);
    lenient().when(event.getRepository()).thenReturn(REPOSITORY);
  }

  @Test
  void shouldNotifyWithoutBranchIfBranchProviderNotSupported() {
    when(hookContext.isFeatureSupported(HookFeature.BRANCH_PROVIDER)).thenReturn(false);

    hook.notify(event);

    verify(notifier).notifyWithoutBranch(REPOSITORY);
  }

  @Test
  void shouldNotTriggerNotificationOnHookWithBranchIfNoBranchesProvided() {
    when(hookContext.isFeatureSupported(HookFeature.BRANCH_PROVIDER)).thenReturn(true);
    when(branchProvider.getCreatedOrModified()).thenReturn(ImmutableList.of());

    hook.notify(event);

    verify(notifier, never()).notifyWithBranch(REPOSITORY, null);
  }

  @Test
  void shouldTriggerNotificationOnHookWithBranch() {
    when(hookContext.isFeatureSupported(HookFeature.BRANCH_PROVIDER)).thenReturn(true);
    when(branchProvider.getCreatedOrModified()).thenReturn(ImmutableList.of("feature/teamscaleConfig"));

    hook.notify(event);

    verify(notifier).notifyWithBranch(REPOSITORY, "feature/teamscaleConfig");
  }
}
