package cz.metacentrum.perun.core.impl.modules.attributes;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.AttributesManager;
import cz.metacentrum.perun.core.api.Resource;
import cz.metacentrum.perun.core.api.exceptions.AttributeNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeAssignmentException;
import cz.metacentrum.perun.core.api.exceptions.WrongReferenceAttributeValueException;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;
import cz.metacentrum.perun.core.implApi.modules.attributes.ResourceAttributesModuleAbstract;
import cz.metacentrum.perun.core.implApi.modules.attributes.ResourceAttributesModuleImplApi;
import java.util.ArrayList;
import java.util.List;

/**
 * Checks and fills default shells per resource.
 *
 * @author Lukáš Pravda   <luky.pravda@gmail.com>
 * @date 28.4.2011 11:12:16
 */
public class urn_perun_resource_attribute_def_def_defaultShell extends ResourceAttributesModuleAbstract
    implements ResourceAttributesModuleImplApi {

  private static final String A_R_shells = AttributesManager.NS_RESOURCE_ATTR_DEF + ":shells";

  /**
   * Checks the attribute with a default shell at the specified resource. The new default shell must be included at
   * specified resource.
   */
  @Override
  public void checkAttributeSemantics(PerunSessionImpl perunSession, Resource resource, Attribute attribute)
      throws WrongReferenceAttributeValueException, WrongAttributeAssignmentException {
    if (attribute.getValue() == null) {
      throw new WrongReferenceAttributeValueException(attribute, null, resource, null, "Attribute value is null.");
    }

    Attribute resourceAttr;
    try {
      resourceAttr =
          perunSession.getPerunBl().getAttributesManagerBl().getAttribute(perunSession, resource, A_R_shells);
    } catch (AttributeNotExistsException ex) {
      throw new InternalErrorException(
          "Attribute with list of shells from resource " + resource.getId() + " could not obtained.", ex);
    }

    if (resourceAttr.getValue() == null) {
      throw new WrongReferenceAttributeValueException(resourceAttr, null, resource, null,
          "Attribute with list of shells from resource has null value.");
    }
    List<String> shells = resourceAttr.valueAsList();

    if (!shells.contains(attribute.valueAsString())) {
      throw new WrongReferenceAttributeValueException(attribute, resourceAttr, resource, null, resource, null,
          "Shell " + attribute.getValue() + " is not at specified resource (" + resource + ")");
    }
  }

  /**
   * Fills the default shell at specified resource. If the facility contains no shells, the exception is thrown
   * otherwise some shell is picked.
   */
  @Override
  public Attribute fillAttribute(PerunSessionImpl perunSession, Resource resource, AttributeDefinition attribute)
      throws WrongAttributeAssignmentException {
    Attribute atr = new Attribute(attribute);
    Attribute resourceAttr;
    try {
      resourceAttr =
          perunSession.getPerunBl().getAttributesManagerBl().getAttribute(perunSession, resource, A_R_shells);
    } catch (AttributeNotExistsException ex) {
      throw new InternalErrorException(
          "Attribute with list of shells from resource " + resource.getId() + " could not obtained.", ex);
    }

    if (resourceAttr.getValue() == null) {
      return atr;
    } else {
      List<String> shells = (List<String>) resourceAttr.getValue();

      if (!shells.isEmpty()) {
        atr.setValue(shells.get(0));
        return atr;
      } else {
        return atr;
      }
    }
  }

  @Override
  public AttributeDefinition getAttributeDefinition() {
    AttributeDefinition attr = new AttributeDefinition();
    attr.setNamespace(AttributesManager.NS_RESOURCE_ATTR_DEF);
    attr.setFriendlyName("defaultShell");
    attr.setDisplayName("Default shell");
    attr.setType(String.class.getName());
    attr.setDescription("Default shell for all members on this resource.");
    return attr;
  }

  @Override
  public List<String> getDependencies() {
    List<String> dependecies = new ArrayList<>();
    dependecies.add(A_R_shells);
    return dependecies;
  }
}
