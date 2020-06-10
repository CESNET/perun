package cz.metacentrum.perun.core.impl.modules.attributes;

import cz.metacentrum.perun.core.implApi.modules.attributes.UserPersistentShadowAttributeWithConfig;

/**
 * Class for checking logins uniqueness in the namespace and filling surf-ram-persistent id.
 * It is only storage! Use module login surf_ram_persistent for access the value.
 *
 */
public class urn_perun_user_attribute_def_def_login_namespace_surf_ram_persistent_shadow
		extends UserPersistentShadowAttributeWithConfig {

	private final static String attrNameSurfRam = "login-namespace:surf-ram-persistent-shadow";

	private final static String CONFIG_EXT_SOURCE_NAME_SURF_RAM = "extSourceNameSurfRam";
	private final static String CONFIG_DOMAIN_NAME_SURF_RAM = "domainNameSurfRam";

	@Override
	public String getExtSourceConfigName() {
		return CONFIG_EXT_SOURCE_NAME_SURF_RAM;
	}

	@Override
	public String getDomainConfigName() {
		return CONFIG_DOMAIN_NAME_SURF_RAM;
	}

	@Override
	public String getFriendlyName() {
		return attrNameSurfRam;
	}

	@Override
	public String getDescription() {
		return "Login for SURF RAM. Do not use it directly! " +
			   "Use \"user:virt:login-namespace:surf-ram-persistent\" attribute instead.";
	}

	@Override
	public String getDisplayName() {
		return "SURF RAM login";
	}

	@Override
	public String getFriendlyNameParameter() {
		return "surf-ram-persistent-shadow";
	}
}
