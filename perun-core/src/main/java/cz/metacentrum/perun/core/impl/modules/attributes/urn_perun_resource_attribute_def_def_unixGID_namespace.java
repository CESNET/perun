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
import cz.metacentrum.perun.core.implApi.modules.attributes.ResourceAttributesModuleAbstract;
import cz.metacentrum.perun.core.implApi.modules.attributes.ResourceAttributesModuleImplApi;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * Resource unixGID-namespace attribute.
 *
 * @author Michal Stava  stavamichal@gmail.com
 */
public class urn_perun_resource_attribute_def_def_unixGID_namespace extends ResourceAttributesModuleAbstract implements ResourceAttributesModuleImplApi {

	private final static Logger log = LoggerFactory.getLogger(urn_perun_resource_attribute_def_def_unixGID_namespace.class);

	private static final String A_G_unixGID_namespace = AttributesManager.NS_GROUP_ATTR_DEF + ":unixGID-namespace";
	private static final String A_R_unixGroupName_namespace = AttributesManager.NS_RESOURCE_ATTR_DEF + ":unixGroupName-namespace";
	private static final String A_G_unixGroupName_namespace = AttributesManager.NS_GROUP_ATTR_DEF + ":unixGroupName-namespace";
	private static final String A_E_usedGids = AttributesManager.NS_ENTITYLESS_ATTR_DEF + ":usedGids";

	public Attribute fillAttribute(PerunSessionImpl sess, Resource resource, AttributeDefinition attributeDefinition) throws InternalErrorException, WrongAttributeAssignmentException {
		Attribute attribute = new Attribute(attributeDefinition);
		String gidNamespace = attribute.getFriendlyNameParameter();

		//First I get all GroupNames of this resource (for any namespaces)
		List <Attribute> groupNamesOfResource = sess.getPerunBl().getAttributesManagerBl().getAllAttributesStartWithNameWithoutNullValue(sess, resource, A_R_unixGroupName_namespace + ":");

		//If there exist some groupName of this resource
		if(!groupNamesOfResource.isEmpty()) {
			//Get All Groups and Resources with some same GroupName in the same Namespace
			List<Group> groupsWithSameGroupNameInSameNamespace = new ArrayList<Group>();
			List<Resource> resourcesWithSameGroupNameInSameNamespace = new ArrayList<Resource>();
			for(Attribute attr: groupNamesOfResource) {
				Attribute groupNameOfGroup;
				try {
					groupNameOfGroup = new Attribute(sess.getPerunBl().getAttributesManagerBl().getAttributeDefinition(sess, A_G_unixGroupName_namespace + ":" + attr.getFriendlyNameParameter()));
				} catch (AttributeNotExistsException ex) {
					throw new ConsistencyErrorException("AttributeDefinition for group_def_unixGroupName-namespace:" + attr.getFriendlyNameParameter() + " must exists", ex);
				}
				groupNameOfGroup.setValue(attr.getValue());

				//Get all resources and groups with some GroupName same with same Namespace
				groupsWithSameGroupNameInSameNamespace.addAll(sess.getPerunBl().getGroupsManagerBl().getGroupsByAttribute(sess, groupNameOfGroup));
				resourcesWithSameGroupNameInSameNamespace.addAll(sess.getPerunBl().getResourcesManagerBl().getResourcesByAttribute(sess, attr));
			}

			//Prepare variable for commonGID
			Integer commonGID = null;

			//Test if exists common GID for this group and other groups and resources
			commonGID = sess.getPerunBl().getModulesUtilsBl().getCommonGIDOfGroupsWithSameNameInSameNamespace(sess, groupsWithSameGroupNameInSameNamespace, gidNamespace, commonGID);
			commonGID = sess.getPerunBl().getModulesUtilsBl().getCommonGIDOfResourcesWithSameNameInSameNamespace(sess, resourcesWithSameGroupNameInSameNamespace, gidNamespace, commonGID);

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
				log.warn("Free unix gid not found for resource:[" + resource + "] in unix group namespace " + gidNamespace);
			} else if(freeGID > 0 || freeGID < 0) {
				//free GID found
				attribute.setValue(freeGID);
			}

			return attribute;

		} catch(AttributeNotExistsException ex) {
			throw new ConsistencyErrorException(ex);
		}
	}

	public void checkAttributeValue(PerunSessionImpl sess, Resource resource, Attribute attribute) throws InternalErrorException, WrongAttributeValueException, WrongReferenceAttributeValueException, WrongAttributeAssignmentException{
		try{
			String gidNamespace = attribute.getFriendlyNameParameter();

			//Special behaviour if gid is null
			Integer attrValue = null;
			if(attribute.getValue() == null) {
				throw new WrongAttributeValueException(attribute, resource, "Unix GID must be set");
			} else {
				attrValue = (Integer) attribute.getValue();
			}

			//Check if GID is within allowed range
			sess.getPerunBl().getModulesUtilsBl().checkIfGIDIsWithinRange(sess, attribute);

			//check if gid is not already depleted
			Attribute usedGids = sess.getPerunBl().getAttributesManagerBl().getAttribute(sess, gidNamespace, A_E_usedGids);
			//null in value means there is no depleted or used gids
			if(usedGids.getValue() != null) {
				Map<String, String> usedGidsValue = (Map<String,String>) usedGids.getValue();
				//Dx, where x is GID means depleted value for GID x
				if(usedGidsValue.containsKey("D" + attrValue.toString())) {
					throw new WrongReferenceAttributeValueException(attribute, usedGids, resource, null, gidNamespace, null, "This GID is already depleted.");
				}
			}

			//Prepare lists for all groups and resources with same GID in the same namespace
			List<Group> allGroupsWithSameGIDInSameNamespace = new ArrayList<Group>();
			List<Resource> allResourcesWithSameGIDInSameNamespace = new ArrayList<Resource>();

			//Prepare attributes for searching through groups and resources
			Attribute resourceGIDAttribute = attribute;
			Attribute groupGIDAttribute = new Attribute(sess.getPerunBl().getAttributesManagerBl().getAttributeDefinition(sess, A_G_unixGID_namespace + ":" + gidNamespace));
			groupGIDAttribute.setValue(resourceGIDAttribute.getValue());

			//Fill lists of Groups and Resources by data
			allGroupsWithSameGIDInSameNamespace.addAll(sess.getPerunBl().getGroupsManagerBl().getGroupsByAttribute(sess, groupGIDAttribute));
			allResourcesWithSameGIDInSameNamespace.addAll(sess.getPerunBl().getResourcesManagerBl().getResourcesByAttribute(sess, resourceGIDAttribute));
			//remove this resource
			allResourcesWithSameGIDInSameNamespace.remove(resource);

			//Prepare list of GroupName attributes of this resource
			List <Attribute> groupNamesOfResource = sess.getPerunBl().getAttributesManagerBl().getAllAttributesStartWithNameWithoutNullValue(sess, resource, A_R_unixGroupName_namespace + ":");

			//Searching through groups
			if(!allGroupsWithSameGIDInSameNamespace.isEmpty()) {
				for(Group g: allGroupsWithSameGIDInSameNamespace) {
					for(Attribute a: groupNamesOfResource) {
						//Prepare group version of this group attribute
						Attribute groupGroupName = new Attribute(sess.getPerunBl().getAttributesManagerBl().getAttributeDefinition(sess, A_G_unixGroupName_namespace + ":" + a.getFriendlyNameParameter()));
						groupGroupName.setValue(a.getValue());

						int compare = sess.getPerunBl().getModulesUtilsBl().haveTheSameAttributeWithTheSameNamespace(sess, g, groupGroupName);

						if(compare > 0) {
							//This is problem, there is the same attribute but have other value
							throw new WrongReferenceAttributeValueException(attribute, a, "There is a group with same GID (namespace: "  + gidNamespace + ") and different unix group name (namespace: " + a.getFriendlyNameParameter() + "). " + g + " " + resource);
						}
						//Other possibilities are not problem, less than 0 mean that same attribute not exists, and 0 mean that attribute exists but have same value
					}
				}
			}

			//Searching through resources
			if(!allResourcesWithSameGIDInSameNamespace.isEmpty()) {
				for(Resource r: allResourcesWithSameGIDInSameNamespace) {
					for(Attribute a: groupNamesOfResource) {


						int compare = sess.getPerunBl().getModulesUtilsBl().haveTheSameAttributeWithTheSameNamespace(sess, r, a);

						if(compare > 0) {
							//This is problem, there is the same attribute but have other value
							throw new WrongReferenceAttributeValueException(attribute, a, "There is a resource with same GID (namespace: "  + gidNamespace + ") and different unix group name (namespace: " + a.getFriendlyNameParameter() + "). " + r + " " + resource);
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
	public void changedAttributeHook(PerunSessionImpl session, Resource resource, Attribute attribute) throws InternalErrorException, WrongReferenceAttributeValueException {
		String gidNamespace = attribute.getFriendlyNameParameter();

		//get attribute with usedGids for update
		//IMPORTANT: for update lock row in table of attr values, be careful when using
		Attribute usedGids;
		try {
			usedGids = session.getPerunBl().getAttributesManagerBl().getEntitylessAttributeForUpdate(session, gidNamespace, A_E_usedGids);
		} catch (AttributeNotExistsException ex) {
			throw new ConsistencyErrorException(ex);
		}

		//Get Map of gids (if there is no value, use empty map
		Map<String, String> usedGidsValue = new LinkedHashMap<>();
		if(usedGids.getValue() != null) usedGidsValue = (Map<String,String>) usedGids.getValue();

		//initial settings
		String key = "R" + resource.getId();
		String oldGid = usedGidsValue.get(key);

		//for removing gid
		if(attribute.getValue() == null) {
			//remove record from map
			if(oldGid != null) {
				usedGidsValue.remove(key);
				//looking for another oldGid value, if not exists, add depleted record
				if(!usedGidsValue.containsValue(oldGid)) {
					usedGidsValue.put("D" + oldGid, oldGid);
				}
			}
		//for setting gid
		} else {
			String newUnixGid = ((Integer) attribute.getValue()).toString();
			//add new record to map
			usedGidsValue.put(key, newUnixGid);
			//looking for another oldGid value, if not exists, add depleted record
			if(oldGid != null && !usedGidsValue.containsValue(oldGid)) {
				usedGidsValue.put("D" + oldGid, oldGid);
			}
		}

		//set new attribute value for usedGids
		usedGids.setValue(usedGidsValue);
		try {
			session.getPerunBl().getAttributesManagerBl().setAttribute(session, gidNamespace, usedGids);
		} catch (WrongAttributeValueException ex) {
			throw new WrongReferenceAttributeValueException(attribute, usedGids, ex);
		} catch (WrongAttributeAssignmentException ex) {
			throw new InternalErrorException(ex);
		}
	}

	@Override
	public List<String> getDependencies() {
		List<String> dependencies = new ArrayList<String>();
		dependencies.add(AttributesManager.NS_ENTITYLESS_ATTR_DEF + ":namespace-GIDRanges");
		//Temporary disallowed for performance reason
		//dependencies.add(A_E_usedGids);
		return dependencies;
	}

	/*public AttributeDefinition getAttributeDefinition() {
		AttributeDefinition attr = new AttributeDefinition();
		attr.setNamespace(AttributesManager.NS_RESOURCE_ATTR_DEF);
		attr.setFriendlyName("unixGID-namespace");
		attr.setType(Integer.class.getName());
		attr.setDescription("Unix GID namespace.");
		return attr;
	}*/
}
