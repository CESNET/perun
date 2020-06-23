package cz.metacentrum.perun.core.implApi.modules.attributes;

import cz.metacentrum.perun.audit.events.AuditEvent;
import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.exceptions.AttributeNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeAssignmentException;
import cz.metacentrum.perun.core.api.exceptions.WrongReferenceAttributeValueException;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;

import java.util.ArrayList;
import java.util.List;

/**
 * Abstract class for all attributes modules
 *
 * @author Michal Stava <stavamichal@gmail.com>
 */
public abstract class AttributesModuleAbstract implements AttributesModuleImplApi {

	public List<String> getDependencies() {
		List<String> dependecies = new ArrayList<>();
		return dependecies;
	}

	public List<String> getAuthorizedRoles() {
		List<String> roles = new ArrayList<>();
		return roles;
	}

	public AttributeDefinition getAttributeDefinition() {
		AttributeDefinition attr = new AttributeDefinition();
		return attr;
	}

	public List<AuditEvent> resolveVirtualAttributeValueChange(PerunSessionImpl perunSession, AuditEvent message) throws WrongReferenceAttributeValueException, AttributeNotExistsException, WrongAttributeAssignmentException {
		return new ArrayList<>();
	}

}
