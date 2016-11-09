package cz.metacentrum.perun.core.implApi.modules.attributes;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.UserExtSource;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeAssignmentException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeValueException;
import cz.metacentrum.perun.core.api.exceptions.WrongReferenceAttributeValueException;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;

/**
 * Abstract class for UserExtSource Attributes modules.
 * -----------------------------------------------------------------------------
 * Implements methods for modules to perform default function.
 * In the function that the method in the module does nothing, it is not necessary to implement it, simply extend this abstract class.
 *
 * @author Jan Zvěřina <zverina.jan@email.cz>
 *
 */
public abstract class UserExtSourceAttributesModuleAbstract extends AttributesModuleAbstract implements UserExtSourceAttributesModuleImplApi {

	public void checkAttributeValue(PerunSessionImpl perunSession, UserExtSource ues, Attribute attribute) throws InternalErrorException, WrongAttributeValueException, WrongReferenceAttributeValueException, WrongAttributeAssignmentException {

	}

	public Attribute fillAttribute(PerunSessionImpl session, UserExtSource ues, AttributeDefinition attribute) throws InternalErrorException, WrongAttributeAssignmentException {
		return new Attribute(attribute);
	}

	public void changedAttributeHook(PerunSessionImpl session, UserExtSource ues, Attribute attribute) throws InternalErrorException, WrongReferenceAttributeValueException {

	}
}
