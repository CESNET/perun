package cz.metacentrum.perun.core.impl.modules.attributes;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.AttributesManager;
import cz.metacentrum.perun.core.api.Group;
import cz.metacentrum.perun.core.api.Resource;
import cz.metacentrum.perun.core.api.exceptions.AttributeNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.ConsistencyErrorException;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeAssignmentException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeValueException;
import cz.metacentrum.perun.core.api.exceptions.WrongReferenceAttributeValueException;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;
import cz.metacentrum.perun.core.implApi.modules.attributes.GroupResourceAttributesModuleAbstract;
import cz.metacentrum.perun.core.implApi.modules.attributes.GroupResourceAttributesModuleImplApi;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * AD Name module
 * Name in AD for group and resource need to be unique in between
 * other AD name where assigned resource has OU Name attribute with same value
 * For example: If the group1 will be assigned to the resource1 and the group2
 * will be assigned to the resource2, on both resources will be attribute OU Name
 * set to value 'SPECIFIC_OU', then value of attribute defined by this module need
 * to be different (unique) for both groups (can't be same). If OU of one of these
 * resources will be different, then both groups can't have the same name.
 *
 * @author Michal Stava  stavamichal@gmail.com
 */
public class urn_perun_group_resource_attribute_def_def_adName extends GroupResourceAttributesModuleAbstract implements GroupResourceAttributesModuleImplApi {

	private static final String A_R_D_AD_OU_NAME = AttributesManager.NS_RESOURCE_ATTR_DEF + ":adOuName";

	private static final Pattern pattern = Pattern.compile("(\\w|-|\\.)*");

	@Override
	public void checkAttributeSyntax(PerunSessionImpl sess, Group group, Resource resource, Attribute attribute) throws WrongAttributeValueException {
		//Attribute can be null
		if (attribute.getValue() == null) return;

		if (!pattern.matcher(attribute.valueAsString()).matches()) {
			throw new WrongAttributeValueException(attribute, "Invalid attribute adName value. It should contain only letters, digits, hyphens, underscores or dots.");
		}
	}

	@Override
	public void checkAttributeSemantics(PerunSessionImpl sess, Group group, Resource resource, Attribute attribute) throws WrongReferenceAttributeValueException, WrongAttributeAssignmentException {
		//Attribute can be null
		if(attribute.getValue() == null) {
			return;
		}

		Attribute resourceAdOuName;
		try {
			resourceAdOuName = sess.getPerunBl().getAttributesManagerBl().getAttribute(sess, resource, A_R_D_AD_OU_NAME);
		} catch (AttributeNotExistsException ex) {
			throw new ConsistencyErrorException(ex);
		}

		//if assigned
		if(resourceAdOuName.getValue() == null) {
			return;
		}

		List<Group> groupsWithSameADNameInSameOu = sess.getPerunBl().getSearcherBl().getGroupsByGroupResourceSetting(sess, attribute, resourceAdOuName);
		//Remove itself from the list of groups with the same ad name in same ou
		groupsWithSameADNameInSameOu.remove(group);
		if(!groupsWithSameADNameInSameOu.isEmpty()) {
			throw new WrongReferenceAttributeValueException(attribute, resourceAdOuName, group, resource, resource, null,
					"Attribute AD Name can't be set for group and resource in this OU, because this value is already " +
					"set for different group and resource in the same OU!");
		}
	}

	@Override
	public List<String> getDependencies() {
		List<String> dependencies = new ArrayList<>();
		dependencies.add(A_R_D_AD_OU_NAME);
		return dependencies;
	}

	@Override
	public AttributeDefinition getAttributeDefinition() {
		AttributeDefinition attr = new AttributeDefinition();
		attr.setNamespace(AttributesManager.NS_GROUP_RESOURCE_ATTR_DEF);
		attr.setFriendlyName("adName");
		attr.setDisplayName("AD Name");
		attr.setType(String.class.getName());
		attr.setDescription("Name of AD");
		return attr;
	}
}
