package cz.metacentrum.perun.oidc;

import cz.metacentrum.perun.core.api.BeansUtils;
import cz.metacentrum.perun.core.api.PerunClient;
import cz.metacentrum.perun.core.api.PerunSession;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.exceptions.AttributeNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.PrivilegeException;
import cz.metacentrum.perun.core.api.exceptions.RpcException;
import cz.metacentrum.perun.core.api.exceptions.UserNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeAssignmentException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;


/**
 * @author Ondrej Velisek <ondrejvelisek@gmail.com>
 */
public class OIDC {

    private final static Logger log = LoggerFactory.getLogger(OIDC.class);

    private final String USERINFO_METHOD = "userinfo";
    private final String PROPERTIES_FILE = "perun-oidc-scopes.properties";

    public Object process(PerunSession sess, String method) throws InternalErrorException, WrongAttributeAssignmentException, UserNotExistsException, PrivilegeException {

        if (USERINFO_METHOD.equals(method)) {

	        Map<String, String> properties = BeansUtils.getAllPropertiesFromCustomConfiguration(PROPERTIES_FILE);

            if (sess.getPerunClient().getScopes().contains(PerunClient.SCOPE_ALL)) {

                return getUserinfo(sess, properties.keySet(), properties);

            } else {

                return getUserinfo(sess, sess.getPerunClient().getScopes(), properties);

            }

        } else {
            throw new RpcException(RpcException.Type.UNKNOWN_METHOD, "No method "+method+" was found. Try /"+USERINFO_METHOD+" instead.");
        }

    }



    private Map<String, Object> getUserinfo(PerunSession sess, Collection<String> allowedScopes, Map<String, String> properties)
		    throws InternalErrorException, PrivilegeException, UserNotExistsException, WrongAttributeAssignmentException {

        User user = sess.getPerunPrincipal().getUser();
        Map<String, Object> userinfo = new HashMap<>();

        for (String scope : allowedScopes) {

	        String property = properties.get(scope);
	        if (property == null) {
		        log.warn("No attributes are mapped to scope "+scope+". Configure it in /etc/perun/"+PROPERTIES_FILE);
		        continue;
	        }

            for (String claim : Arrays.asList(property.split(","))) {
				if (claim.isEmpty()) {
					continue;
				}

	            try {
		            String claimName = claim.split("/", 2)[0];
		            String attrName = claim.split("/", 2)[1];

		            try {
			            userinfo.put(claimName, sess.getPerun().getAttributesManager().getAttribute(sess, user, attrName).getValue());

		            } catch (AttributeNotExistsException e) {
			            log.warn("Cannot map claim to perun attribute ignoring. Attribute "+attrName+" for claim "+claimName+" in scope "+scope+" does not exists. Check your /etc/perun/"+PROPERTIES_FILE+" file.", e);
		            }

	            } catch (IndexOutOfBoundsException e) {
		            throw new InternalErrorException("Properties file /etc/perun/"+PROPERTIES_FILE+" is wrongly configured.", e);
	            }

            }
        }

        return userinfo;

    }
}
