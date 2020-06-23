package cz.metacentrum.perun.core.impl.modules.attributes;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.AttributesManager;
import cz.metacentrum.perun.core.api.Facility;
import cz.metacentrum.perun.core.api.Group;
import cz.metacentrum.perun.core.api.Resource;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.exceptions.AttributeNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.ConsistencyErrorException;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeAssignmentException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeValueException;
import cz.metacentrum.perun.core.api.exceptions.WrongReferenceAttributeValueException;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;
import cz.metacentrum.perun.core.implApi.modules.attributes.GroupResourceAttributesModuleAbstract;
import cz.metacentrum.perun.core.implApi.modules.attributes.GroupResourceAttributesModuleImplApi;

import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Module for project owner login
 *
 * @author Michal Stava <stavamichal@gmail.com>
 * @date 25.2.2014
 */
public class urn_perun_group_resource_attribute_def_def_projectOwnerLogin extends GroupResourceAttributesModuleAbstract implements GroupResourceAttributesModuleImplApi {

	private static final String A_UF_V_login = AttributesManager.NS_USER_FACILITY_ATTR_VIRT + ":login";
	private static final Pattern pattern = Pattern.compile("^[a-zA-Z0-9][-A-z0-9_.@/]*$");

	public void checkAttributeSyntax(PerunSessionImpl sess, Group group, Resource resource, Attribute attribute) throws WrongAttributeValueException {
		String ownerLogin = attribute.valueAsString();
		if (ownerLogin == null) return;

		Matcher match = pattern.matcher(ownerLogin);

		if (!match.matches()) {
			throw new WrongAttributeValueException(attribute, group, resource, "Bad format of attribute projectOwnerLogin (expected something like 'alois25').");
		}
	}

	@Override
	public void checkAttributeSemantics(PerunSessionImpl sess, Group group, Resource resource, Attribute attribute) throws WrongReferenceAttributeValueException, WrongAttributeAssignmentException {
		String ownerLogin = attribute.valueAsString();
		if (ownerLogin == null) return;

		//Get Facility from resource
		Facility facility = sess.getPerunBl().getResourcesManagerBl().getFacility(sess, resource);

		Attribute loginNamespaceAttribute = null;
		try {
			loginNamespaceAttribute = sess.getPerunBl().getAttributesManagerBl().getAttribute(sess, facility, AttributesManager.NS_FACILITY_ATTR_DEF + ":login-namespace");
		} catch (AttributeNotExistsException e) {
			throw new ConsistencyErrorException(e);
		}

		// facility has namespace for logins
		if (loginNamespaceAttribute != null && loginNamespaceAttribute.valueAsString() != null) {

			try {

				// get login attr
				AttributeDefinition loginAttributeDef = sess.getPerunBl().getAttributesManagerBl().getAttributeDefinition(sess, AttributesManager.NS_USER_ATTR_DEF + ":login-namespace:"+loginNamespaceAttribute.valueAsString());

				// check that some user have it with our value
				Attribute loginAttribute = new Attribute(loginAttributeDef);
				loginAttribute.setValue(ownerLogin);

				List<User> usersWithlogin = sess.getPerunBl().getUsersManagerBl().getUsersByAttribute(sess, loginAttribute);
				if (usersWithlogin.isEmpty()) {
					throw new WrongReferenceAttributeValueException(attribute, null, group, resource, "There is no user with login '" + ownerLogin+"' in namespace '"+loginNamespaceAttribute.valueAsString()+"'.");
				}

			} catch (AttributeNotExistsException e) {
				// there is no user login namespace attribute with namespace set on facility
				throw new ConsistencyErrorException(e);
			}

		} else {
			throw new WrongReferenceAttributeValueException(attribute, loginNamespaceAttribute, group, resource, facility, null, "Login-namespace on facility can`t be empty.");
		}

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
