package cz.metacentrum.perun.core.impl.modules.attributes;

import cz.metacentrum.perun.core.implApi.modules.attributes.UserPersistentShadowAttributeWithConfig;

/**
 * Class for checking logins uniqueness in the namespace and filling eduteams-persistent id.
 * It is only storage! Use module login eduteams_persistent for access the value.
 *
 */
public class urn_perun_user_attribute_def_def_login_namespace_eduteams_persistent_shadow
		extends UserPersistentShadowAttributeWithConfig {

	private final static String attrNameEduTeams = "login-namespace:eduteams-persistent-shadow";

	private final static String CONFIG_EXT_SOURCE_NAME_EDUTEAMS = "extSourceNameEduTeams";
	private final static String CONFIG_DOMAIN_NAME_EDUTEAMS = "domainNameEduTeams";

	@Override
	public String getExtSourceConfigName() {
		return CONFIG_EXT_SOURCE_NAME_EDUTEAMS;
	}

	@Override
	public String getDomainConfigName() {
		return CONFIG_DOMAIN_NAME_EDUTEAMS;
	}

	@Override
	public String getFriendlyName() {
		return attrNameEduTeams;
	}

	@Override
	public String getDescription() {
		return "Login to eduTEAMS. Do not use it directly! " +
			   "Use \"user:virt:login-namespace:eduteams-persistent\" attribute instead.";
	}

	@Override
	public String getDisplayName() {
		return "eduTEAMS login";
	}

	@Override
	public String getFriendlyNameParameter() {
		return "eduteams-persistent-shadow";
	}
}
