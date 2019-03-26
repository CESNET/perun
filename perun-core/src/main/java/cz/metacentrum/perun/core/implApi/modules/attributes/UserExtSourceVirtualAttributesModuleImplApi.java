package cz.metacentrum.perun.core.implApi.modules.attributes;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.UserExtSource;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;

/**
 * This interface serves as a template for virtual attributes.
 *
 * @author Jan Zvěřina <zverina.jan@email.cz>
 */
public interface UserExtSourceVirtualAttributesModuleImplApi extends UserExtSourceAttributesModuleImplApi, VirtualAttributesModuleImplApi {

	/**
	 * This method will return computed value.
	 *
	 * @param sess PerunSession
	 * @param ues UserExternalSource
	 * @param attribute attribute to operate on
	 * @return
	 */
	Attribute getAttributeValue(PerunSessionImpl sess, UserExtSource ues, AttributeDefinition attribute);

	/**
	 * Method sets attributes' values which are dependent on this virtual attribute.
	 *
	 * @param sess PerunSession
	 * @param ues UserExternalSource
	 * @param attribute attribute to operate on
	 * @return true if attribute was really changed
	 */
	boolean setAttributeValue(PerunSessionImpl sess, UserExtSource ues, Attribute attribute);

	/**
	 * Currently do nothing.
	 *
	 * @param sess PerunSession
	 * @param ues UserExternalSource
	 * @param attribute attribute to operate on
	 * @return {@code true} if attribute was changed (deleted) or {@code false} if attribute was not present in a first place
	 */
	boolean removeAttributeValue(PerunSessionImpl sess, UserExtSource ues, AttributeDefinition attribute);
}
