package cz.metacentrum.perun.core.implApi.modules.attributes;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.AttributesManager;
import cz.metacentrum.perun.core.api.ExtSource;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.UserExtSource;
import cz.metacentrum.perun.core.api.exceptions.ExtSourceNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.UserExtSourceExistsException;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;
import cz.metacentrum.perun.core.impl.modules.attributes.urn_perun_user_attribute_def_def_login_namespace;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract class that can be used to created persistent shadow modules.
 *
 * @author Vojtech Sassmann <vojtech.sassmann@gmail.com>
 */
public abstract class UserPersistentShadowAttribute extends urn_perun_user_attribute_def_def_login_namespace {

  private static final Logger LOG = LoggerFactory.getLogger(UserPersistentShadowAttribute.class);

  /**
   * ChangedAttributeHook() sets UserExtSource with following properties: - extSourceType is IdP - extSourceName is
   * {getExtSourceName()} - user's extSource login is the same as his persistent attribute
   */
  @Override
  public void changedAttributeHook(PerunSessionImpl session, User user, Attribute attribute) {
    try {
      String userNamespace = attribute.getFriendlyNameParameter();

      if (userNamespace.equals(getFriendlyNameParameter()) && attribute.getValue() != null) {
        ExtSource extSource =
            session.getPerunBl().getExtSourcesManagerBl().getExtSourceByName(session, getExtSourceName());
        UserExtSource userExtSource = new UserExtSource(extSource, 0, attribute.getValue().toString());

        session.getPerunBl().getUsersManagerBl().addUserExtSource(session, user, userExtSource);
      }
    } catch (UserExtSourceExistsException ex) {
      LOG.warn("Attribute: {}, External source already exists for the user.", getFriendlyNameParameter(), ex);
    } catch (ExtSourceNotExistsException ex) {
      throw new InternalErrorException(
          "Attribute: " + getFriendlyNameParameter() + ", IdP external source doesn't exist.", ex);
    }
  }

  /**
   * Format is: "[hash]@{getDomainName()}" where [hash] represents sha1hash counted from user's id and perun instance id
   * a login-namespace name.
   */
  @Override
  public Attribute fillAttribute(PerunSessionImpl perunSession, User user, AttributeDefinition attribute) {

    Attribute filledAttribute = new Attribute(attribute);

    if (attribute.getFriendlyName().equals(getFriendlyName())) {
      String domain = "@" + getDomainName();
      filledAttribute.setValue(sha1HashCount(user, domain).toString() + domain);
      return filledAttribute;
    } else {
      // without value
      return filledAttribute;
    }
  }

  @Override
  public AttributeDefinition getAttributeDefinition() {
    AttributeDefinition attr = new AttributeDefinition();
    attr.setNamespace(AttributesManager.NS_USER_ATTR_DEF);
    attr.setFriendlyName(getFriendlyName());
    attr.setDisplayName(getDisplayName());
    attr.setType(String.class.getName());
    attr.setDescription(getDescription());
    return attr;
  }

  /**
   * Get description of the attribute.
   *
   * @return attribute's description
   */
  public abstract String getDescription();

  /**
   * Get attribute's display name.
   *
   * @return attribute's display name
   */
  public abstract String getDisplayName();

  /**
   * Get domain name that is used to fill the attribute value
   *
   * @return domain name used in fill
   */
  public abstract String getDomainName();

  /**
   * Get name of the extSource where the login will be set.
   *
   * @return extSource name for the login
   */
  public abstract String getExtSourceName();

  /**
   * Get attribute's friendly name.
   *
   * @return attribute's friendly name
   */
  public abstract String getFriendlyName();

  /**
   * Get attribute's friendly name parameter.
   *
   * @return attribute's friendly name parameter.
   */
  public abstract String getFriendlyNameParameter();
}
