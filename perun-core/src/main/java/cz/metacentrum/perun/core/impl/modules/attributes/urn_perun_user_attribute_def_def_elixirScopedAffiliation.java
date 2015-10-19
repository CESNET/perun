package cz.metacentrum.perun.core.impl.modules.attributes;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.AttributesManager;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.exceptions.*;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;
import cz.metacentrum.perun.core.implApi.modules.attributes.UserAttributesModuleAbstract;
import cz.metacentrum.perun.core.implApi.modules.attributes.UserAttributesModuleImplApi;

import java.util.List;

/**
 * Attribute represents list of scoped affiliations.
 * Value must be {member,affiliate,faculty}@DNS_name
 *
 * @author Pavel Zlamal <zlamal@cesnet.cz>
 */
public class urn_perun_user_attribute_def_def_elixirScopedAffiliation extends UserAttributesModuleAbstract implements UserAttributesModuleImplApi {

	private static final String REGEX = "^[member|affiliate|faculty]+@[-A-Za-z0-9]+(\\.[-A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";

	@Override
	public void checkAttributeValue(PerunSessionImpl perunSession, User user, Attribute attribute) throws InternalErrorException, WrongAttributeValueException, WrongReferenceAttributeValueException, WrongAttributeAssignmentException {

		List<String> values = ((List<String>)attribute.getValue());
		if (values != null && !values.isEmpty()) {
			for (String value : values) {
				// check each value
				if(!value.matches(REGEX)) throw new WrongAttributeValueException(attribute, "Wrong format. List of \"[member|affiliate|faculty]@scope\" expected.");
			}
		}

	}

	@Override
	public AttributeDefinition getAttributeDefinition() {
		AttributeDefinition attr = new AttributeDefinition();
		attr.setNamespace(AttributesManager.NS_USER_ATTR_DEF);
		attr.setFriendlyName("elixirScopedAffiliation");
		attr.setDisplayName("Elixir Scoped Affiliation");
		attr.setType(List.class.getName());
		attr.setDescription("List of users affiliations with scope. Like: [member|affiliate|faculty]@scope");
		return attr;
	}

}
