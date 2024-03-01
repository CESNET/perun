package cz.metacentrum.perun.core.impl.modules.attributes;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.AttributesManager;
import cz.metacentrum.perun.core.api.Facility;
import cz.metacentrum.perun.core.api.Resource;
import cz.metacentrum.perun.core.api.exceptions.AttributeNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeAssignmentException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeValueException;
import cz.metacentrum.perun.core.api.exceptions.WrongReferenceAttributeValueException;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;
import cz.metacentrum.perun.core.implApi.modules.attributes.ResourceAttributesModuleAbstract;
import cz.metacentrum.perun.core.implApi.modules.attributes.ResourceAttributesModuleImplApi;
import java.util.ArrayList;
import java.util.List;

/**
 * Checks and fills shells at specified resource
 *
 * @author Lukáš Pravda   <luky.pravda@gmail.com>
 * @date 28.4.2011 14:48:04
 */
public class urn_perun_resource_attribute_def_def_shells extends ResourceAttributesModuleAbstract
    implements ResourceAttributesModuleImplApi {

  private static final String A_F_shells = AttributesManager.NS_FACILITY_ATTR_DEF + ":shells";

  /**
   * Checks the attribute with all available shells from resource's facility
   */
  @Override
  public void checkAttributeSemantics(PerunSessionImpl perunSession, Resource resource, Attribute attribute)
      throws WrongReferenceAttributeValueException, WrongAttributeAssignmentException {
    List<String> shells = attribute.valueAsList();

    if (shells == null) {
      throw new WrongReferenceAttributeValueException(attribute, null, resource, null, "Attribute cannot be null.");
    }

    Facility facility = perunSession.getPerunBl().getResourcesManagerBl().getFacility(perunSession, resource);
    Attribute allShellsPerFacility;
    try {
      allShellsPerFacility =
          perunSession.getPerunBl().getAttributesManagerBl().getAttribute(perunSession, facility, A_F_shells);
    } catch (AttributeNotExistsException ex) {
      throw new InternalErrorException(
          "Attribute with list of shells from facility " + facility.getId() + " could not obtained.", ex);
    }


    if (allShellsPerFacility.getValue() == null) {
      throw new WrongReferenceAttributeValueException(attribute, allShellsPerFacility, resource, null, facility, null,
          "Attribute with list of shells from facility cannot be null.");
    }
    if (!(allShellsPerFacility.valueAsList()).containsAll(shells)) {
      throw new WrongReferenceAttributeValueException(attribute, allShellsPerFacility, resource, null, facility, null,
          "Some shells from specified resource are not at home facility " + facility.getId());
    }
  }

  @Override
  public void checkAttributeSyntax(PerunSessionImpl perunSession, Resource resource, Attribute attribute)
      throws WrongAttributeValueException {
    List<String> shells = attribute.valueAsList();

    if (shells == null) {
      return;
    }

    for (String st : shells) {
      perunSession.getPerunBl().getModulesUtilsBl().checkFormatOfShell(st, attribute);
    }
  }

  /**
   * Fills the list of shells at the specified resource from facility
   */
  @Override
  public Attribute fillAttribute(PerunSessionImpl perunSession, Resource resource, AttributeDefinition attribute)
      throws WrongAttributeAssignmentException {
    Attribute atr = new Attribute(attribute);
    Facility facility = perunSession.getPerunBl().getResourcesManagerBl().getFacility(perunSession, resource);

    Attribute allShellsPerFacility;
    try {
      allShellsPerFacility =
          perunSession.getPerunBl().getAttributesManagerBl().getAttribute(perunSession, facility, A_F_shells);
    } catch (AttributeNotExistsException ex) {
      throw new InternalErrorException(
          "Attribute with list of shells from facility " + facility.getId() + " could not obtained.", ex);
    }

    atr.setValue(allShellsPerFacility.getValue());

    return atr;
  }

  @Override
  public AttributeDefinition getAttributeDefinition() {
    AttributeDefinition attr = new AttributeDefinition();
    attr.setNamespace(AttributesManager.NS_RESOURCE_ATTR_DEF);
    attr.setFriendlyName("shells");
    attr.setDisplayName("Available shells");
    attr.setType(ArrayList.class.getName());
    attr.setDescription("All available shells");
    return attr;
  }

  @Override
  public List<String> getDependencies() {
    List<String> dependecies = new ArrayList<>();
    dependecies.add(A_F_shells);
    return dependecies;
  }
}
