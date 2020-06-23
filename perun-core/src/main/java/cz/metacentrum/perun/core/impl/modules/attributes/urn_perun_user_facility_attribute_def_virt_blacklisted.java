package cz.metacentrum.perun.core.impl.modules.attributes;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.AttributesManager;
import cz.metacentrum.perun.core.api.Facility;
import cz.metacentrum.perun.core.api.SecurityTeam;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;
import cz.metacentrum.perun.core.implApi.modules.attributes.SkipValueCheckDuringDependencyCheck;
import cz.metacentrum.perun.core.implApi.modules.attributes.UserFacilityVirtualAttributesModuleAbstract;

import java.util.List;

/**
 * Get boolean value. TRUE if user is blacklisted by one of the security teams which are added to facilty.
 *
 * @author Ondrej Velisek <ondrejvelisek@gmail.com>
 */
@SkipValueCheckDuringDependencyCheck
public class urn_perun_user_facility_attribute_def_virt_blacklisted extends UserFacilityVirtualAttributesModuleAbstract {
	@Override
	public Attribute getAttributeValue(PerunSessionImpl sess, User user, Facility facility, AttributeDefinition attributeDefinition) {
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
