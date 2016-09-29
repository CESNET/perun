package cz.metacentrum.perun.core.implApi.modules.pwdmgr;

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

	Map<String,String> generateAccount(PerunSession session, Map<String, String> parameters) throws InternalErrorException;

	void reservePassword(PerunSession session, String userLogin, String password) throws InternalErrorException;

	void reserveRandomPassword(PerunSession session, String userLogin) throws InternalErrorException;

	void checkPassword(PerunSession sess, String userLogin, String password) throws InternalErrorException, LoginNotExistsException;

	void changePassword(PerunSession sess, String userLogin, String newPassword) throws InternalErrorException, LoginNotExistsException;

	void validatePassword(PerunSession sess, String userLogin) throws InternalErrorException;

	void deletePassword(PerunSession sess, String userLogin) throws InternalErrorException;

}
