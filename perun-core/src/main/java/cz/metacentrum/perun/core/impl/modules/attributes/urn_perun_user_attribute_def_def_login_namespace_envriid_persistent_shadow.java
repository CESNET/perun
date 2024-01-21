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
 * Class for checking logins uniqueness in the namespace and filling envriid-persistent id.
 * It is only storage! Use module login envriid_persistent for access the value.
 */
public class urn_perun_user_attribute_def_def_login_namespace_envriid_persistent_shadow extends UserPersistentShadowAttribute {

    private final static Logger log = LoggerFactory.getLogger(urn_perun_user_attribute_def_def_login_namespace_envriid_persistent_shadow.class);

    private final static String extSourceName = "https://login.envri.perun-aai.org/idp/";
    private final static String domainName = "login.envri.perun-aai.org";
    private final static String attrName = "login-namespace:envriid-persistent-shadow";

    @Override
    public String getFriendlyName() {
        return attrName;
    }

    @Override
    public String getExtSourceName() {
        return extSourceName;
    }

    @Override
    public String getDomainName() {
        return domainName;
    }

    @Override
    public String getDescription() {
        return "Login to ENVRI ID. Do not use it directly! Use virt:envriid-persistent attribute instead.";
    }

    @Override
    public String getDisplayName() {
        return "ENVRI ID login";
    }

    @Override
    public String getFriendlyNameParameter() {
        return "envriid-persistent-shadow";
    }

}
