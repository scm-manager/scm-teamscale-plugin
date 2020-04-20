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
import com.github.legman.Subscribe;
import sonia.scm.EagerSingleton;
import sonia.scm.HandlerEventType;
import sonia.scm.plugin.Extension;
import sonia.scm.plugin.Requires;
import sonia.scm.repository.Repository;

import javax.inject.Inject;

@Requires("scm-review-plugin")
@Extension
@EagerSingleton
public class PullRequestCreatedNotifyHook {

  private static final String PULL_REQUEST_CREATED_EVENT = "SCM-Pull-Request-Created-Event";
  private final Notifier notifier;

  @Inject
  public PullRequestCreatedNotifyHook(Notifier notifier) {
    this.notifier = notifier;
  }

  @Subscribe
  public void handleEvent(PullRequestEvent event) {
    if (event.getEventType() == HandlerEventType.CREATE) {
      notifier.notifyViaHttp(event.getRepository(), createPullRequestCreatedNotification(event.getRepository(), event.getItem()), PULL_REQUEST_CREATED_EVENT);
    }
  }

  private PullRequestCreatedNotification createPullRequestCreatedNotification(Repository repository, PullRequest pullRequest) {
    String repositoryUrl = notifier.createRepositoryUrl(repository);
    String repositoryId = notifier.createRepositoryId(repository);
    return new PullRequestCreatedNotification(repositoryUrl, repositoryId, pullRequest.getId(), pullRequest.getSource(), pullRequest.getTarget());
  }
}
