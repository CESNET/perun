package cz.metacentrum.perun.core.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import cz.metacentrum.perun.core.api.OidcConfig;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import org.springframework.core.io.Resource;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;


public class PerunOidcConfigLoader {

	private Resource configurationPath;

	public void setConfigurationPath(Resource configurationPath) {
		this.configurationPath = configurationPath;
	}

	public OidcConfig loadPerunOidcConfig() {
		OidcConfig oidcConfig = new OidcConfig();
		JsonNode rootNode = loadConfigurationFile(configurationPath);

		try {
			oidcConfig.setClientId(rootNode.get("client_id").asText());
			oidcConfig.setOidcDeviceCodeUri(rootNode.get("oidc_device_code_uri").asText());
			oidcConfig.setOidcTokenEndpointUri(rootNode.get("oidc_token_endpoint_uri").asText());
			oidcConfig.setOidcTokenRevokeEndpointUri(rootNode.get("oidc_token_revoke_endpoint_uri").asText());
			oidcConfig.setAcrValues(rootNode.get("acr_values").asText());
			oidcConfig.setScopes(rootNode.get("scopes").asText());
			oidcConfig.setPerunApiEndpoint(rootNode.get("perun_api_endpoint").asText());
			oidcConfig.setEnforceMfa(rootNode.get("enforce_mfa").asBoolean());
		} catch (NullPointerException ex) {
			throw new InternalErrorException("The format of perun-oidc-config.yml is incorrect. Check that all required fields are present.");
		}
		return oidcConfig;
	}

	private JsonNode loadConfigurationFile(Resource resource) {
		ObjectMapper objectMapper = new ObjectMapper(new YAMLFactory());
		JsonNode rootNode;
		try (InputStream is = resource.getInputStream()) {
			rootNode = objectMapper.readTree(is);
		} catch (FileNotFoundException e) {
			throw new InternalErrorException("Configuration file not found for oidc config. It should be in: " + resource, e);
		} catch (IOException e) {
			throw new InternalErrorException("IO exception was thrown during the processing of the file: " + resource, e);
		}

		return rootNode;
	}

}
