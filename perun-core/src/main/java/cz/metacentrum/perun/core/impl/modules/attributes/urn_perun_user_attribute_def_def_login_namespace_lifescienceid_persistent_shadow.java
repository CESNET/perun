package cz.metacentrum.perun.core.impl.modules.attributes;

import cz.metacentrum.perun.core.implApi.modules.attributes.UserPersistentShadowAttribute;

/**
 * Class for checking logins uniqueness in the namespace and filling lifescienceid-persistent id.
 * It is only storage! Use module login lifescienceid_persistent for access the value.
 *
 * @author Pavel Zlámal <zlamal@cesnet.cz>
 */
public class urn_perun_user_attribute_def_def_login_namespace_lifescienceid_persistent_shadow
		extends UserPersistentShadowAttribute {

	private final static String extSourceNamelifescienceid = "https://proxy.lifescienceid.org/proxy";
	private final static String domainNamelifescienceid = "lifescienceid.org";
	private final static String attrNamelifescienceid = "login-namespace:lifescienceid-persistent-shadow";

	@Override
	public String getFriendlyName() {
		return attrNamelifescienceid;
	}

	@Override
	public String getExtSourceName() {
		return extSourceNamelifescienceid;
	}

	@Override
	public String getDomainName() {
		return domainNamelifescienceid;
	}

	@Override
	public String getDescription() {
		return "Login to Lifescienceid. Do not use it directly! " +
			   "Use \"user:virt:login-namespace:lifescienceid-persistent\" attribute instead.";
	}

	@Override
	public String getDisplayName() {
		return "Lifescienceid login";
	}

	@Override
	public String getFriendlyNameParameter() {
		return "lifescienceid-persistent-shadow";
	}
}
