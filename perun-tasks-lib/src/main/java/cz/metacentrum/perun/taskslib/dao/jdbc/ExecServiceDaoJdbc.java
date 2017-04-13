package cz.metacentrum.perun.taskslib.dao.jdbc;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.support.JdbcDaoSupport;
import org.springframework.transaction.annotation.Transactional;

import cz.metacentrum.perun.core.api.Service;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.impl.ServicesManagerImpl;
import cz.metacentrum.perun.core.impl.Utils;
import cz.metacentrum.perun.taskslib.dao.ExecServiceDao;
import cz.metacentrum.perun.taskslib.model.ExecService;
import cz.metacentrum.perun.taskslib.model.ExecService.ExecServiceType;

/**
 *
 * @author Michal Karm Babacek
 *         JavaDoc coming soon...
 *
 */
@Transactional
public class ExecServiceDaoJdbc extends JdbcDaoSupport implements ExecServiceDao {

	public final static String execServiceMappingSelectQuery = " exec_services.id as exec_services_id, exec_services.default_delay as exec_s_default_delay," +
		" exec_services.default_recurrence as exec_s_default_recurrence, exec_services.enabled as exec_services_enabled," +
		" exec_services.script as exec_services_script, exec_services.type as exec_services_type, " +
		" exec_services.service_id as exec_services_service_id ";

	public static final RowMapper<ExecService> EXEC_SERVICE_ROWMAPPER = new RowMapper<ExecService>() {

		public ExecService mapRow(ResultSet rs, int i) throws SQLException {

			ExecService execService = new ExecService();
			execService.setId(rs.getInt("exec_services_id"));
			execService.setDefaultDelay(rs.getInt("exec_s_default_delay"));
			execService.setDefaultRecurrence(rs.getInt("exec_s_default_recurrence"));
			char enabled = rs.getString("exec_services_enabled").charAt(0);
			if (enabled == '0') {
				execService.setEnabled(false);
			} else {
				execService.setEnabled(true);
			}

			execService.setScript(rs.getString("exec_services_script"));
			if (rs.getString("exec_services_type").equalsIgnoreCase(ExecServiceType.GENERATE.toString())) {
				execService.setExecServiceType(ExecServiceType.GENERATE);
			} else if (rs.getString("exec_services_type").equalsIgnoreCase(ExecServiceType.SEND.toString())) {
				execService.setExecServiceType(ExecServiceType.SEND);
			} else {
				throw new IllegalArgumentException("ExecService type unknown :-(");
			}

			execService.setService(ServicesManagerImpl.SERVICE_MAPPER.mapRow(rs, i));

			return execService;
		}

	};

	@Override
	public int insertExecService(ExecService execService) throws InternalErrorException {
		char enabled = '0';
		if (execService.isEnabled()) {
			enabled = '1';
		}
		int newExecServiceId = Utils.getNewId(this.getJdbcTemplate(), "exec_services_id_seq");
		this.getJdbcTemplate().update("insert into exec_services(id, default_delay, default_recurrence, enabled, script, type, service_id) values (?,?,?,?,?,?,?)", newExecServiceId,
				execService.getDefaultDelay(), execService.getDefaultRecurrence(), "" + enabled, execService.getScript(), execService.getExecServiceType().toString(),
				execService.getService().getId());

		return newExecServiceId;
	}

	@Override
	public List<ExecService> listExecServices() {
		return this.getJdbcTemplate().query(
				"select " + execServiceMappingSelectQuery + ", " + ServicesManagerImpl.serviceMappingSelectQuery +
				" from exec_services left join services on exec_services.service_id=services.id", EXEC_SERVICE_ROWMAPPER);
	}

	@Override
	public List<ExecService> listExecServices(int serviceId) {
		return this.getJdbcTemplate().query(
				"select " + execServiceMappingSelectQuery + ", " + ServicesManagerImpl.serviceMappingSelectQuery  +
				" from exec_services left join services on exec_services.service_id=services.id where service_id = ?",
				new Integer[] { serviceId }, ExecServiceDaoJdbc.EXEC_SERVICE_ROWMAPPER);
	}

	private int queryForInt(String sql, Object... args) throws DataAccessException {
		Integer i = getJdbcTemplate().queryForObject(sql, args, Integer.class);
		return (i != null ? i : 0);
	}

	@Override
	public int countExecServices() {
		return queryForInt("select count(*) from exec_services");
	}

	@Override
	public ExecService getExecService(int execServiceId) {
		try {
			return this.getJdbcTemplate().queryForObject(
					"select " + execServiceMappingSelectQuery + ", " + ServicesManagerImpl.serviceMappingSelectQuery  +
					" from exec_services left join services on exec_services.service_id=services.id where exec_services.id = ?",
					new Integer[] { execServiceId }, EXEC_SERVICE_ROWMAPPER);
		} catch (EmptyResultDataAccessException ex) {
			return null;
		}
	}

	@Override
	public void updateExecService(ExecService execService) {
		char enabled = '0';
		if (execService.isEnabled()) {
			enabled = '1';
		}
		this.getJdbcTemplate().update("update exec_services set default_delay = ?, default_recurrence = ?, enabled = ?, script = ?, type = ?, service_id = ? where id = ?",
				execService.getDefaultDelay(), execService.getDefaultRecurrence(), "" + enabled, execService.getScript(), execService.getExecServiceType().toString(),
				execService.getService().getId(), execService.getId());
	}

	@Override
	public void deleteExecService(int execServiceId) {
		this.getJdbcTemplate().update("delete from exec_services where id = ?", execServiceId);
	}

	@Override
	public void deleteAllExecServicesByService(int serviceId) {
		this.getJdbcTemplate().update("delete from exec_services where service_id = ?", serviceId);
	}

}
