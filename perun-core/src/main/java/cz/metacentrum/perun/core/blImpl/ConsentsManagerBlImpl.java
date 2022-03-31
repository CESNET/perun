package cz.metacentrum.perun.core.blImpl;

import cz.metacentrum.perun.core.api.ConsentHub;
import cz.metacentrum.perun.core.api.Facility;
import cz.metacentrum.perun.core.api.PerunSession;
import cz.metacentrum.perun.core.api.exceptions.ConsentHubExistsException;
import cz.metacentrum.perun.core.api.exceptions.ConsentHubNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.ConsentHubAlreadyRemovedException;
import cz.metacentrum.perun.core.bl.ConsentsManagerBl;
import cz.metacentrum.perun.core.bl.PerunBl;
import cz.metacentrum.perun.core.implApi.ConsentsManagerImplApi;

import java.util.List;

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
}
