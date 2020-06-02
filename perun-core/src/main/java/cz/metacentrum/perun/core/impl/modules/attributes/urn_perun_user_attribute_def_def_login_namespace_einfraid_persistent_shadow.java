package cz.metacentrum.perun.core.impl.modules.attributes;

import cz.metacentrum.perun.core.implApi.modules.attributes.UserPersistentShadowAttribute;

/**
 * Class for checking logins uniqueness in the namespace and filling einfraid-persistent id.
 * It is only storage! Use module login elixir_persistent for access the value.
 *
 * @author Pavel Zlámal <zlamal@cesnet.cz>
 */
public class urn_perun_user_attribute_def_def_login_namespace_einfraid_persistent_shadow
		extends UserPersistentShadowAttribute {

	private final static String extSourceNameEinfraid = "https://login.cesnet.cz/idp/";
	private final static String domainNameEinfraid = "einfra.cesnet.cz";
	private final static String attrNameEinfraid = "login-namespace:einfraid-persistent-shadow";

	@Override
	public String getFriendlyName() {
		return attrNameEinfraid;
	}

	@Override
	public String getExtSourceName() {
		return extSourceNameEinfraid;
	}

	@Override
	public String getDomainName() {
		return domainNameEinfraid;
	}

	@Override
	public String getDescription() {
		return "Login to EINFRA ID. Do not use it directly! Use virt:einfraid-persistent attribute instead.";
	}

	@Override
	public String getDisplayName() {
		return "EINFRA ID login";
	}

	@Override
	public String getFriendlyNameParameter() {
		return "einfraid-persistent-shadow";
	}
}
