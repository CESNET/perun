package cz.metacentrum.perun.scim.api.endpoints;

import static cz.metacentrum.perun.scim.api.SCIMDefaults.GROUPS_PATH;
import static cz.metacentrum.perun.scim.api.SCIMDefaults.URN_GROUP;
import static cz.metacentrum.perun.scim.api.SCIMDefaults.URN_USER;
import static cz.metacentrum.perun.scim.api.SCIMDefaults.USERS_PATH;

import com.fasterxml.jackson.databind.ObjectMapper;
import cz.metacentrum.perun.scim.api.entities.ResourceTypeSCIM;
import cz.metacentrum.perun.scim.api.exceptions.SCIMException;
import jakarta.ws.rs.core.Response;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Endpoint controller, that returns all SCIM resource types.
 *
 * @author Sona Mastrakova <sona.mastrakova@gmail.com>
 * @date 08.10.2016
 */
public class ResourceTypesEndpointController {

  private static List<ResourceTypeSCIM> getAllResourceTypes() {
    List<ResourceTypeSCIM> resources = new ArrayList();

    // prepare user resource
    ResourceTypeSCIM userResource = new ResourceTypeSCIM();
    userResource.setId("User");
    userResource.setName("User");
    userResource.setEndpoint(USERS_PATH);
    userResource.setDescription("User Account");
    userResource.setSchema(URN_USER);
    resources.add(userResource);

    // prepare group resource
    ResourceTypeSCIM groupResource = new ResourceTypeSCIM();
    groupResource.setId("Group");
    groupResource.setName("Group");
    groupResource.setEndpoint(GROUPS_PATH);
    groupResource.setDescription("Group");
    groupResource.setSchema(URN_GROUP);
    resources.add(groupResource);

    return resources;
  }

  public Response getResourceTypes() throws SCIMException {
    try {
      List<ResourceTypeSCIM> result = new ArrayList<>();
      result.addAll(getAllResourceTypes());

      ObjectMapper mapper = new ObjectMapper();
      return Response.ok(mapper.writeValueAsString(result)).build();
    } catch (IOException ex) {
      throw new SCIMException("Cannot convert resource types to json string", ex);
    }
  }
}
