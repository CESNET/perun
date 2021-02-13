package cz.metacentrum.perun.core.impl.modules.attributes;

import cz.metacentrum.perun.core.implApi.modules.attributes.UserPersistentShadowAttributeWithConfig;

/**
 * Class for checking logins uniqueness in the namespace and filling researcher-access-persistent id.
 * It is only storage! Use module login researcher-access_persistent for access the value.
 *
 */
public class urn_perun_user_attribute_def_def_login_namespace_researcher_access_persistent_shadow
	extends UserPersistentShadowAttributeWithConfig {

	private final static String attrNameResearcherAccess = "login-namespace:researcher-access-persistent-shadow";

	private final static String CONFIG_EXT_SOURCE_NAME_RESEARCHER_ACCESS = "extSourceNameResearcherAccess";
	private final static String CONFIG_DOMAIN_NAME_RESEARCHER_ACCESS = "domainNameResearcherAccess";

	@Override
	public String getExtSourceConfigName() {
		return CONFIG_EXT_SOURCE_NAME_RESEARCHER_ACCESS;
	}

	@Override
	public String getDomainConfigName() {
		return CONFIG_DOMAIN_NAME_RESEARCHER_ACCESS;
	}

	@Override
	public String getFriendlyName() {
		return attrNameResearcherAccess;
	}

	@Override
	public String getDescription() {
		return "Login for Researcher Access. Do not use it directly! " +
			"Use \"user:virt:login-namespace:researcher-access-persistent\" attribute instead.";
	}

	@Override
	public String getFriendlyNameParameter() {
		return "researcher-access-persistent-shadow";
	}

	@Override
	public String getDisplayName() {
		return "Researcher Access login";
	}
}
