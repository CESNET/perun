package cz.metacentrum.perun.core.implApi.modules.attributes;

import cz.metacentrum.perun.audit.events.AuditEvent;
import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.exceptions.AttributeNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeAssignmentException;
import cz.metacentrum.perun.core.api.exceptions.WrongReferenceAttributeValueException;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;

import java.util.ArrayList;
import java.util.List;

/**
 * Abstract class for User Virtual Attributes modules.
 * Implements methods for modules to perform default function.
 * In the function that the method in the module does nothing, it is not necessary to implement it, simply extend this abstract class.
 *
 * @author Michal Stava <stavamichal@gmail.com>
 *
 */
public abstract class UserVirtualAttributesModuleAbstract extends UserAttributesModuleAbstract implements UserVirtualAttributesModuleImplApi {


	public Attribute getAttributeValue(PerunSessionImpl perunSession, User user, AttributeDefinition attribute) {
		return new Attribute(attribute);
	}

	public boolean setAttributeValue(PerunSessionImpl perunSession, User user, Attribute attribute) {
		return false;
	}

	public void removeAttributeValue(PerunSessionImpl perunSession, User user, AttributeDefinition attribute) {

	}

	@Override
	public List<AuditEvent> resolveVirtualAttributeValueChange(PerunSessionImpl perunSession, AuditEvent message) throws WrongReferenceAttributeValueException, AttributeNotExistsException, WrongAttributeAssignmentException {
		return new ArrayList<>();
	}

	@Override
	public List<User> searchInAttributesValues(PerunSessionImpl perunSession, String attribute) {
		return null;
	}

	@Override
	public List<String> getStrongDependencies() {
		return new ArrayList<>();
	}
}
