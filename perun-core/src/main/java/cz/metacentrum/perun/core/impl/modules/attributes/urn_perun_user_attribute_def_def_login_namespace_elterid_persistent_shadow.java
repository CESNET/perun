package cz.metacentrum.perun.core.impl.modules.attributes;

import cz.metacentrum.perun.core.implApi.modules.attributes.UserPersistentShadowAttribute;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class for checking logins uniqueness in the namespace and filling elterid-persistent id. It is only storage! Use
 * module login elterid_persistent for access the value.
 */
public class urn_perun_user_attribute_def_def_login_namespace_elterid_persistent_shadow
    extends UserPersistentShadowAttribute {

  private static final Logger LOG =
      LoggerFactory.getLogger(urn_perun_user_attribute_def_def_login_namespace_elterid_persistent_shadow.class);

  private static final String extSourceName = "https://login.aai.elter-ri.eu/idp/";
  private static final String domainName = "aai.elter-ri.eu";
  private static final String attrName = "login-namespace:elterid-persistent-shadow";

  @Override
  public String getDescription() {
    return "Login to eLTER AAI ID. Do not use it directly! Use virt:elterid-persistent attribute instead.";
  }

  @Override
  public String getDisplayName() {
    return "eLTER AAI ID login";
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
    return "elterid-persistent-shadow";
  }

}
