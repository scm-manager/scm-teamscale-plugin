package com.cloudogu.scm.teamscale.config;

import com.cloudogu.scm.teamscale.Constants;
import com.google.common.annotations.VisibleForTesting;
import de.otto.edison.hal.Link;
import de.otto.edison.hal.Links;
import org.mapstruct.AfterMapping;
import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import sonia.scm.api.v2.resources.BaseMapper;
import sonia.scm.api.v2.resources.LinkBuilder;
import sonia.scm.api.v2.resources.ScmPathInfoStore;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryPermissions;

import javax.inject.Inject;

import static de.otto.edison.hal.Links.linkingTo;

@Mapper
public abstract class ConfigurationMapper extends BaseMapper {

  @VisibleForTesting
  @SuppressWarnings("squid:S2068")
  public static final String DUMMY_PASSWORD = "__DUMMY__";

  @Inject
  private ScmPathInfoStore scmPathInfoStore;

  public abstract ConfigurationDto map(Configuration configuration, @Context Repository repository);

  public abstract Configuration map(ConfigurationDto configurationDto, @Context Configuration oldConfiguration);

  @AfterMapping
  public void replacePasswordWithDummy(@MappingTarget ConfigurationDto target) {
    target.setPassword(DUMMY_PASSWORD);
  }

  @AfterMapping
  public void restorePasswordOnDummy(@MappingTarget Configuration target, @Context Configuration oldConfiguration) {
    if (DUMMY_PASSWORD.equals(target.getPassword())) {
      target.setPassword(oldConfiguration.getPassword());
    }
  }

  @AfterMapping
  public void addLinks(@MappingTarget ConfigurationDto target, @Context Repository repository) {
    Links.Builder linksBuilder = linkingTo().self(self(repository));
    if (RepositoryPermissions.custom(Constants.NAME, repository).isPermitted()) {
      linksBuilder.single(Link.link("update", update(repository)));
    }
    target.add(linksBuilder.build());
  }

  private String self(Repository repository) {
    LinkBuilder linkBuilder = new LinkBuilder(scmPathInfoStore.get(), ConfigurationResource.class);
    return linkBuilder.method("getConfiguration").parameters(repository.getNamespace(), repository.getName()).href();
  }

  private String update(Repository repository) {
    LinkBuilder linkBuilder = new LinkBuilder(scmPathInfoStore.get(), ConfigurationResource.class);
    return linkBuilder.method("updateConfiguration").parameters(repository.getNamespace(), repository.getName()).href();
  }
}
