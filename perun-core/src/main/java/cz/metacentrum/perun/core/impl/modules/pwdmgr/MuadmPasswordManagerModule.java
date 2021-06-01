package cz.metacentrum.perun.core.impl.modules.pwdmgr;

import cz.metacentrum.perun.core.api.PerunSession;
import cz.metacentrum.perun.core.api.exceptions.InvalidLoginException;
import cz.metacentrum.perun.core.api.exceptions.PasswordStrengthException;
import cz.metacentrum.perun.core.bl.PerunBl;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MuadmPasswordManagerModule extends GenericPasswordManagerModule {

	private final static Logger log = LoggerFactory.getLogger(MuadmPasswordManagerModule.class);

	public MuadmPasswordManagerModule() {
		this.actualLoginNamespace = "mu-adm";

		if (!binTrue.equals(this.passwordManagerProgram)) {
			passwordManagerProgram += "." + actualLoginNamespace;
		}
	}

	@Override
	public void checkLoginFormat(PerunSession sess, String login) throws InvalidLoginException {

		((PerunBl)sess.getPerun()).getModulesUtilsBl().checkLoginNamespaceRegex(actualLoginNamespace, login, GenericPasswordManagerModule.defaultLoginPattern);

		if (!((PerunBl)sess.getPerun()).getModulesUtilsBl().isUserLoginPermitted(actualLoginNamespace, login)) {
			log.warn("Login '{}' is not allowed in {} namespace by configuration.", login, actualLoginNamespace);
			throw new InvalidLoginException("Login '"+login+"' is not allowed in 'mu' namespace by configuration.");
		}

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

		if (!StringUtils.isAsciiPrintable(password)) {
			log.warn("Password for {}:{} must contain only printable characters.", actualLoginNamespace, login);
			throw new PasswordStrengthException("Password for " + actualLoginNamespace + ":" + login + " must contain only printable characters.");
		}
	}
}
