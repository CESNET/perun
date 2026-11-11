package cz.metacentrum.perun.core.impl.modules.attributes;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.ExtSource;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.UserExtSource;
import cz.metacentrum.perun.core.api.exceptions.ExtSourceNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.UserExtSourceExistsException;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;
import cz.metacentrum.perun.core.implApi.modules.attributes.UserPersistentShadowAttribute;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class for checking logins uniqueness in the namespace and filling einfraid-persistent id. It is only storage! Use
 * module login elixir_persistent for access the value.
 *
 * @author Pavel Zlámal <zlamal@cesnet.cz>
 */
public class urn_perun_user_attribute_def_def_login_namespace_einfraid_persistent_shadow
    extends UserPersistentShadowAttribute {

  private static final Logger LOG =
      LoggerFactory.getLogger(urn_perun_user_attribute_def_def_login_namespace_einfraid_persistent_shadow.class);

  private static final String extSourceNameEinfraid = "https://login.cesnet.cz/idp/";
  private static final String extSourceNameEinfraCZ = "https://login.e-infra.cz/idp/";
  private static final String extSourceNameCESiD = "https://login.cesid.cesnet.cz/cas/idp";
  private static final String domainNameEinfraid = "einfra.cesnet.cz";
  private static final String attrNameEinfraid = "login-namespace:einfraid-persistent-shadow";

  /**
   * ChangedAttributeHook() sets UserExtSource with following properties: - extSourceType is IdP - extSourceName is
   * {getExtSourceName()} - user's extSource login is the same as his persistent attribute
   */
  @Override
  public void changedAttributeHook(PerunSessionImpl session, User user, Attribute attribute) {
    try {

      // create default identity based on module configuration
      super.changedAttributeHook(session, user, attribute);

      // duplicate logic for e-INFRA CZ proxy identity and CESiD proxy identity
      String userNamespace = attribute.getFriendlyNameParameter();

      if (userNamespace.equals(getFriendlyNameParameter()) && attribute.getValue() != null) {
        ExtSource extSourceEinfra =
            session.getPerunBl().getExtSourcesManagerBl().getExtSourceByName(session, extSourceNameEinfraCZ);
        UserExtSource userExtSourceEinfra = new UserExtSource(extSourceEinfra, 0, attribute.getValue().toString());

        session.getPerunBl().getUsersManagerBl().addUserExtSource(session, user, userExtSourceEinfra);

        ExtSource extSourceCesid =
            session.getPerunBl().getExtSourcesManagerBl().getExtSourceByName(session, extSourceNameCESiD);
        UserExtSource userExtSourceCesid = new UserExtSource(extSourceCesid, 0, attribute.getValue().toString());

        session.getPerunBl().getUsersManagerBl().addUserExtSource(session, user, userExtSourceCesid);
      }

    } catch (UserExtSourceExistsException ex) {
      LOG.warn("Attribute: {}, External source already exists for the user.", getFriendlyNameParameter(), ex);
    } catch (ExtSourceNotExistsException ex) {
      throw new InternalErrorException(
          "Attribute: " + getFriendlyNameParameter() + ", IdP external source doesn't exist.", ex);
    }
  }

  @Override
  public String getDescription() {
    return "Login to EINFRA ID. Do not use it directly! Use virt:einfraid-persistent attribute instead.";
  }

  @Override
  public String getDisplayName() {
    return "EINFRA ID login";
  }

  @Override
  public String getDomainName() {
    return domainNameEinfraid;
  }

  @Override
  public String getExtSourceName() {
    return extSourceNameEinfraid;
  }

  @Override
  public String getFriendlyName() {
    return attrNameEinfraid;
  }

  @Override
  public String getFriendlyNameParameter() {
    return "einfraid-persistent-shadow";
  }

}
