package cz.metacentrum.perun.core.impl.modules.attributes;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.AttributesManager;
import cz.metacentrum.perun.core.api.Group;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeAssignmentException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeValueException;
import cz.metacentrum.perun.core.api.exceptions.WrongReferenceAttributeValueException;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;
import cz.metacentrum.perun.core.implApi.modules.attributes.GroupAttributesModuleAbstract;
import cz.metacentrum.perun.core.implApi.modules.attributes.GroupAttributesModuleImplApi;

import java.util.ArrayList;

/**
 * @author Pavel Vyskocil <vyskocilpavel@muni.cz>
 */
public class urn_perun_group_attribute_def_def_synchronizationTimes extends GroupAttributesModuleAbstract implements GroupAttributesModuleImplApi {

	@Override
	public void checkAttributeValue(PerunSessionImpl sess, Group group, Attribute attribute) throws InternalErrorException, WrongAttributeValueException, WrongReferenceAttributeValueException, WrongAttributeAssignmentException {
		String pattern = "^(([0-1][0-9])|(2[0-3])):[0-5][0,5]$";
		//Null value is ok, means no settings for group
		if(attribute.getValue() == null) return;

		ArrayList<String> attrValues = (ArrayList<String>) attribute.getValue();

		for (String attrValue : attrValues) {
			if (!attrValue.matches(pattern)) {
				throw new WrongAttributeValueException(attribute, group, "Some of values are not in format HH:MM or are not rounded to 5 minute. For example 20:50 or 20:55.");
			}
		}
	}

	@Override
	public AttributeDefinition getAttributeDefinition() {
		AttributeDefinition attr = new AttributeDefinition();
		attr.setNamespace(AttributesManager.NS_GROUP_ATTR_DEF);
		attr.setFriendlyName("synchronizationTimes");
		attr.setDisplayName("synchronizationTimes");
		attr.setType(String.class.getName());
		attr.setDescription("List of time values for synchronization in format HH:MM rounded to 5 minute. For example 20:50 or 20:55");
		return attr;
	}
}
