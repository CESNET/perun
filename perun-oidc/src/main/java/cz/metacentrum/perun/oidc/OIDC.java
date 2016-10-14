package cz.metacentrum.perun.oidc;

import cz.metacentrum.perun.core.api.BeansUtils;
import cz.metacentrum.perun.core.api.PerunClient;
import cz.metacentrum.perun.core.api.PerunSession;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.exceptions.AttributeNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.PrivilegeException;
import cz.metacentrum.perun.core.api.exceptions.RpcException;
import cz.metacentrum.perun.core.api.exceptions.UserNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeAssignmentException;
import cz.metacentrum.perun.rpc.deserializer.Deserializer;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.NullNode;
import org.codehaus.jackson.node.ObjectNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * @author Ondrej Velisek <ondrejvelisek@gmail.com>
 */
public class OIDC {

	private final static Logger log = LoggerFactory.getLogger(OIDC.class);

	private final String USERINFO_METHOD = "userinfo";
	private final String USERINFO_FOR_USER_METHOD = "getSpecificUserinfo";
	private final String CONFIG_FILE = "perun-oidc-scopes.properties";

	// if config value matches this regex it is consider as perun attribute and is replaced by real value.
	private Pattern attrRegex = Pattern.compile("^urn:perun:.*");


	public Object process(PerunSession sess, String method, Deserializer params) throws InternalErrorException, WrongAttributeAssignmentException, UserNotExistsException, PrivilegeException {

		if (USERINFO_METHOD.equals(method)) {

			User user = sess.getPerunPrincipal().getUser();
			Map<String, String> properties = BeansUtils.getAllPropertiesFromCustomConfiguration(CONFIG_FILE);

			if (sess.getPerunClient().getScopes().contains(PerunClient.SCOPE_ALL)) {

				return getUserinfo(sess, user, properties.keySet(), properties);

			} else {

				return getUserinfo(sess, user, sess.getPerunClient().getScopes(), properties);

			}

		} else if (USERINFO_FOR_USER_METHOD.equals(method)) {

			User user = sess.getPerun().getUsersManager().getUserById(sess, params.readInt("user"));
			Map<String, String> properties = BeansUtils.getAllPropertiesFromCustomConfiguration(CONFIG_FILE);

			if (sess.getPerunClient().getScopes().contains(PerunClient.SCOPE_ALL)) {

				return getUserinfo(sess, user, properties.keySet(), properties);

			} else {

				return getUserinfo(sess, user, sess.getPerunClient().getScopes(), properties);

			}

		} else {
			throw new RpcException(RpcException.Type.UNKNOWN_METHOD, "No method "+method+" was found. Try /"+USERINFO_METHOD+" instead.");
		}

	}


	private ObjectNode getUserinfo(final PerunSession sess, final User user, Collection<String> allowedScopes, Map<String, String> properties)
			throws InternalErrorException, PrivilegeException, UserNotExistsException, WrongAttributeAssignmentException {

		ObjectMapper mapper = new ObjectMapper();
		ObjectNode userinfo = mapper.createObjectNode();

		for (final String scope : allowedScopes) {

			String property = properties.get(scope);
			if (property == null || property.isEmpty()) {
				log.info("No values are mapped to scope "+scope+". Configure them in "+ CONFIG_FILE);
				continue;
			}
			try {
				JsonNode claims = mapper.readTree(property);

				if (!claims.isObject()) {
					throw new InternalErrorException("Config file "+ CONFIG_FILE +" is wrongly configured. Values have to be valid JSON objects.");
				}

				claims = replaceAttrsInJsonTree(sess, user, claims);
				if (claims != null && claims.isObject()) {
					userinfo.putAll((ObjectNode) claims);
				}

			} catch (IOException e) {
				throw new InternalErrorException("Config file "+ CONFIG_FILE +" is wrongly configured. Values have to be valid JSON objects.", e);
			}

		}

		return userinfo;

	}


	private JsonNode replaceAttrsInJsonTree(PerunSession sess, User user, JsonNode node) throws InternalErrorException, WrongAttributeAssignmentException, PrivilegeException, UserNotExistsException {

		ObjectMapper mapper = new ObjectMapper();
		if (!node.isObject()) {

			if (node.isTextual()) {
				Matcher attrMatcher = attrRegex.matcher(node.getTextValue());
				if (attrMatcher.matches()) {

					try {
						Object value = sess.getPerun().getAttributesManager().getAttribute(sess, user, node.getTextValue()).getValue();
						if (value != null) {
							return mapper.convertValue(value, JsonNode.class);
						} else {
							log.info("Attribute "+node.getTextValue()+" is not present for user "+user);
							return NullNode.getInstance();
						}
					} catch (AttributeNotExistsException e) {
						log.warn("Attribute "+node.getTextValue()+" does not exists");
						return null;
					}

				}
			}
			return node;

		} else {
			ObjectNode object = (ObjectNode) node;
			ObjectNode userinfo = mapper.createObjectNode();

			Iterator<Map.Entry<String, JsonNode>> fieldsIterator = object.getFields();
			while (fieldsIterator.hasNext()) {
				Map.Entry<String, JsonNode> child = fieldsIterator.next();
				JsonNode result = replaceAttrsInJsonTree(sess, user, child.getValue());
				if (result != null) {
					userinfo.put(child.getKey(), result);
				}
			}

			if (userinfo.size() == 0) {
				return null;
			} else {
				return userinfo;
			}

		}

	}
}
