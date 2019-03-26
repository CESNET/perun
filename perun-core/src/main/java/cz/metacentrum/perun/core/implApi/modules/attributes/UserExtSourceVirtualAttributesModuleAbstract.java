package cz.metacentrum.perun.core.implApi.modules.attributes;

import cz.metacentrum.perun.audit.events.AuditEvent;
import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.UserExtSource;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;

import java.util.ArrayList;
import java.util.List;

/**
 * Abstract class for UserExtSource Virtual Attributes modules.
 * Implements methods for modules to perform default function.
 * In the function that the method in the module does nothing, it is not necessary to implement it, simply extend this abstract class.
 *
 * @author Jan Zvěřina <zverina.jan@email.cz>
 *
 */
public abstract class UserExtSourceVirtualAttributesModuleAbstract extends UserExtSourceAttributesModuleAbstract implements UserExtSourceVirtualAttributesModuleImplApi {

	@Override
	public Attribute getAttributeValue(PerunSessionImpl perunSession, UserExtSource ues, AttributeDefinition attribute) {
		return new Attribute(attribute);
	}

	@Override
	public boolean setAttributeValue(PerunSessionImpl perunSession, UserExtSource ues, Attribute attribute) {
		return false;
	}

	@Override
	public boolean removeAttributeValue(PerunSessionImpl perunSession, UserExtSource ues, AttributeDefinition attribute) {
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
