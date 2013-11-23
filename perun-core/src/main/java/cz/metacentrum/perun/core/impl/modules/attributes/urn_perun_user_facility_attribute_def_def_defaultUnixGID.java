package cz.metacentrum.perun.core.impl.modules.attributes;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.AttributesManager;
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
        if(gid == null) return;

        Attribute namespaceAttribute;
        try {
          namespaceAttribute = sess.getPerunBl().getAttributesManagerBl().getAttribute(sess, facility, AttributesManager.NS_FACILITY_ATTR_DEF + ":unixGID-namespace");
        } catch(AttributeNotExistsException ex) {
          throw new ConsistencyErrorException(ex);
        }
        if(namespaceAttribute.getValue() == null) throw new WrongReferenceAttributeValueException(attribute, namespaceAttribute, "Reference attribute is null");
        String namespaceName = (String) namespaceAttribute.getValue();

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
        List<Pair<Group, Resource>> groupResourceWhichIsUnixGroup = sess.getPerunBl().getGroupsManagerBl().getGroupResourcePairsByAttribute(sess, groupResourceIsUnixGroupAttrtibute);

        for(Pair<Group, Resource> groupResource : groupResourceWhichIsUnixGroup ) {
          if(!groupWithSameGid.contains(groupResource.getLeft())) continue; // group from Group-resource pair doesn't have same GID as a checked one
          if(!allowedResources.contains(groupResource.getRight())) continue;  //user is not allowed on resource, so he's not allowed on group too
          if(sess.getPerunBl().getGroupsManagerBl().isUserMemberOfGroup(sess, user, groupResource.getLeft())) {
            //We found group with same id where user is allowed
            //Ceck if group is assigned on resource
            List<Group> assignedGroups = sess.getPerunBl().getResourcesManagerBl().getAssignedGroups(sess, groupResource.getRight());
            if(assignedGroups.contains(groupResource.getLeft())) return; //attribute is Ok
          }
        }

        throw new WrongAttributeValueException(attribute, "User isn't allowed to have the default unix group which have this gid (" + gid + ") or such group doesn't exist.  " + user);

    }

    @Override
    public List<String> getDependencies() {
      List<String> dependencies = new ArrayList<String>();
      dependencies.add(AttributesManager.NS_FACILITY_ATTR_DEF + ":unixGID-namespace");
      dependencies.add(AttributesManager.NS_RESOURCE_ATTR_DEF + ":unixGID-namespace" + ":*");
      dependencies.add(AttributesManager.NS_GROUP_ATTR_DEF + ":unixGID-namespace" + ":*");
      dependencies.add( AttributesManager.NS_GROUP_RESOURCE_ATTR_DEF + ":isUnixGroup");
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
