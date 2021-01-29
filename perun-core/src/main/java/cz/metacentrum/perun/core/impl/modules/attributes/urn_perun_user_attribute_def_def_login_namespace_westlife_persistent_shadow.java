package cz.metacentrum.perun.core.impl.modules.attributes;

import cz.metacentrum.perun.core.implApi.modules.attributes.UserPersistentShadowAttribute;

/**
 * Class for checking logins uniqueness in the namespace and filling westlife-persistent id.
 * It is only storage! Use module login westlife_persistent for access the value.
 *
 * @author Pavel Zlámal <zlamal@cesnet.cz>
 */
@Deprecated
public class urn_perun_user_attribute_def_def_login_namespace_westlife_persistent_shadow
		extends UserPersistentShadowAttribute {

	private final static String extSourceNameWestlife = "https://auth.west-life.eu/proxy/saml2/idp/metadata.php";
	private final static String domainNameWestlife = "west-life.eu";
	private final static String attrNameWestlife = "login-namespace:westlife-persistent-shadow";

	@Override
	public String getFriendlyName() {
		return attrNameWestlife;
	}

	@Override
	public String getExtSourceName() {
		return extSourceNameWestlife;
	}

	@Override
	public String getDomainName() {
		return domainNameWestlife;
	}

	@Override
	public String getDescription() {
		return "Login to West-life. Do not use it directly! " +
			   "Use \"user:virt:login-namespace:westlife-persistent\" attribute instead.";
	}

	@Override
	public String getDisplayName() {
		return "WEST-LIFE login";
	}

	@Override
	public String getFriendlyNameParameter() {
		return "westlife-persistent-shadow";
	}
}
