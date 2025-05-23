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
import com.cloudogu.scm.review.comment.api.CommentDto;
import com.cloudogu.scm.review.pullrequest.api.PullRequestRootResource;
import com.cloudogu.scm.review.pullrequest.api.PullRequestSelector;
import com.cloudogu.scm.review.pullrequest.api.PullRequestSortSelector;
import com.cloudogu.scm.review.pullrequest.dto.PullRequestDto;
import com.google.common.base.Strings;
import de.otto.edison.hal.HalRepresentation;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import sonia.scm.api.v2.resources.ErrorDto;
import sonia.scm.plugin.Requires;
import sonia.scm.repository.NamespaceAndName;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryManager;
import sonia.scm.repository.RepositoryPermissions;
import sonia.scm.web.VndMediaType;

import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;

import static com.cloudogu.scm.teamscale.Constants.READ_FINDINGS_PERMISSION;
import static com.cloudogu.scm.teamscale.Constants.WRITE_FINDINGS_PERMISSION;

@OpenAPIDefinition(tags = {
  @Tag(name = "Pull Request for Teamscale", description = "Pull request endpoints provided by the teamscale-plugin")
})
@Requires("scm-review-plugin")
@Path("v2/teamscale/pull-request/")
public class PullRequestResource {

  private final PullRequestRootResource pullRequestRootResource;
  private final RepositoryManager repositoryManager;
  private final FindingsService findingsService;
  private final FindingsMapper findingsMapper;
  private final BranchResolver branchResolver;

  private static final String PR_MEDIATYPE = VndMediaType.PREFIX + "teamscalePullRequest" + VndMediaType.SUFFIX;
  private static final String FINDINGS_MEDIATYPE = VndMediaType.PREFIX + "teamscaleFindings" + VndMediaType.SUFFIX;

  @Inject
  public PullRequestResource(PullRequestRootResource pullRequestRootResource, RepositoryManager repositoryManager, FindingsService findingsService, FindingsMapper findingsMapper, BranchResolver branchResolver) {
    this.pullRequestRootResource = pullRequestRootResource;
    this.repositoryManager = repositoryManager;
    this.findingsService = findingsService;
    this.findingsMapper = findingsMapper;
    this.branchResolver = branchResolver;
  }

  @GET
  @Path("{namespace}/{name}/{pullRequestId}")
  @Produces(PR_MEDIATYPE)
  @Operation(
    summary = "Get pull request",
    description = "Returns a single pull request by id.",
    tags = "Pull Request for Teamscale",
    operationId = "teamscale_get_pull_request"
  )
  @ApiResponse(
    responseCode = "200",
    description = "success",
    content = @Content(
      mediaType = PR_MEDIATYPE,
      schema = @Schema(implementation = PullRequestDto.class)
    )
  )
  @ApiResponse(responseCode = "401", description = "not authenticated / invalid credentials")
  @ApiResponse(responseCode = "403", description = "not authorized, the current user does not have the \"readPullRequest\" privilege")
  @ApiResponse(
    responseCode = "500",
    description = "internal server error",
    content = @Content(
      mediaType = VndMediaType.ERROR_TYPE,
      schema = @Schema(implementation = ErrorDto.class)
    )
  )
  public PullRequestDto getSinglePullRequest(@Context UriInfo uriInfo,
                                             @PathParam("namespace") String namespace,
                                             @PathParam("name") String name,
                                             @PathParam("pullRequestId") String pullRequestId) {
    Repository repository = repositoryManager.get(new NamespaceAndName(namespace, name));
    PullRequestDto dto = pullRequestRootResource.getPullRequestResource().get(uriInfo, namespace, name, pullRequestId);
    setPullRequestBranchRevisions(repository, dto);

    return dto;
  }

  @GET
  @Path("{namespace}/{name}/")
  @Produces(PR_MEDIATYPE)
  @Operation(
    summary = "Collection of pull requests",
    description = "Returns a list of pull requests by status.",
    tags = "Pull Request for Teamscale",
    operationId = "teamscale_get_pull_request_collection"
  )
  @ApiResponse(
    responseCode = "200",
    description = "success",
    content = @Content(
      mediaType = PR_MEDIATYPE,
      schema = @Schema(implementation = HalRepresentation.class)
    )
  )
  @ApiResponse(responseCode = "401", description = "not authenticated / invalid credentials")
  @ApiResponse(responseCode = "403", description = "not authorized, the current user does not have the \"readPullRequest\" privilege")
  @ApiResponse(
    responseCode = "500",
    description = "internal server error",
    content = @Content(
      mediaType = VndMediaType.ERROR_TYPE,
      schema = @Schema(implementation = ErrorDto.class)
    )
  )
  public HalRepresentation getAllPullRequests(@Context UriInfo uriInfo,
                                              @PathParam("namespace") String namespace,
                                              @PathParam("name") String name,
                                              @QueryParam("status") @DefaultValue("OPEN") PullRequestSelector pullRequestSelector) {
    Repository repository = repositoryManager.get(new NamespaceAndName(namespace, name));

    HalRepresentation pullRequests = pullRequestRootResource
      .getAll(
        uriInfo,
        namespace,
        name,
        pullRequestSelector,
        PullRequestSortSelector.ID_ASC)
      .readEntity(HalRepresentation.class);

    pullRequests
      .getEmbedded()
      .getItemsBy("pullRequests")
      .forEach(embeddedPr ->
        setPullRequestBranchRevisions(repository, (PullRequestDto) embeddedPr)
      );

    return pullRequests;

  }

  private void setPullRequestBranchRevisions(Repository repository, PullRequestDto embeddedPr) {
    if (Strings.isNullOrEmpty(embeddedPr.getSourceRevision())) {
      embeddedPr.setSourceRevision(branchResolver.resolve(repository, embeddedPr.getSource()).getRevision());
    }
    if (Strings.isNullOrEmpty(embeddedPr.getTargetRevision())) {
      embeddedPr.setTargetRevision(branchResolver.resolve(repository, embeddedPr.getTarget()).getRevision());
    }
  }

  @POST
  @Path("comments/{namespace}/{name}/{pullRequestId}")
  @Consumes(PR_MEDIATYPE)
  @Operation(
    summary = "Create pull request comment",
    description = "Creates a new pull request comment.",
    tags = "Pull Request for Teamscale",
    operationId = "teamscale_create_comment"
  )
  @ApiResponse(responseCode = "201", description = "create success")
  @ApiResponse(responseCode = "400", description = "Invalid body, e.g. illegal change of namespace or name")
  @ApiResponse(responseCode = "401", description = "not authenticated / invalid credentials")
  @ApiResponse(responseCode = "403", description = "not authorized, the current user does not have the \"commentPullRequest\" privilege")
  @ApiResponse(
    responseCode = "500",
    description = "internal server error",
    content = @Content(
      mediaType = VndMediaType.ERROR_TYPE,
      schema = @Schema(implementation = ErrorDto.class)
    )
  )
  public Response createComment(@PathParam("namespace") String namespace,
                                @PathParam("name") String name,
                                @PathParam("pullRequestId") String pullRequestId,
                                @QueryParam("sourceRevision") String expectedSourceRevision,
                                @QueryParam("targetRevision") String expectedTargetRevision,
                                @Valid @NotNull CommentDto commentDto) {
    return pullRequestRootResource
      .getPullRequestResource()
      .comments()
      .create(namespace, name, pullRequestId, expectedSourceRevision, expectedTargetRevision, commentDto);
  }

  @GET
  @Path("comments/{namespace}/{name}/{pullRequestId}")
  @Produces(PR_MEDIATYPE)
  @Operation(
    summary = "Get all pull request comments",
    description = "Returns all pull request comments.",
    tags = "Pull Request for Teamscale",
    operationId = "teamscale_get_comments"
  )
  @ApiResponse(
    responseCode = "200",
    description = "success",
    content = @Content(
      mediaType = PR_MEDIATYPE,
      schema = @Schema(implementation = HalRepresentation.class)
    )
  )
  @ApiResponse(responseCode = "401", description = "not authenticated / invalid credentials")
  @ApiResponse(responseCode = "403", description = "not authorized, the current user does not have the \"commentPullRequest\" privilege")
  @ApiResponse(
    responseCode = "500",
    description = "internal server error",
    content = @Content(
      mediaType = VndMediaType.ERROR_TYPE,
      schema = @Schema(implementation = ErrorDto.class)
    )
  )
  public HalRepresentation getAllComments(@Context UriInfo uriInfo,
                                          @PathParam("namespace") String namespace,
                                          @PathParam("name") String name,
                                          @PathParam("pullRequestId") String pullRequestId) {
    return pullRequestRootResource.getPullRequestResource().comments().getAll(uriInfo, namespace, name, pullRequestId);
  }

  @DELETE
  @Path("comments/{namespace}/{name}/{pullRequestId}/{commentId}")
  @Operation(
    summary = "Delete pull request comment",
    description = "Deletes a pull request comment.",
    tags = "Pull Request for Teamscale",
    operationId = "teamscale_delete_comment"
  )
  @ApiResponse(responseCode = "204", description = "delete success or nothing to delete")
  @ApiResponse(responseCode = "401", description = "not authenticated / invalid credentials")
  @ApiResponse(responseCode = "403", description = "not authorized, the current user does not have the \"commentPullRequest\" privilege")
  @ApiResponse(
    responseCode = "500",
    description = "internal server error",
    content = @Content(
      mediaType = VndMediaType.ERROR_TYPE,
      schema = @Schema(implementation = ErrorDto.class)
    )
  )
  public Response deleteComment(@Context UriInfo uriInfo,
                                @PathParam("namespace") String namespace,
                                @PathParam("name") String name,
                                @PathParam("pullRequestId") String pullRequestId,
                                @PathParam("commentId") String commentId,
                                @QueryParam("sourceRevision") String expectedSourceRevision,
                                @QueryParam("targetRevision") String expectedTargetRevision) {
    pullRequestRootResource
      .getPullRequestResource()
      .comments()
      .getCommentResource()
      .deleteComment(uriInfo, namespace, name, pullRequestId, commentId, expectedSourceRevision, expectedTargetRevision);
    return Response.noContent().build();
  }

  @GET
  @Path("{namespace}/{name}/{pullRequestId}/findings")
  @Produces(FINDINGS_MEDIATYPE)
  @Operation(
    summary = "Get teamscale findings for pull request",
    description = "Returns teamscale findings for pull request.",
    tags = "Pull Request for Teamscale",
    operationId = "teamscale_get_findings"
  )
  @ApiResponse(
    responseCode = "200",
    description = "success",
    content = @Content(
      mediaType = FINDINGS_MEDIATYPE,
      schema = @Schema(implementation = HalRepresentation.class)
    )
  )
  @ApiResponse(responseCode = "401", description = "not authenticated / invalid credentials")
  @ApiResponse(responseCode = "403", description = "not authorized, the current user does not have the \"readTeamscaleFindings\" privilege")
  @ApiResponse(
    responseCode = "500",
    description = "internal server error",
    content = @Content(
      mediaType = VndMediaType.ERROR_TYPE,
      schema = @Schema(implementation = ErrorDto.class)
    )
  )
  public Response getFindings(@Context UriInfo uriInfo,
                              @PathParam("namespace") String namespace,
                              @PathParam("name") String name,
                              @PathParam("pullRequestId") String pullRequestId) {
    Repository repository = repositoryManager.get(new NamespaceAndName(namespace, name));
    RepositoryPermissions.custom(READ_FINDINGS_PERMISSION, repository).check();
    return Response.ok(findingsMapper.map(findingsService.getFindings(repository, pullRequestId), repository, pullRequestId)).build();
  }

  @PUT
  @Path("{namespace}/{name}/{pullRequestId}/findings")
  @Consumes(FINDINGS_MEDIATYPE)
  @Operation(
    summary = "Update teamscale findings for pull request",
    description = "Updates teamscale findings for pull request.",
    tags = "Pull Request for Teamscale",
    operationId = "teamscale_put_findings"
  )
  @ApiResponse(
    responseCode = "204",
    description = "no content"
  )
  @ApiResponse(responseCode = "401", description = "not authenticated / invalid credentials")
  @ApiResponse(responseCode = "403", description = "not authorized, the current user does not have the \"writeTeamscaleFindings\" privilege")
  @ApiResponse(
    responseCode = "500",
    description = "internal server error",
    content = @Content(
      mediaType = VndMediaType.ERROR_TYPE,
      schema = @Schema(implementation = ErrorDto.class)
    )
  )
  public void updateFindings(@Context UriInfo uriInfo,
                             @PathParam("namespace") String namespace,
                             @PathParam("name") String name,
                             @PathParam("pullRequestId") String pullRequestId,
                             FindingsDto findingsDto) {
    Repository repository = repositoryManager.get(new NamespaceAndName(namespace, name));
    RepositoryPermissions.custom(WRITE_FINDINGS_PERMISSION, repository).check();
    findingsService.setFindings(repository, pullRequestId, findingsDto.getContent());
  }
}
