package cz.metacentrum.perun.core.impl.modules.attributes;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.AttributesManager;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeAssignmentException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeValueException;
import cz.metacentrum.perun.core.api.exceptions.WrongReferenceAttributeValueException;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;
import cz.metacentrum.perun.core.implApi.modules.attributes.UserAttributesModuleAbstract;
import cz.metacentrum.perun.core.implApi.modules.attributes.UserAttributesModuleImplApi;

import java.util.ArrayList;
import java.util.List;

/**
 * Attribute represents IP Addresses in ArrayList.
 *
 * Value must meet the proper format of IPv4 or IPv6.
 *
 * @author Metodej Klang <metodej.klang@gmail.com>
 */
public class urn_perun_user_attribute_def_def_IPAddresses extends UserAttributesModuleAbstract implements UserAttributesModuleImplApi {

	private static final String IPv4_PATTERN = "^(25[0-5]|2[0-4]\\d|[0-1]?\\d?\\d)(\\.(25[0-5]|2[0-4]\\d|[0-1]?\\d?\\d)){3}$";
	private static final String IPv6_PATTERN = "^(?:[0-9a-fA-F]{1,4}:){7}[0-9a-fA-F]{1,4}$";
	private static final String IPv6_PATTERN_SHORT = "^((?:[0-9A-Fa-f]{1,4}(?::[0-9A-Fa-f]{1,4})*)?)::((?:[0-9A-Fa-f]{1,4}(?::[0-9A-Fa-f]{1,4})*)?)$";

	@Override
	public void checkAttributeValue(PerunSessionImpl perunSession, User user, Attribute attribute) throws InternalErrorException, WrongAttributeValueException, WrongAttributeAssignmentException, WrongReferenceAttributeValueException {
		List<String> value = attribute.valueAsList();
		for (String address : value) {
			if (!address.matches(IPv4_PATTERN) && !address.matches(IPv6_PATTERN) && !address.matches(IPv6_PATTERN_SHORT))
				throw new WrongAttributeValueException(attribute, "IP address is not in the correct format.");
		}
	}

	@Override
	public AttributeDefinition getAttributeDefinition() {
		AttributeDefinition attr = new AttributeDefinition();
		attr.setNamespace(AttributesManager.NS_USER_ATTR_DEF);
		attr.setFriendlyName("IPAddresses");
		attr.setDisplayName("IP Addresses");
		attr.setType(ArrayList.class.getName());
		attr.setDescription("List of IP Addresses.");
		return attr;
	}
}
