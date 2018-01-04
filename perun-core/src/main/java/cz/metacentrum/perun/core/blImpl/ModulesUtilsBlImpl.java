package cz.metacentrum.perun.core.blImpl;

import cz.metacentrum.perun.auditparser.AuditParser;
import cz.metacentrum.perun.core.api.*;
import cz.metacentrum.perun.core.api.exceptions.*;
import cz.metacentrum.perun.core.bl.ModulesUtilsBl;
import cz.metacentrum.perun.core.bl.PerunBl;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;
import cz.metacentrum.perun.core.impl.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import java.util.stream.Collectors;

/**
 * @author Michal Stava <stavamichal@gmail.com>
 */
public class ModulesUtilsBlImpl implements ModulesUtilsBl {

	final static Logger log = LoggerFactory.getLogger(ServicesManagerBlImpl.class);
	private PerunBl perunBl;
	Map<String,String> perunNamespaces = null;

	public static final String A_E_namespace_minGID = AttributesManager.NS_ENTITYLESS_ATTR_DEF + ":namespace-minGID";
	public static final String A_E_namespace_maxGID = AttributesManager.NS_ENTITYLESS_ATTR_DEF + ":namespace-maxGID";
	public static final String A_G_unixGID_namespace = AttributesManager.NS_GROUP_ATTR_DEF + ":unixGID-namespace";
	public static final String A_G_unixGroupName_namespace = AttributesManager.NS_GROUP_ATTR_DEF + ":unixGroupName-namespace";
	public static final String A_R_unixGID_namespace = AttributesManager.NS_RESOURCE_ATTR_DEF + ":unixGID-namespace";
	public static final String A_R_unixGroupName_namespace = AttributesManager.NS_RESOURCE_ATTR_DEF + ":unixGroupName-namespace";
	public static final String A_F_unixGID_namespace = AttributesManager.NS_FACILITY_ATTR_DEF + ":unixGID-namespace";
	public static final String A_F_unixGroupName_namespace = AttributesManager.NS_FACILITY_ATTR_DEF + ":unixGroupName-namespace";
	public static final String A_F_googleGroupName_namespace = AttributesManager.NS_FACILITY_ATTR_DEF + ":googleGroupNameNamespace";
	private static final String A_E_usedGids = AttributesManager.NS_ENTITYLESS_ATTR_DEF + ":usedGids";

	//Often used patterns
	public static final Pattern quotaWithMetricsPattern = Pattern.compile("^([0-9]+([.][0-9]+)?[KMGTPE]?):([0-9]+([.][0-9]+)?[KMGTPE]?)$");
	public static final Pattern quotaWithoutMetricsPattern = Pattern.compile("^([0-9]+)(:)([0-9]+)$");
	public static final Pattern numberPattern = Pattern.compile("[0-9]+([.][0-9]+)?");
	public static final Pattern letterPattern = Pattern.compile("[A-Z]");
	public static final Pattern fqdnPattern = Pattern.compile("^((?!-)[a-zA-Z0-9-]{1,63}(?<!-)\\.)+[a-zA-Z]{2,63}\\.?$");

	public final static List<String> reservedNamesForUnixGroups = Arrays.asList("root", "daemon", "tty", "bin", "sys", "sudo", "nogroup",
	          "hadoop", "hdfs", "mapred", "yarn", "hsqldb", "derby", "jetty", "hbase", "zookeeper", "users", "oozie", "hive");
	public final static List<String> unpermittedNamesForUserLogins = Arrays.asList("arraysvcs", "at", "backup", "bin", "daemon", "Debian-exim", "flexlm", "ftp", "games",
		        "gdm", "glite", "gnats", "haldaemon", "identd", "irc", "libuuid", "list", "lp", "mail", "man",
		        "messagebus", "news", "nobody", "ntp", "openslp", "pcp", "polkituser", "postfix", "proxy",
		        "pulse", "puppet", "root", "saned", "smmsp", "smmta", "sshd", "statd", "suse-ncc", "sync",
		        "sys", "uucp", "uuidd", "www-data", "wwwrun", "zenssh", "tomcat6", "tomcat7", "tomcat8",
		        "nn", "dn", "rm", "nm", "sn", "jn", "jhs", "http", "yarn", "hdfs", "mapred", "hadoop", "hsqldb", "derby",
		        "jetty", "hbase", "zookeeper", "hive", "hue", "oozie", "httpfs");

	//Definition of K = KB, M = MB etc.
	public static final long M = 1024;
	public static final long G = M * 1024;
	public static final long T = G * 1024;
	public static final long P = T * 1024;
	public static final long E = P * 1024;

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
		//If there are no groups, return commonGID from param (it can be null)
		if(groupsWithSameGroupNameInSameNamespace == null || groupsWithSameGroupNameInSameNamespace.isEmpty()) return commonGID;
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
		//If there are no resources, return commonGID from param (it can be null)
		if(resourcesWithSameGroupNameInSameNamespace == null || resourcesWithSameGroupNameInSameNamespace.isEmpty()) return commonGID;
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

	public void checkReservedUnixGroupNames(Attribute groupNameAttribute) throws InternalErrorException, WrongAttributeValueException {
		if(groupNameAttribute == null) return;
		checkPerunNamespacesMap();

		String reservedNames = perunNamespaces.get(groupNameAttribute.getFriendlyName() + ":reservedNames");
		if (reservedNames != null) {
			List<String> reservedNamesList = Arrays.asList(reservedNames.split("\\s*,\\s*"));
			if (reservedNamesList.contains(groupNameAttribute.getValue()))
				throw new WrongAttributeValueException(groupNameAttribute, "This groupName is reserved.");
		} else {
			//Property not found in our attribute map, so we will use the default hardcoded values instead
			if (reservedNamesForUnixGroups.contains(groupNameAttribute.getValue()))
				throw new WrongAttributeValueException(groupNameAttribute, "This groupName is reserved.");
		}
	}

	public void checkUnpermittedUserLogins(Attribute loginAttribute) throws InternalErrorException, WrongAttributeValueException {
		if(loginAttribute == null) return;
		checkPerunNamespacesMap();

		String unpermittedNames = perunNamespaces.get(loginAttribute.getFriendlyName() + ":reservedNames");
		if (unpermittedNames != null) {
			List<String> unpermittedNamesList = Arrays.asList(unpermittedNames.split("\\s*,\\s*"));
			if (unpermittedNamesList.contains(loginAttribute.getValue()))
				throw new WrongAttributeValueException(loginAttribute, "This login is not permitted.");
		} else {
			//Property not found in our attribute map, so we will use the default hardcoded values instead
			if (unpermittedNamesForUserLogins.contains(loginAttribute.getValue()))
				throw new WrongAttributeValueException(loginAttribute, "This login is not permitted.");
		}
	}

	@Override
	public Attribute getGoogleGroupNameNamespaceAttributeWithNotNullValue(PerunSessionImpl sess, Resource resource) throws InternalErrorException, WrongReferenceAttributeValueException {
		Facility facility = sess.getPerunBl().getResourcesManagerBl().getFacility(sess, resource);
		try {
			Attribute googleGroupNameNamespaceAttribute = sess.getPerunBl().getAttributesManagerBl().getAttribute(sess, facility, A_F_googleGroupName_namespace);
			if(googleGroupNameNamespaceAttribute.getValue() == null) throw new WrongReferenceAttributeValueException(googleGroupNameNamespaceAttribute);
			return googleGroupNameNamespaceAttribute;
		} catch(AttributeNotExistsException ex) {
			throw new ConsistencyErrorException(ex);
		} catch(WrongAttributeAssignmentException ex) {
			throw new InternalErrorException(ex);
		}
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

		Matcher emailMatcher = Utils.emailPattern.matcher(email);
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

	public void checkAttributeRegex(Attribute attribute, String defaultRegex) throws InternalErrorException, WrongAttributeValueException {
		if (attribute == null || attribute.getValue() == null) throw new InternalErrorException("Attribute or it's value is null.");
		String attributeValue = (String) attribute.getValue();
		checkPerunNamespacesMap();

		String regex = perunNamespaces.get(attribute.getFriendlyName() + ":regex");
		if (regex != null) {
			//Check if regex is valid
			try {
				Pattern.compile(regex);
			} catch (PatternSyntaxException e) {
				log.error("Regex pattern \"" + regex + "\" from \"" + attribute.getFriendlyName() + ":regex\"" + " property of perun-namespaces.properties file is invalid.");
				throw new InternalErrorException("Regex pattern \"" + regex + "\" from \"" + attribute.getFriendlyName() + ":regex\"" + " property of perun-namespaces.properties file is invalid.");
			}
			if(!attributeValue.matches(regex)) {
				throw new WrongAttributeValueException(attribute, "Wrong format. Regex: \"" + regex +"\" expected for this attribute:");
			}
		} else {
			//Regex property not found in our attribute map, so use the default hardcoded regex
			if (defaultRegex == null) return;
			if (!attributeValue.matches(defaultRegex)) {
				throw new WrongAttributeValueException(attribute, "Wrong format. Regex: \"" + defaultRegex +"\" expected for this attribute:");
			}
		}
	}

	/**
	 * Internal protected method.
	 * Checks this.perunNamespaces map, which is always initialized as null.
	 * If null, it tries to load the configuration into this map from a perun-namespaces.properties file.
	 * If the file does not exist, it creates an empty HashMap, so it's not null anymore.
	 */
	protected void checkPerunNamespacesMap() {
		if (perunNamespaces == null) {
			try {
				perunNamespaces = BeansUtils.getAllPropertiesFromCustomConfiguration("perun-namespaces.properties");
			} catch (InternalErrorException e) {
				perunNamespaces = new HashMap<>();
			}
		}
	}

	@Override
	public void checkIfQuotasIsInLimit(Map<String, Pair<BigDecimal, BigDecimal>> quotaToCheck, Map<String, Pair<BigDecimal, BigDecimal>> limitQuota) throws QuotaNotInAllowedLimitException, InternalErrorException {
		if(quotaToCheck == null) throw new InternalErrorException("Quota to check can't be null.");
		if(limitQuota == null) throw new InternalErrorException("Limit quota can't be null.");

		//If there is no value to check, then everything is in limit (we don't need to limit anything)
		if(quotaToCheck.isEmpty()) return;

		//test every record of quotaToCheck against record in limitQuota
		for(String volumeToCheck: quotaToCheck.keySet()) {
			if(!limitQuota.containsKey(volumeToCheck)) {
				throw new QuotaNotInAllowedLimitException(quotaToCheck, limitQuota, "Volume " + volumeToCheck + " is missing in limitQuota.");
			}

			Pair<BigDecimal, BigDecimal> volumeToCheckQuotas = quotaToCheck.get(volumeToCheck);
			Pair<BigDecimal, BigDecimal> volumeToCheckLimitQuotas = limitQuota.get(volumeToCheck);

			//Check limit of softQuota, zero limit means unlimited so no need for testing
			if(volumeToCheckLimitQuotas.getLeft().compareTo(BigDecimal.ZERO) != 0) {
				if (volumeToCheckQuotas.getLeft().compareTo(BigDecimal.ZERO) == 0 || volumeToCheckQuotas.getLeft().compareTo(volumeToCheckLimitQuotas.getLeft()) > 0) {
					throw new QuotaNotInAllowedLimitException(quotaToCheck, limitQuota, "SoftQuota of volume " + volumeToCheck + " is bigger than limit.");
				}
			}

			//Check limit of hardQuota, zero limit means unlimited so no need for testing
			if(volumeToCheckLimitQuotas.getRight().compareTo(BigDecimal.ZERO) != 0) {
				if (volumeToCheckQuotas.getRight().compareTo(BigDecimal.ZERO) == 0 || volumeToCheckQuotas.getRight().compareTo(volumeToCheckLimitQuotas.getRight()) > 0) {
					throw new QuotaNotInAllowedLimitException(quotaToCheck, limitQuota, "HardQuota of volume " + volumeToCheck + " is bigger than limit.");
				}
			}
		}
	}

	@Override
	public Map<String, Pair<BigDecimal, BigDecimal>> checkAndTransferQuotas(Attribute quotasAttribute, PerunBean firstPlaceholder, PerunBean secondPlaceholder, boolean withMetrics) throws InternalErrorException, WrongAttributeValueException {
		//firstPlaceholder can't be null
		if(firstPlaceholder == null) throw new InternalErrorException("Missing first mandatory placeHolder (PerunBean).");
		//If quotas attribute is null or it's value is empty, return empty hash map
		if(quotasAttribute == null || quotasAttribute.getValue() == null) return new HashMap<>();

		//Prepare result container and value of attribute
		Map<String, Pair<BigDecimal, BigDecimal>> transferedQuotas = new HashMap<>();
		Map<String, String> defaultQuotasMap = (Map<String, String>) quotasAttribute.getValue();

		//List to test if all paths are unique (/var/log and /var/log/ are the same so these two paths are not unique)
		List<String> uniquePaths = new ArrayList<>();
		for(String path: defaultQuotasMap.keySet()) {
			//null is not correct path for volume on File System
			if(path == null || path.isEmpty()) throw new WrongAttributeValueException(quotasAttribute, firstPlaceholder, secondPlaceholder, "The path of some volume where quota should be set is null.");

			//testing if path is unique
			String canonicalPath;
			try {
				canonicalPath = new URI(path).normalize().getPath();
				if(!canonicalPath.endsWith("/")) canonicalPath = canonicalPath.concat("/");
			} catch (URISyntaxException ex) {
				throw new WrongAttributeValueException(quotasAttribute, firstPlaceholder, secondPlaceholder, "Path '" + path + "' is not correct form.");
			}

			if(uniquePaths.contains(canonicalPath)) throw new WrongAttributeValueException(quotasAttribute, firstPlaceholder, secondPlaceholder, "Paths are not unique, there are two same paths: " + path);
			else uniquePaths.add(canonicalPath);

			String quota = defaultQuotasMap.get(path);
			//quota can't be null, if exists in attribute, must be set in some way
			if(quota == null) throw new WrongAttributeValueException(quotasAttribute, firstPlaceholder, secondPlaceholder, "The quota of some volume where quota should be set is null.");

			//check format of quota parameter (for data with metrics, for count of files without metrics)
			Matcher quotaMatcher;
			if(withMetrics) {
				quotaMatcher = ModulesUtilsBlImpl.quotaWithMetricsPattern.matcher(quota);
				if(!quotaMatcher.matches()) throw new WrongAttributeValueException(quotasAttribute, firstPlaceholder, secondPlaceholder, "Format of quota in quotas attribute is not correct.");
			} else {
				quotaMatcher = ModulesUtilsBlImpl.quotaWithoutMetricsPattern.matcher(quota);
				if(!quotaMatcher.matches()) throw new WrongAttributeValueException(quotasAttribute, firstPlaceholder, secondPlaceholder, "Format of quota in quotas attribute is not correct.");
			}

			//Parse quotas to variables
			String softQuota = quotaMatcher.group(1);
			String hardQuota = quotaMatcher.group(3);

			//Parse number pattern and letter pattern from whole quotas

			//SoftQuotaNumber
			BigDecimal softQuotaAfterTransfer;
			BigDecimal hardQuotaAfterTransfer;
			//special behavior with metrics
			if(withMetrics) {
				String softQuotaNumber = null;
				Matcher numberMatcher = numberPattern.matcher(softQuota);
				if(!numberMatcher.find()) throw new ConsistencyErrorException("Matcher can't find number in softQuota '" + softQuota + "' in attribute " + quotasAttribute);
				softQuotaNumber = numberMatcher.group();

				//SoftQuotaLetter
				String softQuotaLetter = null;
				Matcher letterMatcher = letterPattern.matcher(softQuota);
				//in this case no letter means default and default is G
				if(!letterMatcher.find()) softQuotaLetter = "G";
				else softQuotaLetter = letterMatcher.group();

				//HardQuotaNumber
				String hardQuotaNumber = null;
				numberMatcher = numberPattern.matcher(hardQuota);
				if(!numberMatcher.find()) throw new ConsistencyErrorException("Matcher can't find number in hardQuota '" + hardQuota + "' in attribute " + quotasAttribute);
				hardQuotaNumber = numberMatcher.group();

				//HardQuotaLetter
				String hardQuotaLetter;
				letterMatcher = letterPattern.matcher(hardQuota);
				//in this case no letter means default and default is G
				if(!letterMatcher.find()) hardQuotaLetter = "G";
				else hardQuotaLetter = letterMatcher.group();

				//Prepare whole big decimal numbers
				softQuotaAfterTransfer = new BigDecimal(softQuotaNumber);
				hardQuotaAfterTransfer = new BigDecimal(hardQuotaNumber);

				//multiplying for softQuota
				switch (softQuotaLetter) {
					case "K":
						break; //K is basic metric, no need to multiply it
					case "G":
						softQuotaAfterTransfer = softQuotaAfterTransfer.multiply(BigDecimal.valueOf(G));
						break;
					case "M":
						softQuotaAfterTransfer = softQuotaAfterTransfer.multiply(BigDecimal.valueOf(M));
						break;
					case "T":
						softQuotaAfterTransfer = softQuotaAfterTransfer.multiply(BigDecimal.valueOf(T));
						break;
					case "P":
						softQuotaAfterTransfer = softQuotaAfterTransfer.multiply(BigDecimal.valueOf(P));
						break;
					case "E":
						softQuotaAfterTransfer = softQuotaAfterTransfer.multiply(BigDecimal.valueOf(E));
						break;
					default:
						throw new ConsistencyErrorException("There is not allowed character in soft quota letter '" + softQuotaLetter + "'.");
				}

				//multiplying for softQuota
				switch (hardQuotaLetter) {
					case "K":
						break; //K is basic metric, no need to multiply it
					case "G":
						hardQuotaAfterTransfer = hardQuotaAfterTransfer.multiply(BigDecimal.valueOf(G));
						break;
					case "M":
						hardQuotaAfterTransfer = hardQuotaAfterTransfer.multiply(BigDecimal.valueOf(M));
						break;
					case "T":
						hardQuotaAfterTransfer = hardQuotaAfterTransfer.multiply(BigDecimal.valueOf(T));
						break;
					case "P":
						hardQuotaAfterTransfer = hardQuotaAfterTransfer.multiply(BigDecimal.valueOf(P));
						break;
					case "E":
						hardQuotaAfterTransfer = hardQuotaAfterTransfer.multiply(BigDecimal.valueOf(E));
						break;
					default:
						throw new ConsistencyErrorException("There is not allowed character in hard quota letter '" + hardQuotaLetter + "'.");
				}
			//easy way without metrics
			} else {
				softQuotaAfterTransfer = new BigDecimal(softQuota);
				hardQuotaAfterTransfer = new BigDecimal(hardQuota);
			}

			//test comparing softQuota and hardQuota (softQuota must be less or equals than hardQuota, 0 means unlimited)
			//1] if softQuota is unlimited, but hardQuota not = exception
			if(softQuotaAfterTransfer.compareTo(BigDecimal.valueOf(0)) == 0 && hardQuotaAfterTransfer.compareTo(BigDecimal.valueOf(0)) != 0) {
				throw new WrongAttributeValueException(quotasAttribute, firstPlaceholder, secondPlaceholder, "SoftQuota is set to unlimited (0) but hardQuota is limited to '" + hardQuota + "'.");
			//2] if hardQuota is not unlimited but still it is less then softQuota = exception
			} else if(hardQuotaAfterTransfer.compareTo(BigDecimal.valueOf(0)) != 0 && hardQuotaAfterTransfer.compareTo(softQuotaAfterTransfer) < 0) {
				throw new WrongAttributeValueException(quotasAttribute, firstPlaceholder, secondPlaceholder, "One of quotas is not correct. HardQuota '" + hardQuota + "' is less then softQuota '" + softQuota + "'.");
			}
			//other cases are ok

			transferedQuotas.put(canonicalPath, new Pair(softQuotaAfterTransfer, hardQuotaAfterTransfer));
		}

		return transferedQuotas;
	}

	@Override
	public Map<String, String> transferQuotasBackToAttributeValue(Map<String, Pair<BigDecimal, BigDecimal>> transferedQuotasMap, boolean withMetrics) throws InternalErrorException {
		Map<String, String> attributeQuotasValue = new HashMap<>();
		//if null or empty, return empty attribute value map for quotas
		if(transferedQuotasMap == null || transferedQuotasMap.isEmpty()) return attributeQuotasValue;

		//every path with quotas transfer step by step
		for(String path: transferedQuotasMap.keySet()) {
			Pair<BigDecimal, BigDecimal> quotas =  transferedQuotasMap.get(path);
			BigDecimal softQuotaBD = quotas.getLeft();
			BigDecimal hardQuotaBD = quotas.getRight();

			//Divide decimal till it is still natural number
			//Soft Quota
			String softQuota = "0";
			//Zero means unlimited, stay the same
			if(softQuotaBD.compareTo(BigDecimal.ZERO) != 0) {
				if(withMetrics) softQuota = Utils.bigDecimalBytesToReadableStringWithMetric(softQuotaBD);
				else softQuota = softQuotaBD.toPlainString();
			}
			//Hard Quota
			String hardQuota = "0";
			//Zero means unlimited, stay the same
			if(hardQuotaBD.compareTo(BigDecimal.ZERO) != 0) {
				if(withMetrics) hardQuota = Utils.bigDecimalBytesToReadableStringWithMetric(hardQuotaBD);
				else hardQuota = hardQuotaBD.toPlainString();
			}

			//add softQuota and hardQuota to result (50T:60T)
			attributeQuotasValue.put(path, softQuota + ":" + hardQuota);
		}
		return attributeQuotasValue;
	}

	@Override
	public Map<String,Pair<BigDecimal, BigDecimal>> mergeMemberAndResourceTransferredQuotas(Map<String, Pair<BigDecimal, BigDecimal>> resourceQuotas, Map<String, Pair<BigDecimal, BigDecimal>> memberResourceQuotas, Map<String, Pair<BigDecimal, BigDecimal>> quotasOverride) {
		Map<String,Pair<BigDecimal, BigDecimal>> mergedTransferedQuotas = new HashMap<>();

		//first go through member-resource quotas values
		for(String path: memberResourceQuotas.keySet()) {
			//override has the highest priority
			if (quotasOverride.containsKey(path)) {
				mergedTransferedQuotas.put(path, quotasOverride.get(path));
			} else {
				//if override not exists, take the original value
				mergedTransferedQuotas.put(path, memberResourceQuotas.get(path));
			}
		}

		//save unique values from resource quotas (not exists in member-resource quotas)
		for(String path: resourceQuotas.keySet()) {
			//skip already saved values, they are not unique
			if(mergedTransferedQuotas.containsKey(path)) continue;

			//take override if exists
			if(quotasOverride.containsKey(path)) {
				mergedTransferedQuotas.put(path, quotasOverride.get(path));
			} else {
				mergedTransferedQuotas.put(path, resourceQuotas.get(path));
			}
		}

		return mergedTransferedQuotas;
	}

	public Map<String, Pair<BigDecimal, BigDecimal>> countUserFacilityQuotas(List<Map<String, Pair<BigDecimal, BigDecimal>>> allUserQuotas) {
		Map<String, Pair<BigDecimal, BigDecimal>> resultTransferredQuotas = new HashMap<>();
		//for every transfered map of merged quotas count one result transfered map
		for(Map<String, Pair<BigDecimal, BigDecimal>> mapValue : allUserQuotas) {
			//for every path in one transfered map
			for(String pathKey: mapValue.keySet()) {
				//if path not exists in result map, add it with it's values
				if(!resultTransferredQuotas.containsKey(pathKey)) {
					resultTransferredQuotas.put(pathKey, mapValue.get(pathKey));
				//if path already exists in result map, sum their quotas together
				} else {
					Pair<BigDecimal, BigDecimal> quotasValue1 = resultTransferredQuotas.get(pathKey);
					Pair<BigDecimal, BigDecimal> quotasValue2 = mapValue.get(pathKey);
					//for soft quota (left part of pair)
					BigDecimal softQuota = BigDecimal.ZERO;
					if(quotasValue1.getLeft().compareTo(BigDecimal.ZERO) != 0 && quotasValue2.getLeft().compareTo(BigDecimal.ZERO) != 0) {
						softQuota = quotasValue1.getLeft().add(quotasValue2.getLeft());
					}
					//for hard quota (right part of pair)
					BigDecimal hardQuota = BigDecimal.ZERO;
					if(quotasValue1.getRight().compareTo(BigDecimal.ZERO) != 0 && quotasValue2.getRight().compareTo(BigDecimal.ZERO) != 0) {
						hardQuota = quotasValue1.getRight().add(quotasValue2.getRight());
					}
					//create new pair of summed numbers
					Pair<BigDecimal, BigDecimal> finalQuotasValue = new Pair(softQuota, hardQuota);
					//add new summed pair to the result map
					resultTransferredQuotas.put(pathKey, finalQuotasValue);
				}
			}
		}
		//return result map
		return resultTransferredQuotas;
	}

	@Override
	public boolean isFQDNValid(PerunSessionImpl sess, String fqdn) {
		if (fqdn == null) return false;
		Matcher fqdnMatcher = fqdnPattern.matcher(fqdn);
		return fqdnMatcher.find();
	}

	/**
	 * Normalize string for purpose of generating safe login value.
	 *
	 * @return normalized string
	 */
	public static String normalizeStringForLogin(String toBeNormalized) {

		if (toBeNormalized == null || toBeNormalized.trim().isEmpty()) return null;

		toBeNormalized = toBeNormalized.toLowerCase();
		toBeNormalized = java.text.Normalizer.normalize(toBeNormalized, java.text.Normalizer.Form.NFD).replaceAll("\\p{InCombiningDiacriticalMarks}+","");
		toBeNormalized = toBeNormalized.replaceAll("[^a-zA-Z]+", "");

		// unable to fill login for users without name or with partial name
		if (toBeNormalized == null || toBeNormalized.isEmpty()) {
			return null;
		}

		return toBeNormalized;

	}

	/**
	 * Shared logic for purpose of login generation
	 */
	public static class LoginGenerator {

		/**
		 * Define joining function for anonymous classes
		 */
		public interface LoginGeneratorFunction {

			/**
			 * Generate login for user using his name
			 * @param firstName
			 * @param lastName
			 * @return generated login
			 */
			public String generateLogin(String firstName, String lastName);

		}

		/**
		 * Generate login for user using his name and joining function
		 *
		 * @param user User to get data from
		 * @param function Function to join fist/lastName to login
		 * @return generated login
		 */
		public String generateLogin(User user, LoginGeneratorFunction function) {

			String firstName = user.getFirstName();
			String lastName = user.getLastName();

			// get only first part of first name and remove spec. chars
			if (firstName != null && !firstName.isEmpty()) {
				firstName = ModulesUtilsBlImpl.normalizeStringForLogin(firstName.split(" ")[0]);
			}

			// get only last part of last name and remove spec. chars
			if (lastName != null && !lastName.isEmpty()) {
				List<String> names = Arrays.asList(lastName.split(" "));
				lastName = names.get(names.size() - 1);
				lastName = ModulesUtilsBlImpl.normalizeStringForLogin(lastName.split(" ")[0]);
			}

			// unable to fill login for users without name or with partial name
			if (firstName == null || firstName.isEmpty() || lastName == null || lastName.isEmpty()) {
				return null;
			}

			return function.generateLogin(firstName, lastName);

		}

	}

	public User getUserFromMessage(PerunSessionImpl sess, String message) throws InternalErrorException {
		List<PerunBean> perunBeans = AuditParser.parseLog(message);

		User user = null;
		UserExtSource userExtSource = null;
		Member member = null;

		for(PerunBean perunBean: perunBeans) {
			if(perunBean instanceof User) {
				user = (User) perunBean;
				break;
			} else if (perunBean instanceof UserExtSource && userExtSource == null) {
				userExtSource = (UserExtSource) perunBean;
			} else if (perunBean instanceof Member && member == null) {
				member = (Member) perunBean;
			}
		}

		//if we don't have object user, try to parse user id from userExtSource (-1 means no userId was defined)
		if(user == null && userExtSource != null && userExtSource.getUserId() != -1) {
			try {
				user = getPerunBl().getUsersManagerBl().getUserById(sess, userExtSource.getUserId());
			} catch (UserNotExistsException ex) {
				log.debug("User from UserExtSource {} can't be found by id in Perun. Probably not exists any more or UserExtSource was audited without proper UserId!", userExtSource);
				return null;
			}
		} else if (user == null && member != null) {
			try {
				user = getPerunBl().getUsersManagerBl().getUserById(sess, member.getUserId());
			} catch (UserNotExistsException ex) {
				log.debug("User from Member {} can't be found by id in Perun. Probably not exists any more or Member was audited without proper UserId!", userExtSource);
				return null;
			}
		}

		return user;
	}

	public Map<Integer, Integer> checkAndConvertGIDRanges(Attribute gidRangesAttribute) throws InternalErrorException, WrongAttributeValueException {
		//Prepare structure for better working with GID Ranges
		Map<Integer, Integer> convertedRanges = new HashMap<>();

		//For null attribute throw an exception
		if(gidRangesAttribute == null) throw new InternalErrorException("Can't get value from null attribute!");

		Map<String, String> gidRanges = (LinkedHashMap) gidRangesAttribute.getValue();

		//Return empty map if there is empty input of gidRanges in method parameters
		if(gidRanges == null || gidRanges.isEmpty()) return convertedRanges;

		//Check every range if it is in correct format and it is valid range
		for(String minimumOfRange: gidRanges.keySet()) {
			//Check not null
			if(minimumOfRange == null || minimumOfRange.isEmpty()) throw new WrongAttributeValueException(gidRangesAttribute, "Minimum in one of gid ranges is empty!");
			String maximumOfRange = gidRanges.get(minimumOfRange);
			if(maximumOfRange == null || maximumOfRange.isEmpty()) throw new WrongAttributeValueException(gidRangesAttribute, "Maximum in one of gid ranges is empty!");

			//Transfer string to numbers
			Integer minimum;
			Integer maximum;

			try {
				minimum = Integer.valueOf(minimumOfRange);
				maximum = Integer.valueOf(maximumOfRange);
			} catch (NumberFormatException ex) {
				throw new WrongAttributeValueException(gidRangesAttribute, "Min or max value of some range is not correct number format.");
			}

			//Check if min value from range is bigger than 0
			if(minimum < 1) throw new WrongAttributeValueException(gidRangesAttribute, "Minimum of one of gid ranges is less than 0.");

			//Check if it is correct range
			if(minimum>maximum) throw new WrongAttributeValueException(gidRangesAttribute, "One of gid ranges is not correct range. Minimum of this range is bigger then it's maximum.");

			//Put this valid range to the map of correct gid ranges
			convertedRanges.put(minimum, maximum);
		}

		//Check gid ranges overlaps (there should be no overlaps)
		Integer lastMaxValue = 0;
		for(Integer minValue: convertedRanges.keySet().stream().sorted().collect(Collectors.toList())) {
			if(minValue <= lastMaxValue) throw new WrongAttributeValueException(gidRangesAttribute, "There is an overlap between two gid ranges.");
			lastMaxValue = convertedRanges.get(minValue);
		}

		return convertedRanges;
	}

	public PerunBl getPerunBl() {
		return this.perunBl;
	}

	public void setPerunBl(PerunBl perunBl) {
		this.perunBl = perunBl;
	}
}
