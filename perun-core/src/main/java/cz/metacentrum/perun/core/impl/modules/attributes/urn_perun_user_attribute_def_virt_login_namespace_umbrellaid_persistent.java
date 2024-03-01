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
import cz.metacentrum.perun.core.implApi.modules.attributes.SkipValueCheckDuringDependencyCheck;
import cz.metacentrum.perun.core.implApi.modules.attributes.UserVirtualAttributesModuleAbstract;
import java.util.Collections;
import java.util.List;

/**
 * Class for access def:umbrellaid-persistent-shadow attribute. It generates a value if it is not set already.
 */
@SkipValueCheckDuringDependencyCheck
public class urn_perun_user_attribute_def_virt_login_namespace_umbrellaid_persistent
    extends UserVirtualAttributesModuleAbstract {

  public static final String SHADOW = "urn:perun:user:attribute-def:def:login-namespace:umbrellaid-persistent-shadow";

  public AttributeDefinition getAttributeDefinition() {
    AttributeDefinition attr = new AttributeDefinition();
    attr.setNamespace(AttributesManager.NS_USER_ATTR_VIRT);
    attr.setFriendlyName("login-namespace:umbrellaid-persistent");
    attr.setDisplayName("UmbrellaID login");
    attr.setType(String.class.getName());
    attr.setDescription(
        "Login for UmbrellaID. It is filled by proxy during registration or set automatically with first call.");
    return attr;
  }

  @Override
  public Attribute getAttributeValue(PerunSessionImpl sess, User user, AttributeDefinition attributeDefinition) {
    Attribute umbrellaIDPersistent = new Attribute(attributeDefinition);

    try {
      Attribute umbrellaIDPersistentShadow =
          sess.getPerunBl().getAttributesManagerBl().getAttribute(sess, user, SHADOW);

      if (umbrellaIDPersistentShadow.getValue() == null || umbrellaIDPersistentShadow.valueAsString().isEmpty()) {

        umbrellaIDPersistentShadow =
            sess.getPerunBl().getAttributesManagerBl().fillAttribute(sess, user, umbrellaIDPersistentShadow);

        if (umbrellaIDPersistentShadow.getValue() == null || umbrellaIDPersistentShadow.valueAsString().isEmpty()) {
          throw new InternalErrorException("UmbrellaID ID couldn't be set automatically");
        }
        sess.getPerunBl().getAttributesManagerBl().setAttribute(sess, user, umbrellaIDPersistentShadow);
      }

      umbrellaIDPersistent.setValue(umbrellaIDPersistentShadow.getValue());
      return umbrellaIDPersistent;

    } catch (WrongAttributeAssignmentException | WrongAttributeValueException | WrongReferenceAttributeValueException |
             AttributeNotExistsException e) {
      throw new InternalErrorException(e);
    }
  }

  @Override
  public List<String> getStrongDependencies() {
    return Collections.singletonList(SHADOW);
  }
}
