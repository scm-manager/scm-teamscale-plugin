package com.cloudogu.scm.teamscale.config;

import com.cloudogu.scm.teamscale.Constants;
import sonia.scm.api.v2.resources.Enrich;
import sonia.scm.api.v2.resources.HalAppender;
import sonia.scm.api.v2.resources.HalEnricher;
import sonia.scm.api.v2.resources.HalEnricherContext;
import sonia.scm.api.v2.resources.LinkBuilder;
import sonia.scm.api.v2.resources.ScmPathInfoStore;
import sonia.scm.plugin.Extension;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryPermissions;

import javax.inject.Inject;
import javax.inject.Provider;

@Extension
@Enrich(Repository.class)
public class RepositoryConfigHalEnricher implements HalEnricher {

  private final Provider<ScmPathInfoStore> scmPathInfoStoreProvider;
  private final ConfigStore configStore;

  @Inject
  public RepositoryConfigHalEnricher(Provider<ScmPathInfoStore> scmPathInfoStoreProvider, ConfigStore configStore) {
    this.scmPathInfoStoreProvider = scmPathInfoStoreProvider;
    this.configStore = configStore;
  }

  @Override
  public void enrich(HalEnricherContext context, HalAppender appender) {
    Repository repository = context.oneRequireByType(Repository.class);
    if (!configStore.getGlobalConfiguration().isDisableRepositoryConfiguration() && RepositoryPermissions.custom(Constants.NAME, repository).isPermitted()) {
      String linkBuilder = new LinkBuilder(scmPathInfoStoreProvider.get().get(), ConfigurationResource.class)
        .method("getConfiguration")
        .parameters(repository.getNamespace(), repository.getName())
        .href();

      appender.appendLink("teamscaleConfig", linkBuilder);
    }
  }
}

