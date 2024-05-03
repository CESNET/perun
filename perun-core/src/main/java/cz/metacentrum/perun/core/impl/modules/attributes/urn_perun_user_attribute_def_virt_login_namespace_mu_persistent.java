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
 * Class for access def:login-namespace:mu-persistent-shadow attribute. It generates value if you call it for the
 * first time.
 */
@SkipValueCheckDuringDependencyCheck
public class urn_perun_user_attribute_def_virt_login_namespace_mu_persistent
    extends UserVirtualAttributesModuleAbstract {

  public static final String SHADOW = "urn:perun:user:attribute-def:def:login-namespace:mu-persistent-shadow";

  @Override
  public AttributeDefinition getAttributeDefinition() {
    AttributeDefinition attr = new AttributeDefinition();
    attr.setNamespace(AttributesManager.NS_USER_ATTR_VIRT);
    attr.setFriendlyName("login-namespace:mu-persistent");
    attr.setDisplayName("MU eduPersonUniqueId");
    attr.setType(String.class.getName());
    attr.setDescription("eduPersonUniqueId at MU. It is set automatically with first call.");
    return attr;
  }

  @Override
  public Attribute getAttributeValue(PerunSessionImpl sess, User user, AttributeDefinition attributeDefinition) {
    Attribute muPersistent = new Attribute(attributeDefinition);

    try {
      Attribute muPersistentShadow = sess.getPerunBl().getAttributesManagerBl().getAttribute(sess, user, SHADOW);

      if (muPersistentShadow.getValue() == null) {

        muPersistentShadow =
            sess.getPerunBl().getAttributesManagerBl().fillAttribute(sess, user, muPersistentShadow);

        if (muPersistentShadow.getValue() == null) {
          throw new InternalErrorException("MU eduPersonUniqueId couldn't be set automatically");
        }
        sess.getPerunBl().getAttributesManagerBl().setAttribute(sess, user, muPersistentShadow);
      }

      muPersistent.setValue(muPersistentShadow.getValue());
      return muPersistent;

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
