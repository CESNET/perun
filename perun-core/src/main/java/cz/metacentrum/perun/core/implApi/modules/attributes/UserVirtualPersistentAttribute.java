package cz.metacentrum.perun.core.implApi.modules.attributes;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.exceptions.AttributeNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeAssignmentException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeValueException;
import cz.metacentrum.perun.core.api.exceptions.WrongReferenceAttributeValueException;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;
import java.util.Collections;
import java.util.List;
import org.apache.commons.lang3.StringUtils;

/**
 * Parent class to login-namespace persistent attributes, contains mainly the common logic generating the attribute
 * value through related persistent-shadow attribute
 *
 * @author David Flor <493294@mail.mani.cz>
 */
public abstract class UserVirtualPersistentAttribute
    extends UserVirtualAttributesModuleAbstract {

  @Override
  public Attribute getAttributeValue(PerunSessionImpl sess, User user, AttributeDefinition attributeDefinition) {
    Attribute persistentAttr = new Attribute(attributeDefinition);

    boolean generationDisabled = sess.getPerunBl().getModulesUtilsBl().isNamespaceIDGenerationDisabled(sess,
        StringUtils.removeEnd(persistentAttr.getFriendlyNameParameter(), "-persistent"));


    try {
      Attribute persistentShadowAttr = sess.getPerunBl().getAttributesManagerBl()
                                           .getAttribute(sess, user, this.getShadow());

      if (!generationDisabled && persistentShadowAttr.getValue() == null) {

        persistentShadowAttr =
            sess.getPerunBl().getAttributesManagerBl().fillAttribute(sess, user, persistentShadowAttr);

        if (persistentShadowAttr.getValue() == null) {
          throw new InternalErrorException(persistentAttr.getFriendlyNameParameter() +
                                               " login couldn't be set automatically");
        }
        sess.getPerunBl().getAttributesManagerBl().setAttribute(sess, user, persistentShadowAttr);
      }

      persistentAttr.setValue(persistentShadowAttr.getValue());
      return persistentAttr;

    } catch (WrongAttributeAssignmentException | WrongAttributeValueException | WrongReferenceAttributeValueException |
             AttributeNotExistsException e) {
      throw new InternalErrorException(e);
    }
  }

  public abstract String getShadow();

  @Override
  public List<String> getStrongDependencies() {
    return Collections.singletonList(this.getShadow());
  }
}
