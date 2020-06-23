package cz.metacentrum.perun.core.impl.modules.attributes;

import cz.metacentrum.perun.core.api.ActionType;
import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AuthzResolver;
import cz.metacentrum.perun.core.api.Group;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeAssignmentException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeValueException;
import cz.metacentrum.perun.core.api.exceptions.WrongReferenceAttributeValueException;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;
import cz.metacentrum.perun.core.implApi.modules.attributes.GroupAttributesModuleAbstract;
import cz.metacentrum.perun.core.implApi.modules.attributes.GroupAttributesModuleImplApi;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Group googleGroup-namespace attribute.
 *
 * @author Michal Holič  holic.michal@gmail.com
 */
public class urn_perun_group_attribute_def_def_googleGroupName_namespace extends GroupAttributesModuleAbstract implements GroupAttributesModuleImplApi {

	private static final Pattern pattern = Pattern.compile("^[-_a-zA-Z0-9']+$");

	@Override
	public void checkAttributeSyntax(PerunSessionImpl sess, Group group, Attribute attribute) throws WrongAttributeValueException {
		//prepare groupName value variable
		String groupName = null;
		if(attribute.getValue() != null) groupName = attribute.valueAsString();

		if(groupName == null) {
			// if this is group attribute, its ok
			return;
		}
		Matcher matcher = pattern.matcher(groupName);
		if(!matcher.matches()) throw new WrongAttributeValueException(attribute, group, "GroupName attribute content invalid characters. Allowed are only letters, numbers and characters _ and -.");

		//TODO Check reserved google group names
		//sess.getPerunBl().getModulesUtilsBl().checkReservedGoogleGroupNames(attribute);
	}

	@Override
	public void checkAttributeSemantics(PerunSessionImpl sess, Group group, Attribute attribute) throws WrongReferenceAttributeValueException, WrongAttributeAssignmentException {
		// null value is ok
		if (attribute.getValue() == null) return;

		//prepare lists of groups with the same groupName value in the same namespace

		//Fill lists of groups
		List<Group> groupsWithSameGroupNameInTheSameNamespace = new ArrayList<>(sess.getPerunBl().getGroupsManagerBl().getGroupsByAttribute(sess, attribute));

		//If there is no group with same GroupNameInTheSameNamespace, its ok. Remove this group from the list first just to be sure.
		groupsWithSameGroupNameInTheSameNamespace.remove(group);
		//if any other group with same GroupName in this namespace exists, check if user has right to use this name at least in one of these groups
		if (!groupsWithSameGroupNameInTheSameNamespace.isEmpty()) {
			boolean haveRights = false;
			for(Group groupWithSameGroupName: groupsWithSameGroupNameInTheSameNamespace) {
				if(AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, attribute, groupWithSameGroupName)) {
					haveRights = true;
					break;
				}
			}
			//if not, than can't use already used groupName
			if(!haveRights) throw new WrongReferenceAttributeValueException(attribute, null, group, null, "GroupName is already used in this namespace: " + attribute.getFriendlyNameParameter() + " and you haven't right to use it.");
		}
	}

	/*
	public AttributeDefinition getAttributeDefinition() {
		AttributeDefinition attr = new AttributeDefinition();
		attr.setNamespace(AttributesManager.NS_GROUP_ATTR_DEF);
		attr.setFriendlyName("googleGroupName-namespace:*");
		attr.setDisplayName("Google group name in namespace");
		attr.setType(String.class.getName());
		attr.setDescription("Name of the group in some domain in google groups");
		return attr;
	}*/
}
