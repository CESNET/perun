package cz.metacentrum.perun.oidc;

import cz.metacentrum.perun.core.api.PerunClient;
import cz.metacentrum.perun.core.api.PerunSession;
import cz.metacentrum.perun.core.api.exceptions.AttributeNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.PrivilegeException;
import cz.metacentrum.perun.core.api.exceptions.RpcException;
import cz.metacentrum.perun.core.api.exceptions.UserNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeAssignmentException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

import static cz.metacentrum.perun.core.api.PerunClient.Scope.*;

/**
 * @author Ondrej Velisek <ondrejvelisek@gmail.com>
 */
public class OIDC {

    private final static Logger log = LoggerFactory.getLogger(OIDC.class);

    private final String USERINFO_METHOD = "userinfo";

    private final String BONA_FIDE_ATTR = "urn:perun:user:attribute-def:def:bonaFideStatus";
    private final String ELIXIR_ID_ATTR       = "urn:perun:user:attribute-def:virt:login-namespace:elixir-persistent";
    private final String EMAIL_ATTR     = "urn:perun:user:attribute-def:def:preferredMail";
    private final String PHONE_ATTR     = "urn:perun:user:attribute-def:def:phone";
    private final String ADDRESS_ATTR     = "urn:perun:user:attribute-def:def:address";

    public Object process(PerunSession sess, String method) throws InternalErrorException, WrongAttributeAssignmentException, UserNotExistsException, PrivilegeException {

        if (USERINFO_METHOD.equals(method)) {

            Map<String, Object> userinfo = new HashMap<>();

            for (PerunClient.Scope scope : sess.getPerunClient().getScopes()) {

                switch (scope) {


                    case OPENID:
                        userinfo.put("sub", sess.getPerunPrincipal().getUser().getId());
                        break;


                    case PROFILE:
                        userinfo.put("name", sess.getPerunPrincipal().getUser().getDisplayName());
                        userinfo.put("given_name", sess.getPerunPrincipal().getUser().getFirstName());
                        userinfo.put("family_name", sess.getPerunPrincipal().getUser().getLastName());
                        userinfo.put("middle_name", sess.getPerunPrincipal().getUser().getMiddleName());
                        break;


                    case EMAIL:
                        try {
                            Object email = sess.getPerun().getAttributesManager().getAttribute(sess, sess.getPerunPrincipal().getUser(), EMAIL_ATTR).getValue();
                            userinfo.put("email", email);
                        } catch (AttributeNotExistsException e) {
                            log.debug("Scope "+EMAIL.getSopeName()+" cannot be filled because attribute "+EMAIL_ATTR+" does not exist.");
                        }
                        break;


                    case PHONE:
                        try {
                            Object phone = sess.getPerun().getAttributesManager().getAttribute(sess, sess.getPerunPrincipal().getUser(), PHONE_ATTR).getValue();
                            userinfo.put("phone", phone);
                        } catch (AttributeNotExistsException e) {
                            log.debug("Scope "+PHONE.getSopeName()+" cannot be filled because attribute "+PHONE_ATTR+" does not exist.");
                        }
                        break;


                    case ADDRESS:
                        try {
                            Object formatted = sess.getPerun().getAttributesManager().getAttribute(sess, sess.getPerunPrincipal().getUser(), ADDRESS_ATTR).getValue();
                            Map<String, Object> address = new HashMap<>();
                            address.put("formatted", formatted);
                            userinfo.put("address", address);
                        } catch (AttributeNotExistsException e) {
                            log.debug("Scope "+ADDRESS.getSopeName()+" cannot be filled because attribute "+ADDRESS_ATTR+" does not exist.");
                        }
                        break;


                    // CUSTOM SCOPES
                    case BONA_FIDE_STATUS:
                        try {
                            Object bfs = sess.getPerun().getAttributesManager().getAttribute(sess, sess.getPerunPrincipal().getUser(), BONA_FIDE_ATTR).getValue();
                            userinfo.put(BONA_FIDE_STATUS.getSopeName(), bfs);
                        } catch (AttributeNotExistsException e) {
                            log.debug("Scope "+BONA_FIDE_STATUS.getSopeName()+" cannot be filled because attribute "+BONA_FIDE_ATTR+" does not exist.");
                        }
                        break;


                    case ELIXIR_ID:
                        try {
                            Object elixirId = sess.getPerun().getAttributesManager().getAttribute(sess, sess.getPerunPrincipal().getUser(), ELIXIR_ID_ATTR).getValue();
                            userinfo.put(ELIXIR_ID.getSopeName(), elixirId);
                        } catch (AttributeNotExistsException e) {
                            log.debug("Scope "+ELIXIR_ID.getSopeName()+" cannot be filled because attribute "+ELIXIR_ID_ATTR+" does not exist.");
                        }
                        break;

                    // TODO somehow remove duplicit code
                    case ALL:
                        userinfo.put("sub", sess.getPerunPrincipal().getUser().getId());
                        userinfo.put("name", sess.getPerunPrincipal().getUser().getDisplayName());
                        userinfo.put("given_name", sess.getPerunPrincipal().getUser().getFirstName());
                        userinfo.put("family_name", sess.getPerunPrincipal().getUser().getLastName());
                        userinfo.put("middle_name", sess.getPerunPrincipal().getUser().getMiddleName());
                        try {
                            Object email = sess.getPerun().getAttributesManager().getAttribute(sess, sess.getPerunPrincipal().getUser(), EMAIL_ATTR).getValue();
                            userinfo.put("email", email);
                        } catch (AttributeNotExistsException e) {
                            log.debug("Scope "+EMAIL.getSopeName()+" cannot be filled because attribute "+EMAIL_ATTR+" does not exist.");
                        }
                        try {
                            Object phone = sess.getPerun().getAttributesManager().getAttribute(sess, sess.getPerunPrincipal().getUser(), PHONE_ATTR).getValue();
                            userinfo.put("phone", phone);
                        } catch (AttributeNotExistsException e) {
                            log.debug("Scope "+PHONE.getSopeName()+" cannot be filled because attribute "+PHONE_ATTR+" does not exist.");
                        }
                        try {
                            Object formatted = sess.getPerun().getAttributesManager().getAttribute(sess, sess.getPerunPrincipal().getUser(), ADDRESS_ATTR).getValue();
                            Map<String, Object> address = new HashMap<>();
                            address.put("formatted", formatted);
                            userinfo.put("address", address);
                        } catch (AttributeNotExistsException e) {
                            log.debug("Scope "+ADDRESS.getSopeName()+" cannot be filled because attribute "+ADDRESS_ATTR+" does not exist.");
                        }
                        try {
                            Object bfs = sess.getPerun().getAttributesManager().getAttribute(sess, sess.getPerunPrincipal().getUser(), BONA_FIDE_ATTR).getValue();
                            userinfo.put(BONA_FIDE_STATUS.getSopeName(), bfs);
                        } catch (AttributeNotExistsException e) {
                            log.debug("Scope "+BONA_FIDE_STATUS.getSopeName()+" cannot be filled because attribute "+BONA_FIDE_ATTR+" does not exist.");
                        }
                        try {
                            Object elixirId = sess.getPerun().getAttributesManager().getAttribute(sess, sess.getPerunPrincipal().getUser(), ELIXIR_ID_ATTR).getValue();
                            userinfo.put(ELIXIR_ID.getSopeName(), elixirId);
                        } catch (AttributeNotExistsException e) {
                            log.debug("Scope "+ELIXIR_ID.getSopeName()+" cannot be filled because attribute "+ELIXIR_ID_ATTR+" does not exist.");
                        }
                        break;
                }

            }

            return userinfo;

        } else {
            throw new RpcException(RpcException.Type.UNKNOWN_METHOD, "No method "+method+" was found. Try /"+USERINFO_METHOD+" instead.");
        }

    }
}
