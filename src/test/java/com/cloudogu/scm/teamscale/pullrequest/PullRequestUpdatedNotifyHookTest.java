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
package com.cloudogu.scm.teamscale.pullrequest;

import com.cloudogu.scm.review.pullrequest.service.PullRequest;
import com.cloudogu.scm.review.pullrequest.service.PullRequestEvent;
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
class PullRequestUpdatedNotifyHookTest {

  private final Repository REPOSITORY = RepositoryTestData.createHeartOfGold();
  private final String NAMESPACE_AND_NAME = REPOSITORY.getNamespace() + "/" + REPOSITORY.getName();
  private final String REPOSITORY_URL = "/repo/" + NAMESPACE_AND_NAME;

  @Mock
  private Notifier notifier;

  @InjectMocks
  private PullRequestUpdatedNotifyHook hook;

  @Captor
  private ArgumentCaptor<PullRequestUpdatedNotification> captor;

  @Test
  void shouldNotSendNotificationIfNotModifyEvent() {
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
    pullRequest.setId("pr-1");
    PullRequestEvent event = new PullRequestEvent(REPOSITORY, pullRequest, null, HandlerEventType.MODIFY);

    hook.handleEvent(event);

    verify(notifier, never()).notifyViaHttp(any(Repository.class), captor.capture(), anyString());
  }

  @Test
  void shouldSendNotificationIfPullRequestModifiedEvent() {
    when(notifier.isTeamscaleConfigured(REPOSITORY)).thenReturn(true);
    when(notifier.createRepositoryId(REPOSITORY)).thenReturn(NAMESPACE_AND_NAME);
    when(notifier.createRepositoryUrl(REPOSITORY)).thenReturn(REPOSITORY_URL);

    PullRequest pullRequest = new PullRequest();
    pullRequest.setId("pr-1");
    PullRequestEvent event = new PullRequestEvent(REPOSITORY, pullRequest, null, HandlerEventType.MODIFY);

    hook.handleEvent(event);

    verify(notifier).notifyViaHttp(any(Repository.class), captor.capture(), anyString());

    PullRequestUpdatedNotification notification = captor.getValue();
    assertThat(notification.getRepositoryUrl()).isEqualTo(REPOSITORY_URL);
    assertThat(notification.getRepositoryId()).isEqualTo(NAMESPACE_AND_NAME);
    assertThat(notification.getPullRequestId()).isEqualTo(pullRequest.getId());
  }
}
