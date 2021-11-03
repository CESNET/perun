package cz.metacentrum.perun.core.impl.modules.attributes;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.exceptions.AlreadyReservedLoginException;
import cz.metacentrum.perun.core.api.exceptions.ConsistencyErrorException;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeAssignmentException;
import cz.metacentrum.perun.core.api.exceptions.WrongReferenceAttributeValueException;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;
import cz.metacentrum.perun.core.implApi.modules.attributes.UserAttributesModuleImplApi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Module for EINFRA login namespace improves login checks with case-insensitive search!
 *
 * @see cz.metacentrum.perun.core.impl.modules.pwdmgr.EinfraPasswordManagerModule
 *
 * @author Pavel Zlámal <zlamal@cesnet.cz>
 */
public class urn_perun_user_attribute_def_def_login_namespace_einfra extends urn_perun_user_attribute_def_def_login_namespace implements UserAttributesModuleImplApi {

	private final static Logger log = LoggerFactory.getLogger(urn_perun_user_attribute_def_def_login_namespace_einfra.class);

	/**
	 * Checks if the user's login is unique in the namespace organization
	 *
	 * @param sess PerunSession
	 * @param user User to check attribute for
	 * @param attribute Attribute to check value to
	 * @throws InternalErrorException
	 * @throws WrongReferenceAttributeValueException
	 * @throws WrongAttributeAssignmentException
	 */
	@Override
	public void checkAttributeSemantics(PerunSessionImpl sess, User user, Attribute attribute) throws WrongReferenceAttributeValueException, WrongAttributeAssignmentException {

		if (attribute.getValue() == null) throw new WrongReferenceAttributeValueException(attribute, null, user, null, "Value can't be null");

		List<User> usersWithSameLogin = sess.getPerunBl().getUsersManagerBl().getUsersByAttribute(sess, attribute, true);

		usersWithSameLogin.remove(user); //remove self

		if (!usersWithSameLogin.isEmpty()) {
			if(usersWithSameLogin.size() > 1) throw new ConsistencyErrorException("FATAL ERROR: Duplicated Login detected." +  attribute + " " + usersWithSameLogin);
			throw new WrongReferenceAttributeValueException(attribute, attribute, user, null, usersWithSameLogin.get(0), null, "This login " + attribute.getValue() + " is already occupied.");
		}

		try {
			sess.getPerunBl().getUsersManagerBl().checkReservedLogins(sess, attribute.getFriendlyNameParameter(), attribute.valueAsString(), true);
		} catch (AlreadyReservedLoginException ex) {
			throw new WrongReferenceAttributeValueException(attribute, null, user, null, null, null, "Login in specific namespace already reserved.", ex);
		}
	}
}