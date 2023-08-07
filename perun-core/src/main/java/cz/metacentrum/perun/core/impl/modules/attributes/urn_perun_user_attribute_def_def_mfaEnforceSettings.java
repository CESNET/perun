package cz.metacentrum.perun.core.impl.modules.attributes;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeAssignmentException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeValueException;
import cz.metacentrum.perun.core.api.exceptions.WrongReferenceAttributeValueException;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;
import cz.metacentrum.perun.core.implApi.modules.attributes.UserAttributesModuleAbstract;
import cz.metacentrum.perun.core.implApi.modules.attributes.UserAttributesModuleImplApi;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;


/**
 * Check if value of mfaEnforceSetting attribute is valid
 */
public class urn_perun_user_attribute_def_def_mfaEnforceSettings extends UserAttributesModuleAbstract implements UserAttributesModuleImplApi {


	/**
	 * Attribute value should be a valid JSON.
	 * These specific values are allowed:
	 * empty string or null
	 * {"all":true}
	 * {"include_categories":["str1","str2"]}
	 * {"include_categories":["str1","str2"],"exclude_rps":["rp1","rp2"]}
	 *
	 * @param perunSession PerunSession
	 * @param user User
	 * @param attribute Attribute of the user.
	 *
	 * @throws WrongAttributeValueException
	 */
	@Override
	public void checkAttributeSyntax(PerunSessionImpl perunSession, User user, Attribute attribute) throws WrongAttributeValueException {
		String val = attribute.valueAsString();

		// Null or empty string are allowed
		if (val == null || val.isEmpty()) return;

		// Should be string in valid JSON format
		try {
			final ObjectMapper mapper = new ObjectMapper();
			JsonNode root = mapper.readTree(val);

			// Check "all"
			if (root.has("all") && root.size() == 1) return;

			int validNodes = 0;
			// Check "include_categories"
			if (root.has("include_categories")) {
				isStringArrayNode(root.get("include_categories"), "include_categories");
				validNodes += 1;

				// Check "exclude_rps"
				if (root.has("exclude_rps")) {
					isStringArrayNode(root.get("exclude_rps"), "exclude_rps");
					validNodes += 1;
				}
			}

			// Check for NO additional nodes
			if (root.size() == validNodes) return;

			throw new WrongAttributeValueException(
				"Attribute value " + val + " has incorrect format." +
				" Allowed values are:" +
				" empty string or null," +
				" {\"all\":true}," +
				" {\"include_categories\":[\"str1\",\"str2\"]}," +
				" {\"include_categories\":[\"str1\",\"str2\"],\"exclude_rps\":[\"rp1\",\"rp2\"]}");
		} catch (JsonProcessingException e) {
			throw new WrongAttributeValueException("Attribute value " + val + " is not a valid JSON.");
		}
	}

	/**
	 * Checks that node is an array and all values are strings
	 *
	 * @param node JsonNode
	 * @throws WrongAttributeValueException
	 */
	private void isStringArrayNode(JsonNode node, String name) throws WrongAttributeValueException {
		// Check property is valid array
		if (node.isArray()) {
			// Check all items of array are string like
			for (Iterator<JsonNode> it = node.elements(); it.hasNext(); ) {
				JsonNode value = it.next();
				if (!value.isTextual()) {
					throw new WrongAttributeValueException("Property '" + name + "' has non textual value " + value);
				}
			}
		} else {
			throw new WrongAttributeValueException("Property '" + name + "' is not an array.");
		}
	}
	
	/**
	 * The following restrictions are placed on the attribute value:
	 * {"include_categories":["str1","str2"]} str1, str2 is an existing key in the entityless attribute mfaCategories
	 * {"include_categories":["str1","str2"],"exclude_rps":["rp1","rp2"]} str1, str2 is an existing key in the entityless attribute mfaCategories and rp1, rp2 must exist inside the category
	 *
	 * @param perunSession PerunSession
	 * @param user User
	 * @param attribute Attribute of the user.
	 *
	 * @throws WrongReferenceAttributeValueException
	 * @throws WrongAttributeAssignmentException
	 */
	@Override
	public void checkAttributeSemantics(PerunSessionImpl perunSession, User user, Attribute attribute) throws WrongReferenceAttributeValueException, WrongAttributeAssignmentException {
		String val = attribute.valueAsString();
		if (val == null || val.isEmpty()) return;

		Set<String> includeCategories = null;
		Set<String> excludeRps = null;
		try {
			final ObjectMapper mapper = new ObjectMapper();
			JsonNode root = mapper.readTree(val);

			if (root.has("all")) {
				if (root.get("all").asBoolean()) return;
				throw new WrongAttributeAssignmentException("Property 'all' is only valid with true ({\"all\":true}).");
			}

			// Initialize included categories to check
			includeCategories = new HashSet<>();
			fillSet(root.get("include_categories"), includeCategories);

			// Initialize excluded rps to check (optional)
			excludeRps = new HashSet<>();
			if (root.has("exclude_rps")) {
				fillSet(root.get("exclude_rps"), excludeRps);
			}
		} catch (JsonProcessingException e) {
			throw new WrongAttributeAssignmentException("Attribute " + attribute + "is incorrectly assigned.");
		}

		Attribute mfaCategories = perunSession.getPerunBl().getAttributesManagerBl().getEntitylessAttributes(perunSession, "mfaCategories").get(0);
		String mfaSettingsValue = mfaCategories.valueAsString();
		try {
			final ObjectMapper mapper = new ObjectMapper();
			JsonNode mfaCategoriesNode = mapper.readTree(mfaSettingsValue).get("categories");

			// Iterate through categories and check that all included categories exist
			for (Iterator<Map.Entry<String, JsonNode>> catIt = mfaCategoriesNode.fields(); catIt.hasNext(); ) {
				Map.Entry<String, JsonNode> catEntry = catIt.next();

				boolean checkRps = includeCategories.remove(catEntry.getKey());
				if (checkRps) {
					// Iterate through rps and check that all excluded rps exist
					JsonNode rps = catEntry.getValue().get("rps");
					for (Iterator<Map.Entry<String, JsonNode>> rpsIt = rps.fields(); rpsIt.hasNext(); ) {
						Map.Entry<String, JsonNode> rp = rpsIt.next();
						excludeRps.remove(rp.getKey());
					}
				}
			}

			// Both sets should be empty
			if (!includeCategories.isEmpty()) {
				throw new WrongReferenceAttributeValueException("Categories " + includeCategories + " do not exist inside mfaCategories attribute.");
			}
			if (!excludeRps.isEmpty()) {
				throw new WrongReferenceAttributeValueException("Rps " + excludeRps + " do not exist inside included categories in mfaCategories attribute.");
			}
		} catch (JsonProcessingException e) {
			throw new WrongAttributeAssignmentException("Attribute " + mfaCategories + "is incorrectly assigned.");
		}
	}

	/**
	 * Add all elements of node to a set
	 *
	 * @param node JsonNode
	 * @param set HashSet
	 */
	private void fillSet(JsonNode node, Set<String> set) {
		for (Iterator<JsonNode> it = node.elements(); it.hasNext(); ) {
			JsonNode next = it.next();
			set.add(next.textValue());
		}
	}
}
