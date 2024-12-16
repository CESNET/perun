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
 * Class for access def:login-namespace:elterid-persistent-shadow attribute. It generates value if you call it for the
 * first time.
 */
@SkipValueCheckDuringDependencyCheck
public class urn_perun_user_attribute_def_virt_login_namespace_elterid_persistent
    extends UserVirtualAttributesModuleAbstract {

  public static final String SHADOW = "urn:perun:user:attribute-def:def:login-namespace:elterid-persistent-shadow";

  @Override
  public AttributeDefinition getAttributeDefinition() {
    AttributeDefinition attr = new AttributeDefinition();
    attr.setNamespace(AttributesManager.NS_USER_ATTR_VIRT);
    attr.setFriendlyName("login-namespace:elterid-persistent");
    attr.setDisplayName("eLTER AAI ID login");
    attr.setType(String.class.getName());
    attr.setDescription("Login to eLTER AAI ID. It is set automatically with first call.");
    return attr;
  }

  @Override
  public Attribute getAttributeValue(PerunSessionImpl sess, User user, AttributeDefinition attributeDefinition) {
    Attribute elterPersistent = new Attribute(attributeDefinition);

    try {
      Attribute elterPersistentShadow = sess.getPerunBl().getAttributesManagerBl().getAttribute(sess, user, SHADOW);

      if (elterPersistentShadow.getValue() == null) {

        elterPersistentShadow =
            sess.getPerunBl().getAttributesManagerBl().fillAttribute(sess, user, elterPersistentShadow);

        if (elterPersistentShadow.getValue() == null) {
          throw new InternalErrorException("eLTER AAI ID couldn't be set automatically");
        }
        sess.getPerunBl().getAttributesManagerBl().setAttribute(sess, user, elterPersistentShadow);
      }

      elterPersistent.setValue(elterPersistentShadow.getValue());
      return elterPersistent;

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
