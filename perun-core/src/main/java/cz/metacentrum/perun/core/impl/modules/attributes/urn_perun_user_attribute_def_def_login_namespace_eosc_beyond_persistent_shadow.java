package cz.metacentrum.perun.core.impl.modules.attributes;

import cz.metacentrum.perun.core.implApi.modules.attributes.UserPersistentShadowAttributeWithConfig;

/**
 * Class for checking logins uniqueness in the namespace and filling eosc-beyond-persistent id. It is only storage!
 * Use module login eosc_beyond_persistent for access the value.
 */
public class urn_perun_user_attribute_def_def_login_namespace_eosc_beyond_persistent_shadow
    extends UserPersistentShadowAttributeWithConfig {

  private static final String attrNameEOSC = "login-namespace:eosc-beyond-persistent-shadow";

  private static final String CONFIG_EXT_SOURCE_NAME_EOSC = "extSourceNameEOSC";
  private static final String CONFIG_DOMAIN_NAME_EOSC = "domainNameEOSC";

  @Override
  public String getDescription() {
    return "Login for EOSC Beyond. Do not use it directly! " +
           "Use \"user:virt:login-namespace:eosc-beyond-persistent\" attribute instead.";
  }

  @Override
  public String getDisplayName() {
    return "EOSC Beyond login";
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
    return "eosc-beyond-persistent-shadow";
  }

}
