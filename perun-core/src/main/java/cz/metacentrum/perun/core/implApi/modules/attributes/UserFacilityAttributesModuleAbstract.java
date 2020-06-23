package cz.metacentrum.perun.core.implApi.modules.attributes;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.Facility;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeAssignmentException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeValueException;
import cz.metacentrum.perun.core.api.exceptions.WrongReferenceAttributeValueException;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;

/**
 * Abstract class for Facility User Attributes modules.
 * Implements methods for modules to perform default function.
 * In the function that the method in the module does nothing, it is not necessary to implement it, simply extend this abstract class.
 *
 * @author Michal Stava <stavamichal@gmail.com>
 *
 */
public abstract class UserFacilityAttributesModuleAbstract extends AttributesModuleAbstract implements UserFacilityAttributesModuleImplApi {

	public void checkAttributeSyntax(PerunSessionImpl perunSession, User user, Facility facility, Attribute attribute) throws WrongAttributeValueException {

	}

	public void checkAttributeSemantics(PerunSessionImpl perunSession, User user, Facility facility, Attribute attribute) throws WrongReferenceAttributeValueException, WrongAttributeAssignmentException{

	}

	public Attribute fillAttribute(PerunSessionImpl session, User user, Facility facility, AttributeDefinition attribute) throws WrongAttributeAssignmentException {
		return new Attribute(attribute);
	}

	public void changedAttributeHook(PerunSessionImpl session, User user, Facility facility, Attribute attribute) {

	}
}
