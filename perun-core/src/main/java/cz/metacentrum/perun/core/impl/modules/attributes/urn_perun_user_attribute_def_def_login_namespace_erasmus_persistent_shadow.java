package cz.metacentrum.perun.core.impl.modules.attributes;

import cz.metacentrum.perun.core.implApi.modules.attributes.UserPersistentShadowAttributeWithConfig;

/**
 * Class for checking logins uniqueness in the namespace and filling erasmus-persistent id.
 * It is only storage! Use module login erasmus_persistent for access the value.
 */
public class urn_perun_user_attribute_def_def_login_namespace_erasmus_persistent_shadow
    extends UserPersistentShadowAttributeWithConfig {

  private final static String attrNameErasmus = "login-namespace:erasmus-persistent-shadow";

  private final static String CONFIG_EXT_SOURCE_NAME_ERASMUS = "extSourceNameErasmus";
  private final static String CONFIG_DOMAIN_NAME_ERASMUS = "domainNameErasmus";

  @Override
  public String getExtSourceConfigName() {
    return CONFIG_EXT_SOURCE_NAME_ERASMUS;
  }

  @Override
  public String getDomainConfigName() {
    return CONFIG_DOMAIN_NAME_ERASMUS;
  }

  @Override
  public String getFriendlyName() {
    return attrNameErasmus;
  }

  @Override
  public String getDescription() {
    return "Login for ERASMUS. Do not use it directly! " +
        "Use \"user:virt:login-namespace:erasmus-persistent\" attribute instead.";
  }

  @Override
  public String getFriendlyNameParameter() {
    return "erasmus-persistent-shadow";
  }

  @Override
  public String getDisplayName() {
    return "ERASMUS login";
  }
}
