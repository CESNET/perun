package cz.metacentrum.perun.core.impl.modules.attributes;

import cz.metacentrum.perun.core.api.ActionType;
import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.AttributesManager;
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

/**
 * Group googleGroup-namespace attribute.
 *
 * @author Michal Holič  holic.michal@gmail.com
 */
public class urn_perun_group_attribute_def_def_googleGroup_namespace_einfra_cesnet_cz extends GroupAttributesModuleAbstract implements GroupAttributesModuleImplApi {

	@Override
	public void checkAttributeValue(PerunSessionImpl sess, Group group, Attribute attribute) throws InternalErrorException, WrongAttributeValueException, WrongReferenceAttributeValueException, WrongAttributeAssignmentException {
		//prepare groupName value variable
		String groupName = null;
		if(attribute.getValue() != null) groupName = (String) attribute.getValue();

		if(groupName == null) {
			// if this is group attribute, its ok
			return;
		}else if(!groupName.matches("^[-_a-zA-Z0-9]+$")){
			throw new WrongAttributeValueException(attribute,"GroupName attributte content invalid characters. Allowed are only letters, numbers and characters _ and -.");
		}

		//TODO Check reserved google group names
		//sess.getPerunBl().getModulesUtilsBl().checkReservedGoogleGroupNames(attribute);

		//prepare lists of groups with the same groupName value in the same namespace
		List<Group> groupsWithSameGroupNameInTheSameNamespace = new ArrayList<Group>();

		//Fill lists of groups
		groupsWithSameGroupNameInTheSameNamespace.addAll(sess.getPerunBl().getGroupsManagerBl().getGroupsByAttribute(sess, attribute));

		//If there is no group with same GroupNameInTheSameNamespace, its ok. Remove this group from the list first just to be sure.
		groupsWithSameGroupNameInTheSameNamespace.remove(group);
		if(groupsWithSameGroupNameInTheSameNamespace.isEmpty()) return;

		//Check if I have right to write on the attribute using the group
		boolean haveRights = AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, attribute, group, null);
		if(!haveRights) throw new WrongReferenceAttributeValueException(attribute, "User has no rights to write on the attribute using the group.");
	}

	public AttributeDefinition getAttributeDefinition() {
		AttributeDefinition attr = new AttributeDefinition();
		attr.setNamespace(AttributesManager.NS_GROUP_ATTR_DEF);
		attr.setFriendlyName("googleGroupName-namespace:einfra.cesnet.cz");
		attr.setDisplayName("Google group name (domain: einfra.cesnet.cz)");
		attr.setType(String.class.getName());
		attr.setDescription("Name of the group in einfra.cesnet.cz domain in google groups");
		return attr;
	}
}
