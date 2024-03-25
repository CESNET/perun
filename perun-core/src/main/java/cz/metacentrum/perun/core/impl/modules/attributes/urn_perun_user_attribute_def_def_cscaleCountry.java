package cz.metacentrum.perun.core.impl.modules.attributes;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeValueException;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;
import cz.metacentrum.perun.core.implApi.modules.attributes.UserAttributesModuleAbstract;
import cz.metacentrum.perun.core.implApi.modules.attributes.UserAttributesModuleImplApi;
import java.util.Locale;
import java.util.Set;

/**
 * Check constraint on C-SCALE user country code
 *
 * @author Pavel Zlámal <zlamal@cesnet.cz>
 */
public class urn_perun_user_attribute_def_def_cscaleCountry extends UserAttributesModuleAbstract
    implements UserAttributesModuleImplApi {

  private static final Set<String> countryCodes = Set.of(Locale.getISOCountries());

  /**
   * Checks if users country has allowed value
   *
   * @param sess      PerunSession
   * @param user      user
   * @param attribute Attribute of the user
   * @throws WrongAttributeValueException if the attribute value has wrong/illegal syntax
   */
  @Override
  public void checkAttributeSyntax(PerunSessionImpl sess, User user, Attribute attribute)
      throws WrongAttributeValueException {
    if (attribute.getValue() == null) {
      return;
    }

    if (!countryCodes.contains(attribute.valueAsString())) {
      throw new WrongAttributeValueException(attribute, "Attribute value must be valid ISO 3166 Alpha-2 country code.");
    }
  }

}
