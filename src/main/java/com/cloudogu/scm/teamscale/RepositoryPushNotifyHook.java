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
package com.cloudogu.scm.teamscale;

import com.github.legman.Subscribe;
import sonia.scm.EagerSingleton;
import sonia.scm.plugin.Extension;
import sonia.scm.repository.PostReceiveRepositoryHookEvent;
import sonia.scm.repository.Repository;
import sonia.scm.repository.api.HookContext;
import sonia.scm.repository.api.HookFeature;

import javax.inject.Inject;
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
