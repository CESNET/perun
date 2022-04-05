package cz.metacentrum.perun.core.entry;

import cz.metacentrum.perun.core.api.Consent;
import cz.metacentrum.perun.core.api.ConsentStatus;
import cz.metacentrum.perun.core.api.AuthzResolver;
import cz.metacentrum.perun.core.api.ConsentsManager;
import cz.metacentrum.perun.core.api.PerunSession;
import cz.metacentrum.perun.core.api.Facility;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.exceptions.ConsentHubNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.ConsentHubExistsException;
import cz.metacentrum.perun.core.api.exceptions.ConsentNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.PrivilegeException;
import cz.metacentrum.perun.core.api.exceptions.UserNotExistsException;
import cz.metacentrum.perun.core.api.ConsentHub;
import cz.metacentrum.perun.core.api.exceptions.FacilityNotExistsException;
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
	public List<Consent> getAllConsents(PerunSession sess) throws PrivilegeException {
		Utils.checkPerunSession(sess);

		// auth
		if (!AuthzResolver.authorizedInternal(sess, "getAllConsents_policy")) {
			throw new PrivilegeException("getAllConsents");
		}

		return consentsManagerBl.getAllConsents(sess);
	}

	@Override
	public List<Consent> getConsentsForConsentHub(PerunSession sess, int id, ConsentStatus status) throws PrivilegeException, ConsentHubNotExistsException {
		Utils.checkPerunSession(sess);

		ConsentHub consentHub = getPerunBl().getConsentsManagerBl().getConsentHubById(sess, id);
		// auth
		if (consentHub.getFacilities().stream().noneMatch(facility -> AuthzResolver.authorizedInternal(sess, "getConsentsForConsentHub_int_ConsentStatus_policy", facility))) {
			throw new PrivilegeException("getConsentsForConsentHub");
		}

		return consentsManagerBl.getConsentsForConsentHub(sess, id, status);
	}

	@Override
	public List<Consent> getConsentsForConsentHub(PerunSession sess, int id) throws PrivilegeException, ConsentHubNotExistsException {
		Utils.checkPerunSession(sess);

		ConsentHub consentHub = getPerunBl().getConsentsManagerBl().getConsentHubById(sess, id);
		// auth
		if (consentHub.getFacilities().stream().noneMatch(facility -> AuthzResolver.authorizedInternal(sess, "getConsentsForConsentHub_int_policy", facility))) {
			throw new PrivilegeException("getConsentsForConsentHub");
		}

		return consentsManagerBl.getConsentsForConsentHub(sess, id);
	}

	@Override
	public List<Consent> getConsentsForUser(PerunSession sess, int id, ConsentStatus status) throws UserNotExistsException, PrivilegeException {
		Utils.checkPerunSession(sess);

		User user = getPerunBl().getUsersManager().getUserById(sess, id);

		// auth
		if (!AuthzResolver.authorizedInternal(sess, "getConsentsForUser_int_ConsentStatus_policy", user)) {
			throw new PrivilegeException("getConsentsForUser");
		}

		List<Consent> consents = consentsManagerBl.getConsentsForUser(sess, id, status);
		consents.removeIf(consent -> consent.getConsentHub().getFacilities().stream().noneMatch(facility -> AuthzResolver.authorizedInternal(sess, "filter-getConsentsForUser_policy", facility, user)));


		return consents;
	}

	@Override
	public List<Consent> getConsentsForUser(PerunSession sess, int id) throws UserNotExistsException, PrivilegeException {
		Utils.checkPerunSession(sess);

		User user = getPerunBl().getUsersManager().getUserById(sess, id);

		// auth
		if (!AuthzResolver.authorizedInternal(sess, "getConsentsForUser_int_policy", user)) {
			throw new PrivilegeException("getConsentsForUser");
		}

		List<Consent> consents = consentsManagerBl.getConsentsForUser(sess, id);
		consents.removeIf(consent -> consent.getConsentHub().getFacilities().stream().noneMatch(facility -> AuthzResolver.authorizedInternal(sess, "filter-getConsentsForUser_policy", facility, user)));

		return consents;
	}

	@Override
	public List<Consent> getConsentsForUserAndConsentHub(PerunSession sess, int userId, int consentHubId) throws PrivilegeException, UserNotExistsException, ConsentHubNotExistsException {
		Utils.checkPerunSession(sess);

		User user = getPerunBl().getUsersManager().getUserById(sess, userId);
		ConsentHub consentHub = getPerunBl().getConsentsManagerBl().getConsentHubById(sess, consentHubId);

		// auth
		if (!AuthzResolver.authorizedInternal(sess, "getConsentsForUserAndConsentHub_int_int_policy", user)) {
			throw new PrivilegeException("getConsentsForUserAndConsentHub");
		}
		List<Consent> consents = consentsManagerBl.getConsentsForUserAndConsentHub(sess, userId, consentHubId);
		consents.removeIf(consent -> consent.getConsentHub().getFacilities().stream().noneMatch(facility -> AuthzResolver.authorizedInternal(sess, "filter-getConsentsForUserAndConsentHub_int_int_policy", facility, user)));

		return consents;
	}

	@Override
	public Consent getConsentForUserAndConsentHub(PerunSession sess, int userId, int consentHubId, ConsentStatus status) throws PrivilegeException, UserNotExistsException, ConsentHubNotExistsException, ConsentNotExistsException {
		Utils.checkPerunSession(sess);

		User user = getPerunBl().getUsersManager().getUserById(sess, userId);
		ConsentHub consentHub = getPerunBl().getConsentsManagerBl().getConsentHubById(sess, consentHubId);

		// auth
		if (consentHub.getFacilities().stream().noneMatch(facility -> AuthzResolver.authorizedInternal(sess, "getConsentForUserAndConsentHub_int_int_ConsentStatus_policy", facility, user))) {
			throw new PrivilegeException("getConsentsForUserAndConsentHub");
		}

		return consentsManagerBl.getConsentForUserAndConsentHub(sess, userId, consentHubId, status);
	}

	@Override
	public Consent getConsentById(PerunSession sess, int id) throws ConsentNotExistsException, PrivilegeException {
		Utils.checkPerunSession(sess);

		Consent consent = consentsManagerBl.getConsentById(sess, id);
		User user;
		try {
			user = getPerunBl().getUsersManager().getUserById(sess, consent.getUserId());
		} catch (UserNotExistsException ex) {
			throw new InternalErrorException(ex);
		}

		// auth
		if (consent.getConsentHub().getFacilities().stream().noneMatch(facility -> AuthzResolver.authorizedInternal(sess, "getConsentById_int_policy", facility, user))) {
			throw new PrivilegeException("getConsentById");
		}

		return consent;
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

		// Authorization
		if (!AuthzResolver.authorizedInternal(sess, "getConsentHubById_int_policy")) {
			throw new PrivilegeException(sess, "getConsentHubById");
		}

		// Block of code prepared for manage FACILITY ADMIN/OBSERVER roles
		// Don't forget to check roles in perun-roles.yml
		/*ConsentHub consentHub = consentsManagerBl.getConsentHubById(sess, id);
		List<Facility> facilities = consentHub.getFacilities();
		facilities.removeIf(facility -> !AuthzResolver.authorizedInternal(sess, "filter-getConsentHub_policy", facility));

		// Authorization
		if (facilities.isEmpty()) {
			throw new PrivilegeException(sess, "getConsentHubById");
		}*/

		return consentsManagerBl.getConsentHubById(sess, id);
	}

	@Override
	public ConsentHub getConsentHubByName(PerunSession sess, String name) throws ConsentHubNotExistsException, PrivilegeException {
		Utils.notNull(sess, "sess");
		Utils.notNull(name, "name");

		// Authorization
		if (!AuthzResolver.authorizedInternal(sess, "getConsentHubByName_String_policy")) {
			throw new PrivilegeException(sess, "getConsentHubByName");
		}

		// Block of code prepared for manage FACILITY ADMIN/OBSERVER roles
		// Don't forget to check roles in perun-roles.yml
		/*ConsentHub consentHub = consentsManagerBl.getConsentHubByName(sess, name);
		List<Facility> facilities = consentHub.getFacilities();
		facilities.removeIf(facility -> !AuthzResolver.authorizedInternal(sess, "filter-getConsentHub_policy", facility));

		// Authorization
		if (facilities.isEmpty()) {
			throw new PrivilegeException(sess, "getConsentHubByName");
		}*/

		return consentsManagerBl.getConsentHubByName(sess, name);
	}

	@Override
	public ConsentHub getConsentHubByFacility(PerunSession sess, int facilityId) throws ConsentHubNotExistsException, PrivilegeException, FacilityNotExistsException {
		Utils.notNull(sess, "sess");

		// Authorization
		if (!AuthzResolver.authorizedInternal(sess, "getConsentHubByFacility_Facility_policy", perunBl.getFacilitiesManagerBl().getFacilityById(sess, facilityId))) {
			throw new PrivilegeException(sess, "getConsentHubByFacility");
		}

		return consentsManagerBl.getConsentHubByFacility(sess, facilityId);
	}

	@Override
	public ConsentHub updateConsentHub(PerunSession sess, ConsentHub consentHub) throws ConsentHubNotExistsException, PrivilegeException, ConsentHubExistsException {
		Utils.checkPerunSession(sess);
		Utils.notNull(consentHub.getName(), "consentHub.name");
		if (consentHub.getName().isEmpty()) {
			throw new InternalErrorException("ConsentHub name to be updated cannot be empty.");
		}

		getConsentsManagerBl().checkConsentHubExists(sess, consentHub);

		//Authorization
		List<Facility> facilities = consentHub.getFacilities();
		for (Facility facility : facilities) {
			if (!AuthzResolver.authorizedInternal(sess, "updateConsentHub_ConsentHub_policy", facility)) {
				throw new PrivilegeException(sess, "updateConsentHub");
			}
		}

		return getConsentsManagerBl().updateConsentHub(sess, consentHub);
	}
}