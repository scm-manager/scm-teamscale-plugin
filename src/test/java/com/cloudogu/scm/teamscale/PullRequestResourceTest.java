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
package com.cloudogu.scm.teamscale;

import com.cloudogu.scm.review.comment.api.CommentResource;
import com.cloudogu.scm.review.comment.api.CommentRootResource;
import com.cloudogu.scm.review.pullrequest.api.PullRequestRootResource;
import com.cloudogu.scm.review.pullrequest.api.PullRequestSelector;
import org.jboss.resteasy.mock.MockHttpRequest;
import org.jboss.resteasy.mock.MockHttpResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryTestData;
import sonia.scm.web.RestDispatcher;
import sonia.scm.web.VndMediaType;

import javax.ws.rs.core.UriInfo;
import java.net.URISyntaxException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class PullRequestResourceTest {

  private final Repository REPOSITORY = RepositoryTestData.createHeartOfGold();

  private static final String MEDIATYPE = VndMediaType.PREFIX + "teamscale_pr" + VndMediaType.SUFFIX;

  @Mock
  private PullRequestRootResource pullRequestRootResource;

  @Mock
  private com.cloudogu.scm.review.pullrequest.api.PullRequestResource reviewPullRequestResource;

  @Mock
  private CommentRootResource commentRootResource;

  @Mock
  private CommentResource commentResource;


  private RestDispatcher restDispatcher;

  @BeforeEach
  void initDispatcher() {
    PullRequestResource pullRequestResource = new PullRequestResource(pullRequestRootResource);
    restDispatcher = new RestDispatcher();
    restDispatcher.addSingletonResource(pullRequestResource);
  }

  @Test
  void shouldGetAllPullRequests() throws URISyntaxException {
    MockHttpRequest request = MockHttpRequest
      .get("/v2/teamscale/pull-request/" + REPOSITORY.getNamespace() + "/" + REPOSITORY.getName())
      .contentType(MEDIATYPE);

    MockHttpResponse response = new MockHttpResponse();

    restDispatcher.invoke(request, response);

    verify(pullRequestRootResource).getAll(any(UriInfo.class), anyString(), anyString(), any(PullRequestSelector.class));
  }

  @Test
  void shouldGetSinglePullRequest() throws URISyntaxException {
    when(pullRequestRootResource.getPullRequestResource()).thenReturn(reviewPullRequestResource);
    MockHttpRequest request = MockHttpRequest
      .get("/v2/teamscale/pull-request/" + REPOSITORY.getNamespace() + "/" + REPOSITORY.getName() + "/1")
      .contentType(MEDIATYPE);

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
      .contentType(MEDIATYPE);

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
      .contentType(MEDIATYPE);

    MockHttpResponse response = new MockHttpResponse();

    restDispatcher.invoke(request, response);

    verify(pullRequestRootResource).getPullRequestResource();
    verify(reviewPullRequestResource).comments();
    verify(commentRootResource).getCommentResource();
    verify(commentResource).deleteComment(any(), anyString(), anyString(), anyString(), anyString(), anyString(), anyString());

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
      .contentType(MEDIATYPE);

    MockHttpResponse response = new MockHttpResponse();

    restDispatcher.invoke(request, response);

    verify(pullRequestRootResource).getPullRequestResource();
    verify(reviewPullRequestResource).comments();

    assertThat(response.getStatus()).isEqualTo(204);
  }

}
