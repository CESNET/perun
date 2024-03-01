package cz.metacentrum.perun.openapi;

import cz.metacentrum.perun.openapi.invoker.ApiClient;
import java.util.List;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

/**
 * Main Perun RPC client class. Uses ApiClient and model generated from OpenAPI description of Perun RPC API. The
 * ApiClient pools HTTP connections and keeps cookies. Use it like this:
 * <pre>
 *     PerunRPC perunRPC = new PerunRPC(PerunRPC.PERUN_URL_CESNET, user, password);
 *     try {
 *        Group group = perunRPC.getGroupsManager().getGroupById(1);
 *     } catch (HttpClientErrorException ex) {
 *         throw PerunException.to(ex);
 *     } catch (RestClientException ex) {
 *       LOG.error("connection problem",ex);
 *     }
 * </pre>
 *
 * @author Martin Kuba makub@ics.muni.cz
 */
public class PerunRPC {

  public static final String PERUN_URL_CESNET = "https://perun.cesnet.cz/krb/rpc";
  public static final String PERUN_URL_ELIXIR = "https://perun.elixir-czech.cz/krb/rpc";
  public static final String PERUN_URL_MUNI = "https://idm.ics.muni.cz/krb/rpc";

  private final ApiClient apiClient;

  private final AttributesManagerApi attributesManager;
  private final AuthzResolverApi authzResolver;
  private final DatabaseManagerApi databaseManager;
  private final ExtSourcesManagerApi extSourcesManager;
  private final FacilitiesManagerApi facilitiesManager;
  private final GroupsManagerApi groupsManager;
  private final MembersManagerApi membersManager;
  private final OwnersManagerApi ownersManager;
  private final RegistrarManagerApi registrarManager;
  private final ResourcesManagerApi resourcesManager;
  private final UsersManagerApi usersManager;
  private final UtilsApi utils;
  private final VosManagerApi vosManager;
  private final ServicesManagerApi servicesManager;

  public PerunRPC(RestTemplate restTemplate) {
    if (restTemplate == null) {
      restTemplate = new RestTemplate(new HttpComponentsClientHttpRequestFactory());
    }
    // set converters from HTTP response to Java objects
    restTemplate.setMessageConverters(List.of(
        // register JSON response converter to find modules including JsonNullableModule
        new MappingJackson2HttpMessageConverter(
            Jackson2ObjectMapperBuilder.json().findModulesViaServiceLoader(true).build()),
        // register String response converter
        new StringHttpMessageConverter()));

    //HTTP connection pooling and cookie reuse (PerunSession is created only for the first request)
    apiClient = new ApiClient(restTemplate);
    apiClient.setUserAgent("Perun OpenAPI Java client");
    //all the managers share the ApiClient and thus the connection pool and cookies
    attributesManager = new AttributesManagerApi(apiClient);
    authzResolver = new AuthzResolverApi(apiClient);
    databaseManager = new DatabaseManagerApi(apiClient);
    extSourcesManager = new ExtSourcesManagerApi(apiClient);
    facilitiesManager = new FacilitiesManagerApi(apiClient);
    groupsManager = new GroupsManagerApi(apiClient);
    membersManager = new MembersManagerApi(apiClient);
    ownersManager = new OwnersManagerApi(apiClient);
    registrarManager = new RegistrarManagerApi(apiClient);
    resourcesManager = new ResourcesManagerApi(apiClient);
    usersManager = new UsersManagerApi(apiClient);
    utils = new UtilsApi(apiClient);
    vosManager = new VosManagerApi(apiClient);
    servicesManager = new ServicesManagerApi(apiClient);
  }

  public PerunRPC() {
    this(null);
  }

  public PerunRPC(String perunURL, String username, String password, RestTemplate restTemplate) {
    this(restTemplate);
    apiClient.setBasePath(perunURL);
    apiClient.setUsername(username);
    apiClient.setPassword(password);
  }

  /**
   * Sets base path and credentials for HTTP basic authentication
   *
   * @param perunURL URL up to the "rpc" part, e.g. https://perun-dev.cesnet.cz/krb/rpc-joe
   * @param username for BasicAuth
   * @param password for BasicAuth
   */
  public PerunRPC(String perunURL, String username, String password) {
    this(perunURL, username, password, null);
  }

  /**
   * Provides generated ApiClient. It is initialized with RestTemplate and HttpComponentsClientHttpRequestFactory. The
   * ApiClient can be used for:
   * <ul>
   *     <li>setting authentication using e.g. <code>getApiClient().setBearerToken(token);</code></li>
   *     <li>setting base path e.g. <code>getApiClient().setBasePath("https://perun.example.org/oidc");</code></li>
   *     <li>setting user agent header, e.g. <code>getApiClient().setUserAgent("My application")</cod></li>
   * </ul>.
   *
   * @return ApiClient
   */
  public ApiClient getApiClient() {
    return apiClient;
  }

  public AttributesManagerApi getAttributesManager() {
    return attributesManager;
  }

  public AuthzResolverApi getAuthzResolver() {
    return authzResolver;
  }

  public DatabaseManagerApi getDatabaseManager() {
    return databaseManager;
  }

  public ExtSourcesManagerApi getExtSourcesManager() {
    return extSourcesManager;
  }

  public FacilitiesManagerApi getFacilitiesManager() {
    return facilitiesManager;
  }

  public GroupsManagerApi getGroupsManager() {
    return groupsManager;
  }

  public MembersManagerApi getMembersManager() {
    return membersManager;
  }

  public OwnersManagerApi getOwnersManager() {
    return ownersManager;
  }

  public RegistrarManagerApi getRegistrarManager() {
    return registrarManager;
  }

  public ResourcesManagerApi getResourcesManager() {
    return resourcesManager;
  }

  public ServicesManagerApi getServicesManager() {
    return servicesManager;
  }

  public UsersManagerApi getUsersManager() {
    return usersManager;
  }

  public UtilsApi getUtils() {
    return utils;
  }

  public VosManagerApi getVosManager() {
    return vosManager;
  }
}
