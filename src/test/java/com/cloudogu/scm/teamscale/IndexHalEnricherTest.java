package com.cloudogu.scm.teamscale;

import com.cloudogu.scm.teamscale.config.IndexHalEnricher;
import com.google.inject.util.Providers;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.util.ThreadContext;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.api.v2.resources.HalAppender;
import sonia.scm.api.v2.resources.HalEnricherContext;
import sonia.scm.api.v2.resources.ScmPathInfoStore;

import javax.inject.Provider;
import java.net.URI;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class IndexHalEnricherTest {

  @Mock
  private HalAppender appender;

  @Mock
  private Subject subject;

  private IndexHalEnricher enricher;


  @BeforeEach
  void bindSubject() {
    ThreadContext.bind(subject);
  }

  @AfterEach
  void unbindSubject() {
    ThreadContext.unbindSubject();
  }

  @BeforeEach
  void setUp() {
    ScmPathInfoStore scmPathInfoStore = new ScmPathInfoStore();
    scmPathInfoStore.set(() -> URI.create("https://scm-manager.org/scm/api/"));
    Provider<ScmPathInfoStore> scmPathInfoStoreProvider = Providers.of(scmPathInfoStore);
    enricher = new IndexHalEnricher(scmPathInfoStoreProvider);
  }

  @Test
  public void testEnrich() {
    when(subject.isPermitted("configuration:read:teamscale")).thenReturn(true);
    enricher.enrich(HalEnricherContext.of(), appender);
    verify(appender).appendLink("teamscaleConfig", "https://scm-manager.org/scm/api/v2/teamscale/configuration/");
  }
}
