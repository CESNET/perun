package cz.metacentrum.perun.core.impl.modules.attributes;

import cz.metacentrum.perun.core.implApi.modules.attributes.UserPersistentShadowAttribute;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class for checking logins uniqueness in the namespace and filling mu-persistent id. It is only storage! Use
 * module login mu_persistent for access the value.
 */
public class urn_perun_user_attribute_def_def_login_namespace_mu_persistent_shadow
    extends UserPersistentShadowAttribute {

  private static final Logger LOG =
      LoggerFactory.getLogger(urn_perun_user_attribute_def_def_login_namespace_mu_persistent_shadow.class);

  private static final String extSourceName = "https://idp2.ics.muni.cz/idp/shibboleth";
  private static final String domainName = "muni.cz";
  private static final String attrName = "login-namespace:mu-persistent-shadow";

  @Override
  public String getDescription() {
    return "MU eduPersonUniqueId. Do not use it directly! Use virt:mu-persistent attribute instead.";
  }

  @Override
  public String getDisplayName() {
    return "MU eduPersonUniqueId";
  }

  @Override
  public String getDomainName() {
    return domainName;
  }

  @Override
  public String getExtSourceName() {
    return extSourceName;
  }

  @Override
  public String getFriendlyName() {
    return attrName;
  }

  @Override
  public String getFriendlyNameParameter() {
    return "mu-persistent-shadow";
  }

}
