package cz.metacentrum.perun.core.impl.modules.attributes;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.AttributesManager;
import cz.metacentrum.perun.core.api.Group;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;
import cz.metacentrum.perun.core.implApi.modules.attributes.GroupVirtualAttributesModuleAbstract;
import cz.metacentrum.perun.core.implApi.modules.attributes.GroupVirtualAttributesModuleImplApi;
import cz.metacentrum.perun.core.implApi.modules.attributes.SkipValueCheckDuringDependencyCheck;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.List;

/**
 * Module returns group users in JSON structure with properties: "id", "login-namespace:elixir-persistent", "login-namespace:elixir" and "preferredMail"
 * retrieved from respective user attributes.
 *
 * @author Pavel Vyskocil <vyskocilpavel@muni.cz>
 */
@SkipValueCheckDuringDependencyCheck
public class urn_perun_group_attribute_def_virt_denbiProjectMembers extends GroupVirtualAttributesModuleAbstract implements GroupVirtualAttributesModuleImplApi  {

	private static final String ELIXIR_PERSISTENT = "urn:perun:user:attribute-def:virt:login-namespace:elixir-persistent";
	private static final String PREFERRED_MAIL = "urn:perun:user:attribute-def:def:preferredMail";
	private static final String ELIXIR_LOGIN = "urn:perun:user:attribute-def:def:login-namespace:elixir";

	@Override
	public Attribute getAttributeValue(PerunSessionImpl perunSession, Group group, AttributeDefinition attribute) throws InternalErrorException {

		List<User> users = perunSession.getPerunBl().getGroupsManagerBl().getGroupUsers(perunSession, group);
		JSONArray jsonMembers = new JSONArray();
		for (User user : users) {
			JSONObject jsonUser = new JSONObject();
			try {
				List<Attribute> attributes = perunSession.getPerunBl().getAttributesManagerBl().getAttributes(perunSession, user, Arrays.asList(ELIXIR_PERSISTENT, ELIXIR_LOGIN, PREFERRED_MAIL));
				jsonUser.put("id", user.getId());
				for (Attribute attr : attributes) {
					jsonUser.put(attr.getFriendlyName(), attr.getValue());
				}
				jsonMembers.put(jsonUser);
			} catch (JSONException e){
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
