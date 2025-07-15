package cz.metacentrum.perun.core.impl.modules;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Connector class used to call CEITEC CRM API.
 * It supports check for users existence and retrieval of existing or newly generated login and CN
 * to be later set to a proper attributes.
 *
 * @author Pavel Zlámal <zlamal@cesnet.cz>
 */
public class CeitecCrmConnector {

  private static final Logger LOG = LoggerFactory.getLogger(CeitecCrmConnector.class);
  private final ModulesYamlConfigLoader loader = new ModulesYamlConfigLoader();
  private final CloseableHttpClient httpClient = HttpClients.createDefault();
  private AccessToken accessToken = null;

  public CeitecCrmConnector() {
  }

  /**
   * Check whether user with specified CEITEC ID exists in CRM.
   *
   * @param ceitecId ID to check
   * @return TRUE when CRM API response is 200, FALSE when 404, throws exception otherwise
   */
  public boolean checkCrmUserExists(String ceitecId) {

    HttpGet request = getRequest(ceitecId, null, null);

    try {
      CloseableHttpResponse response = httpClient.execute(request);
      StatusLine statusLine = response.getStatusLine();
      if (404 == statusLine.getStatusCode()) {
        // User with passed ceitec_id doesn't exist
        return false;
      } else if (200 == statusLine.getStatusCode()) {
        // ok
        return true;
      } else {
        throw new HttpResponseException(statusLine.getStatusCode(), statusLine.getReasonPhrase());
      }
    } catch (IOException e) {
      throw handleException(e, ceitecId);
    }

  }

  /**
   * Reads CEITEC "login" from user entry in CRM. "CEITEC\" prefix is dropped.
   * Returns existing login if user is matched or generates new by pre-defined rules.
   *
   * @param ceitecId CEITEC ID
   * @param eppns Attribute with all users' EPPNs
   * @param user User to get login for
   * @return Existing or generated login to be set to u:d:login-namespace:ceitec
   */
  public String getLogin(String ceitecId, Attribute eppns, User user) {

    HttpGet request = getRequest(ceitecId, eppns, user);

    ResponseHandler<String> rh = httpResponse -> {
      StatusLine statusLine = httpResponse.getStatusLine();
      if (statusLine.getStatusCode() != 200) {
        throw new HttpResponseException(statusLine.getStatusCode(), statusLine.getReasonPhrase());
      }
      HttpEntity entity = httpResponse.getEntity();
      if (entity == null) {
        throw new ClientProtocolException("CRM API response contains no content.");
      }
      String json = EntityUtils.toString(entity, StandardCharsets.UTF_8);
      JSONObject o = new JSONObject(json);
      String login = o.getString("domainname");
      if (StringUtils.isNotBlank(login)) {
        return login.substring(login.indexOf("\\") + 1);
      } else {
        return null;
      }
    };

    try {
      return httpClient.execute(request, rh);
    } catch (IOException e) {
      throw handleException(e, ceitecId);
    }

  }

  /**
   * Reads CN (common name) attribute from user entry in CRM.
   * Returns existing CN if user is matched or generates new by pre-defined rules.
   *
   * @param ceitecId CEITEC ID
   * @param eppns Attribute with all users' EPPNs
   * @param user User to get CN for
   * @return Existing or generated CN to be set to u:d:cnCeitecAD
   */
  public String getCn(String ceitecId, Attribute eppns, User user) {

    HttpGet request = getRequest(ceitecId, eppns, user);

    ResponseHandler<String> rh = httpResponse -> {
      StatusLine statusLine = httpResponse.getStatusLine();
      if (statusLine.getStatusCode() != 200) {
        throw new HttpResponseException(statusLine.getStatusCode(), statusLine.getReasonPhrase());
      }
      HttpEntity entity = httpResponse.getEntity();
      if (entity == null) {
        throw new ClientProtocolException("CRM API response contains no content.");
      }
      String json = EntityUtils.toString(entity, StandardCharsets.UTF_8);
      JSONObject o = new JSONObject(json);
      return o.getString("ce_cn_ceitec_ad");
    };

    try {
      return httpClient.execute(request, rh);
    } catch (IOException e) {
      throw handleException(e, ceitecId);
    }

  }

  /**
   * Call GET on CEITEC CRM API to search for e-INFRA CZ users
   *
   * @param ceitecId Unique ID of user in CEITEC
   * @param eppns Users EPPNs known to Perun
   * @param user User know to Perun to get name from.
   * @return GET request to be executed
   */
  private HttpGet getRequest(String ceitecId, Attribute eppns, User user) {

    getAccessToken();

    if (!loader.moduleFileExists("Ceitec")) {
      throw new InternalErrorException("Configuration for CEITEC API is missing.");
    }
    String einfraUserUrl = loader.loadString("Ceitec", "getEinfraUserUrl");
    HttpGet getEinfraUserRequest = new HttpGet(einfraUserUrl);
    final URIBuilder uri = new URIBuilder(getEinfraUserRequest.getURI());

    if (!StringUtils.isEmpty(ceitecId)) {
      uri.addParameter("id", ceitecId);
    }
    if (user != null && !StringUtils.isEmpty(user.getFirstName())) {
      uri.addParameter("firstname", user.getFirstName());
    }
    if (user != null && !StringUtils.isEmpty(user.getLastName())) {
      uri.addParameter("lastname", user.getLastName());
    }
    if (eppns != null && eppns.getValue() != null) {
      eppns.valueAsList().forEach(eppn -> {
        uri.addParameter("eppns[]", eppn);
      });
    }
    try {
      getEinfraUserRequest.setURI(uri.build());
    } catch (URISyntaxException e) {
      throw new InternalErrorException("Unable to construct CEITEC CRM API request URI.", e);
    }

    // get access token
    getEinfraUserRequest.setHeader("Authorization", "Bearer " + accessToken.getAccessToken());

    return getEinfraUserRequest;

  }

  /**
   * Handle IO exceptions and translate them to proper perun exceptions.
   *
   * @param e IOException thrown when making request to CRM API.
   * @return InternalErrorException exception with proper message
   */
  private InternalErrorException handleException(IOException e, String ceitecId) {
    LOG.error("Error contacting CEITEC CRM API. Used CEITEC_ID: '" + ceitecId + "'.", e);
    if (e instanceof HttpResponseException) {
      int code = ((HttpResponseException) e).getStatusCode();
      String reason = ((HttpResponseException) e).getReasonPhrase();
      return new InternalErrorException("Request to CEITEC CRM returned an error " + code + " - " + reason, e);
    }
    return new InternalErrorException("Can't connect to CEITEC CRM API.", e);
  }

  /**
   * Loads access token used to call CEITEC CRM API
   */
  private void getAccessToken() {

    if (accessToken == null || accessToken.isExpired()) {

      if (!loader.moduleFileExists("Ceitec")) {
        throw new InternalErrorException("Configuration for CEITEC API is missing.");
      }
      String tokenUrl = loader.loadString("Ceitec", "tokenUrl");
      String apiKey = loader.loadString("Ceitec", "apiKey");

      // get access token
      HttpPost tokenRequest = new HttpPost(tokenUrl);
      JSONObject jsonObject = new JSONObject();
      jsonObject.put("apiKey", apiKey);
      StringEntity stringEntity = new StringEntity(jsonObject.toString(), ContentType.APPLICATION_JSON);
      tokenRequest.setEntity(stringEntity);
      ResponseHandler<AccessToken> rh = httpResponse -> {
        StatusLine statusLine = httpResponse.getStatusLine();
        if (statusLine.getStatusCode() >= 300) {
          throw new HttpResponseException(statusLine.getStatusCode(), statusLine.getReasonPhrase());
        }
        HttpEntity entity = httpResponse.getEntity();
        if (entity == null) {
          throw new ClientProtocolException("CRM API token endpoint response contains no content.");
        }
        try (InputStream instream = entity.getContent()) {
          return new ObjectMapper().readValue(instream, AccessToken.class);
        }
      };

      try {
        accessToken = httpClient.execute(tokenRequest, rh);
      } catch (IOException e) {
        LOG.error("Can't contact CEITEC API.", e);
        throw new InternalErrorException("Can't contact CEITEC API.", e);
      }

    }

  }

  /**
   * Reads configuration from /etc/perun/modules/Ceitec.yaml and returns TRUE
   * if migration of CEITEC users is currently running.
   *
   * @return TRUE if migration is running, FALSE otherwise.
   */
  public boolean isMigrationRunning() {
    return Boolean.parseBoolean(loader.loadString("Ceitec", "migrationRunning"));
  }

  /**
   * API access token returned from CRM login endpoint
   */
  private static class AccessToken {

    @JsonProperty
    String accessToken;
    @JsonProperty
    String expiresAt;

    public AccessToken() {
    }

    public AccessToken(String accessToken, String expiresAt) {
      this.accessToken = accessToken;
      this.expiresAt = expiresAt;
    }

    public String getAccessToken() {
      return accessToken;
    }

    public void setAccessToken(String accessToken) {
      this.accessToken = accessToken;
    }

    public String getExpiresAt() {
      return expiresAt;
    }

    public void setExpiresAt(String expiresAt) {
      this.expiresAt = expiresAt;
    }

    /**
     * Return TRUE if token is expired or 5 minutes before expiration.
     *
     * @return TRUE if token is expired or is soon to-be expired.
     */
    public boolean isExpired() {
      if (StringUtils.isBlank(accessToken) || StringUtils.isBlank(expiresAt)) {
        // missing token
        return true;
      }

      // check five minutes before expiration
      LocalDateTime now = LocalDateTime.now().plusMinutes(5);
      return now.isAfter(LocalDateTime.parse(expiresAt));

    }

  }

}
