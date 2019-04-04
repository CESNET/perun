package cz.metacentrum.perun.taskslib.dao.jdbc;

import cz.metacentrum.perun.core.api.Service;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.impl.ServicesManagerImpl;
import cz.metacentrum.perun.core.impl.Utils;
import cz.metacentrum.perun.taskslib.dao.ServiceDenialDao;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.support.JdbcDaoSupport;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * Implementation on Services blocking on Facility and Destinations.
 *
 * @author Michal Karm Babacek
 * @author Pavel Zlámal
 */
@Transactional
public class ServiceDenialDaoJdbc extends JdbcDaoSupport implements ServiceDenialDao {

	@Override
	public void blockServiceOnFacility(int serviceId, int facilityId) throws InternalErrorException {
		int newBanId = Utils.getNewId(this.getJdbcTemplate(), "service_denials_id_seq");
		this.getJdbcTemplate().update("insert into service_denials(id, facility_id, service_id) values (?,?,?)", newBanId, facilityId, serviceId);
	}

	@Override
	public void blockServiceOnDestination(int serviceId, int destinationId) throws InternalErrorException {
		int newBanId = Utils.getNewId(this.getJdbcTemplate(), "service_denials_id_seq");
		this.getJdbcTemplate().update("insert into service_denials(id, destination_id, service_id) values (?,?,?)", newBanId, destinationId, serviceId);
	}

	@Override
	public List<Service> getServicesBlockedOnFacility(int facilityId) {
		return getJdbcTemplate()
			.query("select " + ServicesManagerImpl.serviceMappingSelectQuery +
					" from services left join service_denials on service_denials.service_id = services.id where service_denials.facility_id = ?",
					ServicesManagerImpl.SERVICE_MAPPER, facilityId);
	}

	@Override
	public List<Service> getServicesBlockedOnDestination(int destinationId) {
		return getJdbcTemplate()
			.query("select " + ServicesManagerImpl.serviceMappingSelectQuery +
					" from services left join service_denials on service_denials.service_id = services.id where service_denials.destination_id = ?",
					ServicesManagerImpl.SERVICE_MAPPER, destinationId);
	}

	@Override
	public List<Service> getServicesFromDestination(int destinationId) {
		List<Service> servicesFromDestination = getJdbcTemplate().query("select distinct " + ServicesManagerImpl.serviceMappingSelectQuery +
								" from services join facility_service_destinations on facility_service_destinations.service_id = services.id" +
								" where facility_service_destinations.destination_id = ?",
						ServicesManagerImpl.SERVICE_MAPPER, destinationId);
		return servicesFromDestination;
	}

	@Override
	public boolean isServiceBlockedOnFacility(int serviceId, int facilityId) {
		int denials = this.queryForInt("select count(*) from service_denials where service_id = ? and facility_id = ?", serviceId, facilityId);
		if (denials > 0) {
			return true;
		}
		return false;
	}

	@Override
	public boolean isServiceBlockedOnDestination(int serviceId, int destinationId) {
		int denials = this.queryForInt("select count(*) from service_denials where service_id = ? and destination_id = ?", serviceId, destinationId);
		if (denials > 0) {
			return true;
		}
		return false;
	}

	@Override
	public void unblockAllServicesOnFacility(int facilityId) {
		this.getJdbcTemplate().update("delete from service_denials where facility_id = ?", facilityId);
	}

	@Override
	public void unblockAllServicesOnDestination(int destinationId) {
		this.getJdbcTemplate().update("delete from service_denials where destination_id = ?", destinationId);
	}

	@Override
	public void unblockServiceOnFacility(int serviceId, int facilityId) {
		this.getJdbcTemplate().update("delete from service_denials where facility_id = ? and service_id = ?", facilityId, serviceId);
	}

	@Override
	public void unblockServiceOnDestination(int serviceId, int destinationId) {
		this.getJdbcTemplate().update("delete from service_denials where destination_id = ? and service_id = ?", destinationId, serviceId);
	}

	private int queryForInt(String sql, Object... args) throws DataAccessException {
		Integer i = getJdbcTemplate().queryForObject(sql, args, Integer.class);
		return (i != null ? i : 0);
	}

}
