package cz.metacentrum.perun.core.impl.modules.attributes;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeAssignmentException;
import cz.metacentrum.perun.core.api.exceptions.WrongReferenceAttributeValueException;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;

/**
 * Class for checking login uniqueness in the namespace and filling login value (can be NULL)
 *
 * @author Johana Supikova <xsupikov@fi.muni.cz>
 */
public class urn_perun_user_attribute_def_def_login_namespace_mu_adm
    extends urn_perun_user_attribute_def_def_login_namespace {

  @Override
  public void checkAttributeSemantics(PerunSessionImpl sess, User user, Attribute attribute)
      throws WrongReferenceAttributeValueException, WrongAttributeAssignmentException {
    if (attribute.getValue() == null) {
      return;
    }

    super.checkAttributeSemantics(sess, user, attribute);
  }
}
