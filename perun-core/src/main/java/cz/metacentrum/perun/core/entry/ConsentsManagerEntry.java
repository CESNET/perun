package cz.metacentrum.perun.core.entry;

import cz.metacentrum.perun.core.api.AuthzResolver;
import cz.metacentrum.perun.core.api.ConsentsManager;
import cz.metacentrum.perun.core.api.ConsentHub;
import cz.metacentrum.perun.core.api.Facility;
import cz.metacentrum.perun.core.api.PerunSession;
import cz.metacentrum.perun.core.api.exceptions.ConsentHubNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.PrivilegeException;
import cz.metacentrum.perun.core.bl.ConsentsManagerBl;
import cz.metacentrum.perun.core.bl.PerunBl;
import cz.metacentrum.perun.core.impl.Utils;

import java.util.List;

/**
 * Consents entry logic.
 *
 * @author Radoslav Čerhák <r.cerhak@gmail.com>
 */
public class ConsentsManagerEntry implements ConsentsManager {

	private ConsentsManagerBl consentsManagerBl;
	private PerunBl perunBl;

	public ConsentsManagerEntry(PerunBl perunBl) {
		this.perunBl = perunBl;
		this.consentsManagerBl = perunBl.getConsentsManagerBl();
	}

	public ConsentsManagerEntry() {}

	public ConsentsManagerBl getConsentsManagerBl() {
		return this.consentsManagerBl;
	}

	public void setConsentsManagerBl(ConsentsManagerBl consentsManagerBl) {
		this.consentsManagerBl = consentsManagerBl;
	}

	public PerunBl getPerunBl() {
		return this.perunBl;
	}

	public void setPerunBl(PerunBl perunBl) {
		this.perunBl = perunBl;
	}

	@Override
	public List<ConsentHub> getAllConsentHubs(PerunSession sess) throws PrivilegeException {
		Utils.notNull(sess, "sess");

		// Authorization
		if (!AuthzResolver.authorizedInternal(sess, "getAllConsentHubs_policy")) {
			throw new PrivilegeException(sess, "getAllConsentHubs");
		}
		return consentsManagerBl.getAllConsentHubs(sess);
	}

	@Override
	public ConsentHub getConsentHubById(PerunSession sess, int id) throws ConsentHubNotExistsException, PrivilegeException {
		Utils.notNull(sess, "sess");

		ConsentHub consentHub = consentsManagerBl.getConsentHubById(sess, id);
		List<Facility> facilities = consentHub.getFacilities();
		facilities.removeIf(facility -> !AuthzResolver.authorizedInternal(sess, "getConsentHubById_int_policy", facility));

		// Authorization
		if (facilities.isEmpty()) {
			throw new PrivilegeException(sess, "getConsentHubById");
		}

		return consentHub;
	}

	@Override
	public ConsentHub getConsentHubByName(PerunSession sess, String name) throws ConsentHubNotExistsException, PrivilegeException {
		Utils.notNull(sess, "sess");
		Utils.notNull(name, "name");

		ConsentHub consentHub = consentsManagerBl.getConsentHubByName(sess, name);
		List<Facility> facilities = consentHub.getFacilities();
		facilities.removeIf(facility -> !AuthzResolver.authorizedInternal(sess, "getConsentHubById_int_policy", facility));

		// Authorization
		if (facilities.isEmpty()) {
			throw new PrivilegeException(sess, "getConsentHubByName");
		}

		return consentHub;
	}

}
