package com.cloudogu.scm.teamscale;

import com.github.legman.Subscribe;
import sonia.scm.EagerSingleton;
import sonia.scm.plugin.Extension;
import sonia.scm.repository.PostReceiveRepositoryHookEvent;
import sonia.scm.repository.api.HookContext;
import sonia.scm.repository.api.HookFeature;

import javax.inject.Inject;
import java.util.List;

@EagerSingleton
@Extension
public class RepositoryPushNotifyHook {

  private final Notifier notifier;

  @Inject
  public RepositoryPushNotifyHook(Notifier notifier) {
    this.notifier = notifier;
  }

  @Subscribe
  public void notify(PostReceiveRepositoryHookEvent event) {
    if (event.getContext().isFeatureSupported(HookFeature.BRANCH_PROVIDER)) {
      for (String branchName : getBranchesFromContext(event.getContext())) {
        notifier.notifyWithBranch(event.getRepository(), branchName);
      }
    } else {
      notifier.notifyWithoutBranch(event.getRepository());
    }
  }

  private List<String> getBranchesFromContext(HookContext context) {
    return context.getBranchProvider().getCreatedOrModified();
  }
}
