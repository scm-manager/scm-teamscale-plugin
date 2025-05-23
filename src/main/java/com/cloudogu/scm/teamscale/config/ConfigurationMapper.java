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

package com.cloudogu.scm.teamscale.config;

import com.cloudogu.scm.teamscale.Constants;
import de.otto.edison.hal.Link;
import de.otto.edison.hal.Links;
import org.mapstruct.AfterMapping;
import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import sonia.scm.api.v2.resources.BaseMapper;
import sonia.scm.api.v2.resources.LinkBuilder;
import sonia.scm.api.v2.resources.ScmPathInfoStore;
import sonia.scm.config.ConfigurationPermissions;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryPermissions;

import jakarta.inject.Inject;

import static de.otto.edison.hal.Links.linkingTo;

@Mapper
public abstract class ConfigurationMapper extends BaseMapper {

  @Inject
  private ScmPathInfoStore scmPathInfoStore;

  public abstract ConfigurationDto map(Configuration configuration, @Context Repository repository);

  public abstract Configuration map(ConfigurationDto configurationDto);

  public abstract GlobalConfigurationDto map(GlobalConfiguration configuration);

  public abstract GlobalConfiguration map(GlobalConfigurationDto configurationDto);

  @AfterMapping
  public void addLinks(@MappingTarget GlobalConfigurationDto target) {
    Links.Builder linksBuilder = linkingTo().self(globalSelf());
    if (ConfigurationPermissions.write(Constants.NAME).isPermitted()) {
      linksBuilder.single(Link.link("update", globalUpdate()));
    }
    target.add(linksBuilder.build());
  }

  private String globalSelf() {
    LinkBuilder linkBuilder = new LinkBuilder(scmPathInfoStore.get(), ConfigurationResource.class);
    return linkBuilder.method("getGlobalConfiguration").parameters().href();
  }

  private String globalUpdate() {
    LinkBuilder linkBuilder = new LinkBuilder(scmPathInfoStore.get(), ConfigurationResource.class);
    return linkBuilder.method("updateGlobalConfiguration").parameters().href();
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
