package cz.metacentrum.perun.core.blImpl;

import cz.metacentrum.perun.audit.events.ConsentManager.ChangedConsentStatus;
import cz.metacentrum.perun.audit.events.ConsentManager.ConsentCreated;
import cz.metacentrum.perun.audit.events.ConsentManager.ConsentDeleted;
import cz.metacentrum.perun.core.api.BeansUtils;
import cz.metacentrum.perun.core.api.ConsentHub;
import cz.metacentrum.perun.core.api.Consent;
import cz.metacentrum.perun.core.api.ConsentStatus;
import cz.metacentrum.perun.core.api.Facility;
import cz.metacentrum.perun.core.api.Member;
import cz.metacentrum.perun.core.api.PerunSession;
import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.Resource;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.exceptions.ConsentExistsException;
import cz.metacentrum.perun.core.api.exceptions.ConsentNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.FacilityAlreadyAssigned;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.InvalidConsentStatusException;
import cz.metacentrum.perun.core.api.exceptions.RelationNotExistsException;
import cz.metacentrum.perun.core.api.Service;
import cz.metacentrum.perun.core.api.exceptions.ConsentHubAlreadyRemovedException;
import cz.metacentrum.perun.core.api.exceptions.ConsentHubExistsException;
import cz.metacentrum.perun.core.api.exceptions.ConsentHubNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.ConsistencyErrorException;
import cz.metacentrum.perun.core.api.exceptions.UserNotExistsException;
import cz.metacentrum.perun.core.bl.ConsentsManagerBl;
import cz.metacentrum.perun.core.bl.PerunBl;
import cz.metacentrum.perun.core.impl.PerunLocksUtils;
import cz.metacentrum.perun.core.impl.Utils;
import cz.metacentrum.perun.core.implApi.ConsentsManagerImplApi;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toMap;

/**
 * Consents BL logic.
 *
 * @author Radoslav Čerhák <r.cerhak@gmail.com>
 */
public class ConsentsManagerBlImpl implements ConsentsManagerBl {

	private final ConsentsManagerImplApi consentsManagerImpl;
	private PerunBl perunBl;

	public ConsentsManagerBlImpl(ConsentsManagerImplApi consentsManagerImpl) {
		this.consentsManagerImpl = consentsManagerImpl;
	}

	public ConsentsManagerImplApi getConsentsManagerImpl() {
		return this.consentsManagerImpl;
	}

	public PerunBl getPerunBl() {
		return this.perunBl;
	}

	public void setPerunBl(PerunBl perunBl) {
		this.perunBl = perunBl;
	}


	@Override
	public Consent createConsent(PerunSession sess, Consent consent) throws ConsentExistsException, ConsentHubNotExistsException, UserNotExistsException {
		Utils.notNull(consent, "consent");

		// Check if UNSIGNED consent exists and delete it
		try {
			Consent oldConsent =  getConsentForUserAndConsentHub(sess, consent.getUserId(), consent.getConsentHub().getId(), ConsentStatus.UNSIGNED);
			if (oldConsent != null) {
				deleteConsent(sess, oldConsent);
			}
		} catch (ConsentNotExistsException e) {
			// OK
		}

		// Add attributes from ConsentHub
		ConsentHub consentHub = getConsentHubById(sess, consent.getConsentHub().getId());
		User user = perunBl.getUsersManagerBl().getUserById(sess, consent.getUserId());
		Set<AttributeDefinition> filter = new HashSet<>();
		for (Facility facility : consentHub.getFacilities()) {
			for (Resource resource : perunBl.getUsersManagerBl().getAssignedResources(sess, facility, user)) {
				for (Service service : perunBl.getResourcesManagerBl().getAssignedServices(sess, resource)) {
					for (AttributeDefinition attr : perunBl.getAttributesManagerBl().getRequiredAttributesDefinition(sess, service)) {
						if (Utils.isUserRelatedAttribute(attr)) {
							filter.add(attr);
						}
					}
				}
			}
		}
		consent.setAttributes(filter.stream().toList());

		consent = getConsentsManagerImpl().createConsent(sess, consent);
		getPerunBl().getAuditer().log(sess, new ConsentCreated(consent));

		return consent;
	}

	@Override
	public void deleteConsent(PerunSession sess, Consent consent) throws ConsentNotExistsException {
		Utils.notNull(consent, "consent");

		getConsentsManagerImpl().deleteConsent(sess, consent);
		getPerunBl().getAuditer().log(sess, new ConsentDeleted(consent));
	}


	@Override
	public List<Consent> getAllConsents(PerunSession sess) {
		return consentsManagerImpl.getAllConsents(sess);
	}


	@Override
	public List<Consent> getConsentsForConsentHub(PerunSession sess, int id, ConsentStatus status) {
		return consentsManagerImpl.getConsentsForConsentHub(sess, id, status);
	}

	@Override
	public List<Consent> getConsentsForConsentHub(PerunSession sess, int id) {
		return consentsManagerImpl.getConsentsForConsentHub(sess, id);
	}

	@Override
	public List<Consent> getConsentsForUser(PerunSession sess, int id, ConsentStatus status) {
		return consentsManagerImpl.getConsentsForUser(sess, id, status);
	}

	@Override
	public List<Consent> getConsentsForUser(PerunSession sess, int id) {
		return consentsManagerImpl.getConsentsForUser(sess, id);
	}

	@Override
	public List<Consent> getConsentsForUserAndConsentHub(PerunSession sess, int userId, int consentHubId) {
		return consentsManagerImpl.getConsentsForUserAndConsentHub(sess, userId, consentHubId);
	}

	@Override
	public Consent getConsentForUserAndConsentHub(PerunSession sess, int userId, int consentHubId, ConsentStatus status) throws ConsentNotExistsException {
		return consentsManagerImpl.getConsentForUserAndConsentHub(sess, userId, consentHubId, status);
	}

	@Override
	public Consent getConsentById(PerunSession sess, int id) throws ConsentNotExistsException {
		return consentsManagerImpl.getConsentById(sess, id);
	}

	@Override
	public void checkConsentExists(PerunSession sess, Consent consent) throws ConsentNotExistsException {
		consentsManagerImpl.checkConsentExists(sess, consent);
	}

	@Override
	public List<ConsentHub> getAllConsentHubs(PerunSession sess) {
		return getConsentsManagerImpl().getAllConsentHubs(sess);
	}

	@Override
	public ConsentHub getConsentHubById(PerunSession sess, int id) throws ConsentHubNotExistsException {
		return getConsentsManagerImpl().getConsentHubById(sess, id);
	}

	@Override
	public ConsentHub getConsentHubByName(PerunSession sess, String name) throws ConsentHubNotExistsException {
		return getConsentsManagerImpl().getConsentHubByName(sess, name);
	}

	@Override
	public ConsentHub getConsentHubByFacility(PerunSession sess, int facilityId) throws ConsentHubNotExistsException {
		return getConsentsManagerImpl().getConsentHubByFacility(sess, facilityId);
	}

	@Override
	public ConsentHub createConsentHub(PerunSession sess, ConsentHub consentHub) throws ConsentHubExistsException{
		if (consentHubExists(sess, consentHub)) {
			throw new ConsentHubExistsException(consentHub);
		}

		return getConsentsManagerImpl().createConsentHub(sess, consentHub);
	}

	@Override
	public void deleteConsentHub(PerunSession sess, ConsentHub consentHub) throws ConsentHubAlreadyRemovedException {

		// Remove all consents for this ConsentHub
		for (Consent consent : getConsentsForConsentHub(sess, consentHub.getId())) {
			try {
				deleteConsent(sess, consent);
			} catch (ConsentNotExistsException e) {
				// IGNORE
			}
		}

		getConsentsManagerImpl().deleteConsentHub(sess, consentHub);
	}

	@Override
	public boolean consentHubExists(PerunSession sess, ConsentHub consentHub) {
		return getConsentsManagerImpl().consentHubExists(sess, consentHub);
	}

	@Override
	public void checkConsentHubExists(PerunSession sess, ConsentHub consentHub) throws ConsentHubNotExistsException {
		getConsentsManagerImpl().checkConsentHubExists(sess, consentHub);
	}

	@Override
	public ConsentHub updateConsentHub(PerunSession perunSession, ConsentHub consentHub) throws ConsentHubExistsException {
		getConsentsManagerImpl().updateConsentHub(perunSession, consentHub);
		try {
			return getConsentHubById(perunSession, consentHub.getId());
		} catch (ConsentHubNotExistsException e) {
			throw new ConsistencyErrorException("Updated consentHub " + consentHub + " was not found!");
		}
	}

	@Override
	public void addFacility(PerunSession sess, ConsentHub consentHub, Facility facility) throws FacilityAlreadyAssigned {
		getConsentsManagerImpl().addFacility(sess, consentHub, facility);
	}

	@Override
	public void removeFacility(PerunSession sess, ConsentHub consentHub, Facility facility) throws RelationNotExistsException, ConsentHubAlreadyRemovedException {
		getConsentsManagerImpl().removeFacility(sess, consentHub, facility);
		if (getConsentsManagerImpl().getFacilitiesForConsentHub(consentHub).size() == 0) {
			getConsentsManagerImpl().deleteConsentHub(sess, consentHub);
		}
	}

	@Override
	public Consent changeConsentStatus(PerunSession sess, Consent consent, ConsentStatus status) throws InvalidConsentStatusException {
		if (status != ConsentStatus.GRANTED && status != ConsentStatus.REVOKED) {
			throw new InvalidConsentStatusException("Invalid consent status value.");
		}
		if (status == consent.getStatus()) {
			throw new InvalidConsentStatusException("Tried to set consent status on current value.");
		}
		consent.setStatus(status);

		checkExistingConsents(sess, consent);
		getConsentsManagerImpl().changeConsentStatus(sess, consent);
		getPerunBl().getAuditer().log(sess, new ChangedConsentStatus(consent));
		try {
			return getConsentById(sess, consent.getId());
		} catch (ConsentNotExistsException e) {
			throw new InternalErrorException(e);
		}
	}

	private void checkExistingConsents(PerunSession sess, Consent consent) {
		for (Consent currentConsent : getConsentsForUserAndConsentHub(sess, consent.getUserId(), consent.getConsentHub().getId())) {
			if (currentConsent.getId() != consent.getId() && currentConsent.getStatus() != ConsentStatus.UNSIGNED) {
				try {
					deleteConsent(sess, currentConsent);
				} catch (ConsentNotExistsException e) {
					throw new InternalErrorException(e);
				}
			}
		}
	}

	@Override
	public List<Member> evaluateConsents(PerunSession sess, Service service, Facility facility, List<Member> members) {
		if (!BeansUtils.getCoreConfig().getForceConsents()) {
			return members;
		}

		ConsentHub consentHub;
		try {
			consentHub = this.getConsentHubByFacility(sess, facility.getId());
		} catch (ConsentHubNotExistsException e) {
			throw new ConsistencyErrorException("Facility " + facility + " is not in any consent hub.", e);
		}
		if (!consentHub.isEnforceConsents()) {
			return members;
		}

		PerunLocksUtils.lockConsentHub(consentHub);

		List<AttributeDefinition> requiredAttributes = getPerunBl().getAttributesManagerBl()
			.getRequiredAttributesDefinition(sess, service);
		requiredAttributes.removeIf(attrDef -> !Utils.isUserRelatedAttribute(attrDef));
		if (requiredAttributes.isEmpty()) {
			return members;
		}

		List<Consent> consents = this.getConsentsForConsentHub(sess, consentHub.getId());
		// map of users ids to map of their consents (consent status to the user's consent with such status)
		Map<Integer, Map<ConsentStatus, Consent>> userIdToConsents = consents.stream()
			.collect(groupingBy(Consent::getUserId, toMap(Consent::getStatus, Function.identity())));

		return members.stream()
			.filter(member -> hasValidConsent(sess, member, consentHub, requiredAttributes, userIdToConsents.get(member.getUserId())))
			.toList();
	}

	/**
	 * Returns true if the given member has a consent for the consent hub with
	 * status GRANTED and it contains all the required attributes.
	 *
	 * If the member has no consent in any status that contains all the required attributes,
	 * a new UNSIGNED consent is created for the user.
	 *
	 * @param sess perun session
	 * @param member the member to check
	 * @param consentHub the consent hub
	 * @param requiredAttributes a list of attributes a consent should contain
	 * @param usersConsents map of user's consents (consent status to the user's consent witch such status)
	 * @return true if the given member has a valid consent
	 */
	private boolean hasValidConsent(PerunSession sess, Member member, ConsentHub consentHub, List<AttributeDefinition> requiredAttributes,
									Map<ConsentStatus, Consent> usersConsents) {
		if (usersConsents == null || usersConsents.isEmpty()) {
			try {
				this.createConsent(sess, new Consent(-1, member.getUserId(), consentHub, null));
			} catch (ConsentExistsException | ConsentHubNotExistsException | UserNotExistsException e) {
				throw new InternalErrorException(e);
			}
			return false;
		}
		Consent grantedConsent = usersConsents.get(ConsentStatus.GRANTED);
		Consent unsignedConsent = usersConsents.get(ConsentStatus.UNSIGNED);
		Consent revokedConsent = usersConsents.get(ConsentStatus.REVOKED);

		if (grantedConsent != null && grantedConsent.getAttributes().containsAll(requiredAttributes)) {
			return true;
		} else if (revokedConsent != null && revokedConsent.getAttributes().containsAll(requiredAttributes)) {
			return false;
		} else if (unsignedConsent == null || !unsignedConsent.getAttributes().containsAll(requiredAttributes)) {
			try {
				this.createConsent(sess, new Consent(-1, member.getUserId(), consentHub, null));
			} catch (ConsentExistsException | ConsentHubNotExistsException | UserNotExistsException e) {
				throw new InternalErrorException(e);
			}
			return false;
		}

		return false;
	}
}
