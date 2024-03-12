package cz.metacentrum.perun.core.impl.modules.attributes;

import cz.metacentrum.perun.core.implApi.modules.attributes.UserPersistentShadowAttributeWithConfig;

/**
 * Class for checking logins uniqueness in the namespace and filling eurofusion-persistent id. It is only storage! Use
 * module login eurofusion_persistent for access the value.
 */
public class urn_perun_user_attribute_def_def_login_namespace_eurofusion_persistent_shadow
    extends UserPersistentShadowAttributeWithConfig {

  private static final String attrNameEuroFusion = "login-namespace:eurofusion-persistent-shadow";

  private static final String CONFIG_EXT_SOURCE_NAME_EURO_FUSION = "extSourceNameEuroFusion";
  private static final String CONFIG_DOMAIN_NAME_EURO_FUSION = "domainNameEuroFusion";

  @Override
  public String getDescription() {
    return "Login for EUROfusion. Do not use it directly! " +
           "Use \"user:virt:login-namespace:eurofusion-persistent\" attribute instead.";
  }

  @Override
  public String getDisplayName() {
    return "EUROfusion login";
  }

  @Override
  public String getDomainConfigName() {
    return CONFIG_DOMAIN_NAME_EURO_FUSION;
  }

  @Override
  public String getExtSourceConfigName() {
    return CONFIG_EXT_SOURCE_NAME_EURO_FUSION;
  }

  @Override
  public String getFriendlyName() {
    return attrNameEuroFusion;
  }

  @Override
  public String getFriendlyNameParameter() {
    return "eurofusion-persistent-shadow";
  }
}
