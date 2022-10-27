package cz.metacentrum.perun.oidc;

import com.fasterxml.jackson.databind.JsonNode;
import cz.metacentrum.perun.core.api.BeansUtils;
import cz.metacentrum.perun.core.api.exceptions.ExpiredTokenException;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.Map;

import static cz.metacentrum.perun.core.api.PerunPrincipal.MFA_TIMESTAMP;

public class UserDataResolver {

	private final String SOURCE_IDP_NAME = "sourceIdPName";
	private final String MAIL = "mail";
	private final String DISPLAY_NAME = "displayName";
	private final String EMAIL = "email";
	private static final Logger log = LoggerFactory.getLogger(UserDataResolver.class);
	private final EndpointCaller endpointCaller = new EndpointCaller();


	public EndpointResponse fetchUserData(String accessToken, String issuer, Map<String, String> additionalInformation) throws ExpiredTokenException {
		boolean useUserInfo = BeansUtils.getCoreConfig().getRequestUserInfoEndpoint();
		boolean useIntrospection = BeansUtils.getCoreConfig().getRequestIntrospectionEndpoint();

		EndpointResponse response = new EndpointResponse(null, null);
		if (!useUserInfo && !useIntrospection) {
			log.info("Fetching data from userInfo or introspection endpoint is not allowed.");
			return response;
		}

		JsonNode configurationResponse = endpointCaller.callConfigurationEndpoint(issuer);

		if (useIntrospection) {
			String url = JsonNodeParser.getSimpleField(configurationResponse, EndpointCaller.INTROSPECTION_ENDPOINT);
			if (url != null && !url.isBlank()) {
				JsonNode responseData = endpointCaller.callIntrospectionEndpoint(accessToken, url);
				response = processResponseData(responseData, additionalInformation);
				log.info("Retrieved info from introspection endpoint " + response);
			} else {
				log.error("Introspection endpoint URL not retrieved from well-known configuration from issuer " + issuer);
			}
		}

		if (useUserInfo) {
			String url = JsonNodeParser.getSimpleField(configurationResponse, EndpointCaller.USERINFO_ENDPOINT);
			if (url != null && !url.isBlank()) {
				JsonNode responseData = endpointCaller.callUserInfoEndpoint(accessToken, url);
				EndpointResponse userInfoResponse = processResponseData(responseData, additionalInformation);
				response = response.getIssuer() == null ? userInfoResponse : response;
				log.info("Retrieved info from user info endpoint " + response);
			} else {
				log.error("UserInfo endpoint URL not retrieved from well-known configuration from issuer " + issuer);
			}
		}

		return response;
	}

	public EndpointResponse fetchIntrospectionData(String accessToken, String issuer, Map<String, String> additionalInformation) throws ExpiredTokenException {
		boolean useIntrospection = BeansUtils.getCoreConfig().getRequestIntrospectionEndpoint();
		EndpointResponse response = new EndpointResponse(null, null);

		if (useIntrospection) {
			JsonNode configurationResponse = endpointCaller.callConfigurationEndpoint(issuer);
			String url = JsonNodeParser.getSimpleField(configurationResponse, EndpointCaller.INTROSPECTION_ENDPOINT);
			if (url != null && !url.isBlank()) {
				JsonNode responseData = endpointCaller.callIntrospectionEndpoint(accessToken, url);
				response = processResponseData(responseData, additionalInformation);
			} else {
				log.error("Introspection endpoint URL not retrieved from well-known configuration from issuer " + issuer);
			}
		}

		return response;
	}

	/**
	 * Filling additional information with user's name, user's email and user-friendly format of extsource name
	 * @param endpointResponse
	 * @param additionalInformation
	 */
	private void fillAdditionalInformationWithUserData(JsonNode endpointResponse, Map<String, String> additionalInformation) {
		String name = JsonNodeParser.getName(endpointResponse);
		if(StringUtils.isNotEmpty(name)) {
			additionalInformation.putIfAbsent(DISPLAY_NAME, name);
		}

		String email = JsonNodeParser.getSimpleField(endpointResponse, EMAIL);
		if(StringUtils.isNotEmpty(email)) {
			additionalInformation.putIfAbsent(MAIL, email);
		}

		// retrieve friendly IdP name from the nested property
		String idpName = JsonNodeParser.getIdpName(endpointResponse);
		if (StringUtils.isNotEmpty(idpName)) {
			additionalInformation.putIfAbsent(SOURCE_IDP_NAME, idpName);
		}

		// update MFA timestamp
		String mfaTimestamp = JsonNodeParser.getMfaTimestamp(endpointResponse);
		if (mfaTimestamp != null && !mfaTimestamp.isEmpty()) {
			Instant mfaReadableTimestamp = Instant.ofEpochSecond(Long.parseLong(mfaTimestamp));
			additionalInformation.putIfAbsent(MFA_TIMESTAMP, mfaReadableTimestamp.toString());
		}
	}

	private EndpointResponse processResponseData(JsonNode endpointResponse, Map<String, String> additionalInformation) {
		fillAdditionalInformationWithUserData(endpointResponse, additionalInformation);

		String extSourceName = JsonNodeParser.getExtSourceName(endpointResponse);
		if (StringUtils.isEmpty(extSourceName)) {
			log.info("Issuer from endpoint was empty or null: {}", extSourceName);
		}
		String extSourceLogin = JsonNodeParser.getExtSourceLogin(endpointResponse);
		if (StringUtils.isEmpty(extSourceLogin)) {
			log.info("User sub from endpoint was empty or null: {}", extSourceLogin);
		}

		return new EndpointResponse(extSourceName, extSourceLogin);
	}

}
