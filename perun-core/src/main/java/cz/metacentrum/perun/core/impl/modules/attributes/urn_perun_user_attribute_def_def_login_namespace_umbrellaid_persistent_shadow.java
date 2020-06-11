package cz.metacentrum.perun.core.impl.modules.attributes;

import cz.metacentrum.perun.core.implApi.modules.attributes.UserPersistentShadowAttributeWithConfig;

/**
 * Class for checking logins uniqueness in the namespace and filling umbrellaid-persistent id.
 * It is only storage! Use module login umbrellaid_persistent for access the value.
 *
 */
public class urn_perun_user_attribute_def_def_login_namespace_umbrellaid_persistent_shadow
	extends UserPersistentShadowAttributeWithConfig {

	private final static String attrNameUmbrellaID = "login-namespace:umbrellaid-persistent-shadow";

	private final static String CONFIG_EXT_SOURCE_NAME_UMBRELLA_ID = "extSourceNameUmbrellaID";
	private final static String CONFIG_DOMAIN_NAME_UMBRELLA_ID = "domainNameUmbrellaID";

	@Override
	public String getExtSourceConfigName() {
		return CONFIG_EXT_SOURCE_NAME_UMBRELLA_ID;
	}

	@Override
	public String getDomainConfigName() {
		return CONFIG_DOMAIN_NAME_UMBRELLA_ID;
	}

	@Override
	public String getFriendlyName() {
		return attrNameUmbrellaID;
	}

	@Override
	public String getDescription() {
		return "Login for UmbrellaID. Do not use it directly! " +
			"Use \"user:virt:login-namespace:umbrellaid-persistent\" attribute instead.";
	}

	@Override
	public String getFriendlyNameParameter() {
		return "umbrellaid-persistent-shadow";
	}

	@Override
	public String getDisplayName() {
		return "UmbrellaID login";
	}
}
