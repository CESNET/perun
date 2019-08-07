package cz.metacentrum.perun.core.impl.modules.attributes;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.AttributesManager;
import cz.metacentrum.perun.core.api.Group;
import cz.metacentrum.perun.core.api.Resource;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeValueException;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;
import cz.metacentrum.perun.core.implApi.modules.attributes.GroupResourceAttributesModuleAbstract;
import cz.metacentrum.perun.core.implApi.modules.attributes.GroupResourceAttributesModuleImplApi;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Module for project directory permissions.
 * Standard unix file system permissions in numeric format.
 *
 * @author Michal Stava <stavamichal@gmail.com>
 * @date 25.2.2014
 */
public class urn_perun_group_resource_attribute_def_def_projectDirPermissions extends GroupResourceAttributesModuleAbstract implements GroupResourceAttributesModuleImplApi {

	private static final Pattern pattern = Pattern.compile("^[01234567]{3}$");

	@Override
	public void checkAttributeSemantics(PerunSessionImpl sess, Group group, Resource resource, Attribute attribute) throws WrongAttributeValueException {
		Integer permissions = (Integer) attribute.getValue();
		//Permissions can be null (if null, it means DEFAULT 750)
		if (permissions == null) return;

		String perm = permissions.toString();

		//Only 3 consecutive numbers with value >=0 and <=7 are allowed
		Matcher match = pattern.matcher(perm);

		if (!match.matches()) {
			throw new WrongAttributeValueException(attribute, group, resource, "Bad format of attribute projectDirPermissions (expected something like '750').");
		}
	}

	@Override
	public AttributeDefinition getAttributeDefinition() {
		AttributeDefinition attr = new AttributeDefinition();
		attr.setNamespace(AttributesManager.NS_GROUP_RESOURCE_ATTR_DEF);
		attr.setFriendlyName("projectDirPermissions");
		attr.setDisplayName("Project directory permission");
		attr.setType(Integer.class.getName());
		attr.setDescription("Permissions (ACL) to directory, where the project exists. Standard unix file system permissions in numeric format.");
		return attr;
	}
}
