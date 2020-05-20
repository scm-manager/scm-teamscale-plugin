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

import com.cloudogu.scm.review.comment.api.CommentResource;
import com.cloudogu.scm.review.comment.api.CommentRootResource;
import com.cloudogu.scm.review.pullrequest.api.PullRequestRootResource;
import com.cloudogu.scm.review.pullrequest.api.PullRequestSelector;
import org.apache.shiro.authz.AuthorizationException;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.util.ThreadContext;
import org.jboss.resteasy.mock.MockHttpRequest;
import org.jboss.resteasy.mock.MockHttpResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryManager;
import sonia.scm.repository.RepositoryTestData;
import sonia.scm.web.RestDispatcher;
import sonia.scm.web.VndMediaType;

import javax.ws.rs.core.UriInfo;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class PullRequestResourceTest {

  private final Repository REPOSITORY = RepositoryTestData.createHeartOfGold();

  private static final String PR_MEDIATYPE = VndMediaType.PREFIX + "teamscalePullRequest" + VndMediaType.SUFFIX;
  private static final String FINDINGS_MEDIATYPE = VndMediaType.PREFIX + "teamscaleFindings" + VndMediaType.SUFFIX;

  @Mock
  private PullRequestRootResource pullRequestRootResource;

  @Mock
  private com.cloudogu.scm.review.pullrequest.api.PullRequestResource reviewPullRequestResource;

  @Mock
  private FindingsService findingsService;

  @Mock
  private RepositoryManager repositoryManager;

  @Mock
  private FindingsMapper findingsMapper;

  @Mock
  private CommentRootResource commentRootResource;

  @Mock
  private CommentResource commentResource;

  @Mock
  private Subject subject;

  private RestDispatcher restDispatcher;

  @BeforeEach
  void initDispatcher() {
    PullRequestResource pullRequestResource = new PullRequestResource(pullRequestRootResource, repositoryManager, findingsService, findingsMapper);
    restDispatcher = new RestDispatcher();
    restDispatcher.addSingletonResource(pullRequestResource);
  }

  @Test
  void shouldGetAllPullRequests() throws URISyntaxException {
    MockHttpRequest request = MockHttpRequest
      .get("/v2/teamscale/pull-request/" + REPOSITORY.getNamespace() + "/" + REPOSITORY.getName())
      .contentType(PR_MEDIATYPE);

    MockHttpResponse response = new MockHttpResponse();

    restDispatcher.invoke(request, response);

    verify(pullRequestRootResource).getAll(any(UriInfo.class), anyString(), anyString(), any(PullRequestSelector.class));
  }

  @Test
  void shouldGetSinglePullRequest() throws URISyntaxException {
    when(pullRequestRootResource.getPullRequestResource()).thenReturn(reviewPullRequestResource);
    MockHttpRequest request = MockHttpRequest
      .get("/v2/teamscale/pull-request/" + REPOSITORY.getNamespace() + "/" + REPOSITORY.getName() + "/1")
      .contentType(PR_MEDIATYPE);

    MockHttpResponse response = new MockHttpResponse();

    restDispatcher.invoke(request, response);

    verify(pullRequestRootResource).getPullRequestResource();
    verify(reviewPullRequestResource).get(any(UriInfo.class), anyString(), anyString(), anyString());
  }

  @Test
  void shouldGetAllComments() throws URISyntaxException {
    when(pullRequestRootResource.getPullRequestResource()).thenReturn(reviewPullRequestResource);
    when(reviewPullRequestResource.comments()).thenReturn(commentRootResource);
    MockHttpRequest request = MockHttpRequest
      .get("/v2/teamscale/pull-request/comments/" + REPOSITORY.getNamespace() + "/" + REPOSITORY.getName() + "/1")
      .contentType(PR_MEDIATYPE);

    MockHttpResponse response = new MockHttpResponse();

    restDispatcher.invoke(request, response);

    verify(pullRequestRootResource).getPullRequestResource();
    verify(reviewPullRequestResource).comments();
    verify(commentRootResource).getAll(any(UriInfo.class), anyString(), anyString(), anyString());
  }

  @Test
  void shouldDeleteComment() throws URISyntaxException {
    when(pullRequestRootResource.getPullRequestResource()).thenReturn(reviewPullRequestResource);
    when(reviewPullRequestResource.comments()).thenReturn(commentRootResource);
    when(commentRootResource.getCommentResource()).thenReturn(commentResource);
    MockHttpRequest request = MockHttpRequest
      .delete("/v2/teamscale/pull-request/comments/" + REPOSITORY.getNamespace() + "/" + REPOSITORY.getName() + "/1/abc")
      .contentType(PR_MEDIATYPE);

    MockHttpResponse response = new MockHttpResponse();

    restDispatcher.invoke(request, response);

    verify(pullRequestRootResource).getPullRequestResource();
    verify(reviewPullRequestResource).comments();
    verify(commentRootResource).getCommentResource();

    assertThat(response.getStatus()).isEqualTo(204);
  }

  @Test
  void shouldCreateNewComment() throws URISyntaxException {
    when(pullRequestRootResource.getPullRequestResource()).thenReturn(reviewPullRequestResource);
    when(reviewPullRequestResource.comments()).thenReturn(commentRootResource);

    byte[] commentJson = "{\"comment\" : \"this is my comment\"}".getBytes();

    MockHttpRequest request = MockHttpRequest
      .post("/v2/teamscale/pull-request/comments/" + REPOSITORY.getNamespace() + "/" + REPOSITORY.getName() + "/1/abc?sourceRevision=source&targetRevision=target")
      .content(commentJson)
      .contentType(PR_MEDIATYPE);

    MockHttpResponse response = new MockHttpResponse();

    restDispatcher.invoke(request, response);

    verify(pullRequestRootResource).getPullRequestResource();
    verify(reviewPullRequestResource).comments();

    assertThat(response.getStatus()).isEqualTo(204);
  }

  @Nested
  class WithSubject {

    @BeforeEach
    void bindSubject() {
      ThreadContext.bind(subject);
    }

    @AfterEach
    void tearDownSubject() {
      ThreadContext.unbindSubject();
    }

    @Test
    void shouldThrowAuthorizationExceptionIfNotPermittedToRead() throws URISyntaxException {
      String permission = "repository:readTeamscaleFindings:" + REPOSITORY.getId();
      doThrow(AuthorizationException.class).when(subject).checkPermission(permission);

      when(repositoryManager.get(REPOSITORY.getNamespaceAndName())).thenReturn(REPOSITORY);

      MockHttpRequest request = MockHttpRequest
        .get("/v2/teamscale/pull-request/" + REPOSITORY.getNamespace() + "/" + REPOSITORY.getName() + "/1/findings")
        .contentType(FINDINGS_MEDIATYPE);

      MockHttpResponse response = new MockHttpResponse();

      restDispatcher.invoke(request, response);

      assertThat(response.getStatus()).isEqualTo(403);
      verify(findingsService, never()).getFindings(any(), any());
    }

    @Test
    void shouldThrowAuthorizationExceptionIfNotPermittedToWrite() throws URISyntaxException {
      String permission = "repository:writeTeamscaleFindings:" + REPOSITORY.getId();
      doThrow(AuthorizationException.class).when(subject).checkPermission(permission);

      String content = "teamscale findings: 2";
      byte[] contentJson = ("{\"content\" : \"" + content + "\"}").getBytes();
      when(repositoryManager.get(REPOSITORY.getNamespaceAndName())).thenReturn(REPOSITORY);

      MockHttpRequest request = MockHttpRequest
        .put("/v2/teamscale/pull-request/" + REPOSITORY.getNamespace() + "/" + REPOSITORY.getName() + "/1/findings")
        .content(contentJson)
        .contentType(FINDINGS_MEDIATYPE);

      MockHttpResponse response = new MockHttpResponse();

      restDispatcher.invoke(request, response);

      assertThat(response.getStatus()).isEqualTo(403);
      verify(findingsService, never()).setFindings(any(), any(), any());
    }

    @Test
    void shouldGetFindings() throws URISyntaxException, UnsupportedEncodingException {
      String content = "teamscale findings: 2";
      Findings findings = new Findings(content);
      when(findingsService.getFindings(REPOSITORY, "1")).thenReturn(findings);
      when(repositoryManager.get(REPOSITORY.getNamespaceAndName())).thenReturn(REPOSITORY);
      when(findingsMapper.map(findings, REPOSITORY, "1")).thenReturn(new FindingsDto(content));

      MockHttpRequest request = MockHttpRequest
        .get("/v2/teamscale/pull-request/" + REPOSITORY.getNamespace() + "/" + REPOSITORY.getName() + "/1/findings")
        .contentType(FINDINGS_MEDIATYPE);

      MockHttpResponse response = new MockHttpResponse();

      restDispatcher.invoke(request, response);

      assertThat(response.getStatus()).isEqualTo(200);
      assertThat(response.getContentAsString()).isEqualTo("{\"content\":\"teamscale findings: 2\"}");
    }

    @Test
    void shouldUpdateFindings() throws URISyntaxException {
      String content = "teamscale findings: 2";
      byte[] contentJson = ("{\"content\" : \"" + content + "\"}").getBytes();
      when(repositoryManager.get(REPOSITORY.getNamespaceAndName())).thenReturn(REPOSITORY);

      MockHttpRequest request = MockHttpRequest
        .put("/v2/teamscale/pull-request/" + REPOSITORY.getNamespace() + "/" + REPOSITORY.getName() + "/1/findings")
        .content(contentJson)
        .contentType(FINDINGS_MEDIATYPE);

      MockHttpResponse response = new MockHttpResponse();

      restDispatcher.invoke(request, response);

      verify(findingsService).setFindings(REPOSITORY, "1", content);
      assertThat(response.getStatus()).isEqualTo(204);
    }
  }
}

