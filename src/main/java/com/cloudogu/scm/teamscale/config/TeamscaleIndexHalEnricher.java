package com.cloudogu.scm.teamscale.config;

import com.cloudogu.scm.teamscale.Constants;
import sonia.scm.api.v2.resources.Enrich;
import sonia.scm.api.v2.resources.HalAppender;
import sonia.scm.api.v2.resources.HalEnricher;
import sonia.scm.api.v2.resources.HalEnricherContext;
import sonia.scm.api.v2.resources.Index;
import sonia.scm.api.v2.resources.LinkBuilder;
import sonia.scm.api.v2.resources.ScmPathInfoStore;
import sonia.scm.config.ConfigurationPermissions;
import sonia.scm.plugin.Extension;

import javax.inject.Inject;
import javax.inject.Provider;

@Extension
@Enrich(Index.class)
public class TeamscaleIndexHalEnricher implements HalEnricher {

  private Provider<ScmPathInfoStore> scmPathInfoStoreProvider;

  @Inject
  public TeamscaleIndexHalEnricher(Provider<ScmPathInfoStore> scmPathInfoStoreProvider) {
    this.scmPathInfoStoreProvider = scmPathInfoStoreProvider;
  }

  private String createLink() {
    return new LinkBuilder(scmPathInfoStoreProvider.get().get(), ConfigurationResource.class)
      .method("getGlobalConfiguration")
      .parameters()
      .href();
  }

  @Override
  public void enrich(HalEnricherContext context, HalAppender appender) {
    if (ConfigurationPermissions.read(Constants.NAME).isPermitted()) {
      appender.appendLink("teamscaleConfig", createLink());
    }
  }
}
