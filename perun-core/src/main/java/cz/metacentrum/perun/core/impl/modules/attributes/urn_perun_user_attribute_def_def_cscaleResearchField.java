package cz.metacentrum.perun.core.impl.modules.attributes;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeValueException;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;
import cz.metacentrum.perun.core.implApi.modules.attributes.UserAttributesModuleAbstract;
import cz.metacentrum.perun.core.implApi.modules.attributes.UserAttributesModuleImplApi;
import java.util.Arrays;
import java.util.List;

/**
 * Check constraint on C-SCALE research field
 *
 * @author Pavel Zlámal <zlamal@cesnet.cz>
 */
public class urn_perun_user_attribute_def_def_cscaleResearchField extends UserAttributesModuleAbstract
    implements UserAttributesModuleImplApi {

  private static final List<String> allowedValues =
      Arrays.asList("atmosphere", "climate", "emergency", "energy", "land", "marine", "security", "other");

  /**
   * Checks if users research field has allowed value
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

    if (!allowedValues.contains(attribute.valueAsString())) {
      throw new WrongAttributeValueException(attribute,
          "Attribute must have one of allowed values: atmosphere, climate, emergency, energy, land, marine, security," +
          " other.");
    }
  }

}
