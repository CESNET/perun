package cz.metacentrum.perun.openapi;

import cz.metacentrum.perun.openapi.invoker.ApiClient;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

/**
 * Main Perun RPC client class. Uses ApiClient and model generated from OpenAPI description of Perun RPC API.
 * The ApiClient pools HTTP connections and keeps cookies.
 * Use it like this:
 * <pre>
 *     PerunRPC perunRPC = new PerunRPC(PerunRPC.PERUN_URL_CESNET, user, password);
 *     try {
 *        Group group = perunRPC.getGroupsManager().getGroupById(1);
 *     } catch (HttpClientErrorException ex) {
 *         throw PerunException.to(ex);
 *     } catch (RestClientException ex) {
 *        log.error("connection problem",ex);
 *     }
 * </pre>
 * @author Martin Kuba makub@ics.muni.cz
 */
public class PerunRPC {

    public static final String PERUN_URL_CESNET = "https://perun.cesnet.cz/krb/rpc";
    public static final String PERUN_URL_ELIXIR = "https://perun.elixir-czech.cz/krb/rpc";
    public static final String PERUN_URL_MUNI = "https://idm.ics.muni.cz/krb/rpc";

    final private ApiClient apiClient;

    final private AttributesManagerApi attributesManager;
    final private AuthzResolverApi authzResolver;
    final private DatabaseManagerApi databaseManager;
    final private ExtSourcesManagerApi extSourcesManager;
    final private FacilitiesManagerApi facilitiesManager;
    final private GroupsManagerApi groupsManager;
    final private MembersManagerApi membersManager;
    final private OwnersManagerApi ownersManager;
    final private RegistrarManagerApi registrarManager;
    final private ResourcesManagerApi resourcesManager;
    final private UsersManagerApi usersManager;
    final private UtilsApi utils;
    final private VosManagerApi vosManager;

    public PerunRPC(RestTemplate restTemplate) {
    	if(restTemplate==null) {
    		restTemplate = new RestTemplate(new HttpComponentsClientHttpRequestFactory());
		}
		//HTTP connection pooling and cookie reuse (PerunSession is created only for the first request)
		apiClient = new ApiClient(restTemplate);
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
	}

    public PerunRPC() {
		this(null);
	}

	public PerunRPC(String perunURL, String username, String password, RestTemplate restTemplate) {
    	this(restTemplate);
		apiClient.setBasePath(perunURL);
		apiClient.setUsername(username);
		apiClient.setPassword(password);
		apiClient.setUserAgent("Perun OpenAPI Java client");
	}

    /**
     * Sets base path and credentials for HTTP basic authentication
     * @param perunURL URL up to the "rpc" part, e.g. https://perun-dev.cesnet.cz/krb/rpc-joe
     * @param username for BasicAuth
     * @param password for BasicAuth
     */
    public PerunRPC(String perunURL, String username, String password) {
        this(perunURL, username, password, null);
    }

    /**
     * Provides generated ApiClient. It is initialized with RestTemplate and HttpComponentsClientHttpRequestFactory.
     * The ApiClient can be used for:
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
