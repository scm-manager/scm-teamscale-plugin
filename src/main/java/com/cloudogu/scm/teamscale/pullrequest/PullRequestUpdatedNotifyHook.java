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

import com.cloudogu.scm.review.pullrequest.service.BasicPullRequestEvent;
import com.cloudogu.scm.review.pullrequest.service.PullRequest;
import com.cloudogu.scm.review.pullrequest.service.PullRequestEmergencyMergedEvent;
import com.cloudogu.scm.review.pullrequest.service.PullRequestMergedEvent;
import com.cloudogu.scm.review.pullrequest.service.PullRequestRejectedEvent;
import com.cloudogu.scm.review.pullrequest.service.PullRequestReopenedEvent;
import com.cloudogu.scm.review.pullrequest.service.PullRequestUpdatedEvent;
import com.cloudogu.scm.teamscale.Notifier;
import com.github.legman.Subscribe;
import sonia.scm.EagerSingleton;
import sonia.scm.plugin.Extension;
import sonia.scm.plugin.Requires;
import sonia.scm.repository.Repository;

import jakarta.inject.Inject;

@Requires("scm-review-plugin")
@Extension
@EagerSingleton
public class PullRequestUpdatedNotifyHook {

  private static final String EVENT_TYPE = "SCM-Pull-Request-Updated-Event";
  private final Notifier notifier;

  @Inject
  public PullRequestUpdatedNotifyHook(Notifier notifier) {
    this.notifier = notifier;
  }

  @Subscribe
  public void handleEvent(PullRequestUpdatedEvent event) {
    handleBaseEvent(event);
  }

  @Subscribe
  public void handleEvent(PullRequestMergedEvent event) {
    handleBaseEvent(event);
  }

  @Subscribe
  public void handleEvent(PullRequestEmergencyMergedEvent event) {
    handleBaseEvent(event);
  }

  @Subscribe
  public void handleEvent(PullRequestRejectedEvent event) {
    handleBaseEvent(event);
  }

  @Subscribe
  public void handleEvent(PullRequestReopenedEvent event) {
    handleBaseEvent(event);
  }

  private void handleBaseEvent(BasicPullRequestEvent event) {
    if (notifier.isTeamscaleConfigured(event.getRepository())) {
      notifier.notifyViaHttp(event.getRepository(), createPullRequestUpdatedNotification(event.getRepository(), event.getPullRequest()), EVENT_TYPE);
    }
  }

  private PullRequestUpdatedNotification createPullRequestUpdatedNotification(Repository repository, PullRequest pullRequest) {
    String repositoryUrl = notifier.createRepositoryUrl(repository);
    String repositoryId = notifier.createRepositoryId(repository);
    return new PullRequestUpdatedNotification(repositoryUrl, repositoryId, pullRequest.getId());
  }
}
