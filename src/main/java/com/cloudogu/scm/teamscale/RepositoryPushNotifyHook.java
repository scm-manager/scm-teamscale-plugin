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

import com.github.legman.Subscribe;
import sonia.scm.EagerSingleton;
import sonia.scm.plugin.Extension;
import sonia.scm.repository.PostReceiveRepositoryHookEvent;
import sonia.scm.repository.Repository;
import sonia.scm.repository.api.HookContext;
import sonia.scm.repository.api.HookFeature;

import jakarta.inject.Inject;
import java.util.List;

@EagerSingleton
@Extension
public class RepositoryPushNotifyHook {

  private static final String PUSH_EVENT = "SCM-Push-Event";
  private final Notifier notifier;

  @Inject
  public RepositoryPushNotifyHook(Notifier notifier) {
    this.notifier = notifier;
  }

  @Subscribe
  public void notify(PostReceiveRepositoryHookEvent event) {
    if (notifier.isTeamscaleConfigured(event.getRepository())) {
      if (event.getContext().isFeatureSupported(HookFeature.BRANCH_PROVIDER)) {
        for (String branchName : getBranchesFromContext(event.getContext())) {
          PushNotification notification = createPushNotificationWithBranch(event.getRepository(), branchName);
          notifier.notifyViaHttp(event.getRepository(), notification, PUSH_EVENT);
        }
      } else {
        PushNotification notification = createPushNotification(event.getRepository());
        notifier.notifyViaHttp(event.getRepository(), notification, PUSH_EVENT);
      }
    }
  }

  private List<String> getBranchesFromContext(HookContext context) {
    return context.getBranchProvider().getCreatedOrModified();
  }

  private PushNotification createPushNotification(Repository repository) {
    String repositoryUrl = notifier.createRepositoryUrl(repository);
    String repositoryId = notifier.createRepositoryId(repository);
    return new PushNotification(repositoryUrl, repositoryId);
  }

  private PushNotification createPushNotificationWithBranch(Repository repository, String branchName) {
    PushNotification notification = createPushNotification(repository);
    return new PushNotification(notification.getRepositoryUrl(), notification.getRepositoryId(), branchName);
  }
}
