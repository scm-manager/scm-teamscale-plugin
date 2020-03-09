package com.cloudogu.scm.teamscale;

import com.github.legman.Subscribe;
import sonia.scm.EagerSingleton;
import sonia.scm.plugin.Extension;
import sonia.scm.repository.PostReceiveRepositoryHookEvent;

import javax.inject.Inject;

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
    notifier.sendCommitNotification(event);
  }
}
