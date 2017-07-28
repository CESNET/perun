package cz.metacentrum.perun.core.impl.modules.pwdmgr;

import cz.metacentrum.perun.core.api.PerunSession;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.LoginNotExistsException;
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
	public Map<String, String> generateAccount(PerunSession session, Map<String, String> parameters) throws InternalErrorException {
		log.debug("generateAccount(parameters={})",parameters);
		Map<String, String> result = new HashMap<>();
		result.put(LOGIN_PREFIX+"dummy", Integer.toString(9000000+RANDOM.nextInt(1000000)));
		return result;
	}

	@Override
	public void reservePassword(PerunSession session, String userLogin, String password) throws InternalErrorException {
		log.debug("reservePassword(userLogin={})",userLogin);
	}

	@Override
	public void reserveRandomPassword(PerunSession session, String userLogin) throws InternalErrorException {
		log.debug("reserveRandomPassword(userLogin={})",userLogin);
	}

	@Override
	public void checkPassword(PerunSession sess, String userLogin, String password) throws InternalErrorException, LoginNotExistsException {
		log.debug("checkPassword(userLogin={})",userLogin);
	}

	@Override
	public void changePassword(PerunSession sess, String userLogin, String newPassword) throws InternalErrorException, LoginNotExistsException {
		log.debug("changePassword(userLogin={})",userLogin);
	}

	@Override
	public void validatePassword(PerunSession sess, String userLogin) throws InternalErrorException {
		log.debug("validatePassword(userLogin={})",userLogin);
	}

	@Override
	public void deletePassword(PerunSession sess, String userLogin) throws InternalErrorException {
		log.debug("deletePassword(userLogin={})",userLogin);
	}
}
