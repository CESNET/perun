package cz.metacentrum.perun.core.blImpl;

import cz.metacentrum.perun.core.api.ActionType;
import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.AttributesManager;
import cz.metacentrum.perun.core.api.AuthzResolver;
import cz.metacentrum.perun.core.api.Facility;
import cz.metacentrum.perun.core.api.Group;
import cz.metacentrum.perun.core.api.Pair;
import cz.metacentrum.perun.core.api.Resource;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.exceptions.AttributeNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.ConsistencyErrorException;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeAssignmentException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeValueException;
import cz.metacentrum.perun.core.api.exceptions.WrongReferenceAttributeValueException;
import cz.metacentrum.perun.core.bl.ModulesUtilsBl;
import cz.metacentrum.perun.core.bl.PerunBl;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;
import cz.metacentrum.perun.core.impl.Utils;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Michal Stava <stavamichal@gmail.com>
 */
public class ModulesUtilsBlImpl implements ModulesUtilsBl {

	final static Logger log = LoggerFactory.getLogger(ServicesManagerBlImpl.class);
	private PerunBl perunBl;

	public static final String A_E_namespace_minGID = AttributesManager.NS_ENTITYLESS_ATTR_DEF + ":namespace-minGID";
	public static final String A_E_namespace_maxGID = AttributesManager.NS_ENTITYLESS_ATTR_DEF + ":namespace-maxGID";
	public static final String A_G_unixGID_namespace = AttributesManager.NS_GROUP_ATTR_DEF + ":unixGID-namespace";
	public static final String A_G_unixGroupName_namespace = AttributesManager.NS_GROUP_ATTR_DEF + ":unixGroupName-namespace";
	public static final String A_R_unixGID_namespace = AttributesManager.NS_RESOURCE_ATTR_DEF + ":unixGID-namespace";
	public static final String A_R_unixGroupName_namespace = AttributesManager.NS_RESOURCE_ATTR_DEF + ":unixGroupName-namespace";
	public static final String A_F_unixGID_namespace = AttributesManager.NS_FACILITY_ATTR_DEF + ":unixGID-namespace";
	public static final String A_F_unixGroupName_namespace = AttributesManager.NS_FACILITY_ATTR_DEF + ":unixGroupName-namespace";
	private static final String A_E_usedGids = AttributesManager.NS_ENTITYLESS_ATTR_DEF + ":usedGids";

	public final static List<String> reservedNamesForUnixGroups = Arrays.asList("root", "daemon", "tty", "bin", "sys", "sudo", "nogroup");

	public ModulesUtilsBlImpl() {

	}

	public boolean isNamespaceEqualsToFacilityUnixGroupNameNamespace(PerunSessionImpl sess, Facility facility, String namespace) throws InternalErrorException, AttributeNotExistsException, WrongAttributeAssignmentException{
		Utils.notNull(facility, "facility");
		Utils.notNull(namespace, "namespace");
		Utils.notNull(sess, "perunSessionImpl");
		Attribute facilityNamespaceAttr = sess.getPerunBl().getAttributesManagerBl().getAttribute(sess, facility, A_F_unixGroupName_namespace + ":" + namespace);
		if(facilityNamespaceAttr.getValue() == null) return false;
		if(!namespace.equals(facilityNamespaceAttr.getValue())) {
			return false;
		}
		return true;
	}

	public List<Resource> findCollisionResourcesWithSameGroupName(PerunSessionImpl sess, Resource resource, String namespace) throws InternalErrorException, WrongAttributeAssignmentException, AttributeNotExistsException {
		Utils.notNull(sess, "perunSessionImpl");
		Utils.notNull(resource, "resource");
		Attribute resourceUnixGroupName = getPerunBl().getAttributesManagerBl().getAttribute(sess, resource, A_R_unixGroupName_namespace + ":" + namespace);
		List<Resource> resourcesWithSameUnixGroupName = getPerunBl().getResourcesManagerBl().getResourcesByAttribute(sess, resourceUnixGroupName);
		resourcesWithSameUnixGroupName.remove(resource);
		return resourcesWithSameUnixGroupName;
	}

	public List<Resource> findCollisionResourcesWithSameGroupName(PerunSessionImpl sess, Group group, String namespace) throws InternalErrorException, WrongAttributeAssignmentException, AttributeNotExistsException {
		Utils.notNull(sess, "perunSessionImpl");
		Utils.notNull(group, "group");
		Utils.notNull(namespace, "namespace");
		Attribute groupUnixGroupName = getPerunBl().getAttributesManagerBl().getAttribute(sess, group, A_G_unixGroupName_namespace + ":" + namespace);
		Attribute copyResourceUnixGroupName = new Attribute(getPerunBl().getAttributesManagerBl().getAttributeDefinition(sess, A_R_unixGroupName_namespace + ":" + namespace));
		copyResourceUnixGroupName.setValue(groupUnixGroupName.getValue());
		return getPerunBl().getResourcesManagerBl().getResourcesByAttribute(sess, copyResourceUnixGroupName);
	}

	public List<Group> findCollisionGroupsWithSamgeGroupName(PerunSessionImpl sess, Resource resource, String namespace) throws InternalErrorException, WrongAttributeAssignmentException, AttributeNotExistsException {
		Utils.notNull(sess, "perunSessionImpl");
		Utils.notNull(resource, "resource");
		Utils.notNull(namespace, "namespace");
		Attribute resourceUnixGroupName = getPerunBl().getAttributesManagerBl().getAttribute(sess, resource, A_R_unixGroupName_namespace + ":" + namespace);
		Attribute copyGroupUnixGroupName = new Attribute(getPerunBl().getAttributesManagerBl().getAttributeDefinition(sess, A_G_unixGroupName_namespace + ":" + namespace));
		copyGroupUnixGroupName.setValue(resourceUnixGroupName.getValue());
		return getPerunBl().getGroupsManagerBl().getGroupsByAttribute(sess, copyGroupUnixGroupName);

	}

	public List<Group> findCollisionGroupsWithSamgeGroupName(PerunSessionImpl sess, Group group, String namespace) throws InternalErrorException, WrongAttributeAssignmentException, AttributeNotExistsException {
		Utils.notNull(sess, "perunSessionImpl");
		Utils.notNull(group, "group");
		Utils.notNull(namespace, "namespace");
		Attribute groupUnixGroupName = getPerunBl().getAttributesManagerBl().getAttribute(sess, group, A_G_unixGroupName_namespace + ":" + namespace);
		List<Group> groupsWithsameGroupName = getPerunBl().getGroupsManagerBl().getGroupsByAttribute(sess, groupUnixGroupName);
		groupsWithsameGroupName.remove(group);
		return groupsWithsameGroupName;
	}

	public List<Resource> findCollisionResourcesWithSameGid(PerunSessionImpl sess, Resource resource, String namespace) throws InternalErrorException, WrongAttributeAssignmentException, AttributeNotExistsException {
		Utils.notNull(sess, "perunSessionImpl");
		Utils.notNull(resource, "resource");
		Utils.notNull(namespace, "namespace");
		Attribute resourceUnixGid = getPerunBl().getAttributesManagerBl().getAttribute(sess, resource, A_R_unixGID_namespace + ":" + namespace);
		List<Resource> resourcesWithSameGid = getPerunBl().getResourcesManagerBl().getResourcesByAttribute(sess, resourceUnixGid);
		resourcesWithSameGid.remove(resource);
		return resourcesWithSameGid;
	}

	public List<Resource> findCollisionResourcesWithSameGid(PerunSessionImpl sess, Group group, String namespace) throws InternalErrorException, WrongAttributeAssignmentException, AttributeNotExistsException {
		Utils.notNull(sess, "perunSessionImpl");
		Utils.notNull(group, "group");
		Utils.notNull(namespace, "namespace");
		Attribute groupUnixGid = getPerunBl().getAttributesManagerBl().getAttribute(sess, group, A_G_unixGID_namespace + ":" + namespace);
		Attribute copyResourceUnixGid = new Attribute(getPerunBl().getAttributesManagerBl().getAttributeDefinition(sess, A_R_unixGID_namespace + ":" + namespace));
		copyResourceUnixGid.setValue(groupUnixGid.getValue());
		return getPerunBl().getResourcesManagerBl().getResourcesByAttribute(sess, copyResourceUnixGid);
	}

	public List<Group> findCollisionGroupsWithSamgeGroupGid(PerunSessionImpl sess, Resource resource, String namespace) throws InternalErrorException, WrongAttributeAssignmentException, AttributeNotExistsException {
		Utils.notNull(sess, "perunSessionImpl");
		Utils.notNull(resource, "resource");
		Utils.notNull(namespace, "namespace");
		Attribute resourceUnixGid = getPerunBl().getAttributesManagerBl().getAttribute(sess, resource, A_R_unixGID_namespace + ":" + namespace);
		Attribute copyGroupUnixGid = new Attribute(getPerunBl().getAttributesManagerBl().getAttributeDefinition(sess, A_G_unixGID_namespace + ":" + namespace));
		copyGroupUnixGid.setValue(resourceUnixGid.getValue());
		return getPerunBl().getGroupsManagerBl().getGroupsByAttribute(sess, copyGroupUnixGid);
	}

	public List<Group> findCollisionGroupsWithSamgeGroupGid(PerunSessionImpl sess, Group group, String namespace) throws InternalErrorException, WrongAttributeAssignmentException, AttributeNotExistsException {
		Utils.notNull(sess, "perunSessionImpl");
		Utils.notNull(group, "group");
		Utils.notNull(namespace, "namespace");
		Attribute groupUnixGid = getPerunBl().getAttributesManagerBl().getAttribute(sess, group, A_G_unixGID_namespace + ":" + namespace);
		List<Group> groupsWithSameUnixGid = getPerunBl().getGroupsManagerBl().getGroupsByAttribute(sess, groupUnixGid);
		groupsWithSameUnixGid.remove(group);
		return groupsWithSameUnixGid;
	}

	public boolean hasAccessToWriteToAttributeForAnyResource(PerunSessionImpl sess, AttributeDefinition attrDef, List<Resource> resources) throws InternalErrorException {
		Utils.notNull(sess, "perunSessionImpl");
		Utils.notNull(attrDef, "attributeDefinition");
		if(resources == null || resources.isEmpty()) return false;
		for(Resource r: resources) {
			if(AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, attrDef , r, null)) return true;
		}
		return false;
	}

	public boolean hasAccessToWriteToAttributeForAnyGroup(PerunSessionImpl sess, AttributeDefinition attrDef, List<Group> groups) throws InternalErrorException {
		Utils.notNull(sess, "perunSessionImpl");
		Utils.notNull(attrDef, "attributeDefinition");
		if(groups == null || groups.isEmpty()) return false;
		for(Group g: groups) {
			if(AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, attrDef, g, null)) return true;
		}
		return false;
	}

	public Pair<Integer, Integer> getMinAndMaxGidForNamespace(PerunSessionImpl sess, String namespace) throws InternalErrorException, AttributeNotExistsException, WrongAttributeAssignmentException {
		Utils.notNull(sess, "perunSessionImpl");
		Utils.notNull(namespace, "namespace");
		Attribute minGidAttribute = sess.getPerunBl().getAttributesManagerBl().getAttribute(sess, namespace, A_E_namespace_minGID);
		Integer minGid = null;
		if(minGidAttribute.getValue() != null) minGid = (Integer) minGidAttribute.getValue();

		Attribute maxGidAttribute = sess.getPerunBl().getAttributesManagerBl().getAttribute(sess, namespace, A_E_namespace_maxGID);
		Integer maxGid = null;
		if(maxGidAttribute.getValue() != null) maxGid = (Integer) maxGidAttribute.getValue();

		return new Pair(minGid, maxGid);
	}

	public Integer getFirstFreeGidForResourceOrGroup(PerunSessionImpl sess, String namespace) throws InternalErrorException, AttributeNotExistsException, WrongAttributeAssignmentException {
		Utils.notNull(sess, "perunSessionImpl");
		Utils.notNull(namespace, "namespace");
		Pair<Integer, Integer> minAndMaxGid = this.getMinAndMaxGidForNamespace(sess, namespace);

		//If there is no min or max gid, return null instead of number, its same like no free gid was able
		if(minAndMaxGid == null || minAndMaxGid.getLeft() == null || minAndMaxGid.getRight() == null) return null;

		AttributeDefinition resourceUnixGid = getPerunBl().getAttributesManagerBl().getAttributeDefinition(sess, A_R_unixGID_namespace + ":" + namespace);
		List<Object> allGids = sess.getPerunBl().getAttributesManagerBl().getAllValues(sess, resourceUnixGid);
		AttributeDefinition groupUnixGid = getPerunBl().getAttributesManagerBl().getAttributeDefinition(sess, A_G_unixGID_namespace + ":" + namespace);
		allGids.addAll(sess.getPerunBl().getAttributesManagerBl().getAllValues(sess, groupUnixGid)); //note: it doesn't matter if the group is not active (isUnixGroup attribute != 1)

		for(int i = minAndMaxGid.getLeft(); i < minAndMaxGid.getRight(); i++) {
			if(!allGids.contains(i)) {
				return i;
			}
		}
		return null;
	}

	public void checkIfGIDIsWithinRange(PerunSessionImpl sess, Attribute attribute) throws InternalErrorException, WrongReferenceAttributeValueException, WrongAttributeAssignmentException, AttributeNotExistsException, WrongAttributeValueException {
		Utils.notNull(attribute, "attribute");
		Integer gid = null;
		if(attribute.getValue() != null) gid = (Integer) attribute.getValue();

		if(gid == null) throw new WrongAttributeValueException(attribute, "Gid with null value is not allowed.");

		String gidNamespace = attribute.getFriendlyNameParameter();

		Attribute minGidAttribute = sess.getPerunBl().getAttributesManagerBl().getAttribute(sess, gidNamespace, A_E_namespace_minGID);
		if(minGidAttribute.getValue() == null) throw new WrongReferenceAttributeValueException(attribute, minGidAttribute);
		Integer minGid = (Integer) minGidAttribute.getValue();

		Attribute maxGidAttribute = sess.getPerunBl().getAttributesManagerBl().getAttribute(sess, gidNamespace, A_E_namespace_maxGID);
		if(maxGidAttribute.getValue() == null) throw new WrongReferenceAttributeValueException(attribute, maxGidAttribute);
		Integer maxGid = (Integer) maxGidAttribute.getValue();

		if ( gid < minGid || gid > maxGid ) {
			throw new WrongAttributeValueException(attribute,"GID number is not in allowed values min: "+minGid+", max:"+maxGid);
		}
	}

	public void checkIfListOfGIDIsWithinRange(PerunSessionImpl sess, User user, Attribute attribute) throws InternalErrorException, WrongReferenceAttributeValueException, WrongAttributeAssignmentException, AttributeNotExistsException, WrongAttributeValueException {
		Utils.notNull(attribute, "attribute");
		List<String> gIDs = (List<String>)attribute.getValue();
		if (gIDs != null){
			for(String sGid : gIDs){
				try{
					Integer gid = new Integer(sGid);
					String gidNamespace = attribute.getFriendlyNameParameter();

					Attribute minGidAttribute = sess.getPerunBl().getAttributesManagerBl().getAttribute(sess, gidNamespace, A_E_namespace_minGID);
					if(minGidAttribute.getValue() == null) throw new WrongReferenceAttributeValueException(attribute, minGidAttribute, "Attribute minGid cannot be null");
					Integer minGid = (Integer) minGidAttribute.getValue();

					Attribute maxGidAttribute = sess.getPerunBl().getAttributesManagerBl().getAttribute(sess, gidNamespace, A_E_namespace_maxGID);
					if(maxGidAttribute.getValue() == null) throw new WrongReferenceAttributeValueException(attribute, maxGidAttribute, "Attribute maxGid cannot be null");
					Integer maxGid = (Integer) maxGidAttribute.getValue();

					if ( gid < minGid || gid > maxGid ) {
						throw new WrongAttributeValueException(attribute,"GID number is not in allowed values min: "+minGid+", max:"+maxGid);
					}
				}catch(NumberFormatException ex){
					throw new WrongAttributeValueException(attribute ,user,"attribute is not a number", ex);
				}
			}
		}
	}

	public Integer getFreeGID(PerunSessionImpl sess, Attribute attribute) throws InternalErrorException, AttributeNotExistsException, WrongAttributeAssignmentException {
		Utils.notNull(attribute, "attribute");
		String gidNamespace = attribute.getFriendlyNameParameter();

		Attribute minGidAttribute = sess.getPerunBl().getAttributesManagerBl().getAttribute(sess, gidNamespace, A_E_namespace_minGID);
		if(minGidAttribute.getValue() == null) return 0;
		Integer minGid = (Integer) minGidAttribute.getValue();

		Attribute maxGidAttribute = sess.getPerunBl().getAttributesManagerBl().getAttribute(sess, gidNamespace, A_E_namespace_maxGID);
		if(maxGidAttribute.getValue() == null) return 0;
		Integer maxGid = (Integer) maxGidAttribute.getValue();

		List<Integer> allGids = new ArrayList<>();
		Attribute usedGids = sess.getPerunBl().getAttributesManagerBl().getAttribute(sess, gidNamespace, A_E_usedGids);

		if(usedGids.getValue() == null) return minGid;
		else {
			Map<String,String> usedGidsValue = (Map<String, String>) usedGids.getValue();
			Set<String> keys = usedGidsValue.keySet();
			
			for(String key: keys) {
				allGids.add(Integer.parseInt(usedGidsValue.get(key)));
			}
		}
		
		for(int i = minGid; i < maxGid; i++) {
			if(!allGids.contains(i)) {
				return i;
			}
		}
		
		return null;
	}

	public Integer getCommonGIDOfGroupsWithSameNameInSameNamespace(PerunSessionImpl sess, List<Group> groupsWithSameGroupNameInSameNamespace, String gidNamespace, Integer commonGID) throws InternalErrorException, WrongAttributeAssignmentException {
		if(groupsWithSameGroupNameInSameNamespace == null || groupsWithSameGroupNameInSameNamespace.isEmpty()) return null;
		Utils.notNull(gidNamespace, "gidNamespace");

		Group commonGIDGroup = null;  //only for more verbose exception messages
		for(Group g: groupsWithSameGroupNameInSameNamespace) {
			try {
				Attribute attr = sess.getPerunBl().getAttributesManagerBl().getAttribute(sess, g, A_G_unixGID_namespace + ":" + gidNamespace);
				if(attr.getValue() != null) {
					if(commonGID == null) {
						commonGIDGroup = g;
						commonGID = (Integer) attr.getValue();
					} else {
						if(!commonGID.equals((Integer) attr.getValue())) throw new ConsistencyErrorException("There are at least 1 groups/resources with same GroupName in same namespace but with different GID in same namespaces. Conflict found: "  + g + "(gid=" + attr.getValue()+ ") and " + commonGIDGroup + "(gid=" + commonGID + ")");
					}
				}
			} catch (AttributeNotExistsException ex) {
				throw new ConsistencyErrorException(ex);
			}
		}

		return commonGID;
	}

	public Integer getCommonGIDOfResourcesWithSameNameInSameNamespace(PerunSessionImpl sess, List<Resource> resourcesWithSameGroupNameInSameNamespace, String gidNamespace, Integer commonGID) throws InternalErrorException, WrongAttributeAssignmentException {
		if(resourcesWithSameGroupNameInSameNamespace == null || resourcesWithSameGroupNameInSameNamespace.isEmpty()) return null;
		Utils.notNull(gidNamespace,"gidNamespace");

		Resource commonGIDResource = null;   //only for more verbose exception messages
		for(Resource r: resourcesWithSameGroupNameInSameNamespace) {
			try {
				Attribute attr = sess.getPerunBl().getAttributesManagerBl().getAttribute(sess, r, A_R_unixGID_namespace + ":" + gidNamespace);
				if(attr.getValue() != null) {
					if(commonGID == null) {
						commonGIDResource = r;
						commonGID = (Integer) attr.getValue();
					} else {
						if(!commonGID.equals((Integer) attr.getValue())) throw new ConsistencyErrorException("There are at least 1 groups/resources with same GroupName in same namespace but with different GID in same namespaces. Conflict found: " + r + "(gid=" + attr.getValue()+ ") and " + commonGIDResource + "(gid=" + commonGID + ")");
					}
				}
			} catch (AttributeNotExistsException ex) {
				throw new ConsistencyErrorException(ex);
			}
		}

		return commonGID;
	}

	public int haveTheSameAttributeWithTheSameNamespace(PerunSessionImpl sess, Group group, Attribute attr) throws InternalErrorException, WrongAttributeAssignmentException {
		Utils.notNull(group, "group");
		Utils.notNull(attr, "attr");

		String attributeNamespace = attr.getFriendlyNameParameter();
		if(attributeNamespace == null || attributeNamespace.isEmpty()) throw new InternalErrorException("Attribute has no namespace, this method can't be use.");

		try {
			Attribute testingAttribute = sess.getPerunBl().getAttributesManagerBl().getAttribute(sess, group, attr.getName());
			if(testingAttribute.getValue() == null) return -1;
			else {
				if(!testingAttribute.getValue().equals(attr.getValue())) return 1;
			}
		} catch (AttributeNotExistsException ex) {
			throw new ConsistencyErrorException(ex);
		}
		return 0;
	}

	public int haveTheSameAttributeWithTheSameNamespace(PerunSessionImpl sess, Resource resource, Attribute attr) throws InternalErrorException, WrongAttributeAssignmentException{
		Utils.notNull(resource, "resource");
		Utils.notNull(attr, "attr");

		String attributeNamespace = attr.getFriendlyNameParameter();
		if(attributeNamespace == null || attributeNamespace.isEmpty()) throw new InternalErrorException("Attribute has no namespace, this method can't be use.");

		try {
			Attribute testingAttribute = sess.getPerunBl().getAttributesManagerBl().getAttribute(sess, resource, attr.getName());
			if(testingAttribute.getValue() == null) return -1;
			else {
				if(!testingAttribute.getValue().equals(attr.getValue())) return 1;
			}
		} catch (AttributeNotExistsException ex) {
			throw new ConsistencyErrorException(ex);
		}
		return 0;
	}

	public boolean haveRightToWriteAttributeInAnyGroupOrResource(PerunSessionImpl sess, List<Group> groups, List<Resource> resources, AttributeDefinition groupAttribute, AttributeDefinition resourceAttribute) throws InternalErrorException {
		if(groups != null && !groups.isEmpty() && groupAttribute != null) {
			for(Group g: groups) {
				if(AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, groupAttribute, g, null)) return true;
			}
		}

		if(resources != null && !resources.isEmpty() && resourceAttribute != null) {
			for(Resource r: resources) {
				if(AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, resourceAttribute, r, null)) return true;
			}
		}

		return false;
	}

	public List<Attribute> getListOfResourceGIDsFromListOfGroupGIDs(PerunSessionImpl sess, List<Attribute> groupGIDs) throws InternalErrorException, AttributeNotExistsException {
		List<Attribute> resourceGIDs = new ArrayList<Attribute>();
		if(groupGIDs == null || groupGIDs.isEmpty()) {
			return resourceGIDs;
		}

		for(Attribute a: groupGIDs) {
			Attribute resourceGID = new Attribute(sess.getPerunBl().getAttributesManagerBl().getAttributeDefinition(sess, A_R_unixGID_namespace + ":" + a.getFriendlyNameParameter()));
			resourceGID.setValue(a.getValue());
			resourceGIDs.add(resourceGID);
		}

		return resourceGIDs;
	}

	public List<Attribute> getListOfGroupGIDsFromListOfResourceGIDs(PerunSessionImpl sess, List<Attribute> resourceGIDs) throws InternalErrorException, AttributeNotExistsException {
		List<Attribute> groupGIDs = new ArrayList<Attribute>();
		if(resourceGIDs == null || resourceGIDs.isEmpty()) {
			return groupGIDs;
		}

		for(Attribute a: resourceGIDs) {
			Attribute groupGID = new Attribute(sess.getPerunBl().getAttributesManagerBl().getAttributeDefinition(sess, A_G_unixGID_namespace + ":" + a.getFriendlyNameParameter()));
			groupGID.setValue(a.getValue());
			groupGIDs.add(groupGID);
		}

		return groupGIDs;
	}

	public Set<String> getSetOfGIDNamespacesWhereFacilitiesHasTheSameGroupNameNamespace(PerunSessionImpl sess, List<Facility> facilities, Attribute unixGroupNameNamespace) throws InternalErrorException, WrongAttributeAssignmentException, WrongReferenceAttributeValueException {
		Set<String> gidNamespaces = new HashSet<String>();
		if(facilities == null || facilities.isEmpty()) return gidNamespaces;
		Utils.notNull(facilities, "facilities");

		for(Facility f: facilities) {
			Attribute facilityGroupNameNamespace;
			try {
				facilityGroupNameNamespace = sess.getPerunBl().getAttributesManagerBl().getAttribute(sess, f, A_F_unixGroupName_namespace);
				if(facilityGroupNameNamespace.getValue() != null) {
					//if they are same, save GID-namespace from this facility to hashSet
					if(unixGroupNameNamespace.getFriendlyNameParameter().equals((String) facilityGroupNameNamespace.getValue())) {
						Attribute facilityGIDNamespace = sess.getPerunBl().getAttributesManagerBl().getAttribute(sess, f, A_F_unixGID_namespace);
						//If facilityGIDNamespace exists and is not null, save to the hashSet of gidNamespaces
						if(facilityGIDNamespace.getValue() != null) {
							gidNamespaces.add((String) facilityGIDNamespace.getValue());
						}
					}
				}
			} catch (AttributeNotExistsException ex) {
				throw new ConsistencyErrorException(ex);
			}
		}

		return gidNamespaces;
	}

	public Set<String> getSetOfGroupNameNamespacesWhereFacilitiesHasTheSameGIDNamespace(PerunSessionImpl sess, List<Facility> facilities, Attribute unixGIDNamespace) throws InternalErrorException, WrongAttributeAssignmentException, WrongReferenceAttributeValueException {
		Set<String> groupNameNamespaces = new HashSet<String>();
		if(facilities == null || facilities.isEmpty()) return groupNameNamespaces;
		Utils.notNull(unixGIDNamespace, "unixGIDNamespace");

		for(Facility f: facilities) {
			Attribute facilityGIDNamespace;
			try {
				facilityGIDNamespace = sess.getPerunBl().getAttributesManagerBl().getAttribute(sess, f, A_F_unixGID_namespace);
				if(facilityGIDNamespace.getValue() != null) {
					//if they are same, save GroupName-namespace from this facility to hashSet
					if(unixGIDNamespace.getFriendlyNameParameter().equals((String) facilityGIDNamespace.getValue())) {
						Attribute facilityGroupNameNamespace = sess.getPerunBl().getAttributesManagerBl().getAttribute(sess, f, A_F_unixGroupName_namespace);
						//If facilityGroupNameNamespace exists and is not null, save to the hashSet of gidNamespaces
						if(facilityGroupNameNamespace.getValue() != null) {
							groupNameNamespaces.add((String) facilityGroupNameNamespace.getValue());
						} else {
							throw new WrongReferenceAttributeValueException(unixGIDNamespace, facilityGroupNameNamespace, "Facility has gidNamespace set, but groupNameNamespace not set.");
						}
					}
				}
			} catch (AttributeNotExistsException ex) {
				throw new ConsistencyErrorException(ex);
			}
		}

		return groupNameNamespaces;
	}

	public void checkReservedNames(Attribute groupName) throws WrongAttributeValueException {
		if(groupName == null) return;
		if(reservedNamesForUnixGroups.contains(groupName.getValue())) throw new WrongAttributeValueException(groupName, "This groupName is reserved.");
	}

	public Attribute getUnixGroupNameNamespaceAttributeWithNotNullValue(PerunSessionImpl sess, Resource resource) throws InternalErrorException, WrongReferenceAttributeValueException {
		Facility facility = sess.getPerunBl().getResourcesManagerBl().getFacility(sess, resource);
		try {
			Attribute unixGroupNameNamespaceAttribute = sess.getPerunBl().getAttributesManagerBl().getAttribute(sess, facility, A_F_unixGroupName_namespace);
			if(unixGroupNameNamespaceAttribute.getValue() == null) throw new WrongReferenceAttributeValueException(unixGroupNameNamespaceAttribute);
			return unixGroupNameNamespaceAttribute;
		} catch(AttributeNotExistsException ex) {
			throw new ConsistencyErrorException(ex);
		} catch(WrongAttributeAssignmentException ex) {
			throw new InternalErrorException(ex);
		}
	}

	public Attribute getUnixGIDNamespaceAttributeWithNotNullValue(PerunSessionImpl sess, Resource resource) throws InternalErrorException, WrongReferenceAttributeValueException {
		Facility facility = sess.getPerunBl().getResourcesManagerBl().getFacility(sess, resource);
		try {
			Attribute unixGIDNamespaceAttribute = sess.getPerunBl().getAttributesManagerBl().getAttribute(sess, facility, A_F_unixGID_namespace);
			if(unixGIDNamespaceAttribute.getValue() == null) throw new WrongReferenceAttributeValueException(unixGIDNamespaceAttribute);
			return unixGIDNamespaceAttribute;
		} catch(AttributeNotExistsException ex) {
			throw new ConsistencyErrorException(ex);
		} catch(WrongAttributeAssignmentException ex) {
			throw new InternalErrorException(ex);
		}
	}

	public boolean isGroupUnixGIDNamespaceFillable(PerunSessionImpl sess, Group group, Attribute groupUnixGIDNamespace) throws InternalErrorException, WrongReferenceAttributeValueException, WrongAttributeAssignmentException {
		Utils.notNull(group, "group");
		Utils.notNull(groupUnixGIDNamespace, "groupUnixGIDNamespace");

		//Get All Facilities from group
		Set<Facility> facilitiesOfGroup = new HashSet<Facility>();
		List<Resource> resourcesOfGroup = sess.getPerunBl().getResourcesManagerBl().getAssignedResources(sess, group);
		for(Resource r: resourcesOfGroup) {
			facilitiesOfGroup.add(sess.getPerunBl().getResourcesManagerBl().getFacility(sess, r));
		}

		//Prepare list of gid namespaces of all facilities which have the same groupName namespace like this unixGroupName namespace
		Set<String> groupNameNamespaces = this.getSetOfGroupNameNamespacesWhereFacilitiesHasTheSameGIDNamespace(sess, new ArrayList<Facility>(facilitiesOfGroup), groupUnixGIDNamespace);

		if(!groupNameNamespaces.isEmpty()) {
			for(String s: groupNameNamespaces) {
				try {
					Attribute groupNameNamespace = sess.getPerunBl().getAttributesManagerBl().getAttribute(sess, group, A_G_unixGroupName_namespace + ":" + s);
					if(groupNameNamespace.getValue() != null) {
						return true;
					}
				} catch (AttributeNotExistsException ex) {
					throw new ConsistencyErrorException(ex);
				}
			}
		}
		return false;
	}

	@Override
	public boolean isNameOfEmailValid(PerunSessionImpl sess, String email) {
		if (email == null) return false;

		Pattern emailPattern = Pattern.compile("^[-_A-Za-z0-9+]+(\\.[-_A-Za-z0-9]+)*@[-A-Za-z0-9]+(\\.[-A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$");
		Matcher emailMatcher = emailPattern.matcher(email);
		if (emailMatcher.find()) return true;

		return false;
	}

	public void checkFormatOfShell(String shell, Attribute attribute) throws WrongAttributeValueException {
		//previous regex ^/[-a-zA-Z0-9_/]*$"
		Pattern pattern = Pattern.compile("^(/[-_a-zA-Z0-9]+)+$");

		Matcher match = pattern.matcher(shell);

		if (!match.matches()) {
			throw new WrongAttributeValueException(attribute, "Bad shell attribute format " + shell);
		}

	}

	public PerunBl getPerunBl() {
		return this.perunBl;
	}

	public void setPerunBl(PerunBl perunBl) {
		this.perunBl = perunBl;
	}
}
