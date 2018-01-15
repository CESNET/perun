package cz.metacentrum.perun.core.implApi.modules.attributes;

import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.Role;
import java.util.ArrayList;
import java.util.List;

/**
 * Abstract class for all attributes modules
 *
 * @author Michal Stava <stavamichal@gmail.com>
 */
public abstract class AttributesModuleAbstract implements AttributesModuleImplApi {

	public List<String> getDependencies() {
		List<String> dependecies = new ArrayList<String>();
		return dependecies;
	}

	public List<Role> getAuthorizedRoles() {
		List<Role> roles = new ArrayList<Role>();
		return roles;
	}

	public AttributeDefinition getAttributeDefinition() {
		AttributeDefinition attr = new AttributeDefinition();
		return attr;
	}
}
