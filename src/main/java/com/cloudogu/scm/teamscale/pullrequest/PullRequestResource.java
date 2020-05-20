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

import com.cloudogu.scm.review.comment.api.CommentDto;
import com.cloudogu.scm.review.pullrequest.api.PullRequestRootResource;
import com.cloudogu.scm.review.pullrequest.api.PullRequestSelector;
import com.cloudogu.scm.review.pullrequest.dto.PullRequestDto;
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

import javax.inject.Inject;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

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

  private static final String MEDIATYPE = VndMediaType.PREFIX + "teamscale_pr" + VndMediaType.SUFFIX;

  @Inject
  public PullRequestResource(PullRequestRootResource pullRequestRootResource, RepositoryManager repositoryManager, FindingsService findingsService, FindingsMapper findingsMapper) {
    this.pullRequestRootResource = pullRequestRootResource;
    this.repositoryManager = repositoryManager;
    this.findingsService = findingsService;
    this.findingsMapper = findingsMapper;
  }

  @GET
  @Path("{namespace}/{name}/{pullRequestId}")
  @Produces(MEDIATYPE)
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
      mediaType = MEDIATYPE,
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
    return pullRequestRootResource.getPullRequestResource().get(uriInfo, namespace, name, pullRequestId);
  }

  @GET
  @Path("{namespace}/{name}/")
  @Produces(MEDIATYPE)
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
      mediaType = MEDIATYPE,
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
    return pullRequestRootResource.getAll(uriInfo, namespace, name, pullRequestSelector);
  }

  @POST
  @Path("comments/{namespace}/{name}/{pullRequestId}/{commentId}")
  @Consumes(MEDIATYPE)
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
                                @PathParam("commentId") String commentId,
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
  @Produces(MEDIATYPE)
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
      mediaType = MEDIATYPE,
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
  @Produces(MEDIATYPE)
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
      mediaType = MEDIATYPE,
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
  @Consumes(MEDIATYPE)
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
