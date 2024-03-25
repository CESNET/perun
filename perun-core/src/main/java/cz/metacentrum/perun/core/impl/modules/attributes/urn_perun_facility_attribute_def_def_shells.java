package cz.metacentrum.perun.core.impl.modules.attributes;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.AttributesManager;
import cz.metacentrum.perun.core.api.Facility;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeValueException;
import cz.metacentrum.perun.core.api.exceptions.WrongReferenceAttributeValueException;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;
import cz.metacentrum.perun.core.implApi.modules.attributes.FacilityAttributesModuleAbstract;
import cz.metacentrum.perun.core.implApi.modules.attributes.FacilityAttributesModuleImplApi;
import java.util.ArrayList;
import java.util.List;

/**
 * Checks if all the shells at specified facility are in proper format.
 *
 * @author Lukáš Pravda   <luky.pravda@gmail.com>
 * @date 21.4.2011 9:44:49
 */
public class urn_perun_facility_attribute_def_def_shells extends FacilityAttributesModuleAbstract
    implements FacilityAttributesModuleImplApi {

  @Override
  public void checkAttributeSemantics(PerunSessionImpl perunSession, Facility facility, Attribute attribute)
      throws WrongReferenceAttributeValueException {
    if (attribute.getValue() == null) {
      throw new WrongReferenceAttributeValueException(attribute, "This attribute cannot be null.");
    }
  }

  /**
   * Checks if the facility has properly set shells. There must be at least one shell per facility which must match
   * regular expression e.g. corretct unix path.
   */
  @Override
  public void checkAttributeSyntax(PerunSessionImpl perunSession, Facility facility, Attribute attribute)
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
   * Method for filling shells at specified facility is not implemented yet. Probably it will not be neccessary.
   */
  @Override
  public Attribute fillAttribute(PerunSessionImpl session, Facility facility, AttributeDefinition attribute) {
    return new Attribute(attribute);
  }

  @Override
  public AttributeDefinition getAttributeDefinition() {
    AttributeDefinition attr = new AttributeDefinition();
    attr.setNamespace(AttributesManager.NS_FACILITY_ATTR_DEF);
    attr.setFriendlyName("shells");
    attr.setDisplayName("Available shells");
    attr.setType(ArrayList.class.getName());
    attr.setDescription("All available shells");
    return attr;
  }

}
