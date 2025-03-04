package cz.metacentrum.perun.core.impl.modules.attributes;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.AttributesManager;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.exceptions.AttributeNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeAssignmentException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeValueException;
import cz.metacentrum.perun.core.api.exceptions.WrongReferenceAttributeValueException;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;
import cz.metacentrum.perun.core.implApi.modules.attributes.UserVirtualAttributesModuleAbstract;

/**
 * Module used to generate login value based on users name.
 * Has defined counterpart attribute to actually generate/store its value.
 * DEPRECATED - login was supposed to be used as EPPN on MyAcademicID instance, but that is no longer needed.
 */
@Deprecated
public class urn_perun_user_attribute_def_virt_login_namespace_erasmus_username
    extends UserVirtualAttributesModuleAbstract {

  public static final String USERNAME_DEF = "urn:perun:user:attribute-def:def:login-namespace:erasmus-username";

  public AttributeDefinition getAttributeDefinition() {
    AttributeDefinition attr = new AttributeDefinition();
    attr.setNamespace(AttributesManager.NS_USER_ATTR_VIRT);
    attr.setFriendlyName("login-namespace:erasmus-username");
    attr.setDisplayName("ERASMUS username");
    attr.setType(String.class.getName());
    attr.setDescription("ERASMUS username. It is set automatically with first call.");
    return attr;
  }

  @Override
  public Attribute getAttributeValue(PerunSessionImpl sess, User user, AttributeDefinition attributeDefinition) {
    Attribute erasmusUsernameVirt = new Attribute(attributeDefinition);

    try {
      Attribute erasmusUsername = sess.getPerunBl().getAttributesManagerBl().getAttribute(sess, user, USERNAME_DEF);

      if (erasmusUsername.getValue() == null) {
        erasmusUsername = sess.getPerunBl().getAttributesManagerBl().fillAttribute(sess, user, erasmusUsername);

        if (erasmusUsername.getValue() == null) {
          throw new InternalErrorException("Erasmus username couldn't be set automatically");
        }
        sess.getPerunBl().getAttributesManagerBl().setAttribute(sess, user, erasmusUsername);
      }

      erasmusUsernameVirt.setValue(erasmusUsername.getValue());
      return erasmusUsernameVirt;

    } catch (WrongAttributeAssignmentException | WrongAttributeValueException | WrongReferenceAttributeValueException |
             AttributeNotExistsException e) {
      throw new InternalErrorException(e);
    }
  }
}
