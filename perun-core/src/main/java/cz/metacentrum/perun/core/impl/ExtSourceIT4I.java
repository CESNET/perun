package cz.metacentrum.perun.core.impl;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.CollectionType;
import cz.metacentrum.perun.core.api.ExtSource;
import cz.metacentrum.perun.core.api.ExtSourcesManager;
import cz.metacentrum.perun.core.api.GroupsManager;
import cz.metacentrum.perun.core.api.PerunClient;
import cz.metacentrum.perun.core.api.PerunPrincipal;
import cz.metacentrum.perun.core.api.PerunSession;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.UserExtSource;
import cz.metacentrum.perun.core.api.exceptions.ExtSourceNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.ExtSourceUnsupportedOperationException;
import cz.metacentrum.perun.core.api.exceptions.SubjectNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.UserExtSourceNotExistsException;
import cz.metacentrum.perun.core.blImpl.GroupsManagerBlImpl;
import cz.metacentrum.perun.core.implApi.ExtSourceSimpleApi;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.CookieStore;
import org.apache.http.client.HttpClient;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Implementation of ExtSource for IT4I SCS API. It is used to retrieve groups (projects) from IT4I
 * including group members (list of users einfra logins).
 * <p>
 * All synchronizations must be set to "lightweight" mode with fixed members ext source value to "PERUN"
 * as IT4I doesn't provide full user data and Perun is source of truth in this case.
 *
 * @author Pavel Zlámal <zlamal@cesnet.cz>
 */
public class ExtSourceIT4I extends ExtSourceImpl implements ExtSourceSimpleApi {

  private final static Logger log = LoggerFactory.getLogger(ExtSourceIT4I.class);
  private final static String GROUP_PATH = "einfra-groups";
  private final static String USER_PATH = "einfra-group-members";
  // this will allow us to keep session on all subsequent synchronization calls
  private static CookieStore cookieStore = new BasicCookieStore();
  private static AccessToken accessToken = null;
  private final PerunPrincipal pp = new PerunPrincipal("perunSynchronizer", ExtSourcesManager.EXTSOURCE_NAME_INTERNAL,
      ExtSourcesManager.EXTSOURCE_INTERNAL);
  private PerunSession session = null;
  private String tokenUrl;
  private String apiUrl;
  private String username;
  private String password;

  @Override
  public List<Map<String, String>> findSubjectsLogins(String searchString)
      throws ExtSourceUnsupportedOperationException {
    throw new ExtSourceUnsupportedOperationException();
  }

  @Override
  public List<Map<String, String>> findSubjectsLogins(String searchString, int maxResults)
      throws ExtSourceUnsupportedOperationException {
    throw new ExtSourceUnsupportedOperationException();
  }

  @Override
  public Map<String, String> getSubjectByLogin(String login)
      throws SubjectNotExistsException, ExtSourceUnsupportedOperationException {
    throw new ExtSourceUnsupportedOperationException();
  }

  @Override
  public List<Map<String, String>> getGroupSubjects(Map<String, String> attributes)
      throws ExtSourceUnsupportedOperationException {

    init();

    // Value in the filter has already been set when group structure was synchronized and group created
    // Expected value supported by IT4I API is "group_id=?" where ? is replaced with groups login in "it4i" namespace.
    String filterQuery = attributes.get(GroupsManager.GROUPMEMBERSQUERY_ATTRNAME);
    String localFilter = (filterQuery != null) ? filterQuery : "";

    HttpClientBuilder httpClientBuilder = HttpClientBuilder.create();
    httpClientBuilder.setDefaultCookieStore(cookieStore);
    HttpClient httpClient = httpClientBuilder.build();

    HttpGet get = new HttpGet(apiUrl + USER_PATH + "?" + localFilter);
    get.setHeader("Authorization", "Bearer " + getToken());

    try {
      List<String> userLogins = httpClient.execute(get, new ResponseHandler<List<String>>() {
        @Override
        public List<String> handleResponse(HttpResponse httpResponse) throws ClientProtocolException, IOException {

          StatusLine statusLine = httpResponse.getStatusLine();
          if (statusLine.getStatusCode() >= 300) {
            throw new HttpResponseException(statusLine.getStatusCode(), statusLine.getReasonPhrase());
          }
          HttpEntity entity = httpResponse.getEntity();
          if (entity == null) {
            throw new ClientProtocolException("Response contains no content");
          }

          ObjectMapper mapper = new ObjectMapper();
          CollectionType collectionType =
              new ObjectMapper().getTypeFactory().constructCollectionType(List.class, String.class);

          try (InputStream instream = entity.getContent()) {
            return mapper.readValue(instream, collectionType);
          }

        }
      });

      List<Map<String, String>> result = new ArrayList<>();

      // return list of mappings with only logins, they will be mapped to existing UES based on the group members ExtSource by sync logic.
      for (String userLogin : userLogins) {
        Map<String, String> map = new HashMap<>();

        // convert external einfra login to perun user ID based on the user:def:login-namespace:einfra attribute value
        User user = perunBl.getModulesUtilsBl().getUserByLoginInNamespace(session, userLogin, "einfra");
        if (user == null) {
          log.warn(
              "Subject with login '{}' skipped when retrieved from IT4I SCS since no related User was found in Perun with the same login!",
              userLogin);
          continue;
        }
        // Check if proper UES exists and is mapped to the same user
        String extLogin = String.valueOf(user.getId());
        ExtSource ex = null;
        UserExtSource ues = null;
        try {
          ex = perunBl.getExtSourcesManagerBl().getExtSourceByName(session, ExtSourcesManager.EXTSOURCE_NAME_PERUN);
          ues = perunBl.getUsersManagerBl().getUserExtSourceByExtLogin(session, ex, extLogin);
          if (!Objects.equals(user.getId(), ues.getUserId())) {
            log.warn(
                "Subject with login '{}' skipped when retrieved from IT4I SCS since login value in Perun attribute is not mapped to the same User as existing UserExtSource in Perun.",
                userLogin);
            continue;
          }

          map.put("login", extLogin);
          result.add(map);

        } catch (ExtSourceNotExistsException e) {
          log.warn(
              "Subject with login '{}' skipped when retrieved from IT4I SCS since there is no ExtSource named PERUN.",
              userLogin);
        } catch (UserExtSourceNotExistsException e) {
          log.warn(
              "Subject with login '{}' skipped when retrieved from IT4I SCS since there is no UserExtSource with same login in Perun!",
              userLogin);
        }

      }

      return result;

    } catch (IOException e) {
      throw new RuntimeException(e);
    }

  }

  @Override
  public void close() throws ExtSourceUnsupportedOperationException {
    // no-op
  }

  @Override
  public List<Map<String, String>> getSubjectGroups(Map<String, String> attributes)
      throws ExtSourceUnsupportedOperationException {

    init();

    List<Map<String, String>> result = new ArrayList<>();
    String filterQuery = attributes.get(GroupsManager.GROUPSQUERY_ATTRNAME);
    String localFilter = (filterQuery != null) ? filterQuery : "";

    HttpClientBuilder httpClientBuilder = HttpClientBuilder.create();
    httpClientBuilder.setDefaultCookieStore(cookieStore);
    HttpClient httpClient = httpClientBuilder.build();

    HttpGet get = new HttpGet(apiUrl + GROUP_PATH + "?skip_members=true");
    get.setHeader("Authorization", "Bearer " + getToken());

    ResponseHandler<List<IT4IGroup>> rh = new ResponseHandler<List<IT4IGroup>>() {
      @Override
      public List<IT4IGroup> handleResponse(HttpResponse httpResponse) throws ClientProtocolException, IOException {

        StatusLine statusLine = httpResponse.getStatusLine();
        if (statusLine.getStatusCode() >= 300) {
          throw new HttpResponseException(statusLine.getStatusCode(), statusLine.getReasonPhrase());
        }
        HttpEntity entity = httpResponse.getEntity();
        if (entity == null) {
          throw new ClientProtocolException("Response contains no content");
        }

        ObjectMapper mapper = new ObjectMapper();
        CollectionType collectionType =
            new ObjectMapper().getTypeFactory().constructCollectionType(List.class, IT4IGroup.class);

        try (InputStream instream = entity.getContent()) {
          return mapper.readValue(instream, collectionType);
        }

      }
    };


    try {

      Map<String, IT4IGroup> structure = it4iGroupsToStructure(httpClient.execute(get, rh));
      // remove all entries not starting with our filter
      structure.keySet().stream().filter(s -> !s.startsWith(localFilter)).toList().forEach(structure.keySet()::remove);

      for (IT4IGroup group : structure.values()) {
        Map<String, String> map = new HashMap<>();
        map.put(GroupsManagerBlImpl.GROUP_LOGIN, group.getLogin());
        map.put(GroupsManagerBlImpl.GROUP_NAME, group.getGroup_name());
        map.put(GroupsManagerBlImpl.PARENT_GROUP_LOGIN, group.getParent_group_login());
        map.put(GroupsManagerBlImpl.GROUP_DESCRIPTION, group.getDescription());
        result.add(map);
      }

    } catch (IOException e) {
      throw new RuntimeException(e);
    }

    return result;

  }

  @Override
  public List<Map<String, String>> getUsersSubjects() throws ExtSourceUnsupportedOperationException {
    throw new ExtSourceUnsupportedOperationException();
  }

  /**
   * Initialize ExtSource by its configuration
   */
  private void init() {
    tokenUrl = getAttributes().get("tokenUrl");
    apiUrl = getAttributes().get("apiUrl");
    username = getAttributes().get("username");
    password = getAttributes().get("password");
    if (this.session == null) {
      this.session = perunBl.getPerunSession(pp, new PerunClient());
    }
  }

  /**
   * Converts list of all IT4I group to the structure based on the group login and parent login.
   * Logins converted to the perun group naming format "top_group:sub_group:sub_sub_group" are used as keys in the mapping.
   *
   * @param it4IGroups List of groups retrieved from SCS
   * @return Mapping of full group logins to the group
   */
  private Map<String, IT4IGroup> it4iGroupsToStructure(List<IT4IGroup> it4IGroups) {

    Map<String, IT4IGroup> result = new HashMap<>();
    buildGroupsStructure(result, "", null, it4IGroups);
    return result;

  }

  /**
   * Recursively build the IT4IGroups structure from the list of groups.
   *
   * @param structure   structure to put resolved groups to
   * @param prefix      complete group prefix updated during recursive call like "top:", "top:parent:", ....
   * @param parentLogin current parent group login
   * @param groups      groups to process
   */
  private void buildGroupsStructure(Map<String, IT4IGroup> structure, String prefix, String parentLogin,
                                    List<IT4IGroup> groups) {

    for (IT4IGroup group : groups) {

      String login = group.getLogin();
      String groupParentLogin = group.getParent_group_login();

      if (Objects.equals(parentLogin, groupParentLogin)) {
        structure.put(prefix + login, group);
        buildGroupsStructure(structure, prefix + login + ":", login, groups);
      }

    }

  }

  /**
   * Retrieves access token value from the IT4I SCS API token endpoint.
   * It re-uses value of existing valid token or replaces it with the new one.
   *
   * @return access token value
   */
  private synchronized String getToken() {

    if (accessToken != null && accessToken.isValid()) {
      return accessToken.getAccess_token();
    }

    HttpClientBuilder httpClientBuilder = HttpClientBuilder.create();
    httpClientBuilder.setDefaultCookieStore(cookieStore);
    HttpClient httpClient = httpClientBuilder.build();

    NameValuePair clientId = new BasicNameValuePair("client_id", username);
    NameValuePair clientSecret = new BasicNameValuePair("client_secret", password);
    NameValuePair grantType = new BasicNameValuePair("grant_type", "client_credentials");

    HttpEntity body =
        new UrlEncodedFormEntity(Arrays.asList(clientId, clientSecret, grantType), StandardCharsets.UTF_8);
    HttpPost post = new HttpPost(tokenUrl);
    post.setEntity(body);
    ResponseHandler<AccessToken> rh = new ResponseHandler<AccessToken>() {
      @Override
      public AccessToken handleResponse(HttpResponse httpResponse) throws IOException {

        StatusLine statusLine = httpResponse.getStatusLine();
        if (statusLine.getStatusCode() >= 300) {
          throw new HttpResponseException(statusLine.getStatusCode(), statusLine.getReasonPhrase());
        }
        HttpEntity entity = httpResponse.getEntity();
        if (entity == null) {
          throw new ClientProtocolException("Response contains no content");
        }
        try (InputStream instream = entity.getContent()) {
          return new ObjectMapper().readValue(instream, AccessToken.class);
        }

      }
    };

    try {
      accessToken = httpClient.execute(post, rh);
      log.trace("{}", accessToken);
      return accessToken.getAccess_token();
    } catch (IOException e) {
      log.error("Couldn't contact token endpoint.", e);
      return null;
    }

  }

  /**
   * Representation of Group returned from IT4I SCS API endpoint.
   */
  static class IT4IGroup implements Comparable<IT4IGroup> {

    private String login;
    private String parent_group_login;
    private String group_name;
    private String description;
    private List<String> members;

    public String getLogin() {
      return login;
    }

    public void setLogin(String login) {
      this.login = login;
    }

    public String getParent_group_login() {
      return parent_group_login;
    }

    public void setParent_group_login(String parent_group_login) {
      this.parent_group_login = parent_group_login;
    }

    public String getGroup_name() {
      return group_name;
    }

    public void setGroup_name(String group_name) {
      this.group_name = group_name;
    }

    public String getDescription() {
      return description;
    }

    public void setDescription(String description) {
      this.description = description;
    }

    public List<String> getMembers() {
      return members;
    }

    public void setMembers(List<String> members) {
      this.members = members;
    }

    @Override
    public int compareTo(ExtSourceIT4I.IT4IGroup o) {
      return login.compareTo(o.getLogin());
    }

  }

  /**
   * Class represent access token returned from the IT4I SCS API token endpoint.
   * We locally store time of creation to determine validity later on.
   */
  static class AccessToken {
    @JsonIgnore
    private final Instant createdIn;
    private String access_token;
    private long expires_in;
    private long refresh_expires_in;
    private String token_type;
    @JsonProperty(value = "not-before-policy")
    private long not_before_policy;
    private String scope;

    public AccessToken() {
      createdIn = Instant.now();
    }

    /**
     * Return actual access token value which can be used when calling SCS IT4I API.
     *
     * @return access token value
     */
    public String getAccess_token() {
      return access_token;
    }

    public void setAccess_token(String access_token) {
      this.access_token = access_token;
    }

    public long getExpires_in() {
      return expires_in;
    }

    public void setExpires_in(long expires_in) {
      this.expires_in = expires_in;
    }

    public long getRefresh_expires_in() {
      return refresh_expires_in;
    }

    public void setRefresh_expires_in(long refresh_expires_in) {
      this.refresh_expires_in = refresh_expires_in;
    }

    public String getToken_type() {
      return token_type;
    }

    public void setToken_type(String token_type) {
      this.token_type = token_type;
    }

    public long getNot_before_policy() {
      return not_before_policy;
    }

    public void setNot_before_policy(long not_before_policy) {
      this.not_before_policy = not_before_policy;
    }

    public String getScope() {
      return scope;
    }

    public void setScope(String scope) {
      this.scope = scope;
    }

    /**
     * Check access token validity based on the time of creation and expiration.
     *
     * @return TRUE if access token is still valid
     */
    public boolean isValid() {
      return Instant.now().isBefore(createdIn.plus(getExpires_in(), ChronoUnit.SECONDS));
    }

  }

}
