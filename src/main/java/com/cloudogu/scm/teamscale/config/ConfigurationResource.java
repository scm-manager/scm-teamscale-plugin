package com.cloudogu.scm.teamscale.config;

import com.google.inject.Inject;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import sonia.scm.api.v2.resources.ErrorDto;
import sonia.scm.web.VndMediaType;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@OpenAPIDefinition(tags = {
  @Tag(name = "Teamscale Plugin", description = "Teamscale plugin provided endpoints")
})
@Path("v2/teamscale/configuration")
public class ConfigurationResource {

  private final ConfigurationService configurationService;

  @Inject
  public ConfigurationResource(ConfigurationService configurationService) {
    this.configurationService = configurationService;
  }

  @PUT
  @Path("/{namespace}/{name}")
  @Consumes({MediaType.APPLICATION_JSON})
  @Operation(
    summary = "Update repository-specific teamscale configuration",
    description = "Modifies the repository-specific teamscale configuration.",
    tags = "Teamscale Plugin",
    operationId = "teamscale_update_repo_config"
  )
  @ApiResponse(responseCode = "200", description = "success")
  @ApiResponse(responseCode = "401", description = "not authenticated / invalid credentials")
  @ApiResponse(responseCode = "403", description = "not authorized /  the current user does not have the right privilege")
  @ApiResponse(
    responseCode = "500",
    description = "internal server error",
    content = @Content(
      mediaType = VndMediaType.ERROR_TYPE,
      schema = @Schema(implementation = ErrorDto.class)
    )
  )
  public Response updateConfiguration(@PathParam("namespace") String namespace, @PathParam("name") String name, ConfigurationDto updatedConfig) {
    configurationService.updateConfig(namespace, name, updatedConfig);
    return Response.ok().build();
  }

  @GET
  @Path("/{namespace}/{name}")
  @Produces({MediaType.APPLICATION_JSON})
  @Operation(
    summary = "Get repository-specific teamscale configuration",
    description = "Returns the repository-specific teamscale configuration.",
    tags = "Teamscale Plugin",
    operationId = "teamscale_get_repo_config")
  @ApiResponse(
    responseCode = "200",
    description = "success",
    content = @Content(
      mediaType = MediaType.APPLICATION_JSON,
      schema = @Schema(implementation = ConfigurationDto.class)
    )
  )
  @ApiResponse(responseCode = "401", description = "not authenticated / invalid credentials")
  @ApiResponse(responseCode = "403", description = "not authorized /  the current user does not have the right privilege")
  @ApiResponse(
    responseCode = "404",
    description = "not found / no repository available for given parameters",
    content = @Content(
      mediaType = VndMediaType.ERROR_TYPE,
      schema = @Schema(implementation = ErrorDto.class)
    )
  )
  @ApiResponse(
    responseCode = "500",
    description = "internal server error",
    content = @Content(
      mediaType = VndMediaType.ERROR_TYPE,
      schema = @Schema(implementation = ErrorDto.class)
    )
  )
  public Response getConfiguration(@PathParam("namespace") String namespace, @PathParam("name") String name) {
    return Response.ok(configurationService.getConfiguration(namespace, name)).build();
  }
}
