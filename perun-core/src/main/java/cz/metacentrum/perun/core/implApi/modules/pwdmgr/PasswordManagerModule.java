package cz.metacentrum.perun.core.implApi.modules.pwdmgr;

import cz.metacentrum.perun.core.api.AttributesManager;
import cz.metacentrum.perun.core.api.PerunSession;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.exceptions.ExtSourceNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.InvalidLoginException;
import cz.metacentrum.perun.core.api.exceptions.PasswordStrengthException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeValueException;
import cz.metacentrum.perun.core.api.exceptions.WrongReferenceAttributeValueException;

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
	String ALT_PASSWORD_PREFIX = AttributesManager.NS_USER_ATTR_DEF + ":altPasswords:";

	Map<String,String> generateAccount(PerunSession sess, Map<String, String> parameters) throws PasswordStrengthException;

	void reservePassword(PerunSession sess, String userLogin, String password) throws InvalidLoginException, PasswordStrengthException;

	void reserveRandomPassword(PerunSession sess, String userLogin) throws InvalidLoginException;

	void checkPassword(PerunSession sess, String userLogin, String password);

	void changePassword(PerunSession sess, String userLogin, String newPassword) throws InvalidLoginException, PasswordStrengthException;

	void validatePassword(PerunSession sess, String userLogin, User user) throws InvalidLoginException;

	void deletePassword(PerunSession sess, String userLogin) throws InvalidLoginException;

	void createAlternativePassword(PerunSession sess, User user, String passwordId, String password) throws PasswordStrengthException;

	void deleteAlternativePassword(PerunSession sess, User user, String passwordId);

	void checkLoginFormat(PerunSession sess, String login) throws InvalidLoginException;

	void checkPasswordStrength(PerunSession sess, String login, String password) throws PasswordStrengthException;

	String generateRandomPassword(PerunSession sess, String login);

}
