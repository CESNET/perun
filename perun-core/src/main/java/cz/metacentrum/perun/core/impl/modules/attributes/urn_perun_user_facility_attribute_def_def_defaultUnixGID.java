package cz.metacentrum.perun.core.impl.modules.attributes;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.AttributesManager;
import cz.metacentrum.perun.core.api.Facility;
import cz.metacentrum.perun.core.api.Group;
import cz.metacentrum.perun.core.api.Resource;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.exceptions.AttributeNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.ConsistencyErrorException;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeAssignmentException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeValueException;
import cz.metacentrum.perun.core.api.exceptions.WrongReferenceAttributeValueException;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;
import cz.metacentrum.perun.core.implApi.modules.attributes.UserFacilityAttributesModuleAbstract;
import cz.metacentrum.perun.core.implApi.modules.attributes.UserFacilityAttributesModuleImplApi;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Slavek Licehammer &lt;glory@ics.muni.cz&gt;
 */
public class urn_perun_user_facility_attribute_def_def_defaultUnixGID extends UserFacilityAttributesModuleAbstract implements UserFacilityAttributesModuleImplApi {

	/**
	 * Checks the new default GID of the user at the specified facility. The new GID must be equals to any of resource unixGID attribute where resource is from speciafie facility (and user must have acces to this resource) or from groupResource:unixGID attribute (groups if from the resources and user have acess to them)
	 *
	 * TODO Known issues: Can't detect if unixGid is not set on all resources and groups where user is allowed. This will be reported as WrongAttributeValueException, but it should be WrongReferenceAttributeValueException
	 */
	@Override
	public void checkAttributeSemantics(PerunSessionImpl sess, User user, Facility facility, Attribute attribute) throws WrongReferenceAttributeValueException, WrongAttributeAssignmentException {
		Integer gid = attribute.valueAsInteger();
		if(gid == null) return;

		Attribute namespaceAttribute;
		try {
			namespaceAttribute = sess.getPerunBl().getAttributesManagerBl().getAttribute(sess, facility, AttributesManager.NS_FACILITY_ATTR_DEF + ":unixGID-namespace");
		} catch(AttributeNotExistsException ex) {
			throw new ConsistencyErrorException(ex);
		}
		if(namespaceAttribute.getValue() == null) throw new WrongReferenceAttributeValueException(attribute, namespaceAttribute, user, facility, facility, null, "Reference attribute is null");
		String namespaceName = namespaceAttribute.valueAsString();

		Attribute unixGroupNameNamespace;
		try {
			unixGroupNameNamespace = sess.getPerunBl().getAttributesManagerBl().getAttribute(sess, facility, AttributesManager.NS_FACILITY_ATTR_DEF + ":unixGroupName-namespace");
		} catch(AttributeNotExistsException ex) {
			throw new ConsistencyErrorException(ex);
		}
		if(unixGroupNameNamespace.getValue() == null) throw new WrongReferenceAttributeValueException(attribute, unixGroupNameNamespace, user, facility, facility, null, "Reference attribute is null");
		String unixGroupNameNamespaceName = unixGroupNameNamespace.valueAsString();

		Attribute resourceGidAttribute;
		try {
			resourceGidAttribute = new Attribute(sess.getPerunBl().getAttributesManagerBl().getAttributeDefinition(sess, AttributesManager.NS_RESOURCE_ATTR_DEF + ":unixGID-namespace:" + namespaceName));
		} catch(AttributeNotExistsException ex) {
			throw new ConsistencyErrorException("Namespace from value of " + namespaceAttribute + " doesn't exists. (Resource attribute " + AttributesManager.NS_RESOURCE_ATTR_DEF + ":unixGID-namespace:" + namespaceName + " doesn't exists", ex);
		}
		resourceGidAttribute.setValue(attribute.getValue());
		List<Resource> allowedResources = sess.getPerunBl().getUsersManagerBl().getAllowedResources(sess, facility, user);
		List<Resource> allowedResourcesWithSameGid = sess.getPerunBl().getResourcesManagerBl().getResourcesByAttribute(sess, resourceGidAttribute);
		allowedResourcesWithSameGid.retainAll(allowedResources);

		if(!allowedResourcesWithSameGid.isEmpty()) return; //We found at least one allowed resource with same gid as the user have => attribute is OK

		Attribute groupGidAttribute;
		try {
			groupGidAttribute = new Attribute(sess.getPerunBl().getAttributesManagerBl().getAttributeDefinition(sess, AttributesManager.NS_GROUP_ATTR_DEF + ":unixGID-namespace:" + namespaceName));
			groupGidAttribute.setValue(attribute.getValue());
		} catch(AttributeNotExistsException ex) {
			throw new ConsistencyErrorException("Namespace from value of " + namespaceAttribute + " doesn't exists. (Group-resource attribute " + AttributesManager.NS_GROUP_ATTR_DEF + ":unixGID-namespace:" + namespaceName + " doesn't exists", ex);
		}

		List<Group> groupWithSameGid = sess.getPerunBl().getGroupsManagerBl().getGroupsByAttribute(sess, groupGidAttribute);

		groupWithSameGid.retainAll(sess.getPerunBl().getFacilitiesManagerBl().getAllowedGroups(sess, facility, null, null));

		for(Group group : groupWithSameGid) {
			//check if group has unix group name in namespace required by facility
			try {
				Attribute unixGroupName = sess.getPerunBl().getAttributesManagerBl().getAttribute(sess, group, AttributesManager.NS_GROUP_ATTR_DEF + ":unixGroupName-namespace:" + unixGroupNameNamespaceName);
				if(unixGroupName.getValue() == null || (unixGroupName.valueAsString()).isEmpty()) {
					continue;
				}
			} catch(AttributeNotExistsException ex) {
				throw new InternalErrorException(ex);
			}

			//check if the user is member of the group
			if(sess.getPerunBl().getGroupsManagerBl().isUserMemberOfGroup(sess, user, group)) {
				return;	//attribute is OK
			}
		}

		throw new WrongReferenceAttributeValueException(attribute, null, user, facility, "User isn't allowed to have the default unix group which have this gid (" + gid + ") or such group doesn't exist.  " + user);

	}

		@Override
		public List<String> getDependencies() {
			List<String> dependencies = new ArrayList<>();
			dependencies.add(AttributesManager.NS_FACILITY_ATTR_DEF + ":unixGID-namespace");
			dependencies.add(AttributesManager.NS_FACILITY_ATTR_DEF + ":unixGroupName-namespace");
			//Disallowed because of crosschecks between modules and peformance reason
			//dependencies.add(A_G_unixGID_namespace + ":*");
			//dependencies.add(A_R_unixGID_namespace + ":*");
			//dependencies.add(A_G_unixGroupName_namespace + ":*");
			return dependencies;
		}

		public AttributeDefinition getAttributeDefinition() {
			AttributeDefinition attr = new AttributeDefinition();
			attr.setNamespace(AttributesManager.NS_USER_FACILITY_ATTR_DEF);
			attr.setFriendlyName("defaultUnixGID");
			attr.setType(Integer.class.getName());
			attr.setDescription("Default Unix Group ID.");
			return attr;
	}
}
