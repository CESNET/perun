package cz.metacentrum.perun.core.impl.modules.pwdmgr;

import cz.metacentrum.perun.core.api.PerunSession;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.exceptions.InvalidLoginException;
import cz.metacentrum.perun.core.api.exceptions.PasswordStrengthException;
import cz.metacentrum.perun.core.implApi.modules.pwdmgr.PasswordManagerModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * Dummy password module for debugging.
 *
 * @author Martin Kuba makub@ics.muni.cz
 */
public class DummyPasswordManagerModule implements PasswordManagerModule {

	private final static Logger log = LoggerFactory.getLogger(DummyPasswordManagerModule.class);

	private static final Random RANDOM = new Random();
	@Override
	public Map<String, String> generateAccount(PerunSession sess, Map<String, String> parameters) {
		log.debug("generateAccount(parameters={})",parameters);
		Map<String, String> result = new HashMap<>();
		result.put(LOGIN_PREFIX+"dummy", Integer.toString(9000000+RANDOM.nextInt(1000000)));
		return result;
	}

	@Override
	public void reservePassword(PerunSession sess, String userLogin, String password) {
		log.debug("reservePassword(userLogin={})",userLogin);
	}

	@Override
	public void reserveRandomPassword(PerunSession sess, String userLogin) {
		log.debug("reserveRandomPassword(userLogin={})",userLogin);
	}

	@Override
	public void checkPassword(PerunSession sess, String userLogin, String password) {
		log.debug("checkPassword(userLogin={})",userLogin);
	}

	@Override
	public void changePassword(PerunSession sess, String userLogin, String newPassword) {
		log.debug("changePassword(userLogin={})",userLogin);
	}

	@Override
	public void validatePassword(PerunSession sess, String userLogin) {
		log.debug("validatePassword(userLogin={})",userLogin);
	}

	@Override
	public void deletePassword(PerunSession sess, String userLogin) {
		log.debug("deletePassword(userLogin={})",userLogin);
	}

	@Override
	public void createAlternativePassword(PerunSession sess, User user, String passwordId, String password) {
		log.debug("createAlternativePassword(user={},passwordId={})", user, passwordId);
	}

	@Override
	public void deleteAlternativePassword(PerunSession sess, User user, String passwordId) {
		log.debug("deleteAlternativePassword(user={},passwordId={})", user, passwordId);
	}

	@Override
	public void checkLoginFormat(PerunSession sess, String login) {
		log.debug("checkLoginFormat(userLogin={})", login);
	}

	@Override
	public void checkPasswordStrength(PerunSession sess, String login, String password) {
		log.debug("checkPasswordStrength(userLogin={})", login);
	}

}
