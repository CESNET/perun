package cz.metacentrum.perun.core.impl.modules.pwdmgr;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributesManager;
import cz.metacentrum.perun.core.api.PerunSession;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.exceptions.AttributeNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeAssignmentException;
import cz.metacentrum.perun.core.api.exceptions.rt.EmptyPasswordRuntimeException;
import cz.metacentrum.perun.core.api.exceptions.rt.LoginNotExistsRuntimeException;
import cz.metacentrum.perun.core.api.exceptions.rt.PasswordCreationFailedRuntimeException;
import cz.metacentrum.perun.core.api.exceptions.rt.PasswordDeletionFailedRuntimeException;
import cz.metacentrum.perun.core.bl.PerunBl;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

/**
 * This module allows us to set alternative password for Samba share services.
 * We do not have logins in du-samba namespace at all. It uses "einfra" namespace login instead.
 *
 * @author Pavel Zlamal <zlamal@cesnet.cz>
 */
public class SambaduPasswordManagerModule extends GenericPasswordManagerModule {

	private final static Logger log = LoggerFactory.getLogger(SambaduPasswordManagerModule.class);

	public SambaduPasswordManagerModule() {

		// set proper namespace
		this.actualLoginNamespace = "samba-du";

		// if we are not faking password manager by using /bin/true value in the config,
		// then append namespace to the script path to trigger correct password manager script.

		if (!binTrue.equals(this.passwordManagerProgram)) {
			passwordManagerProgram += ".samba-du";
		}
		if (!binTrue.equals(this.altPasswordManagerProgram)) {
			altPasswordManagerProgram += ".samba-du";
		}

	}

	@Override
	public Map<String, String> generateAccount(PerunSession session, Map<String, String> parameters) throws InternalErrorException {
		throw new InternalErrorException("Generating account in login namespace 'samba-du' is not supported.");
	}

	@Override
	public void reservePassword(PerunSession session, String userLogin, String password) throws InternalErrorException {
		throw new InternalErrorException("Reserving password in login namespace 'samba-du' is not supported.");
	}

	@Override
	public void reserveRandomPassword(PerunSession session, String userLogin) throws InternalErrorException {
		throw new InternalErrorException("Reserving random password in login namespace 'samba-du' is not supported.");
	}

	@Override
	public void checkPassword(PerunSession sess, String userLogin, String password) {
		throw new InternalErrorException("Checking password in login namespace 'samba-du' is not supported.");
	}

	@Override
	public void changePassword(PerunSession sess, String userLogin, String newPassword) throws InternalErrorException {
		throw new InternalErrorException("Changing password in login namespace 'samba-du' is not supported.");
	}

	@Override
	public void validatePassword(PerunSession sess, String userLogin) {
		throw new InternalErrorException("Validating password in login namespace 'samba-du' is not supported.");
	}

	@Override
	public void deletePassword(PerunSession sess, String userLogin) throws InternalErrorException {
		throw new InternalErrorException("Deleting password in login namespace 'samba-du' is not supported.");
	}

	@Override
	public void createAlternativePassword(PerunSession sess, User user, String passwordId, String password) {
		if (StringUtils.isBlank(password)) {
			throw new EmptyPasswordRuntimeException("Password for " + actualLoginNamespace + ":" + passwordId + " cannot be empty.");
		}
		// TODO - enforce EINFRA password checks

		ProcessBuilder pb = new ProcessBuilder(altPasswordManagerProgram);
		// pass variables as ENV
		Map<String,String> env = pb.environment();
		// we do not store passwords under passwordId in the backend, but rather use 'einfra' login
		env.put("PMGR_LOGIN", getEinfraLogin(sess, user));
		env.put("PMGR_PASSWORD", password);

		Process process;
		try {
			process = pb.start();
		} catch (IOException e) {
			throw new InternalErrorException(e);
		}
		handleAltPwdManagerExit(process, user, actualLoginNamespace, passwordId);

	}

	@Override
	public void deleteAlternativePassword(PerunSession sess, User user, String passwordId) {
		throw new InternalErrorException("Deleting alternative password in login namespace 'samba-du' is not supported.");
	}

	/**
	 * Handle exit codes of samba-du password manager scripts
	 *
	 * @param process Running password manager script process.
	 * @param user User for which operation was performed.
	 * @param loginNamespace Namespace in which operation was performed.
	 * @param passwordId ID of alt password entry for which it was performed.
	 */
	@Override
	protected void handleAltPwdManagerExit(Process process, User user, String loginNamespace, String passwordId) {

		try {
			if (process.waitFor() != 0) {
				// on any exit code it means creation failed
				throw new PasswordCreationFailedRuntimeException("Alternative password creation failed for " + user + ". Namespace: " + loginNamespace + ", passwordId: " + passwordId + ".");
			}
		} catch (InterruptedException e) {
			throw new InternalErrorException(e);
		}

	}

	/**
	 * Retrieve "einfra" login of the user, since its necessary to set samba password at the backend.
	 *
	 * @param session session
	 * @param user user to get login for
	 * @return einfra login
	 * @throws LoginNotExistsRuntimeException if user doesn't have "einfra" login
	 */
	private String getEinfraLogin(PerunSession session, User user) {

		String login = null;
		try {
			Attribute attribute = ((PerunBl)session.getPerun()).getAttributesManagerBl().getAttribute(session, user, AttributesManager.NS_USER_ATTR_DEF+":login-namespace:einfra");
			if (attribute.getValue() == null) {
				log.warn("{} doesn't have login in namespace 'einfra', so the Samba password can't be set.", user);
				throw new LoginNotExistsRuntimeException(user+" doesn't have login in namespace 'einfra', so the Samba password can't be set.");
			}
			login = attribute.valueAsString();
		} catch (WrongAttributeAssignmentException | AttributeNotExistsException e) {
			// shouldn't happen
			log.error("We couldn't retrieve 'einfra' login for the {} to create/delete alt password for samba-du.", user, e);
			throw new InternalErrorException("We couldn't retrieve 'einfra' login for the "+user+" to create/delete alt password for samba-du.");
		}
		return login;

	}

}
