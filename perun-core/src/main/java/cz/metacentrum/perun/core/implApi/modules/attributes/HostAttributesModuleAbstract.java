package cz.metacentrum.perun.core.implApi.modules.attributes;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.Host;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeValueException;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;

/**
 * Abstract class for Host Attributes modules.
 * -----------------------------------------------------------------------------
 * Implements methods for modules to perform default function.
 * In the function that the method in the module does nothing, it is not necessary to implement it, simply extend this abstract class.
 *
 * @author Peter Balcirak <peter.balcirak@gmail.com>
 *
 */
public abstract class HostAttributesModuleAbstract implements HostAttributesModuleImplApi {

	public void checkAttributeSyntax(PerunSessionImpl session, Host host, Attribute attribute) throws WrongAttributeValueException {

	}

	public void checkAttributeSemantics(PerunSessionImpl session, Host host, Attribute attribute) {

	}

	public Attribute fillAttribute(PerunSessionImpl session, Host host, AttributeDefinition attribute) {
		return new Attribute(attribute);
	}

	public void changedAttributeHook(PerunSessionImpl session, Host host, Attribute attribute) {

	}
}
