package cz.metacentrum.perun.core.impl.modules.pwdmgr;

import cz.metacentrum.perun.core.api.PerunSession;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;

/**
 * Password manager for EINFRA login-namespace. It performs custom checks
 * on password strength and login format.
 *
 * It calls generic pwd manager script logic with ".einfra"
 *
 * @author Pavel Zlamal <zlamal@cesnet.cz>
 */
public class EinfraPasswordManagerModule extends GenericPasswordManagerModule {

	public EinfraPasswordManagerModule() {

		// set proper namespace
		this.actualLoginNamespace = "einfra";

		// if we are not faking password manager by using /bin/true value in the config,
		// then append namespace to the script path to trigger correct password manager script.

		if (!binTrue.equals(this.passwordManagerProgram)) {
			passwordManagerProgram += ".einfra";
		}
		if (!binTrue.equals(this.altPasswordManagerProgram)) {
			altPasswordManagerProgram += ".einfra";
		}

	}

	@Override
	public void reservePassword(PerunSession session, String userLogin, String password) throws InternalErrorException {
		checkLoginFormat(userLogin);
		checkPasswordStrength(password);
		super.reservePassword(session, userLogin, password);
	}

	@Override
	public void reserveRandomPassword(PerunSession session, String userLogin) throws InternalErrorException {
		// FIXME - probably generate password here and perform standard reservation
		checkLoginFormat(userLogin);
		super.reserveRandomPassword(session, userLogin);
	}

	@Override
	public void changePassword(PerunSession sess, String userLogin, String newPassword) throws InternalErrorException {
		checkPasswordStrength(newPassword);
		super.changePassword(sess, userLogin, newPassword);
	}

	@Override
	public void createAlternativePassword(PerunSession sess, User user, String passwordId, String password) {
		checkPasswordStrength(password);
		super.createAlternativePassword(sess, user, passwordId, password);
	}

	private void checkLoginFormat(String userLogin) {
		// TODO
	}

	private void checkPasswordStrength(String password) {
		// TODO
	}

}
