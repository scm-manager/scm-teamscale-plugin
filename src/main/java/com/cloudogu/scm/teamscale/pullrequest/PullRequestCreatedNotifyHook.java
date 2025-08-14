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
import com.cloudogu.scm.teamscale.Notifier;
import com.github.legman.Subscribe;
import sonia.scm.EagerSingleton;
import sonia.scm.HandlerEventType;
import sonia.scm.plugin.Extension;
import sonia.scm.plugin.Requires;
import sonia.scm.repository.Repository;

import jakarta.inject.Inject;

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
    if (event.getEventType() == HandlerEventType.CREATE && notifier.isTeamscaleConfigured(event.getRepository())) {
      notifier.notifyViaHttp(event.getRepository(), createPullRequestCreatedNotification(event.getRepository(), event.getItem()), PULL_REQUEST_CREATED_EVENT);
    }
  }

  private PullRequestCreatedNotification createPullRequestCreatedNotification(Repository repository, PullRequest pullRequest) {
    String repositoryUrl = notifier.createRepositoryUrl(repository);
    String repositoryId = notifier.createRepositoryId(repository);
    return new PullRequestCreatedNotification(repositoryUrl, repositoryId, pullRequest.getId(), pullRequest.getSource(), pullRequest.getTarget());
  }
}
