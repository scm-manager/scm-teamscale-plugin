package com.cloudogu.scm.teamscale.pullrequest;

import com.cloudogu.scm.review.pullrequest.service.PullRequest;
import sonia.scm.api.v2.resources.Enrich;
import sonia.scm.api.v2.resources.HalAppender;
import sonia.scm.api.v2.resources.HalEnricher;
import sonia.scm.api.v2.resources.HalEnricherContext;
import sonia.scm.api.v2.resources.LinkBuilder;
import sonia.scm.api.v2.resources.ScmPathInfoStore;
import sonia.scm.plugin.Extension;
import sonia.scm.plugin.Requires;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryPermissions;

import javax.inject.Inject;
import javax.inject.Provider;

import static com.cloudogu.scm.teamscale.Constants.READ_FINDINGS_PERMISSION;

@Requires("scm-review-plugin")
@Enrich(PullRequest.class)
@Extension
public class PullRequestLinkEnricher implements HalEnricher {

  private final Provider<ScmPathInfoStore> pathInfoStore;

  @Inject
  public PullRequestLinkEnricher(Provider<ScmPathInfoStore> pathInfoStore) {
    this.pathInfoStore = pathInfoStore;
  }

  @Override
  public void enrich(HalEnricherContext context, HalAppender appender) {

    Repository repository = context.oneRequireByType(Repository.class);
    PullRequest pullRequest = context.oneRequireByType(PullRequest.class);

    if (RepositoryPermissions.custom(READ_FINDINGS_PERMISSION, repository).isPermitted()) {
      appender.appendLink("teamscaleFindings", createFindingsLink(repository, pullRequest.getId()));
    }
  }

  private String createFindingsLink(Repository repository, String pullRequestId) {
    LinkBuilder linkBuilder = new LinkBuilder(pathInfoStore.get().get(), PullRequestResource.class);
    return linkBuilder
      .method("getFindings")
      .parameters(repository.getNamespace(), repository.getName(), pullRequestId)
      .href();
  }
}
