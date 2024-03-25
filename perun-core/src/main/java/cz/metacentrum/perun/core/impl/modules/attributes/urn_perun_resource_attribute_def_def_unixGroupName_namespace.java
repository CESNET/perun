package cz.metacentrum.perun.core.impl.modules.attributes;

import cz.metacentrum.perun.core.api.Attribute;
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
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * @author Slavek Licehammer &lt;glory@ics.muni.cz&gt;
 */
public class urn_perun_resource_attribute_def_def_unixGroupName_namespace extends ResourceAttributesModuleAbstract
    implements ResourceAttributesModuleImplApi {

  protected static final Pattern defaultUnixGroupNamePattern = Pattern.compile("^[-_.a-zA-Z0-9]+$");
  private static final String A_F_unixGroupName_namespace =
      AttributesManager.NS_FACILITY_ATTR_DEF + ":unixGroupName-namespace";
  private static final String A_F_unixGID_namespace = AttributesManager.NS_FACILITY_ATTR_DEF + ":unixGID-namespace";
  private static final String A_R_unixGID_namespace = AttributesManager.NS_RESOURCE_ATTR_DEF + ":unixGID-namespace";
  private static final String A_G_unixGID_namespace = AttributesManager.NS_GROUP_ATTR_DEF + ":unixGID-namespace";
  private static final String A_G_unixGroupName_namespace =
      AttributesManager.NS_GROUP_ATTR_DEF + ":unixGroupName-namespace";

  @Override
  public void changedAttributeHook(PerunSessionImpl session, Resource resource, Attribute attribute)
      throws WrongReferenceAttributeValueException {
    //Need to know if this is remove or set, if value is null, its remove, otherway it is set
    String groupNameNamespace = attribute.getFriendlyNameParameter();

    try {
      if (attribute.getValue() != null) {
        //First need to find facility for the group
        Facility facilityOfResource = session.getPerunBl().getResourcesManagerBl().getFacility(session, resource);
        String gidNamespace = null;

        //If facility has the same namespace of GroupName like attribute unixGroupName-namespace, then prepare
        // gidNamespace
        Attribute facilityGroupNameNamespace = session.getPerunBl().getAttributesManagerBl()
            .getAttribute(session, facilityOfResource, A_F_unixGroupName_namespace);
        if (facilityGroupNameNamespace.getValue() != null) {
          if (groupNameNamespace.equals(facilityGroupNameNamespace.getValue())) {
            Attribute facilityGIDNamespace = session.getPerunBl().getAttributesManagerBl()
                .getAttribute(session, facilityOfResource, A_F_unixGID_namespace);
            if (facilityGIDNamespace.getValue() != null) {
              gidNamespace = (String) facilityGIDNamespace.getValue();
            }
          }
        }

        //If there is any gidNamespace which is need to be set, do it there
        if (gidNamespace != null) {
          Attribute resourceUnixGIDNamespace = session.getPerunBl().getAttributesManagerBl()
              .getAttribute(session, resource, A_R_unixGID_namespace + ":" + gidNamespace);
          if (resourceUnixGIDNamespace.getValue() == null) {
            resourceUnixGIDNamespace = session.getPerunBl().getAttributesManagerBl()
                .fillAttribute(session, resource, resourceUnixGIDNamespace);
            if (resourceUnixGIDNamespace.getValue() == null) {
              throw new WrongReferenceAttributeValueException(attribute, resourceUnixGIDNamespace);
            }

            try {
              session.getPerunBl().getAttributesManagerBl().setAttribute(session, resource, resourceUnixGIDNamespace);
            } catch (WrongAttributeValueException ex) {
              throw new WrongReferenceAttributeValueException(attribute, resourceUnixGIDNamespace, ex);
            }
          } else {
            session.getPerunBl().getAttributesManagerBl()
                .checkAttributeSemantics(session, resource, resourceUnixGIDNamespace);
          }
        }

      }
      //Attribute value can be null, for now, no changes for removing some GroupName of this Resource
    } catch (WrongAttributeAssignmentException ex) {
      //TODO: need to add WrongAttributeAssignmentException to header of modules methods
      throw new InternalErrorException(ex);
    } catch (AttributeNotExistsException ex) {
      throw new ConsistencyErrorException(ex);
    }
  }

  @Override
  public void checkAttributeSemantics(PerunSessionImpl sess, Resource resource, Attribute attribute)
      throws WrongReferenceAttributeValueException, WrongAttributeAssignmentException {
    //prepare namespace and groupName value variables
    String groupName = null;
    if (attribute.getValue() != null) {
      groupName = attribute.valueAsString();
    }
    String groupNameNamespace = attribute.getFriendlyNameParameter();

    if (groupName == null) {
      // if this is resource, its not ok
      throw new WrongReferenceAttributeValueException(attribute, null, resource, null,
          "Attribute groupName-namespace for resourece can't be null.");
    }

    try {
      //prepare attributes group and resource unixGroupName
      Attribute groupUnixGroupName = new Attribute(sess.getPerunBl().getAttributesManagerBl()
          .getAttributeDefinition(sess, A_G_unixGroupName_namespace + ":" + groupNameNamespace));
      Attribute resourceUnixGroupName = attribute;
      groupUnixGroupName.setValue(attribute.getValue());

      //prepare lists of groups and resources with the same groupName value in the same namespace

      //Fill lists of groups and resources
      List<Group> groupsWithSameGroupNameInTheSameNamespace =
          new ArrayList<>(sess.getPerunBl().getGroupsManagerBl().getGroupsByAttribute(sess, groupUnixGroupName));
      List<Resource> resourcesWithSameGroupNameInTheSameNamespace = new ArrayList<>(
          sess.getPerunBl().getResourcesManagerBl().getResourcesByAttribute(sess, resourceUnixGroupName));
      //Remove self from the list of resources with the same namespace
      //FIXME This behavior is not correct, because there are two possible situations:
      // - This is the Check right after setting a new value (correct behavior), we need to skip this value from
      // checking
      // - This is the Check without setting a new value (not correct), and if this resource is the only one with the
      // value, we will lose the right for it
      resourcesWithSameGroupNameInTheSameNamespace.remove(resource);

      //If there is no group or resource with same GroupNameInTheSameNamespace, its ok
      if (groupsWithSameGroupNameInTheSameNamespace.isEmpty() &&
          resourcesWithSameGroupNameInTheSameNamespace.isEmpty()) {
        return;
      }

      //First need to know that i have right to write any of duplicit groupName-namespace attribute
      boolean haveRights = sess.getPerunBl().getModulesUtilsBl()
          .haveRightToWriteAttributeInAnyGroupOrResource(sess, groupsWithSameGroupNameInTheSameNamespace,
              resourcesWithSameGroupNameInTheSameNamespace, groupUnixGroupName, resourceUnixGroupName);
      if (!haveRights) {
        throw new WrongReferenceAttributeValueException(attribute, null, resource, null,
            "This groupName is already used for other group or resource and user has no rights to use it.");
      }

      //Now if rights are ok, prepare lists of UnixGIDs attributes of this group (also equivalent resource GID)
      List<Attribute> resourceUnixGIDs = sess.getPerunBl().getAttributesManagerBl()
          .getAllAttributesStartWithNameWithoutNullValue(sess, resource, A_R_unixGID_namespace + ":");
      List<Attribute> groupVersionUnixGIDs =
          sess.getPerunBl().getModulesUtilsBl().getListOfGroupGIDsFromListOfResourceGIDs(sess, resourceUnixGIDs);

      //In list of duplicit groups looking for GID in same namespace but with different value, thats not correct
      if (!groupsWithSameGroupNameInTheSameNamespace.isEmpty()) {
        for (Group g : groupsWithSameGroupNameInTheSameNamespace) {
          for (Attribute a : groupVersionUnixGIDs) {
            int compare;
            compare = sess.getPerunBl().getModulesUtilsBl().haveTheSameAttributeWithTheSameNamespace(sess, g, a);

            if (compare > 0) {
              throw new WrongReferenceAttributeValueException(attribute, a, resource, null, g, null,
                  "One of the group GIDs is from the same namespace like other group GID but with different values.");
            }
          }
        }
      }

      //In list of duplicit resources looking for GID in same namespace but with different value, thats not correct
      if (!resourcesWithSameGroupNameInTheSameNamespace.isEmpty()) {
        for (Resource r : resourcesWithSameGroupNameInTheSameNamespace) {
          for (Attribute a : resourceUnixGIDs) {
            int compare;
            compare = sess.getPerunBl().getModulesUtilsBl().haveTheSameAttributeWithTheSameNamespace(sess, r, a);

            if (compare > 0) {
              throw new WrongReferenceAttributeValueException(attribute, a, resource, null, r, null,
                  "One of the group GIDs is from the same namespace like other resource GIDs but with different " +
                  "values.");
            }
          }
        }
      }

    } catch (AttributeNotExistsException ex) {
      throw new ConsistencyErrorException(ex);
    }
  }

  @Override
  public void checkAttributeSyntax(PerunSessionImpl sess, Resource resource, Attribute attribute)
      throws WrongAttributeValueException {
    if (attribute.getValue() == null) {
      return;
    }

    //Check attribute regex
    sess.getPerunBl().getModulesUtilsBl().checkAttributeRegex(attribute, defaultUnixGroupNamePattern);

    //Check reserved unix group names
    sess.getPerunBl().getModulesUtilsBl().checkReservedUnixGroupNames(attribute);
  }

  @Override
  public List<String> getDependencies() {
    List<String> dependencies = new ArrayList<>();
    //Disallowed because of crosschecks between modules and performance reason
    //dependencies.add(A_G_unixGID_namespace + ":*");
    //dependencies.add(A_R_unixGID_namespace + ":*");
    //Disallowed because it does not affect value of dependent attribute
    //dependencies.add(A_G_unixGroupName_namespace + ":*");
    return dependencies;
  }

  /*public AttributeDefinition getAttributeDefinition() {
      AttributeDefinition attr = new AttributeDefinition();
      attr.setNamespace(AttributesManager.NS_RESOURCE_ATTR_DEF);
      attr.setFriendlyName("unixGroupName-namespace");
      attr.setType(String.class.getName());
      attr.setDescription("Unix group name namespace.");
      return attr;
  }*/
}
