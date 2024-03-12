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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Slavek Licehammer &lt;glory@ics.muni.cz&gt;
 */
public class urn_perun_facility_attribute_def_def_shell_passwd_scp extends FacilityAttributesModuleAbstract
    implements FacilityAttributesModuleImplApi {

  private static final Pattern pattern = Pattern.compile("^(/[-_.a-zA-Z0-9]+)+$");

  @Override
  public void checkAttributeSyntax(PerunSessionImpl perunSession, Facility facility, Attribute attribute)
      throws WrongAttributeValueException {
    String shell = attribute.valueAsString();

    if (shell == null) {
      return;
    }
    Matcher matcher = pattern.matcher(shell);
    if (!matcher.matches()) {
      throw new WrongAttributeValueException(attribute, "Wrong format. ^(/[-_.A-z0-9]+)+$ expected");
    }
  }

  @Override
  public void checkAttributeSemantics(PerunSessionImpl perunSession, Facility facility, Attribute attribute)
      throws WrongReferenceAttributeValueException {
    if (attribute.getValue() == null) {
      throw new WrongReferenceAttributeValueException(attribute, "Value can't be null");
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
    attr.setFriendlyName("shell-passwd-scp");
    attr.setDisplayName("Shell for passwd_scp");
    attr.setType(String.class.getName());
    attr.setDescription("Shell for passwd-scp service");
    return attr;
  }

}
