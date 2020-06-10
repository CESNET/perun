package cz.metacentrum.perun.core.impl.modules.attributes;

import cz.metacentrum.perun.core.implApi.modules.attributes.UserPersistentShadowAttributeWithConfig;

/**
 * Class for checking logins uniqueness in the namespace and filling fenix-persistent id.
 * It is only storage! Use module login fenix_persistent for access the value.
 *
 */
public class urn_perun_user_attribute_def_def_login_namespace_fenix_persistent_shadow
		extends UserPersistentShadowAttributeWithConfig {

	private final static String attrNameFenix = "login-namespace:fenix-persistent-shadow";

	private final static String CONFIG_EXT_SOURCE_NAME_FENIX = "extSourceNameFenix";
	private final static String CONFIG_DOMAIN_NAME_FENIX = "domainNameFenix";

	@Override
	public String getExtSourceConfigName() {
		return CONFIG_EXT_SOURCE_NAME_FENIX;
	}

	@Override
	public String getDomainConfigName() {
		return CONFIG_DOMAIN_NAME_FENIX;
	}

	@Override
	public String getFriendlyName() {
		return attrNameFenix;
	}

	@Override
	public String getDescription() {
		return "Login for FENIX. Do not use it directly! " +
			   "Use \"user:virt:login-namespace:fenix-persistent\" attribute instead.";
	}

	@Override
	public String getFriendlyNameParameter() {
		return "fenix-persistent-shadow";
	}

	@Override
	public String getDisplayName() {
		return "FENIX login";
	}
}
