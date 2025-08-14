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

import com.cloudogu.scm.review.pullrequest.service.PullRequest;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.util.ThreadContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.api.v2.resources.HalAppender;
import sonia.scm.api.v2.resources.HalEnricherContext;
import sonia.scm.api.v2.resources.ScmPathInfoStore;
import sonia.scm.repository.Repository;

import jakarta.inject.Provider;
import java.net.URI;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PullRequestLinkEnricherTest {

  private static final Repository REPOSITORY = new Repository("1", "git", "space", "x");

  @Mock
  private Provider<ScmPathInfoStore> scmPathInfoStoreProvider;

  @Mock
  private ScmPathInfoStore scmPathInfoStore;

  @Mock(answer = Answers.RETURNS_DEEP_STUBS)
  private HalEnricherContext context;
  @Mock
  private HalAppender appender;

  @Mock
  private Subject subject;

  @InjectMocks
  private PullRequestLinkEnricher pullRequestLinkEnricher;

  @BeforeEach
  void bindSubject() {
    ThreadContext.bind(subject);
  }

  @AfterEach
  void tearDownSubject() {
    ThreadContext.unbindSubject();
  }

  @Test
  void shouldEnrichCILinkToPullRequest() {
    PullRequest pr = new PullRequest();
    pr.setId("1");

    when(subject.isPermitted("repository:readTeamscaleFindings:1")).thenReturn(true);
    when(scmPathInfoStoreProvider.get()).thenReturn(scmPathInfoStore);
    when(scmPathInfoStore.get()).thenReturn(() -> URI.create("http://scm.com/"));
    when(context.oneRequireByType(Repository.class)).thenReturn(REPOSITORY);
    when(context.oneRequireByType(PullRequest.class)).thenReturn(pr);

    pullRequestLinkEnricher.enrich(context, appender);

    String expectedHref = "http://scm.com/v2/teamscale/pull-request/space/x/1/findings";
    verify(appender).appendLink("teamscaleFindings", expectedHref);
  }

  @Test
  void shouldNotEnrichCiLinkIfNotPermitted() {
    PullRequest pr = new PullRequest();
    pr.setId("1");

    when(context.oneRequireByType(Repository.class)).thenReturn(REPOSITORY);
    when(context.oneRequireByType(PullRequest.class)).thenReturn(pr);

    pullRequestLinkEnricher.enrich(context, appender);

    verify(appender, never()).appendLink(any(), any());
  }

}
