package cz.metacentrum.perun.scim.api;

/**
 * SCIMDefaults class contains all endpoints, urns and other important constants.
 *
 * @author Sona Mastrakova <sona.mastrakova@gmail.com>
 * @date 08.10.2016
 */
public class SCIMDefaults {

  public static final String BASE_PATH = "api/v2";
  public static final String USERS_PATH = "/Users";
  public static final String GROUPS_PATH = "/Groups";
  public static final String SERVICE_PROVIDER_CONFIGS_PATH = "/ServiceProviderConfigs";
  public static final String RESOURCE_TYPES_PATH = "/ResourceTypes";
  public static final String SCHEMAS_PATH = "/Schemas";

  public static final String URN_USER = "urn:ietf:params:scim:schemas:core:2.0:User";
  public static final String URN_GROUP = "urn:ietf:params:scim:schemas:core:2.0:Group";
  public static final String URN_SERVICE_PROVIDER_CONFIG =
      "urn:ietf:params:scim:schemas:core:2.0:ServiceProviderConfig";
  public static final String URN_RESOURCE_TYPE = "urn:ietf:params:scim:schemas:core:2.0:ResourceType";
  public static final String URN_SCHEMA = "urn:ietf:params:scim:schemas:core:2.0:Schema";
  public static final String URN_LIST_RESPONSE = "urn:ietf:params:scim:api:messages:2.0:ListResponseDto";

  public static final String AUTH_DOCUMENTATION =
      "https://gitlab.ics.muni.cz/perun/perun-idm/perun/-/tree/main/perun-scim";
  public static final String AUTH_HTTP_NAME = "HTTP Basic";
  public static final String AUTH_HTTP_DESC = "HTTP Basic Standard";
  public static final String AUTH_OAUTH2_NAME = "OAuth 2.0";
  public static final String AUTH_OAUTH2_DESC = "OAuth 2.0 Protocol";
}
