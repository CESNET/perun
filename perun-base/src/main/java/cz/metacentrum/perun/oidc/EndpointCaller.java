package cz.metacentrum.perun.oidc;

import com.fasterxml.jackson.databind.JsonNode;
import cz.metacentrum.perun.core.api.BeansUtils;
import cz.metacentrum.perun.core.api.exceptions.ExpiredTokenException;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;


public class EndpointCaller {
	private static final Logger log = LoggerFactory.getLogger(EndpointCaller.class);

	public static String INTROSPECTION_ENDPOINT = "introspection_endpoint";
	public static String USERINFO_ENDPOINT = "userinfo_endpoint";

	private final RestTemplate restTemplate = new RestTemplate();
	public final static String WELL_KNOWN_CONFIGURATION_PATH = "/.well-known/openid-configuration";

	public JsonNode callConfigurationEndpoint(String issuer) {
		JsonNode config = restTemplate.getForObject(issuer + WELL_KNOWN_CONFIGURATION_PATH, JsonNode.class);
		if (config == null) {
			log.error("No configuration retrieved from " + issuer + WELL_KNOWN_CONFIGURATION_PATH);
			throw new InternalErrorException("No configuration retrieved from " + issuer);
		}
		return config;
	}

	public JsonNode callEndpoint(String url) {
		JsonNode response = restTemplate.getForObject(url, JsonNode.class);
		if (response == null) {
			log.error("No data retrieved from " + url);
			throw new InternalErrorException("No configuration retrieved from " + url);
		}
		return response;
	}

	public JsonNode callIntrospectionEndpoint(String accessToken, String endpointUrl) throws ExpiredTokenException {
		HttpHeaders headers = prepareBasicAuthHeaders();

		MultiValueMap<String, String> requestBody = new LinkedMultiValueMap<>();
		requestBody.add("token", accessToken);

		HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(requestBody, headers);
		ResponseEntity<JsonNode> endpointResponse;

		try {
			endpointResponse = restTemplate.exchange(endpointUrl, HttpMethod.POST, entity, JsonNode.class);
		} catch (HttpClientErrorException ex) {
			if (ex.getStatusCode() == HttpStatus.FORBIDDEN) {
				log.error("Token {} is no longer valid when calling: {}", accessToken, endpointUrl);
				throw new ExpiredTokenException("Your token is no longer valid. Please retry from the start.");
			} else {
				log.error("Failed to get introspectionResponse for access token: {}. The response code was: {}", accessToken, ex.getStatusCode());
				throw new InternalErrorException("Failed to get introspectionResponse for access token: " + accessToken + ". The response was: " + ex.getResponseBodyAsString());
			}
		}

		return resolveResponse(endpointResponse);
	}

	public JsonNode callUserInfoEndpoint(String accessToken, String endpointUrl) throws ExpiredTokenException {

		HttpHeaders headers = new HttpHeaders();
		headers.setBearerAuth(accessToken);
		HttpEntity<Void> entity = new HttpEntity<>(headers);
		ResponseEntity<JsonNode> endpointResponse;

		try {
			endpointResponse = restTemplate.exchange(endpointUrl, HttpMethod.GET, entity, JsonNode.class);
		} catch (HttpClientErrorException ex) {
			if (ex.getStatusCode() == HttpStatus.FORBIDDEN) {
				log.error("Token {} is no longer valid when calling: {}", accessToken, endpointUrl);
				throw new ExpiredTokenException("Your token is no longer valid. Please retry from the start.");
			} else {
				log.error("Failed to get userInfoResponse for access token: {}. The response code was: {}", accessToken, ex.getStatusCode());
				throw new InternalErrorException("Failed to get userInfoResponse for access token: " + accessToken + ". The response was: " + ex.getResponseBodyAsString());
			}
		}

		return resolveResponse(endpointResponse);
	}

	private static JsonNode resolveResponse(ResponseEntity<JsonNode> endpointResponse) {
		JsonNode userInfo = endpointResponse.getBody();
		if (userInfo == null) {
			log.error("Response from endpoint was null.");
			throw new InternalErrorException("Could not retrieve user data from endpoint.");
		}
		if (StringUtils.isNotEmpty(userInfo.path("error").asText())) {
			log.error("Call to endpoint failed, the error is: {}", userInfo);
			throw new InternalErrorException("Call to endpoint failed, the error is" + userInfo);
		}
		log.debug("Endpoint call - user info retrieved: {}", userInfo);
		return userInfo;
	}


	/** Returns clientId and clientSecret as basicAuth headers */
	private HttpHeaders prepareBasicAuthHeaders() {
		HttpHeaders headers = new HttpHeaders();
		String clientId = BeansUtils.getCoreConfig().getOidcClientId();
		String clientSecret = BeansUtils.getCoreConfig().getOidcClientSecret();
		headers.setBasicAuth(clientId, clientSecret);
		return headers;
	}
}
