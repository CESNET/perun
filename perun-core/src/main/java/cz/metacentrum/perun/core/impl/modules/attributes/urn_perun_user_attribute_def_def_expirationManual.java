package cz.metacentrum.perun.core.impl.modules.attributes;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.AttributesManager;
import cz.metacentrum.perun.core.api.BeansUtils;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeValueException;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;
import cz.metacentrum.perun.core.implApi.modules.attributes.UserAttributesModuleAbstract;
import cz.metacentrum.perun.core.implApi.modules.attributes.UserAttributesModuleImplApi;
import java.text.ParseException;
import java.util.Date;

/**
 * Checks syntax for manual expiration (used on VSUP)
 *
 * @author Pavel Zlámal <zlamal@cesnet.cz>
 */
public class urn_perun_user_attribute_def_def_expirationManual extends UserAttributesModuleAbstract
    implements UserAttributesModuleImplApi {

  @Override
  public void checkAttributeSyntax(PerunSessionImpl perunSession, User user, Attribute attribute)
      throws WrongAttributeValueException {

    String expirationTime = attribute.valueAsString();
    if (expirationTime == null) {
      return; // NULL is ok
    }

    Date testDate;
    try {
      testDate = BeansUtils.getDateFormatterWithoutTime().parse(expirationTime);
    } catch (ParseException ex) {
      throw new WrongAttributeValueException(attribute, "Date parsing failed", ex);
    }
    if (!BeansUtils.getDateFormatterWithoutTime().format(testDate).equals(expirationTime)) {
      throw new WrongAttributeValueException(attribute, "Wrong format yyyy-MM-dd expected.");
    }

  }

  @Override
  public AttributeDefinition getAttributeDefinition() {
    AttributeDefinition attr = new AttributeDefinition();
    attr.setNamespace(AttributesManager.NS_USER_ATTR_DEF);
    attr.setFriendlyName("expirationManual");
    attr.setDisplayName("Expirace (ruční)");
    attr.setType(String.class.getName());
    attr.setDescription(
        "Ruční nastavení expirace vztahu k VŠUP (platnost účtu v AD). Hodnota musí být ve tvaru: \"%Y-%m-%d\", tedy " +
        "např.: \"2017-06-27\".");
    return attr;
  }

}
