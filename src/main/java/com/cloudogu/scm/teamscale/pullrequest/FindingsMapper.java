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
package com.cloudogu.scm.teamscale.pullrequest;

import com.cloudogu.scm.review.pullrequest.service.PullRequest;
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

import static com.cloudogu.scm.teamscale.Constants.WRITE_FINDINGS_PERMISSION;
import static de.otto.edison.hal.Links.linkingTo;

@Mapper
public abstract class FindingsMapper extends BaseMapper {

  @Inject
  private ScmPathInfoStore scmPathInfoStore;

  public abstract FindingsDto map(Findings findings, Repository repository, String pullRequestId);

  @AfterMapping
  public void addLinks(@MappingTarget FindingsDto target, @Context Repository repository, @Context PullRequest pullRequest) {
    Links.Builder linksBuilder = linkingTo().self(self(repository, pullRequest));
    if (RepositoryPermissions.custom(WRITE_FINDINGS_PERMISSION, repository).isPermitted()) {
      linksBuilder.single(Link.link("update", update(repository, pullRequest)));
    }
    target.add(linksBuilder.build());
  }

  private String self(Repository repository, PullRequest pullRequest) {
    LinkBuilder linkBuilder = new LinkBuilder(scmPathInfoStore.get(), PullRequestResource.class);
    return linkBuilder.method("getFindings").parameters(repository.getNamespace(), repository.getName(), pullRequest.getId()).href();
  }

  private String update(Repository repository, PullRequest pullRequest) {
    LinkBuilder linkBuilder = new LinkBuilder(scmPathInfoStore.get(), PullRequestResource.class);
    return linkBuilder.method("updateFindings").parameters(repository.getNamespace(), repository.getName(), pullRequest.getId()).href();
  }
}
