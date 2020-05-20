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

import javax.inject.Provider;
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
