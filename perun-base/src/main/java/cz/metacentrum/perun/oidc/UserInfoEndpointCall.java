package cz.metacentrum.perun.oidc;

import com.fasterxml.jackson.databind.JsonNode;
import cz.metacentrum.perun.core.api.BeansUtils;
import cz.metacentrum.perun.core.api.exceptions.ExpiredTokenException;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import static cz.metacentrum.perun.core.api.PerunPrincipal.MFA_TIMESTAMP;

/**
 * Class for executing call to User info endpoint.
 *
 * @author Lucie Kureckova <luckureckova@gmail.com>
 */
public class UserInfoEndpointCall {

	private final static Logger log = LoggerFactory.getLogger(UserInfoEndpointCall.class);

	public UserInfoEndpointResponse getUserInfoEndpointData(String accessToken, String issuer, Map<String, String> additionalInformation) throws ExpiredTokenException {
		RestTemplate restTemplate = new RestTemplate();
		JsonNode config = restTemplate.getForObject(issuer + "/.well-known/openid-configuration", JsonNode.class);
		HttpHeaders headers = new HttpHeaders();
		headers.setBearerAuth(accessToken);
		HttpEntity<Void> entity = new HttpEntity<>(headers);
		ResponseEntity<JsonNode> userInfoResponse;
		try {
			userInfoResponse = restTemplate.exchange(config.path("userinfo_endpoint").textValue(), HttpMethod.GET, entity, JsonNode.class);
		} catch (HttpClientErrorException ex) {
			if (ex.getStatusCode() == HttpStatus.FORBIDDEN) {
				log.error("Token {} is no longer valid for issuer: {}", accessToken, issuer);
				throw new ExpiredTokenException("Your token is no longer valid. Please retry from the start.");
			} else {
				log.error("Failed to get userInfoResponse for access token: {}. The response code was: {}", accessToken, ex.getStatusCode());
				throw new InternalErrorException("Failed to get userInfoResponse for access token: " + accessToken + ". The response code was: " + ex.getStatusCode());
			}
		}
		JsonNode userInfo = userInfoResponse.getBody();
		if(StringUtils.isNotEmpty(userInfo.path("error").asText())) {
			log.error("Call to user info endpoint failed, the error is: {}", userInfo);
			throw new InternalErrorException("Call to user info endpoint failed, the error is" + userInfo);
		}
		log.debug("user info retrieved: {}", userInfo);

		fillAdditionalInformationWithDataFromUserInfo(userInfo, additionalInformation);
		fillMfaTimestamp(userInfo, additionalInformation);

		String extSourceName = getExtSourceName(userInfo);

		String extSourceLogin = getExtSourceLogin(userInfo);

		return new UserInfoEndpointResponse(extSourceName, extSourceLogin);
	}

	/**
	 * Parsing extsource login (sub) from user info.
	 * We are looking for the first nonempty property value that can be login
	 * @param userInfo
	 * @return extsource login
	 */
	private String getExtSourceLogin(JsonNode userInfo) {
		List<String> ExtSourceLoginOptions = BeansUtils.getCoreConfig().getUserInfoEndpointExtSourceLogin();
		String login = "";
		for(String property: ExtSourceLoginOptions) {
			JsonNode loginNode = userInfo.path(property);
			if(loginNode.isArray()) {
				loginNode = loginNode.get(0);
			}
			if(loginNode.isTextual()) {
				login = loginNode.asText();
			}
			if(StringUtils.isNotEmpty(login)) {
				return login;
			}
		}
		if(StringUtils.isEmpty(login)) {
			log.info("user sub from user info endpoint was empty or null: {}", login);
		}
		return login;
	}

	/**
	 * Parsing extsource name from userinfo
	 * @param userInfo
	 * @return extsource name
	 */
	private String getExtSourceName(JsonNode userInfo) {
		String pathToExtSourceName = BeansUtils.getCoreConfig().getUserInfoEndpointExtSourceName();
		String extSourceName = userInfo.path(pathToExtSourceName).asText();
		if(StringUtils.isEmpty(extSourceName)) {
			log.info("issuer from user info endpoint was empty or null: {}", extSourceName);
		}
		return extSourceName;
	}

	/**
	 * Filling additional information with user's name, user's email and user-friendly format of extsource name
	 * @param userInfo
	 * @param additionalInformation
	 */
	private void fillAdditionalInformationWithDataFromUserInfo(JsonNode userInfo, Map<String, String> additionalInformation) {
		//retrieving name
		String name = userInfo.path("name").asText();
		if(StringUtils.isEmpty(name)) {
			String firstName = userInfo.path("given_name").asText();
			String familyName = userInfo.path("family_name").asText();
			if(StringUtils.isNotEmpty(firstName) && StringUtils.isNotEmpty(familyName)) {
				name =  firstName + " " + familyName;
			}
		}
		if(StringUtils.isNotEmpty(name)) {
			additionalInformation.put("displayName", name);
		}

		//retrieving email
		String email = userInfo.path("email").asText();
		if(StringUtils.isNotEmpty(email)) {
			additionalInformation.put("mail", email);
		}

		//retrieve friendly IdP name from the nested property
		List<String> pathToIdpName = BeansUtils.getCoreConfig().getUserInfoEndpointExtSourceFriendlyName();
		JsonNode node = userInfo;
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
		if(StringUtils.isNotEmpty(idpName)) {
			additionalInformation.put("sourceIdPName", idpName);
		}
	}

	/**
	 * Filling additional information with mfa timestamp if acr value is equal to MFA acr value
	 * @param userInfo
	 * @param additionalInformation
	 */
	private void fillMfaTimestamp(JsonNode userInfo, Map<String, String> additionalInformation) {
		String acrProperty = BeansUtils.getCoreConfig().getUserInfoEndpointAcrPropertyName();
		String acr = userInfo.path(acrProperty).asText();
		if (StringUtils.isNotEmpty(acr) && acr.equals(BeansUtils.getCoreConfig().getUserInfoEndpointMfaAcrValue())) {
			String mfaTimestampProperty = BeansUtils.getCoreConfig().getUserInfoEndpointMfaAuthTimestampPropertyName();
			String mfaTimestamp = userInfo.path(mfaTimestampProperty).asText();
			if (StringUtils.isNotEmpty(mfaTimestamp)) {
				additionalInformation.put(MFA_TIMESTAMP, mfaTimestamp);
			}
		}
	}
}
