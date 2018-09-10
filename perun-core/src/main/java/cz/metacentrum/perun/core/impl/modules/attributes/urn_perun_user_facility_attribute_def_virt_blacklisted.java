package cz.metacentrum.perun.core.impl.modules.attributes;

import cz.metacentrum.perun.core.api.*;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;
import cz.metacentrum.perun.core.implApi.modules.attributes.FacilityUserVirtualAttributesModuleAbstract;

import java.util.List;

/**
 * Get boolean value. TRUE if user is blacklisted by one of the security teams which are added to facilty.
 *
 * @author Ondrej Velisek <ondrejvelisek@gmail.com>
 */
public class urn_perun_user_facility_attribute_def_virt_blacklisted extends FacilityUserVirtualAttributesModuleAbstract {
	@Override
	public Attribute getAttributeValue(PerunSessionImpl sess, Facility facility, User user, AttributeDefinition attributeDefinition) throws InternalErrorException {
		Attribute attribute = new Attribute(attributeDefinition);

		List<SecurityTeam> securityTeams = sess.getPerunBl().getFacilitiesManagerBl().getAssignedSecurityTeams(sess, facility);

		for (SecurityTeam st : securityTeams) {
			if (sess.getPerunBl().getSecurityTeamsManagerBl().isUserBlacklisted(sess, st, user)) {
				attribute.setValue(true);
				return attribute;
			}
		}

		attribute.setValue(null);
		return attribute;
	}

	@Override
	public AttributeDefinition getAttributeDefinition() {
		AttributeDefinition attr = new AttributeDefinition();
		attr.setNamespace(AttributesManager.NS_USER_ATTR_VIRT);
		attr.setFriendlyName("blacklisted");
		attr.setDisplayName("Blacklisted");
		attr.setType(Boolean.class.getName());
		attr.setDescription("Flag which means if user is banned by Security team");
		return attr;
	}
}
