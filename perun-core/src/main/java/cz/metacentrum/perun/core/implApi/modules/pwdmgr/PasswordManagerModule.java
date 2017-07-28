package cz.metacentrum.perun.core.implApi.modules.pwdmgr;

import cz.metacentrum.perun.core.api.AttributesManager;
import cz.metacentrum.perun.core.api.PerunSession;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.LoginNotExistsException;

import java.util.Map;

/**
 * Interface defining function of password manager module.
 * Each login-namespace in Perun can define own Module.
 *
 * @author Pavel Zlámal <zlamal@cesnet.cz>
 */
public interface PasswordManagerModule {

	//keys for input parameters map
	String FIRST_NAME_KEY = "urn:perun:user:attribute-def:core:firstName";
	String LAST_NAME_KEY = "urn:perun:user:attribute-def:core:lastName";
	String TITLE_BEFORE_KEY = "urn:perun:user:attribute-def:core:titleBefore";
	String TITLE_AFTER_KEY = "urn:perun:user:attribute-def:core:titleAfter";
	String BIRTH_DAY_KEY = "urn:perun:user:attribute-def:def:birthDay";
	String BIRTH_NUMBER_KEY = "urn:perun:user:attribute-def:def:rc";
	String MAIL_KEY = "urn:perun:member:attribute-def:def:mail";
	String PASSWORD_KEY = "password";
	//prefix for output, add namespace
	String LOGIN_PREFIX = AttributesManager.NS_USER_ATTR_DEF + ":" + AttributesManager.LOGIN_NAMESPACE + ":";

	Map<String,String> generateAccount(PerunSession session, Map<String, String> parameters) throws InternalErrorException;

	void reservePassword(PerunSession session, String userLogin, String password) throws InternalErrorException;

	void reserveRandomPassword(PerunSession session, String userLogin) throws InternalErrorException;

	void checkPassword(PerunSession sess, String userLogin, String password) throws InternalErrorException, LoginNotExistsException;

	void changePassword(PerunSession sess, String userLogin, String newPassword) throws InternalErrorException, LoginNotExistsException;

	void validatePassword(PerunSession sess, String userLogin) throws InternalErrorException;

	void deletePassword(PerunSession sess, String userLogin) throws InternalErrorException;

}
