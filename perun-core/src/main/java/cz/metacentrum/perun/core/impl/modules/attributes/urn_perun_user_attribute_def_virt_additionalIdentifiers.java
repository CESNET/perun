package cz.metacentrum.perun.core.impl.modules.attributes;

import cz.metacentrum.perun.audit.events.AttributesManagerEvents.AllAttributesRemovedForUserExtSource;
import cz.metacentrum.perun.audit.events.AttributesManagerEvents.AttributeRemovedForUes;
import cz.metacentrum.perun.audit.events.AttributesManagerEvents.AttributeSetForUes;
import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;
import cz.metacentrum.perun.core.implApi.modules.attributes.SkipValueCheckDuringDependencyCheck;
import cz.metacentrum.perun.core.implApi.modules.attributes.UserVirtualAttributeCollectedFromUserExtSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * All identifiers collected from:
 *  - UserExtSource attribute additionalIdentifiers
 *
 * @author Michal Stava <Michal.Stava@cesnet.cz>
 */
@SuppressWarnings("unused")
@SkipValueCheckDuringDependencyCheck
public class urn_perun_user_attribute_def_virt_additionalIdentifiers extends UserVirtualAttributeCollectedFromUserExtSource {

	private final Logger log = LoggerFactory.getLogger(this.getClass());

	@Override
	public String getSourceAttributeFriendlyName() {
		return "additionalIdentifiers";
	}

	@Override
	public String getDestinationAttributeFriendlyName() {
		return "additionalIdentifiers";
	}

	@Override
	public Attribute getAttributeValue(PerunSessionImpl sess, User user, AttributeDefinition destinationAttributeDefinition) {
		//get already filled value obtained from UserExtSources
		Attribute attribute = super.getAttributeValue(sess, user, destinationAttributeDefinition);

		Attribute destinationAttribute = new Attribute(destinationAttributeDefinition);
		//get values previously obtained and add them to Set representing final value
		//for values use set because of avoiding duplicities
		Set<String> valuesWithoutDuplicities = new HashSet<>(attribute.valueAsList());

		//convert set to list (values in list will be without duplicities)
		destinationAttribute.setValue(new ArrayList<>(valuesWithoutDuplicities));
		return destinationAttribute;
	}

	@Override
	public List<AttributeHandleIdentifier> getHandleIdentifiers() {
		List<AttributeHandleIdentifier> handleIdentifiers = super.getHandleIdentifiers();
		handleIdentifiers.add(auditEvent -> {
			if (auditEvent instanceof AllAttributesRemovedForUserExtSource) {
				return ((AllAttributesRemovedForUserExtSource) auditEvent).getUserExtSource().getId();
			} else {
				return null;
			}
		});
		handleIdentifiers.add(auditEvent -> {
			if (auditEvent instanceof AttributeSetForUes && ((AttributeSetForUes) auditEvent).getAttribute().getFriendlyName().equals(getSourceAttributeFriendlyName())) {
				return ((AttributeSetForUes) auditEvent).getUes().getId();
			} else {
				return null;
			}
		});
		handleIdentifiers.add(auditEvent -> {
			if (auditEvent instanceof AttributeRemovedForUes && ((AttributeRemovedForUes) auditEvent).getAttribute().getFriendlyName().equals(getSourceAttributeFriendlyName())) {
				return ((AttributeRemovedForUes) auditEvent).getUes().getId();
			} else {
				return null;
			}
		});
		return handleIdentifiers;
	}
}
