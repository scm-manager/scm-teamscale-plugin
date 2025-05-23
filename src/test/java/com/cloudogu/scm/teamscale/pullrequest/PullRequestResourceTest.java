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

import com.cloudogu.scm.review.BranchResolver;
import com.cloudogu.scm.review.comment.api.CommentResource;
import com.cloudogu.scm.review.comment.api.CommentRootResource;
import com.cloudogu.scm.review.pullrequest.api.PullRequestRootResource;
import com.cloudogu.scm.review.pullrequest.api.PullRequestSelector;
import com.cloudogu.scm.review.pullrequest.dto.PullRequestDto;
import com.google.common.collect.ImmutableList;
import de.otto.edison.hal.Embedded;
import de.otto.edison.hal.HalRepresentation;
import de.otto.edison.hal.Link;
import de.otto.edison.hal.Links;
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
import sonia.scm.repository.Branch;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryManager;
import sonia.scm.repository.RepositoryTestData;
import sonia.scm.web.RestDispatcher;
import sonia.scm.web.VndMediaType;

import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PullRequestResourceTest {

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
  private BranchResolver branchResolver;

  @Mock
  private Subject subject;

  private RestDispatcher restDispatcher;

  @BeforeEach
  void initDispatcher() {
    PullRequestResource pullRequestResource = new PullRequestResource(pullRequestRootResource, repositoryManager, findingsService, findingsMapper, branchResolver);
    restDispatcher = new RestDispatcher();
    restDispatcher.addSingletonResource(pullRequestResource);
  }

  @Test
  void shouldGetAllPullRequestsWithSourceAndTargetRevisions() throws URISyntaxException, UnsupportedEncodingException {
    MockHttpRequest request = MockHttpRequest
      .get("/v2/teamscale/pull-request/" + REPOSITORY.getNamespace() + "/" + REPOSITORY.getName())
      .contentType(PR_MEDIATYPE);
    when(repositoryManager.get(REPOSITORY.getNamespaceAndName())).thenReturn(REPOSITORY);
    when(branchResolver.resolve(REPOSITORY, "source")).thenReturn(Branch.normalBranch("develop", "123456"));
    when(branchResolver.resolve(REPOSITORY, "target")).thenReturn(Branch.normalBranch("master", "987654"));

    when(pullRequestRootResource.getAll(any(UriInfo.class), anyString(), anyString(), any(PullRequestSelector.class), any()))
      .thenReturn(Response.ok(HalRepresentations.createCollection(
        true,
        "self",
        "create",
        ImmutableList.of(
          createPullRequestDto("1", "source", "target"),
          createPullRequestDto("2", "target", "source")),
        "pullRequests")).build()
      );

    MockHttpResponse response = new MockHttpResponse();

    restDispatcher.invoke(request, response);

    verify(pullRequestRootResource).getAll(any(UriInfo.class), anyString(), anyString(), any(PullRequestSelector.class), any());

    assertThat(response.getContentAsString())
      .contains("\"sourceRevision\":\"987654\",\"targetRevision\":\"123456\"")
      .contains("\"sourceRevision\":\"123456\",\"targetRevision\":\"987654\"");
  }

  private PullRequestDto createPullRequestDto(String id, String source, String target) {
    PullRequestDto dto = new PullRequestDto();
    dto.setSource(source);
    dto.setTarget(target);
    dto.setId(id);
    return dto;
  }

  @Test
  void shouldGetSinglePullRequest() throws URISyntaxException, UnsupportedEncodingException {
    when(pullRequestRootResource.getPullRequestResource()).thenReturn(reviewPullRequestResource);
    PullRequestDto pullRequestDto = createPullRequestDto("1","develop", "master");
    when(reviewPullRequestResource.get(any(), anyString(), anyString(), anyString())).thenReturn(pullRequestDto);
    when(repositoryManager.get(REPOSITORY.getNamespaceAndName())).thenReturn(REPOSITORY);
    when(branchResolver.resolve(REPOSITORY, "develop")).thenReturn(Branch.normalBranch("develop", "123456"));
    when(branchResolver.resolve(REPOSITORY, "master")).thenReturn(Branch.normalBranch("master", "987654"));
    MockHttpRequest request = MockHttpRequest
      .get("/v2/teamscale/pull-request/" + REPOSITORY.getNamespace() + "/" + REPOSITORY.getName() + "/1")
      .contentType(PR_MEDIATYPE);

    MockHttpResponse response = new MockHttpResponse();

    restDispatcher.invoke(request, response);

    verify(pullRequestRootResource).getPullRequestResource();

    assertThat(response.getContentAsString()).contains("\"sourceRevision\":\"123456\",\"targetRevision\":\"987654\"");
  }

  @Test
  void shouldGetSinglePullRequestWithSourceBranchDeleted() throws URISyntaxException, UnsupportedEncodingException {
    when(pullRequestRootResource.getPullRequestResource()).thenReturn(reviewPullRequestResource);
    PullRequestDto pullRequestDto = createPullRequestDto("1","develop", "master");
    pullRequestDto.setSourceRevision("555888");
    pullRequestDto.setTargetRevision("424242");
    when(reviewPullRequestResource.get(any(), anyString(), anyString(), anyString())).thenReturn(pullRequestDto);
    when(repositoryManager.get(REPOSITORY.getNamespaceAndName())).thenReturn(REPOSITORY);
    MockHttpRequest request = MockHttpRequest
      .get("/v2/teamscale/pull-request/" + REPOSITORY.getNamespace() + "/" + REPOSITORY.getName() + "/1")
      .contentType(PR_MEDIATYPE);

    MockHttpResponse response = new MockHttpResponse();

    restDispatcher.invoke(request, response);

    verify(pullRequestRootResource).getPullRequestResource();

    assertThat(response.getContentAsString()).contains("\"sourceRevision\":\"555888\",\"targetRevision\":\"424242\"");
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
      .post("/v2/teamscale/pull-request/comments/" + REPOSITORY.getNamespace() + "/" + REPOSITORY.getName() + "/1?sourceRevision=source&targetRevision=target")
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

  static class HalRepresentations {

    private HalRepresentations() {
    }

    public static HalRepresentation createCollection(
      boolean permittedToCreate,
      String selfLink,
      String createLink,
      List<? extends HalRepresentation> dtoList, String attributeName
    ) {
      Links.Builder builder = Links.linkingTo().self(selfLink);

      if (permittedToCreate) {
        builder.single(Link.link("create", createLink));
      }

      return new HalRepresentation(builder.build(), Embedded.embedded(attributeName, dtoList));
    }

  }
}
