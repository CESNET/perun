package cz.metacentrum.perun.scim.api.endpoints;

import static cz.metacentrum.perun.scim.api.SCIMDefaults.AUTH_DOCUMENTATION;
import static cz.metacentrum.perun.scim.api.SCIMDefaults.AUTH_OAUTH2_DESC;
import static cz.metacentrum.perun.scim.api.SCIMDefaults.AUTH_OAUTH2_NAME;
import static cz.metacentrum.perun.scim.api.SCIMDefaults.URN_SERVICE_PROVIDER_CONFIG;

import cz.metacentrum.perun.scim.api.entities.AuthenticationSchemes;
import cz.metacentrum.perun.scim.api.entities.ServiceProviderConfiguration;
import cz.metacentrum.perun.scim.api.exceptions.SCIMException;
import jakarta.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.json.JsonMapper;

/**
 * Service Provider Configuration endpoint, that returns specification compliance, authentication schemas and data
 * models.
 *
 * @author Sona Mastrakova <sona.mastrakova@gmail.com>
 * @date 08.10.2016
 */
public class ServiceProviderConfigsEndpointController {

  private List<AuthenticationSchemes> getAuthenticationSchemes() {
    List<AuthenticationSchemes> schemes = new ArrayList<>();

    AuthenticationSchemes autheticationSchemes = new AuthenticationSchemes();
    autheticationSchemes.setName(AUTH_OAUTH2_NAME);
    autheticationSchemes.setDescription(AUTH_OAUTH2_DESC);

    schemes.add(autheticationSchemes);
    return schemes;
  }

  public Response getServiceProviderConfigs() throws SCIMException {
    try {
      ServiceProviderConfiguration result = new ServiceProviderConfiguration();
      result.setAuthenticationSchemes(getAuthenticationSchemes());
      result.setDocumentationUrl(AUTH_DOCUMENTATION);
      result.setPatchSupport(false);
      result.setFilterSupport(false);
      result.setBulkSupport(false);
      result.setChangePasswordSupport(false);
      result.setEtagSupport(false);
      result.setSortSupport(false);
      result.setXmlDataFormatSupport(false);

      List<String> schemas = new ArrayList<>();
      schemas.add(URN_SERVICE_PROVIDER_CONFIG);
      result.setSchemas(schemas);

      JsonMapper mapper = JsonMapper.builder().build();
      return Response.ok(mapper.writeValueAsString(result)).build();
    } catch (JacksonException ex) {
      throw new SCIMException("Cannot convert service provider configuration to json string", ex);
    }
  }
}
