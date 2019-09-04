package cz.metacentrum.perun.core.impl.modules.attributes;

import cz.metacentrum.perun.core.api.*;
import cz.metacentrum.perun.core.api.exceptions.*;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;
import cz.metacentrum.perun.core.implApi.modules.attributes.GroupAttributesModuleAbstract;
import cz.metacentrum.perun.core.implApi.modules.attributes.GroupAttributesModuleImplApi;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Pavel Vyskocil <vyskocilpavel@muni.cz>
 * @author Peter Balcirak <peter.balcirak@gmail.com>
 */
public class urn_perun_group_attribute_def_def_groupSynchronizationTimes extends GroupAttributesModuleAbstract implements GroupAttributesModuleImplApi {

	private static final Pattern pattern = Pattern.compile("^(([0-1][0-9])|(2[0-3])):[0-5][0,5]$");

	@Override
	public void checkAttributeSyntax(PerunSessionImpl sess, Group group, Attribute attribute) throws InternalErrorException, WrongAttributeValueException {
		//Null value is ok, means no settings for group
		if(attribute.getValue() == null) return;

		ArrayList<String> attrValues = attribute.valueAsList();

		for (String attrValue : attrValues) {
			Matcher matcher = pattern.matcher(attrValue);
			if (!matcher.matches()) {
				throw new WrongAttributeValueException(attribute, group, "Some of values are not in format HH:MM or are not rounded to 5 minute. For example 08:50 or 20:55.");
			}
		}
	}

	@Override
	public void checkAttributeSemantics(PerunSessionImpl sess, Group group, Attribute attribute) throws WrongAttributeAssignmentException, InternalErrorException, WrongReferenceAttributeValueException {
		if (attribute.getValue() == null) return;
		try {
			Attribute foundAttribute = sess.getPerunBl().getAttributesManagerBl().getAttribute(sess, group, GroupsManager.GROUPSYNCHROINTERVAL_ATTRNAME);
			if(foundAttribute.getValue() != null) {
				throw new WrongReferenceAttributeValueException(attribute, foundAttribute, group, null, group, null, "Attribute " + attribute.getName() + " cannot be set because attribute " + GroupsManager.GROUPSYNCHROINTERVAL_ATTRNAME + " is already set.");
			}
		} catch (AttributeNotExistsException exc) {
			throw new ConsistencyErrorException("Attribute " + GroupsManager.GROUPSYNCHROINTERVAL_ATTRNAME + " is supposed to exist", exc);
		}
	}


	@Override
	public List<String> getDependencies() {
		List<String> dependencies = new ArrayList<>();
		dependencies.add(GroupsManager.GROUPSYNCHROINTERVAL_ATTRNAME);
		return dependencies;
	}

	@Override
	public AttributeDefinition getAttributeDefinition() {
		AttributeDefinition attr = new AttributeDefinition();
		attr.setNamespace(AttributesManager.NS_GROUP_ATTR_DEF);
		attr.setFriendlyName("groupSynchronizationTimes");
		attr.setDisplayName("Group synchronization times");
		attr.setType(ArrayList.class.getName());
		attr.setDescription("List of time values for group synchronization in format HH:MM rounded to 5 minute. For example 08:50 or 20:55");
		return attr;
	}
}