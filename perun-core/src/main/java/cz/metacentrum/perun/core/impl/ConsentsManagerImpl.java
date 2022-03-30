package cz.metacentrum.perun.core.impl;

import cz.metacentrum.perun.core.api.BeansUtils;
import cz.metacentrum.perun.core.api.ConsentHub;
import cz.metacentrum.perun.core.api.PerunSession;
import cz.metacentrum.perun.core.api.exceptions.ConsentHubExistsException;
import cz.metacentrum.perun.core.api.exceptions.ConsentHubNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.implApi.ConsentsManagerImplApi;
import cz.metacentrum.perun.core.api.Facility;
import cz.metacentrum.perun.core.api.exceptions.ConsentHubAlreadyRemovedException;
import cz.metacentrum.perun.core.api.exceptions.ConsistencyErrorException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcPerunTemplate;
import org.springframework.jdbc.core.RowMapper;

import javax.sql.DataSource;
import java.util.List;

/**
 * Consents database logic.
 *
 * @author Radoslav Čerhák <r.cerhak@gmail.com>
 */
public class ConsentsManagerImpl implements ConsentsManagerImplApi {

	final static Logger log = LoggerFactory.getLogger(ConsentsManagerImpl.class);

	private static JdbcPerunTemplate jdbc;

	protected final static String consentHubMappingSelectQuery = "consent_hubs.id as consent_hubs_id, consent_hubs.name as consent_hubs_name, consent_hubs.enforce_consents as consent_hubs_enforce_consents, " +
		"consent_hubs.created_at as consent_hubs_created_at, consent_hubs.created_by as consent_hubs_created_by, consent_hubs.modified_at as consent_hubs_modified_at, " +
		"consent_hubs.modified_by as consent_hubs_modified_by, consent_hubs.created_by_uid as consent_hubs_created_by_uid, consent_hubs.modified_by_uid as consent_hubs_modified_by_uid";

	protected static final RowMapper<ConsentHub> CONSENT_HUB_MAPPER = (resultSet, i) -> {
		ConsentHub consentHub = new ConsentHub();
		consentHub.setId(resultSet.getInt("consent_hubs_id"));
		consentHub.setName(resultSet.getString("consent_hubs_name"));
		consentHub.setEnforceConsents(resultSet.getBoolean("consent_hubs_enforce_consents"));
		consentHub.setCreatedAt(resultSet.getString("consent_hubs_created_at"));
		consentHub.setCreatedBy(resultSet.getString("consent_hubs_created_by"));
		consentHub.setModifiedAt(resultSet.getString("consent_hubs_modified_at"));
		consentHub.setModifiedBy(resultSet.getString("consent_hubs_modified_by"));
		if(resultSet.getInt("consent_hubs_created_by_uid") == 0) consentHub.setCreatedByUid(null);
		else consentHub.setCreatedByUid(resultSet.getInt("consent_hubs_created_by_uid"));
		if(resultSet.getInt("consent_hubs_modified_by_uid") == 0) consentHub.setModifiedByUid(null);
		else consentHub.setModifiedByUid(resultSet.getInt("consent_hubs_modified_by_uid"));
		return consentHub;
	};

	public ConsentsManagerImpl(DataSource perunPool) {
		jdbc = new JdbcPerunTemplate(perunPool);
		jdbc.setQueryTimeout(BeansUtils.getCoreConfig().getQueryTimeout());
	}

	@Override
	public List<ConsentHub> getAllConsentHubs(PerunSession sess) {
		try {
			List<ConsentHub> consentHubs = jdbc.query("select " + consentHubMappingSelectQuery + " from consent_hubs", CONSENT_HUB_MAPPER);
			for(ConsentHub consentHub : consentHubs) {
				consentHub.setFacilities(getFacilitiesForConsentHub(consentHub));
			}
			return consentHubs;
		} catch (RuntimeException ex) {
			throw new InternalErrorException(ex);
		}
	}

	@Override
	public ConsentHub getConsentHubById(PerunSession sess, int id) throws ConsentHubNotExistsException {
		try {
			ConsentHub consentHub = jdbc.queryForObject("select " + consentHubMappingSelectQuery + " from consent_hubs where id=?", CONSENT_HUB_MAPPER, id);
			consentHub.setFacilities(getFacilitiesForConsentHub(consentHub));
			return consentHub;
		} catch (EmptyResultDataAccessException ex) {
			throw new ConsentHubNotExistsException(ex);
		} catch (RuntimeException ex) {
			throw new InternalErrorException(ex);
		}
	}

	@Override
	public ConsentHub getConsentHubByName(PerunSession sess, String name) throws ConsentHubNotExistsException {
		try {
			ConsentHub consentHub =  jdbc.queryForObject("select " + consentHubMappingSelectQuery + " from consent_hubs where name=?", CONSENT_HUB_MAPPER, name);
			consentHub.setFacilities(getFacilitiesForConsentHub(consentHub));
			return consentHub;
		} catch (EmptyResultDataAccessException ex) {
			throw new ConsentHubNotExistsException(ex);
		} catch (RuntimeException ex) {
			throw new InternalErrorException(ex);
		}
	}

	@Override
	public List<Facility> getFacilitiesForConsentHub(ConsentHub consentHub) {
		try {
			return jdbc.query("select " + FacilitiesManagerImpl.facilityMappingSelectQuery + " from facilities join consent_hubs_facilities on facilities.id=consent_hubs_facilities.facility_id" +
				" where consent_hubs_facilities.consent_hub_id=?", FacilitiesManagerImpl.FACILITY_MAPPER, consentHub.getId());
		} catch (RuntimeException ex) {
			throw new InternalErrorException(ex);
		}
	}

	@Override
	public ConsentHub getConsentHubByFacility(PerunSession sess, int facilityId) throws ConsentHubNotExistsException {
		try {
			ConsentHub consentHub = jdbc.queryForObject("select " + consentHubMappingSelectQuery + " from consent_hubs join consent_hubs_facilities on consent_hubs.id=consent_hubs_facilities.consent_hub_id where consent_hubs_facilities.facility_id=?", CONSENT_HUB_MAPPER, facilityId);
			consentHub.setFacilities(getFacilitiesForConsentHub(consentHub));
			return consentHub;
		} catch (EmptyResultDataAccessException ex) {
			throw new ConsentHubNotExistsException(ex);
		} catch (RuntimeException ex) {
			throw new InternalErrorException(ex);
		}
	}

	@Override
	public boolean consentHubExists(PerunSession sess, ConsentHub consentHub) {
		try {
			int numberOfExistences = jdbc.queryForInt("select count(1) from consent_hubs where id=?", consentHub.getId());
			if (numberOfExistences == 1) {
				return true;
			} else if (numberOfExistences > 1) {
				throw new ConsistencyErrorException("Consent hub " + consentHub + " exists more than once.");
			}
			return false;
		} catch(EmptyResultDataAccessException ex) {
			return false;
		} catch(RuntimeException ex) {
			throw new InternalErrorException(ex);
		}
	}

	@Override
	public void deleteConsentHub(PerunSession perunSession, ConsentHub consentHub) throws ConsentHubAlreadyRemovedException {
		//TODO: remove all user consents first

		try {
			jdbc.update("delete from consent_hubs_facilities where consent_hub_id=?", consentHub.getId());

			int numAffected = jdbc.update("delete from consent_hubs where id=?", consentHub.getId());
			if (numAffected == 0) throw new ConsentHubAlreadyRemovedException("ConsentHub: " + consentHub);
			log.info("ConsentHub deleted: {}", consentHub);
		} catch (RuntimeException e) {
			throw new InternalErrorException(e);
		}
	}

	@Override
	public ConsentHub createConsentHub(PerunSession sess, ConsentHub consentHub) {
		try {
			int id = Utils.getNewId(jdbc, "consent_hubs_id_seq");
			// if name not set, use facility name
			if (consentHub.getName() == null) {
				consentHub.setName(consentHub.getFacilities().get(0).getName());
			}

			jdbc.update("insert into consent_hubs(id,name,enforce_consents,created_by,created_at,modified_by,modified_at,created_by_uid,modified_by_uid) " +
					"values (?,?,?,?," + Compatibility.getSysdate() + ",?," + Compatibility.getSysdate() + ",?,?)", id, consentHub.getName(),
				consentHub.isEnforceConsents(), sess.getPerunPrincipal().getActor(), sess.getPerunPrincipal().getActor(), sess.getPerunPrincipal().getUserId(), sess.getPerunPrincipal().getUserId());
			for (Facility facility : consentHub.getFacilities()) {
				jdbc.update("insert into consent_hubs_facilities(consent_hub_id,facility_id,created_by,created_at,modified_by,modified_at,created_by_uid,modified_by_uid) " +
						"values (?,?,?," + Compatibility.getSysdate() + ",?," + Compatibility.getSysdate() + ",?,?)", id, facility.getId(),
					sess.getPerunPrincipal().getActor(), sess.getPerunPrincipal().getActor(), sess.getPerunPrincipal().getUserId(), sess.getPerunPrincipal().getUserId());
			}
			log.info("ConsentHub created: {}", consentHub);

			consentHub.setId(id);
			return consentHub;
		} catch(RuntimeException ex) {
			throw new InternalErrorException(ex);
		}
	}

	@Override
	public void updateConsentHub(PerunSession sess, ConsentHub consentHub) throws ConsentHubExistsException {
		try {
			jdbc.update("update consent_hubs set name=?, enforce_consents=?, modified_by=?, modified_by_uid=?," +
					" modified_at=" + Compatibility.getSysdate() + " where id=?", consentHub.getName(), consentHub.isEnforceConsents(),
					sess.getPerunPrincipal().getActor(), sess.getPerunPrincipal().getUserId(), consentHub.getId());
			log.info("ConsentHub with id {} updated: {}", consentHub.getId(), consentHub);

		} catch (DataIntegrityViolationException ex) {
			throw new ConsentHubExistsException("Consent hub with name " + consentHub.getName() + " already exists.");
		} catch (RuntimeException ex) {
			throw new InternalErrorException(ex);
		}
	}

	@Override
	public void checkConsentHubExists(PerunSession sess, ConsentHub consentHub) throws ConsentHubNotExistsException {
		if(!consentHubExists(sess, consentHub)) throw new ConsentHubNotExistsException("ConsentHub not exists: " + consentHub);
	}
}
