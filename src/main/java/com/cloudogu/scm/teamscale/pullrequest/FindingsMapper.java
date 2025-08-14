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

package com.cloudogu.scm.teamscale.pullrequest;

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

import jakarta.inject.Inject;

import static com.cloudogu.scm.teamscale.Constants.WRITE_FINDINGS_PERMISSION;
import static de.otto.edison.hal.Links.linkingTo;

@Mapper
public abstract class FindingsMapper extends BaseMapper {

  @Inject
  private ScmPathInfoStore scmPathInfoStore;

  public abstract FindingsDto map(Findings findings, @Context Repository repository, @Context String pullRequestId);

  @AfterMapping
  public void addLinks(@MappingTarget FindingsDto target, @Context Repository repository, @Context String pullRequestId) {
    Links.Builder linksBuilder = linkingTo().self(self(repository, pullRequestId));
    if (RepositoryPermissions.custom(WRITE_FINDINGS_PERMISSION, repository).isPermitted()) {
      linksBuilder.single(Link.link("update", update(repository, pullRequestId)));
    }
    target.add(linksBuilder.build());
  }

  private String self(Repository repository, String pullRequestId) {
    LinkBuilder linkBuilder = new LinkBuilder(scmPathInfoStore.get(), PullRequestResource.class);
    return linkBuilder.method("getFindings").parameters(repository.getNamespace(), repository.getName(), pullRequestId).href();
  }

  private String update(Repository repository, String pullRequestId) {
    LinkBuilder linkBuilder = new LinkBuilder(scmPathInfoStore.get(), PullRequestResource.class);
    return linkBuilder.method("updateFindings").parameters(repository.getNamespace(), repository.getName(), pullRequestId).href();
  }
}
