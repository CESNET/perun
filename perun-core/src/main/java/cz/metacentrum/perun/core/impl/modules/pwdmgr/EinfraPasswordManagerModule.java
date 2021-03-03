package cz.metacentrum.perun.core.impl.modules.pwdmgr;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributesManager;
import cz.metacentrum.perun.core.api.ExtSource;
import cz.metacentrum.perun.core.api.PerunSession;
import cz.metacentrum.perun.core.api.SponsoredUserData;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.UserExtSource;
import cz.metacentrum.perun.core.api.exceptions.AttributeNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.ExtSourceNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.InvalidLoginException;
import cz.metacentrum.perun.core.api.exceptions.InvalidSponsoredUserDataException;
import cz.metacentrum.perun.core.api.exceptions.PasswordStrengthException;
import cz.metacentrum.perun.core.api.exceptions.UserExtSourceExistsException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeAssignmentException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeValueException;
import cz.metacentrum.perun.core.api.exceptions.WrongReferenceAttributeValueException;
import cz.metacentrum.perun.core.api.exceptions.rt.LoginNotExistsRuntimeException;
import cz.metacentrum.perun.core.api.exceptions.rt.PasswordCreationFailedRuntimeException;
import cz.metacentrum.perun.core.api.exceptions.rt.PasswordDeletionFailedRuntimeException;
import cz.metacentrum.perun.core.api.exceptions.rt.PerunRuntimeException;
import cz.metacentrum.perun.core.bl.PerunBl;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Password manager for EINFRA login-namespace. It provides custom checks on login format
 * and password strength. Also implementation for alternative passwords is customized.
 *
 * It calls generic pwd manager script logic with ".einfra"
 *
 * @author Pavel Zlamal <zlamal@cesnet.cz>
 */
public class EinfraPasswordManagerModule extends GenericPasswordManagerModule {

	private final static Logger log = LoggerFactory.getLogger(EinfraPasswordManagerModule.class);

	protected final Pattern einfraLoginPattern = Pattern.compile("^[a-z][a-z0-9_-]{1,14}$");

	protected final Pattern einfraPasswordContainsDigit = Pattern.compile(".*[0-9].*");
	protected final Pattern einfraPasswordContainsLower = Pattern.compile(".*[a-z].*");
	protected final Pattern einfraPasswordContainsUpper = Pattern.compile(".*[A-Z].*");
	protected final Pattern einfraPasswordContainsSpec = Pattern.compile(".*[\\x20-\\x2F\\x3A-\\x40\\x5B-\\x60\\x7B-\\x7E].*");

	public EinfraPasswordManagerModule() {

		// set proper namespace
		this.actualLoginNamespace = "einfra";

		// if we are not faking password manager by using /bin/true value in the config,
		// then append namespace to the script path to trigger correct password manager script.

		if (!binTrue.equals(this.passwordManagerProgram)) {
			// TODO - create einfra specific standard password manager
			//passwordManagerProgram += ".einfra";
		}
		if (!binTrue.equals(this.altPasswordManagerProgram)) {
			altPasswordManagerProgram += ".einfra";
		}

	}

	@Override
	public String handleSponsorship(PerunSession sess, SponsoredUserData userData) throws InvalidLoginException, PasswordStrengthException {
		reservePassword(sess, userData.getLogin(), userData.getPassword());

		return userData.getLogin();
	}

	@Override
	public void reserveRandomPassword(PerunSession sess, String userLogin) throws InvalidLoginException {
		// FIXME - probably generate password for einfra here and perform standard reservation
		super.reserveRandomPassword(sess, userLogin);
	}

	@Override
	public void validatePassword(PerunSession sess, String userLogin, User user) throws InvalidLoginException {
		if (user == null) {
			user = ((PerunBl) sess.getPerun()).getModulesUtilsBl().getUserByLoginInNamespace(sess, userLogin, actualLoginNamespace);
		}

		if (user == null) {
			log.warn("No user was found by login '{}' in {} namespace.", userLogin, actualLoginNamespace);
		} else {
			// set extSources and extSource related attributes
			try {
				List<String> kerberosLogins = new ArrayList<>();

				// Set META and EINFRA userExtSources
				ExtSource extSource = ((PerunBl) sess.getPerun()).getExtSourcesManagerBl().getExtSourceByName(sess, "META");
				UserExtSource ues = new UserExtSource(extSource, userLogin + "@META");
				ues.setLoa(0);

				try {
					((PerunBl) sess.getPerun()).getUsersManagerBl().addUserExtSource(sess, user, ues);
				} catch (UserExtSourceExistsException ex) {
					//this is OK
				}

				extSource = ((PerunBl) sess.getPerun()).getExtSourcesManagerBl().getExtSourceByName(sess, "EINFRA");
				ues = new UserExtSource(extSource, userLogin + "@EINFRA");
				ues.setLoa(0);

				try {
					((PerunBl) sess.getPerun()).getUsersManagerBl().addUserExtSource(sess, user, ues);
				} catch (UserExtSourceExistsException ex) {
					//this is OK
				}

				extSource = ((PerunBl) sess.getPerun()).getExtSourcesManagerBl().getExtSourceByName(sess, "https://login.ics.muni.cz/idp/shibboleth");
				ues = new UserExtSource(extSource, userLogin + "@meta.cesnet.cz");
				ues.setLoa(0);

				try {
					((PerunBl) sess.getPerun()).getUsersManagerBl().addUserExtSource(sess, user, ues);
				} catch (UserExtSourceExistsException ex) {
					//this is OK
				}

				// Store also Kerberos logins
				Attribute kerberosLoginsAttr = ((PerunBl) sess.getPerun()).getAttributesManagerBl().getAttribute(sess, user, AttributesManager.NS_USER_ATTR_DEF + ":" + "kerberosLogins");
				if (kerberosLoginsAttr != null && kerberosLoginsAttr.getValue() != null) {
					kerberosLogins.addAll((List<String>) kerberosLoginsAttr.getValue());
				}

				boolean someChange = false;
				if (!kerberosLogins.contains(userLogin + "@EINFRA")) {
					kerberosLogins.add(userLogin + "@EINFRA");
					someChange = true;
				}
				if (!kerberosLogins.contains(userLogin + "@META")) {
					kerberosLogins.add(userLogin + "@META");
					someChange = true;
				}

				if (someChange && kerberosLoginsAttr != null) {
					kerberosLoginsAttr.setValue(kerberosLogins);
					((PerunBl) sess.getPerun()).getAttributesManagerBl().setAttribute(sess, user, kerberosLoginsAttr);
				}
			} catch (WrongAttributeAssignmentException | AttributeNotExistsException | ExtSourceNotExistsException | WrongAttributeValueException | WrongReferenceAttributeValueException ex) {
				throw new InternalErrorException(ex);
			}
		}

		// validate password
		super.validatePassword(sess, userLogin, user);
	}

	@Override
	public void createAlternativePassword(PerunSession sess, User user, String passwordId, String password) throws PasswordStrengthException {
		checkPasswordStrength(sess, passwordId, password);

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

		handleAltPwdManagerExit(process, new PasswordCreationFailedRuntimeException("Alternative password creation failed for " + user + ". Namespace: " + actualLoginNamespace + ", passwordId: " + passwordId + "."));

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

		handleAltPwdManagerExit(process, new PasswordDeletionFailedRuntimeException("Alternative password deletion failed for " + user + ". Namespace: " + actualLoginNamespace + ", passwordId: " + passwordId + "."));

	}

	@Override
	public void checkLoginFormat(PerunSession sess, String login) throws InvalidLoginException {

		// check login syntax/format
		((PerunBl)sess.getPerun()).getModulesUtilsBl().checkLoginNamespaceRegex(actualLoginNamespace, login, einfraLoginPattern);

		// check if login is permitted
		if (!((PerunBl)sess.getPerun()).getModulesUtilsBl().isUserLoginPermitted(actualLoginNamespace, login)) {
			log.warn("Login '{}' is not allowed in {} namespace by configuration.", login, actualLoginNamespace);
			throw new InvalidLoginException("Login '"+login+"' is not allowed in '"+actualLoginNamespace+"' namespace by configuration.");
		}

		// TODO - we will probably want to split regex checks on multiple conditions and let user know about all problems at once

	}

	@Override
	public void checkPasswordStrength(PerunSession sess, String login, String password) throws PasswordStrengthException {

		if (StringUtils.isBlank(password)) {
			log.warn("Password for {}:{} cannot be empty.", actualLoginNamespace, login);
			throw new PasswordStrengthException("Password for " + actualLoginNamespace + ":" + login + " cannot be empty.");
		}

		if (password.length() < 10) {
			log.warn("Password for {}:{} is too short. At least 10 characters are required.", actualLoginNamespace, login);
			throw new PasswordStrengthException("Password for " + actualLoginNamespace + ":" + login + " is too short. At least 10 characters is required.");
		}

		// if login is at least 3 chars, test if its not contained in password
		if (login != null && login.length() > 2) {
			String backwardsLogin = StringUtils.reverse(login);
			if (password.toLowerCase().contains(login.toLowerCase()) ||
					password.toLowerCase().contains(backwardsLogin.toLowerCase())) {
				log.warn("Password for {}:{} must not match/contain login or backwards login.", actualLoginNamespace, login);
				throw new PasswordStrengthException("Password for " + actualLoginNamespace + ":" + login + " must not match/contain login or backwards login.");
			}
		}

		// TODO - fetch user and get names to make sure they are not part of password

		if (!StringUtils.isAsciiPrintable(password)) {
			log.warn("Password for {}:{} must contain only printable characters.", actualLoginNamespace, login);
			throw new PasswordStrengthException("Password for " + actualLoginNamespace + ":" + login + " must contain only printable characters.");
		}

		// check that it contains at least 3 groups of 4
		int groupsCounter = 0;
		if (einfraPasswordContainsDigit.matcher(password).matches()) groupsCounter++;
		if (einfraPasswordContainsUpper.matcher(password).matches()) groupsCounter++;
		if (einfraPasswordContainsLower.matcher(password).matches()) groupsCounter++;
		if (einfraPasswordContainsSpec.matcher(password).matches()) groupsCounter++;

		if (groupsCounter < 3) {
			log.warn("Password for {}:{} is too weak. It has to contain at least 3 kinds of characters from: lower-case letter, upper-case letter, digit, spec. character.", actualLoginNamespace, login);
			throw new PasswordStrengthException("Password for " + actualLoginNamespace + ":" + login + " is too weak. It has to contain at least 3 kinds of characters from: lower-case letter, upper-case letter, digit, spec. character.");
		}

	}

	private void handleAltPwdManagerExit(Process process, PerunRuntimeException exToThrow) {
		try {
			if (process.waitFor() != 0) {
				throw exToThrow;
			}
		} catch (InterruptedException e) {
			throw new InternalErrorException(e);
		}
	}

	/**
	 * Retrieve "einfra" login of the user, since its necessary for alternative password backend
	 *
	 * @param sess Session
	 * @param user user to get login for
	 * @return einfra login
	 * @throws LoginNotExistsRuntimeException if user doesn't have "einfra" login
	 */
	private String getEinfraLogin(PerunSession sess, User user) {

		String login = null;
		try {
			Attribute attribute = ((PerunBl)sess.getPerun()).getAttributesManagerBl().getAttribute(sess, user, AttributesManager.NS_USER_ATTR_DEF+":login-namespace:einfra");
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
	 * @param sess session
	 * @param user user to get mail for
	 * @return users preferred mail
	 */
	private String getMail(PerunSession sess, User user) {

		try {
			Attribute attribute = ((PerunBl)sess.getPerun()).getAttributesManagerBl().getAttribute(sess, user, AttributesManager.NS_USER_ATTR_DEF+":preferredMail");
			return attribute.valueAsString();
		} catch (WrongAttributeAssignmentException | AttributeNotExistsException e) {
			// shouldn't happen
			log.error("We couldn't retrieve mail for the {} to create/delete alternative password.", user, e);
			throw new InternalErrorException("We couldn't retrieve mail for the "+user+" to create/delete alternative password.");
		}

	}

}
