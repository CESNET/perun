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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Milan Halenar <255818@mail.muni.cz>
 * @date 27.4.2011
 */
public class urn_perun_facility_attribute_def_def_homeMountPoints extends FacilityAttributesModuleAbstract
    implements FacilityAttributesModuleImplApi {

  private static final Pattern pattern = Pattern.compile("^/[-a-zA-Z.0-9_/]*$");

  /**
   * Checks attribute facility_homeMountPoints, this attribute must be valid *nix path
   *
   * @param perunSession current session
   * @param facility     facility to which this attribute belongs
   * @param attribute    checked attribute
   * @throws WrongAttributeValueException if the attribute value has wrong/illegal syntax
   */
  @Override
  public void checkAttributeSyntax(PerunSessionImpl perunSession, Facility facility, Attribute attribute)
      throws WrongAttributeValueException {

    if (attribute.getValue() == null) {
      return;
    }

    List<String> homeMountPoints = attribute.valueAsList();
    for (String st : homeMountPoints) {
      Matcher match = pattern.matcher(st);
      if (!match.matches()) {
        throw new WrongAttributeValueException(attribute, "Bad homeMountPoints attribute format " + st);
      }
    }
  }

  /**
   * Checks attribute facility_homeMountPoints, this attribute must not be null
   *
   * @param perunSession current session
   * @param facility     facility to which this attribute belongs
   * @param attribute    checked attribute
   * @throws WrongReferenceAttributeValueException if the attribute value is null
   */
  @Override
  public void checkAttributeSemantics(PerunSessionImpl perunSession, Facility facility, Attribute attribute)
      throws WrongReferenceAttributeValueException {
    if (attribute.getValue() == null) {
      throw new WrongReferenceAttributeValueException(attribute, "Attribute cannot be null.");
    }
  }

  @Override
  public Attribute fillAttribute(PerunSessionImpl session, Facility facility, AttributeDefinition attribute) {
    return new Attribute(attribute);
  }

  @Override
  public AttributeDefinition getAttributeDefinition() {
    AttributeDefinition attr = new AttributeDefinition();
    attr.setNamespace(AttributesManager.NS_FACILITY_ATTR_DEF);
    attr.setFriendlyName("homeMountPoints");
    attr.setDisplayName("Home mount points");
    attr.setType(ArrayList.class.getName());
    attr.setDescription("All available home mount points.");
    return attr;
  }

}
