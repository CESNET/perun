package cz.metacentrum.perun.core.impl.modules.attributes;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
 * Group unixGID-namespace attribute.
 *
 * @author Michal Stava  stavamichal@gmail.com
 */
public class urn_perun_group_attribute_def_def_unixGID_namespace extends GroupAttributesModuleAbstract implements GroupAttributesModuleImplApi {

	private final static Logger log = LoggerFactory.getLogger(urn_perun_group_attribute_def_def_unixGID_namespace.class);

	private static final String A_R_unixGID_namespace = AttributesManager.NS_RESOURCE_ATTR_DEF + ":unixGID-namespace";
	private static final String A_R_unixGroupName_namespace = AttributesManager.NS_RESOURCE_ATTR_DEF + ":unixGroupName-namespace";
	private static final String A_G_unixGroupName_namespace = AttributesManager.NS_GROUP_ATTR_DEF + ":unixGroupName-namespace";

	public Attribute fillAttribute(PerunSessionImpl sess, Group group, AttributeDefinition attributeDefinition) throws InternalErrorException, WrongAttributeAssignmentException {
		Attribute attribute = new Attribute(attributeDefinition);
		String gidNamespace = attribute.getFriendlyNameParameter();

		//First check if generating is needed (if fill make a sense)
		//Get All Facilities from group
		Set<Facility> facilitiesOfGroup = new HashSet<Facility>();
		List<Resource> resourcesOfGroup = sess.getPerunBl().getResourcesManagerBl().getAssignedResources(sess, group);
		for(Resource r: resourcesOfGroup) {
			facilitiesOfGroup.add(sess.getPerunBl().getResourcesManagerBl().getFacility(sess, r));
		}
		//Prepare list of gid namespaces of all facilities which have the same groupName namespace like this unixGroupName namespace
		Set<String> groupNameNamespaces;
		try {
			groupNameNamespaces = sess.getPerunBl().getModulesUtilsBl().getSetOfGroupNameNamespacesWhereFacilitiesHasTheSameGIDNamespace(sess, new ArrayList<Facility>(facilitiesOfGroup), attribute);
		} catch(WrongReferenceAttributeValueException ex) {
			//TODO: need to add WrongAttributeAssignmentException to header of modules methods
			throw new InternalErrorException(ex);
		}
		//If this group has GroupName-namespace attribute with notNull value in any namespace from groupNameNamespaces, continue, else return attribute with null value
		try {
			if(!sess.getPerunBl().getModulesUtilsBl().isGroupUnixGIDNamespaceFillable(sess, group, attribute)) return attribute;
		} catch (WrongReferenceAttributeValueException ex) {
			throw new ConsistencyErrorException(ex);
		}

		//After check I get all GroupNames of this group (for any namespaces)
		List <Attribute> groupNamesOfGroup = sess.getPerunBl().getAttributesManagerBl().getAllAttributesStartWithNameWithoutNullValue(sess, group, A_G_unixGroupName_namespace + ":");

		//If there exist some groupName of this group
		if(!groupNamesOfGroup.isEmpty()) {
			//Get All Groups and Resources with some same GroupName in the same Namespace
			Set<Group> groupsWithSameGroupNameInSameNamespace = new HashSet<Group>();
			Set<Resource> resourcesWithSameGroupNameInSameNamespace = new HashSet<Resource>();
			for(Attribute attr: groupNamesOfGroup) {
				Attribute groupNameOfResource;
				try {
					groupNameOfResource = new Attribute(sess.getPerunBl().getAttributesManagerBl().getAttributeDefinition(sess, A_R_unixGroupName_namespace + ":" + attr.getFriendlyNameParameter()));
				} catch (AttributeNotExistsException ex) {
					throw new ConsistencyErrorException("AttributeDefinition for resource_def_unixGroupName-namespace:" + attr.getFriendlyNameParameter() + " must exists", ex);
				}
				groupNameOfResource.setValue(attr.getValue());

				//Get all resources and groups with some GroupName same with same Namespace
				groupsWithSameGroupNameInSameNamespace.addAll(sess.getPerunBl().getGroupsManagerBl().getGroupsByAttribute(sess, attr));
				resourcesWithSameGroupNameInSameNamespace.addAll(sess.getPerunBl().getResourcesManagerBl().getResourcesByAttribute(sess, groupNameOfResource));
			}

			//Prepare variable for commonGID
			Integer commonGID = null;

			//Test if exists common GID for this group and other groups and resources
			commonGID = sess.getPerunBl().getModulesUtilsBl().getCommonGIDOfGroupsWithSameNameInSameNamespace(sess, new ArrayList(groupsWithSameGroupNameInSameNamespace), gidNamespace, commonGID);
			commonGID = sess.getPerunBl().getModulesUtilsBl().getCommonGIDOfResourcesWithSameNameInSameNamespace(sess, new ArrayList(resourcesWithSameGroupNameInSameNamespace), gidNamespace, commonGID);

			//If commonGID exists, set it
			if(commonGID != null) {
				attribute.setValue(commonGID);
				return attribute;
			}
		}

		//If commonGID not exists, try to set new one
		try {
			Integer freeGID = sess.getPerunBl().getModulesUtilsBl().getFreeGID(sess, attribute);

			if(freeGID == null) {
				//free GID not found
				log.warn("Free unix gid not found for group:[" + group + "] in unix group namespace " + gidNamespace);
			} else if(freeGID > 0 || freeGID < 0) {
				//free GID found
				attribute.setValue(freeGID);
			}

			return attribute;

		} catch(AttributeNotExistsException ex) {
			throw new ConsistencyErrorException(ex);
		}
	}

	public void checkAttributeValue(PerunSessionImpl sess, Group group, Attribute attribute) throws InternalErrorException, WrongAttributeValueException, WrongReferenceAttributeValueException, WrongAttributeAssignmentException{
		try{
			String gidNamespace = attribute.getFriendlyNameParameter();

			//Special behaviour if gid is null
			if(attribute.getValue() == null) {
				List<Facility> groupFacilities = new ArrayList<Facility>();
				for(Resource r: sess.getPerunBl().getResourcesManagerBl().getAssignedResources(sess, group)) {
					groupFacilities.add(sess.getPerunBl().getResourcesManagerBl().getFacility(sess, r));
				}

				Set<String> namespacesWhereGroupMustHaveGIDifItHaveUnixNameThere = sess.getPerunBl().getModulesUtilsBl().getSetOfGroupNameNamespacesWhereFacilitiesHasTheSameGIDNamespace(sess, groupFacilities, attribute);
				for(String namespace : namespacesWhereGroupMustHaveGIDifItHaveUnixNameThere) {
					Attribute unixGroupName = sess.getPerunBl().getAttributesManagerBl().getAttribute(sess, group, A_G_unixGroupName_namespace + ":" + namespace);
					if(unixGroupName.getValue() != null) {
						throw new WrongAttributeValueException(attribute, group, "Group is propagated to the facility where it have set unix group name so it must have unix GID too.");
					}
				}
				return;   //Group is not propagated to any facility in this GID namespace or it doesn't have set unix name there so it doesn't need to have unix GID.
			}

			//Prepare lists for all groups and resources with same GID in the same namespace
			List<Group> allGroupsWithSameGIDInSameNamespace = new ArrayList<Group>();
			List<Resource> allResourcesWithSameGIDInSameNamespace = new ArrayList<Resource>();

			//Prepare attributes for searching through groups and resources
			Attribute groupGIDAttribute = attribute;
			Attribute resourceGIDAttribute = new Attribute(sess.getPerunBl().getAttributesManagerBl().getAttributeDefinition(sess, A_R_unixGID_namespace + ":" + gidNamespace));
			resourceGIDAttribute.setValue(groupGIDAttribute.getValue());

			//Fill lists of Groups and Resources by data
			allGroupsWithSameGIDInSameNamespace.addAll(sess.getPerunBl().getGroupsManagerBl().getGroupsByAttribute(sess, groupGIDAttribute));
			allResourcesWithSameGIDInSameNamespace.addAll(sess.getPerunBl().getResourcesManagerBl().getResourcesByAttribute(sess, resourceGIDAttribute));

			//If there is no group or resource with same GID in the same namespace, its ok so only check if gid is within range
			if(allGroupsWithSameGIDInSameNamespace.isEmpty() && allResourcesWithSameGIDInSameNamespace.isEmpty()) {
				sess.getPerunBl().getModulesUtilsBl().checkIfGIDIsWithinRange(sess, attribute);
				return;
			}

			//Prepare list of GroupName attributes of this group
			List <Attribute> groupNamesOfGroup = sess.getPerunBl().getAttributesManagerBl().getAllAttributesStartWithNameWithoutNullValue(sess, group, A_G_unixGroupName_namespace + ":");

			//Searching through groups
			if(!allGroupsWithSameGIDInSameNamespace.isEmpty()) {
				for(Group g: allGroupsWithSameGIDInSameNamespace) {
					for(Attribute a: groupNamesOfGroup) {
						int compare = sess.getPerunBl().getModulesUtilsBl().haveTheSameAttributeWithTheSameNamespace(sess, g, a);

						if(compare > 0) {
							//This is problem, there is the same attribute but have other value
							throw new WrongReferenceAttributeValueException(attribute, a, "There is a group with same GID (namespace: "  + gidNamespace + ") and different unix group name (namespace: " + a.getFriendlyNameParameter() + "). " + g + " " + group);
						}
						//Other possibilities are not problem, less than 0 mean that same attribute not exists, and 0 mean that attribute exists but have same value
					}
				}
			}

			//Searching through resources
			if(!allResourcesWithSameGIDInSameNamespace.isEmpty()) {
				for(Resource r: allResourcesWithSameGIDInSameNamespace) {
					for(Attribute a: groupNamesOfGroup) {
						//Prepare resource version of this group attribute
						Attribute resourceGroupName = new Attribute(sess.getPerunBl().getAttributesManagerBl().getAttributeDefinition(sess, A_R_unixGroupName_namespace + ":" + a.getFriendlyNameParameter()));
						resourceGroupName.setValue(a.getValue());

						int compare = sess.getPerunBl().getModulesUtilsBl().haveTheSameAttributeWithTheSameNamespace(sess, r, resourceGroupName);

						if(compare > 0) {
							//This is problem, there is the same attribute but have other value
							throw new WrongReferenceAttributeValueException(attribute, a, "There is a resource with same GID (namespace: "  + gidNamespace + ") and different unix group name (namespace: " + a.getFriendlyNameParameter() + "). " + r + " " + group);
						}
						//Other possibilities are not problem, less than 0 mean that same attribute not exists, and 0 mean that attribute exists but have same value
					}
				}
			}

		} catch(AttributeNotExistsException ex) {
			throw new ConsistencyErrorException(ex);
		}
	}

	@Override
	public List<String> getDependencies() {
		List<String> dependencies = new ArrayList<String>();
		dependencies.add(AttributesManager.NS_ENTITYLESS_ATTR_DEF + ":namespace-minGID");
		dependencies.add(AttributesManager.NS_ENTITYLESS_ATTR_DEF + ":namespace-maxGID");
		return dependencies;
	}

	public AttributeDefinition getAttributeDefinition() {
		AttributeDefinition attr = new AttributeDefinition();
		attr.setNamespace(AttributesManager.NS_GROUP_ATTR_DEF);
		attr.setFriendlyName("unixGID-namespace");
		attr.setType(Integer.class.getName());
		attr.setDescription("Unix GID namespace.");
		return attr;
	}
}
