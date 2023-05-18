package cz.metacentrum.perun.core.impl;

import cz.metacentrum.perun.core.api.BeansUtils;
import cz.metacentrum.perun.core.api.ExtSourcesManager;
import cz.metacentrum.perun.core.api.PerunClient;
import cz.metacentrum.perun.core.api.PerunPrincipal;
import cz.metacentrum.perun.core.api.PerunSession;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.bl.PerunBl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;

/**
 * This component checks that none of the default blocked logins is already used.
 *
 * @author Sarka Palkovicova
 */
public class DefaultBlockedLoginChecker {
	private static final Logger log = LoggerFactory.getLogger(DefaultBlockedLoginChecker.class);
	private final PerunSession sess;
	private PerunBl perunBl;

	public DefaultBlockedLoginChecker(PerunBl perunBl) {
		String synchronizerPrincipal = "perunDefaultBlockedLoginChecker";
		this.sess = perunBl.getPerunSession(
			new PerunPrincipal(synchronizerPrincipal, ExtSourcesManager.EXTSOURCE_NAME_INTERNAL, ExtSourcesManager.EXTSOURCE_INTERNAL),
			new PerunClient());
		this.perunBl = perunBl;
	}

	public PerunBl getPerunBl() {
		return perunBl;
	}

	public void setPerunBl(PerunBl perunBl) {
		this.perunBl = perunBl;
	}

	public void checkDefaultBlockedLogins() {
		log.debug("DefaultBlockedLoginChecker starts checking default blocked logins.");

		Set<String> logins = BeansUtils.getCoreConfig().getBlockedLogins();
		for (String login : logins) {
			if (perunBl.getAttributesManagerBl().isLoginAlreadyUsed(sess, login, null)) {
				log.error("Login {} can not be blocked by default because it is already used.", login);
				throw new InternalErrorException("Login " + login + " can not be blocked by default because it is already used. Please edit the core config!");
			}
		}
	}

}
