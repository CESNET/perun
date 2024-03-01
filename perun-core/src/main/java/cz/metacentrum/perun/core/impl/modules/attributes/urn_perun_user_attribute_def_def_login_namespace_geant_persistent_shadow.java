package cz.metacentrum.perun.core.impl.modules.attributes;

import cz.metacentrum.perun.core.implApi.modules.attributes.UserPersistentShadowAttributeWithConfig;

/**
 * Class for checking logins uniqueness in the namespace and filling geant-persistent id.
 * It is only storage! Use module login geant_persistent for access the value.
 */
public class urn_perun_user_attribute_def_def_login_namespace_geant_persistent_shadow
    extends UserPersistentShadowAttributeWithConfig {

  private final static String attrNameGeant = "login-namespace:geant-persistent-shadow";

  private final static String CONFIG_EXT_SOURCE_NAME_GEANT = "extSourceNameGeant";
  private final static String CONFIG_DOMAIN_NAME_GEANT = "domainNameGeant";

  @Override
  public String getExtSourceConfigName() {
    return CONFIG_EXT_SOURCE_NAME_GEANT;
  }

  @Override
  public String getDomainConfigName() {
    return CONFIG_DOMAIN_NAME_GEANT;
  }

  @Override
  public String getFriendlyName() {
    return attrNameGeant;
  }

  @Override
  public String getDescription() {
    return "Login for GEANT. Do not use it directly! " +
        "Use \"user:virt:login-namespace:geant-persistent\" attribute instead.";
  }

  @Override
  public String getFriendlyNameParameter() {
    return "geant-persistent-shadow";
  }

  @Override
  public String getDisplayName() {
    return "GEANT login";
  }
}
