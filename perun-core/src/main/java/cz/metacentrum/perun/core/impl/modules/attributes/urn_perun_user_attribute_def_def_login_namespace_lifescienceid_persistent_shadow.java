package cz.metacentrum.perun.core.impl.modules.attributes;

import cz.metacentrum.perun.core.implApi.modules.attributes.UserPersistentShadowAttributeWithConfig;

/**
 * Class for checking logins uniqueness in the namespace and filling lifescienceid-persistent id.
 * It is only storage! Use module login lifescienceid_persistent for access the value.
 *
 * @author Pavel Zlámal <zlamal@cesnet.cz>
 */
public class urn_perun_user_attribute_def_def_login_namespace_lifescienceid_persistent_shadow
		extends UserPersistentShadowAttributeWithConfig {

	private final static String attrNameLifeScience = "login-namespace:lifescienceid-persistent-shadow";

	private final static String CONFIG_EXT_SOURCE_NAME_LIFESCIENCE = "extSourceNameLifeScience";
	private final static String CONFIG_DOMAIN_NAME_LIFESCIENCE = "domainNameLifeScience";

	@Override
	public String getExtSourceConfigName() {
		return CONFIG_EXT_SOURCE_NAME_LIFESCIENCE;
	}

	@Override
	public String getDomainConfigName() {
		return CONFIG_DOMAIN_NAME_LIFESCIENCE;
	}

	@Override
	public String getFriendlyName() {
		return attrNameLifeScience;
	}

	@Override
	public String getDescription() {
		return "Login to Lifescienceid. Do not use it directly! " +
			"Use \"user:virt:login-namespace:lifescienceid-persistent\" attribute instead.";
	}

	@Override
	public String getFriendlyNameParameter() {
		return "lifescienceid-persistent-shadow";
	}

	@Override
	public String getDisplayName() {
		return "Lifescienceid login";
	}
}
