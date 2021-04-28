package cz.metacentrum.perun.core.impl.modules.attributes;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.AttributesManager;
import cz.metacentrum.perun.core.api.Group;
import cz.metacentrum.perun.core.api.GroupsManager;
import cz.metacentrum.perun.core.api.exceptions.AttributeNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.ConsistencyErrorException;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeAssignmentException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeValueException;
import cz.metacentrum.perun.core.api.exceptions.WrongReferenceAttributeValueException;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;
import cz.metacentrum.perun.core.implApi.modules.attributes.GroupAttributesModuleAbstract;
import cz.metacentrum.perun.core.implApi.modules.attributes.GroupAttributesModuleImplApi;

/**
 * Synchronization enabled
 *
 * true if synchronization is enabled and attributes synchronizationInterval, groupMembersQuery and groupExtSource are all filled in
 * false if not
 * empty if there is no setting (means not synchronized)
 *
 * @author Michal Stava  stavamichal@gmail.com
 */
public class urn_perun_group_attribute_def_def_synchronizationEnabled extends GroupAttributesModuleAbstract implements GroupAttributesModuleImplApi {

	@Override
	public void checkAttributeSyntax(PerunSessionImpl sess, Group group, Attribute attribute) throws WrongAttributeValueException {
		//Null value is ok, means no settings for group
		if(attribute.getValue() == null) return;

		String attrValue = attribute.valueAsString();

		if(!attrValue.equals("true") && !attrValue.equals("false")) {
			throw new WrongAttributeValueException(attribute, group, "If attribute is not null, only string 'true' or 'false' is correct format.");
		}
	}

	@Override
	public void checkAttributeSemantics(PerunSessionImpl sess, Group group, Attribute attribute) throws WrongAttributeAssignmentException, WrongReferenceAttributeValueException {
		if (attribute.getValue() == null) return;
		try {
			if (attribute.valueAsString().equals("true")) {
				if (sess.getPerunBl().getGroupsManagerBl().isGroupForAutoRegistration(sess, group)) {
					throw new WrongReferenceAttributeValueException(attribute, "Synchronization cannot be enabled for groups in auto registration.");
				}

				Attribute requiredAttribute = sess.getPerunBl().getAttributesManagerBl().getAttribute(sess, group, GroupsManager.GROUPMEMBERSQUERY_ATTRNAME);
				if (requiredAttribute.getValue() == null) {
					throw new WrongReferenceAttributeValueException(attribute, requiredAttribute, group, null, group, null, requiredAttribute.toString() + " must be set in order to enable synchronization.");
				}

				requiredAttribute = sess.getPerunBl().getAttributesManagerBl().getAttribute(sess, group, GroupsManager.GROUPEXTSOURCE_ATTRNAME);
				if (requiredAttribute.getValue() == null) {
					throw new WrongReferenceAttributeValueException(attribute, requiredAttribute, group, null, group, null, requiredAttribute.toString() + " must be set in order to enable synchronization.");
				}
			}
		} catch (AttributeNotExistsException e) {
			throw new ConsistencyErrorException(e);
		}
	}

	@Override
	public AttributeDefinition getAttributeDefinition() {
		AttributeDefinition attr = new AttributeDefinition();
		attr.setNamespace(AttributesManager.NS_GROUP_ATTR_DEF);
		attr.setFriendlyName("synchronizationEnabled");
		attr.setDisplayName("Synchronization Enabled");
		attr.setType(String.class.getName());
		attr.setDescription("Enables group synchronization from external source.");
		return attr;
	}
}
