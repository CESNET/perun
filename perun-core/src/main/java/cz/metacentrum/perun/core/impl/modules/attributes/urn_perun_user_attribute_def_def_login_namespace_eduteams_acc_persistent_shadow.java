package cz.metacentrum.perun.core.impl.modules.attributes;

import cz.metacentrum.perun.core.implApi.modules.attributes.UserPersistentShadowAttribute;

/**
 * Class for checking logins uniqueness in the namespace and filling eduteams-acc-persistent id.
 * It is only storage! Use module login eduteams_acc_persistent for access the value.
 *
 */
public class urn_perun_user_attribute_def_def_login_namespace_eduteams_acc_persistent_shadow
		extends UserPersistentShadowAttribute {

	private final static String extSourceNameEduteams = "https://proxy.acc.eduteams.org/proxy";
	private final static String domainNameEduteams = "eduteams.org";
	private final static String attrNameEduteams = "login-namespace:eduteams-acc-persistent-shadow";

	@Override
	public String getFriendlyName() {
		return attrNameEduteams;
	}

	@Override
	public String getExtSourceName() {
		return extSourceNameEduteams;
	}

	@Override
	public String getDomainName() {
		return domainNameEduteams;
	}

	@Override
	public String getDescription() {
		return "Login to eduTEAMS. Do not use it directly! " +
			   "Use \"user:virt:login-namespace:eduteams-acc-persistent\" attribute instead.";
	}

	@Override
	public String getDisplayName() {
		return "eduTEAMS login";
	}

	@Override
	public String getFriendlyNameParameter() {
		return "eduteams-acc-persistent-shadow";
	}
}
