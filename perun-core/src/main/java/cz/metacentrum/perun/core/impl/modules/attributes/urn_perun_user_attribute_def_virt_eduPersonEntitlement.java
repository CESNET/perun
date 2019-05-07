package cz.metacentrum.perun.core.impl.modules.attributes;

import cz.metacentrum.perun.audit.events.AttributesManagerEvents.AllAttributesRemovedForUser;
import cz.metacentrum.perun.audit.events.AttributesManagerEvents.AttributeRemovedForUser;
import cz.metacentrum.perun.audit.events.AttributesManagerEvents.AttributeSetForUser;
import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.AttributesManager;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.exceptions.AttributeNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeAssignmentException;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;
import cz.metacentrum.perun.core.implApi.modules.attributes.UserVirtualAttributeCollectedFromUserExtSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * All entitlements collected from:
 *  - UserExtSources attributes
 *  - urn:perun:user:attribute-def:virt:groupNames
 *
 * @author Dominik Frantisek Bucik <bucik@ics.muni.cz>
 */
@SuppressWarnings("unused")
public class urn_perun_user_attribute_def_virt_eduPersonEntitlement extends UserVirtualAttributeCollectedFromUserExtSource {

	private final Logger log = LoggerFactory.getLogger(this.getClass());

	@Override
	public String getSourceAttributeFriendlyName() {
		return "entitlement";
	}

	/**
	 * Get friendly name of secondary source attribute
	 * @return friendly name of secondary source attribute
	 */
	String getSecondarySourceAttributeFriendlyName() {
		return "groupNames";
	}

	/**
	 * Get name of secondary source attribute
	 * @return name of secondary source attribute
	 */
	String getSecondarySourceAttributeName() {
		return AttributesManager.NS_USER_ATTR_VIRT + ":" + getSecondarySourceAttributeFriendlyName();
	}

	@Override
	public String getDestinationAttributeFriendlyName() {
		return "eduPersonEntitlement";
	}

	@Override
	public Attribute getAttributeValue(PerunSessionImpl sess, User user, AttributeDefinition destinationAttributeDefinition) throws InternalErrorException {
		//get already filled value obtained from UserExtSources
		Attribute attribute = super.getAttributeValue(sess, user, destinationAttributeDefinition);

		Attribute destinationAttribute = new Attribute(destinationAttributeDefinition);
		//get values previously obtained and add them to Set representing final value
		//for values use set because of avoiding duplicities
		Set<String> valuesWithoutDuplicities = new HashSet<>(attribute.valueAsList());
		valuesWithoutDuplicities.addAll(getEntitlementFromGroups(sess, user));

		//convert set to list (values in list will be without duplicities)
		destinationAttribute.setValue(new ArrayList<>(valuesWithoutDuplicities));
		return destinationAttribute;
	}

	/**
	 * Get entitlements represented originally as groupNames.
	 * @param sess Perun session
	 * @param user user for whom the entitlements are being collected
	 * @return Set of values
	 * @throws InternalErrorException when attribute has wrong assignment
	 */
	private Set<String> getEntitlementFromGroups(PerunSessionImpl sess, User user) throws InternalErrorException {
		Set<String> result = new HashSet<>();

		try {
			Attribute groupNames = sess.getPerunBl().getAttributesManagerBl()
					.getAttribute(sess, user, getSecondarySourceAttributeName());

			if (groupNames != null && groupNames.getValue() != null && groupNames.valueAsList() != null) {
				result.addAll(groupNames.valueAsList());
			}
		} catch (WrongAttributeAssignmentException e) {
			throw new InternalErrorException("Wrong assignment of " + getSecondarySourceAttributeFriendlyName() + " for user " + user.getId(), e);
		} catch (AttributeNotExistsException e) {
			log.debug("Attribute {} of user {} does not exist, values will be skipped",
					getSecondarySourceAttributeFriendlyName(), user.getId());
		}

		return result;
	}

	@Override
	public List<AttributeHandleIdentifier> getHandleIdentifiers() {
		List<AttributeHandleIdentifier> handleIdentifiers = super.getHandleIdentifiers();
		handleIdentifiers.add(auditEvent -> {
			if (auditEvent instanceof AllAttributesRemovedForUser) {
				return ((AllAttributesRemovedForUser) auditEvent).getUser().getId();
			} else {
				return null;
			}
		});
		handleIdentifiers.add(auditEvent -> {
			if (auditEvent instanceof AttributeSetForUser && ((AttributeSetForUser) auditEvent).getAttribute().getFriendlyName().equals(getSecondarySourceAttributeFriendlyName())) {
				return ((AttributeSetForUser) auditEvent).getUser().getId();
			} else {
				return null;
			}
		});
		handleIdentifiers.add(auditEvent -> {
			if (auditEvent instanceof AttributeRemovedForUser && ((AttributeRemovedForUser) auditEvent).getAttribute().getFriendlyName().equals(getSecondarySourceAttributeFriendlyName())) {
				return ((AttributeRemovedForUser) auditEvent).getUser().getId();
			} else {
				return null;
			}
		});
		return handleIdentifiers;
	}
}
