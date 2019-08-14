package cz.metacentrum.perun.core.impl.modules.attributes;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.AttributesManager;
import cz.metacentrum.perun.core.api.Group;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.exceptions.AttributeNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeAssignmentException;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;
import cz.metacentrum.perun.core.implApi.modules.attributes.GroupVirtualAttributesModuleAbstract;
import cz.metacentrum.perun.core.implApi.modules.attributes.GroupVirtualAttributesModuleImplApi;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;



/**
 * Module for virtual group attribute.
 *
 * Return login-namespace:userId, elixir-persistent, preferredMail and login for all user in group as JSON.
 * @author Pavel Vyskocil <vyskocilpavel@muni.cz>
 */
public class urn_perun_group_attribute_def_virt_denbiProjectMembers extends GroupVirtualAttributesModuleAbstract implements GroupVirtualAttributesModuleImplApi  {

	public static final String USER_ID = "urn:perun:user:attribute-def:core:id";
	public static final String ELIXIR_PERSISTENT = "urn:perun:user:attribute-def:virt:login-namespace:elixir-persistent";
	public static final String PREFERRED_MAIL = "urn:perun:user:attribute-def:def:preferredMail";
	public static final String ELIXIR_LOGIN = "urn:perun:user:attribute-def:def:login-namespace:elixir";

	@Override
	public Attribute getAttributeValue(PerunSessionImpl perunSession, Group group, AttributeDefinition attribute) throws InternalErrorException {
		List<User> users = perunSession.getPerunBl().getGroupsManagerBl().getGroupUsers(perunSession, group);
		JSONArray jsonMembers = new JSONArray();
		for (User user: users) {
			JSONObject jsonUser = new JSONObject();
			try{
				Attribute userId = perunSession.getPerunBl().getAttributesManagerBl().getAttribute(perunSession, user, USER_ID);
				jsonUser.put(userId.getFriendlyName(), userId.getValue());

				Attribute elixirPersistent = perunSession.getPerunBl().getAttributesManagerBl().getAttribute(perunSession, user, ELIXIR_PERSISTENT);
				jsonUser.put(elixirPersistent.getFriendlyName(), elixirPersistent.getValue());

				Attribute preferredMail = perunSession.getPerunBl().getAttributesManagerBl().getAttribute(perunSession, user, PREFERRED_MAIL);
				jsonUser.put(preferredMail.getFriendlyName(), preferredMail.getValue());

				Attribute elixirLogin = perunSession.getPerunBl().getAttributesManagerBl().getAttribute(perunSession, user, ELIXIR_LOGIN);
				jsonUser.put(elixirLogin.getFriendlyName(), elixirLogin.getValue());

				jsonMembers.put(jsonUser);

			} catch (JSONException | AttributeNotExistsException | WrongAttributeAssignmentException e){
				throw new InternalErrorException(e);
			}
		}
		Attribute members = new Attribute(attribute);
		members.setValue(jsonMembers.toString());
		return members;
	}

	//IMPORTANT - this is very performance demanding operation, we will skip it
	/*
	@Override
	public List<String> getStrongDependencies() {
		List<String> strongDependencies = new ArrayList<>();
		strongDependencies.add(USER_ID);
		strongDependencies.add(ELIXIR_PERSISTENT);
		strongDependencies.add(PREFERRED_MAIL);
		strongDependencies.add(ELIXIR_LOGIN);
		return strongDependencies;
	}*/

	@Override
	public AttributeDefinition getAttributeDefinition() {
		AttributeDefinition attr = new AttributeDefinition();
		attr.setNamespace(AttributesManager.NS_GROUP_ATTR_VIRT);
		attr.setFriendlyName("denbiProjectMembers");
		attr.setDisplayName("Project Members");
		attr.setType(String.class.getName());
		attr.setDescription("Project Members");
		return attr;
	}
}
