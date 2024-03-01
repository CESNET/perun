package cz.metacentrum.perun.core.impl.modules.attributes;

import cz.metacentrum.perun.core.implApi.modules.attributes.UserPersistentShadowAttributeWithConfig;

/**
 * Class for checking logins uniqueness in the namespace and filling eosc-federation-persistent id. It is only storage!
 * Use module login eosc_federation_persistent for access the value.
 */
public class urn_perun_user_attribute_def_def_login_namespace_eosc_federation_persistent_shadow
    extends UserPersistentShadowAttributeWithConfig {

  private static final String attrNameEOSC = "login-namespace:eosc-federation-persistent-shadow";

  private static final String CONFIG_EXT_SOURCE_NAME_EOSC = "extSourceNameEOSC";
  private static final String CONFIG_DOMAIN_NAME_EOSC = "domainNameEOSC";

  @Override
  public String getDescription() {
    return "Login for EOSC Federation. Do not use it directly! " +
           "Use \"user:virt:login-namespace:eosc-federation-persistent\" attribute instead.";
  }

  @Override
  public String getDisplayName() {
    return "EOSC Federation login";
  }

  @Override
  public String getDomainConfigName() {
    return CONFIG_DOMAIN_NAME_EOSC;
  }

  @Override
  public String getExtSourceConfigName() {
    return CONFIG_EXT_SOURCE_NAME_EOSC;
  }

  @Override
  public String getFriendlyName() {
    return attrNameEOSC;
  }

  @Override
  public String getFriendlyNameParameter() {
    return "eosc-federation-persistent-shadow";
  }

}
