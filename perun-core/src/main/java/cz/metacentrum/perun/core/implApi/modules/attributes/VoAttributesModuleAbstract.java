package cz.metacentrum.perun.core.implApi.modules.attributes;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.Vo;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeValueException;
import cz.metacentrum.perun.core.api.exceptions.WrongReferenceAttributeValueException;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;

/**
 * Abstract class for Vo Attributes modules.
 * ----------------------------------------------------------------------------- Implements methods for modules to
 * perform default function. In the function that the method in the module does nothing, it is not necessary to
 * implement it, simply extend this abstract class.
 *
 * @author Michal Stava <stavamichal@gmail.com>
 */
public abstract class VoAttributesModuleAbstract extends AttributesModuleAbstract implements VoAttributesModuleImplApi {

  public void changedAttributeHook(PerunSessionImpl session, Vo vo, Attribute attribute) {

  }

  public void checkAttributeSemantics(PerunSessionImpl perunSession, Vo vo, Attribute attribute)
      throws WrongReferenceAttributeValueException {

  }

  public void checkAttributeSyntax(PerunSessionImpl perunSession, Vo vo, Attribute attribute)
      throws WrongAttributeValueException {

  }

  public Attribute fillAttribute(PerunSessionImpl session, Vo vo, AttributeDefinition attribute) {
    return new Attribute(attribute);
  }
}
