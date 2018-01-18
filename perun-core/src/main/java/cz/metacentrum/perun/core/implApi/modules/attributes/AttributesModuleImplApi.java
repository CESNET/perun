package cz.metacentrum.perun.core.implApi.modules.attributes;

import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.PerunSession;
import cz.metacentrum.perun.core.api.RichAttribute;
import cz.metacentrum.perun.core.api.Role;
import java.util.List;

/**
 * This interface serves as a template for defined common properties
 *
 * @author Michal Stava <stavamichal@gmail.com>
 */
public interface AttributesModuleImplApi {

	/**
	 * Get list of attributes whose value are used in checking of validity of this attribute.
	 * In other words any change of these attributes' values can cause that value of this attribute is no longer valid.
	 *
	 * An attribute should depend on all attributes which values are used in method "checkAttributeValue" defined in attribute module.
	 *
	 * @see cz.metacentrum.perun.core.bl.AttributesManagerBl#checkAttributeDependencies(PerunSession, RichAttribute)
	 *
	 * @return list of attributes this attribute depends on
	 */
	List<String> getDependencies();

	/**
	 *  Return list of roles, which are authorized to work with this module
	 *
	 * @return list of roles
	 */
	List<Role> getAuthorizedRoles();

	/**
	 * Return attributes definition which is represented by the module
	 *
	 * @return attribute definition
	 *
	 */
	AttributeDefinition getAttributeDefinition();
}
