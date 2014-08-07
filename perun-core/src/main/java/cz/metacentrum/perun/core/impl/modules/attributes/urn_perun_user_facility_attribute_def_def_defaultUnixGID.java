package cz.metacentrum.perun.core.impl.modules.attributes;

import java.util.List;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.AttributesManager;
import cz.metacentrum.perun.core.api.Facility;
import cz.metacentrum.perun.core.api.Group;
import cz.metacentrum.perun.core.api.Resource;
import cz.metacentrum.perun.core.api.Service;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.exceptions.AttributeNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.ConsistencyErrorException;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.ServiceNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeAssignmentException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeValueException;
import cz.metacentrum.perun.core.api.exceptions.WrongReferenceAttributeValueException;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;
import cz.metacentrum.perun.core.implApi.modules.attributes.FacilityUserAttributesModuleAbstract;
import cz.metacentrum.perun.core.implApi.modules.attributes.FacilityUserAttributesModuleImplApi;
import java.util.ArrayList;

/**
 *
 * @author Slavek Licehammer &lt;glory@ics.muni.cz&gt;
 */
public class urn_perun_user_facility_attribute_def_def_defaultUnixGID extends FacilityUserAttributesModuleAbstract implements FacilityUserAttributesModuleImplApi {

	@Override
	/**
	 * Checks the new default GID of the user at the specified facility. The new GID must be equals to any of resource unixGID attribute where resource is from speciafie facility (and user must have acces to this resource) or from groupResource:unixGID attribute (groups if from the resources and user have acess to them)
	 *
	 * TODO Known issues: Can't detect if unixGid is not set on all resources and groups where user is allowed. This will be reported as WrongAttributeValueException, but it should be WrongReferenceAttributeValueException
	 */
	public void checkAttributeValue(PerunSessionImpl sess, Facility facility, User user, Attribute attribute) throws WrongAttributeValueException, WrongReferenceAttributeValueException, InternalErrorException, WrongAttributeAssignmentException {
		Integer gid = (Integer) attribute.getValue();
		if(gid == null) throw new WrongAttributeValueException(attribute, user, facility, "Attribute value is null.");

		Attribute namespaceAttribute;
		try {
			namespaceAttribute = sess.getPerunBl().getAttributesManagerBl().getAttribute(sess, facility, AttributesManager.NS_FACILITY_ATTR_DEF + ":unixGID-namespace");
		} catch(AttributeNotExistsException ex) {
			throw new ConsistencyErrorException(ex);
		}
		if(namespaceAttribute.getValue() == null) throw new WrongReferenceAttributeValueException(attribute, namespaceAttribute, "Reference attribute is null");
		String namespaceName = (String) namespaceAttribute.getValue();

		Attribute unixGroupNameNamespace;
		try {
			unixGroupNameNamespace = sess.getPerunBl().getAttributesManagerBl().getAttribute(sess, facility, AttributesManager.NS_FACILITY_ATTR_DEF + ":unixGroupName-namespace");
		} catch(AttributeNotExistsException ex) {
			throw new ConsistencyErrorException(ex);
		}
		if(unixGroupNameNamespace.getValue() == null) throw new WrongReferenceAttributeValueException(attribute, unixGroupNameNamespace, user, facility, facility, null, "Reference attribute is null");
		String unixGroupNameNamespaceName = (String) unixGroupNameNamespace.getValue();

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

		Attribute groupResourceIsUnixGroupAttrtibute;
		try {
			groupResourceIsUnixGroupAttrtibute = new Attribute(sess.getPerunBl().getAttributesManagerBl().getAttributeDefinition(sess, AttributesManager.NS_GROUP_RESOURCE_ATTR_DEF + ":isUnixGroup"));
			groupResourceIsUnixGroupAttrtibute.setValue(1);
		} catch(AttributeNotExistsException ex) {
			throw new ConsistencyErrorException(ex);
		}

		List<Group> groupWithSameGid = sess.getPerunBl().getGroupsManagerBl().getGroupsByAttribute(sess, groupGidAttribute);

		Service groupService;
		try {
			groupService = sess.getPerunBl().getServicesManagerBl().getServiceByName(sess, "group");
		} catch(ServiceNotExistsException ex) {
			throw new InternalErrorException(ex);
		}
		List<Group> candidateGroups = groupWithSameGid;
		candidateGroups.retainAll(sess.getPerunBl().getFacilitiesManagerBl().getAllowedGroups(sess, facility, null, groupService));

		for(Group group : candidateGroups) {
			//check if group has unix group name in namespace required by facility
			try {
				Attribute unixGroupName = sess.getPerunBl().getAttributesManagerBl().getAttribute(sess, group, AttributesManager.NS_GROUP_ATTR_DEF + ":unixGroupName-namespace:" + unixGroupNameNamespaceName);
				if(unixGroupName.getValue() == null || ((String) unixGroupName.getValue()).isEmpty()) {
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

		throw new WrongAttributeValueException(attribute, "User isn't allowed to have the default unix group which have this gid (" + gid + ") or such group doesn't exist.  " + user);

	}

	@Override
	/**
	 * Fills the new GID for the user at the specified facility. Gets the first resource from facility (on which the user have acesss) which have filled attribute unixGID and fill with this value.
	 */
	public Attribute fillAttribute(PerunSessionImpl sess, Facility facility, User user, AttributeDefinition attributeDefinition) throws InternalErrorException, WrongAttributeAssignmentException {
		Attribute attribute = new Attribute(attributeDefinition);

		List<Resource> allowedResources = sess.getPerunBl().getUsersManagerBl().getAllowedResources(sess, facility, user);
		try {
			for(Resource resource : allowedResources) {
				List<AttributeDefinition> resourceRequiredAttributesDefinitions = sess.getPerunBl().getAttributesManagerBl().getResourceRequiredAttributesDefinition(sess, resource);

				//if this attribute is not required by the services on the resource, skip the resource
				if(!resourceRequiredAttributesDefinitions.contains(new AttributeDefinition(attributeDefinition))) continue;

				Attribute unixGidAttribute = sess.getPerunBl().getAttributesManagerBl().getAttribute(sess, resource, AttributesManager.NS_RESOURCE_ATTR_VIRT + ":unixGID");
				if(unixGidAttribute.getValue() != null) {
					attribute.setValue(unixGidAttribute.getValue());
					return attribute;
				}
			}
		} catch(AttributeNotExistsException ex) {
			throw new ConsistencyErrorException(ex);
		}

		return attribute;
	}

	@Override
	public List<String> getDependencies() {
		List<String> dependencies = new ArrayList<String>();
		dependencies.add(AttributesManager.NS_FACILITY_ATTR_DEF + ":unixGID-namespace");
		dependencies.add(AttributesManager.NS_FACILITY_ATTR_DEF + ":unixGroupName-namespace");
		dependencies.add(AttributesManager.NS_RESOURCE_ATTR_DEF + ":unixGID-namespace" + ":*");
		dependencies.add(AttributesManager.NS_GROUP_ATTR_DEF + ":unixGID-namespace" + ":*");
		return dependencies;
	}

	public AttributeDefinition getAttributeDefinition() {
		AttributeDefinition attr = new AttributeDefinition();
		attr.setNamespace(AttributesManager.NS_USER_FACILITY_ATTR_DEF);
		attr.setFriendlyName("defaultUnixGID");
		attr.setDisplayName("Default unix GID");
		attr.setType(Integer.class.getName());
		attr.setDescription("Default Unix Group ID.");
		return attr;
	}
}
