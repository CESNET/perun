package cz.metacentrum.perun.taskslib.dao.jdbc;

import java.util.ArrayList;
import java.util.List;

import org.springframework.jdbc.core.support.JdbcDaoSupport;
import org.springframework.transaction.annotation.Transactional;

import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.impl.ServicesManagerImpl;
import cz.metacentrum.perun.core.impl.Utils;
import cz.metacentrum.perun.taskslib.dao.ExecServiceDenialDao;
import cz.metacentrum.perun.taskslib.model.ExecService;

/**
 * @author Michal Karm Babacek
 */
@Transactional
public class ExecServiceDenialDaoJdbc extends JdbcDaoSupport implements ExecServiceDenialDao {

	@Override
	public void banExecServiceOnFacility(int execServiceId, int facilityId) throws InternalErrorException {
		int newBanId = Utils.getNewId(this.getJdbcTemplate(), "service_denials_id_seq");
		this.getJdbcTemplate().update("insert into service_denials(id, facility_id, exec_service_id) values (?,?,?)", newBanId, facilityId, execServiceId);
	}

	@Override
	public void banExecServiceOnDestination(int execServiceId, int destinationId) throws InternalErrorException {
		int newBanId = Utils.getNewId(this.getJdbcTemplate(), "service_denials_id_seq");
		this.getJdbcTemplate().update("insert into service_denials(id, destination_id, exec_service_id) values (?,?,?)", newBanId, destinationId, execServiceId);
	}

	@Override
	public List<ExecService> listDenialsForFacility(int facilityId) {
		List<ExecService> deniedExecServices = getJdbcTemplate()
			.query("" +
					"select " + ExecServiceDaoJdbc.execServiceMappingSelectQuery + ", " + ServicesManagerImpl.serviceMappingSelectQuery +
					" from exec_services left join service_denials on service_denials.exec_service_id = exec_services.id left join services on " +
					" services.id=exec_services.service_id where service_denials.facility_id = ?",
					new Integer[] { facilityId }, ExecServiceDaoJdbc.EXEC_SERVICE_ROWMAPPER);
		if (deniedExecServices != null) {
			return deniedExecServices;
		} else {
			return new ArrayList<ExecService>();
		}
	}

	@Override
	public List<ExecService> listDenialsForDestination(int destinationId) {
		List<ExecService> deniedExecServices = getJdbcTemplate()
			.query("" +
					"select " + ExecServiceDaoJdbc.execServiceMappingSelectQuery + ", " + ServicesManagerImpl.serviceMappingSelectQuery +
					" from exec_services left join service_denials on service_denials.exec_service_id = exec_services.id left join services on " +
					" services.id=exec_services.service_id where service_denials.destination_id = ?",
					new Integer[] { destinationId }, ExecServiceDaoJdbc.EXEC_SERVICE_ROWMAPPER);
		if (deniedExecServices != null) {
			return deniedExecServices;
		} else {
			return new ArrayList<ExecService>();
		}
	}

	@Override
	public boolean isExecServiceDeniedOnFacility(int execServiceId, int facilityId) {
		int denials = 0;
		denials = this.getJdbcTemplate().queryForInt("select count(*) from service_denials where exec_service_id = ? and facility_id = ?",
				new Object[] { execServiceId, facilityId });
		if (denials > 0) {
			return true;
		}
		return false;
	}

	@Override
	public boolean isExecServiceDeniedOnDestination(int execServiceId, int destinationId) {
		int denials = 0;
		denials = this.getJdbcTemplate().queryForInt("select count(*) from service_denials where exec_service_id = ? and destination_id = ?",
				new Object[] { execServiceId, destinationId });
		if (denials > 0) {
			return true;
		}
		return false;
	}

	@Override
	public void freeAllDenialsOnFacility(int facilityId) {
		this.getJdbcTemplate().update("delete from service_denials where facility_id = ?", facilityId);
	}

	@Override
	public void freeAllDenialsOnDestination(int destinationId) {
		this.getJdbcTemplate().update("delete from service_denials where destination_id = ?", destinationId);
	}

	@Override
	public void freeDenialOfExecServiceOnFacility(int execServiceId, int facilityId) {
		this.getJdbcTemplate().update("delete from service_denials where facility_id = ? and exec_service_id = ?", facilityId, execServiceId);
	}

	@Override
	public void freeDenialOfExecServiceOnDestination(int execServiceId, int destinationId) {
		this.getJdbcTemplate().update("delete from service_denials where destination_id = ? and exec_service_id = ?", destinationId, execServiceId);
	}

}
