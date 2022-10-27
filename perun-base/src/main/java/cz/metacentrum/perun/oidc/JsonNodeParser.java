package cz.metacentrum.perun.oidc;

import com.fasterxml.jackson.databind.JsonNode;
import cz.metacentrum.perun.core.api.BeansUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.List;

public class JsonNodeParser {

	private static final String introspectionTimestampPropertyName = "auth_time";

	/**
	 * Returns mfa timestamp [epoch seconds] if acr value is equal to MFA acr value
	 * @param endpointResponse parsed response from userInfo endpoint
	 */
	public static String getMfaTimestamp(JsonNode endpointResponse) {
		String acrProperty = BeansUtils.getCoreConfig().getUserInfoEndpointAcrPropertyName();
		String acr = endpointResponse.path(acrProperty).asText();
		if (StringUtils.isNotEmpty(acr) && acr.equals(BeansUtils.getCoreConfig().getUserInfoEndpointMfaAcrValue())) {
			String introspectionTimestamp = endpointResponse.path(introspectionTimestampPropertyName).asText();
			if (StringUtils.isNotEmpty(introspectionTimestamp)) {
				return introspectionTimestamp;
			}
			// if introspection timestamp property not filled, user endpoint was used
			String mfaTimestampProperty = BeansUtils.getCoreConfig().getUserInfoEndpointMfaAuthTimestampPropertyName();
			String mfaTimestamp = endpointResponse.path(mfaTimestampProperty).asText();
			if (StringUtils.isNotEmpty(mfaTimestamp)) {
				return mfaTimestamp;
			}
		}

		return null;
	}

	/**
	 * Returns value of a simple non-compound property
	 * @param endpointResponse
	 * @param fieldName
	 * @return
	 */
	public static String getSimpleField(JsonNode endpointResponse, String fieldName) {
		return endpointResponse.path(fieldName).asText();
	}

	public static String getName(JsonNode endpointResponse) {
		String name = endpointResponse.path("name").asText();
		if(StringUtils.isEmpty(name)) {
			String firstName = endpointResponse.path("given_name").asText();
			String familyName = endpointResponse.path("family_name").asText();
			if(StringUtils.isNotEmpty(firstName) && StringUtils.isNotEmpty(familyName)) {
				name =  firstName + " " + familyName;
			}
		}

		return name;
	}

	public static String getIdpName(JsonNode endpointResponse) {
		List<String> pathToIdpName = BeansUtils.getCoreConfig().getUserInfoEndpointExtSourceFriendlyName();
		JsonNode node = endpointResponse;
		String idpName = "";
		for(String path: pathToIdpName) {
			node = node.path(path);
			if(node.isArray()) {
				node = node.get(0);
			}
			if(node.isTextual()) {
				idpName = node.asText();
			}
		}

		return idpName;
	}
	/**
	 * Parsing extsource login (sub) from endpoint response.
	 * We are looking for the first nonempty property value that can be login
	 * @param endpointResponse
	 * @return extsource login
	 */
	public static String getExtSourceLogin(JsonNode endpointResponse) {
		List<String> ExtSourceLoginOptions = BeansUtils.getCoreConfig().getUserInfoEndpointExtSourceLogin();
		String login = "";
		for (String property: ExtSourceLoginOptions) {
			JsonNode loginNode = endpointResponse.path(property);
			if (loginNode.isArray()) {
				loginNode = loginNode.get(0);
			}
			if (loginNode.isTextual()) {
				login = loginNode.asText();
			}
			if (StringUtils.isNotEmpty(login)) {
				return login;
			}
		}

		return login;
	}

	/**
	 * Parsing extsource name from endpoint response
	 * @param endpointResponse
	 * @return extsource name
	 */
	public static String getExtSourceName(JsonNode endpointResponse) {
		String pathToExtSourceName = BeansUtils.getCoreConfig().getUserInfoEndpointExtSourceName();
		return endpointResponse.path(pathToExtSourceName).asText();
	}
}
