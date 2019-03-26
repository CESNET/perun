package cz.metacentrum.perun.core.implApi.modules.attributes;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.Host;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;

/**
 * Interface for checking and filling in host's attributes.
 *
 * @author Peter Balcirak <peter.balcirak@gmail.com>
 */
public interface HostAttributesModuleImplApi extends AttributesModuleImplApi {
	/**
	 * Checks if assigned attribute to the member is valid.
	 *
	 * @param session Perun session
	 * @param host Host
	 * @param attribute Attribute of the member.
	 *
	 */
	void checkAttributeValue(PerunSessionImpl session, Host host, Attribute attribute);

	/**
	 * Tries to fill an attribute to the specified host.
	 *
	 * @param session Perun Session
	 * @param host Host
	 * @param attribute Attribute of the member
	 * @return Attribute which MAY be filled in
	 *
	 */
	Attribute fillAttribute(PerunSessionImpl session, Host host, AttributeDefinition attribute);

	/**
	 * If you need to do some further work with other modules, this method do that
	 *
	 * @param session session
	 * @param host Host
	 * @param attribute the attribute
	 */
	void changedAttributeHook(PerunSessionImpl session, Host host, Attribute attribute);

}
