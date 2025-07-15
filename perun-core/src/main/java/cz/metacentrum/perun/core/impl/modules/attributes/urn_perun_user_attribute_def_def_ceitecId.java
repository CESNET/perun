package cz.metacentrum.perun.core.impl.modules.attributes;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.AttributesManager;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.exceptions.ConsistencyErrorException;
import cz.metacentrum.perun.core.api.exceptions.WrongReferenceAttributeValueException;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;
import cz.metacentrum.perun.core.implApi.modules.attributes.UserAttributesModuleAbstract;
import cz.metacentrum.perun.core.implApi.modules.attributes.UserAttributesModuleImplApi;
import java.util.List;

/**
 * Module for CEITEC internal ID to check case-ignore uniqueness.
 *
 * @author Pavel Zlamal <zlamal@cesnet.cz>
 */
public class urn_perun_user_attribute_def_def_ceitecId extends UserAttributesModuleAbstract
    implements UserAttributesModuleImplApi {

  @Override
  public void checkAttributeSemantics(PerunSessionImpl perunSession, User user, Attribute attribute)
      throws WrongReferenceAttributeValueException {

    // non-empty value is required
    if (attribute.getValue() == null) {
      throw new WrongReferenceAttributeValueException(attribute, null, user, null, "Value can't be null");
    }

    // attribute is supposed to be unique, but we must ensure case-ignore uniqueness
    List<User> usersWithSameCeitecId =
            perunSession.getPerunBl().getUsersManagerBl().getUsersByAttribute(perunSession, attribute, true);
    usersWithSameCeitecId.remove(user); //remove self

    if (!usersWithSameCeitecId.isEmpty()) {
      if (usersWithSameCeitecId.size() > 1) {
        throw new ConsistencyErrorException(
                "Same CEITEC ID is already used." + attribute + " " + usersWithSameCeitecId);
      }
      throw new WrongReferenceAttributeValueException(attribute, attribute, user, null,
              usersWithSameCeitecId.get(0), null, "CEITEC ID: " + attribute.getValue() + " is already used.");
    }

  }

  @Override
  public AttributeDefinition getAttributeDefinition() {
    AttributeDefinition attr = new AttributeDefinition();
    attr.setNamespace(AttributesManager.NS_USER_ATTR_DEF);
    attr.setFriendlyName("ceitecId");
    attr.setDisplayName("CEITEC ID");
    attr.setType(String.class.getName());
    attr.setDescription("CEITEC ID");
    return attr;
  }

}
