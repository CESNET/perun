package cz.metacentrum.perun.scim;

import cz.metacentrum.perun.core.api.PerunSession;

import static cz.metacentrum.perun.scim.api.SCIMDefaults.BASE_PATH;
import static cz.metacentrum.perun.scim.api.SCIMDefaults.GROUPS_PATH;
import static cz.metacentrum.perun.scim.api.SCIMDefaults.RESOURCE_TYPES_PATH;
import static cz.metacentrum.perun.scim.api.SCIMDefaults.SCHEMAS_PATH;
import static cz.metacentrum.perun.scim.api.SCIMDefaults.SERVICE_PROVIDER_CONFIGS_PATH;
import static cz.metacentrum.perun.scim.api.SCIMDefaults.USERS_PATH;

import cz.metacentrum.perun.scim.api.exceptions.SCIMException;
import cz.metacentrum.perun.scim.api.endpoints.GroupResourceEndpointController;
import cz.metacentrum.perun.scim.api.endpoints.ResourceTypesEndpointController;
import cz.metacentrum.perun.scim.api.endpoints.SchemasEndpointController;
import cz.metacentrum.perun.scim.api.endpoints.ServiceProviderConfigsEndpointController;
import cz.metacentrum.perun.scim.api.endpoints.UserResourceEndpointController;

import javax.ws.rs.core.Response;

import org.springframework.stereotype.Component;

/**
 * SCIM protocol
 * <p>
 * SCIM (System for Cross-domain Identity Management) is a protocol for
 * cross-domain access to information about users and groups. With SCIM protocol
 * it is possible to create/update/get/delete resources. Currently only get
 * methods are supported.
 *
 * @author Sona Mastrakova <sona.mastrakova@gmail.com>
 * @date 08.10.2016
 */
@Component
public class SCIM {

  public Response process(PerunSession session, String path, String params) throws SCIMException {

    if (session.getPerunPrincipal().getUser() == null) {
      throw new SCIMException("invalid_user");
    }

    if (path.contains(BASE_PATH + USERS_PATH)) {
      UserResourceEndpointController controller = new UserResourceEndpointController(session);
      String[] pathArray = path.split(BASE_PATH + USERS_PATH);
      String userId = pathArray[1];

      if (userId != null) {
        return controller.getUser(userId.replaceAll("/", ""));
      } else {
        throw new SCIMException("invalid_request");
      }
    }

    if (path.contains(BASE_PATH + GROUPS_PATH)) {
      GroupResourceEndpointController controller = new GroupResourceEndpointController(session);
      String[] pathArray = path.split(BASE_PATH + GROUPS_PATH);
      String groupId = pathArray[1];

      if (groupId != null) {
        return controller.getGroup(groupId.replaceAll("/", ""));
      } else {
        throw new SCIMException("invalid_request");
      }
    }

    if (path.contains(BASE_PATH + SERVICE_PROVIDER_CONFIGS_PATH)) {
      ServiceProviderConfigsEndpointController controller = new ServiceProviderConfigsEndpointController();

      return controller.getServiceProviderConfigs();
    }

    if (path.contains(BASE_PATH + RESOURCE_TYPES_PATH)) {
      ResourceTypesEndpointController controller = new ResourceTypesEndpointController();

      return controller.getResourceTypes();
    }

    if (path.contains(BASE_PATH + SCHEMAS_PATH)) {
      SchemasEndpointController controller = new SchemasEndpointController();

      return controller.getSchemas();
    }

    // else
    throw new SCIMException("invalid_request");
  }
}
