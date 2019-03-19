package cz.metacentrum.perun.core.implApi.modules.attributes;

import cz.metacentrum.perun.audit.events.AuditEvent;
import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.Role;
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
		return new ArrayList<>();
	}

	public List<Role> getAuthorizedRoles() {
		return new ArrayList<>();
	}

	public AttributeDefinition getAttributeDefinition() {
		return new AttributeDefinition();
	}

	public List<AuditEvent> resolveVirtualAttributeValueChange(PerunSessionImpl perunSession, AuditEvent message) throws InternalErrorException, WrongReferenceAttributeValueException, AttributeNotExistsException, WrongAttributeAssignmentException {
		return new ArrayList<>();
	}

}
