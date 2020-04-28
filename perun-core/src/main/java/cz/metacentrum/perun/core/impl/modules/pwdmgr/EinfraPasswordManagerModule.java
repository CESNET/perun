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
import cz.metacentrum.perun.core.bl.PerunBl;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Map;

/**
 * Password manager for EINFRA login-namespace. It performs custom checks
 * on password strength and login format.
 *
 * It calls generic pwd manager script logic with ".einfra"
 *
 * @author Pavel Zlamal <zlamal@cesnet.cz>
 */
public class EinfraPasswordManagerModule extends GenericPasswordManagerModule {

	private final static Logger log = LoggerFactory.getLogger(EinfraPasswordManagerModule.class);

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
		if (StringUtils.isBlank(password)) {
			throw new EmptyPasswordRuntimeException("Password for " + actualLoginNamespace + ":" + passwordId + " cannot be empty.");
		}
		checkPasswordStrength(password);

		ProcessBuilder pb = new ProcessBuilder(altPasswordManagerProgram, PASSWORD_CREATE);
		// pass variables as ENV
		Map<String,String> env = pb.environment();
		env.put("PMGR_PASSWORD_ID", passwordId);
		env.put("PMGR_PASSWORD", password);
		if (StringUtils.isNotBlank(user.getDisplayName())) env.put("PMGR_CN", user.getDisplayName());
		if (StringUtils.isNotBlank(user.getFirstName())) env.put("PMGR_GIVEN_NAME", user.getFirstName());
		if (StringUtils.isNotBlank(user.getLastName())) env.put("PMGR_SN", user.getLastName());
		String mail = getMail(sess, user);
		if (StringUtils.isNotBlank(mail)) env.put("PMGR_MAIL", mail);
		String login = getEinfraLogin(sess, user);
		if (StringUtils.isNotBlank(login)) env.put("PMGR_LOGIN", login);

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

		ProcessBuilder pb = new ProcessBuilder(altPasswordManagerProgram, PASSWORD_DELETE);
		// pass variables as ENV
		Map<String,String> env = pb.environment();
		env.put("PMGR_PASSWORD_ID", passwordId);
		String login = getEinfraLogin(sess, user);
		if (StringUtils.isNotBlank(login)) env.put("PMGR_LOGIN", login);

		Process process;
		try {
			process = pb.start();
		} catch (IOException e) {
			throw new InternalErrorException(e);
		}

		handleAltPwdManagerExit(process, user, actualLoginNamespace, passwordId);

	}

	/**
	 * Retrieve "einfra" login of the user, since its necessary for alternative password backend
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
				log.warn("{} doesn't have login in namespace 'einfra', so the alternative password can't be set.", user);
				throw new LoginNotExistsRuntimeException(user+" doesn't have login in namespace 'einfra', so the alternative password can't be set.");
			}
			login = attribute.valueAsString();
		} catch (WrongAttributeAssignmentException | AttributeNotExistsException e) {
			// shouldn't happen
			log.error("We couldn't retrieve 'einfra' login for the {} to create/delete alternative password.", user, e);
			throw new InternalErrorException("We couldn't retrieve 'einfra' login for the "+user+" to create/delete alternative password.");
		}
		return login;

	}

	/**
	 * Retrieve users preferred mail
	 *
	 * @param session session
	 * @param user user to get mail for
	 * @return users preferred mail
	 */
	private String getMail(PerunSession session, User user) {

		try {
			Attribute attribute = ((PerunBl)session.getPerun()).getAttributesManagerBl().getAttribute(session, user, AttributesManager.NS_USER_ATTR_DEF+":preferredMail");
			return attribute.valueAsString();
		} catch (WrongAttributeAssignmentException | AttributeNotExistsException e) {
			// shouldn't happen
			log.error("We couldn't retrieve mail for the {} to create/delete alternative password.", user, e);
			throw new InternalErrorException("We couldn't retrieve mail for the "+user+" to create/delete alternative password.");
		}

	}

	private void checkLoginFormat(String userLogin) {
		// TODO
	}

	private void checkPasswordStrength(String password) {
		// TODO
	}

}
