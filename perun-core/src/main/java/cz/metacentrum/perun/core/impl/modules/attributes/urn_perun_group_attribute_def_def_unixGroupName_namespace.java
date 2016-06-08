package cz.metacentrum.perun.core.impl.modules.attributes;

import java.util.ArrayList;
import java.util.List;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.AttributesManager;
import cz.metacentrum.perun.core.api.Facility;
import cz.metacentrum.perun.core.api.Group;
import cz.metacentrum.perun.core.api.Resource;
import cz.metacentrum.perun.core.api.exceptions.AttributeNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.ConsistencyErrorException;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeAssignmentException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeValueException;
import cz.metacentrum.perun.core.api.exceptions.WrongReferenceAttributeValueException;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;
import cz.metacentrum.perun.core.implApi.modules.attributes.GroupAttributesModuleAbstract;
import cz.metacentrum.perun.core.implApi.modules.attributes.GroupAttributesModuleImplApi;
import java.util.HashSet;
import java.util.Set;

/**
 *
 * @author Michal Stava  stavamichal@gmail.com
 */
public class urn_perun_group_attribute_def_def_unixGroupName_namespace extends GroupAttributesModuleAbstract implements GroupAttributesModuleImplApi {

	private static final String A_R_unixGroupName_namespace = AttributesManager.NS_RESOURCE_ATTR_DEF + ":unixGroupName-namespace";
	private static final String A_G_unixGID_namespace = AttributesManager.NS_GROUP_ATTR_DEF + ":unixGID-namespace";

	@Override
	public void checkAttributeValue(PerunSessionImpl sess, Group group, Attribute attribute) throws InternalErrorException, WrongAttributeValueException, WrongReferenceAttributeValueException, WrongAttributeAssignmentException{
		//prepare namespace and groupName value variables
		String groupName = null;
		if(attribute.getValue() != null) groupName = (String) attribute.getValue();
		String groupNameNamespace = attribute.getFriendlyNameParameter();

		if(groupName == null) {
			// if this is group attribute, its ok
			return;
		}

		//Check attribute regex
		sess.getPerunBl().getModulesUtilsBl().checkAttributeRegex(attribute, "^[-._a-zA-Z0-9]+$");

		//Check reserved unix group names
		sess.getPerunBl().getModulesUtilsBl().checkReservedUnixGroupNames(attribute);

		try {
			//prepare attributes group and resource unixGroupName
			Attribute groupUnixGroupName = attribute;
			Attribute resourceUnixGroupName = new Attribute(sess.getPerunBl().getAttributesManagerBl().getAttributeDefinition(sess, A_R_unixGroupName_namespace + ":" + groupNameNamespace));
			resourceUnixGroupName.setValue(attribute.getValue());

			//prepare lists of groups and resources with the same groupName value in the same namespace
			List<Group> groupsWithSameGroupNameInTheSameNamespace = new ArrayList<Group>();
			List<Resource> resourcesWithSameGroupNameInTheSameNamespace = new ArrayList<Resource>();

			//Fill lists of groups and resources
			groupsWithSameGroupNameInTheSameNamespace.addAll(sess.getPerunBl().getGroupsManagerBl().getGroupsByAttribute(sess, groupUnixGroupName));
			resourcesWithSameGroupNameInTheSameNamespace.addAll(sess.getPerunBl().getResourcesManagerBl().getResourcesByAttribute(sess, resourceUnixGroupName));

			//If there is no group or resource with same GroupNameInTheSameNamespace, its ok
			if(groupsWithSameGroupNameInTheSameNamespace.isEmpty() && resourcesWithSameGroupNameInTheSameNamespace.isEmpty()) return;

			//First need to know that i have right to write any of duplicit groupName-namespace attribute
			boolean haveRights = sess.getPerunBl().getModulesUtilsBl().haveRightToWriteAttributeInAnyGroupOrResource(sess, groupsWithSameGroupNameInTheSameNamespace, resourcesWithSameGroupNameInTheSameNamespace, groupUnixGroupName, resourceUnixGroupName);
			if(!haveRights) throw new WrongReferenceAttributeValueException(attribute, "This groupName is already used for other group or resource and user has no rights to use it.");

			//Now if rights are ok, prepare lists of UnixGIDs attributes of this group (also equivalent resource GID)
			List<Attribute> groupUnixGIDs = sess.getPerunBl().getAttributesManagerBl().getAllAttributesStartWithNameWithoutNullValue(sess, group, A_G_unixGID_namespace + ":");
			List<Attribute> resourceVersionOfUnixGIDs = sess.getPerunBl().getModulesUtilsBl().getListOfResourceGIDsFromListOfGroupGIDs(sess, groupUnixGIDs);

			//In list of duplicit groups looking for GID in same namespace but with different value, thats not correct
			if(!groupsWithSameGroupNameInTheSameNamespace.isEmpty()) {
				for(Group g: groupsWithSameGroupNameInTheSameNamespace) {
					for(Attribute a: groupUnixGIDs) {
						int compare;
						compare = sess.getPerunBl().getModulesUtilsBl().haveTheSameAttributeWithTheSameNamespace(sess, g, a);

						if(compare > 0) {
							throw new WrongReferenceAttributeValueException(attribute, a, "One of the group GIDs is from the same namespace like other group GID but with different values.");
						}
					}
				}
			}

			//In list of duplicit resources looking for GID in same namespace but with different value, thats not correct
			if(!resourcesWithSameGroupNameInTheSameNamespace.isEmpty()) {
				for(Resource r: resourcesWithSameGroupNameInTheSameNamespace) {
					for(Attribute a: resourceVersionOfUnixGIDs) {
						int compare;
						compare = sess.getPerunBl().getModulesUtilsBl().haveTheSameAttributeWithTheSameNamespace(sess, r, a);

						if(compare > 0) {
							throw new WrongReferenceAttributeValueException(attribute, a, "One of the group GIDs is from the same namespace like other resource GIDs but with different values.");
						}
					}
				}
			}

		} catch(AttributeNotExistsException ex) {
			throw new ConsistencyErrorException(ex);
		}
	}

	@Override
	public void changedAttributeHook(PerunSessionImpl session, Group group, Attribute attribute) throws InternalErrorException, WrongReferenceAttributeValueException {
		//Need to know if this is remove or set, if value is null, its remove, otherway it is set
		String groupNameNamespace = attribute.getFriendlyNameParameter();

		try {
			if(attribute.getValue() == null) {
				//This is ok, for now no changes for removing some GroupName of this Group
			} else {
				//First need to find all facilities for the group
				Set<Facility> facilitiesOfGroup = new HashSet<Facility>();
				List<Resource> resourcesOfGroup = session.getPerunBl().getResourcesManagerBl().getAssignedResources(session, group);
				for(Resource r: resourcesOfGroup) {
					facilitiesOfGroup.add(session.getPerunBl().getResourcesManagerBl().getFacility(session, r));
				}

				//Prepare list of gid namespaces of all facilities which have the same groupName namespace like this unixGroupName namespace
				Set<String> gidNamespaces;
				gidNamespaces = session.getPerunBl().getModulesUtilsBl().getSetOfGIDNamespacesWhereFacilitiesHasTheSameGroupNameNamespace(session, new ArrayList<Facility>(facilitiesOfGroup), attribute);

				//If there is any gidNamespace which is need to be set, do it there
				if(!gidNamespaces.isEmpty()) {
					List<Attribute> gidsToSet = new ArrayList<>();
					for(String s: gidNamespaces) {
						Attribute groupUnixGIDNamespace = session.getPerunBl().getAttributesManagerBl().getAttribute(session, group, A_G_unixGID_namespace + ":" + s);
						//If attribute is not set, then set it (first fill, then set)
						if(groupUnixGIDNamespace.getValue() == null) {
							groupUnixGIDNamespace = session.getPerunBl().getAttributesManagerBl().fillAttribute(session, group, groupUnixGIDNamespace);

							if(groupUnixGIDNamespace.getValue() == null) throw new WrongReferenceAttributeValueException(attribute, groupUnixGIDNamespace);

							//Set after fill (without check because all namespaces must be set before check (there can be relation between namespaces)
							gidsToSet.add(groupUnixGIDNamespace);
						}
					}
					//set and check if there is some gid to set
					if(!gidsToSet.isEmpty()) {
						try {
							session.getPerunBl().getAttributesManagerBl().setAttributes(session, group, gidsToSet);
						} catch (WrongAttributeValueException e) {
							throw new WrongReferenceAttributeValueException(attribute, e.getAttribute(), group, null, e.getAttributeHolder(), e.getAttributeHolderSecondary(), "Problem when setting all needed GIDs in hook.", e);
						}
					}
				}
			}
		} catch (WrongAttributeAssignmentException ex) {
			//TODO: need to add WrongAttributeAssignmentException to header of modules methods
			throw new InternalErrorException(ex);
		} catch (AttributeNotExistsException ex) {
			throw new ConsistencyErrorException(ex);
		}
	}

	/*public AttributeDefinition getAttributeDefinition() {
		AttributeDefinition attr = new AttributeDefinition();
		attr.setNamespace(AttributesManager.NS_GROUP_ATTR_DEF);
		attr.setFriendlyName("unixGroupName-namespace");
		attr.setType(String.class.getName());
		attr.setDescription("Unix Group Name namespace.");
		return attr;
	}*/
}
