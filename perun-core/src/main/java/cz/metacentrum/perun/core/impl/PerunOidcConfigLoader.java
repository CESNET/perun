package cz.metacentrum.perun.core.impl;

import cz.metacentrum.perun.core.api.OidcConfig;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import tools.jackson.databind.JsonNode;
import tools.jackson.dataformat.yaml.YAMLMapper;

public class PerunOidcConfigLoader {

  private static final Logger LOG = LoggerFactory.getLogger(PerunOidcConfigLoader.class);
  private Resource configurationPath;

  private static OidcConfig getOidcConfigFromJsonNode(JsonNode rootNode) {
    OidcConfig oidcConfig = new OidcConfig();
    try {
      oidcConfig.setClientId(rootNode.get("client_id").asString());
      oidcConfig.setOidcDeviceCodeUri(rootNode.get("oidc_device_code_uri").asString());
      oidcConfig.setOidcTokenEndpointUri(rootNode.get("oidc_token_endpoint_uri").asString());
      oidcConfig.setOidcTokenRevokeEndpointUri(rootNode.get("oidc_token_revoke_endpoint_uri").asString());
      oidcConfig.setAcrValues(rootNode.get("acr_values").asString());
      oidcConfig.setScopes(rootNode.get("scopes").asString());
      oidcConfig.setPerunApiEndpoint(rootNode.get("perun_api_endpoint").asString());
      oidcConfig.setEnforceMfa(rootNode.get("enforce_mfa").asBoolean());
    } catch (NullPointerException ex) {
      throw new InternalErrorException(
          "The format of perun-oidc-config.yml is incorrect. Check that all required fields are present.");
    }
    return oidcConfig;
  }

  private JsonNode loadConfigurationFile(Resource resource) throws FileNotFoundException {
    YAMLMapper objectMapper = YAMLMapper.builder().build();
    JsonNode rootNode;
    try (InputStream is = resource.getInputStream()) {
      rootNode = objectMapper.readTree(is);
    } catch (FileNotFoundException e) {
      throw e;
    } catch (IOException e) {
      throw new InternalErrorException("IO exception was thrown during the processing of the file: " + resource, e);
    }

    return rootNode;
  }

  public Map<String, OidcConfig> loadPerunOidcConfigs() {
    Map<String, OidcConfig> oidcConfigs = new HashMap<>();
    JsonNode rootNode;
    try {
      rootNode = loadConfigurationFile(configurationPath);
    } catch (FileNotFoundException ex) {
      LOG.debug("Configuration file for OIDC configurations was not found in : {}, continuing without it.",
          configurationPath);
      return null;
    }

    Iterator<String> configNames = rootNode.propertyNames().iterator();

    while (configNames.hasNext()) {
      String configName = configNames.next();
      oidcConfigs.put(configName, getOidcConfigFromJsonNode(rootNode.get(configName)));
    }
    return oidcConfigs;
  }

  public void setConfigurationPath(Resource configurationPath) {
    this.configurationPath = configurationPath;
  }

}
