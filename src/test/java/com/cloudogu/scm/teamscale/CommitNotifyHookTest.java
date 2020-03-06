package com.cloudogu.scm.teamscale;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.repository.PostReceiveRepositoryHookEvent;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class CommitNotifyHookTest {

  @Mock
  private Notifier notifier;

  private CommitNotifyHook hook;

  @Mock
  private PostReceiveRepositoryHookEvent event;

  @BeforeEach
  void initHook() {
    hook = new CommitNotifyHook(notifier);
  }

  @Test
  void shouldTriggerNotificationOnHook() {
    hook.notify(event);
    verify(notifier).sendCommitNotification(event);
  }
}
