package cz.metacentrum.perun.core.impl;

import cz.metacentrum.perun.core.api.BeansUtils;
import cz.metacentrum.perun.core.api.ConsentHub;
import cz.metacentrum.perun.core.api.PerunSession;
import cz.metacentrum.perun.core.api.exceptions.ConsentHubNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.implApi.ConsentsManagerImplApi;
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

	private static JdbcPerunTemplate jdbc;

	protected final static String consentHubMappingSelectQuery = "consent_hubs.id as consent_hubs_id, consent_hubs.name as consent_hubs_name, consent_hubs.enforce_consents as consent_hubs_enforce_consents, " +
		"consent_hubs.created_at as consent_hubs_created_at, consent_hubs.created_by as consent_hubs_created_by, consent_hubs.modified_at as consent_hubs_modified_at, " +
		"consent_hubs.modified_by as consent_hubs_modified_by, consent_hubs.created_by_uid as consent_hubs_created_by_uid, consent_hubs.modified_by_uid as consent_hubs_modified_by_uid";

	protected static final RowMapper<ConsentHub> CONSENT_HUB_MAPPER = (resultSet, i) -> {
		ConsentHub consentHub = new ConsentHub();
		consentHub.setId(resultSet.getInt("consent_hubs_id"));
		consentHub.setName(resultSet.getString("consent_hubs_name"));
		consentHub.setEnforceConsents(resultSet.getBoolean("enforce_consents"));
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
			return jdbc.query("select " + consentHubMappingSelectQuery + " from consent_hubs", CONSENT_HUB_MAPPER);
		} catch (RuntimeException ex) {
			throw new InternalErrorException(ex);
		}
	}

	@Override
	public ConsentHub getConsentHubById(PerunSession sess, int id) throws ConsentHubNotExistsException {
		try {
			return jdbc.queryForObject("select " + consentHubMappingSelectQuery + " from consent_hubs where id=?", CONSENT_HUB_MAPPER, id);
		} catch(EmptyResultDataAccessException ex) {
			throw new ConsentHubNotExistsException(ex);
		} catch (RuntimeException ex) {
			throw new InternalErrorException(ex);
		}
	}

	@Override
	public ConsentHub getConsentHubByName(PerunSession sess, String name) throws ConsentHubNotExistsException {
		try {
			return jdbc.queryForObject("select " + consentHubMappingSelectQuery + " from consent_hubs where name=?", CONSENT_HUB_MAPPER, name);
		} catch(EmptyResultDataAccessException ex) {
			throw new ConsentHubNotExistsException(ex);
		} catch (RuntimeException ex) {
			throw new InternalErrorException(ex);
		}
	}
}
