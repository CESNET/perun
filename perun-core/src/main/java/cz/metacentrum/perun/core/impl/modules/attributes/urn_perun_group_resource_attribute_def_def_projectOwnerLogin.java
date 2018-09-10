package cz.metacentrum.perun.core.impl.modules.attributes;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.AttributesManager;
import cz.metacentrum.perun.core.api.Facility;
import cz.metacentrum.perun.core.api.Group;
import cz.metacentrum.perun.core.api.Member;
import cz.metacentrum.perun.core.api.Resource;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.exceptions.AttributeNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.ConsistencyErrorException;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeAssignmentException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeValueException;
import cz.metacentrum.perun.core.api.exceptions.WrongReferenceAttributeValueException;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;
import cz.metacentrum.perun.core.implApi.modules.attributes.ResourceGroupAttributesModuleAbstract;
import cz.metacentrum.perun.core.implApi.modules.attributes.ResourceGroupAttributesModuleImplApi;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Module for project owner login
 *
 * @author Michal Stava <stavamichal@gmail.com>
 * @date 25.2.2014
 */
public class urn_perun_group_resource_attribute_def_def_projectOwnerLogin extends ResourceGroupAttributesModuleAbstract implements ResourceGroupAttributesModuleImplApi {

	private static final String A_UF_V_login = AttributesManager.NS_USER_FACILITY_ATTR_VIRT + ":login";

	@Override
	public void checkAttributeValue(PerunSessionImpl sess, Resource resource, Group group, Attribute attribute) throws InternalErrorException, WrongAttributeValueException, WrongReferenceAttributeValueException, WrongAttributeAssignmentException {
		String ownerLogin = (String) attribute.getValue();
		if (ownerLogin == null) return;

		Pattern pattern = Pattern.compile("^[a-zA-Z0-9][-A-z0-9_.@/]*$");
		Matcher match = pattern.matcher(ownerLogin);

		if (!match.matches()) {
			throw new WrongAttributeValueException(attribute, group, resource, "Bad format of attribute projectOwnerLogin (expected something like 'alois25').");
		}

		//Get Facility from resource
		Facility facility = sess.getPerunBl().getResourcesManagerBl().getFacility(sess, resource);

		//Get all users
		List<User> users = sess.getPerunBl().getUsersManagerBl().getUsers(sess);

		//Check if exists any user with this login
		for(User u: users) {
			Attribute userLogin = null;
			try {
				userLogin = sess.getPerunBl().getAttributesManagerBl().getAttribute(sess, facility, u, A_UF_V_login);
			} catch (AttributeNotExistsException ex) {
				throw new ConsistencyErrorException("Not existing attribute user_login", ex);
			}
			if (ownerLogin.equals(userLogin.getValue())) return;
		}

		throw new WrongAttributeValueException(attribute, group, resource, "There is no user with this login:'" + ownerLogin);
	}

	@Override
	public List<String> getDependencies() {
		return Collections.singletonList(A_UF_V_login);
	}

	@Override
	public AttributeDefinition getAttributeDefinition() {
		AttributeDefinition attr = new AttributeDefinition();
		attr.setNamespace(AttributesManager.NS_GROUP_RESOURCE_ATTR_DEF);
		attr.setFriendlyName("projectOwnerLogin");
		attr.setDisplayName("Project owner login");
		attr.setType(String.class.getName());
		attr.setDescription("Login of user, who is owner of project directory.");
		return attr;
	}
}
