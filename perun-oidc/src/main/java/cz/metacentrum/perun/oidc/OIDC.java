package cz.metacentrum.perun.oidc;

import cz.metacentrum.perun.core.api.PerunClient;
import cz.metacentrum.perun.core.api.PerunSession;
import cz.metacentrum.perun.core.api.exceptions.AttributeNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.PrivilegeException;
import cz.metacentrum.perun.core.api.exceptions.RpcException;
import cz.metacentrum.perun.core.api.exceptions.UserNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeAssignmentException;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Ondrej Velisek <ondrejvelisek@gmail.com>
 */
public class OIDC {

    private final String USERINFO_METHOD = "userinfo";

    private final String BONA_FIDE_ATTR = "urn:perun:user:attribute-def:def:bonaFideStatus";
    private final String SUB_ATTR       = "urn:perun:user:attribute-def:virt:login-namespace:elixir-persistent";
    private final String EMAIL_ATTR     = "urn:perun:user:attribute-def:def:preferredMail";

    public Object process(PerunSession sess, String method) throws InternalErrorException, WrongAttributeAssignmentException, UserNotExistsException, AttributeNotExistsException, PrivilegeException {

        if (USERINFO_METHOD.equals(method)) {

            Map<String, Object> userinfo = new HashMap<>();

            for (PerunClient.Scope scope : sess.getPerunClient().getScopes()) {

                Object value = null;
                switch (scope) {
                    case BONA_FIDE_STATUS :
                        value = sess.getPerun().getAttributesManager().getAttribute(sess, sess.getPerunPrincipal().getUser(), BONA_FIDE_ATTR).getValue();
                        break;
                    case SUB :
                        value = sess.getPerun().getAttributesManager().getAttribute(sess, sess.getPerunPrincipal().getUser(), SUB_ATTR).getValue();
                        break;
                    case NAME :
                        value = sess.getPerunPrincipal().getUser().getDisplayName();
                        break;
                    case EMAIL :
                        value = sess.getPerun().getAttributesManager().getAttribute(sess, sess.getPerunPrincipal().getUser(), EMAIL_ATTR).getValue();
                        break;
                }
                userinfo.put(scope.getSopeName(), value);

            }

            return userinfo;

        } else {
            throw new RpcException(RpcException.Type.UNKNOWN_METHOD, "No method "+method+" was found. Try "+USERINFO_METHOD+" instead.");
        }

    }
}
