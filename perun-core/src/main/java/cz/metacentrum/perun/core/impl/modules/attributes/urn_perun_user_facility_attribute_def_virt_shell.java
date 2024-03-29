package cz.metacentrum.perun.core.impl.modules.attributes;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.AttributesManager;
import cz.metacentrum.perun.core.api.Facility;
import cz.metacentrum.perun.core.api.Resource;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.exceptions.AttributeNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.ConsistencyErrorException;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeAssignmentException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeValueException;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;
import cz.metacentrum.perun.core.impl.Utils;
import cz.metacentrum.perun.core.implApi.modules.attributes.SkipValueCheckDuringDependencyCheck;
import cz.metacentrum.perun.core.implApi.modules.attributes.UserFacilityVirtualAttributesModuleAbstract;
import cz.metacentrum.perun.core.implApi.modules.attributes.UserFacilityVirtualAttributesModuleImplApi;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author Jakub Peschel <410368@mail.muni.cz>
 */
@SkipValueCheckDuringDependencyCheck
public class urn_perun_user_facility_attribute_def_virt_shell extends UserFacilityVirtualAttributesModuleAbstract
    implements UserFacilityVirtualAttributesModuleImplApi {

  @Override
  public AttributeDefinition getAttributeDefinition() {
    AttributeDefinition attr = new AttributeDefinition();
    attr.setNamespace(AttributesManager.NS_USER_FACILITY_ATTR_VIRT);
    attr.setFriendlyName("shell");
    attr.setDisplayName("shell");
    attr.setType(String.class.getName());
    attr.setDescription("Computed shell from user preferences");
    return attr;
  }

  @Override
  public Attribute getAttributeValue(PerunSessionImpl sess, User user, Facility facility,
                                     AttributeDefinition attributeDefinition) {
    Attribute attr = new Attribute(attributeDefinition);
    try {
      Attribute attribute = sess.getPerunBl().getAttributesManagerBl()
          .getAttribute(sess, facility, user, AttributesManager.NS_USER_FACILITY_ATTR_DEF + ":shell");
      if (attribute.getValue() != null) {
        Utils.copyAttributeToVirtualAttributeWithValue(attribute, attr);
        return attr;
      }
    } catch (WrongAttributeAssignmentException | AttributeNotExistsException ex) {
      throw new InternalErrorException(ex);
    }

    try {
      Attribute facilityShells = sess.getPerunBl().getAttributesManagerBl()
          .getAttribute(sess, facility, AttributesManager.NS_FACILITY_ATTR_DEF + ":shells");
      Attribute userPrefferedShells = (sess.getPerunBl().getAttributesManagerBl()
          .getAttribute(sess, user, AttributesManager.NS_USER_ATTR_DEF + ":preferredShells"));
      List<Resource> resources = sess.getPerunBl().getUsersManagerBl().getAllowedResources(sess, facility, user);
      Set<String> resourcesShells = new HashSet<>();

      for (Resource resource : resources) {
        List<String> resourcesShellsForTest = (List<String>) sess.getPerunBl().getAttributesManagerBl()
            .getAttribute(sess, resource, AttributesManager.NS_RESOURCE_ATTR_DEF + ":shells").getValue();
        if (resourcesShellsForTest != null) {
          resourcesShells.addAll(resourcesShellsForTest);
        }
      }
      if (userPrefferedShells.getValue() != null) {
        for (String prefShell : (List<String>) userPrefferedShells.getValue()) {
          if (resourcesShells.contains(prefShell)) {
            Utils.copyAttributeToViAttributeWithoutValue(userPrefferedShells, attr);
            attr.setValue(prefShell);
            return attr;
          }
        }
      }
      if (facilityShells.getValue() != null) {
        for (String facilityShell : (List<String>) facilityShells.getValue()) {
          if (resourcesShells.contains(facilityShell)) {
            Utils.copyAttributeToViAttributeWithoutValue(facilityShells, attr);
            attr.setValue(facilityShell);
            return attr;
          }
        }
      }

    } catch (AttributeNotExistsException | WrongAttributeAssignmentException ex) {
      throw new InternalErrorException(ex);
    }
    return attr;

  }

  @Override
  public List<String> getStrongDependencies() {
    List<String> strongDependencies = new ArrayList<>();
    strongDependencies.add(AttributesManager.NS_USER_FACILITY_ATTR_DEF + ":shell");
    strongDependencies.add(AttributesManager.NS_FACILITY_ATTR_DEF + ":shells");
    strongDependencies.add(AttributesManager.NS_USER_ATTR_DEF + ":preferredShells");
    strongDependencies.add(AttributesManager.NS_RESOURCE_ATTR_DEF + ":shells");
    return strongDependencies;
  }

  @Override
  public boolean setAttributeValue(PerunSessionImpl sess, User user, Facility facility, Attribute attribute) {
    try {
      Attribute attributeToSet = sess.getPerunBl().getAttributesManagerBl()
          .getAttribute(sess, facility, user, AttributesManager.NS_USER_FACILITY_ATTR_DEF + ":shell");
      return sess.getPerunBl().getAttributesManagerBl().setAttributeWithoutCheck(sess, facility, user, attributeToSet);
    } catch (WrongAttributeAssignmentException ex) {
      throw new ConsistencyErrorException(ex);
    } catch (AttributeNotExistsException | WrongAttributeValueException ex) {
      throw new InternalErrorException(ex);
    }
  }
}

