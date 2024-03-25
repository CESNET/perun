package cz.metacentrum.perun.core.impl.modules.attributes;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.AttributesManager;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.exceptions.ConsistencyErrorException;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.UserNotExistsException;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;
import cz.metacentrum.perun.core.implApi.modules.attributes.UserAttributesModuleAbstract;
import cz.metacentrum.perun.core.implApi.modules.attributes.UserAttributesModuleImplApi;

/**
 * Update title before name on User if value in attribute is changed.
 *
 * @author Pavel Zlámal <zlamal@cesnet.cz>
 */
public class urn_perun_user_attribute_def_def_titleBeforeDc2 extends UserAttributesModuleAbstract
    implements UserAttributesModuleImplApi {

  /**
   * When title before name from DC2 changes, update User.
   *
   * @param session
   * @param user
   * @param attribute
   * @throws InternalErrorException
   */
  @Override
  public void changedAttributeHook(PerunSessionImpl session, User user, Attribute attribute) {

    user.setTitleBefore((String) attribute.getValue());
    try {
      session.getPerunBl().getUsersManagerBl().updateNameTitles(session, user);
    } catch (UserNotExistsException e) {
      throw new ConsistencyErrorException("User we set attributes for doesn't exists!", e);
    }

  }

  @Override
  public AttributeDefinition getAttributeDefinition() {
    AttributeDefinition attr = new AttributeDefinition();
    attr.setNamespace(AttributesManager.NS_USER_ATTR_DEF);
    attr.setFriendlyName("titleBeforeDc2");
    attr.setDisplayName("Title before (DC2)");
    attr.setType(Integer.class.getName());
    attr.setDescription("Title before name from DC2.");
    return attr;
  }

}
