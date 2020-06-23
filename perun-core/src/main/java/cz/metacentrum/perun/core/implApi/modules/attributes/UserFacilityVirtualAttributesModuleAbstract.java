package cz.metacentrum.perun.core.implApi.modules.attributes;

import cz.metacentrum.perun.audit.events.AuditEvent;
import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.Facility;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.WrongReferenceAttributeValueException;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;

import java.util.ArrayList;
import java.util.List;

/**
 * Abstract class for Facility User Virtual Attributes modules.
 * Implements methods for modules to perform default function.
 * In the function that the method in the module does nothing, it is not necessary to implement it, simply extend this abstract class.
 *
 * @author Michal Stava <stavamichal@gmail.com>
 *
 */
public abstract class UserFacilityVirtualAttributesModuleAbstract extends UserFacilityAttributesModuleAbstract implements UserFacilityVirtualAttributesModuleImplApi {


	public Attribute getAttributeValue(PerunSessionImpl perunSession, User user, Facility facility, AttributeDefinition attribute) {
		return new Attribute(attribute);
	}

	public boolean setAttributeValue(PerunSessionImpl perunSession, User user, Facility facility, Attribute attribute) throws WrongReferenceAttributeValueException {
		return false;
	}

	public boolean removeAttributeValue(PerunSessionImpl perunSession, User user, Facility facility, AttributeDefinition attribute) {
		return false;
	}

	@Override
	public List<AuditEvent> resolveVirtualAttributeValueChange(PerunSessionImpl perunSession, AuditEvent message) {
		return new ArrayList<>();
	}

	@Override
	public List<String> getStrongDependencies() {
		List<String> dependecies = new ArrayList<>();
		return dependecies;
	}
}
