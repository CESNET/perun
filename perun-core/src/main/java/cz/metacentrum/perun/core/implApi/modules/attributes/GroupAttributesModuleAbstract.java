package cz.metacentrum.perun.core.implApi.modules.attributes;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.Group;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeAssignmentException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeValueException;
import cz.metacentrum.perun.core.api.exceptions.WrongReferenceAttributeValueException;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;

/**
 * Abstract class for Group Attributes modules.
 * ----------------------------------------------------------------------------- Implements methods for modules to
 * perform default function. In the function that the method in the module does nothing, it is not necessary to
 * implement it, simply extend this abstract class.
 *
 * @author Michal Stava <stavamichal@gmail.com>
 */
public abstract class GroupAttributesModuleAbstract extends AttributesModuleAbstract {

  public void changedAttributeHook(PerunSessionImpl session, Group group, Attribute attribute)
      throws WrongReferenceAttributeValueException {

  }

  public void checkAttributeSemantics(PerunSessionImpl perunSession, Group group, Attribute attribute)
      throws WrongReferenceAttributeValueException, WrongAttributeAssignmentException {

  }

  public void checkAttributeSyntax(PerunSessionImpl perunSession, Group group, Attribute attribute)
      throws WrongAttributeValueException {

  }

  public Attribute fillAttribute(PerunSessionImpl session, Group group, AttributeDefinition attribute)
      throws WrongAttributeAssignmentException {
    return new Attribute(attribute);
  }
}
